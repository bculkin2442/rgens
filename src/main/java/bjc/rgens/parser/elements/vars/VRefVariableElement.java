package bjc.rgens.parser.elements.vars;

import bjc.rgens.parser.GenerationState;
import bjc.rgens.parser.GrammarException;

/**
 * Variable reference variable element.
 * 
 * @author Ben Culkin
 *
 */
public class VRefVariableElement extends VariableElement {
	/**
	 * The name of the variable to element.
	 */
	public final String nam;

	/**
	 * Create a new variable referencing variable element.
	 * @param forbidSpaces Whether or not to forbid spaces in the element.
	 * @param nam The name of the variable.
	 */
	public VRefVariableElement(boolean forbidSpaces, String nam) {
		super(forbidSpaces);

		this.nam = nam;
	}

	@Override
	public void generate(GenerationState state) {
		String strang = state.findVar(nam);

		if(forbidSpaces && strang.contains(" ")) {
			throw new GrammarException(String.format("Cannot include variable %s w/ spaces in body in rule name", nam));
		}

		state.appendContents(strang);
	}
}
