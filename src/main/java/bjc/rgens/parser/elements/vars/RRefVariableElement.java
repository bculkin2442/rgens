package bjc.rgens.parser.elements.vars;

import bjc.rgens.parser.GenerationState;
import bjc.rgens.parser.GrammarException;
import bjc.rgens.parser.Rule;

public class RRefVariableElement extends VariableElement {
	public String value;

	private boolean forbidSpaces;

	public RRefVariableElement(boolean forbidSpaces, String val) {
		value = val;
	}
	
	public void generate(GenerationState state) {
		if(!state.rlVars.containsKey(value)) {
			throw new GrammarException("No rule variable named " + value);
		}

		Rule rl = state.findRule(value, true);

		GenerationState newState = state.newBuf();

		rl.generate(newState);

		String res = newState.contents.toString();

		if(forbidSpaces && res.contains(" ")) {
			throw new GrammarException("Spaces not allowed in this context (rule-reference %s)");
		}

		state.contents.append(res);
	}
}
