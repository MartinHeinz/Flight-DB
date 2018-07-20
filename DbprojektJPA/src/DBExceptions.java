
public class DBExceptions extends Exception {

	/**
	 * Exceptions pre databazu.
	 */
	private static final long serialVersionUID = 1L;
	
	  public DBExceptions() { super(); }
	  public DBExceptions(String message) { super(message); }
	  public DBExceptions(String message, Throwable cause) { super(message, cause); }
	  public DBExceptions(Throwable cause) { super(cause); }
}
