import java.util.ArrayList;

public enum MessageType {

	/**
	 * Sent by the client to the server when the client wants to connect with the server
	 * Data: messageText contains Name of the user
	 */
	CONNECTION_REQUEST_MESSAGE(true, false, new Class[0], true),
	
	/**
	 * Sent by the server to the client when the server accepts the connection
	 * Data: [0] A list of all currently connected users
	 * Data: [1] The client's User object
	 */
	CONNECTION_ACKNOWLEDGEMENT_MESSAGE(false, true, new Class[]{ArrayList.class, User.class}, false),
	
	/**
	 * Whenever a user connects or disconnects, this message is sent to all of the
	 * other users.
	 * Data: [0] The User Object of the user
	 * Data: [1] A Boolean: True if they joined the server, false otherwise
	 */
	USER_STATUS_CHANGE_MESSAGE(true, true, new Class[]{User.class, Boolean.class}, false),
	
	/**
	 * This message is sent for every message typed in the chat.
	 * The message originates from the client who sent the message, and then is
	 * broadcasted to all the other users.
	 * Data: messageText contains the content of the message
	 */
	CHAT_MESSAGE(true, false, new Class[0], true),
	
	/**
	 * Sent by the client to the server when the client wants to disconnect from the server
	 */
	DISCONNECT_REQUEST_MESSAGE(true, false),
	
	/**
	 * Sent by the server to the client when the server accepts the user's disconnection
	 */
	DISCONNECT_ACKNOWLEDGEMENT_MESSAGE(false, true);

	/**
	 * These booleans control who is allowed to be the originator of this kind of message
	 */
	public boolean canBeSentByClient;
	public boolean canBeSentByServer;
	
	/**
	 * This boolean controls whether this type of message uses the messageText String in message.
	 * If false, then the string cannot be specified or get'ed from Message objects that use
	 * this as our message type.
	 */
	public boolean includesMessageTextString;
	
	/**
	 * This array of Class controls both the number and types of objects that 
	 * are required to be in the message detail array. This is bad design.
	 */
	@SuppressWarnings("rawtypes")
	public Class[] messageDetailTypes;
	
	MessageType(boolean cC, boolean cS) {
		this(cC, cS, new Class[0], false);
	}
	MessageType(boolean canBeSentByClient, boolean canBeSentByServer, Class[] messageDetailTypes, boolean includesMessageTextString) {
		this.messageDetailTypes = messageDetailTypes;
		this.canBeSentByClient = canBeSentByClient;
		this.canBeSentByServer = canBeSentByServer;
		this.includesMessageTextString = includesMessageTextString;
	}
	
	
}
