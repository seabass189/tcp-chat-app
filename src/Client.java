import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

/**
 * This class represents an instance of a client. The user primarily interacts with this class
 * and by doing so can send and receive the messages of other users.
 * @author Connor 
 *
 */
public class Client implements Runnable
{
	/*
	 * This field contains the socket that connects to the Server
	 */
	private Socket clientSocket;

	/*
	 * This field contains a BufferedReader that will accept the user's input
	 */
	private BufferedReader in;

	/*
	 * This field contains a ObjectOutputStream that will send Messages to the server
	 */
	private ObjectOutputStream toServer;

	/*
	 * This field contains a ObjectInputStream that will accept messages from the server
	 */
	private ObjectInputStream fromServer;

	/*
	 * This field contains a User object that represents the client
	 */
	private User self;

	/*
	 * This field contains a boolean that is true while the program is running, and set to false when it should stop
	 */
	private boolean stop;



	public Client()
	{
		try
		{
			//Instantiates the reader for user input
			in = new BufferedReader(new InputStreamReader(System.in));

			//Set a default address
			InetAddress address = InetAddress.getLocalHost();

			//Prompts the user for host's name and port number to create a socket
			System.out.print("Enter host to connect to (blank line for " +
					address.getHostAddress() + "): ");
			String hostString = in.readLine();
			if (!hostString.equals("")) {
				address = InetAddress.getByName(hostString);
				// TODO: Validate this input
			}

			int port = Server.DEFAULT_PORT;

			System.out.print("Enter port to connect to (blank line for " +
					port + "): ");
			String portString = in.readLine();
			if (!portString.equals("")) {
				port = Integer.parseInt(portString);
			}

			clientSocket = new Socket(address, port);

			//Input and output streams are instantiated using the generated socket
			//			toServer = new ObjectOutputStream(clientSocket.getOutputStream());


			//Sends and receives connection messages and gets its identifier from the Server
			//and also prints out the other users in the chat room
			self = connect();

			//The boolean stop remains false until the program is ended
			stop = false;

			//Two threads are generated with the user thread designed to listen for user input
			//while the server thread waits for messages from the server
			Thread user = new Thread(this, "user");
			Thread server = new Thread(this, "server");

			//The threads continue listening for input until the variable stop is changed
			//and ends the while loop
			user.start();
			server.start();
		}
		catch(IOException e)
		{
			e.printStackTrace();
			stop = true;
		}
	}

	//Sends a connection request message to the server to inform them. Receives an acknowledgement
	//message and extracts a User objects and an ArrayList of UserHandlers. The User is used
	//for further messages sent to the server. ArrayList is read through and printed to inform
	//user of other people in the chat room.
	private User connect()
	{
		//Prompts the user for a username and creates a temporary User object
		System.out.print("Enter username: ");
		String username;
		try {
			username = in.readLine();
			User ourUserObject = new User(username);

			//Upon acknowledgement the client extracts the data held in the message
			toServer = new ObjectOutputStream(clientSocket.getOutputStream());

			//The User object is used to send a connection request Message to the server
			Message connRequest = new Message(MessageType.CONNECTION_REQUEST_MESSAGE, ourUserObject, username, null);
			toServer.writeObject(connRequest);

			fromServer = new ObjectInputStream(clientSocket.getInputStream());
			Message connAck = (Message)fromServer.readObject();
			//			fromServer.close();
			Object[] details = connAck.getMessageDetails();


			//The client's User object is updated to match the User held in the server
			ourUserObject = (User)details[1];

			//The arraylist of UserHandlers is extracted and the client iterates through the list
			//to inform the user of the other users currently present
			@SuppressWarnings("unchecked")
			ArrayList<User> otherUsers = (ArrayList<User>)details[0];


			System.out.print("Welcome to the room! There are " + otherUsers.size() + " other users here");
			if (otherUsers.size() > 0) {
				System.out.print(" (");
				for(int i = otherUsers.size() - 1; i>=0; i--)
				{
					System.out.print(otherUsers.get(i).getUsername());
					if (i == 1) {
						System.out.print(" and ");
					}
					else if (i > 0) {
						System.out.print(", ");
					}
				}
				System.out.print(")");
			}

			System.out.println(".");

			return ourUserObject;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			stop = true;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			stop = true;
		}
		return null;

	}

	public void run()
	{
		//The user thread
		if(Thread.currentThread().getName().contentEquals("user"))
		{
			userInput(self);
		}

		//The server thread
		else if(Thread.currentThread().getName().contentEquals("server"))
		{
			serverInput();
		}
	}

	//Responds to input from the user
	private void userInput(User self)
	{

		while(!stop)
		{
			String text = null;
			try
			{
				//Reads the user's input
				text = in.readLine();
			}

			catch(IOException e)
			{
				System.out.println(e.getMessage());
			}

			if(text!=null)
			{
				//If the input is not the string "." then the client sends a message
				if(!text.contentEquals("."))
				{
					sendMessage(self, text);
				}

				//If the input is the string "." then the client goes through termination
				//procedures
				else
				{
					sendDisconnectRequest(self);
				}
			}
		}
	}

	//Responds to incoming messages from the server
	private void serverInput()
	{
		while(!stop)
		{
			//Client captures the message received from the server and its data
			Message received = null;

			try {
				received = (Message)fromServer.readObject();
			} catch (ClassNotFoundException | IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				stop = true;
			}

			if(received!=null)
			{
				Object[] details = received.getMessageDetails();
				switch (received.getType()) {
				case CHAT_MESSAGE:
					System.out.print(received.getOriginatingUser().getUsername());
					System.out.print(" (" 
							+ received.getMessageTimestamp().getHour() + ":"
							+ received.getMessageTimestamp().getMinute() 
							+ ")");
					System.out.print(": ");
					System.out.println(received.getMessageText());
					break;
				case DISCONNECT_ACKNOWLEDGEMENT_MESSAGE:
					stop = true;
					System.out.println("You have disconnected.");
					//TODO: We might need to force stop other threads here? Maybe system.exit()?
					break;
				case USER_STATUS_CHANGE_MESSAGE:
					this.statusChange(details);
					break;
				default:
					System.out.println("Ignoring Invalid Message Type '" + received.getType() + "'.");
					break;
				}
			}
		}
	}

	//Sends a chat message from the user to the server
	private void sendMessage(User self, String text)
	{
		//Creates a new instance of the Message objects and sends it to the server
		Message message = new Message(MessageType.CHAT_MESSAGE, self, text, null);
		try {
			toServer.writeObject(message);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			stop = true;
		}
	}

	//Sends a disconnect request to the server and upon acknowledgement terminates the threads
	private void sendDisconnectRequest(User self)
	{
		//Creates a disconnect request message and sends to server
		Message request = new Message(MessageType.DISCONNECT_REQUEST_MESSAGE, self, null, null);
		try {
			toServer.writeObject(request);
			//Upon receiving a message and verifying the disconnect acknowledgement
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			stop = true; //If this goes bad, then we really abort hard
		}

	}

	//Prints a message reporting the change of a chat member's status
	private void statusChange(Object[] details)
	{
		//Obtains the User object held in the message
		//If true then the client tells the user that another person has joined
		//If false then the client tells the user a person has left
		User temp = (User)details[0];
		if((boolean)details[1]==true)
		{
			System.out.println(temp.getUsername()+" has joined the room");
		}
		else
		{
			System.out.println(temp.getUsername()+" has left the room");
		}
	}

	public static void main(String[] args)
	{
		Client client = new Client();
	}
}
