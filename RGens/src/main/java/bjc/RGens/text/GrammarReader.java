package bjc.RGens.text;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.StringTokenizer;

import bjc.utils.FunctionalStringTokenizer;
import bjc.utils.data.FunctionalList;
import bjc.utils.gen.WeightedGrammar;

public class GrammarReader {
	private void doCase(StringTokenizer stk, ReaderState rs) {
		int prob = 1;

		if (!rs.isUniform()) {
			prob = Integer.parseInt(stk.nextToken());
		}

		rs.getRules().addCase(rs.getRule(), prob, FunctionalList
				.fromString(new FunctionalStringTokenizer(stk), s -> s));
	}

	private void doPragmas(StringTokenizer stk, ReaderState rs) {
		String tk = stk.nextToken();
		switch (tk) {
			case "uniform": // set probability requirement for cases
				rs.toggleUniformity();
				break;
			case "subordinate": // make this grammar subordinate to a new
								// one
				subordinateGrammar(stk, rs);
				break;
			case "promote": // Invert the positions of this grammar and one
							// of its subgrammars
				promoteGrammar(stk, rs);
				break;
			case "removeSubGrammar": // Delete a subgrammar
				removeSubGrammar(stk, rs);
				break;
			case "removeRule": // Remove a rule and all of its associated
								// cases
				removeRule(stk, rs);
				break;
			case "loadSubGrammar": // Load a subgrammar from a file
				loadSubGrammar(stk, rs);
				break;
			case "newSubGrammar":
				rs.pushGrammar(new WeightedGrammar<>());
				break;
			case "editSubgrammar":
				editSubGrammar(stk, rs);
				break;
			case "editParent":
				rs.popGrammar();
				break;
			case "saveGrammar":
				saveGrammar(stk, rs);
				break;
			default: // woops. unknown pragma
				throw new UnknownPragmaException(
						"Unknown pragma named: " + tk);
		}
	}

	private void doRule(String tk, StringTokenizer stk, ReaderState rs) {
		rs.getRules().addRule(tk);
		
		doCase(stk, rs);
	}

	private void editSubGrammar(StringTokenizer stk, ReaderState rs) {
		String sgName = stk.nextToken();
		rs.pushGrammar(rs.getRules().getSubGrammar(sgName));
	}

	public WeightedGrammar<String> fromStream(InputStream is) {
		ReaderState rs = new ReaderState();

		Scanner scn = new Scanner(is);

		while (scn.hasNextLine()) {
			String ln = scn.nextLine();
			if (ln.equals("")) { // end this rules cases
				rs.setRule(null);
				continue;
			}
			StringTokenizer stk = new StringTokenizer(ln, " ");

			String tk = stk.nextToken();

			switch (tk) {
				case "#": // comments
					break;
				case "pragma": // twiddle internal state
					doPragmas(stk, rs);
					break;
				case "\t":
					doCase(stk, rs);
					break;
				default:
					doRule(tk, stk, rs);
					break;
			}
		}

		scn.close();
		return rs.getRules();
	}

	private void loadSubGrammar(StringTokenizer stk, ReaderState rs) {
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

	private void promoteGrammar(StringTokenizer stk, ReaderState rs) {
		String gName = stk.nextToken();
		String rName = stk.nextToken();

		WeightedGrammar<String> nwg = rs.getRules().getSubGrammar(gName);
		nwg.addSubGrammar(rName, rs.getRules());
		rs.setRules(nwg);
	}

	private void removeRule(StringTokenizer stk, ReaderState rs) {
		String rName = stk.nextToken();

		rs.getRules().removeRule(rName);
	}

	private void removeSubGrammar(StringTokenizer stk, ReaderState rs) {
		String sgName = stk.nextToken();
		rs.getRules().removeSubgrammar(sgName);
	}

	private void saveGrammar(StringTokenizer stk, ReaderState rs) {
		String sgName = stk.nextToken();
		WeightedGrammar<String> sg = rs.popGrammar();
		rs.getRules().addSubGrammar(sgName, sg);
	}

	private void subordinateGrammar(StringTokenizer stk, ReaderState rs) {
		String gName = stk.nextToken();
		WeightedGrammar<String> nwg = new WeightedGrammar<>();

		nwg.addSubGrammar(gName, rs.getRules());
		rs.setRules(nwg);
	}
}
