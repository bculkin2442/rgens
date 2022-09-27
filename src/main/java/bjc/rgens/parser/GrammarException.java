package bjc.rgens.parser;

/**
 * The exception thrown when something goes wrong while parsing a grammar.
 *
 * @author student
 */
public class GrammarException extends RuntimeException {
	/* Serialization ID. */
	private static final long serialVersionUID = -7287427479316953668L;

	private String rootMessage;

	/**
	 * Create a new grammar exception with the specified message.
	 *
	 * @param msg
	 *            The message for this exception.
	 */
	public GrammarException(String msg) {
		super(msg);
	}

	/**
	 * Create a new grammar exception with the specified message and cause.
	 *
	 * @param msg
	 *              The message for this exception.
	 *
	 * @param cause
	 *              The cause of this exception.
	 */
	public GrammarException(String msg, Exception cause) {
		super(msg, cause);
	}

	/**
	 * Create a new grammar exception with the specified message.
	 *
	 * @param msg
	 *                The message for this exception.
	 * @param rootMsg
	 *                The root message for this exception
	 */
	public GrammarException(String msg, String rootMsg) {
		super(msg);

		this.rootMessage = rootMsg;
	}

	/**
	 * Create a new grammar exception with the specified message and cause.
	 *
	 * @param msg
	 *                The message for this exception.
	 *
	 * @param cause
	 *                The cause of this exception.
	 * @param rootMsg
	 *                The root message for this exception.
	 */
	public GrammarException(String msg, Exception cause, String rootMsg) {
		super(msg, cause);

		this.rootMessage = rootMsg;
	}

	/**
	 * Get the root cause of this exception.
	 *
	 * @return The root cause of this exception.
	 */
	public String getRootMessage() {
		return rootMessage == null ? getMessage() : rootMessage;
	}
}
