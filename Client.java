import java.awt.Point;
import java.io.IOException;
import java.io.ObjectInputStream;
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
	private static List<ObjectOutputStream> outputStreams;
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
//		otherClients = new ArrayList<>();
		outputStreams = new ArrayList<>();
		try {
			socket = new Socket(SERVER_ADDR, SERVER_PORT);
//			accept id number
			ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
			try {
				myNum = (int) input.readObject();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			
			try {
				otherClients = (ArrayList<ClientTuple>) input.readObject();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			
			if(otherClients.size() > 0) {
//				create new ClientConnector
				new ClientConnector();
//				connectToOthers.start();
			}
			
			ClientAcceptor acceptsClients = new ClientAcceptor();
			acceptsClients.start();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private class ServerListener extends Thread {
		
	
//		THIS DOES:
//			adds incoming ClientTuple to the list of ClientTuples
//		@Override
			
	}
	
//	This class exists only to connect the current Client to 
//	all other existing Clients, then it quits
	class ClientConnector extends Thread {
		
		
		public ClientConnector(){
			this.start();
		}
		
		@Override
		public void run() {
			for(ClientTuple client : otherClients) {
				try {
					Socket s = new Socket(client.getAddr(), client.getPort());
					ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
					ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
					
//					add oos to list in the correct location
					Client.outputStreams.add(oos);
					
//					start new ClientHandler
					new ClientHandler(ois);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private class ClientAcceptor extends Thread {
		
		@Override
		public void run() {
			while(true) {
				try {
					Socket s = serverSock.accept();
					
					ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
					ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
					
					Client.outputStreams.add(oos);
					
					new ClientHandler(ois);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
		}
		
	}
	
	
	private class ClientHandler extends Thread {

		private ObjectInputStream input;
		
		public ClientHandler(ObjectInputStream ois) {
			this.input = ois;
			this.start();
		}
		
		@Override
		public void run() {
			
			while(true) {
				
				try {
					// TODO wait for data
					Point newPoint = (Point) input.readObject();
					// TODO update model
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
//					TODO check for socket closure, handle accordingly
					e.printStackTrace();
				}
			}
		}	
	}
	
	
}
