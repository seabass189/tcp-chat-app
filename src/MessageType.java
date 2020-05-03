
public enum MessageType {

	/**
	 * Sent by the client to the server when the client wants to connect with the server
	 * Data: messageText contains Name of the user
	 */
	//TODO: is the originating user of this type of message supposed to be null?
	CONNECTION_REQUEST_MESSAGE(0, true, false, true),
	
	/**
	 * Sent by the server to the client when the server accepts the connection
	 * Data: [0] A list of all currently connected users
	 * Data: [1] The client's User object
	 */
	CONNECTION_ACKNOWLEDGEMENT_MESSAGE(2, false, true),
	
	/**
	 * Whenever a user connects or disconnects, this message is sent to all of the
	 * other users.
	 * Data: [0] The User Object of the user
	 * Data: [1] A Boolean: True if they joined the server, false otherwise
	 */
	USER_STATUS_CHANGE_MESSAGE(2, true, true),
	
	/**
	 * This message is sent for every message typed in the chat.
	 * The message originates from the client who sent the message, and then is
	 * broadcasted to all the other users.
	 * Data: messageText contains the content of the message
	 */
	CHAT_MESSAGE(0, true, false, true),
	
	/**
	 * Sent by the client to the server when the client wants to disconnect from the server
	 */
	DISCONNECT_REQUEST_MESSAGE(0, true, false),
	
	/**
	 * Sent by the server to the client when the server accepts the user's disconnection
	 */
	DISCONNECT_ACKNOWLEDGEMENT_MESSAGE(0, false, true);

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
	 * This int controls the number of objects that are required to be in the message detail
	 * array. This is bad design, and should be replaced.
	 */
	public int messageDetailCount;
	
	MessageType(int mDC, boolean cC, boolean cS) {
		this(mDC, cC, cS, false);
	}
	
	MessageType(int messageDetailCount, boolean canBeSentByClient, boolean canBeSentByServer, boolean includesMessageTextString) {
		this.messageDetailCount = messageDetailCount;
		this.canBeSentByClient = canBeSentByClient;
		this.canBeSentByServer = canBeSentByServer;
		this.includesMessageTextString = includesMessageTextString;
	}
	
	
}
