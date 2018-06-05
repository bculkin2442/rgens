package bjc.rgens.parser.elements;

import bjc.rgens.parser.GenerationState;

public class LitVariableCaseElement extends VariableCaseElement {
	public LitVariableCaseElement(String name, String def) {
		super(name, def, VariableType.NORMAL);
	}

	public void generate(GenerationState state) {
		state.vars.put(varName, varDef);
	}
}
