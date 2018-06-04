package bjc.rgens.parser;

import bjc.rgens.parser.elements.CaseElement;
import bjc.utils.funcdata.FunctionalList;
import bjc.utils.funcdata.IList;
import bjc.utils.funcutils.ListUtils;
import bjc.utils.funcutils.SetUtils;
import bjc.utils.funcutils.TriConsumer;
import bjc.utils.ioutils.blocks.Block;
import bjc.utils.ioutils.blocks.BlockReader;
import bjc.utils.ioutils.blocks.SimpleBlockReader;

import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Reads {@link RGrammar} from a input stream.
 *
 * @author student
 */
public class RGrammarParser {
	/**
	 *  Whether we are in debug mode or not.
	 */
	public static final boolean DEBUG = false;
	public static final boolean LINES = true;

	/*
	 * Templates for level-dependent delimiters.
	 */
	/* Pragma block delimiter. */
	private static final String TMPL_PRAGMA_BLOCK_DELIM   = "\\R\\t{%d}(?!\\t)";
	/* Rule declaration block delimiter. */
	private static final String TMPL_RULEDECL_BLOCK_DELIM = "\\R\\t\\t{%d}";
	/* Where block delimiter. */
	private static final String TMPL_WHERE_BLOCK_DELIM    = "\\R\\t{%d}(?:in|end)\\R";
	/* Top-level block delimiter. */
	private static final String TMPL_TOPLEVEL_BLOCK_DELIM = "\\R\\t{%d}\\.?\\R";

	/* Pragma impls. */
	private static Map<String, TriConsumer<String, RGrammarBuilder, Integer>> pragmas;

	/* Initialize pragmas. */
	static {
		pragmas = new HashMap<>();

		pragmas.put("initial-rule", (body, build, level) -> {
			int sep = body.indexOf(' ');

			if (sep != -1) {
				String msg = "Initial-rule pragma takes only one argument, the name of the initial rule";
				throw new GrammarException(msg);
			}

			build.setInitialRule(body);
		});

		pragmas.put("despace-rule", (body, build, level) -> {
			int sep = body.indexOf(' ');

			if (sep != -1) {
				String msg = "despace-rule pragma takes only one argument, the name of the rule to despace";
				throw new GrammarException(msg);
			}

			build.despaceRule(body);
		});

		pragmas.put("export-rule", (body, build, level) -> {
			String[] exports = body.split(" ");

			for (String export : exports) {
				build.addExport(export);
			}
		});

		pragmas.put("regex-rule", (body, build, level) -> {
			int nameIndex = body.indexOf(" ");

			if(nameIndex == -1) {
				throw new GrammarException("Regex-rule pragma takes two arguments: the name of the rule to process, then the regex to apply after the rule has been generated.");
			}

			String name = body.substring(0, nameIndex).trim();
			String patt = body.substring(nameIndex + 1).trim();

			//build.regexizeRule(name, patt);
		});

		pragmas.put("suffix-with", (body, build, level) -> {
			String[] parts = body.trim().split(" ");

			if (parts.length < 2) {
				String msg = "Suffix-with pragma takes at least two arguments, the name of the rule to suffix, then what to suffix it with\n\tThis can be more than one token, to get them suffixed as a group";

				throw new GrammarException(msg);
			}

			build.suffixWith(parts[0], Arrays.copyOfRange(parts, 1, parts.length));
		});

		pragmas.put("prefix-with", (body, build, level) -> {
			String[] parts = body.trim().split(" ");

			if (parts.length < 2) {
				String msg = "Prefix-with pragma takes at least two arguments, the name of the rule to prefix, then what to prefix it with\n\tThis can be more than one token, to get them prefixed as a group";

				throw new GrammarException(msg);
			}

			build.prefixWith(parts[0], Arrays.copyOfRange(parts, 1, parts.length));
		});
	}

