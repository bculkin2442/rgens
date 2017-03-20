package bjc.rgens.newparser;

import bjc.utils.funcutils.TriConsumer;
import bjc.utils.parserutils.BlockReader;
import bjc.utils.parserutils.BlockReader.Block;

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
	/*
	 * Templates for level-dependent delimiters.
	 */
	private static final String	TMPL_PRAGMA_BLOCK_DELIM		= "\\n\\t{%d}(?!\\t)";
	private static final String	TMPL_RULEDECL_BLOCK_DELIM	= "\\n\\t\\t{%d}";

	/*
	 * Templates for non-level-dependent delimiters.
	 */
	private static final String TOPLEVEL_BLOCK_DELIM = "\\n\\.?\\n";

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
		try(BlockReader reader = new BlockReader(TOPLEVEL_BLOCK_DELIM, new InputStreamReader(is))) {
			if(!reader.hasNextBlock()) {
				throw new GrammarException("At least one top-level block must be present");
			}

			try {
				RGrammarBuilder build = new RGrammarBuilder();

				reader.forEachBlock((block) -> {
					handleBlock(build, block.contents, 0);
				});

				return build.toRGrammar();
			} catch(GrammarException gex) {
				throw new GrammarException(String.format("Error in block (%s)", reader.getBlock()),
						gex);
			}
		} catch(Exception ex) {
			throw new GrammarException(String.format("Unknown error handling block"), ex);
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
		int typeSep = block.indexOf(' ');

		if(typeSep == -1) {
			throw new GrammarException(
					"A block must start with a type, followed by a space, then the rest of the block");
		}

		String blockType = block.substring(0, typeSep);

		if(blockType.equalsIgnoreCase("pragma")) {
			handlePragmaBlock(block, build, level);
		} else if(blockType.startsWith("[")) {
			handleRuleBlock(block, build, level);
		} else if(blockType.equalsIgnoreCase("where")) {
			handleWhereBlock(block, build, level);
		} else {
			throw new GrammarException(String.format("Unknown block type: '%s'", blockType));
		}
	}

	/*
	 * Handle reading a block of pragmas.
	 */
	private void handlePragmaBlock(String block, RGrammarBuilder build, int level) throws GrammarException {
		try(BlockReader pragmaReader = new BlockReader(String.format(TMPL_PRAGMA_BLOCK_DELIM, level),
				new StringReader(block))) {
			try {
				pragmaReader.forEachBlock((pragma) -> {
					String pragmaContents = pragma.contents;

					int pragmaSep = pragmaContents.indexOf(' ');

					if(pragmaSep == -1) {
						throw new GrammarException(
								"A pragma invocation must consist of the word pragma,"
										+ " followed by a space, then the body of the pragma");
					}

					String pragmaLeader = pragmaContents.substring(0, pragmaSep);
					String pragmaBody = pragmaContents.substring(pragmaSep);

					if(!pragmaLeader.equalsIgnoreCase("pragma")) {
						throw new GrammarException(
								String.format("Illegal line leader in pragma block: '%s'",
										pragmaLeader));
					} else {
						handlePragma(pragmaBody, build, level);
					}
				});
			} catch(GrammarException gex) {
				Block pragma = pragmaReader.getBlock();

				throw new GrammarException(String.format("Error in pragma: (%s)", pragma), gex);
			}
		} catch(Exception ex) {
			throw new GrammarException("Unknown error handling pragma block", ex);
		}
	}

	/*
	 * Handle an individual pragma in a block.
	 */
	private void handlePragma(String pragma, RGrammarBuilder build, int level) throws GrammarException {
		int bodySep = pragma.indexOf(' ');

		if(bodySep == -1) bodySep = pragma.length();

		String pragmaName = pragma.substring(0, bodySep);
		String pragmaBody = pragma.substring(bodySep);

		if(pragmas.containsKey(pragmaName)) {
			pragmas.get(pragmaName).accept(pragmaBody, build, level);
		} else {
			throw new GrammarException(String.format("Unknown pragma named '%s'", pragmaName));
		}
	}

	/*
	 * Handle a block of a rule declaration and one or more cases.
	 */
	private void handleRuleBlock(String ruleBlock, RGrammarBuilder build, int level) throws GrammarException {
		try(BlockReader ruleReader = new BlockReader(String.format(TMPL_RULEDECL_BLOCK_DELIM, level),
				new StringReader(ruleBlock))) {
			try {
				if(ruleReader.hasNextBlock()) {
					/*
					 * Rule with a declaration followed by
					 * multiple cases.
					 */
					ruleReader.nextBlock();
					Block declBlock = ruleReader.getBlock();

					String declContents = declBlock.contents;
					handleRuleDecl(build, declContents);

					ruleReader.forEachBlock((block) -> {
						handleRuleCase(block.contents, build);
					});
				} else {
					/*
					 * Rule with a declaration followed by a
					 * single case.
					 */
					handleRuleDecl(build, ruleBlock);
				}
			} catch(GrammarException gex) {
				throw new GrammarException(
						String.format("Error in rule case (%s)", ruleReader.getBlock()), gex);
			}
		} catch(Exception ex) {
			throw new GrammarException("Unknown error handling rule block", ex);
		}
	}

	/*
	 * Handle a rule declaration and its initial case.
	 */
	private void handleRuleDecl(RGrammarBuilder build, String declContents) {
		int declSep = declContents.indexOf("\u2192");

		if(declSep == -1) {
			throw new GrammarException("A rule must be given at least one case in its declaration, and"
					+ "seperated from that case by \u2192");
		}

		String ruleName = declContents.substring(0, declSep).trim();
		String ruleBody = declContents.substring(declSep).trim();

		if(ruleName.equals("")) {
			throw new GrammarException("The empty string is not a valid rule name");
		}

		build.setCurrentRule(ruleName);

		handleRuleCase(ruleBody, build);
	}

	/*
	 * Handle a single case of a rule.
	 */
	private void handleRuleCase(String cse, RGrammarBuilder build) {
		for(String csepart : cse.split(" ")) {
			build.addCasePart(csepart);
		}

		build.finishCase();
	}

	/*
	 * Handle a where block (a block with local rules).
	 */
	private void handleWhereBlock(String block, RGrammarBuilder build, int level) throws GrammarException {
		try(BlockReader whereReader = new BlockReader("", new StringReader(block))) {
			try {
				
			} catch(GrammarException gex) {
				throw new GrammarException(
						String.format("Error in where block (%s)", whereReader.getBlock()),
						gex);
			}
		} catch(Exception ex) {
			throw new GrammarException("Unknown error in where block", ex);
		}
	}
}
