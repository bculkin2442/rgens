package bjc.rgens.parser.elements;

/**
 * Case element which writes a string.
 * 
 * @author Ben Culkin
 *
 */
public abstract class StringCaseElement extends CaseElement {
	/**
	 * String written by this element.
	 */
	public final String val;

	/**
	 * Create a new string inserting case element.
	 * 
	 * @param vl
	 *           The string to insert.
	 */
	protected StringCaseElement(String vl) {
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
