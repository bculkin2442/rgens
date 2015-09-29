package bjc.RGens.text;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.function.BiConsumer;

import bjc.utils.funcdata.FunctionalStringTokenizer;
import bjc.utils.gen.WeightedGrammar;

public class GrammarReader {
	private static Map<String, BiConsumer<StringTokenizer, ReaderState>>	actMap;
	private static Map<String, BiConsumer<StringTokenizer, ReaderState>>	pragmaMap;

	static {
		initPragmas();

		actMap = new HashMap<>();
		
		actMap.put("#", (stk, rs) -> {
			return;
		});
		
		actMap.put("pragma", GrammarReader::doPragmas);
		actMap.put("\t", GrammarReader::doCase);
	}

	private static void doCase(StringTokenizer stk, ReaderState rs) {
		int prob = 1;

		if (!rs.isUniform()) {
			prob = Integer.parseInt(stk.nextToken());
		}

		rs.getRules().addCase(rs.getRule(), prob, new FunctionalStringTokenizer(stk).toList(s -> s));
	}

	private static void doPragmas(StringTokenizer stk, ReaderState rs) {
		String tk = stk.nextToken();

		pragmaMap.getOrDefault(tk, (strk, ras) -> {
			throw new UnknownPragmaException("Unknown pragma " + tk);
		}).accept(stk, rs);
	}

	private static void doRule(String tk, StringTokenizer stk,
			ReaderState rs) {
		rs.getRules().addRule(tk);

		doCase(stk, rs);
	}

	private static void editSubGrammar(StringTokenizer stk,
			ReaderState rs) {
		String sgName = stk.nextToken();
		rs.pushGrammar(rs.getRules().getSubGrammar(sgName));
	}

	public static WeightedGrammar<String> fromStream(InputStream is) {
		ReaderState rs = new ReaderState();

		Scanner scn = new Scanner(is);

		while (scn.hasNextLine()) {
			String ln = scn.nextLine();
			
			if (ln.equals("")) {
				rs.setRule(null);
				continue;
			}
			
			StringTokenizer stk = new StringTokenizer(ln, " ");

			actMap.getOrDefault(stk.nextToken(), (stak, ras) -> doRule(stk.nextToken(), stak, ras))
					.accept(stk, rs);
		}

		scn.close();
		return rs.getRules();
	}

	private static void initPragmas() {
		pragmaMap = new HashMap<>();

		pragmaMap.put("uniform", (stk, rs) -> rs.toggleUniformity());
		pragmaMap.put("subordinate", GrammarReader::subordinateGrammar);
		pragmaMap.put("promote", GrammarReader::promoteGrammar);
		pragmaMap.put("remove-sub-grammar",
				GrammarReader::removeSubGrammar);
		pragmaMap.put("remove-rule", GrammarReader::removeRule);
		pragmaMap.put("load-sub-grammar", GrammarReader::loadSubGrammar);
		pragmaMap.put("new-sub-grammar",
				(stk, rs) -> rs.pushGrammar(new WeightedGrammar<>()));
		pragmaMap.put("edit-sub-grammar", GrammarReader::editSubGrammar);
		pragmaMap.put("edit-parent", (stk, rs) -> rs.popGrammar());
		pragmaMap.put("save-sub-grammar", GrammarReader::saveGrammar);
	}

	private static void loadSubGrammar(StringTokenizer stk,
			ReaderState rs) {
		String sgName = stk.nextToken();
		String fName = stk.nextToken();

		try {
			rs.getRules().addSubGrammar(sgName,
					fromStream(new FileInputStream(fName)));
		} catch (FileNotFoundException e) {
			throw new PragmaErrorException("Could not load subgrammar "
					+ sgName + " from file " + fName);
		}
	}

	private static void promoteGrammar(StringTokenizer stk,
			ReaderState rs) {
		String gName = stk.nextToken();
		String rName = stk.nextToken();

		WeightedGrammar<String> nwg = rs.getRules().getSubGrammar(gName);
		nwg.addSubGrammar(rName, rs.getRules());
		rs.setRules(nwg);
	}

	private static void removeRule(StringTokenizer stk, ReaderState rs) {
		String rName = stk.nextToken();

		rs.getRules().removeRule(rName);
	}

	private static void removeSubGrammar(StringTokenizer stk,
			ReaderState rs) {
		String sgName = stk.nextToken();
		rs.getRules().removeSubgrammar(sgName);
	}

	private static void saveGrammar(StringTokenizer stk, ReaderState rs) {
		String sgName = stk.nextToken();
		WeightedGrammar<String> sg = rs.popGrammar();
		rs.getRules().addSubGrammar(sgName, sg);
	}

	private static void subordinateGrammar(StringTokenizer stk,
			ReaderState rs) {
		String gName = stk.nextToken();
		WeightedGrammar<String> nwg = new WeightedGrammar<>();

		nwg.addSubGrammar(gName, rs.getRules());
		rs.setRules(nwg);
	}
}
