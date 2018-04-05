package bjc.rgens.parser.elements;

import static bjc.rgens.parser.elements.CaseElement.ElementType.*;

import bjc.rgens.parser.GrammarException;

/*
 * @TODO 10/11/17 Ben Culkin :CaseElementSplit Split this into multiple
 * subclasses based off of a value of ElementType.
 */
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
	 */
	public static enum ElementType {
		/** An element that represents a literal string. */
		LITERAL,
		/** An element that represents a rule reference. */
		RULEREF,
		/** An element that represents a random range. */
		RANGE,
		/** An element that represents a variable that stores a string. */
		VARDEF,
		/**
		 * An element that represents a variable that stores the result of generating a
		 * rule.
		 */
		EXPVARDEF;
	}

	/* Regexps for marking rule types. */
	private static final String SPECIAL_CASELEM = "\\{[^}]+\\}";
	private static final String REFER_CASELEM = "\\[[^\\]]+\\]";
	private static final String RANGE_CASELM = "\\[\\d+\\.\\.\\d+\\]";

	/** The type of this element. */
	public final ElementType type;

	/**
	 * Create a new case element.
	 *
	 * @param typ
	 *            The type of this element.
	 */
	protected CaseElement(ElementType typ) {
		type = typ;
	}

	@Override
	public String toString() {
		switch (type) {
		default:
			return String.format("Unknown type '%s'", type);
		}
	}

	/**
	 * Create a case element from a string.
	 *
	 * @param csepart
	 *            The string to convert.
	 *
	 * @return A case element representing the string.
	 */
	public static CaseElement createElement(String csepart) {
		if (csepart == null) {
			throw new NullPointerException("Case part cannot be null");
		}

		if (csepart.matches(SPECIAL_CASELEM)) {
			/* Handle special cases. */
			String specialBody = csepart.substring(1, csepart.length() - 1);

			System.out.printf("\t\tTRACE: special body is '%s'\n", specialBody);

			if (specialBody.matches("\\S+:=\\S+")) {
				/* Handle expanding variable definitions. */
				String[] parts = specialBody.split(":=");

				if (parts.length != 2) {
					String msg = "Expanded variables must be a name and a definition, seperated by :=";

					throw new GrammarException(msg);
				}

				return new ExpVariableCaseElement(parts[0], parts[1]);
			} else if (specialBody.matches("\\S+=\\S+")) {
				/* Handle regular variable definitions. */
				String[] parts = specialBody.split("=");

				if (parts.length != 2) {
					String msg = "Variables must be a name and a definition, seperated by =";

					throw new GrammarException(msg);
				}

				return new LitVariableCaseElement(parts[0], parts[1]);
			} else if (specialBody.matches("{empty}")) {
				/* Literal blank, for empty cases. */
				return new BlankCaseElement();
			} else {
				throw new IllegalArgumentException(String.format("Unknown special case part '%s'", specialBody));
			}
		} else if (csepart.matches(REFER_CASELEM)) {
			if (csepart.matches(RANGE_CASELM)) {
				/* Handle ranges */
				String rawRange = csepart.substring(1, csepart.length() - 1);

				int firstNum = Integer.parseInt(rawRange.substring(0, rawRange.indexOf('.')));
				int secondNum = Integer.parseInt(rawRange.substring(rawRange.lastIndexOf('.') + 1));

				return new RangeCaseElement(firstNum, secondNum);
			}

			return new RuleCaseElement(csepart);
		} else {
			return new LiteralCaseElement(csepart);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CaseElement other = (CaseElement) obj;
		if (type != other.type)
			return false;
		return true;
	}
}