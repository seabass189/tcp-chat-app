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
	 * Returns the user's username
	 * @return The client-facing username of this user
	 */
	public String getUsername() {
		return null;
		// TODO
	}
	
	/**
	 * Returns this user's unique ID
	 * @return An ID that uniquely identifies this user.
	 */
	public int getId() {
		return -1;
		// TODO
	}
	
}
