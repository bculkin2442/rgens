package bjc.RGens.parser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.function.BiConsumer;

import bjc.utils.exceptions.UnknownPragmaException;
import bjc.utils.funcdata.FunctionalStringTokenizer;
import bjc.utils.gen.WeightedGrammar;

/**
 * Read a weighted grammar from a stream
 * 
 * @author ben
 *
 */
public class GrammarReader {
	private static Map<String, BiConsumer<StringTokenizer, ReaderState>>	actMap;
	private static Map<String, BiConsumer<StringTokenizer, ReaderState>>	pragmaMap;

	static {
		initPragmas();

		actMap = new Hashtable<>();

		actMap.put("#", (stk, rs) -> {
			return;
		});

		actMap.put("pragma", GrammarReader::doPragmas);
		actMap.put("\t", GrammarReader::doCase);
	}

	private static void addSubgrammarPragmas() {
		pragmaMap.put("edit-sub-grammar", GrammarReader::editSubGrammar);
		pragmaMap.put("edit-parent", (stk, rs) -> rs.popGrammar());

		pragmaMap.put("load-sub-grammar", GrammarReader::loadSubGrammar);

		pragmaMap.put("new-sub-grammar",
				(stk, rs) -> rs.pushGrammar(new WeightedGrammar<>()));

		pragmaMap.put("promote", GrammarReader::promoteGrammar);

		pragmaMap.put("remove-sub-grammar",
				GrammarReader::removeSubGrammar);
		pragmaMap.put("remove-rule", GrammarReader::removeRule);

		pragmaMap.put("save-sub-grammar", GrammarReader::saveGrammar);
		pragmaMap.put("subordinate", GrammarReader::subordinateGrammar);
	}

	/**
	 * @param stk
	 *            documented to conform with expected sig
	 */
	private static void debugGrammar(StringTokenizer stk, ReaderState rs) {
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

	private static void doPragmas(StringTokenizer stk, ReaderState rs) {
		String tk = stk.nextToken();

		pragmaMap.getOrDefault(tk, (strk, ras) -> {
			throw new UnknownPragmaException("Unknown pragma " + tk);
		}).accept(stk, rs);
	}

	private static void doRule(String tk, StringTokenizer stk,
			ReaderState rs) {
		rs.currGrammar().addRule(tk);
		rs.setRule(tk);

		doCase(stk, rs);
	}

	private static void editSubGrammar(StringTokenizer stk,
			ReaderState rs) {
		String sgName = stk.nextToken();
		rs.pushGrammar(rs.currGrammar().getSubgrammar(sgName));
	}

	/**
	 * Create a grammar from the contents of the stream
	 * 
	 * @param is
	 *            The stream to read from
	 * @return A grammar, read from the contents of the stream
	 */
	public static WeightedGrammar<String> fromStream(InputStream is) {
		ReaderState rs = new ReaderState();
		rs.toggleUniformity();

		Scanner scn = new Scanner(is);

		while (scn.hasNextLine()) {
			String ln = scn.nextLine();

			if (ln.equals("")) {
				rs.setRule(null);
				continue;
			} else if (ln.startsWith("\t")) {
				doCase(new StringTokenizer(ln.substring(1), " "), rs);
			} else {
				StringTokenizer stk = new StringTokenizer(ln, " ");

				String nxtToken = stk.nextToken();
				actMap.getOrDefault(nxtToken,
						(stak, ras) -> doRule(nxtToken, stak, ras))
						.accept(stk, rs);
			}
		}

		scn.close();
		return rs.currGrammar();
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

		pragmaMap.put("debug", GrammarReader::debugGrammar);
		pragmaMap.put("import-rule", GrammarReader::importRule);
		pragmaMap.put("initial-rule", GrammarReader::initialRule);
		pragmaMap.put("uniform", (stk, rs) -> rs.toggleUniformity());

		pragmaMap.put("multi-prefix-with", GrammarReader::multiPrefixRule);
		pragmaMap.put("multi-suffix-with", GrammarReader::multiSuffixRule);

		pragmaMap.put("prefix-with", GrammarReader::prefixRule);
		pragmaMap.put("suffix-with", GrammarReader::suffixRule);
	}

	private static void loadSubGrammar(StringTokenizer stk,
			ReaderState rs) {
		String sgName = stk.nextToken();
		String fName = stk.nextToken();

		try (FileInputStream fileStream = new FileInputStream(fName)) {

			rs.currGrammar().addSubgrammar(sgName, fromStream(fileStream));
		} catch (IOException ioex) {
			PragmaErrorException peex = new PragmaErrorException(
					"Could not load subgrammar " + sgName + " from file "
							+ fName);

			peex.initCause(ioex);

			throw peex;
		}
	}

	@SuppressWarnings("unused")
	private static void multiPrefixRule(StringTokenizer stk,
			ReaderState rs) {
		// TODO not yet implemented
	}

	@SuppressWarnings("unused")
	private static void multiSuffixRule(StringTokenizer stk,
			ReaderState rs) {
		// TODO not yet implemented
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
