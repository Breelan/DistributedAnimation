import java.awt.Point;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;

public class Client extends JFrame implements Observer {
	
	
//	WE NEED:
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
	private static int MY_PORT;
	private static ClientTuple myTuple;
	private OurSprite theSprite;
	
	public static void main(String[] args) {
		MY_PORT = Integer.parseInt(args[0]);
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
		theSprite = new OurSprite();
		theSprite.addObserver(this);
		
		outputStreams = new ArrayList<>();
		outputStreams = Collections.synchronizedList(new ArrayList<>());
		try {
			System.out.println("about to connect to server");
			socket = new Socket(SERVER_ADDR, SERVER_PORT);
			
//			accept id number
			ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
			ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
			
			System.out.println("finished creating oos and ois to server");
			try {
				myNum = (int) input.readObject();
				System.out.println("muNum: " + myNum);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			
			try {
//				otherClients = (ArrayList<ClientTuple>) input.readObject();
				otherClients = (List<ClientTuple>) input.readObject();
				myTuple = new ClientTuple(InetAddress.getLocalHost(), MY_PORT, myNum);
				output.writeObject(myTuple);
				System.out.println("wrote myTuple to server");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			
			if(otherClients.size() > 0) {
				System.out.println("started ClientConnector");
//				create new ClientConnector
				new ClientConnector();
//				connectToOthers.start();
			}
			
//			ClientAcceptor acceptsClients = new ClientAcceptor();
//			acceptsClients.start();
			new ClientAcceptor();
			
//			are you client 0? - start animating
			if(otherClients.size() == 0) {
				System.out.println("about to initialize new point");
				theSprite.setPoint(new Point(50, 50));
			}
			
//			new ServerListener();
			
		} catch (IOException e) {

			e.printStackTrace();
		}
		
	}
	
//	private class ServerListener extends Thread {
//		
//	
////		THIS DOES:
////			adds incoming ClientTuple to the list of ClientTuples
//		
//		@Override
//		public void run() {
//			ObjectInputStream serverInput;
//			try {
//				serverInput = new ObjectInputStream(socket.getInputStream());
//				while(true) {
//					try {
//						ClientTuple newClient = (ClientTuple) serverInput.readObject();
//						
////						TODO figure out where to add this guy
//						
//					} catch (ClassNotFoundException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//
//		}
//			
//	}
	
//	This class exists only to connect the current Client to 
//	all other existing Clients, then it quits
	class ClientConnector extends Thread {
		
		
		public ClientConnector(){
			this.start();
		}
		
		@Override
		public void run() {
			System.out.println("ClientConnector started");
			for(ClientTuple client : otherClients) {
				try {
					Socket s = new Socket(client.getAddr(), client.getPort());
					ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
					ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
					System.out.println("created oos and ois for client #" + client.getNum());
//					send myTuple to the other client
					oos.writeObject(myTuple);
					
//					add oos to list in the correct location
					Client.outputStreams.add(oos);
					
//					start new ClientHandler
					new ClientListener(ois);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private class ClientAcceptor extends Thread {
		
		public ClientAcceptor() {
			try {
				serverSock = new ServerSocket(MY_PORT);
			} catch (IOException e) {
				e.printStackTrace();
			}
			this.start();
		}
		
		@Override
		public void run() {
			System.out.println("inside ClientAcceptor");
			while(true) {
				try {
//					wait for new friend
					Socket s = serverSock.accept();
					System.out.println("someone new connected");
					ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
					ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
					
					try {
//						accept new Client's ClientTuple
						ClientTuple newTuple = (ClientTuple) ois.readObject();
//						figure out where newTuple should go in ClientTuple list
						int index = 0;
						for(ClientTuple client: otherClients) {
							if(client.getNum() < newTuple.getNum()) {
								break;
							}
							index++;
						}
						
//						insert newTuple
						otherClients.add(index, newTuple);
						
						Client.outputStreams.add(index, oos);
						System.out.println("added new oos to outputStreams");
						
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}

					new ClientListener(ois);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
		}
		
	}
	
	
	private class ClientListener extends Thread {

		private ObjectInputStream input;
		
		public ClientListener(ObjectInputStream ois) {
			this.input = ois;
			this.start();
		}
		
		@Override
		public void run() {
			System.out.println("started new ClientListener");
			while(true) {
				
				try {
					// wait for data
					Point newPoint = (Point) input.readObject();
					// update model
					theSprite.setPoint(newPoint);
					
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
//					TODO check for socket closure, handle accordingly
					e.printStackTrace();
				}
			}
		}	
	}


	@Override
	public void update(Observable sprite, Object arg1) {

		Point currPoint = theSprite.getPoint();
		System.out.println(theSprite.getPoint());
//		int x = currPoint.getX() + 5;
		if((int)currPoint.getX() > 100) {
			System.out.println("reached edge of screen");
			theSprite.changeDirection();
//			return;
		} else if ((int)currPoint.getX() < 0) {
			theSprite.changeDirection();
		}
		
//		else {
			if(theSprite.getDirection() < 0) {
//				subtract 5 from x
				theSprite.setPoint(new Point((int)currPoint.getX() - 5, (int)currPoint.getY()));
			} else {
//				add 5 to x
				theSprite.setPoint(new Point((int)currPoint.getX() + 5, (int)currPoint.getY()));
			}
				
//		}
		
//		this.repaint();
		
		
	}
	
	
}
