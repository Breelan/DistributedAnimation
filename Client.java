import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

public class Client extends JFrame {
	
	
//	WE NEED:
//	1. ClientHandler to check if another client leaves
//	2. List of client ObjectOutputStreams (sorted)
//	3. List of ClientTuples (sorted)
//	4. algorithm to decide which client to send the
//	sprite position to
//	5. view to display the sprite
//	6. timer to run the animations
//	7. POTENTIAL keylistener to control the sprite

	private int myNum;
	private List<ClientTuple> otherClients;
	private List<ObjectOutputStream> outputStreams;
	private ServerSocket serverSock;
	private Socket socket;
	private static final String SERVER_ADDR = "localhost";
//	TODO use this instead of string eventually?
//	private static final InetAddress address;
	private static final int SERVER_PORT = 4003; 
	
	public static void main(String[] args) {
//		TODO pull in ip address dynamically
//		int temp = Integer.parseInt(args[0]);
//		serverAddr = (InetAddress)temp;
		new Client().setVisible(true);
	}
	
	public Client() {
//		set up jframe
		this.setTitle("A Client");
		this.setSize(500, 500);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(false);
		
//		initialize data structures
		otherClients = new ArrayList<>();
		outputStreams = new ArrayList<>();
		try {
			socket = new Socket(SERVER_ADDR, SERVER_PORT);
			
//			the server sends you a list of ClientTuples
//			if you're client 0, do nothing
			
//			if you're client x, for n in ClientTuples:
//				wait for connections from other clients
//				accept();
			
//			OR
			
//			create new threads, 1 for each other client
//				connect() to each new client
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private class ServerListener extends Thread {
//		THIS DOES:
//			adds incoming ClientTuple to the list of ClientTuples
//			starts a new ClientHandler associated with the new client
			
	}
	
	
	private class ClientHandler extends Thread {
//		THIS DOES:
//			connects to a new client - creates new ios and oos
//				connect();
		
//				OR
		
//				accept() connection from new client
		
		
//				add oos to list of outputStreams
//			listens for incoming data from a specific client
//			upon client leaving, removes their data from the ClientTuple
//			and outputStreams list
	
	}
	
	
}
