package bjc.rgens.newparser;

import bjc.utils.funcdata.FunctionalList;
import bjc.utils.funcdata.IList;

import java.util.Random;

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
	 * 
	 * @throws IllegalArgumentException
	 *                 If the rule name is invalid.
	 */
	public Rule(String ruleName) {
		if(ruleName == null) {
			throw new NullPointerException("Rule name must not be null");
		} else if(ruleName.equals("")) {
			throw new IllegalArgumentException("The empty string is not a valid rule name");
		}

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
		if(cse == null) {
			throw new NullPointerException("Case must not be null");
		}

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

	/**
	 * Get a random case from this rule.
	 * 
	 * @param rnd
	 *                The random number generator to use.
	 * 
	 * @return A random case from this rule.
	 */
	public RuleCase getCase(Random rnd) {
		return ruleCases.randItem(rnd::nextInt);
	}

	/**
	 * Get all the cases of this rule.
	 * 
	 * @return All the cases in this rule.
	 */
	public IList<RuleCase> getCases() {
		return ruleCases;
	}

	@Override
	public int hashCode() {
		final int prime = 31;

		int result = 1;
		result = prime * result + ((ruleCases == null) ? 0 : ruleCases.hashCode());
		result = prime * result + ((ruleName == null) ? 0 : ruleName.hashCode());

		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj == null) return false;
		if(!(obj instanceof Rule)) return false;

		Rule other = (Rule) obj;

		if(ruleCases == null) {
			if(other.ruleCases != null) return false;
		} else if(!ruleCases.equals(other.ruleCases)) return false;

		if(ruleName == null) {
			if(other.ruleName != null) return false;
		} else if(!ruleName.equals(other.ruleName)) return false;

		return true;
	}

	@Override
	public String toString() {
		return String.format("Rule [ruleName='%s', ruleCases=%s]", ruleName, ruleCases);
	}
}