package bjc.rgens.parser.elements;

import bjc.rgens.parser.GenerationState;

public class LiteralCaseElement extends StringCaseElement {
	public LiteralCaseElement(String vl) {
		super(vl, true);
	}

	@Override
	public void generate(GenerationState state) {
		state.contents.append(val);
	}
}
