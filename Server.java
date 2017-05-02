import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
				
				ClientTuple newTuple = null;
				try {
					newTuple = (ClientTuple) newInput.readObject();
//					add new client to the list
					synchronized(clients) {
						clients.add(newTuple);	
					}
					System.out.println("added new client to list of clienttuples");
					
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				
				synchronized(allSockets) {
					allSockets.add(s);
				}
				System.out.println("new client's stream added to allSockets");

				ServerClientHandler handler = new ServerClientHandler(newTuple, newOutput, newInput, clients, allSockets);
				handler.start();

				System.out.println("Accepted a new connection from " + s.getInetAddress());
			} catch (IOException e) {
				e.printStackTrace();
			} 
		}
	}
	
}

class ServerClientHandler extends Thread {
	
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private List<ClientTuple> clients;
	private List<Socket> allSockets;
	private ClientTuple myTuple;
	private boolean isConnected = true;
	
	public ServerClientHandler(ClientTuple theTuple, ObjectOutputStream newOutput, ObjectInputStream newInput, List<ClientTuple> clients, List<Socket> sockets) {
		System.out.println("inside ServerClientHandler constructor");
		this.myTuple = theTuple;
		this.output = newOutput;
		this.input = newInput;
		this.clients = clients;
		this.allSockets = sockets;
		
	}
	
	@Override
	public void run() {
		System.out.println("inside client handler");
		
		while(isConnected) {

			Object incoming;
			
			try {
				incoming = input.readObject();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
//				e.printStackTrace();
				System.out.println("client #" + myTuple.getNum() + " is disconnecting");
				
//				close the connection
				try {
					this.input.close();
					this.output.close();
					
					synchronized(clients) {
						int i;
						for(i = 0; i < clients.size(); i++) {
//							TODO add this back in when testing on separate machines
//							if(clients.get(i).getAddr().equals(myTuple.getAddr())) {
							if(clients.get(i).getPort() == myTuple.getPort()) {
								break;
							}
						}
						clients.remove(i);
					}
					
					isConnected = false;
					
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
			}
		}
	}

}
