package bjc.rgens.newparser;

import bjc.utils.funcutils.TriConsumer;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Reads {@link RGrammar} from a input stream.
 * 
 * @author student
 *
 */
public class RGrammarParser {
	/**
	 * The exception thrown when something goes wrong while parsing a
	 * grammar.
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

	/*
	 * Pragma impls.
	 */
	private static Map<String, TriConsumer<String, RGrammarBuilder, Integer>> pragmas;

	/*
	 * Initialize pragmas.
	 */
	static {
		pragmas = new HashMap<>();
	}

	/**
	 * Read a {@link RGrammar} from an input stream.
	 * 
	 * @param is
	 *                The input stream to read from.
	 * 
	 * @return The grammar represented by the stream.
	 * 
	 * @throws GrammarException
	 *                 Thrown if the grammar has a syntax error.
	 */
	public RGrammar readGrammar(InputStream is) throws GrammarException {
		LineNumberReader lnReader = new LineNumberReader(new InputStreamReader(is));

		int blockNo = 0;
		try(Scanner scn = new Scanner(lnReader)) {
			scn.useDelimiter("\\n\\.?\\n");

			RGrammarBuilder build = new RGrammarBuilder();

			while(scn.hasNext()) {
				String block = scn.next();
				blockNo += 1;

				handleBlock(build, block, 0);
			}

			return build.toRGrammar();
		} catch(GrammarException gex) {
			throw new GrammarException(String.format("Error in block %d at line %d of stream", blockNo,
					lnReader.getLineNumber()), gex);
		}
	}

	/*
	 * Throughout these, level indicates the nesting level of that
	 * construct.
	 */

	/*
	 * Handles an arbitrary block.
	 */
	private void handleBlock(RGrammarBuilder build, String block, int level) throws GrammarException {
		if(block.startsWith("pragma")) {
			handlePragmaBlock(block, build, level);
		} else if(block.startsWith("[")) {
			handleRuleBlock(block, build, level);
		} else if(block.startsWith("where")) {
			handleWhereBlock(block, build, level);
		} else {
			throw new GrammarException(String.format("Unknown block: %s", block));
		}
	}

	/*
	 * Handle reading a block of pragmas.
	 */
	private void handlePragmaBlock(String block, RGrammarBuilder build, int level) throws GrammarException {
		LineNumberReader lnReader = new LineNumberReader(new StringReader(block));

		int pragmaNo = 0;
		try(Scanner deblocker = new Scanner(lnReader)) {
			deblocker.useDelimiter(String.format("\\n\\t{%d}(?!\\t)", level));

			while(deblocker.hasNext()) {
				String pragma = deblocker.next();
				pragmaNo += 1;

				if(!pragma.startsWith("pragma")) {
					throw new GrammarException(String.format("Illegal line: %s", pragma));
				} else {
					handlePragma(pragma.substring(7), build, level);
				}
			}
		} catch(GrammarException gex) {
			throw new GrammarException(String.format("Error in pragma %d at line %d", pragmaNo,
					lnReader.getLineNumber()), gex);
		}
	}

	private void handlePragma(String pragma, RGrammarBuilder build, int level) throws GrammarException {
		String pragmaName = pragma.substring(0, pragma.indexOf(' '));

		if(pragmas.containsKey(pragmaName)) {
			pragmas.get(pragmaName).accept(pragma.substring(pragma.indexOf(' ')), build, level);
		} else {
			throw new GrammarException(String.format("Unknown pragma %s", pragmaName));
		}
	}

	/*
	 * Handle a block of a rule and multiple cases.
	 */
	private void handleRuleBlock(String block, RGrammarBuilder build, int level) throws GrammarException {
		LineNumberReader lnReader = new LineNumberReader(new StringReader(block));

		int caseNo = 0;
		try(Scanner scn = new Scanner(lnReader)) {
			scn.useDelimiter(String.format("\\n\\t\\t{%d}", level));

			String decl = scn.next();

			String ruleName = decl.substring(0, decl.indexOf(' '));

			if(ruleName.equals("")) {
				throw new GrammarException("The empty string is not a valid rule name");
			}

			build.setCurrentRule(ruleName);

			String initCase = decl.substring(decl.indexOf(' '));
			caseNo += 1;

			handleRuleCase(initCase, build);

			while(scn.hasNext()) {
				String cse = scn.next();
				caseNo += 1;

				handleRuleCase(cse, build);
			}
		} catch(GrammarException gex) {
			throw new GrammarException(
					String.format("Error in case %d at line %d", caseNo, lnReader.getLineNumber()),
					gex);
		}
	}

	/*
	 * Handle a single case of a rule.
	 */
	private void handleRuleCase(String initCase, RGrammarBuilder build) {
		// TODO Auto-generated method stub

	}

	/*
	 * Handle a block of a rule with local rules.
	 */
	private void handleWhereBlock(String block, RGrammarBuilder build, int level) {
		// TODO Auto-generated method stub
	}
}
