package bjc.rgens.parser.elements.vars;

import bjc.rgens.parser.GenerationState;

/**
 * Case element which references a literal variable.
 * 
 * @author Ben Culkin
 *
 */
public class LiteralVariableElement extends VariableElement {
	/**
	 * The name for the variable.
	 */
	public String val;

	/**
	 * Create a case element which references a literal variable.
	 * 
	 * @param forbidSpaces Whether to forbid spaces in the result.
	 * @param val The name of the literal variable.
	 */
	public LiteralVariableElement(boolean forbidSpaces, String val) {
		super(forbidSpaces);

		this.val = val;
	}

	@Override
	public void generate(GenerationState state) {
		state.appendContents(val);
	}
}
