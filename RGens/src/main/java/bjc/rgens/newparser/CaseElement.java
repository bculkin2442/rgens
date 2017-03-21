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
		 * An element that represents a literal string.
		 */
		LITERAL,
		/**
		 * An element that represents a rule reference.
		 */
		RULEREF,
		/**
		 * An element that represents a random range.
		 */
		RANGE;
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
	 * The starting integer value of this element.
	 * 
	 * <h2>Used For</h2>
	 * <dl>
	 * <dt>RANGE</dt>
	 * <dd>The inclusive start of the range</dd>
	 * </dl>
	 */
	private int start;

	/**
	 * The starting integer value of this element.
	 * 
	 * <h2>Used For</h2>
	 * <dl>
	 * <dt>RANGE</dt>
	 * <dd>The inclusive end of the range</dd>
	 * </dl>
	 */
	private int snd;

	/**
	 * Create a new case element.
	 * 
	 * @param typ
	 *                The type of this element.
	 * 
	 * @throws IllegalArgumentException
	 *                 If the specified type needs parameters.
	 */
	public CaseElement(ElementType typ) {
		switch(typ) {
		case LITERAL:
		case RULEREF:
			throw new IllegalArgumentException("This type requires a string parameter.");
		case RANGE:
			throw new IllegalArgumentException("This type requires two int parameters.");
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
	public CaseElement(ElementType typ, String val) {
		switch(typ) {
		case LITERAL:
		case RULEREF:
			break;
		case RANGE:
			throw new IllegalArgumentException("This type requires two int parameters.");
		default:
			throw new IllegalArgumentException("This type doesn't have a string parameter.");
		}

		type = typ;

		literalVal = val;
	}

	/**
	 * Create a new case element that has two integer values.
	 * 
	 * @param typ
	 *                The type of this element.
	 * @param first
	 *                The first integer value for this element.
	 * @param second
	 *                The second integer value for this element.
	 * 
	 * @throws IllegalArgumentException
	 *                 If the specified type doesn't take two integer
	 *                 parameters.
	 */
	public CaseElement(ElementType typ, int first, int second) {
		switch(typ) {
		case LITERAL:
		case RULEREF:
			throw new IllegalArgumentException("This type requires a string parameter.");
		case RANGE:
			break;
		default:
			throw new IllegalArgumentException("This type doesn't have two int parameters");
		}

		type = typ;

		this.start = first;
		this.snd = second;
	}

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

	/**
	 * Get the starting integer value for this element.
	 * 
	 * @return The starting integer value for this element.
	 * 
	 * @throws IllegalStateException
	 *                 If this type doesn't have a starting integer value.
	 */
	public int getStart() {
		switch(type) {
		case RANGE:
			break;
		default:
			throw new IllegalStateException(
					String.format("Type '%s' doesn't have a starting integer value", type));

		}
		return start;
	}

	/**
	 * Get the ending integer value for this element.
	 * 
	 * @return The ending integer value for this element.
	 * 
	 * @throws IllegalStateException
	 *                 If this type doesn't have a ending integer value.
	 */
	public int getEnd() {
		switch(type) {
		case RANGE:
			break;
		default:
			throw new IllegalStateException(
					String.format("Type '%s' doesn't have a ending integer value", type));

		}

		return snd;
	}

	@Override
	public String toString() {
		switch(type) {
		case LITERAL:
		case RULEREF:
			return literalVal;
		case RANGE:
			return String.format("[%d..%d]", start, snd);
		default:
			return String.format("Unknown type '%s'", type);
		}
	}
}