package bjc.rgens.newparser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.Scanner;

/**
 * Reads {@link RGrammar} from a input stream.
 * 
 * @author student
 *
 */
public class RGrammarParser {
	/**
	 * The exception thrown when something goes wrong while parsing a grammar.
	 * 
	 * @author student
	 *
	 */
	public static class GrammarException extends Exception {
		/*
		 * Serialization ID.
		 */
		private static final long serialVersionUID = -7287427479316953668L;

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
		 *            The message for this exception.
		 * 
		 * @param cause
		 *            The cause of this exception.
		 */
		public GrammarException(String msg, Exception cause) {
			super(msg, cause);
		}
	}

	/**
	 * Read a {@link RGrammar} from an input stream.
	 * 
	 * @param is
	 *            The input stream to read from.
	 * 
	 * @return The grammar represented by the stream.
	 * 
	 * @throws GrammarException
	 *             Thrown if the grammar has a syntax error.
	 */
	public RGrammar readGrammar(InputStream is) throws GrammarException {
		LineNumberReader lnReader = new LineNumberReader(new InputStreamReader(is));

		int blockNo = 0;
		try (Scanner scn = new Scanner(lnReader)) {
			scn.useDelimiter("\\n\\.?\\n");

			RGrammarBuilder build = new RGrammarBuilder();

			while (scn.hasNext()) {
				String block = scn.next();
				blockNo += 1;

				if (block.startsWith("pragma")) {
					handlePragmaBlock(block, build);
				} else if (block.startsWith("[")) {
					handleRuleBlock(block, build);
				} else {
					throw new GrammarException(String.format("Unknown block: %s", lnReader.getLineNumber(), block));
				}
			}
		} catch (GrammarException gex) {
			throw new GrammarException(String.format("Error in block %d at line %d of stream", blockNo, lnReader.getLineNumber()), gex);
		}

		return null;
	}

	/*
	 * Handle reading a block of pragmas
	 */
	private void handlePragmaBlock(String block, RGrammarBuilder build) throws GrammarException {
		LineNumberReader lnReader = new LineNumberReader(new StringReader(block));

		int pragmaNo = 0;
		try (Scanner deblocker = new Scanner(lnReader)) {
			deblocker.useDelimiter("\\n(?!\\t)");

			while (deblocker.hasNext()) {
				String pragma = deblocker.next();
				pragmaNo += 1;

				if (!pragma.startsWith("pragma")) {
					throw new GrammarException(String.format("Illegal line: %s",
							lnReader.getLineNumber(), pragma));
				} else {
					handlePragma(pragma.substring(7), build);
				}
			}
		} catch (GrammarException gex) {
			throw new GrammarException(String.format("Error in pragma %d at line %d", pragmaNo, lnReader.getLineNumber()), gex);
		}
	}

	private void handlePragma(String pragma, RGrammarBuilder build) {
		
	}

	/*
	 * Handle a block of rules.
	 */
	private void handleRuleBlock(String block, RGrammarBuilder build) {
		LineNumberReader lnReader = new LineNumberReader(new StringReader(block));

		try(Scanner scn = new Scanner(lnReader)) {
			
		}
	}
}
