import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//import network.ClientHandler;

public class Server {

	public static final int SERVER_PORT = 4003;
	private static ServerSocket sock;
	private static List<ClientTuple> clients = Collections.synchronizedList(new ArrayList<>());
	private static List<Socket> allSockets = Collections.synchronizedList(new ArrayList<>());
	
	
	public static void main(String[] args) {
		
		int clientNum = 0;
		
		try {
			sock = new ServerSocket(SERVER_PORT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Server started on port " + SERVER_PORT);
		
		
		while (true) {
			// Accept a connection from the ServerSocket.
			Socket s;
			try {
				System.out.println("about to accept connection");
				s = sock.accept();
				System.out.println("someone connected");
//				pull out the address and port
//				InetAddress clientAddress = s.getInetAddress();
//				int clientPort = s.getPort();
//				ClientTuple newClient = new ClientTuple(clientAddress, clientPort, clientNum);
				
				ObjectOutputStream newOutput = new ObjectOutputStream(s.getOutputStream());
				ObjectInputStream newInput = new ObjectInputStream(s.getInputStream());
				
				System.out.println("finished creating ois and oos to new client");
//				tell the client which one they are
				newOutput.writeObject(clientNum);
				clientNum++;
				
				newOutput.writeObject(clients);
				
				ClientTuple newTuple;
				try {
					newTuple = (ClientTuple) newInput.readObject();
//					add new client to the list
					clients.add(newTuple);
					System.out.println("added new client to list of clienttuples");
					
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				
				
//				send the new client info to all other connected clients
//				for(Socket socket: allSockets) {
//					ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
//					oos.writeObject(newClient);
//				}
				
				allSockets.add(s);
				System.out.println("new client's stream added to allSockets");

//				ObjectInputStream is = new ObjectInputStream(s.getInputStream());
//				ObjectOutputStream os = new ObjectOutputStream(s.getOutputStream());
//				outputStreams.add(os);
				
//				ClientHandler handler = new ClientHandler(is, os, clients);
//				ServerClientHandler handler = new ServerClientHandler(s, clients, allSockets);
				ServerClientHandler handler = new ServerClientHandler(newOutput, newInput, clients, allSockets);
				handler.start();

				System.out.println("Accepted a new connection from " + s.getInetAddress());
			} catch (IOException e) {
				e.printStackTrace();
			} 
			
//			TODO tell all the other clients someone has connected


			// Save the output stream to our clients list so we can
			// broadcast to this client later

			// add the output stream from server
//			clients.add(os);

			// Start a new ClientHandler thread for this client.
			// create a new ClientHandler (its own class) - takes input stream
			// and output stream

		}
	}
	
}

class ServerClientHandler extends Thread {
	
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private List<ClientTuple> clients;
	private List<Socket> allSockets;
	private Socket s;
	private boolean isConnected = true;
	
//	public ClientHandler(ObjectInputStream is, ObjectOutputStream os, List<ClientTuple> clients) {
//	public ServerClientHandler(Socket s, List<ClientTuple> clients, List<Socket> sockets) {
	public ServerClientHandler(ObjectOutputStream newOutput, ObjectInputStream newInput, List<ClientTuple> clients, List<Socket> sockets) {
		System.out.println("inside ServerClientHandler constructor");
//		this.output = os;
//		this.input = is;
//		this.s = s;
		this.output = newOutput;
		this.input = newInput;
		this.clients = clients;
		this.allSockets = sockets;
		
//		try {
//			output = new ObjectOutputStream(s.getOutputStream());
//			input = new ObjectInputStream(s.getInputStream());
//			System.out.println("finished creating oos and ois for new client");
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}
	
	@Override
	public void run() {
		System.out.println("inside client handler");
		while(isConnected) {
//			TODO check the state of the socket/objectinputstream
//				if it's closed, remove this client from your list
			Object incoming;
			
			try {
				incoming = input.readObject();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
				
//				close the connection
				try {
					this.input.close();
					this.output.close();
					
//					for(int i = 0; i < clients.size(); i++) {
//						if(clients.get(i).getAddr().equals(s.getInetAddress())) {
//							clients.remove(i);
//							break;
//						}
//					}
					
					for (ClientTuple client : clients) {
						if(client.getAddr().equals(s.getInetAddress())) {
							clients.remove(client);
							break;
						}
					}
					
					for (Socket socket : allSockets) {
						if(socket == s) {
							allSockets.remove(socket);
						}
					}
					
					isConnected = false;
					
					
				} catch (IOException e1) {
//					TODO remove client from the list
					e1.printStackTrace();
				}
				
			}
		}
	}

}
