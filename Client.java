import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Client extends JPanel implements Observer {
	
	
//	WE NEED:
//	5. view to display the sprite
//	6. timer to run the animations
//	7. POTENTIAL keylistener to control the sprite
	
//	add gui stuff
	private Image train;
	private Image background;
	
	private int myNum;
	private static List<ClientTuple> otherClients;
	static List<ObjectOutputStream> outputStreams;
	private ServerSocket serverSock;
	private Socket socket;
//	private static final String SERVER_ADDR = "localhost";
	private static String MY_ADDR;
//	TODO use this instead of string eventually?
//	private static final InetAddress address;
	private static final int SERVER_PORT = 4003;
	private static int MY_PORT;
	private static ClientTuple myTuple;
	private static OurSprite theSprite;
//	private JPanel drawingPanel;
	
	public static void main(String[] args) {
		MY_ADDR = args[0];
		MY_PORT = Integer.parseInt(args[1]);
//		TODO pull in ip address dynamically
//		int temp = Integer.parseInt(args[0]);
//		serverAddr = (InetAddress)temp;
		JFrame frame = new JFrame();
		frame.setTitle("Client Port #" + MY_PORT);
		frame.setSize(500, 150);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.addWindowListener(new ClientWindowListener());
		frame.add(new Client());
		frame.setVisible(true);
//		new Client().setVisible(true);
		
//		TODO check if this is client 0 and start sprite moving
		if(otherClients.size() == 0) {
			System.out.println("about to initialize new point");
			theSprite.setPoint(new Point(0, 50));
		}
	}
	
	public Client() {
		
//		initialize data structures
		try {
//			hunter = ImageIO.read(new File("images/TheHunter.png"));
			background = ImageIO.read(new File("images/background.jpg"));
			train = ImageIO.read(new File("images/locomotive.png"));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		theSprite = new OurSprite();
		theSprite.setPoint(new Point(-10, -10));
		theSprite.addObserver(this);
		
//		this.addWindowListener(new OurWindowListener());
		
	    otherClients = Collections.synchronizedList(new ArrayList<>());
		outputStreams = Collections.synchronizedList(new ArrayList<>());
		
		
		try {
			System.out.println("about to connect to server");
			socket = new Socket(MY_ADDR, SERVER_PORT);
			
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
				otherClients = (List<ClientTuple>) input.readObject();
				System.out.println("otherClients list is size " + otherClients.size());
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
			}
			
			new ClientAcceptor();
			
//			are you client 0? - start animating
//			if(otherClients.size() == 0) {
//				System.out.println("about to initialize new point");
//				theSprite.setPoint(new Point(50, 50));
//			}
			
		} catch (IOException e) {

			e.printStackTrace();
		}
	}
	
	
//	This class exists only to connect the current Client to 
//	all other existing Clients, then it quits
	class ClientConnector extends Thread {
		
		
		public ClientConnector(){
			this.start();
		}
		
		@Override
		public void run() {
			System.out.println("ClientConnector started");
			synchronized(otherClients) {
				for(ClientTuple client : otherClients) {
					System.out.println("attempting to connect to client #" + client.getNum());
					try {
						Socket s = new Socket(client.getAddr(), client.getPort());
						ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
						ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
						System.out.println("created oos and ois for client #" + client.getNum());
//						send myTuple to the other client
						oos.writeObject(myTuple);
						
//						add oos to list in the correct location
						synchronized(Client.outputStreams) {
							Client.outputStreams.add(oos);
						}
						
//						start new ClientHandler
						new ClientListener(ois, client.getNum());
					} catch (IOException e) {
						e.printStackTrace();
					}
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
					ClientTuple newTuple = null;
					try {
//						accept new Client's ClientTuple
						newTuple = (ClientTuple) ois.readObject();
						System.out.println("accepted ClientTuple from #" + newTuple.getNum());
//						figure out where newTuple should go in ClientTuple list
						int index = 0;
						synchronized(otherClients) {
							for(ClientTuple client: otherClients) {
								if(client.getNum() < newTuple.getNum()) {
									index++;
								} else {
									break;
								}
							}
						}
						
						
//						insert newTuple
						synchronized(otherClients) {
							otherClients.add(index, newTuple);
						}
						
						synchronized(Client.outputStreams) {
							Client.outputStreams.add(index, oos);
						}
						System.out.println("added new oos to outputStreams");
						
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}

					new ClientListener(ois, newTuple.getNum());
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
		}
		
	}
	
	
	private class ClientListener extends Thread {

		private ObjectInputStream input;
		private int threadNum;
		private boolean isConnected = true;
		
		public ClientListener(ObjectInputStream ois, int num) {
			this.input = ois;
			this.threadNum = num;
			this.start();
		}
		
		@Override
		public void run() {
			System.out.println("started new ClientListener");
			while(isConnected) {
				
				try {
					// wait for data
					OurSprite newSprite = (OurSprite) input.readObject();

					// update model
					if(newSprite.getDirection() < 0) {
						newSprite.setPoint(new Point(500, (int)newSprite.getPoint().getY()));
					} else {
						newSprite.setPoint(new Point(0, (int)newSprite.getPoint().getY()));
					}
					
					theSprite.setSprite(newSprite);
					
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
//					check for socket closure, handle accordingly
//					e.printStackTrace();
					System.out.println("client #" + threadNum + " is disconnecting");
					try {
						this.input.close();
						
//						remove client from ClientTuple list and
//						ObjectOutputStream list
						int index = 0;
						synchronized(otherClients) {
							for (ClientTuple client : otherClients) {
								if(client.getNum() == threadNum) {
									otherClients.remove(client);
									break;
								} else {
									index++;
								}
							}
						}
						
						synchronized(outputStreams) {
							outputStreams.remove(index);
						}
						
						isConnected = false;
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					
				}
			}
		}	
	}
	
	public void paintComponent(Graphics g) {
		System.out.println("inside paintcomponent");
		super.paintComponents(g);
		Graphics2D g2 = (Graphics2D) g;
		
		g2.drawImage(background, 0, 0, null);
		
		if(theSprite.getPoint().getX() >= 0 && theSprite.getPoint().getX() < 500) {
			g2.drawImage(train, (int) theSprite.getPoint().getX(), (int) theSprite.getPoint().getY(), null);
		}
	}


	@Override
	public void update(Observable sprite, Object arg1) {

		Point currPoint = theSprite.getPoint();
		repaint();
		
		new Timer().schedule(new TimerTask() {
			
			@Override
			public void run() {
				System.out.println(theSprite.getPoint());
				if((int)currPoint.getX() >= 500) {
					System.out.println("reached edge of screen");
					
					int sendTo = findNextRight();
					System.out.println("I should send to " + sendTo);
					
					if(sendTo < 0) {
						theSprite.changeDirection();
					} else {
						ObjectOutputStream sender;
						synchronized(outputStreams) {
							sender = outputStreams.get(sendTo);
						}
						
						try {
							sender.writeObject(theSprite);
							return;
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					
				} else if ((int)currPoint.getX() < 0) {
					
					int sendTo = findNextLeft();
					System.out.println("I should send to " + sendTo);
					if(sendTo < 0) {
						theSprite.changeDirection();
					} else {
						ObjectOutputStream sender;
						synchronized(outputStreams) {
							sender = outputStreams.get(sendTo);
						}
						try {
							sender.writeObject(theSprite);
							return;
						} catch (IOException e) {
//							TODO recover from this error:
//							remove closed clienttuple
//							remove closed oos
							e.printStackTrace();
						}
					}
				}
				
					if(theSprite.getDirection() < 0) {
//						subtract 1 from x
						theSprite.setPoint(new Point((int)currPoint.getX() - 1, (int)currPoint.getY()));

					} else {
//						add 1 to x
						theSprite.setPoint(new Point((int)currPoint.getX() + 1, (int)currPoint.getY()));
					}
			}

		}, 50);
				
	}
	
	
	public int findNextRight() {
		int index = 0;
		synchronized(otherClients) {
			for(ClientTuple client : otherClients) {
				if(client.getNum() > myNum) {
					return index;
				} else {
					index++;
				}
			}
			return -1;
		}
		
	}
	
	public int findNextLeft() {
		
		synchronized(otherClients) {
			for(int i = otherClients.size() - 1; i >= 0; i--) {
				if(otherClients.get(i).getNum() < myNum) {
					return i;
				} 
			}
			
			return -1;
		}
		
	}

}

class ClientWindowListener implements WindowListener {

	@Override
	public void windowActivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent e) {
//		close all ObjectOutputStreams in outputStreams
		for(ObjectOutputStream stream : Client.outputStreams) {
			try {
				stream.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		
		System.exit(0);
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
	
}
