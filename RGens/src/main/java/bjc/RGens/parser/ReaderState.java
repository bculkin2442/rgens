package bjc.RGens.parser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Stack;

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

	private Stack<WeightedGrammar<String>>	grammarStack;

	private String							currentRule;

	private boolean							isUniform;

	private Path							currentDirectory;

	/**
	 * Create a new reader state
	 * 
	 * @param inputPath
	 *            The path to this grammar
	 */
	public ReaderState(Path inputPath) {
		grammarStack = new Stack<>();

		currentGrammar = new WeightedGrammar<>();

		// Grammars start out uniform
		isUniform = true;

		currentDirectory = inputPath.getParent();
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
	public void setCurrentGrammar(
			WeightedGrammar<String> newWorkingGrammar) {
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
	 * Start work on a new sub-grammar of the previous grammar
	 */
	public void startNewSubgrammar() {
		grammarStack.push(currentGrammar);

		currentGrammar = new WeightedGrammar<>();
	}

	/**
	 * Move to editing the grammar that is the parent of this current one
	 */
	public void editParent() {
		currentGrammar = grammarStack.pop();
	}

	/**
	 * Add a case to the current grammar
	 * 
	 * @param ruleProbability
	 *            The probability for this case to occur
	 * @param ruleParts
	 *            The parts that make up this case
	 */
	public void addCase(int ruleProbability,
			IList<String> ruleParts) {
		currentGrammar.addCase(currentRule, ruleProbability, ruleParts);
	}

	/**
	 * Edit a subgrammar of the current grammar
	 * 
	 * @param subgrammarName
	 *            The name of the subgrammar to edit
	 */
	public void editSubgrammar(String subgrammarName) {
		WeightedGrammar<String> subgrammar = currentGrammar
				.getSubgrammar(subgrammarName);

		grammarStack.push(currentGrammar);

		currentGrammar = subgrammar;
	}

	/**
	 * Start editing a new rule in the current grammar
	 * 
	 * @param ruleName
	 *            The name of the new rule to edit
	 */
	public void startNewRule(String ruleName) {
		currentGrammar.addRule(ruleName);
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
	 * Alias a current subgrammar to a new name
	 * 
	 * @param subgrammarName
	 *            The name of the subgrammar to alias
	 * @param subgrammarAlias
	 *            The name of the alias for the subgrammar
	 */
	public void addGrammarAlias(String subgrammarName,
			String subgrammarAlias) {
		currentGrammar.addGrammarAlias(subgrammarName, subgrammarAlias);
	}

	/**
	 * Load a subgrammar into this one.
	 * 
	 * @param subgrammarName
	 *            The name to assign to the subgrammar
	 * @param subgrammarPath
	 *            The path to load the subgrammar from
	 */
	public void loadSubgrammar(String subgrammarName,
			String subgrammarPath) {
		Path loadPath = currentDirectory.resolve(subgrammarPath);

		try {
			WeightedGrammar<String> subgrammar = RBGrammarReader
					.fromPath(loadPath);

			currentGrammar.addSubgrammar(subgrammarName, subgrammar);
		} catch (IOException ioex) {
			PragmaErrorException peex = new PragmaErrorException(
					"Couldn't load subgrammar " + subgrammarName + " from "
							+ subgrammarPath);

			peex.initCause(ioex);

			throw peex;
		}
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
	 * Promote the specified subgrammar above the current grammar
	 * 
	 * @param subgrammarName
	 *            The name of the subgrammar to promote
	 * @param subordinateName
	 *            The name to bind this grammar to the subgrammar under
	 */
	public void promoteGrammar(String subgrammarName,
			String subordinateName) {
		WeightedGrammar<String> subgrammar = currentGrammar
				.getSubgrammar(subgrammarName);

		currentGrammar.deleteSubgrammar(subgrammarName);

		subgrammar.addSubgrammar(subordinateName, currentGrammar);

		currentGrammar = subgrammar;
	}

	/**
	 * Delete a rule from the current subgrammar
	 * 
	 * @param ruleName
	 *            The name of the rule to delete
	 */
	public void deleteRule(String ruleName) {
		currentGrammar.deleteRule(ruleName);
	}

	/**
	 * Delete a subgrammar from the current grammar
	 * 
	 * @param subgrammarName
	 *            The name of the subgrammar to delete
	 */
	public void deleteSubgrammar(String subgrammarName) {
		currentGrammar.deleteSubgrammar(subgrammarName);
	}

	/**
	 * Save the current grammar as a subgrammar of the previous one
	 * 
	 * @param subgrammarName
	 *            The name of the subgrammar to save this under
	 */
	public void saveSubgrammar(String subgrammarName) {
		WeightedGrammar<String> newWorkingGrammar = grammarStack.pop();

		newWorkingGrammar.addSubgrammar(subgrammarName, currentGrammar);

		currentGrammar = newWorkingGrammar;
	}

	/**
	 * Subordinate this grammar as a subgrammar to a new grammar
	 * 
	 * @param grammarName
	 *            The name for the subgrammar to bind the current grammar
	 *            to
	 */
	public void subordinateGrammar(String grammarName) {
		WeightedGrammar<String> newWorkingGrammar = new WeightedGrammar<>();

		newWorkingGrammar.addSubgrammar(grammarName, currentGrammar);

		currentGrammar = newWorkingGrammar;
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
