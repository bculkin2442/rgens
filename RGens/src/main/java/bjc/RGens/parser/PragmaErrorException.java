package bjc.RGens.parser;

/**
 * Exception for error executing a pragma
 * 
 * @author ben
 *
 */
public class PragmaErrorException extends RuntimeException {
	private static final long serialVersionUID = 7245421182038076899L;

	/**
	 * Create a new exception with the given message
	 * 
	 * @param message
	 *            The message of the exception
	 */
	public PragmaErrorException(String message) {
		super(message);
	}
}
