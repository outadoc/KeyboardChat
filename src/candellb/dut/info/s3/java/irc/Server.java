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
 * <p/>
 * Created by outadoc on 25/11/14.
 */
public class Server implements Runnable {

	// Le port sur lequel les clients devront se connecter
	public static final int SERVER_PORT = 13337;

	// La liste des clients connectés au serveur
	private List<Client> connectedClients;
	private SSLServerSocket serverSocket;

	public Server() {
		this.connectedClients = new LinkedList<Client>();

		// On instancie un nouveau facteur et on le range dans la BAL
		Mailbox.setPostman(new Postman(this));
	}

	public void run() {
		// On retire SSL des protocoles supportés, trop vieux, trop faillible
		String[] supportedProtocols = new String[]{"TLSv1", "TLSv1.1", "TLSv1.2"};

		// On charge notre trousseau avec le certificat du serveur, Java se charge du reste
		System.setProperty("javax.net.ssl.keyStore", "ks_td_reseau");
		System.setProperty("javax.net.ssl.keyStorePassword", "potato");

		try {
			// On créé un serveur SSL ici, avec SSLServerSocket
			SSLServerSocketFactory sslFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
			serverSocket = (SSLServerSocket) sslFactory.createServerSocket(SERVER_PORT);

			serverSocket.setEnabledProtocols(supportedProtocols);

			// Si on arrive ici, le serveur est lancé !
			System.out.println("server ok to go, listening on port " + SERVER_PORT);
			System.out.println("supported protocols: " + Arrays.toString(supportedProtocols));

			while(true) {
				SSLSocket clientSocket = (SSLSocket) serverSocket.accept();

				// Quand un client se connecte, on l'ajoute à la liste et on le gère sur un nouveau thread
				Client client = new Client(clientSocket, this);
				Thread clientThread = new Thread(client);
				connectedClients.add(client);

				clientThread.start();
			}
		} catch(IOException e) {
			if(connectedClients.isEmpty()) {
				System.out.println("server stopped successfully!");
			} else {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Ferme toutes les connexions au client, puis ferme le serveur.
	 */
	public void stopServer() {
		// Ferme les connexions de tous les clients
		for(Client client : connectedClients) {
			client.closeClient();
		}

		System.out.println("stopping server...");

		try {
			// Ferme la chaussette du serveur
			serverSocket.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Récupère la liste des clients connectés.
	 *
	 * @return une liste contenant les clients encore connectés
	 */
	public List<Client> getConnectedClients() {
		return connectedClients;
	}
}
