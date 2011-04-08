import jexxus.common.Delivery;
import jexxus.server.ServerConnection;
import jexxus.server.ServerConnectionListener;

public class EchoSCL implements ServerConnectionListener {

	public EchoSCL() {
		
	}
	public void receive(byte[] ret, ServerConnection sender) {
		System.out.println("Received message: " + new String(ret));
		//Just echo what was sent.
		sender.send(ret,Delivery.RELIABLE);
	}

	public void clientConnected(ServerConnection connected) {
		System.out.println("Client Connected: " + connected.getIP());
	}

	public void clientDisconnected(ServerConnection disconnected
	, boolean forced) {
		System.out.println("Connection lost: " + disconnected);
	}

}
