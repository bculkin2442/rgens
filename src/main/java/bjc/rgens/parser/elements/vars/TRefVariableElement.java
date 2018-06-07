package bjc.rgens.parser.elements.vars;

import bjc.rgens.parser.GenerationState;
import bjc.rgens.parser.GrammarException;
import bjc.rgens.parser.templates.GrammarTemplate;

/*
 * @TODO
 *
 * finish when template vars are implemented.
 */
public class TRefVariableElement extends VariableElement {
	/*
	public String value;

	private boolean forbidSpaces;

	public TRefVariableElement(boolean forbidSpaces, String val) {
		value = val;
	}*/
	
	public void generate(GenerationState state) {
		/*
		if(!state.rlVars.containsKey(val)) {
			throw new GrammarException("No rule variable named " + val);
		}

		Rule rl = state.rlVars.get(val);

		GenerationState newState = state.newBuf();

		rl.generate(newState);

		String res = newState.contents.toString();

		if(forbidSpaces && res.contains(" ")) {
			throw new GrammarException("Spaces not allowed in this context (rule-var %s)");
		}

		return res;
	*/
	}
}
