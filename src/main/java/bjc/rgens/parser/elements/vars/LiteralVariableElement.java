package bjc.rgens.parser.elements.vars;

import bjc.rgens.parser.GenerationState;

public class LiteralVariableElement extends VariableElement {
	public String val;

	public LiteralVariableElement(boolean forbidSpaces, String val) {
		super(forbidSpaces);

		this.val = val;
	}

	public void generate(GenerationState state) {
		state.contents.append(val);
	}
}
