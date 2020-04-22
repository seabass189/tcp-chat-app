
public enum MessageType {

	/**
	 * Sent by the client to the server when the client wants to connect with the server
	 * Data: messageText contains Name of the user
	 */
	CONNECTION_REQUEST_MESSAGE(true, false, true),
	
	/**
	 * Sent by the server to the client when the server accepts the connection
	 * Data: ** A list of all currently connected users
	 */
	CONNECTION_ACKNOWLEDGEMENT_MESSAGE(false, true),
	
	/**
	 * Whenever a user connects or disconnects, this message is sent to all of the
	 * other users.
	 * Data: The User Object of the user, and ** whether they are coming or going
	 */
	USER_STATUS_CHANGE_MESSAGE(true, true),
	
	/**
	 * This message is sent for every message typed in the chat.
	 * The message originates from the client who sent the message, and then is
	 * broadcasted to all the other users.
	 * Data: User who sent the message
	 * Data: messageText contains the content of the message
	 */
	CHAT_MESSAGE(true, true, true),
	
	/**
	 * Sent by the client to the server when the client wants to disconnect from the server
	 */
	DISCONNECT_REQUEST_MESSAGE(true, false),
	
	/**
	 * Sent by the server to the client when the server accepts the user's disconnection
	 */
	DISCONNECT_ACKNOWLEDGEMENT_MESSAGE(false, true);

	/**
	 * These booleans control who is allowed to send this kind of message
	 */
	public boolean canBeSentByClient;
	public boolean canBeSentByServer;
	
	/**
	 * This boolean controls whether this type of message uses the messageText String in message.
	 * If false, then the string cannot be specified or get'ed from Message objects that use
	 * this as our message type.
	 */
	public boolean includesMessageTextString;
	
	MessageType(boolean cC, boolean cS) {
		this(cC, cS, false);
	}
	
	MessageType(boolean canBeSentByClient, boolean canBeSentByServer, boolean includesMessageTextString) {
		this.canBeSentByClient = canBeSentByClient;
		this.canBeSentByServer = canBeSentByServer;
	}
	
	
}
