package bjc.rgens.newparser;

/**
 * The exception thrown when something goes wrong while parsing a
 * grammar.
 * 
 * @author student
 *
 */
public class GrammarException extends RuntimeException {
	/*
	 * Serialization ID.
	 */
	private static final long serialVersionUID = -7287427479316953668L;

	/**
	 * Create a new grammar exception with the specified message.
	 * 
	 * @param msg
	 *                The message for this exception.
	 */
	public GrammarException(String msg) {
		super(msg);
	}

	/**
	 * Create a new grammar exception with the specified message and
	 * cause.
	 * 
	 * @param msg
	 *                The message for this exception.
	 * 
	 * @param cause
	 *                The cause of this exception.
	 */
	public GrammarException(String msg, Exception cause) {
		super(msg, cause);
	}
}