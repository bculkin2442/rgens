package bjc.rgens.parser;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import bjc.rgens.parser.elements.CaseElement;
import bjc.data.Pair;
import bjc.funcdata.ListEx;

/**
 * @TODO Ben Culkin 9/8/18 :GrammarFormatter
 *
 * Update this to suppor the new features when possible.
 */
/**
 * Format randomized grammars to strings properly.
 *
 * @author EVE
 */
public class RGrammarFormatter {
	/**
	 * Format a grammar into a file that represents that grammar.
	 *
	 * @param gram
	 * 	The grammar to format.
	 *
	 * @return
	 * 	The formatted grammar.
	 */
	public static String formatGrammar(RGrammar gram) {
		StringBuilder sb = new StringBuilder();

		Map<String, Rule> rules = gram.getRules();

		String initRuleName = gram.getInitialRule();

		Set<String> processedRules = new HashSet<>();

		if (initRuleName != null) {
			processRule(rules.get(initRuleName), sb);

			processedRules.add(initRuleName);
		}

		for (Rule rule : rules.values()) {
			if (!processedRules.contains(rule.name)) {
				sb.append("\n\n");

				processRule(rule, sb);
			}

			processedRules.add(rule.name);
		}

		return sb.toString().trim();
	}

	/* Format a rule. */
	private static void processRule(Rule rule, StringBuilder sb) {
		ListEx<Pair<Integer, RuleCase>> cases = rule.getCases();

		StringBuilder ruleBuilder = new StringBuilder();

		ruleBuilder.append(rule.name);
		ruleBuilder.append(" \u2192 ");

		int markerPos = ruleBuilder.length();

		processCase(cases.first().getRight(), ruleBuilder);

		sb.append(ruleBuilder.toString().trim());

		ruleBuilder = new StringBuilder();

		for (Pair<Integer, RuleCase> cse : cases.tail()) {
			sb.append("\n\t");

			for (int i = 8; i < markerPos; i++) {
				ruleBuilder.append(" ");
			}

			/* @TODO do this right, once we pick the syntax */
			processCase(cse.getRight(), ruleBuilder);

			sb.append(ruleBuilder.toString());

			ruleBuilder = new StringBuilder();
		}

	}

	/* Format a case. */
	private static void processCase(RuleCase cse, StringBuilder sb) {
		/* Process each element, adding a space. */
		for (CaseElement element : cse.elementList) {
			sb.append(element.toString());
			sb.append(" ");
		}

		/* Remove the trailing space. */
		sb.deleteCharAt(sb.length() - 1);
	}
}
