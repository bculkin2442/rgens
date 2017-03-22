package bjc.rgens.newparser;

import bjc.utils.funcdata.FunctionalList;
import bjc.utils.funcdata.IList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static bjc.rgens.newparser.RuleCase.CaseType.*;

/**
 * Construct randomized grammars piece by piece.
 * 
 * @author EVE
 *
 */
public class RGrammarBuilder {
	private IList<CaseElement> currentCase;

	private Rule currRule;

	private Map<String, Rule> rules;

	private Set<String> exportedRules;

	private String initialRule;

	/**
	 * Create a new randomized grammar builder.
	 */
	public RGrammarBuilder() {
		rules = new HashMap<>();

		exportedRules = new HashSet<>();

		currentCase = new FunctionalList<>();
	}

	/**
	 * Starts a rule with the provided name.
	 * 
	 * If the rule already exists, it will be opened for adding additional
	 * cases instead.
	 * 
	 * @param rName
	 *                The name of the rule currently being built.
	 * 
	 * @throws IllegalArgumentException
	 *                 If the rule name is the empty string.
	 */
	public void startRule(String rName) {
		if(rName == null) {
			throw new NullPointerException("Rule name must not be null");
		} else if(rName.equals("")) {
			throw new IllegalArgumentException("The empty string is not a valid rule name");
		}

		currRule = new Rule(rName);
	}

	/**
	 * Convert this builder into a grammar.
	 * 
	 * @return The grammar built by this builder
	 */
	public RGrammar toRGrammar() {
		RGrammar grammar = new RGrammar(rules);

		grammar.setInitialRule(initialRule);

		grammar.setExportedRules(exportedRules);

		return grammar;
	}

	/**
	 * Adds a case part to this rule.
	 * 
	 * <h2>Case part formatting</h2>
	 * <dl>
	 * <dt>Rule Reference</dt>
	 * <dd>Rule references are marked by being surrounded with square
	 * brackets (the square brackets are part of the rule's name)</dd>
	 * <dt>Literal Strings</dt>
	 * <dd>Literal strings are the default case part type.</dd>
	 * </dl>
	 * 
	 * @param csepart
	 */
	public void addCasePart(String csepart) {
		CaseElement element = CaseElement.createElement(csepart);

		currentCase.add(element);
	}

	/**
	 * Finalizes editing a rule.
	 * 
	 * Saves the rule to the rule map.
	 * 
	 * @throws IllegalStateException
	 *                 Must be invoked while a rule is being edited.
	 */
	public void finishRule() {
		if(currRule == null) {
			throw new IllegalStateException("Must start a rule before finishing one");
		}

		rules.put(currRule.ruleName, currRule);
	}

	/**
	 * Finishes the current case being edited.
	 * 
	 * @throws IllegalStateException
	 *                 Must be invoked while a rule is being edited.
	 */
	public void finishCase() {
		if(currRule == null) {
			throw new IllegalStateException("Must start a rule before finishing a case");
		}

		currRule.addCase(new RuleCase(NORMAL, currentCase));

		currentCase = new FunctionalList<>();
	}

	/**
	 * Begins a case for the current rule.
	 * 
	 * @throws IllegalStateException
	 *                 Must be invoked while a rule is being edited.
	 */
	public void beginCase() {
		if(currRule == null) {
			throw new IllegalStateException("Must start a rule before adding cases");
		}
	}

	/**
	 * Set the initial rule of the grammar.
	 * 
	 * @param init
	 *                The initial rule of the grammar.
	 * 
	 * @throws IllegalArgumentException
	 *                 If the rule is either not valid or not defined in the
	 *                 grammar.
	 */
	public void setInitialRule(String init) {
		if(init == null) {
			throw new NullPointerException("init must not be null");
		} else if(init.equals("")) {
			throw new IllegalArgumentException("The empty string is not a valid rule name");
		} else if(!rules.containsKey(init)) {
			throw new IllegalArgumentException(String.format("No local rule named '%s' found", init));
		}

		initialRule = init;
	}

	/**
	 * Add an exported rule to this grammar.
	 * 
	 * @param export
	 *                The name of the rule to export.
	 * 
	 * @throws IllegalArgumentException
	 *                 If the rule is either not valid or not defined in the
	 *                 grammar.
	 */
	public void addExport(String export) {
		if(export == null) {
			throw new NullPointerException("Export name must not be null");
		} else if(export.equals("")) {
			throw new NullPointerException("The empty string is not a valid rule name");
		} else if(!rules.containsKey(export)) {
			throw new IllegalArgumentException(String.format("No local rule named '%s' found", export));
		}

		exportedRules.add(export);
	}

	/**
	 * Suffix a given case element to every case of a specific rule.
	 * 
	 * @param ruleName
	 *                The rule to suffix.
	 * 
	 * @param suffix
	 *                The suffix to add.
	 * 
	 * @throws IllegalArgumentException
	 *                 If the rule name is either invalid or not defined by
	 *                 this grammar, or if the suffix is invalid.
	 */
	public void suffixWith(String ruleName, String suffix) {
		if(ruleName == null) {
			throw new NullPointerException("Rule name must not be null");
		} else if(ruleName.equals("")) {
			throw new IllegalArgumentException("The empty string is not a valid rule name");
		} else if(!rules.containsKey(ruleName)) {
			throw new IllegalArgumentException(String.format("No local rule named '%s' found", ruleName));
		}

		CaseElement element = CaseElement.createElement(suffix);

		for(RuleCase ruleCase : rules.get(ruleName).getCases()) {
			ruleCase.getElements().add(element);
		}
	}
}