	/**
	 * Read a {@link RGrammar} from an input stream.
	 *
	 * @param is
	 * 	The input stream to read from.
	 *
	 * @return 
	 * 	The grammar represented by the stream.
	 *
	 * @throws GrammarException
	 * 	Thrown if the grammar has a syntax error.
	 */
	public static RGrammar readGrammar(Reader is) throws GrammarException {
		String dlm = String.format(TMPL_TOPLEVEL_BLOCK_DELIM, 0);

		try (BlockReader reader = new SimpleBlockReader(dlm, is)) {
			if (!reader.hasNextBlock()) {
				throw new GrammarException("At least one top-level block must be present");
			}

			try {
				RGrammarBuilder build = new RGrammarBuilder();

				reader.forEachBlock((block) -> {
					if(DEBUG)
						System.err.printf("Handling top-level block (%s)\n", block);

					handleBlock(build, block.contents, 0);
				});

				if(LINES)
					System.err.printf("%d ", reader.getBlock().endLine);

				return build.toRGrammar();
			} catch (GrammarException gex) {
				String msg = String.format("Error in block (%s)", reader.getBlock());
				throw new GrammarException(msg, gex);
			}
		} catch (Exception ex) {
			throw new GrammarException("Unknown error handling block", ex);
		}
	}

	/* Throughout these, level indicates the nesting level of that construct. */

	/* Handles an arbitrary block. */
	private static void handleBlock(RGrammarBuilder build, String block, 
			int level) throws GrammarException {
		/* Discard empty blocks. */
		if (block.equals("") || block.matches("\\R"))
			return;

		int typeSep = block.indexOf(' ');

		if (typeSep == -1) {
			throw new GrammarException(
			        "A block must start with a introducer, followed by a space, then the rest of the block");
		}

		String blockType = block.substring(0, typeSep).trim();

		if (blockType.equalsIgnoreCase("pragma")) {
			handlePragmaBlock(block, build, level);
		} else if (blockType.startsWith("[")) {
			handleRuleBlock(block, build, level);
		} else if (blockType.equalsIgnoreCase("where")) {
			handleWhereBlock(block, build, level);
		} else if (blockType.startsWith("#")) {
			if(DEBUG)
				System.err.printf("Handled comment block (%s)\n", block);
			/*
			 * Comment block.
			 *
			 * @TODO 10/11/17 Ben Culkin :GrammarComment
			 *
			 * 	Attach these to the grammar somehow so that they
			 * 	can be re-output during formatting.
			 */
			return;
		} else {
			String msg = String.format("Unknown block type: '%s'", blockType);
			throw new GrammarException(msg);
		}
	}

	/* Handle reading a block of pragmas. */
	private static void handlePragmaBlock(String block, RGrammarBuilder build,
	                                      int level) throws GrammarException {
		String dlm = String.format(TMPL_PRAGMA_BLOCK_DELIM, level);
		try (BlockReader pragmaReader = new SimpleBlockReader(dlm, new StringReader(block))) {
			try {
				pragmaReader.forEachBlock((pragma) -> {
					if(DEBUG)
						System.err.printf("Handled pragma block (%s)\n", pragma);

					String pragmaContents = pragma.contents;

					int pragmaSep = pragmaContents.indexOf(' ');

					if (pragmaSep == -1) {
						String msg = "A pragma invocation must consist of the word pragma, followed by a space, then the body of the pragma";

						throw new GrammarException(msg);
					}

					String pragmaLeader = pragmaContents.substring(0, pragmaSep);
					String pragmaBody   = pragmaContents.substring(pragmaSep + 1);

					if (!pragmaLeader.equalsIgnoreCase("pragma")) {
						String msg = String.format("Illegal line leader in pragma block: '%s'", pragmaLeader);

						throw new GrammarException(msg);
					}

					handlePragma(pragmaBody, build, level);
				});
			} catch (GrammarException gex) {
				Block pragma = pragmaReader.getBlock();
				String msg   = String.format("Error in pragma: (%s)", pragma);

				throw new GrammarException(msg, gex);
			}
		} catch (Exception ex) {
			throw new GrammarException("Unknown error handling pragma block", ex);
		}
	}

	/* Handle an individual pragma in a block. */
	private static void handlePragma(String pragma, RGrammarBuilder build,
	                                 int level) throws GrammarException {
		int bodySep = pragma.indexOf(' ');

		if (bodySep == -1)
			bodySep = pragma.length();

		String pragmaName = pragma.substring(0, bodySep);
		String pragmaBody = pragma.substring(bodySep + 1);

		if (pragmas.containsKey(pragmaName)) {
			try {
				if(DEBUG)
					System.err.printf("Handled pragma '%s'\n", pragmaName);

				pragmas.get(pragmaName).accept(pragmaBody, build, level);
			} catch (GrammarException gex) {
				String msg = String.format("Error in pragma '%s'", pragmaName);

				throw new GrammarException(msg, gex);
			}
		} else {
			String msg = String.format("Unknown pragma '%s'", pragmaName);

			throw new GrammarException(msg);
		}
	}

