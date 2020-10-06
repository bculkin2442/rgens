package bjc.rgens.parser.elements;

import bjc.rgens.parser.GenerationState;

/**
 * Case element that appends a literal.
 * @author Ben Culkin
 *
 */
public class LiteralCaseElement extends CaseElement {
	/**
	 * The value for this element.
	 */
	public String val;

	/**
	 * Create a new case element.
	 * 
	 * @param val The value to append.
	 */
	public LiteralCaseElement(String val) {
		super(true);

		this.val = val;
	}

	@Override
	public void generate(GenerationState state) {
		state.appendContents(val);
	}
}
