package candellb.dut.info.s3.java.irc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import javax.net.ssl.SSLSocket;

/**
 * Classe chargée de gérer un client tout au long de sa vie.
 * Accueille le client avec un message, lui demande son pseudo, et récupère ses messages.
 * <p/>
 * Created by outadoc on 25/11/14.
 */
public class Client implements Runnable {

	private static final String COMMAND_QUIT = "/quit";
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
			sendMessage("Tapez " + COMMAND_QUIT + " pour quitter le chat.\n\n");

			// Tant que le client n'a pas tapé la commande de déconnexion
			while(!nextLine.equals(COMMAND_QUIT)) {
				// On affiche un prompt, et on demande un message
				sendMessage("> ");
				nextLine = reader.readLine();

				if(!nextLine.equals(COMMAND_QUIT) && !nextLine.isEmpty()) {
					// On envoie son message à tous les autres clients
					Mailbox.getInstance().sendMessage("<" + getUsername() + "> " + nextLine + "\n");
				}
			}
		} catch(IOException | NullPointerException e) {
			e.printStackTrace();
		} finally {
			server.getConnectedClients().remove(this);

			try {
				if(!socket.isClosed()) {
					socket.close();
				}

				Mailbox.getInstance().sendMessage("*** " + getUsername() + " a quitté le chat\n");
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
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
