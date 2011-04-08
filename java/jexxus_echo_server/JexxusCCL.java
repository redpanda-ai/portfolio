import jexxus.client.ClientConnectionListener;

public class JexxusCCL 
implements ClientConnectionListener {
	public void receive(byte[] data) {
		System.out.println("Received message: " + new String(data));
	}
	public void connectionBroken(boolean forced) {
		System.out.println("Connection lost.");
	}
}
