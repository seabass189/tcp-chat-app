import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * 
 */

/**
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

	private static int listeningPort = 6789;

	/**
	 * 
	 */
	private static ServerSocket welcomeSocket;

	/**
	 * 
	 */
	private static ArrayList<UserHandler> currentUserHandlers = new ArrayList<UserHandler>();

	private static void addNewClient(Message message, Socket cSocket) {
		UserHandler newUH;
		if (currentUserHandlers.size() < 1) {
			newUH = new UserHandler(cSocket, message.getOriginatingUser(), new ArrayList<UserHandler>());
		} else {
			UserHandler temp = currentUserHandlers.get(0);
			newUH = new UserHandler(cSocket, message.getOriginatingUser(), temp.getCurrentUserHandlers());
			currentUserHandlers.add(newUH);
		}
	}

	/**
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
