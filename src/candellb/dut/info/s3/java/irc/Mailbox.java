package candellb.dut.info.s3.java.irc;

import java.io.IOException;

/**
 * Created by outadoc on 25/11/14.
 */
public class Mailbox {

	private static Postman postman;
	private static Mailbox instance;

	private Mailbox() {
	}

	public static void setPostman(Postman postman) {
		Mailbox.postman = postman;
	}

	public static Mailbox getInstance() {
		if(instance == null) {
			instance = new Mailbox();
		}

		return instance;
	}

	public synchronized void sendMessage(String message) {
		if(postman == null) {
			throw new IllegalStateException("Mailbox must be set before sending a message.");
		}

		try {
			System.out.println("broadcasting: " + message);
			postman.broadcastMessageToAllClients(message);
		} catch(IOException e) {
			e.printStackTrace();
		}

		notifyAll();
	}

}
