package bjc.rgens.parser.elements;

import bjc.rgens.parser.GenerationState;

public class LiteralCaseElement extends CaseElement {
	public String val;

	public LiteralCaseElement(String val) {
		super(true);

		this.val = val;
	}

	public void generate(GenerationState state) {
		state.contents.append(val);
	}
}
