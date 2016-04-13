package bjc.RGens.parser;

import java.util.Stack;

import bjc.utils.gen.WeightedGrammar;

/**
 * Represents the internal state of reader
 * 
 * @author ben
 *
 */
public class ReaderState {
	private String							currRule	= "";

	// Pragma settings
	private boolean							isUniform	= false;

	private Stack<WeightedGrammar<String>>	wg;

	/**
	 * Create a new reader state
	 */
	public ReaderState() {
		wg = new Stack<>();
		wg.push(new WeightedGrammar<>());
	}

	/**
	 * Get the current grammar
	 * 
	 * @return The current grammar
	 */
	public WeightedGrammar<String> currGrammar() {
		return wg.peek();
	}

	/**
	 * Get the current rule
	 * 
	 * @return The current rule
	 */
	public String getRule() {
		return currRule;
	}

	/**
	 * Use {@link #currGrammar()} instead
	 * 
	 * @return The current grammar
	 */
	@Deprecated
	public WeightedGrammar<String> getRules() {
		return wg.peek();
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
	 * Pop the grammar currently being worked on
	 * 
	 * @return The grammar currently being worked on
	 */
	public WeightedGrammar<String> popGrammar() {
		return wg.pop();
	}

	/**
	 * Push a new grammar to work on
	 * 
	 * @param nwg
	 *            The new grammar to work on
	 */
	public void pushGrammar(WeightedGrammar<String> nwg) {
		wg.push(nwg);
	}

	/**
	 * Set the initial rule of this grammar
	 * 
	 * @param rName
	 *            The initial rule of this grammar
	 */
	public void setInitialRule(String rName) {
		wg.peek().setInitialRule(rName);
	}

	/**
	 * Set the rule currently being worked on
	 * 
	 * @param r
	 *            The rule currently being worked on
	 */
	public void setRule(String r) {
		currRule = r;
	}

	/**
	 * Set the current grammar to be the specified one
	 * 
	 * @param nwg
	 *            The new grammar to use
	 */
	public void setRules(WeightedGrammar<String> nwg) {
		wg.pop();
		wg.push(nwg);
	}

	/**
	 * Toggle this grammars uniformity setting
	 */
	public void toggleUniformity() {
		isUniform = !isUniform;
	}
}
