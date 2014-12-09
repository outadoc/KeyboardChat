package candellb.dut.info.s3.java.irc;

import java.io.IOException;
import java.util.List;

/**
 * Classe Facteur, qui se charge de délivrer les messages envoyés aux clients.
 * <p/>
 * Created by outadoc on 25/11/14.
 */
public class Postman {

	private Server server;

	public Postman(Server server) {
		this.server = server;
	}

	public void broadcastMessageToAllClients(String message) throws IOException {
		List<Client> clientList = server.getConnectedClients();

		for(Client client : clientList) {
			client.sendMessage(message);
		}
	}

}
