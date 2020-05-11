import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * This class represents a UserHandler. This class primarily interacts with the Clients,
 * and saves reference to them using a User. It is responsible for receiving any messages
 * from the Client and forwarding them to the other UserHandlers, as well as sending any
 * messages to the Client that it receives
 * 
 * @author Sebastian Hernandez and Nowndale Sale
 *
 */
public class UserHandler {

	/**
	 * This field represents a version of this class, so we don't accidentally try to load
	 * an object from a previous version of this class (that would be a Bad Thing).
	 * 
	 * Every time this class is modified, the UID should be incremented.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * If this is set to true, then UserHandler will run a test of its functions instead of
	 * actually trying to listen for connections
	 * 
	 * This variable is public so that Server can set it to true if Server is also running
	 * a test
	 */
	public static boolean TEST = false;
	
	/**
	 * This socket directly connects to the Client, and will be used to send messages to
	 * and from Client and this UserHandler
	 */
//	private Socket userSocket;
	
	private ObjectInputStream inFromClient;
	
	private ObjectOutputStream outToClient;

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
	 * This Thread will process any messages in the incomingMessages queue
	 */
	private Thread processingThread;

	/**
	 * This boolean is true when the Client is connected to the chat, and becomes false when
	 * the Client disconnects
	 */
	private boolean bRun = true;

	/**
	 * This Queue holds all outgoing messages that have not yet been sent. When a message
	 * is sent, it will be removed from the front of the Queue
	 */
	private Queue<Message> outgoingMessages = new LinkedList<Message>();
	
	/**
	 * This Queue holds all incoming messages that have been received. When a message
	 * is received and handled, it will be removed from the front of the Queue
	 */
	private Queue<Message> incomingMessages = new LinkedList<Message>();

	/**
	 * Constructor for a new UserHandler. Accepts the following parameters, and then initializes 
	 * the listening and sending Threads. After, it will send the ConnectionAck back to the 
	 * Client, and will let all other UserHandlers know that it has joined the chat
	 * @param connectionSocket becomes userSocket
	 * @param user becomes user
	 * @param currentUsers becomes currentUserHandlers
	 */
	UserHandler(ObjectInputStream in, ObjectOutputStream out, User user){
		this.inFromClient = in;
		this.outToClient = out;
		this.user = user;

		if(!TEST) {
			System.out.println("NOT A TEST");
//			listeningThread= new Thread(this, "listen");//create a thread
			listeningThread = new Thread(new Runnable()
			{
				@Override
				public void run() {
					System.out.println("In listen thread");
					Message message;
					try {
						while ((message = (Message) inFromClient.readObject()) != null && bRun!=false) {
							System.out.println("Message received: " + message);
							addToIncomingMessages(message);
						}
						inFromClient.close();
					} catch (ClassNotFoundException | IOException e) {
						sendDisconnAck();
						sendDisconnUserStatus();
						stop();
					}
				}
			});
			
			sendingThread = new Thread(new Runnable()
			{
				@Override
				public void run() {
					System.out.println("In send thread");
					try {
						while(bRun != false) {
							Message sendingMessage = outgoingMessages.poll();
							if (sendingMessage != null) {
								outToClient.writeObject(sendingMessage);
							}
						}
						outToClient.close();
					} catch (IOException e) {
						sendDisconnAck();
						sendDisconnUserStatus();
						stop();
					}	
				}
			});
			
			listeningThread.start(); //start the thread
			sendingThread.start();
		}
		processingThread = new Thread(new Runnable()
		{
			@Override
			public void run() {
				while(bRun != false) {
					Message incomingMessage = incomingMessages.poll();
					if (incomingMessage != null) {
						handleMessage(incomingMessage);
					}
				}	
			}
		});
		processingThread.start();
		
		System.out.println("Threads created");

		sendConnectionAck();
		sendConnUserStatus();
	}

	/**
	 * This function will create a new ConnectionAck message and send it to the Client
	 * by adding it to the outgoingMessages queue
	 */
	private void sendConnectionAck() {
		Object[] details = {getUserList(Server.getCurrentUserHandlers()), user};
		Message ackMessage = new Message(MessageType.CONNECTION_ACKNOWLEDGEMENT_MESSAGE, User.SERVER, null, details);
		System.out.println("CONN_ACK: " + ackMessage);
		addToOutgoingMessages(ackMessage);
	}
	
	private ArrayList<User> getUserList(ArrayList<UserHandler> list){
		ArrayList<User> userList = new ArrayList<User>();
		for (UserHandler userHandler : list) {
			userList.add(userHandler.getUser());
		}
		return userList;
	}

	/**
	 * This function will create a new DisconnectionAck message and send it to the Client
	 * by adding it to the outgoingMessages queue 
	 */
	private void sendDisconnAck(){
		Message ackMessage = new Message(MessageType.DISCONNECT_ACKNOWLEDGEMENT_MESSAGE, User.SERVER, null, null);
		addToOutgoingMessages(ackMessage);
	}
	
	/**
	 * Message argument will be added to this outgoingMessages queue
	 * @param message - message that is being added to this outgoingMessages queue
	 */
	public void addToOutgoingMessages(Message message) {
		outgoingMessages.add(message);
	}
	
