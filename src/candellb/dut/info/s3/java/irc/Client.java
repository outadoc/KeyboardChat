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

			sendMessage("Pseudo : ");
			username = reader.readLine();

			Mailbox.getInstance().sendMessage("*** " + username + " a rejoint le chat\n");

			sendMessage("\nBienvenue sur IUT Relay Chat, " + username + " !\n");
			sendMessage("Vous pouvez désormais dialoguer avec les " +
					(server.getConnectedClients().size() - 1) + " autres utilisateurs connectés.\n");
			sendMessage("Tapez " + COMMAND_QUIT + " pour quitter le chat.\n\n");

			while(!nextLine.equals(COMMAND_QUIT)) {
				sendMessage("> ");
				nextLine = reader.readLine();

				if(!nextLine.equals(COMMAND_QUIT) && !nextLine.isEmpty()) {
					Mailbox.getInstance().sendMessage("<" + username + "> " + nextLine + "\n");
				}
			}

			Mailbox.getInstance().sendMessage("*** " + username + " a quitté le chat\n");
			socket.close();
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			server.getConnectedClients().remove(this);
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
	public void setUsername(String username) {
		this.username = username;
	}
}
