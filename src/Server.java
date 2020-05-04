import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * This class represents a Server. This class primarily controls the UserHandlers
 * as well as dealing with any new connections. New connections will be given a 
 * new UserHandler and User
 *
 * @author Sebastian Hernandez and Nowndale Sale
 * 
 */
public class Server {

	/**
	 * This field represents a version of this class, so we don't accidentally try to load
	 * an object from a previous version of this class (that would be a Bad Thing).
	 * 
	 * Every time this class is modified, the UID should be incremented.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * If this is set to true, then Server will run a test instead of
	 * actually trying to listen for connections
	 * 
	 * For this to completely work, the 'run' function in 'UserHandler.java' must be
	 * commented out so that an empty Socket can be passed into the test UserHandlers
	 */
	private static final boolean TEST = true;

	/**
	 * The port where Server will listen for new connections is set before execution
	 */
	private static int listeningPort = 6789;

	/**
	 * This field is the ServerSocket which will listen for new connections.
	 * It will be instantiated using the listeningPort
	 */
	private static ServerSocket welcomeSocket;

	/**
	 * This field is an ArrayList that contains all of the current UserHandlers
	 */
	private static ArrayList<UserHandler> currentUserHandlers = new ArrayList<UserHandler>();
	
	/**
	 * @return the list of current user handlers
	 */
	public static ArrayList<UserHandler> getCurrentUserHandlers() {
		return currentUserHandlers;
	}
	
	/**
	 * Accepts a new UserHandler and adds it to the list of currentUserHandlers in Server
	 * @param uh - new UserHandler
	 */
	public static void addToCurrentUserHandlers(UserHandler uh) {
		currentUserHandlers.add(uh);
	}
	
	/**
	 * Accepts a UserHandler and removes it from the list of currentUserHandlers in Server
	 * @param uh
	 */
	public static void removeFromCurrentUserHandlers(UserHandler uh) {
		currentUserHandlers.remove(uh);
	}
	
	/**
	 * This function will take received connection request messages and the socket it was sent on,
	 * and assign the connection with a User, which will be passed as an argument into a new
	 * UserHandler. The new UserHandler is then added to the currentUserHandlers array list
	 * @param message
	 * @param cSocket
	 */
	private static void addNewClient(Message message, Socket cSocket) {
		User newUser = new User(message.getMessageText());
		UserHandler newUH;
		newUH = new UserHandler(cSocket, newUser);
		addToCurrentUserHandlers(newUH);
	}
	
	private static void test() {
		UserHandler.TEST = true;
		Socket socket = new Socket();
		User testOneUser = new User("testOne");
		User testTwoUser = new User("testTwo");
		User testThreeUser = new User("testThree");
		Message testOne = new Message(MessageType.CONNECTION_REQUEST_MESSAGE, testOneUser, "testOne", null);
		Message testTwo = new Message(MessageType.CONNECTION_REQUEST_MESSAGE, testTwoUser, "testTwo", null);
		Message testThree = new Message(MessageType.CONNECTION_REQUEST_MESSAGE, testThreeUser, "testThree", null);
		Queue<Message> incomingConnectionRequests = new LinkedList<Message>();
		incomingConnectionRequests.add(testOne);
		incomingConnectionRequests.add(testTwo);
		incomingConnectionRequests.add(testThree);
		for (Message message : incomingConnectionRequests) {
			addNewClient(message, socket);
		}
		System.out.println("Current UserHandler List:");
		for (UserHandler userHandler : currentUserHandlers) {
			System.out.println(userHandler.getUser());
		}
		System.out.println("Finito");
	}

	/**
	 * The main method will listen for any incoming messages/connections on the ServerSocket
	 * @param args
	 */
	public static void main(String[] args) {
		if (TEST) {
			test();
		} else {
			try {
				welcomeSocket = new ServerSocket(listeningPort);
				ObjectInputStream inFromClient = null;
				while (true) {
					Socket cSocket = welcomeSocket.accept();
					inFromClient = new ObjectInputStream(cSocket.getInputStream());
					Message message;
					message = (Message) inFromClient.readObject();
					if (message.getType() == MessageType.CONNECTION_REQUEST_MESSAGE) {
						System.out.println("Connection message received: " + message);
						addNewClient(message, cSocket);
					}else {
						throw new IllegalArgumentException("Server received a message that was not of type CONNECTION_REQUEST_MESSAGE.");
					}
				}
			}catch (IllegalArgumentException e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			} catch (Exception e) {
				System.out.println("There was an issue setting up the server.");
				e.printStackTrace();
			}
		}
	}

}
