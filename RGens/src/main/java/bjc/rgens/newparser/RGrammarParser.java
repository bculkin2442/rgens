package bjc.rgens.newparser;

import bjc.utils.funcutils.TriConsumer;
import bjc.utils.ioutils.blocks.Block;
import bjc.utils.ioutils.blocks.BlockReader;
import bjc.utils.ioutils.blocks.SimpleBlockReader;

import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

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
	private static final String TMPL_PRAGMA_BLOCK_DELIM = "\\R\\t{%d}(?!\\t)";
	private static final String TMPL_RULEDECL_BLOCK_DELIM = "\\R\\t\\t{%d}";
	private static final String TMPL_WHERE_BLOCK_DELIM = "\\R\\t{%d}(?:in|end)\\R";
	private static final String TMPL_TOPLEVEL_BLOCK_DELIM = "\\R\\t{%d}\\.?\\R";

	/*
	 * Pragma impls.
	 */
	private static Map<String, TriConsumer<String, RGrammarBuilder, Integer>> pragmas;

	/*
	 * Initialize pragmas.
	 */
	static {
		pragmas = new HashMap<>();

		pragmas.put("initial-rule", (body, build, level) -> {
			int sep = body.indexOf(' ');

			if (sep != -1) {
				throw new GrammarException("Initial-rule pragma takes only one argument, the name of the initial rule");
			}

			build.setInitialRule(body);
		});

		pragmas.put("export-rule", (body, build, level) -> {
			String[] exports = body.split(" ");

			for (String export : exports) {
				build.addExport(export);
			}
		});

		pragmas.put("suffix-with", (body, build, level) -> {
			String[] parts = body.trim().split(" ");

			if (parts.length != 2) {
				throw new GrammarException("Suffix-with pragma takes two arguments,"
				+ " the name of the rule to suffix, then what to suffix it with");
			}

			String name = parts[0];
			String suffix = parts[1];

			if (name.equals("")) {
				throw new GrammarException("The empty string is not a valid rule name");
			}

			build.suffixWith(name, suffix);
		});

		pragmas.put("prefix-with", (body, build, level) -> {
			String[] parts = body.trim().split(" ");

			if (parts.length != 2) {
				throw new GrammarException("Prefix-with pragma takes two arguments,"
				+ " the name of the rule to prefix, then what to prefix it with");
			}

			String name = parts[0];
			String prefix = parts[1];

			if (name.equals("")) {
				throw new GrammarException("The empty string is not a valid rule name");
			}

			build.prefixWith(name, prefix);
		});
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
	public static RGrammar readGrammar(Reader is) throws GrammarException {
		try (BlockReader reader = new SimpleBlockReader(String.format(TMPL_TOPLEVEL_BLOCK_DELIM,
			                0), is)) {
			if (!reader.hasNextBlock()) {
				throw new GrammarException("At least one top-level block must be present");
			}

			try {
				RGrammarBuilder build = new RGrammarBuilder();

				reader.forEachBlock((block) -> {
					handleBlock(build, block.contents, 0);
				});

				return build.toRGrammar();
			} catch (GrammarException gex) {
				throw new GrammarException(String.format("Error in block (%s)", reader.getBlock()), gex);
			}
		} catch (Exception ex) {
			throw new GrammarException(String.format("Unknown error handling block"), ex);
		}
	}

	/*
	 * Throughout these, level indicates the nesting level of that construct.
	 */

	/*
	 * Handles an arbitrary block.
	 */
	private static void handleBlock(RGrammarBuilder build, String block,
	                                int level) throws GrammarException {
		/*
		 * Discard empty blocks
		 */
		if (block.equals(""))
			return;

		if (block.equals("\n"))
			return;

		if (block.equals("\r\n"))
			return;

		int typeSep = block.indexOf(' ');

		if (typeSep == -1) {
			throw new GrammarException(
			        "A block must start with a type, followed by a space, then the rest of the block");
		}

		String blockType = block.substring(0, typeSep);

		if (blockType.equalsIgnoreCase("pragma")) {
			handlePragmaBlock(block, build, level);
		} else if (blockType.startsWith("[")) {
			handleRuleBlock(block, build, level);
		} else if (blockType.equalsIgnoreCase("where")) {
			handleWhereBlock(block, build, level);
		} else if (blockType.equalsIgnoreCase("#")) {
			/*
			 * Comment block.
			 */
			return;
		} else {
			throw new GrammarException(String.format("Unknown block type: '%s'", blockType));
		}
	}

	/*
	 * Handle reading a block of pragmas.
	 */
	private static void handlePragmaBlock(String block, RGrammarBuilder build,
	                                      int level) throws GrammarException {
		try (BlockReader pragmaReader = new SimpleBlockReader(String.format(
			                        TMPL_PRAGMA_BLOCK_DELIM, level),
			                new StringReader(block))) {
			try {
				pragmaReader.forEachBlock((pragma) -> {
					String pragmaContents = pragma.contents;

					int pragmaSep = pragmaContents.indexOf(' ');

					if (pragmaSep == -1) {
						throw new GrammarException("A pragma invocation must consist of the word pragma,"
						+ " followed by a space, then the body of the pragma");
					}

					String pragmaLeader = pragmaContents.substring(0, pragmaSep);
					String pragmaBody = pragmaContents.substring(pragmaSep + 1);

					if (!pragmaLeader.equalsIgnoreCase("pragma")) {
						throw new GrammarException(
						        String.format("Illegal line leader in pragma block: '%s'", pragmaLeader));
					}

					handlePragma(pragmaBody, build, level);
				});
			} catch (GrammarException gex) {
				Block pragma = pragmaReader.getBlock();

				throw new GrammarException(String.format("Error in pragma: (%s)", pragma), gex);
			}
		} catch (Exception ex) {
			throw new GrammarException("Unknown error handling pragma block", ex);
		}
	}

	/*msg
	 * Handle an individual pragma in a block.
	 */
	private static void handlePragma(String pragma, RGrammarBuilder build,
	                                 int level) throws GrammarException {
		int bodySep = pragma.indexOf(' ');

		if (bodySep == -1)
			bodySep = pragma.length();

		String pragmaName = pragma.substring(0, bodySep);
		String pragmaBody = pragma.substring(bodySep + 1);

		if (pragmas.containsKey(pragmaName)) {
			try {
				pragmas.get(pragmaName).accept(pragmaBody, build, level);
			} catch (GrammarException gex) {
				throw new GrammarException(String.format("Error in '%s' pragma", pragmaName), gex);
			}
		} else {
			throw new GrammarException(String.format("Unknown pragma named '%s'", pragmaName));
		}
	}

	/*
	 * Handle a block of a rule declaration and one or more cases.
	 */
	private static void handleRuleBlock(String ruleBlock, RGrammarBuilder build,
	                                    int level) throws GrammarException {
		try (BlockReader ruleReader = new SimpleBlockReader(String.format(
			                        TMPL_RULEDECL_BLOCK_DELIM, level),
			                new StringReader(ruleBlock))) {
			try {
				if (ruleReader.hasNextBlock()) {
					/*
					 * Rule with a declaration followed by multiple cases.
					 */
					ruleReader.nextBlock();
					Block declBlock = ruleReader.getBlock();

					String declContents = declBlock.contents;
					handleRuleDecl(build, declContents);

					ruleReader.forEachBlock((block) -> {
						handleRuleCase(block.contents, build);
					});

					build.finishRule();
				} else {
					/*
					 * Rule with a declaration followed by a single case.
					 */
					handleRuleDecl(build, ruleBlock);

					build.finishRule();
				}
			} catch (GrammarException gex) {
				throw new GrammarException(String.format("Error in rule case (%s)",
				                           ruleReader.getBlock()), gex);
			}
		} catch (Exception ex) {
			throw new GrammarException("Unknown error handling rule block", ex);
		}
	}

	/*
	 * Handle a rule declaration and its initial case.
	 */
	private static void handleRuleDecl(RGrammarBuilder build, String declContents) {
		int declSep = declContents.indexOf("\u2192");

		if (declSep == -1) {
			/*
			 * TODO remove support for the old syntax when all of the files are
			 * converted.
			 */
			declSep = declContents.indexOf(' ');

			if (declSep == -1) {
				throw new GrammarException("A rule must be given at least one case in its declaration, and"
				                           + "seperated from that case by \u2192");
			}

			System.out.println(
			        "WARNING: Empty space separating a declaration and its case is deprecated. Use \u2192 instead");
		}

		String ruleName = declContents.substring(0, declSep).trim();
		String ruleBody = declContents.substring(declSep + 1).trim();

		if (ruleName.equals("")) {
			throw new GrammarException("The empty string is not a valid rule name");
		}

		build.startRule(ruleName);

		handleRuleCase(ruleBody, build);
	}

	/*
	 * Handle a single case of a rule.
	 */
	private static void handleRuleCase(String cse, RGrammarBuilder build) {
		build.beginCase();

		for (String csepart : cse.split(" ")) {
			String partToAdd = csepart.trim();

			/*
			 * Ignore empty parts
			 */
			if (partToAdd.equals(""))
				continue;

			build.addCasePart(partToAdd);
		}

		build.finishCase();
	}

	/*
	 * Handle a where block (a block with local rules).
	 */
	private static void handleWhereBlock(String block, RGrammarBuilder build,
	                                     int level) throws GrammarException {
		int nlIndex = block.indexOf("\\n");

		if (nlIndex == -1) {
			throw new GrammarException("Where block must be a context followed by a body");
		}

		String trimBlock = block.substring(nlIndex).trim();

		String whereDelim = String.format(TMPL_WHERE_BLOCK_DELIM, level);

		try (BlockReader whereReader = new SimpleBlockReader(whereDelim,
			                new StringReader(trimBlock))) {
			try {
				Block whereCtx = whereReader.next();

				StringReader ctxReader = new StringReader(whereCtx.contents.trim());
				String ctxDelim = String.format(TMPL_TOPLEVEL_BLOCK_DELIM, level + 1);

				try (BlockReader bodyReader = new SimpleBlockReader(ctxDelim, ctxReader)) {

				}

				Block whereBody = whereReader.next();

				/**
				 * TODO implement where blocks.
				 *
				 * A where block has the context evaluated in a new context, and
				 * the body executed in that context.
				 */
			} catch (GrammarException gex) {
				throw new GrammarException(String.format("Error in where block (%s)",
				                           whereReader.getBlock()), gex);
			}
		} catch (Exception ex) {
			throw new GrammarException("Unknown error in where block", ex);
		}
	}
}
