package bjc.rgens.parser.elements;

import bjc.rgens.parser.GenerationState;

/**
 * A case element that defines a literal variable.
 * 
 * @author Ben Culkin
 *
 */
public class LitVariableCaseElement extends VariableDefCaseElement {
	/**
	 * Create a new case element for a literal variable.
	 * 
	 * @param name The name for the case element.
	 * @param def The definition for the case element.
	 */
	public LitVariableCaseElement(String name, String def) {
		super(name, def);
	}

	@Override
	public void generate(GenerationState state) {
		state.defineVar(varName, varDef);
	}
}
