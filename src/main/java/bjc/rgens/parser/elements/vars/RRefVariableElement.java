package bjc.rgens.parser.elements.vars;

import bjc.rgens.parser.GenerationState;
import bjc.rgens.parser.GrammarException;
import bjc.rgens.parser.Rule;

public class RRefVariableElement extends VariableElement {
	public String value;

	public RRefVariableElement(boolean forbidSpaces, String val) {
		super(forbidSpaces);

		value = val;
	}
	
	public void generate(GenerationState state) {
		Rule rl = state.findRule(value, true);

		if(rl == null)
			throw new GrammarException(String.format("Could not find rule '%s'", value));

		GenerationState newState = state.newBuf();

		rl.generate(newState);

		String res = newState.contents.toString();

		if(forbidSpaces && res.contains(" ")) {
			throw new GrammarException(String.format("Spaces not allowed in this context (rule-reference %s)", state));
		}

		state.contents.append(res);
	}
}