	/* Handle a block of a rule declaration and one or more cases. */
	private static void handleRuleBlock(String ruleBlock, RGrammarBuilder build,
	                                    int level) throws GrammarException {
		String dlm = String.format(TMPL_RULEDECL_BLOCK_DELIM, level);
		try (BlockReader ruleReader = new SimpleBlockReader(dlm, new StringReader(ruleBlock))) {
			try {
				if (ruleReader.hasNextBlock()) {
					/* Rule with a declaration followed by multiple cases. */
					ruleReader.nextBlock();
					Block declBlock = ruleReader.getBlock();

					String declContents = declBlock.contents;
					Rule rl = handleRuleDecl(build, declContents);

					ruleReader.forEachBlock((block) -> {
						/* Ignore comment lines. */
						if(block.contents.trim().startsWith("#")) return;

						handleRuleCase(block.contents, build, rl);
					});
				} else {
					/* Rule with a declaration followed by a single case. */
					handleRuleDecl(build, ruleBlock);
				}
			} catch (GrammarException gex) {
				String msg = String.format("Error in rule case (%s)", ruleReader.getBlock());

				throw new GrammarException(msg, gex);
			}
		} catch (Exception ex) {
			throw new GrammarException("Unknown error handling rule block", ex);
		}
	}

	/* Handle a rule declaration and its initial case. */
	private static Rule handleRuleDecl(RGrammarBuilder build, String declContents) {
		int declSep = declContents.indexOf("\u2192");

		if (declSep == -1) {
			/*
			 * @NOTE
			 * 	We should maybe remove support for the old
			 * 	syntax at some point. However, maybe we don't
			 * 	want to do so so as to make inputting grammars
			 * 	easier.
			 */
			declSep = declContents.indexOf(' ');

			if (declSep == -1) {
				String msg = "A rule must be given at least one case in its declaration, and seperated from that case by \u2192";

				throw new GrammarException(msg);
			}
		}

		String ruleName = declContents.substring(0, declSep).trim();
		String ruleBody = declContents.substring(declSep + 1).trim();

		if (ruleName.equals("")) {
			throw new GrammarException("The empty string is not a valid rule name");
		}

		Rule rul = build.getOrCreateRule(ruleName);

		handleRuleCase(ruleBody, build, rul);

		return rul;
	}

	/* Handle a single case of a rule. */
	private static void handleRuleCase(String cse, RGrammarBuilder build, Rule rul) {
		IList<CaseElement> caseParts = new FunctionalList<>();

		int weight = 1;

		for (String csepart : cse.split(" ")) {
			String partToAdd = csepart.trim();

			if (partToAdd.equals("")) {
				/* Ignore empty parts */
				continue;
			} else if(partToAdd.matches("\\{\\^\\d+\\}")) {
				/* Set case weights */
				weight = Integer.parseInt(partToAdd.substring(2, partToAdd.length() - 1));
			} else {
				caseParts.add(CaseElement.createElement(partToAdd));
			}
		}

		rul.addCase(new NormalRuleCase(caseParts), weight);
	}

	/* Handle a where block (a block with local rules). */
	private static void handleWhereBlock(String block, RGrammarBuilder build,
	                                     int level) throws GrammarException {
		int nlIndex = block.indexOf("\\nin");

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
					@SuppressWarnings("unused")
					Block whereBody = whereReader.next();

					System.err.printf("\tUNIMPLEMENTED WHERE:\n%s\n", whereBody.contents);
					/**
					 * @TODO 10/11/17 Ben Culkin :WhereBlocks
					 * 	Implement where blocks. 
					 *
					 * 	A where block has the context evaluated
					 * 	in a new context, and the body executed
					 * 	in that context.
					 */
				}
			} catch (GrammarException gex) {
				throw new GrammarException(String.format("Error in where block (%s)",
				                           whereReader.getBlock()), gex);
			}
		} catch (Exception ex) {
			throw new GrammarException("Unknown error in where block", ex);
		}
	}
}
