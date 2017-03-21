package bjc.rgens.newparser;

import java.util.List;
import java.util.Map;

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

	private Map<String, Rule> importRules;

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
	 * @param importedRules
	 *                The set of imported rules to use.
	 */
	public void setImportedRules(Map<String, Rule> importedRules) {
		importRules = importedRules;
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
				if(rules.containsKey(elm.getLiteral())) {
					RuleCase cse = rules.get(elm.getLiteral()).getCase();

					generateCase(cse, state);
				} else if(importRules.containsKey(elm.getLiteral())) {
					RuleCase cse = importRules.get(elm.getLiteral()).getCase();

					generateCase(cse, state);
				} else {
					throw new GrammarException(
							String.format("No rule by name '%s' found", elm.getLiteral()));
				}
				break;
			default:
				throw new GrammarException(String.format("Unknown element type '%s'", elm.type));
			}
		} catch(GrammarException gex) {
			throw new GrammarException(String.format("Error in generating case element (%s)", elm), gex);
		}
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
		if(initialRule.equals("")) {
			throw new GrammarException("The empty string is not a valid rule name");
		} else if(!rules.containsKey(initialRule)) {
			throw new GrammarException(
					String.format("No rule named '%s' local to this grammar found.", initialRule));
		}

		this.initialRule = initialRule;
	}
}
