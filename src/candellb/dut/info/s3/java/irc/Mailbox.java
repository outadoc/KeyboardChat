package candellb.dut.info.s3.java.irc;

import java.io.IOException;

/**
 * Classe boîte aux lettres. Elle est chargée de recueillir et renvoyer les messages qui y sont déposés.
 * Cette classe est un singleton, et peut être obtenue avec getInstance().
 * Affectez un facteur avec setPostman avant de l'utiliser.
 * <p/>
 * Created by outadoc on 25/11/14.
 */
public class Mailbox {

	private static Postman postman;
	private static Mailbox instance;

	/**
	 * Affecte un facteur à la classe.
	 *
	 * @param postman une instance de facteur
	 */
	public static void setPostman(Postman postman) {
		Mailbox.postman = postman;
	}

	public static Mailbox getInstance() {
		if(instance == null) {
			instance = new Mailbox();
		}

		return instance;
	}

	/**
	 * Envoie un message à tous les clients connectés.
	 *
	 * @param message le message à envoyer
	 */
	public synchronized void sendMessage(String message) throws IOException {
		if(postman == null) {
			throw new IllegalStateException("Mailbox must be set before sending a message.");
		}

		System.out.println("broadcasting: " + message);
		postman.broadcastMessageToAllClients(message);

		notifyAll();
	}

}
