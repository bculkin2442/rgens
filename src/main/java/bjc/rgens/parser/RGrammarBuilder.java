package bjc.rgens.parser;

import bjc.rgens.parser.elements.CaseElement;
import bjc.utils.funcdata.FunctionalList;
import bjc.utils.funcdata.IList;

import static bjc.rgens.parser.RuleCase.CaseType.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Construct randomized grammars piece by piece.
 *
 * @author EVE
 */
public class RGrammarBuilder {
	/* The rules being built. */
	private Map<String, Rule> rules;
	/* The current set of exported rules. */
	private Set<String> exportedRules;
	/* The current initial rule. */
	private String initialRule;

	/** Create a new randomized grammar builder. */
	public RGrammarBuilder() {
		rules = new HashMap<>();

		exportedRules = new HashSet<>();
	}

	/**
	 * Get or create a rule by the given name.
	 *
	 * @param rName
	 * 	The name of the rule.
	 *
	 * @return
	 * 	The rule by that name, or a new one if none existed.
	 */
	public Rule getOrCreateRule(String rName) {
		if(rName == null)
			throw new NullPointerException("Rule name must not be null");
		else if(rName.equals(""))
			throw new IllegalArgumentException("The empty string is not a valid rule name");

		if(rules.containsKey(rName))
			return rules.get(rName);
		else {
			Rule ret = new Rule(rName);

			rules.put(rName, ret);

			return ret;
		}
	}

	/**
	 * Convert this builder into a grammar.
	 *
	 * @return 
	 * 	The grammar built by this builder
	 */
	public RGrammar toRGrammar() {
		RGrammar grammar = new RGrammar(rules);

		grammar.setInitialRule(initialRule);

		grammar.setExportedRules(exportedRules);

		return grammar;
	}

	/**
	 * Set the initial rule of the grammar.
	 *
	 * @param init
	 * 	The initial rule of the grammar.
	 *
	 * @throws IllegalArgumentException
	 * 	If the rule is either not valid or not defined in the grammar.
	 */
	public void setInitialRule(String init) {
		if (init == null) {
			throw new NullPointerException("init must not be null");
		} else if (init.equals("")) {
			throw new IllegalArgumentException("The empty string is not a valid rule name");
		}

		initialRule = init;
	}

	/**
	 * Add an exported rule to this grammar.
	 *
	 * @param export
	 * 	The name of the rule to export.
	 *
	 * @throws IllegalArgumentException
	 * 	If the rule is either not valid or not defined in the grammar.
	 */
	public void addExport(String export) {
		if (export == null) {
			throw new NullPointerException("Export name must not be null");
		} else if (export.equals("")) {
			throw new NullPointerException("The empty string is not a valid rule name");
		}

		exportedRules.add(export);
	}

	/**
	 * Suffix a given case element to every case of a specific rule.
	 *
	 * @param ruleName
	 * 	The rule to suffix.
	 *
	 * @param suffix
	 * 	The suffix to add.
	 *
	 * @throws IllegalArgumentException
	 * 	If the rule name is either invalid or not defined by this
	 * 	grammar, or if the suffix is invalid.
	 */
	public void suffixWith(String ruleName, String suffix) {
		if (ruleName == null) {
			throw new NullPointerException("Rule name must not be null");
		} else if (ruleName.equals("")) {
			throw new IllegalArgumentException("The empty string is not a valid rule name");
		} else if(!rules.containsKey(ruleName)) {
			String msg = String.format("Rule '%s' is not a valid rule name.");

			throw new IllegalArgumentException(msg);
		}

		CaseElement element = CaseElement.createElement(suffix);

		FunctionalList<RuleCase> newCases = new FunctionalList<>();

		IList<RuleCase> caseList = rules.get(ruleName).getCases();
		for (RuleCase ruleCase : caseList) {
			FunctionalList<CaseElement> newCase = new FunctionalList<>();

			for(CaseElement elm : ruleCase.getElements()) {
				newCase.add(elm);
			}

			newCase.add(element);

			newCases.add(new RuleCase(NORMAL, newCase));
		}


		for (RuleCase newCase : newCases) {
			caseList.add(newCase);
		}
	}

	/**
	 * Prefix a given case element to every case of a specific rule.
	 *
	 * @param ruleName
	 * 	The rule to prefix.
	 *
	 * @param prefix
	 * 	The prefix to add.
	 *
	 * @throws IllegalArgumentException
	 * 	If the rule name is either invalid or not defined by this
	 * 	grammar, or if the prefix is invalid.
	 */
	public void prefixWith(String ruleName, String prefix) {
		if (ruleName == null) {
			throw new NullPointerException("Rule name must not be null");
		} else if (ruleName.equals("")) {
			throw new IllegalArgumentException("The empty string is not a valid rule name");
		} else if(!rules.containsKey(ruleName)) {
			String msg = String.format("Rule '%s' is not a valid rule name.");

			throw new IllegalArgumentException(msg);
		}

		CaseElement element = CaseElement.createElement(prefix);

		FunctionalList<RuleCase> newCases = new FunctionalList<>();

		IList<RuleCase> caseList = rules.get(ruleName).getCases();
		for (RuleCase ruleCase : caseList) {
			FunctionalList<CaseElement> newCase = new FunctionalList<>();

			newCase.add(element);

			for(CaseElement elm : ruleCase.getElements()) {
				newCase.add(elm);
			}

			newCases.add(new RuleCase(NORMAL, newCase));
		}


		for (RuleCase newCase : newCases) {
			caseList.add(newCase);
		}
	}

	public void despaceRule(String ruleName) {
		if (ruleName == null) {
			throw new NullPointerException("ruleName must not be null");
		} else if (ruleName.equals("")) {
			throw new IllegalArgumentException("The empty string is not a valid rule name");
		}

		IList<RuleCase> caseList = rules.get(ruleName).getCases();

		IList<RuleCase> newCaseList = new FunctionalList<>();

		for(RuleCase cse : caseList) {
			newCaseList.add(new RuleCase(SPACEFLATTEN, cse.getElements()));
		}

		rules.get(ruleName).replaceCases(newCaseList);
	}

	public void regexizeRule(String rule, String pattern) {
		if (rule == null) {
			throw new NullPointerException("rule must not be null");
		} else if(pattern == null) {
			throw new NullPointerException("pattern must not be null");	
		} else if (rule.equals("")) {
			throw new IllegalArgumentException("The empty string is not a valid rule name");
		}

		IList<RuleCase> caseList = rules.get(rule).getCases();

		IList<RuleCase> newCaseList = new FunctionalList<>();

		for(RuleCase cse : caseList) {
			newCaseList.add(new RegexRuleCase(cse.getElements(), pattern));
		}

		rules.get(rule).replaceCases(newCaseList);

	}
}