package bjc.rgens.newparser;

/**
 * A element in a rule case.
 * 
 * @author EVE
 */
public class CaseElement {
	/**
	 * The possible types of an element.
	 * 
	 * @author EVE
	 *
	 */
	public static enum ElementType {
		/**
		 * A element that represents a literal string.
		 */
		LITERAL,
		/**
		 * A element that represents a rule reference.
		 */
		RULEREF;
	}

	/**
	 * The type of this element.
	 */
	public final CaseElement.ElementType type;

	/**
	 * The literal string value of this element.
	 * 
	 * This means that it is a string whose value should always mean the
	 * same thing.
	 * 
	 * <h2>Used For</h2>
	 * <dl>
	 * <dt>LITERAL</dt>
	 * <dd>The string this element represents</dd>
	 * <dt>RULEREF</dt>
	 * <dd>The name of the rule this element references</dd>
	 * </dl>
	 */
	private String literalVal;

	/**
	 * Create a new case element.
	 * 
	 * @param typ
	 *                The type of this element.
	 * 
	 * @throws IllegalArgumentException
	 *                 If the specified type needs parameters.
	 */
	public CaseElement(CaseElement.ElementType typ) {
		switch(typ) {
		case LITERAL:
		case RULEREF:
			throw new IllegalArgumentException("This type requires a string parameter.");
		default:
			break;
		}
		type = typ;
	}

	/**
	 * Create a new case element that has a single string value.
	 * 
	 * @param typ
	 *                The type of this element.
	 * 
	 * @param val
	 *                The string value of this element.
	 * 
	 * @throws IllegalArgumentException
	 *                 If the specified type doesn't take a single string
	 *                 parameter.
	 */
	public CaseElement(CaseElement.ElementType typ, String val) {
		switch(typ) {
		case LITERAL:
		case RULEREF:
			break;
		default:
			throw new IllegalArgumentException("This type doesn't have a string parameter.");
		}

		type = typ;

		literalVal = val;
	}

	/**

	/**
	 * Get the literal string value for this element.
	 * 
	 * @return The literal string value for this element.
	 * 
	 * @throws IllegalStateException
	 *                 If this type doesn't have a literal string value.
	 */
	public String getLiteral() {
		switch(type) {
		case LITERAL:
		case RULEREF:
			break;
		default:
			throw new IllegalStateException(String.format("Type '%s' doesn't have a literal string value"));
		}

		return literalVal;
	}

	@Override
	public String toString() {
		switch(type) {
		case LITERAL:
		case RULEREF:
			return literalVal;
		default:
			return String.format("Unknown type '%s'", type);
		}
	}
}