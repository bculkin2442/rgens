package bjc.rgens.parser.elements.vars;

import bjc.rgens.parser.GenerationState;
import bjc.rgens.parser.GrammarException;

public class VRefVariableElement extends VariableElement {
	public final String nam;

	public VRefVariableElement(boolean forbidSpaces, String nam) {
		super(forbidSpaces);

		this.nam = nam;
	}

	public void generate(GenerationState state) {
		if (!state.vars.containsKey(nam)) {
			throw new GrammarException(String.format("No variable '%s' defined", nam));
		}

		String strang = state.vars.get(nam);
		if(forbidSpaces && strang.contains(" ")) {
			throw new GrammarException(String.format("Cannot include variable %s w/ spaces in body in rule name", nam));
		}

		state.contents.append(strang);
	}
}
