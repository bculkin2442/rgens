package bjc.rgens.parser.templates;

import bjc.rgens.parser.GenerationState;

public class LiteralTemplateElement extends TemplateElement {
	public final String val;

	public LiteralTemplateElement(String val) {
		super(ElementType.LITERAL);

		this.val = val;
	}

	public void generate(GenerationState state) {
		state.contents.append(val);
	}
}
