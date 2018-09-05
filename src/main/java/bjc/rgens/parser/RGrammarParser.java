package bjc.rgens.parser;

import bjc.rgens.parser.elements.*;

import bjc.utils.data.IPair;
import bjc.utils.data.Pair;
import bjc.utils.funcdata.FunctionalList;
import bjc.utils.funcdata.IList;
import bjc.utils.funcutils.ListUtils;
import bjc.utils.funcutils.SetUtils;
import bjc.utils.funcutils.StringUtils;
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

import static bjc.rgens.parser.RGrammarLogging.*;
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
	/**
	 * Whether or not to log endline numbers
	 */
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
			List<String> bits = StringUtils.levelSplit(body, " ");

			if (bits.size() != 1) {
				String msg = "Must specify initial rule";
				throw new GrammarException(msg);
			}

			build.setInitialRule(bits.get(0));
		});

		pragmas.put("grammar-name", (body, build, level) -> {
			List<String> bits = StringUtils.levelSplit(body, " ");

			if (bits.size() != 1) {
				String msg = "Must specify grammar name";
				throw new GrammarException(msg);
			}

			build.name = bits.get(0);
		});

		pragmas.put("despace-rule", (body, build, level) -> {
			List<String> bits = StringUtils.levelSplit(body, " ");

			if (bits.size() < 1) {
				throw new GrammarException("Must specify rules to despace");
			}

			for(String bit : bits) {
				build.despaceRule(bit);
			}
		});

		pragmas.put("export-rule", (body, build, level) -> {
			List<String> exports = StringUtils.levelSplit(body, " ");

			if(exports.size() < 1) {
				throw new GrammarException("Must specify rules to export");
			}

			for (String export : exports) {
				build.addExport(export);
			}
		});

		pragmas.put("recur-limit", (body, build, level) -> {
			List<String> parts = StringUtils.levelSplit(body, " ");

			if(parts.size() != 2) {
				throw new GrammarException("Recur-limit pragma takes two arguments: the name of the rule to set the limit for, and the new value of the limit");
			}

			if(!parts.get(1).matches("\\A\\d+\\Z")) {
				throw new GrammarException("Limit value must be an integer");
			}

			build.setRuleRecur(parts.get(0), Integer.parseInt(parts.get(1)));
		});

		pragmas.put("enable-weight", (body, build, level) -> {
			List<String> parts = StringUtils.levelSplit(body, " ");

			if(parts.size() != 1) {
				throw new GrammarException("Enable-weight pragma takes one argument: the name of the rule to set the weight factor for");
			}

			build.setWeight(parts.get(0));
		});

		pragmas.put("enable-descent", (body, build, level) -> {
			List<String> parts = StringUtils.levelSplit(body, " ");

			if(parts.size() != 2) {
				throw new GrammarException("Enable-descent pragma takes two arguments: the name of the rule to set the descent factor for, and the new value of the factor");
			}

			if(!parts.get(1).matches("\\A\\d+\\Z")) {
				throw new GrammarException("Factor value must be an integer");
			}

			build.setDescent(parts.get(0), Integer.parseInt(parts.get(1)));
		});

		pragmas.put("enable-binomial", (body, build, level) -> {
			// @NOTE 9/4/18
			//
			// This can be kind of hard to read right off. Is there
			// a format to put stuff in that looks better and is
			// more readable?
			List<String> parts = StringUtils.levelSplit(body, " ");

			if(parts.size() != 4) {
				throw new GrammarException("Enable-descent pragma takes four arguments: the name of the rule to set the binomial factors for, and the three binomial parameters (target, bound trials)");
			}

			if(!parts.get(1).matches("\\A\\d+\\Z")) {
				throw new GrammarException("Target value must be an integer");
			}
			if(!parts.get(2).matches("\\A\\d+\\Z")) {
				throw new GrammarException("Bound value must be an integer");
			}
			if(!parts.get(3).matches("\\A\\d+\\Z")) {
				throw new GrammarException("Trials value must be an integer");
			}

			build.setBinomial(parts.get(0), Integer.parseInt(parts.get(1)), Integer.parseInt(parts.get(2)), Integer.parseInt(parts.get(3)));
		});

		pragmas.put("regex-rule", (body, build, level) -> {
			int nameIndex = body.indexOf(" ");

			if(nameIndex == -1) {
				throw new GrammarException("Regex-rule pragma takes two arguments: the name of the rule to process, then the regex to apply after the rule has been generated.");
			}

			String name = body.substring(0, nameIndex).trim();
			String patt = body.substring(nameIndex + 1).trim();

			throw new GrammarException("Regexize-rule pragma not yet supported");
			//build.regexizeRule(name, patt);
		});

		pragmas.put("suffix-with", (body, build, level) -> {
			int idx = body.indexOf(" ");

			if (idx == -1) {
				String msg = "Suffix-with pragma takes at least two arguments, the name of the rule to suffix, then what to suffix it with\n\tThis can be more than one token, to get them suffixed as a group";

				throw new GrammarException(msg);
			}

			build.suffixWith(body.substring(0, idx), parseElementString(body.substring(idx + 1)).getLeft());
		});

		pragmas.put("prefix-with", (body, build, level) -> {
			int idx = body.indexOf(" ");

			if (idx == -1) {
				String msg = "Prefix-with pragma takes at least two arguments, the name of the rule to prefix, then what to prefix it with\n\tThis can be more than one token, to get them prefixed as a group";

				throw new GrammarException(msg);
			}

			build.prefixWith(body.substring(0, idx), parseElementString(body.substring(idx + 1)).getLeft());
		});

		/*
		 * @NOTE 9/4/18
		 *
		 * Right now, we ignore additional elements to autovivify. Not
		 * sure yet if this is the desired behavior.
		 *
		 * As I see it, there are a couple of alternatives:
		 * 
		 * 1) Continue what we're doing. This is simple, but seems
		 *                  somewhat inelegant.
		 *
		 * 2) Error if more than one is provided. Even simpler, but also
		 *                  seems inelegant.
		 *
		 * 3) Parse them independantly. Each element is treated as a
		 *                  seperate autovar. Seems simple, but may
		 *                  cause issues with mixing rule & nonrule
		 *                  variables, as well as naming.
		 *
		 * 4) Parse them together. Autovars are stored as cases instead
		 *                  of case elements. Also simple, but may have
		 *                  some odd corner cases, and I can't think of
		 *                  any cases where the additional power would
		 *                  be useful.
		 *
		 *
		 *
		 *
		 *
		 *
		 * As an additional aside, we currently error if we provide
		 * something that isn't a variable definition. This is because
		 * we pull the name for the auto-vivify variable from the
		 * element. If we go with option 4 above, the user will have to
		 * specify a name for the variable, and we should likely add
		 * some check when the variable is made live that it actually
		 * created the variable it said it would.
		 *
		 */
		pragmas.put("autovivify", (body, build, level) -> {
			doAutoVar(body, build, level, false);
		});

		pragmas.put("autovivify-rule", (body, build, level) -> {
			doAutoVar(body, build, level, true);
		});
	}

	private static void doAutoVar(String body, RGrammarBuilder build, int level, boolean isRule) {
		List<String> bits = StringUtils.levelSplit(body, " ");

		if (bits.size() < 1) {
			String msg = "Must specify name of variable and definition to autovivify";
			throw new GrammarException(msg);
		}

		String[] bitArr = bits.toArray(new String[0]);

		IList<CaseElement> elmList = parseElementString(bitArr).getLeft();
		CaseElement elm = elmList.first();

		if (elmList.getSize() > 1) {
			warn("Ignoring %d additional elements for autovivify: %s", elmList.getSize(), elmList.tail());
		}

		if (!(elm instanceof VariableDefCaseElement)) {
			throw new GrammarException(String.format("Autovivify expression must be a variable defn. (expr. %s)", elm));
		}

		{
			String name = ((VariableDefCaseElement)elm).varName;

			if (isRule) build.addAutoRlVar(name, elm);
			else        build.addAutoVar(name, elm);
		}
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

				for(Block block : reader) {
					if(DEBUG)
						System.err.printf("Handling top-level block (%s)\n", block);

					handleBlock(build, block.contents, 0, block.startLine);
				}

				if(LINES)
					System.err.printf("%d ", reader.getBlock().endLine);

				return build.toRGrammar();
			} catch (GrammarException gex) {
				String msg = String.format("Error in block (%s)", reader.getBlock());
				throw new GrammarException(msg, gex, gex.getRootMessage());
			}
		} catch (Exception ex) {
			throw new GrammarException("Unknown error handling block", ex, ex.getMessage());
		}
	}

	/* Throughout these, level indicates the nesting level of that construct. */

	/* Handles an arbitrary block. */
	private static void handleBlock(RGrammarBuilder build, String block, 
			int level, int lineOffset) throws GrammarException {
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
			handlePragmaBlock(block, build, level, lineOffset);
		} else if (blockType.startsWith("[")) {
			handleRuleBlock(block, build, level, lineOffset);
		} else if (blockType.equalsIgnoreCase("where")) {
			handleWhereBlock(block, build, level, lineOffset);
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
	                                      int level, int lineOffset) throws GrammarException {
		String dlm = String.format(TMPL_PRAGMA_BLOCK_DELIM, level);
		try (BlockReader pragmaReader = new SimpleBlockReader(dlm, new StringReader(block))) {
			try {
				for(Block pragma : pragmaReader) {
					pragma.lineOffset = lineOffset;

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

					handlePragma(pragmaBody, build, level, pragma.startLine + lineOffset);
				}
			} catch (GrammarException gex) {
				Block pragma = pragmaReader.getBlock();
				String msg   = String.format("Error in pragma: (%s)", pragma);

				throw new GrammarException(msg, gex, gex.getRootMessage());
			}
		} catch (Exception ex) {
			throw new GrammarException("Unknown error handling pragma block", ex, ex.getMessage());
		}
	}

	/* Handle an individual pragma in a block. */
	private static void handlePragma(String pragma, RGrammarBuilder build,
	                                 int level, int lineOffset) throws GrammarException {
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
	                                    int level, int lineOffset) throws GrammarException {
		String dlm = String.format(TMPL_RULEDECL_BLOCK_DELIM, level);
		try (BlockReader ruleReader = new SimpleBlockReader(dlm, new StringReader(ruleBlock))) {
			try {
				if (ruleReader.hasNextBlock()) {
					/* Rule with a declaration followed by multiple cases. */
					ruleReader.nextBlock();
					Block declBlock = ruleReader.getBlock();
					declBlock.lineOffset = lineOffset;

					String declContents = declBlock.contents;
					Rule rl = handleRuleDecl(build, declContents, lineOffset + declBlock.startLine);

					for(Block block : ruleReader) {
						/* Ignore comment lines. */
						if(block.contents.trim().startsWith("#")) return;

						handleRuleCase(block.contents, build, rl, block.startLine + lineOffset);
					}
				} else {
					/* Rule with a declaration followed by a single case. */
					handleRuleDecl(build, ruleBlock, lineOffset);
				}
			} catch (GrammarException gex) {
				String msg = String.format("Error in rule case (%s)", ruleReader.getBlock());

				throw new GrammarException(msg, gex, gex.getRootMessage());
			}
		} catch (Exception ex) {
			throw new GrammarException("Unknown error handling rule block", ex, ex.getMessage());
		}
	}

	/* Handle a rule declaration and its initial case. */
	private static Rule handleRuleDecl(RGrammarBuilder build, String declContents, int lineOffset) {
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
				String msg = "A rule must be given at least one case in its declaration, and seperated from that case by \u2192 or ' '";

				throw new GrammarException(msg);
			}
		}

		String ruleName = declContents.substring(0, declSep).trim();
		String ruleBody = declContents.substring(declSep + 1).trim();

		if (ruleName.equals("")) {
			throw new GrammarException("The empty string is not a valid rule name");
		}

		Rule rul = build.getOrCreateRule(ruleName);

		handleRuleCase(ruleBody, build, rul, lineOffset);

		return rul;
	}

	/* Handle a single case of a rule. */
	private static void handleRuleCase(String cse, RGrammarBuilder build, Rule rul, int lineOffset) {
		Pair<IList<CaseElement>, Integer> caseParts = parseElementString(cse);

		rul.addCase(new NormalRuleCase(caseParts.getLeft()), caseParts.getRight());
	}

	/* Handle a where block (a block with local rules). */
	private static void handleWhereBlock(String block, RGrammarBuilder build,
			int level, int lineOffset) throws GrammarException {
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
				whereCtx.lineOffset = lineOffset;

				StringReader ctxReader = new StringReader(whereCtx.contents.trim());
				String ctxDelim = String.format(TMPL_TOPLEVEL_BLOCK_DELIM, level + 1);

				try (BlockReader bodyReader = new SimpleBlockReader(ctxDelim, ctxReader)) {
					@SuppressWarnings("unused")
					Block whereBody = whereReader.next();
					whereBody.lineOffset = lineOffset + whereCtx.startLine;

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
							whereReader.getBlock()), gex, gex.getRootMessage());
			}
		} catch (Exception ex) {
			throw new GrammarException("Unknown error in where block", ex, ex.getMessage());
		}
	}

	public static Pair<IList<CaseElement>, Integer> parseElementString(String cses) {
		/*
		 * @NOTE
		 *
		 * This is done using String.split because using things like (
		 * as groupers breaks certain grammars. Maybe it can be used if
		 * some sort of way to set which groupers to use is added?
		 *
		 *  List<String> cseList = StringUtils.levelSplit(cses.trim(), " ");
		 *
		 *  return parseElementString(cseList.toArray(new String[0]));
		 */

		return parseElementString(cses.split(" "));
	}

	public static Pair<IList<CaseElement>, Integer> parseElementString(String... cses) {
		IList<CaseElement> caseParts = new FunctionalList<>();

		int weight = 1;

		int repCount = 1;

		int serialLower = -1;
		int serialUpper = -1;

		int chance = -1;
		boolean doSerial = false;
		boolean doChance = false;

		for (String csepart : cses) {
			String partToAdd = csepart.trim();

			if (partToAdd.equals("")) {
				/* Ignore empty parts */
				continue;
			} else if(partToAdd.matches("\\<\\^\\d+\\>")) {
				/* Set case weights */
				weight = Integer.parseInt(partToAdd.substring(2, partToAdd.length() - 1));
			} else if(partToAdd.matches("\\<&\\d+\\>")) {
				repCount = Integer.parseInt(partToAdd.substring(2, partToAdd.length() - 1));
			} else if(partToAdd.matches("\\<&\\d+\\.\\.\\d+\\>")) {
				serialLower = Integer.parseInt(partToAdd.substring(2, partToAdd.indexOf(".")));
				serialUpper = Integer.parseInt(partToAdd.substring(partToAdd.lastIndexOf(".") + 1, partToAdd.length() - 1));

				doSerial = true;
			} else if(partToAdd.matches("\\<\\?\\d+\\>")) {
				chance = Integer.parseInt(partToAdd.substring(2, partToAdd.length() - 1));

				doChance = true;
			} else if (partToAdd.matches("\\<\\<\\>")) {
				CaseElement elm = caseParts.popLast();

				if(repCount == 0) {
					/* Skip no-reps */
				} else {
					if(doChance) {
						elm = new ChanceCaseElement(elm, chance);

						doChance = false;
					}

					if(doSerial) {
						elm = new SerialCaseElement(elm, serialLower, serialUpper);

						doSerial = false;
					}

					for(int i = 1; i <= repCount; i++) {
						caseParts.add(elm);
					}

					repCount = 1;
				}
			} else if(partToAdd.matches("\\<[^\\>]+\\>")) {
				throw new GrammarException("Unknown parser meta-rule " + partToAdd);
			} else {
				CaseElement elm = CaseElement.createElement(partToAdd);

				if(repCount == 0) {
					/* Skip no-reps */
				} else {
					if(doChance) {
						elm = new ChanceCaseElement(elm, chance);

						doChance = false;
					}

					if(doSerial) {
						elm = new SerialCaseElement(elm, serialLower, serialUpper);

						doSerial = false;
					}

					for(int i = 1; i <= repCount; i++) {
						caseParts.add(elm);
					}

				}

				repCount = 1;
			}
		}

		return new Pair<>(caseParts, weight);
	}
}
