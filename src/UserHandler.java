import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.PriorityQueue;

/**
 * 
 */

/**
 * @author sebas
 *
 */
public class UserHandler implements Runnable{

	/**
	 * This field represents a version of this class, so we don't accidentally try to load
	 * an object from a previous version of this class (that would be a Bad Thing).
	 * 
	 * Every time this class is modified, the UID should be incremented.
	 */
	private static final long serialVersionUID = 0L;

	private Socket userSocket;

	private User user;

	private Thread listeningThread;

	private Thread sendingThread;

	private boolean bRun = true;

	/**
	 * 
	 */
	private ArrayList<UserHandler> currentUserHandlers = new ArrayList<UserHandler>();

	private PriorityQueue<Message> outgoingMessages = new PriorityQueue<Message>();

	UserHandler(Socket connectionSocket, User user, ArrayList<UserHandler> currentUsers){
		this.userSocket = connectionSocket;
		this.user = user;
		this.currentUserHandlers = currentUsers;

		listeningThread= new Thread(this, "listen");//create a thread
		listeningThread.start(); //start the thread
		sendingThread = new Thread(this, "send");
		sendingThread.start();

		sendConnectionAck();
		sendUserStatusChange(true);
	}

	public void sendConnectionAck() {
		Message ackMessage = new Message(MessageType.CONNECTION_ACKNOWLEDGEMENT_MESSAGE, User.SERVER, null, null);
		outgoingMessages.add(ackMessage);
	}

	public void sendDisconnAck(){
		Message ackMessage = new Message(MessageType.DISCONNECT_ACKNOWLEDGEMENT_MESSAGE, User.SERVER, null, null);
		outgoingMessages.add(ackMessage);
	}

	public void sendChatMessage(Message message){
		outgoingMessages.add(message);
	}

	public User getUser(){
		return user;
	}

	/**
	 * @return the currentUserHandlers
	 */
	public ArrayList<UserHandler> getCurrentUserHandlers() {
		return currentUserHandlers;
	}

	private void handleMessage(Message message) {
		switch (message.getType()) {
		case CHAT_MESSAGE:
			sendChatMessage(message);
			break;
		case DISCONNECT_REQUEST_MESSAGE:
			sendDisconnAck();
			sendUserStatusChange(false);
			stop();
			break;
		default:
			break;
		}
	}

	private void stop() {
		bRun = false;
	}

	public void updateCurrentUsers(UserHandler changingUser, boolean joining) {
		if (joining) {
			currentUserHandlers.add(changingUser);
		} else {
			currentUserHandlers.remove(changingUser);
		}
	}

	public void sendUserStatusChange(boolean joining) {
		for (UserHandler userHandler : currentUserHandlers) {
			userHandler.updateCurrentUsers(this, joining);
		}
	}

	public void run(){
		try {
			ObjectInputStream inFromClient = null;
			ObjectOutputStream outToClient = null;
			if (Thread.currentThread().getName().equals("listen")) {
				inFromClient = new ObjectInputStream(userSocket.getInputStream());
				Message message;
				while ((message = (Message) inFromClient.readObject()) != null && bRun!=false) {
					System.out.println("Message received: " + message);
					handleMessage(message);
				}
			} else if (Thread.currentThread().getName().equals("send") || bRun!=false) {
				outToClient = new ObjectOutputStream(userSocket.getOutputStream());
				while(bRun != false) {
					Message sendingMessage = outgoingMessages.poll();
					if (sendingMessage != null) {
						outToClient.writeObject(sendingMessage);
					}
				}
			}
			inFromClient.close(); outToClient.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

}
