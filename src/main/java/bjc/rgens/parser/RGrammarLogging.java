package bjc.rgens.parser;

/**
 * Handle logging in a centralized way, so as to allow easier collation of the
 * output.
 *
 * @author Ben Culkin
 */
public final class RGrammarLogging {
	/**
	 * Log a message.
	 * @param msg The message to log.
	 * @param vars The variable for the message.
	 */
	public static void log(String msg, Object... vars) {
		System.err.printf(msg, vars);
	}
	
	/**
	 * Log a message, including a newline.
	 * @param msg The message to log.
	 * @param vars The variable for the message.
	 */
	public static void logline(String msg, Object... vars) {
		log(msg + "\n", vars);
	}

	/**
	 * Log an error, including an exception stack trace.
	 * 
	 * @param ex The exception which caused this error.
	 * @param msg The message to log.
	 * @param vars The variables for the message.
	 */
	public static void error(Exception ex, String msg, Object... vars) {
		logline("ERROR: " + msg, vars);

		ex.printStackTrace();

		logline("");
		logline("");
	}

	/**
	 * Log a performance message.
	 * 
	 * @param msg The message to log.
	 * @param vars The variable for the message.
	 */
	public static void perf(String msg, Object... vars) {
		logline("\tPERF: " + msg, vars);
	}

	/**
	 * Log a info message.
	 * 
	 * @param msg The message to log.
	 * @param vars The variable for the message.
	 */
	public static void info(String msg, Object... vars) {
		logline("INFO: " + msg, vars);
	}

	/**
	 * Log a trace message.
	 * 
	 * @param msg The message to log.
	 * @param vars The variable for the message.
	 */
	public static void trace(String msg, Object... vars) {
		logline("\t\tTRACE: " + msg, vars);
	}

	/**
	 * Log a warn message.
	 * 
	 * @param msg The message to log.
	 * @param vars The variable for the message.
	 */
	public static void warn(String msg, Object... vars) {
		logline("WARN: " + msg, vars);
	}

	/**
	 * Log a debug message.
	 * 
	 * @param msg The message to log.
	 * @param vars The variable for the message.
	 */
	public static void debug(String msg, Object... vars) {
		logline("\tDEBUG: " + msg, vars);
	}

	/**
	 * Log a fine message.
	 * 
	 * @param msg The message to log.
	 * @param vars The variable for the message.
	 */
	public static void fine(String msg, Object... vars) {
		logline("\t\tFINE: " + msg, vars);
	}
}
