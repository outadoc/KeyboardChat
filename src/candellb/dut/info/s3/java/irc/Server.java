package candellb.dut.info.s3.java.irc;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

/**
 * Classe serveur ("connecteur").
 * Se charge de créer le socket du serveur et d'attendre la connexion des clients.
 */
public class Server {

	public static final int SERVER_PORT = 13337;

	private List<Client> connectedClients;

	public Server() {
		this.connectedClients = new LinkedList<Client>();

		//on instancie un nouveau facteur et on le range dans la BAL
		Mailbox.setPostman(new Postman(this));
	}

	public void startServer() {
		String[] supportedProtocols = new String[]{"TLSv1", "TLSv1.1", "TLSv1.2"};

		System.setProperty("javax.net.ssl.keyStore", "ks_td_reseau");
		System.setProperty("javax.net.ssl.keyStorePassword", "potato");

		try {
			SSLServerSocketFactory sslFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
			SSLServerSocket serverSocket = (SSLServerSocket) sslFactory.createServerSocket(SERVER_PORT);

			serverSocket.setEnabledProtocols(supportedProtocols);

			System.out.println("server ok to go, listening on port " + SERVER_PORT);
			System.out.println("supported protocols: " + Arrays.toString(supportedProtocols));

			while(true) {
				SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
				Client client = new Client(clientSocket, this);
				Thread clientThread = new Thread(client);
				connectedClients.add(client);

				clientThread.start();
			}

		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public List<Client> getConnectedClients() {
		return connectedClients;
	}
}
