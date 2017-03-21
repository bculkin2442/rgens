package bjc.rgens.newparser;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents a randomized grammar.
 * 
 * @author EVE
 *
 */
public class RGrammar {
	private static class GenerationState {
		public StringBuilder contents;

		public GenerationState(StringBuilder contents) {
			this.contents = contents;
		}
	}

	private Map<String, Rule> rules;

	private Map<String, RGrammar> importRules;

	private Set<String> exportRules;

	private String initialRule;

	/**
	 * Create a new randomized grammar using the specified set of rules.
	 * 
	 * @param rules
	 *                The rules to use.
	 */
	public RGrammar(Map<String, Rule> rules) {
		this.rules = rules;
	}

	/**
	 * Sets the imported rules to use.
	 * 
	 * Imported rules are checked for rule definitions after local
	 * definitions are checked.
	 * 
	 * @param exportedRules
	 *                The set of imported rules to use.
	 */
	public void setImportedRules(Map<String, RGrammar> exportedRules) {
		importRules = exportedRules;
	}

	/**
	 * Generate a string from this grammar, starting from the specified
	 * rule.
	 * 
	 * @param startRule
	 *                The rule to start generating at, or null to use the
	 *                initial rule for this grammar.
	 * 
	 * @return A possible string from the grammar.
	 */
	public String generate(String startRule) {
		String fromRule = startRule;

		if(startRule == null) {
			if(initialRule == null) {
				throw new GrammarException(
						"Must specify a start rule for grammars with no initial rule");
			} else {
				fromRule = initialRule;
			}
		} else {
			if(startRule.equals("")) {
				throw new GrammarException("The empty string is not a valid rule name");
			}
		}

		RuleCase start = rules.get(fromRule).getCase();

		StringBuilder contents = new StringBuilder();

		generateCase(start, new GenerationState(contents));

		return contents.toString();
	}

	/*
	 * Generate a rule case.
	 */
	private void generateCase(RuleCase start, GenerationState state) {
		try {
			switch(start.type) {
			case NORMAL:
				for(CaseElement elm : start.getElements()) {
					generateElement(elm, state);
				}
				break;
			default:
				throw new GrammarException(String.format("Unknown case type '%s'", start.type));
			}
		} catch(GrammarException gex) {
			throw new GrammarException(String.format("Error in generating case (%s)", start), gex);
		}
	}

	/*
	 * Generate a case element.
	 */
	private void generateElement(CaseElement elm, GenerationState state) {
		try {
			switch(elm.type) {
			case LITERAL:
				state.contents.append(elm.getLiteral() + " ");
				break;
			case RULEREF:
				generateRuleReference(elm, state);
				break;
			default:
				throw new GrammarException(String.format("Unknown element type '%s'", elm.type));
			}
		} catch(GrammarException gex) {
			throw new GrammarException(String.format("Error in generating case element (%s)", elm), gex);
		}
	}

	/*
	 * Generate a rule reference.
	 */
	private void generateRuleReference(CaseElement elm, GenerationState state) {
		String refersTo = elm.getLiteral();

		GenerationState newState = new GenerationState(new StringBuilder());

		if(rules.containsKey(refersTo)) {
			RuleCase cse = rules.get(refersTo).getCase();

			generateCase(cse, newState);
		} else if(importRules.containsKey(refersTo)) {
			RGrammar dst = importRules.get(refersTo);

			newState.contents.append(dst.generate(refersTo));
		} else {
			throw new GrammarException(String.format("No rule by name '%s' found", refersTo));
		}

			state.contents.append(newState.contents.toString());
	}

	/**
	 * Get the initial rule of this grammar.
	 * 
	 * @return The initial rule of this grammar.
	 */
	public String getInitialRule() {
		return initialRule;
	}

	/**
	 * Set the initial rule of this grammar.
	 * 
	 * @param initialRule
	 *                The initial rule of this grammar, or null to say there
	 *                is no initial rule.
	 */
	public void setInitialRule(String initialRule) {
		/*
		 * Passing null nulls our initial rule.
		 */
		if(initialRule == null) {
			this.initialRule = null;
			return;
		}

		if(initialRule.equals("")) {
			throw new GrammarException("The empty string is not a valid rule name");
		} else if(!rules.containsKey(initialRule)) {
			throw new GrammarException(
					String.format("No rule named '%s' local to this grammar found.", initialRule));
		}

		this.initialRule = initialRule;
	}

	/**
	 * Gets the rules exported by this grammar.
	 * 
	 * The initial rule is exported by default if specified.
	 * 
	 * @return The rules exported by this grammar.
	 */
	public Set<Rule> getExportedRules() {
		Set<Rule> res = new HashSet<>();

		for(String rname : exportRules) {
			if(!rules.containsKey(rname)) {
				throw new GrammarException(String
						.format("No rule named '%s' local to this grammar found", initialRule));
			}

			res.add(rules.get(rname));
		}

		if(initialRule != null) {
			res.add(rules.get(initialRule));
		}

		return res;
	}

	/**
	 * Set the rules exported by this grammar.
	 * 
	 * @param exportRules
	 *                The rules exported by this grammar.
	 */
	public void setExportedRules(Set<String> exportRules) {
		this.exportRules = exportRules;
	}

	/**
	 * Get all the rules in this grammar.
	 * 
	 * @return All the rules in this grammar.
	 */
	public Map<String, Rule> getRules() {
		return rules;
	}
}
