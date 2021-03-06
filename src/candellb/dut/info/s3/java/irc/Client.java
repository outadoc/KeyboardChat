package candellb.dut.info.s3.java.irc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.SocketException;

import javax.net.ssl.SSLSocket;

/**
 * Classe chargée de gérer un client tout au long de sa vie.
 * Accueille le client avec un message, lui demande son pseudo, et récupère ses messages.
 * <p/>
 * Created by outadoc on 25/11/14.
 */
public class Client implements Runnable {

	private static final String COMMAND_QUIT = "/quit";
	private static final String COMMAND_STOP_SERVER = "/stop";

	private boolean isDead = false;

	private SSLSocket socket;
	private Server server;
	private BufferedWriter writer;
	private BufferedReader reader;
	private String username;

	public Client(SSLSocket socket, Server server) throws IOException {
		this.socket = socket;
		this.server = server;

		this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	}

	@Override
	public void run() {
		try {
			String nextLine = "";

			// Tant qu'on est pas sortis de la boucle avec un break;
			while(true) {
				// On demande son pseudo au client
				sendMessage("Pseudo : ");
				String desiredUsername = reader.readLine();

				try {
					// On essaye de changer le pseudo avec celui désiré; si ça fonctionne, on sort du while
					setUsername(desiredUsername);
					break;
				} catch(UsernameUnavailableException e) {
					// Si le pseudo choisi n'est plus disponible, on continue de boucler et on redemande un pseudo
					e.printStackTrace();
					sendMessage("Désolé, " + desiredUsername + " est déjà pris !\n");
				}
			}

			// Ici, on envoie un message à tous les utilisateurs
			Mailbox.getInstance().sendMessage("*** " + getUsername() + " a rejoint le chat\n");

			// Là, c'est un message privé, il n'est envoyé que sur le socket du client qui vient de se connecter
			sendMessage("\nBienvenue sur IUT Relay Chat, " + getUsername() + " !\n");
			sendMessage("Vous pouvez désormais dialoguer avec les " +
					(server.getConnectedClients().size() - 1) + " autres utilisateurs connectés.\n");
			sendMessage("Tapez " + COMMAND_QUIT + " pour quitter le chat, ou " + COMMAND_STOP_SERVER + " pour arrêter le serveur.\n\n");

			// Tant que le client n'a pas tapé la commande de déconnexion
			while(!nextLine.equals(COMMAND_QUIT)) {
				// On affiche un prompt, et on demande un message
				sendMessage("> ");

				try {
					nextLine = reader.readLine();
				} catch(SocketException e) {
					return;
				}

				if(nextLine.equals(COMMAND_STOP_SERVER)) {
					server.stopServer();
					return;
				}

				if(!isCommand(nextLine) && !nextLine.isEmpty()) {
					// On envoie son message à tous les autres clients
					Mailbox.getInstance().sendMessage("<" + getUsername() + "> " + nextLine + "\n");
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			closeClient();
		}
	}

	/**
	 * Ferme la connexion d'un client, et le retire de la liste des clients.
	 */
	public synchronized void closeClient() {
		// On se rappelle de l'état du client;
		// si cette méthode est appelée deux fois, elle ne fera rien la seconde fois.
		if(isDead) {
			return;
		}

		// Retire le client de la liste des clients connectés
		isDead = true;
		server.getConnectedClients().remove(this);

		try {
			// Ferme la socket si c'est nécessaire, et notifie les autres utilisateurs
			if(!socket.isClosed()) {
				socket.close();
			}

			Mailbox.getInstance().sendMessage("*** " + getUsername() + " a quitté le chat\n");
		} catch(IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Détermine si un message est une commande.
	 *
	 * @param message le message à analyser
	 * @return true si message est une commande, false sinon
	 */
	private boolean isCommand(String message) {
		return (message != null) && (message.equals(COMMAND_QUIT) || message.equals(COMMAND_STOP_SERVER));
	}

	/**
	 * Envoie un message au client.
	 *
	 * @param message le message à envoyer
	 * @throws IOException si une erreur d'entrée/sortie a eu lieu
	 */
	public void sendMessage(String message) throws IOException {
		writer.write(message);
		writer.flush();
	}

	/**
	 * Récupère le pseudo du client.
	 *
	 * @return le pseudo actuel du client
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Change le pseudo du client.
	 *
	 * @param username le nouveau pseudo du client
	 */
	public void setUsername(String username) throws UsernameUnavailableException {
		// On vérifie si le nom d'utilisateur souhaité n'est pas déjà utilisé
		for(Client client : server.getConnectedClients()) {
			if(client.getUsername() != null && client.getUsername().equals(username)) {
				throw new UsernameUnavailableException(username + " est déjà utilisé");
			}
		}

		this.username = username;
	}
}
