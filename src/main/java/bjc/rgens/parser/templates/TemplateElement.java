package bjc.rgens.parser.templates;

import bjc.rgens.parser.GenerationState;

/**
 * Abstract element of a template.
 * 
 * @author Ben Culkin
 *
 */
public abstract class TemplateElement {
	/**
	 * Whether or not to handle spacing.
	 */
	public boolean spacing;

	/**
	 * The template this element belongs to.
	 */
	public GrammarTemplate belongsTo;

	/**
	 * Create a new template element.
	 * 
	 * @param spacing
	 *                Whether or not to handle spacing.
	 */
	protected TemplateElement(boolean spacing) {
		this.spacing = spacing;
	}

	/**
	 * Generate this template element.
	 * 
	 * @param state
	 *              The state for the generation.
	 */
	public abstract void generate(GenerationState state);
}
