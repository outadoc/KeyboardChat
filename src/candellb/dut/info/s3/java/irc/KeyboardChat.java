package candellb.dut.info.s3.java.irc;

/**
 * Classe principale du serveur de chat. Contient le main qui lancera le serveur.
 * <p/>
 * Created by outadoc on 25/11/14.
 */
public class KeyboardChat {

	public static void main(String args[]) {
		// On créé un nouveau serveur et on le lance
		Server server = new Server();
		server.startServer();
	}

}
