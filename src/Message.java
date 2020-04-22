import java.time.LocalDateTime;

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
	 * This field should contain the time that the message was created
	 */
	private LocalDateTime messageTimestamp;
	
	public Message(MessageType t) {
		
		
		
	}

}

