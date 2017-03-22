package bjc.rgens.newparser;

import static bjc.rgens.newparser.CaseElement.ElementType.LITERAL;
import static bjc.rgens.newparser.CaseElement.ElementType.RANGE;
import static bjc.rgens.newparser.CaseElement.ElementType.RULEREF;

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

	private static final String	SPECIAL_CASELEM	= "\\{[^}]\\}";
	private static final String	REFER_CASELEM	= "\\[[^\\]]+\\]";
	private static final String	RANGE_CASELM	= "\\[\\d+\\.\\.\\d+\\]";

	/**
	 * The type of this element.
	 */
	public final ElementType type;

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

	/**
	 * Create a case element from a string.
	 * 
	 * @param csepart
	 *                The string to convert.
	 * 
	 * @return A case element representing the string.
	 */
	public static CaseElement createElement(String csepart) {
		if(csepart == null) {
			throw new NullPointerException("Case part cannot be null");
		}

		if(csepart.matches(CaseElement.SPECIAL_CASELEM)) {
			/*
			 * Handle other cases.
			 */
		} else if(csepart.matches(CaseElement.REFER_CASELEM)) {
			if(csepart.matches(CaseElement.RANGE_CASELM)) {
				/*
				 * Handle ranges
				 */
				String rawRange = csepart.substring(1, csepart.length() - 1);

				int firstNum = Integer.parseInt(rawRange.substring(0, rawRange.indexOf('.')));
				int secondNum = Integer.parseInt(rawRange.substring(rawRange.lastIndexOf('.') + 1));

				return new CaseElement(RANGE, firstNum, secondNum);
			} else {
				return new CaseElement(RULEREF, csepart);
			}
		} else {
			return new CaseElement(LITERAL, csepart);
		}

		throw new IllegalArgumentException(String.format("Unknown case part '%s'"));
	}
}