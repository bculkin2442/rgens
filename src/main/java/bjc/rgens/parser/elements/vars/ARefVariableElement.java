package bjc.rgens.parser.elements.vars;

import bjc.rgens.parser.GenerationState;
import bjc.rgens.parser.Rule;
/**
 * Reference to an array variable.
 * @author bjculkin
 *
 */
public class ARefVariableElement extends VariableElement {
	/**
	 * The name of the value we are referring to.
	 */
	public String value;

	/**
	 * Create a new array reference.
	 * 
	 * @param val The value of the array reference.
	 */
	public ARefVariableElement(String val) {
		super(false);

		value = val;
	}
	
	@Override
	public void generate(GenerationState state) {
		Rule rl = state.findRuleVar(value);

		GenerationState newState = state.newBuf();

		rl.generate(newState);

		String res = newState.getContents();

		state.appendContents(res);
	}
}
