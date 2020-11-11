package bjc.rgens.parser.elements.vars;

import bjc.rgens.parser.GenerationState;
import bjc.rgens.parser.GrammarException;
import bjc.rgens.parser.Rule;

/**
 * Rule reference variable element.
 * @author Ben Culkin
 *
 */
public class RRefVariableElement extends VariableElement {
	/**
	 * The name of the rule to reference.
	 */
	public String value;

	/**
	 * Create a new rule-reference variable element.
	 * 
	 * @param forbidSpaces Whether to forbid spaces or not.
	 * @param val The rule to reference.
	 */
	public RRefVariableElement(boolean forbidSpaces, String val) {
		super(forbidSpaces);

		value = val;
	}
	
	@Override
	public void generate(GenerationState state) {
		Rule rl = state.findRule(value, true);

		if(rl == null)
			throw new GrammarException(String.format("Could not find rule '%s'", value));

		GenerationState newState = state.newBuf();

		rl.generate(newState);

		String res = newState.getContents();

		if(forbidSpaces && res.contains(" ")) {
			throw new GrammarException(String.format("Spaces not allowed in this context (rule-reference %s)", state));
		}

		state.appendContents(res);
	}
}
