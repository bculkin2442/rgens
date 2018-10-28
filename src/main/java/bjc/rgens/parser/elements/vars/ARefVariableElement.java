package bjc.rgens.parser.elements.vars;

import bjc.rgens.parser.GenerationState;
import bjc.rgens.parser.Rule;
/**
 * Reference to an array variable.
 * @author bjculkin
 *
 */
public class ARefVariableElement extends VariableElement {
	public String value;

	public ARefVariableElement(String val) {
		super(false);

		value = val;
	}
	
	public void generate(GenerationState state) {
		Rule rl = state.findRuleVar(value);

		GenerationState newState = state.newBuf();

		rl.generate(newState);

		String res = newState.getContents();

		state.appendContents(res);
	}
}
