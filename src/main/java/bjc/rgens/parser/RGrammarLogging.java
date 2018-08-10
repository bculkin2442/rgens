package bjc.rgens.parser;

/**
 * Handle logging in a centralized way, so as to allow easier collation of the
 * output.
 *
 * @author Ben Culkin
 */
public final class RGrammarLogging {
	public static void log(String msg, Object... vars) {
		System.err.printf(msg, vars);
	}

	public static void logline(String msg, Object... vars) {
		log(msg + "\n", vars);
	}

	public static void error(Exception ex, String msg, Object... vars) {
		logline("ERROR: " + msg, vars);

		ex.printStackTrace();

		logline("");
		logline("");
	}

	public static void perf(String msg, Object... vars) {
		logline("\tPERF: " + msg, vars);
	}

	public static void info(String msg, Object... vars) {
		logline("INFO: " + msg, vars);
	}

	public static void trace(String msg, Object... vars) {
		logline("\t\tTRACE: " + msg, vars);
	}

	public static void warn(String msg, Object... vars) {
		logline("WARN: " + msg, vars);
	}

	public static void debug(String msg, Object... vars) {
		logline("\tDEBUG: " + msg, vars);
	}

	public static void fine(String msg, Object... vars) {
		logline("\t\tFINE: " + msg, vars);
	}
}
