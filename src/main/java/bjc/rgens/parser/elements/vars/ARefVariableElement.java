package bjc.rgens.parser.elements.vars;

import bjc.rgens.parser.GenerationState;
import bjc.rgens.parser.GrammarException;
import bjc.rgens.parser.Rule;

public class ARefVariableElement extends VariableElement {
	public String value;

	public ARefVariableElement(String val) {
		super(false);

		value = val;
	}
	
	public void generate(GenerationState state) {
		if(!state.rlVars.containsKey(value)) {
			throw new GrammarException("No rule variable named " + value);
		}

		Rule rl = state.rlVars.get(value);

		GenerationState newState = state.newBuf();

		rl.generate(newState);

		String res = newState.contents.toString();

		state.contents.append(res);
	}
}
