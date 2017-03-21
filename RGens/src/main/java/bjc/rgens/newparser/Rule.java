package bjc.rgens.newparser;

import bjc.utils.funcdata.FunctionalList;
import bjc.utils.funcdata.IList;

/**
 * A rule in a randomized grammar.
 * 
 * @author EVE
 *
 */
public class Rule {
	/**
	 * The name of this grammar rule.
	 */
	public final String ruleName;

	private IList<RuleCase> ruleCases;

	/**
	 * Create a new grammar rule.
	 * 
	 * @param ruleName
	 *                The name of the grammar rule.
	 */
	public Rule(String ruleName) {
		this.ruleName = ruleName;

		ruleCases = new FunctionalList<>();
	}

	/**
	 * Adds a case to the rule.
	 * 
	 * @param cse
	 *                The case to add.
	 */
	public void addCase(RuleCase cse) {
		ruleCases.add(cse);
	}

	/**
	 * Get a random case from this rule.
	 * 
	 * @return A random case from this rule.
	 */
	public RuleCase getCase() {
		return ruleCases.randItem();
	}
}