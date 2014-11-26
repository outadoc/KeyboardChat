package candellb.dut.info.s3.java.irc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import javax.net.ssl.SSLSocket;

/**
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

			Mailbox.getInstance().sendMessage("*** " + username + " a rejoint le chat");

			sendMessage("\nBienvenue sur IUT Relay Chat, " + username + " !");
			sendMessage("Vous pouvez désormais dialoguer avec les " +
					(server.getConnectedClients().size() - 1) + " autres utilisateurs connectés.");
			sendMessage("Tapez " + COMMAND_QUIT + " pour quitter le chat.\n");

			while(!nextLine.equals(COMMAND_QUIT)) {
				nextLine = reader.readLine();

				if(!nextLine.equals(COMMAND_QUIT) && !nextLine.isEmpty()) {
					Mailbox.getInstance().sendMessage("<" + username + "> " + nextLine);
				}
			}

			Mailbox.getInstance().sendMessage("*** " + username + " a quitté le chat");
			server.getConnectedClients().remove(this);
			socket.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public void sendMessage(String message) throws IOException {
		writer.write(message + "\n");
		writer.flush();
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
