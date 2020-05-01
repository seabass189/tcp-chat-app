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

	/**
	 * This socket directly connects to the Client, and will be used to send messages to
	 * and from Client and this UserHandler
	 */
	private Socket userSocket;

	/**
	 * This User represents the Client, and holds the Client's user name and userID
	 */
	private User user;

	/**
	 * This Thread will use the userSocket in order to listen and receive any incoming
	 * messages from the Client
	 */
	private Thread listeningThread;

	/**
	 * This Thread will use the userSocket in order to send any outgoing
	 * messages to the Client 
	 */
	private Thread sendingThread;

	/**
	 * This boolean is true when the Client is connected to the chat, and becomes false when
	 * the Client disconnects
	 */
	private boolean bRun = true;

	/**
	 * This ArrayList holds all of the other UserHandlers in the group, and does
	 * not include this UserHandler
	 */
	private ArrayList<UserHandler> currentUserHandlers = new ArrayList<UserHandler>();

	/**
	 * This Queue holds all outgoing messages that have not yet been sent. When a message
	 * is sent, it will be removed from the front of the Queue
	 */
	private PriorityQueue<Message> outgoingMessages = new PriorityQueue<Message>();

	/**
	 * Constructor for a new UserHandler. Accepts the following parameters, and then initializes 
	 * the listening and sending Threads. After, it will send the ConnectionAck back to the 
	 * Client, and will let all other UserHandlers know that it has joined the chat
	 * @param connectionSocket becomes userSocket
	 * @param user becomes user
	 * @param currentUsers becomes currentUserHandlers
	 */
	UserHandler(Socket connectionSocket, User user, ArrayList<UserHandler> currentUsers){
		this.userSocket = connectionSocket;
		this.user = user;
		this.currentUserHandlers = currentUsers;

		listeningThread= new Thread(this, "listen");//create a thread
		listeningThread.start(); //start the thread
		sendingThread = new Thread(this, "send");
		sendingThread.start();

		sendConnectionAck();
		sendConnUserStatus();
	}

	/**
	 * This function will create a new ConnectionAck message and send it to the Client
	 * by adding it to the outgoingMessages queue
	 */
	private void sendConnectionAck() {
		Object[] details = {currentUserHandlers, user};
		Message ackMessage = new Message(MessageType.CONNECTION_ACKNOWLEDGEMENT_MESSAGE, User.SERVER, null, details);
		addToOutgoingMessages(ackMessage);
	}

	/**
	 * This function will create a new DisconnectionAck message and send it to the Client
	 * by adding it to the outgoingMessages queue 
	 */
	private void sendDisconnAck(){
		Message ackMessage = new Message(MessageType.DISCONNECT_ACKNOWLEDGEMENT_MESSAGE, User.SERVER, null, null);
		outgoingMessages.add(ackMessage);
	}
	
	/**
	 * Message argument will be added to this outgoingMessages queue
	 * @param message - message that is being added to this outgoingMessages queue
	 */
	public void addToOutgoingMessages(Message message) {
		outgoingMessages.add(message);
	}

	/**
	 * Message is being forwarded to all other UserHandlers,
	 * who will send the message to their own Clients
	 * @param message - message that is being sent out to all other UserHandlers
	 */
	private void sendMessage(Message message){
		for (UserHandler userHandler : currentUserHandlers) {
			userHandler.addToOutgoingMessages(message);
		}
	}

	/**
	 * @return the user of this UserHandler
	 */
	public User getUser(){
		return user;
	}

	/**
	 * @return the currentUserHandlers arraylist
	 */
	public ArrayList<UserHandler> getCurrentUserHandlers() {
		return currentUserHandlers;
	}

	/**
	 * Messages received from Client are processed. Received messages should only be
	 * of type CHAT_MESSAGE or DISCONNECT_REQUEST_MESSAGE
	 * 
	 * CHAT_MESSAGEs will be forwarded
	 * DISCONNECT_REQUEST_MESSAGEs will start disconnect sequence
	 * @param message - message from the Client that needs to be processed
	 */
	private void handleMessage(Message message) {
		switch (message.getType()) {
		case CHAT_MESSAGE:
			sendMessage(message);
			break;
		case DISCONNECT_REQUEST_MESSAGE:
			sendDisconnAck();
			sendDisconnUserStatus();
			stop();
			break;
		default:
			break;
		}
	}

	/**
	 * Function to set boolean bRun to false, which will stop this UserHandler
	 */
	private void stop() {
		bRun = false;
	}
	
	/**
	 * userHandler will be added to this currentUserHandlers list
	 * @param addUserHandler
	 */
	public void addToCurrentUsers(UserHandler addUserHandler) {
		currentUserHandlers.add(addUserHandler);
	}
	
	/**
	 * userHandler will be removed from this currentUserHandlers list
	 * @param removeUserHandler
	 */
	public void removeFromCurrentUsers(UserHandler removeUserHandler) {
		currentUserHandlers.remove(removeUserHandler);
	}

	/**
	 * USER_STATUS_CHANGE_MESSAGE will be sent to every other UserHandler when a new UserHandler is created
	 */
	private void sendConnUserStatus(){
		for (UserHandler userHandler : currentUserHandlers) {
			userHandler.addToCurrentUsers(this);
		}
			Object[] details = {this, true};
			Message statusMessage = new Message(MessageType.USER_STATUS_CHANGE_MESSAGE, User.SERVER, null, details);
			sendMessage(statusMessage);
	}
	
	/**
	 * USER_STATUS_CHANGE_MESSAGE will be sent to every other UserHandler when a UserHandler is disconnecting
	 */
	private void sendDisconnUserStatus(){
		for (UserHandler userHandler : currentUserHandlers) {
			userHandler.removeFromCurrentUsers(this);
		}
			Object[] details = {this, false};
			Message statusMessage = new Message(MessageType.USER_STATUS_CHANGE_MESSAGE, User.SERVER, null, details);
			sendMessage(statusMessage);
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
			System.out.println("Exception caught");
			e.printStackTrace();
		}
	}

}
