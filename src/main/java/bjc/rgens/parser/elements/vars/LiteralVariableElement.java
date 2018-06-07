package bjc.rgens.parser.elements.vars;

import bjc.rgens.parser.GenerationState;

public class LiteralVariableElement extends VariableElement {
	public String val;

	public LiteralVariableElement(String val) {
		this.val = val;
	}

	public void generate(GenerationState state) {
		state.contents.append(val);
	}
}
