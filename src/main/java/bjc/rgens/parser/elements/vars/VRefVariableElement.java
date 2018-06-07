package bjc.rgens.parser.elements.vars;

import bjc.rgens.parser.GenerationState;
import bjc.rgens.parser.GrammarException;

public class VRefVariableElement extends VariableElement {
	public final String nam;

	private final boolean forbidSpaces;

	public VRefVariableElement(boolean forbidSpaces, String nam) {
		this.nam = nam;

		this.forbidSpaces = forbidSpaces;
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
