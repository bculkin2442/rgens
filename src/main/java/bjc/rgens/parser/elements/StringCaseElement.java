package bjc.rgens.parser.elements;

public abstract class StringCaseElement extends CaseElement {
	public final String val;
	
	protected StringCaseElement(String vl, boolean isLiteral) {
		super(true);
		
		val = vl;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((val == null) ? 0 : val.hashCode());
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
		StringCaseElement other = (StringCaseElement) obj;
		if (val == null) {
			if (other.val != null)
				return false;
		} else if (!val.equals(other.val))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return val;
	}
}
