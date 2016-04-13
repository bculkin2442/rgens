package bjc.RGens.parser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.function.BiConsumer;

import bjc.utils.funcdata.FunctionalStringTokenizer;
import bjc.utils.gen.WeightedGrammar;
import bjc.utils.parserutils.RuleBasedConfigReader;

/**
 * Read a grammar from a stream
 * 
 * @author ben
 *
 */
public class RBGrammarReader {
	private static Map<String, BiConsumer<StringTokenizer, ReaderState>> pragmaMap;

	static {
		initPragmas();

	}

	private static void addSubgrammarPragmas() {
		pragmaMap.put("edit-sub-grammar", RBGrammarReader::editSubGrammar);
		pragmaMap.put("edit-parent", (stk, rs) -> rs.popGrammar());

		pragmaMap.put("load-sub-grammar", RBGrammarReader::loadSubGrammar);

		pragmaMap.put("new-sub-grammar",
				(stk, rs) -> rs.pushGrammar(new WeightedGrammar<>()));

		pragmaMap.put("promote", RBGrammarReader::promoteGrammar);

		pragmaMap.put("remove-sub-grammar",
				RBGrammarReader::removeSubGrammar);
		pragmaMap.put("remove-rule", RBGrammarReader::removeRule);

		pragmaMap.put("save-sub-grammar", RBGrammarReader::saveGrammar);
		pragmaMap.put("subordinate", RBGrammarReader::subordinateGrammar);
	}

	private static void debugGrammar(
			@SuppressWarnings("unused") StringTokenizer stk,
			ReaderState rs) {
		System.out.println("Printing rule names: ");

		for (String rul : rs.currGrammar().getRuleNames().toIterable()) {
			System.out.println("\t" + rul);
		}

		System.out.println();
	}

	private static void doCase(StringTokenizer stk, ReaderState rs) {
		int prob = 1;

		if (!rs.isUniform()) {
			prob = Integer.parseInt(stk.nextToken());
		}

		rs.currGrammar().addCase(rs.getRule(), prob,
				new FunctionalStringTokenizer(stk).toList(s -> s));
	}

	private static void editSubGrammar(StringTokenizer stk,
			ReaderState rs) {
		String sgName = stk.nextToken();
		rs.pushGrammar(rs.currGrammar().getSubgrammar(sgName));
	}

	/**
	 * Read a grammar from a stream
	 * 
	 * @param is
	 *            The stream to read from
	 * @return A grammar read from the stream
	 * 
	 *         TODO redo the implementation for this
	 */
	public static WeightedGrammar<String> fromStream(InputStream is) {
		ReaderState rs = new ReaderState();
		rs.toggleUniformity();

		RuleBasedConfigReader<ReaderState> reader = new RuleBasedConfigReader<>(
				null, null, null);

		reader.setStartRule((stk, par) -> par.doWith((left, right) -> {
			rs.currGrammar().addRule(left);
			rs.setRule(left);

			doCase(stk.getInternal(), right);
		}));

		reader.setContinueRule((stk, ras) -> {
			doCase(stk.getInternal(), ras);
		});

		reader.setEndRule((ras) -> {
			ras.setRule(null);
		});

		pragmaMap.forEach((name, act) -> {
			reader.addPragma(name, (tokn, stat) -> {
				act.accept(tokn.getInternal(), stat);
			});
		});

		return reader.fromStream(is, rs).currGrammar();
	}

	private static void importRule(StringTokenizer stk, ReaderState rs) {
		String ruleName = stk.nextToken();
		String sgName = stk.nextToken();

		rs.currGrammar().addGrammarAlias(sgName, ruleName);
	}

	private static void initialRule(StringTokenizer stk, ReaderState rs) {
		String rName = stk.nextToken();

		rs.setInitialRule(rName);
	}

	private static void initPragmas() {
		pragmaMap = new Hashtable<>();

		addSubgrammarPragmas();

		pragmaMap.put("debug", RBGrammarReader::debugGrammar);
		pragmaMap.put("import-rule", RBGrammarReader::importRule);
		pragmaMap.put("initial-rule", RBGrammarReader::initialRule);
		pragmaMap.put("uniform", (stk, rs) -> rs.toggleUniformity());

		pragmaMap.put("multi-prefix-with",
				RBGrammarReader::multiPrefixRule);
		pragmaMap.put("multi-suffix-with",
				RBGrammarReader::multiSuffixRule);

		pragmaMap.put("prefix-with", RBGrammarReader::prefixRule);
		pragmaMap.put("suffix-with", RBGrammarReader::suffixRule);
	}

	private static void loadSubGrammar(StringTokenizer stk,
			ReaderState rs) {
		String sgName = stk.nextToken();
		String fName = stk.nextToken();

		try (FileInputStream fStream = new FileInputStream(fName)) {
			rs.currGrammar().addSubgrammar(sgName, fromStream(fStream));
		} catch (IOException e) {
			PragmaErrorException peex = new PragmaErrorException(
					"Could not load subgrammar " + sgName + " from file "
							+ fName);

			peex.initCause(e);

			throw peex;
		}
	}

	@SuppressWarnings("unused")
	private static void multiPrefixRule(StringTokenizer stk,
			ReaderState rs) {
		// TODO implement me :)
	}

	@SuppressWarnings("unused")
	private static void multiSuffixRule(StringTokenizer stk,
			ReaderState rs) {
		// TODO implement me :)
	}

	private static void prefixRule(StringTokenizer stk, ReaderState rs) {
		String rName = stk.nextToken();
		String prefixToken = stk.nextToken();
		int addProb = rs.isUniform() ? 0
				: Integer.parseInt(stk.nextToken());

		rs.currGrammar().prefixRule(rName, prefixToken, addProb);
	}

	private static void promoteGrammar(StringTokenizer stk,
			ReaderState rs) {
		String gName = stk.nextToken();
		String rName = stk.nextToken();

		WeightedGrammar<String> nwg = rs.currGrammar()
				.getSubgrammar(gName);
		rs.currGrammar().deleteSubgrammar(gName);

		nwg.addSubgrammar(rName, rs.currGrammar());
		rs.setRules(nwg);
	}

	private static void removeRule(StringTokenizer stk, ReaderState rs) {
		String rName = stk.nextToken();

		rs.currGrammar().deleteRule(rName);
	}

	private static void removeSubGrammar(StringTokenizer stk,
			ReaderState rs) {
		String sgName = stk.nextToken();
		rs.currGrammar().deleteSubgrammar(sgName);
	}

	private static void saveGrammar(StringTokenizer stk, ReaderState rs) {
		String sgName = stk.nextToken();
		WeightedGrammar<String> sg = rs.popGrammar();
		rs.currGrammar().addSubgrammar(sgName, sg);
	}

	private static void subordinateGrammar(StringTokenizer stk,
			ReaderState rs) {
		String gName = stk.nextToken();
		WeightedGrammar<String> nwg = new WeightedGrammar<>();

		nwg.addSubgrammar(gName, rs.currGrammar());
		rs.setRules(nwg);
	}

	private static void suffixRule(StringTokenizer stk, ReaderState rs) {
		String rName = stk.nextToken();
		String prefixToken = stk.nextToken();
		int addProb = rs.isUniform() ? 0
				: Integer.parseInt(stk.nextToken());

		rs.currGrammar().suffixRule(rName, prefixToken, addProb);
	}
}
