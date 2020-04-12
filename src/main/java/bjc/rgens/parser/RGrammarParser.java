package bjc.rgens.parser;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import bjc.rgens.parser.RGrammarBuilder.AffixType;
import bjc.rgens.parser.elements.CaseElement;
import bjc.rgens.parser.elements.ChanceCaseElement;
import bjc.rgens.parser.elements.SerialCaseElement;
import bjc.rgens.parser.elements.VariableDefCaseElement;
import bjc.data.ITree;
import bjc.data.Tree;
import bjc.utils.funcutils.StringUtils;
import bjc.utils.ioutils.blocks.Block;
import bjc.utils.ioutils.blocks.BlockReader;
import bjc.utils.ioutils.blocks.SimpleBlockReader;

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

	private static void doAffixWith(String body, RGrammarBuilder build, int level, AffixType afxType, ITree<String> errs) {
		int idx = body.indexOf(" ");

		if (idx == -1) {
			String fmt = "ERROR: Takes at least two arguments, the name of the rule to affix, then what to affix it with\n\tThis can be more than one token, to get them affixed as a group";

			String msg = String.format(fmt, afxType.toString().toLowerCase());

			errs.addChild(msg);

			return;
		}

		String rName = body.substring(0, idx);

		List<CaseElement> elms = new ArrayList<>();

		parseElementString(body.substring(idx + 1), elms, errs);

		build.affixWith(rName, elms, afxType, errs);
	}

	private static void doAutoVar(List<String> bits, RGrammarBuilder build, int level, boolean isRule, ITree<String> errs) {
		if (bits.size() < 1) {
			String msg = "Must specify name of variable and definition to autovivify";

			errs.addChild(msg);

			return;
		}

		String[] bitArr = bits.toArray(new String[0]);

		List<CaseElement> elmList = new ArrayList<>();

		parseElementString(bitArr, elmList, errs);
		CaseElement elm = elmList.get(0);

		if (elmList.size() > 1) {
			String msg = String.format("WARN: Ignoring %d additional elements for autovivify: %s", elmList.size(), elmList.subList(1, elmList.size()));

			errs.addChild(msg);

			return;
		}

		if (!(elm instanceof VariableDefCaseElement)) {
			String msg = String.format("ERROR: Autovivify expression must be a variable defn. (expr. %s)", elm);

			errs.addChild(msg);

			return;
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
	public static RGrammar readGrammar(Reader is, LoadOptions lopts, ITree<String> errs) throws GrammarException {
		String dlm = String.format(TMPL_TOPLEVEL_BLOCK_DELIM, 0);

		try (BlockReader reader = new SimpleBlockReader(dlm, is)) {
			if (!reader.hasNextBlock()) {
				errs.addChild("ERROR: At least one top-level block must be present");

				return null;
			}

				RGrammarBuilder build = new RGrammarBuilder();

				for(Block block : reader) {
					if(lopts.doTrace) {
						String msg = String.format("TRACE: Handling top-level block (%s)\n", block);

						errs.addChild(msg);
					}

					String msg = String.format("INFO: Block %d (%d-%d) (offset %d, %d-%d)", block.blockNo, block.startLine, block.endLine, block.lineOffset, block.lineOffset + block.startLine, block.lineOffset + block.endLine);

					ITree<String> kid = new Tree<>(msg);

					handleBlock(build, block.contents, 0, block.startLine, lopts, kid);

					if (kid.size() > 0) errs.addChild(kid);
				}

				if(lopts.doTrace) {
					errs.addChild(String.format("TRACE: Ended at line %d ", reader.getBlock().endLine));
				}

				return build.toRGrammar();
		} catch (IOException ioex) {
			String msg = String.format("ERROR: Unknown I/O error: %s", ioex.getMessage());

			errs.addChild(msg);
		}

		return null;
	}

	/* 
	 * Throughout these, level indicates the nesting level of that construct,
	 * and lineOffset indicates the total number of lines to adjust the block
	 * line numbers by.
	 */

	/* Handles an arbitrary block. */
	private static void handleBlock(RGrammarBuilder build, String block, int level, int lineOffset, LoadOptions lopts, ITree<String> errs) {
		/* Discard empty blocks. */
		if (block.equals("") || block.matches("\\R")) return;

		int typeSep = block.indexOf(' ');

		if (typeSep == -1) {
			errs.addChild("ERROR: A block must start with a introducer, followed by a space, then the rest of the block");
		}

		String blockType = block.substring(0, typeSep).trim();

		if (blockType.equalsIgnoreCase("pragma")) {
			handlePragmaBlock(block, build, level, lineOffset, lopts, errs);
		} else if (blockType.startsWith("[")) {
			handleRuleBlock(block, build, level, lineOffset, lopts, errs);
		} else if (blockType.equalsIgnoreCase("where")) {
			handleWhereBlock(block, build, level, lineOffset, lopts, errs);
		} else if (blockType.startsWith("#")) {
			if(lopts.doTrace) {
				String msg = String.format("TRACE: Handled comment block (%s)\n", block);

				errs.addChild(msg);
			}

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
			String msg = String.format("ERROR: Unknown block type: '%s'", blockType);

			errs.addChild(msg);
		}
	}

	/* Handle reading a block of pragmas. */
	private static void handlePragmaBlock(String block, RGrammarBuilder build, int level, int lineOffset, LoadOptions lopts, ITree<String> errs) {
		String dlm = String.format(TMPL_PRAGMA_BLOCK_DELIM, level);

		try (BlockReader pragmaReader = new SimpleBlockReader(dlm, new StringReader(block))) {
			for(Block pragma : pragmaReader) {
				pragma.lineOffset = lineOffset;

				if(lopts.doTrace) {
					System.err.printf("TRACE: Handled pragma block (%s)\n", pragma);
				}

				String pragmaContents = pragma.contents;

				int pragmaSep = pragmaContents.indexOf(' ');

				if (pragmaSep == -1) {
					String msg = "ERROR: A pragma invocation must consist of the word pragma, followed by a space, then the body of the pragma";

					errs.addChild(msg);
					return;
				}

				String pragmaLeader = pragmaContents.substring(0, pragmaSep);
				String pragmaBody   = pragmaContents.substring(pragmaSep + 1);

				if (!pragmaLeader.equalsIgnoreCase("pragma")) {
					String msg = String.format("ERROR: Illegal line leader in pragma block: '%s'", pragmaLeader);

					errs.addChild(msg);
					return;
				}

				handlePragma(pragmaBody, build, level, pragma.startLine + lineOffset, lopts, errs);
			}
		} catch (IOException ioex) {
			String msg = String.format("ERROR: Unknown I/O error in pragma block: %s", ioex.getMessage());

			errs.addChild(msg);
		}
	}

	/* Handle an individual pragma in a block. */
	private static void handlePragma(String pragma, RGrammarBuilder build, int level, int lineOffset, LoadOptions lopts, ITree<String> errs) {
		int bodySep = pragma.indexOf(' ');

		if (bodySep == -1) bodySep = pragma.length();

		String pragmaName = pragma.substring(0, bodySep);
		String pragmaBody = pragma.substring(bodySep + 1);

		if(lopts.doTrace) {
			String msg = String.format("TRACE: Handled pragma '%s'\n", pragmaName);

			errs.addChild(msg);
		}

		// Pragma bits
		List<String> bits = StringUtils.levelSplit(pragmaBody, " ");

		String fmt = String.format("INFO: Pragma '%s'", pragmaName);
		ITree<String> kid = new Tree<>(fmt);

		switch (pragmaName) {
		case "initial-rule":
			{
				if (bits.size() != 1) {
					kid.addChild("ERROR: Must specify initial rule");

					break;
				}

				build.setInitialRule(bits.get(0), kid);
			}
			break;
		case "grammar-name":
			{
				if (bits.size() != 1) {
					kid.addChild("ERROR: Must specify grammar name");

					break;
				}

				build.name = bits.get(0);
			}
			break;
		case "despace-rule":
			{
				if (bits.size() < 1) {
					kid.addChild("ERROR: Must specify at least one rule to despace");

					break;
				}

				for(String bit : bits) {
					build.despaceRule(bit, kid, lopts.doTrace);
				}
			}
			break;
		case "export-rule":
			{
				if(bits.size() < 1) {
					kid.addChild("ERROR: Must specify rules to export");

					break;
				}

				for (String export : bits) {
					build.addExport(export);
				}
			}
			break;
		case "recur-limit":
			{
				if(bits.size() != 2) {
					kid.addChild("ERROR: Takes two arguments: the name of the rule to set the limit for, and the new value of the limit");

					break;
				}

				if(!bits.get(1).matches("\\A\\d+\\Z")) {
					kid.addChild("ERROR: Limit value must be an integer");

					break;
				}

				build.setRuleRecur(bits.get(0), Integer.parseInt(bits.get(1)), kid);
			}
			break;
		case "enable-weight":
			{
				if(bits.size() != 1) {
					kid.addChild("ERROR: Takes one argument: the name of the rule to enable standard weighting for");
				}

				build.setWeight(bits.get(0), kid);
			}
			break;
		case "enable-descent":
			{
				if(bits.size() != 2) {
					kid.addChild("ERROR: Takes two arguments: The name of the rule to set to descent mode, and the value of the descent factor");

					break;
				}

				if(!bits.get(1).matches("\\A\\d+\\Z")) {
					kid.addChild("ERROR: Factor value must be an integer");

					break;
				}

				build.setDescent(bits.get(0), Integer.parseInt(bits.get(1)), kid);
			}
			break;
		case "enable-binomial":
			{
				// @NOTE 9/4/18
				//
				// This can be kind of hard to read right off. Is there
				// a format to put stuff in that looks better and is
				// more readable?

				if(bits.size() != 4) {
					kid.addChild("ERROR: Takes four arguments: the name of the rule to set the binomial factors for, and the three binomial parameters (target, bound, trials) (target/bound chance of success)");

					break;
				}

				if(!bits.get(1).matches("\\A\\d+\\Z")) {
					kid.addChild("ERROR: Target value must be an integer");

					break;
				}

				if(!bits.get(2).matches("\\A\\d+\\Z")) {
					kid.addChild("ERROR: Bound value must be an integer");

					break;
				}

				if(!bits.get(3).matches("\\A\\d+\\Z")) {
					kid.addChild("ERROR: Trials value must be an integer");

					break;
				}

				build.setBinomial(bits.get(0), Integer.parseInt(bits.get(1)), Integer.parseInt(bits.get(2)), Integer.parseInt(bits.get(3)), kid);
			}
			break;
		case "find-replace-rule":
			{
				/*
				 * @NOTE 4/9/18
				 *
				 * Consider if we want to replace this with something more akin
				 * to the `definer` feature from DiceLang. This will work fine
				 * in most cases, but there are some cases where you'd want the
				 * extra power. No examples are apparent at the moment.
				 */
				if(bits.size() != 3) {
					kid.addChild("ERROR: Takes three arguments: the name of the rule to process, then the find/replace pair to apply after the rule has been generated.");

					break;
				}

				build.findReplaceRule(bits.get(0), bits.get(1), bits.get(2), kid);
			}
			break;
		case "reject-rule":
			{
				if(bits.size() != 2) {
					kid.addChild("ERROR: Takes two arguments: the name of the rule to process, then the rejection pattern to apply after the rule has been generated.");

					break;
				}

				build.rejectRule(bits.get(0), bits.get(1), kid);
			}
			break;
		case "prefix-with":
			{
				doAffixWith(pragmaBody, build, level, AffixType.PREFIX, kid);
			}
			break;
		case "suffix-with":
			{
				doAffixWith(pragmaBody, build, level, AffixType.SUFFIX, kid);
			}
			break;
		case "circumfix-with":
			{
				doAffixWith(pragmaBody, build, level, AffixType.CIRCUMFIX, kid);
			}
			break;
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
		case "autovivify":
			{
				doAutoVar(bits, build, level, false, kid);
			}
			break;
		case "autovivify-rule":
			{
				doAutoVar(bits, build, level, true, kid);
			}
			break;
		default: 
			{
				String msg = String.format("ERROR: Unknown pragma '%s'", pragmaName);

				kid.addChild(msg);
			}
		}

		if (kid.size() > 0) errs.addChild(kid);
	}

	/* Handle a block of a rule declaration and one or more cases. */
	private static void handleRuleBlock(String ruleBlock, RGrammarBuilder build, int level, int lineOffset, LoadOptions lopts, ITree<String> errs) {
		String dlm = String.format(TMPL_RULEDECL_BLOCK_DELIM, level);

		try (BlockReader ruleReader = new SimpleBlockReader(dlm, new StringReader(ruleBlock))) {
			ITree<String> kid = new Tree<>();

			if (ruleReader.hasNextBlock()) {
				/* Rule with a declaration followed by multiple cases. */
				ruleReader.nextBlock();
				Block declBlock = ruleReader.getBlock();
				declBlock.lineOffset = lineOffset;

				String declContents = declBlock.contents;
				Rule rl = handleRuleDecl(build, declContents, lineOffset + declBlock.startLine, kid);

				// Error occured during rule processing
				if (rl == null) return;

				for(Block block : ruleReader) {
					/* Ignore comment lines. */
					if(block.contents.trim().startsWith("#")) return;

					handleRuleCase(block.contents, build, rl, block.startLine + lineOffset, kid);
				}
			} else {
				/* Rule with a declaration followed by a single case. */
				handleRuleDecl(build, ruleBlock, lineOffset, kid);
			}

			if (kid.size() > 0) errs.addChild(kid);
		} catch (IOException ex) {
			String msg = String.format("ERROR: Unknown error handling rule block (%s)",ex.getMessage());

			errs.addChild(msg);
		}
	}

	/* Handle a rule declaration and its initial case. */
	private static Rule handleRuleDecl(RGrammarBuilder build, String declContents, int lineOffset, ITree<String> errs) {
		int declSep = declContents.indexOf("\u2192");

		if (declSep == -1) {
			/*
			 * @NOTE
			 * 	We should maybe remove support for the old
			 * 	syntax at some point.
			 *
			 * 	We don't want to do so so as to make inputting grammars easier,
			 * 	since that character is not easy to type on a normal keyboard,
			 * 	and takes 4 keystrokes in vim as composed to 1 for the normal
			 * 	one.
			 */
			declSep = declContents.indexOf(' ');

			if (declSep == -1) {
				String msg = "A rule must be given at least one case in its declaration, and seperated from that case by \u2192 or ' '";

				errs.addChild(msg);

				return null;
			}
		}

		String ruleName = declContents.substring(0, declSep).trim();
		String ruleBody = declContents.substring(declSep + 1).trim();

		if (ruleName.equals("")) {
			errs.addChild("ERROR: The empty string is not a valid rule name");

			return null;
		}

		errs.setHead("INFO: Rule " + ruleName);

		Rule rul = build.getOrCreateRule(ruleName, errs);

		if (rul == null) return null;

		handleRuleCase(ruleBody, build, rul, lineOffset, errs);

		return rul;
	}

	/* Handle a single case of a rule. */
	private static void handleRuleCase(String cse, RGrammarBuilder build, Rule rul, int lineOffset, ITree<String> errs) {
		List<CaseElement> elms = new ArrayList<>();

		int weights = parseElementString(cse, elms, errs);

		rul.addCase(new NormalRuleCase(elms), weights);
	}

	/* Handle a where block (a block with local rules). */
	private static void handleWhereBlock(String block, RGrammarBuilder build, int level, int lineOffset, LoadOptions lopts, ITree<String> errs) { 
		int nlIndex = block.indexOf("\\nin");

		if (nlIndex == -1) {
			errs.addChild("ERROR: Where block must be a context followed by a body");

			return;
		}

		String trimBlock = block.substring(nlIndex).trim();

		String whereDelim = String.format(TMPL_WHERE_BLOCK_DELIM, level);

		try (BlockReader whereReader = new SimpleBlockReader(whereDelim, new StringReader(trimBlock))) {
			Block whereCtx = whereReader.next();
			whereCtx.lineOffset = lineOffset;

			StringReader ctxReader = new StringReader(whereCtx.contents.trim());
			String ctxDelim = String.format(TMPL_TOPLEVEL_BLOCK_DELIM, level + 1);

			try (BlockReader bodyReader = new SimpleBlockReader(ctxDelim, ctxReader)) {
				//@SuppressWarnings("unused")
				Block whereBody = whereReader.next();
				whereBody.lineOffset = lineOffset + whereCtx.startLine;

				String msg = String.format("UNIMPLEMENTED WHERE:\n%s\n", whereBody.contents);
				errs.addChild(msg);
				/**
				 * @TODO 10/11/17 Ben Culkin :WhereBlocks
				 * 	Implement where blocks. 
				 *
				 * 	A where block has the context evaluated
				 * 	in a new context, and the body executed
				 * 	in that context.
				 */
			}
		} catch (IOException ioex) {
			//String msg = String.format("Unknown error in where block (%s)", ioex.getMessage());
		}
	}

	public static int parseElementString(String cses, List<CaseElement> elms) {
		return parseElementString(cses, elms, new Tree<>());
	}

	public static int parseElementString(String cses, List<CaseElement> elms, ITree<String> errs) {
		/*
		 * @NOTE
		 *
		 * This is done using String.split because using things like (
		 * as groupers breaks certain grammars. Maybe it can be used if
		 * some sort of way to set which groupers to use is added?
		 *
		 *  List<String> cseList = LevelSplitter.def.levelSplit(cses.trim(), " ");
		 *
		 *  return parseElementString(cseList.toArray(new String[0]));
		 */

		return parseElementString(cses.split(" "), elms, errs);
	}

	public static int parseElementString(String[] cses, List<CaseElement> elms) {
		return parseElementString(cses, elms, new Tree<>());
	}

	public static int parseElementString(String[] cses, List<CaseElement> caseParts, ITree<String> errs) {
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
				/*
				 * @NOTE
				 *
				 * One, am I even using this feature anywhere?
				 * As far as I can tell, this says to apply the
				 * current set of case part rules to the
				 * previous case part. This may be useful in
				 * certain cases, but none come to mind at the
				 * moment.
				 *
				 * @PERF
				 *
				 * For performance reasons, we may want to
				 * consider setting the chance/serial values as
				 * a setting on CaseElement, instead of having
				 * their own CaseElement type.
				 */
				CaseElement elm = caseParts.remove(caseParts.size() - 1);

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

		return weight;
	}
}
