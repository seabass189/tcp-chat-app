import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * This class is a container for the data associated with the users of this program.
 * It includes information like the user's username, and a unique ID so we can distinguish
 * users from each other.
 * 
 */


public class User implements java.io.Serializable {

	/**
	 * This field represents a version of this class, so we don't accidentally try to load
	 * an object from a previous version of this class (that would be a Bad Thing).
	 * 
	 * Every time this class is modified, the UID should be incremented.
	 */
	private static final long serialVersionUID = 0L;
	
	/**
	 * This field contains a hidden counter that is incremented each time a new user object is constructed.
	 * The pre-increment value is assigned to the new user, thus ensuring that each user gets their own unique ID.
	 */
	private static int nextUserID = 0;
	
	/**
	 * This is the user that represents the server.
	 */
	public static final User SERVER = new User("SERVER");

	/**
	 * This string contains the user's username. This should be used for display purposes only,
	 * NOT to uniquely identify this user.
	 */
	private String username;
	
	/**
	 * This integer contains this user's id, uniquely identifying this user.
	 */
	private int userID;
	
	/**
	 * This constructor should only be called the first time this user connects.
	 * If this constructor is called multiple times for the same user, that user will
	 * have multiple IDs, a state of abject failure.
	 * @param username The username of the new user
	 */
	public User(String username) {
		
		this.username = username;
		this.userID = nextUserID++;
		
	}
	
	/**
	 * Returns the user's username
	 * @return The client-facing username of this user
	 */
	public String getUsername() {
		return this.username;
	}
	
	/**
	 * Returns this user's unique ID
	 * @return An ID that uniquely identifies this user.
	 */
	public int getId() {
		return this.userID;
	}
	
	/**
	 * Returns true if this user object represents the server,
	 * false otherwise.
	 * @return
	 */
	public boolean isServer() {
		return (this == User.SERVER);
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other) return true;
		if (other == null || other.getClass() != this.getClass()) return false;
		// Aside from the obvious cases, we are equal when the IDs are the same.
		return this.getId() == ((User) other).getId();
	}
	
	@Override
	public int hashCode() {
		return this.getId();
	}
	
	@Override
	public String toString() {
		return "User #" + this.getId() + " (" + this.getUsername() + ")";
	}
	
	// Test method to ensure that serialization works correctly.
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		System.out.println("Initiating User serialization test.");
		User u1 = new User("Alice");
		User u2 = new User("Bob");
		System.out.println(u1);
		System.out.println(u2);
		System.out.println(User.SERVER);
		System.out.println("Alice is Bob: " + u1.equals(u2));
		System.out.println("Alice is Alice: " + u1.equals(u1));
		System.out.println("Alice is SERVER: " + u1.equals(User.SERVER));
		
		// Write U1 to a byte array. This is functionally equivalent to sending it over a socket.
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream(bytes);
		os.writeObject(u1);
		byte[] userAsBytes = bytes.toByteArray();
		os.close(); bytes.close();
		
		// Read U1 back from the array to u1_serial
		ByteArrayInputStream bytes2 = new ByteArrayInputStream(userAsBytes);
		ObjectInputStream is = new ObjectInputStream(bytes2);
		User u1_serial = (User) is.readObject();
		is.close(); bytes2.close();
		
		//Test to make sure the user wasn't mangled in any way.
		System.out.println("Serialization test:");
		System.out.println(u1 + " --> "  + u1_serial);
		System.out.println("Same object? " + u1.equals(u1_serial));
		
	}
	
}
