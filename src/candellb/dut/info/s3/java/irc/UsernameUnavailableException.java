package candellb.dut.info.s3.java.irc;

/**
 * Exception lancée quand un client demande un pseudo qui n'est plus disponible.
 * <p/>
 * Created by outadoc on 09/12/14.
 */
public class UsernameUnavailableException extends Exception {

	public UsernameUnavailableException(String s) {
		super(s);
	}

}
