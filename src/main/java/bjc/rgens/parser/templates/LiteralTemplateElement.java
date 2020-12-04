package bjc.rgens.parser.templates;

import bjc.data.Tree;

import bjc.rgens.parser.GenerationState;

/**
 * Represents a literal text element.
 *
 * @author Ben Culkin
 */
public class LiteralTemplateElement extends TemplateElement {
	/**
	 * The literal value of the element.
	 */
	public final String val;

	/**
	 * Create a new literal template element.
	 *
	 * @param val
	 *             The string to insert.
	 * 
	 * @param errs
	 *             The place to put errors.
	 */
	public LiteralTemplateElement(String val, Tree<String> errs) {
		super(true);

		this.val = val;
	}

	@Override
	public void generate(GenerationState state) {
		state.appendContents(val);
	}
}