	/**
	 * Message argument will be added to this outgoingMessages queue
	 * @param message - message that is being added to this outgoingMessages queue
	 */
	private void addToIncomingMessages(Message message) {
		incomingMessages.add(message);
	}

	/**
	 * Message is being forwarded to all other UserHandlers,
	 * who will send the message to their own Clients
	 * @param message - message that is being sent out to all other UserHandlers
	 */
	private void sendMessageToCurrentUserHandlers(Message message){
		if (Server.getCurrentUserHandlers().size() != 0){
			for (UserHandler userHandler : Server.getCurrentUserHandlers()) {
				if (userHandler != this) {
					userHandler.addToOutgoingMessages(message);
				}
			}
		}
	}

	/**
	 * @return the user of this UserHandler
	 */
	public User getUser(){
		return user;
	}
	
	/**
	 * Messages received from Client are processed. Received messages should only be
	 * of type CHAT_MESSAGE or DISCONNECT_REQUEST_MESSAGE
	 * 
	 * CHAT_MESSAGEs will be forwarded
	 * USER_STATUS_CHANGE_MESSAGEs will be forwarded
	 * DISCONNECT_REQUEST_MESSAGEs will start disconnect sequence
	 * All other messages will thrown an exception
	 * @param message - message from the Client that needs to be processed
	 */
	private void handleMessage(Message message) {
		switch (message.getType()) {
		case CHAT_MESSAGE:
			sendMessageToCurrentUserHandlers(message);
			break;
		case USER_STATUS_CHANGE_MESSAGE:
			addToOutgoingMessages(message);
			break;
		case DISCONNECT_REQUEST_MESSAGE:
			sendDisconnAck();
			sendDisconnUserStatus();
			stop();
			break;
		case CONNECTION_REQUEST_MESSAGE:
		case CONNECTION_ACKNOWLEDGEMENT_MESSAGE:
		case DISCONNECT_ACKNOWLEDGEMENT_MESSAGE:
			throw new IllegalArgumentException("UserHandler received a message from Client that Client should not be able to send.");
		}
	}

	/**
	 * Function to set boolean bRun to false, which will stop this UserHandler
	 */
	private void stop() {
		bRun = false;
	}
	
	/**
	 * USER_STATUS_CHANGE_MESSAGE will be sent to every other UserHandler when a new UserHandler is created
	 */
	private void sendConnUserStatus(){
		Object[] details = {this.user, true};
		Message statusMessage = new Message(MessageType.USER_STATUS_CHANGE_MESSAGE, User.SERVER, null, details);
		sendMessageToCurrentUserHandlers(statusMessage);
	}
	
	/**
	 * USER_STATUS_CHANGE_MESSAGE will be sent to every other UserHandler when a UserHandler is disconnecting
	 * This UserHandler will tell Server to delete this UserHandler from the currentUserHandlers list
	 */
	private void sendDisconnUserStatus(){
		Object[] details = {this.user, false};
		Message statusMessage = new Message(MessageType.USER_STATUS_CHANGE_MESSAGE, User.SERVER, null, details);
		sendMessageToCurrentUserHandlers(statusMessage);
		Server.removeFromCurrentUserHandlers(this);
	}
	
	public static void main(String[] args) {
		UserHandler.TEST = true;
		UserHandler testOne = new UserHandler(null, null, new User("testOne"));
		UserHandler testTwo = new UserHandler(null, null, new User("testTwo"));
		UserHandler testThree = new UserHandler(null, null, new User("testThree"));
		Server.addToCurrentUserHandlers(testOne);
		Server.addToCurrentUserHandlers(testTwo);
		Server.addToCurrentUserHandlers(testThree);
		
		UserHandler testUH = new UserHandler(null, null, new User("testUser"));
		Server.addToCurrentUserHandlers(testUH);
		
		// Test with a chat message
		Message textMessage = new Message(MessageType.CHAT_MESSAGE, testUH.getUser(), "Hey lookin good", null);
		testUH.addToIncomingMessages(textMessage);
		
		// Test with a user status change message
		Object[] details = {testTwo.getUser(), false};
		Message statusMessage = new Message(MessageType.USER_STATUS_CHANGE_MESSAGE, User.SERVER, null, details);
		testUH.addToIncomingMessages(statusMessage);
		
		// Test with a message that Client cannot send
//		Object[] details2 = {Server.getCurrentUserHandlers(), testUH.getUser()};
//		Message ackMessage = new Message(MessageType.CONNECTION_ACKNOWLEDGEMENT_MESSAGE, User.SERVER, null, details2);
//		testUH.addToIncomingMessages(ackMessage);
		
		// Test with a disconnect request message
		Message disconMessage = new Message(MessageType.DISCONNECT_REQUEST_MESSAGE, testUH.getUser(), null, null);
		testUH.addToIncomingMessages(disconMessage);
				
		try {
			//Allows time for all messages to be processed
			Thread.sleep(500);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("OUTGOING MESSAGES for testUH: ");
		for (Message message : testUH.outgoingMessages) {
			System.out.println("OUT: " + message);
		}
		
		System.out.println("\nOUTGOING MESSAGES for testOne: ");
		for (Message message : testOne.outgoingMessages) {
			System.out.println("OUT: " + message);
		}	
	}
}
