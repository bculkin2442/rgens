package bjc.rgens.parser.elements;

import bjc.rgens.parser.GrammarException;

public abstract class VariableDefCaseElement extends CaseElement {
	/**
	 * The name of the variable this element defines.
	 */
	public final String varName;

	/**
	 * The definition of the variable this element defines.
	 */
	public final String varDef;

	public VariableDefCaseElement(String name, String def) {
		super(false);

		varName = name;
		varDef = def;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((varDef == null) ? 0 : varDef.hashCode());
		result = prime * result + ((varName == null) ? 0 : varName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		VariableDefCaseElement other = (VariableDefCaseElement) obj;
		if (varDef == null) {
			if (other.varDef != null)
				return false;
		} else if (!varDef.equals(other.varDef))
			return false;
		if (varName == null) {
			if (other.varName != null)
				return false;
		} else if (!varName.equals(other.varName))
			return false;
		return true;
	}

	public static CaseElement parseVariable(String varName, String varDef, char op, boolean colon) {
		if(varName.startsWith("$")) {
			// Handle normal/expanding variable definitions
			if(colon) return new ExpVariableCaseElement(varName.substring(1), varDef);

			return new LitVariableCaseElement(varName.substring(1), varDef);
		} else if(varName.startsWith("@")) {
			return new RuleVariableCaseElement(varName.substring(1), varDef, colon);
		} else {
			throw new GrammarException("Unrecognized declaration sigil " + varName.charAt(0));
		}
	}
}
