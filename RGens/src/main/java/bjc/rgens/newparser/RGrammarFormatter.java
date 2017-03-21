package bjc.rgens.newparser;

import bjc.utils.funcdata.IList;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Format randomized grammars to strings properly.
 * 
 * @author EVE
 *
 */
public class RGrammarFormatter {
	/**
	 * Format a grammar into a file that represents that grammar.
	 * 
	 * @param gram
	 *                The grammar to format.
	 * 
	 * @return The formatted grammar.
	 */
	public static String formatGrammar(RGrammar gram) {
		StringBuilder sb = new StringBuilder();

		Map<String, Rule> rules = gram.getRules();

		String initRuleName = gram.getInitialRule();

		Set<String> processedRules = new HashSet<>();

		if(initRuleName != null) {
			processRule(rules.get(initRuleName), sb);

			processedRules.add(initRuleName);
		}

		for(Rule rule : rules.values()) {
			if(!processedRules.contains(rule.ruleName)) {
				sb.append("\n\n");
				
				processRule(rule, sb);
			}

			processedRules.add(rule.ruleName);
		}

		return sb.toString().trim();
	}

	private static void processRule(Rule rule, StringBuilder sb) {
		IList<RuleCase> cases = rule.getCases();

		StringBuilder ruleBuilder = new StringBuilder();

		ruleBuilder.append(rule.ruleName);
		ruleBuilder.append(" \u2192 ");

		int markerPos = ruleBuilder.length();

		processCase(cases.first(), ruleBuilder);

		sb.append(ruleBuilder.toString().trim());

		ruleBuilder = new StringBuilder();

		for(RuleCase cse : cases.tail()) {
			sb.append("\n\t");

			for(int i = 8; i < markerPos; i++) {
				ruleBuilder.append(" ");
			}

			processCase(cse, ruleBuilder);

			sb.append(ruleBuilder.toString());

			ruleBuilder = new StringBuilder();
		}

	}

	private static void processCase(RuleCase cse, StringBuilder sb) {
		/*
		 * Process each element, adding a space.
		 */
		for(CaseElement element : cse.getElements()) {
			sb.append(element.toString());
			sb.append(" ");
		}

		/*
		 * Remove the trailing space.
		 */
		sb.deleteCharAt(sb.length() - 1);
	}
}
