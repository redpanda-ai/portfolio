import jexxus.server.Server;

public class JexxusEchoServer {
	private Server server;
	public JexxusEchoServer() {
		server = new Server(new EchoSCL(), 15652);
		this.server.startServer();
	}
	public static void main(String [] args) {
		System.out.println("Starting Jexxus Echo Server");
		JexxusEchoServer j = new JexxusEchoServer();
		System.out.println("Jexxus Echo Server Online");
	}
}
