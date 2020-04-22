import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * This class represents a single message passed between server and client.
 * A server should be able to send this object to multiple different clients with no
 * problems. As a result, this object is immutable once created. 
 * @author Eli
 *
 */
public class Message implements java.io.Serializable {

	/**
	 * This field represents a version of this class, so we don't accidentally try to load
	 * an object from a previous version of this class (that would be a Bad Thing).
	 * 
	 * Every time this class is modified, the UID should be incremented.
	 */
	private static final long serialVersionUID = 0L;
	
	/**
	 * This field should contain the time (in UTC) that the message was created
	 */
	private LocalDateTime messageTimestamp;
	
	/**
	 * This field should contain the User of the client or server that created this message.
	 */
	private User originatingUser;
	
	public Message(MessageType t, User originatingUser) {
		
		// TODO: Make sure this is the correct date format
		messageTimestamp = LocalDateTime.now(ZoneId.ofOffset("UTC", ZoneOffset.UTC));
		
		// Check to make sure that this message is being sent by a user (client or server) who
		// is allowed to send this kind of message
		if (!t.canBeSentByClient && !originatingUser.isServer()) {
			throw new IllegalArgumentException("Cannot create messages with client as originator when the message type cannot be sent from client!");
		}
		if (!t.canBeSentByServer && originatingUser.isServer()) {
			throw new IllegalArgumentException("Cannot create messages with server as originator when the message type cannot be sent from server!");
		}
	}

}

