package bjc.rgens.newparser;

import bjc.utils.funcdata.FunctionalList;
import bjc.utils.funcdata.IList;

import java.util.HashMap;
import java.util.Map;

import static bjc.rgens.newparser.CaseElement.ElementType.*;
import static bjc.rgens.newparser.RuleCase.CaseType.*;

/**
 * Construct randomized grammars piece by piece.
 * 
 * @author EVE
 *
 */
public class RGrammarBuilder {
	private Map<String, Rule> rules;

	private Rule currRule;

	private IList<CaseElement> currentCase;

	private String initialRule;

	/**
	 * Create a new randomized grammar builder.
	 */
	public RGrammarBuilder() {
		rules = new HashMap<>();

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
		if(csepart.matches("\\[[^\\]]+\\]")) {
			currentCase.add(new CaseElement(RULEREF, csepart));
		} else {
			currentCase.add(new CaseElement(LITERAL, csepart));
		}
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
	 */
	public void setInitialRule(String init) {
		if(init == null) {
			throw new NullPointerException("init must not be null");
		} else if(init.equals("")) {
			throw new IllegalArgumentException("The empty string is not a valid rule name");
		} else if(!rules.containsKey(init)) {
			throw new IllegalArgumentException(String.format("No rule named '%s' found", init));
		}

		initialRule = init;
	}
}
