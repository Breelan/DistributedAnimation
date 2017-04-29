import java.io.Serializable;
import java.net.*;

public class ClientTuple implements Serializable{

	private InetAddress ipAddress;
	private int portNum;
	private int clientNum;
	
	public ClientTuple(InetAddress address, int port, int num) {
		this.ipAddress = address;
		this.portNum = port;
		this.clientNum = num;
	}
	
	public InetAddress getAddr() {
		return ipAddress;
	}
	
	public int getPort() {
		return portNum;
	}
	
	public int getNum() {
		return clientNum;
	}
}
