import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * This class represents a Server. This class primarily controls the UserHandlers
 * as well as dealing with any new connections. New connections will be given a 
 * new UserHandler and User
 *
 * @author sebas
 * 
 */
public class Server {

	/**
	 * This field represents a version of this class, so we don't accidentally try to load
	 * an object from a previous version of this class (that would be a Bad Thing).
	 * 
	 * Every time this class is modified, the UID should be incremented.
	 */
	private static final long serialVersionUID = 0L;

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
	 * This function will take received connection request messages and the socket it was sent on,
	 * and assign the connection with a User, which will be passed as an argument into a new
	 * UserHandler. The new UserHandler is then added to the currentUserHandlers arraylist
	 * @param message
	 * @param cSocket
	 */
	private static void addNewClient(Message message, Socket cSocket) {
		User newUser = new User(message.getMessageText());
		UserHandler newUH;
		if (currentUserHandlers.size() < 1) {
			newUH = new UserHandler(cSocket, newUser, new ArrayList<UserHandler>());
		} else {
			UserHandler temp = currentUserHandlers.get(0);
			newUH = new UserHandler(cSocket, newUser, temp.getCurrentUserHandlers());
			currentUserHandlers.add(newUH);
		}
	}

	/**
	 * The main method will listen for any incoming messages/connections on the ServerSocket
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			welcomeSocket = new ServerSocket(listeningPort);
			ObjectInputStream inFromClient = null;
			while (true) {
				Socket cSocket = welcomeSocket.accept();
				inFromClient = new ObjectInputStream(cSocket.getInputStream());
				Message message;
				message = (Message) inFromClient.readObject();
				System.out.println("Connection message received: " + message);
				addNewClient(message, cSocket);
			}
		} catch (Exception e) {
			System.out.println("There was an issue setting up the server.");
			e.printStackTrace();
		}
	}

}
