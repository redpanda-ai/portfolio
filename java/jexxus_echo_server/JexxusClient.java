import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import jexxus.client.ClientConnection;
import jexxus.client.ClientConnectionListener;
import jexxus.common.Delivery;

public class JexxusClient {
	
	public JexxusClient() {
	}
	public static void main(String [] args) {
		ClientConnectionListener listener = new JexxusCCL();
		ClientConnection conn = new ClientConnection(listener, "localhost"
		, 15652);
		conn.connect();
		System.out.println("Please type something at the prompt.");
		System.out.print("> ");
		BufferedReader dataIn = new BufferedReader( 
		new InputStreamReader(System.in));
		String temp = "";
		try {
			temp = dataIn.readLine();
		} catch (IOException e) {
			System.out.println("Error getting keyboard input");
		}
		conn.send(temp.getBytes(), Delivery.RELIABLE);
		System.exit(0);
	}
}
