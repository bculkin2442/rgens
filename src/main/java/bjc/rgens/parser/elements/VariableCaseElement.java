package bjc.rgens.parser.elements;

public class VariableCaseElement extends CaseElement {
	/**
	 * The name of the variable this element defines.
	 */
	public final String varName;

	/**
	 * The definition of the variable this element defines.
	 */
	public final String varDef;

	public VariableCaseElement(String name, String def, boolean isExp) {
		super(isExp ? ElementType.EXPVARDEF : ElementType.VARDEF);

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
		VariableCaseElement other = (VariableCaseElement) obj;
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

	@Override
	public String toString() {
		if (type == ElementType.VARDEF) {
			return String.format("{%s:=%s}", varName, varDef);
		} else {
			return String.format("{%s=%s}", varName, varDef);
		}
	}
}
