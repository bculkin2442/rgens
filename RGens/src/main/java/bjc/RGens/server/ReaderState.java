package bjc.RGens.server;

import java.util.function.Supplier;

import bjc.utils.funcdata.FunctionalList;
import bjc.utils.funcdata.IList;
import bjc.utils.gen.WeightedGrammar;

/**
 * Represents the internal state of reader
 * 
 * @author ben
 *
 */
public class ReaderState {
	private WeightedGrammar<String>			currentGrammar;
	private String							currentRule;
	private boolean							isUniform;

	private IList<String> exports;

	/**
	 * Create a new reader state
	 */
	public ReaderState() {
		currentGrammar = new WeightedGrammar<>();

		// Grammars start out uniform
		isUniform = true;

		exports = new FunctionalList<>();
	}

	public void addExport(String export) {
		exports.add(export);
	}

	public IList<String> getExports() {
		return exports;
	}

	/**
	 * Get the rule names for the current grammar
	 * 
	 * @return The rule names for the current grammar
	 */
	public IList<String> getRuleNames() {
		return currentGrammar.getRuleNames();
	}

	/**
	 * Check if this reader is currently in uniform mode
	 * 
	 * @return Whether this grammar is in uniform mode
	 */
	public boolean isUniform() {
		return isUniform;
	}

	/**
	 * Set the current grammar to be the specified one
	 * 
	 * @param newWorkingGrammar
	 *            The new grammar to use
	 */
	public void setCurrentGrammar(WeightedGrammar<String> newWorkingGrammar) {
		currentGrammar = newWorkingGrammar;
	}

	/**
	 * Set the rule currently being worked on
	 * 
	 * @param ruleName
	 *            The rule currently being worked on
	 */
	public void setCurrentRule(String ruleName) {
		currentRule = ruleName;
	}

	/**
	 * Set the initial rule of this grammar
	 * 
	 * @param ruleName
	 *            The initial rule of this grammar
	 */
	public void setInitialRule(String ruleName) {
		currentGrammar.setInitialRule(ruleName);
	}

	/**
	 * Toggle this uniformity setting for this grammar
	 */
	public void toggleUniformity() {
		isUniform = !isUniform;
	}

	/**
	 * Add a case to the current grammar
	 * 
	 * @param ruleProbability
	 *            The probability for this case to occur
	 * @param ruleParts
	 *            The parts that make up this case
	 */
	public void addCase(int ruleProbability, IList<String> ruleParts) {
		currentGrammar.addCase(currentRule, ruleProbability, ruleParts);
	}

	/**
	 * Add a special rule to the grammar
	 *
	 * @param ruleName The name of the special rule
	 * @param cse The special case for the rule
	 */
	public void addSpecialRule(String ruleName, Supplier<IList<String>> cse) {
		currentGrammar.addSpecialRule(ruleName, cse);
	}

	/**
	 * Start editing a new rule in the current grammar
	 * 
	 * @param ruleName
	 *            The name of the new rule to edit
	 */
	public void startNewRule(String ruleName) {
		currentGrammar.addRule(ruleName);

		currentRule = ruleName;
	}

	/**
	 * Convert this package of state into a weighted grammar
	 * 
	 * @return The grammar represented by this state
	 */
	public WeightedGrammar<String> getGrammar() {
		return currentGrammar;
	}

	/**
	 * Prefix a token onto all of the cases for the specified rule
	 * 
	 * @param ruleName
	 *            The rule to do prefixing on
	 * @param prefixToken
	 *            The token to prefix onto each case
	 * @param additionalProbability
	 *            The probability modification of the prefixed cases
	 */
	public void prefixRule(String ruleName, String prefixToken,
			int additionalProbability) {
		currentGrammar.prefixRule(ruleName, prefixToken,
				additionalProbability);
	}

	/**
	 * Delete a rule from the current grammar
	 * 
	 * @param ruleName
	 *            The name of the rule to delete
	 */
	public void deleteRule(String ruleName) {
		currentGrammar.deleteRule(ruleName);
	}

	/**
	 * Suffix a token onto all of the cases for the specified rule
	 * 
	 * @param ruleName
	 *            The rule to do suffixing on
	 * @param suffixToken
	 *            The token to suffix onto each case
	 * @param additionalProbability
	 *            The probability modification of the suffixed cases
	 */
	public void suffixRule(String ruleName, String suffixToken,
			int additionalProbability) {
		currentGrammar.suffixRule(ruleName, suffixToken,
				additionalProbability);
	}
}
