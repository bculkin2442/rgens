package bjc.rgens.parser.templates;

import bjc.rgens.parser.GenerationState;

public abstract class TemplateElement {
	public boolean spacing;

	public GrammarTemplate belongsTo;

	protected TemplateElement(boolean spacing) {
		this.spacing = spacing;
	}

	public abstract void generate(GenerationState state);
}
