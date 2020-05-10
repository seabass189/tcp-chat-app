import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;

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
	
	/**
	 * This field may contain some text relevant to whatever kind of message this is.
	 * Or, it may not be allowed to contain anything. This is probably bad design, and
	 * should be superseded if possible.
	 */
	private String messageText;
	
	/**
	 * This field may contain objects relevant to the type of message being passed. This
	 * is definitely bad design. 
	 */
	private Object[] messageDetails;
	
	/**
	 * This field contains what type of message this is.
	 */
	private MessageType type;
	
	public Message(MessageType t, User originatingUser, String messageText, Object[] messageDetails) {
		
		// TODO: Make sure this is the correct date format
		messageTimestamp = LocalDateTime.now(ZoneId.ofOffset("UTC", ZoneOffset.UTC));
		
		this.type = t;
		
		// Check to make sure that this message is being sent by a user (client or server) who
		// is allowed to send this kind of message
		if (!t.canBeSentByClient && !originatingUser.isServer()) {
			throw new IllegalArgumentException("Cannot create messages with client as originator when the message type cannot be sent from client!");
		}
		if (!t.canBeSentByServer && originatingUser.isServer()) {
			throw new IllegalArgumentException("Cannot create messages with server as originator when the message type cannot be sent from server!");
		}
		
		this.originatingUser = originatingUser;
		
		// Check to makes sure that there is only messageText if the type of message supports messageText
		if (!t.includesMessageTextString && messageText != null) {
			throw new IllegalArgumentException("Message type " + t + "does not support the messageText parameter.");
		}
		this.messageText = messageText;
		//TODO: Add secondary object data type.
		
		if (messageDetails == null) {
			messageDetails = new Object[0];
		}
<<<<<<< Updated upstream
		if (messageDetails.length != t.messageDetailCount) {
			throw new IllegalArgumentException("Message type " + t + "must be supplied with " + t.messageDetailCount + "details in the messageDetail array.");
=======
		if (messageDetails.length != t.messageDetailTypes.length) {
			throw new IllegalArgumentException("Message type " + t + " must be supplied with " + t.messageDetailTypes.length + " details in the messageDetail array.");
		}
		// Make sure that everything we put in this list is of the right class.
		for (int i = 0; i < messageDetails.length; i++) {
			if (!(t.messageDetailTypes[i].isInstance(messageDetails[i]))) {
				throw new IllegalArgumentException("Message type " + t + " was supplied with"
						+ " detail #" + i + " as " + messageDetails[i].toString() + " when it was expecting an object of type " + t.messageDetailTypes[i] + ".");
			}
>>>>>>> Stashed changes
		}
		this.messageDetails = messageDetails;
		
		
	}
	
	@Override
	public String toString() {
		return "Message from " + this.originatingUser + ". Type: " + this.type + 
				((this.type.includesMessageTextString) ? " Message: " + this.messageText : "");
	}
	
	// Test method to ensure that serialization works correctly.
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		System.out.println("Initiating Message serialization test.");
		User u1 = new User("Alice");
		System.out.println(u1);
		
		Message[] inp = new Message[] {
				new Message(MessageType.CHAT_MESSAGE, u1, "Hello world!", null),
				new Message(MessageType.CONNECTION_ACKNOWLEDGEMENT_MESSAGE, User.SERVER, null, new Object[] {new ArrayList<User>(), u1}),
				new Message(MessageType.USER_STATUS_CHANGE_MESSAGE, User.SERVER, null, new Object[] {u1, true}),
				new Message(MessageType.USER_STATUS_CHANGE_MESSAGE, User.SERVER, null, new Object[] {u1, false}),
				new Message(MessageType.DISCONNECT_REQUEST_MESSAGE, u1, null, null),
				new Message(MessageType.DISCONNECT_ACKNOWLEDGEMENT_MESSAGE, User.SERVER, null, null),
				
		};
		
		

		System.out.println("Serialization test:");
		for (int i = 0; i < inp.length; i++) {
			Message m1 = inp[i];
			// Write m1 to a byte array. This is functionally equivalent to sending it over a socket.
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			ObjectOutputStream os = new ObjectOutputStream(bytes);
			os.writeObject(m1);
			byte[] userAsBytes = bytes.toByteArray();
			os.close(); bytes.close();
			
			// Read m1 back from the array to m1_serial
			ByteArrayInputStream bytes2 = new ByteArrayInputStream(userAsBytes);
			ObjectInputStream is = new ObjectInputStream(bytes2);
			Message m1_serial = (Message) is.readObject();
			is.close(); bytes2.close();
			
			//Test to make sure the user wasn't mangled in any way.
			System.out.println(m1 + " --> "  + m1_serial);
		}
		
		
	}

}

