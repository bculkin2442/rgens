package bjc.rgens.parser.templates;

import bjc.rgens.parser.GenerationState;

public abstract class TemplateElement {
	public static enum ElementType {
		LITERAL(true),
		TEMPLATE(true),
		PRAGMA(false);

		public final boolean spacing;

		private ElementType(boolean spacing) {
			this.spacing = spacing;
		}
	}

	public final ElementType type;

	public GrammarTemplate belongsTo;

	protected TemplateElement(ElementType type) {
		this.type = type;
	}

	public abstract void generate(GenerationState state);
}
