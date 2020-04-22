
public enum MessageType {

	/**
	 * Sent by the client to the server when the client wants to connect with the server
	 * Data: Name of the user
	 */
	CONNECTION_REQUEST_MESSAGE,
	
	/**
	 * Sent by the server to the client when the server accepts the connection
	 * Data: A list of all currently connected users
	 */
	CONNECTION_ACKNOWLEDGEMENT_MESSAGE,
	
	/**
	 * Whenever a user connects or disconnects, this message is sent to all of the
	 * other users.
	 * Data: The User Object of the user, and whether they are coming or going
	 */
	USER_STATUS_CHANGE_MESSAGE,
	
	/**
	 * This message is sent for every message typed in the chat.
	 * The message originates from the client who sent the message, and then is
	 * broadcasted to all the other users.
	 * Data: User who sent the message
	 * Data: content of the message
	 */
	CHAT_MESSAGE,
	
	/**
	 * Sent by the client to the server when the client wants to disconnect from the server
	 */
	DISCONNECT_REQUEST_MESSAGE,
	
	/**
	 * Sent by the server to the client when the server accepts the user's disconnection
	 */
	DISCONNECT_ACKNOWLEDGEMENT_MESSAGE,
	
}
