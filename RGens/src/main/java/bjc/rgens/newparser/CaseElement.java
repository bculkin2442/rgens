package bjc.rgens.newparser;

import static bjc.rgens.newparser.CaseElement.ElementType.*;

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
		RANGE,
		/**
		 * An element that represents a variable that stores a string.
		 */
		VARDEF,
		/**
		 * An element that represents a variable that stores the result
		 * of generating a rule.
		 */
		EXPVARDEF;
	}

	private static final String     SPECIAL_CASELEM = "\\{[^}]+\\}";
	private static final String     REFER_CASELEM   = "\\[[^\\]]+\\]";
	private static final String     RANGE_CASELM    = "\\[\\d+\\.\\.\\d+\\]";

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
	private int end;

	/**
	 * The name of the variable this element defines.
	 *
	 * <h2>Used For</h2>
	 * <dl>
	 * <dt>VARDEF</dt>
	 * <dd>The name of the variable</dd>
	 * <dt>EXPVARDEF</dt>
	 * <dd>The name of the variable</dd>
	 * </dl>
	 */
	private String varName;

	/**
	 * The definition of the variable this element defines.
	 *
	 * <h2>Used For</h2>
	 * <dl>
	 * <dt>VARDEF</dt>
	 * <dd>The value of the variable</dd>
	 * <dt>EXPVARDEF</dt>
	 * <dd>The rule to expand for the value of this variable</dd>
	 * </dl>
	 */
	private String varDef;

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
		switch (typ) {
		case LITERAL:
		case RULEREF:
			throw new IllegalArgumentException("This type requires a string parameter");

		case RANGE:
			throw new IllegalArgumentException("This type requires two integer parameters");

		case VARDEF:
		case EXPVARDEF:
			throw new IllegalArgumentException("This type requires two string parameters");

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
		switch (typ) {
		case LITERAL:
		case RULEREF:
			break;

		case RANGE:
			throw new IllegalArgumentException("This type requires two integer parameters");

		case VARDEF:
		case EXPVARDEF:
			throw new IllegalArgumentException("This type requires two string parameters");

		default:
			throw new IllegalArgumentException("This type doesn't have a string parameter");
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
		switch (typ) {
		case LITERAL:
		case RULEREF:
			throw new IllegalArgumentException("This type requires a string parameter");

		case RANGE:
			break;

		case VARDEF:
		case EXPVARDEF:
			throw new IllegalArgumentException("This type requires two string parameters");

		default:
			throw new IllegalArgumentException("This type doesn't have two integer parameters");
		}

		type = typ;

		this.start = first;
		this.end = second;
	}

	/**
	 * Create a new case element that has two string values.
	 *
	 * @param typ
	 *                The type of this element.
	 * @param name
	 *                The first string value for this element.
	 * @param def
	 *                The second string value for this element.
	 *
	 * @throws IllegalArgumentException
	 *                 If the specified type doesn't take two string
	 *                 parameters.
	 */
	public CaseElement(ElementType typ, String name, String def) {
		switch (typ) {
		case LITERAL:
		case RULEREF:
			throw new IllegalArgumentException("This type requires a string parameter");

		case RANGE:
			throw new IllegalArgumentException("This type requires two integer parameters");

		case VARDEF:
		case EXPVARDEF:
			break;

		default:
			throw new IllegalArgumentException("This type doesn't have two string parameters");
		}

		type = typ;

		this.varName = name;
		this.varDef = def;
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
		switch (type) {
		case LITERAL:
		case RULEREF:
			break;

		default:
			throw new IllegalStateException(
			        String.format("Type '%s' doesn't have a literal string value"));
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
		switch (type) {
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
		switch (type) {
		case RANGE:
			break;

		default:
			throw new IllegalStateException(
			        String.format("Type '%s' doesn't have a ending integer value", type));
		}

		return end;
	}

	/**
	 * Get the variable name for this element.
	 *
	 * @return The variable name of this element.
	 *
	 * @throws IllegalStateException
	 *                 If the type doesn't have a variable name.
	 */
	public String getName() {
		switch (type) {
		case VARDEF:
		case EXPVARDEF:
			break;

		default:
			throw new IllegalStateException(String.format("Type '%s' doesn't have a name", type));
		}

		return varName;
	}

	/**
	 * Get the variable definition for this element.
	 *
	 * @return The variable definition of this element.
	 *
	 * @throws IllegalStateException
	 *                 If the type doesn't have a variable definition.
	 */
	public String getDefn() {
		switch (type) {
		case VARDEF:
		case EXPVARDEF:
			break;

		default:
			throw new IllegalStateException(String.format("Type '%s' doesn't have a name", type));
		}

		return varDef;
	}

	@Override
	public String toString() {
		switch (type) {
		case LITERAL:
		case RULEREF:
			return literalVal;

		case RANGE:
			return String.format("[%d..%d]", start, end);

		case VARDEF:
			return String.format("{%s:=%s}", varName, varDef);

		case EXPVARDEF:
			return String.format("{%s=%s}", varName, varDef);

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
		if (csepart == null) {
			throw new NullPointerException("Case part cannot be null");
		}

		if (csepart.matches(SPECIAL_CASELEM)) {
			/*
			 * Handle special cases.
			 */
			String specialBody = csepart.substring(1, csepart.length() - 1);

			if (specialBody.matches("\\S+:=\\S+")) {
				/*
				 * Handle expanding variable definitions.
				 */
				String[] parts = specialBody.split(":=");

				if (parts.length != 2) {
					throw new GrammarException("Expanded variables must be a name and a definition,"
					                           + " seperated by :=");
				}

				return new CaseElement(EXPVARDEF, parts[0], parts[1]);
			} else if (specialBody.matches("\\S+=\\S+")) {
				/*
				 * Handle regular variable definitions.
				 */
				/*
				 * Handle expanding variable definitions.
				 */
				String[] parts = specialBody.split("=");

				if (parts.length != 2) {
					throw new GrammarException("Variables must be a name and a definition,"
					                           + " seperated by =");
				}

				return new CaseElement(VARDEF, parts[0], parts[1]);
			} else if (specialBody.matches("{empty}")) {
				/*
				 * Literal blank, for empty cases.
				 */
				return new CaseElement(LITERAL, "");
			} else {
				throw new IllegalArgumentException(
				        String.format("Unknown special case part '%s'", specialBody));
			}
		} else if (csepart.matches(REFER_CASELEM)) {
			if (csepart.matches(RANGE_CASELM)) {
				/*
				 * Handle ranges
				 */
				String rawRange = csepart.substring(1, csepart.length() - 1);

				int firstNum = Integer.parseInt(rawRange.substring(0, rawRange.indexOf('.')));
				int secondNum = Integer.parseInt(rawRange.substring(rawRange.lastIndexOf('.') + 1));

				return new CaseElement(RANGE, firstNum, secondNum);
			}

			return new CaseElement(RULEREF, csepart);
		} else {
			return new CaseElement(LITERAL, csepart);
		}
	}
}