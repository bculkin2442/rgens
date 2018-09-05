package bjc.rgens.parser.elements;

import bjc.rgens.parser.GenerationState;

public class LitVariableCaseElement extends VariableDefCaseElement {
	public LitVariableCaseElement(String name, String def) {
		super(name, def);
	}

	public void generate(GenerationState state) {
		state.defineVar(varName, varDef);
	}
}
