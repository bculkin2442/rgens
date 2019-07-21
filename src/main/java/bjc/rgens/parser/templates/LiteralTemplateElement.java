package bjc.rgens.parser.templates;

import bjc.utils.data.ITree;

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
	 * 		The string to insert.
	 */
	public LiteralTemplateElement(String val, ITree<String> errs) {
		super(true);

		this.val = val;
	}

	@Override
	public void generate(GenerationState state) {
		state.appendContents(val);
	}
}
