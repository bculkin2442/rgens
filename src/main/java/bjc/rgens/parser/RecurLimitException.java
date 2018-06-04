package bjc.rgens.parser;

/**
 * The exception thrown when a rule exceeds its recurrence limit
 *
 * @author student
 */
public class RecurLimitException extends GrammarException {
	/* Serialization ID. */
	private static final long serialVersionUID = -7287427479316953668L;

	/**
	 * Create a new grammar exception with the specified message.
	 *
	 * @param msg
	 * 	The message for this exception.
	 */
	public RecurLimitException(String msg) {
		super(msg);
	}

	/**
	 * Create a new grammar exception with the specified message and
	 * cause.
	 *
	 * @param msg
	 * 	The message for this exception.
	 *
	 * @param cause
	 * 	The cause of this exception.
	 */
	public RecurLimitException(String msg, Exception cause) {
		super(msg, cause);
	}
}
