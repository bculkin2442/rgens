package bjc.rgens.parser.elements;

import bjc.utils.funcutils.StringUtils;

import bjc.rgens.parser.GenerationState;
import bjc.rgens.parser.GrammarException;

/**
 * A element in a rule case.
 *
 * @author EVE
 */
public abstract class CaseElement {
	/**
	 * The possible types of an element.
	 *
	 * @author EVE
	 */
	public static enum ElementType {
		/** An element that represents a literal string. */
		LITERAL(true),
		/** An element that represents a rule reference. */
		RULEREF(true),
		/** An element that represents a random range. */
		RANGE(true),
		/** An element that represents a variable that stores a string. */
		VARIABLE(false);

		public final boolean spacing;

		private ElementType(boolean spacing) {
			this.spacing = spacing;
		}
	}

	/** The type of this element. */
	public boolean spacing;

	protected CaseElement() {
		this(true);
	}

	/**
	 * Create a new case element.
	 *
	 * @param typ
	 *            The type of this element.
	 */
	protected CaseElement(boolean spacing) {
		this.spacing = spacing;
	}

	/**
	 * Generate this case element.
	 *
	 * @param state
	 * 	The current state of generation.
	 */
	public abstract void generate(GenerationState state);

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

		if (csepart.matches("\\{\\S+\\}")) {
			/*
			 * Handle special case elements.
			 *
			 */
			String specialBody = csepart.substring(1, csepart.length() - 1);

			if (specialBody.matches("\\S+:=\\S+")) {
				String[] parts = specialBody.split(":=");
				if(parts.length != 2) {
					throw new GrammarException("Colon variables must have a name and a definition");
				}

				return VariableCaseElement.parseVariable(parts[0], parts[1], true);
			} else if (specialBody.matches("\\S+=\\S+")) {
				String[] parts = specialBody.split("=");
				if(parts.length != 2) {
					throw new GrammarException("Variables must have a name and a definition");
				}

				return VariableCaseElement.parseVariable(parts[0], parts[1], false);
			} else if (specialBody.matches("empty")) {
				/* Literal blank, for empty cases. */
				return new BlankCaseElement();
			} else {
				throw new IllegalArgumentException(String.format("Unknown special case part '%s'", specialBody));
			}
		} else if (csepart.matches("\\[\\S+\\]")) {
			String rawCase = csepart.substring(1, csepart.length() - 1);

			if (rawCase.matches("\\d+\\.\\.\\d+")) {
				int firstNum = Integer.parseInt(rawCase.substring(0, rawCase.indexOf('.')));
				int secondNum = Integer.parseInt(rawCase.substring(rawCase.lastIndexOf('.') + 1));

				return new RangeCaseElement(firstNum, secondNum);
			} else if(rawCase.contains("||")) {
				String[] elms = StringUtils.levelSplit(rawCase, "||").toArray(new String[0]);
				//String[] elms = rawCase.split("\\|\\|");

				return new InlineRuleCaseElement(elms);
			} else if(rawCase.contains("|")) {
				System.err.println("\t\tWARN: Inline rule using | found, they use || now");

				String[] elms = StringUtils.levelSplit(rawCase, "|").toArray(new String[0]);
				return new InlineRuleCaseElement(elms);
			} else if(csepart.contains("$")) {
				if(csepart.contains("-")) {
					return new DependantRuleReference(csepart);
				}

				return new VariableRuleReference(csepart);
			} else if(csepart.contains("@")) {
				// Trim @
				return new RuleVarRefCaseElement(rawCase.substring(1));
			} else {
				return new NormalRuleReference(csepart);
			}
		} else if(csepart.startsWith("%") && !csepart.equals("%")) {
			String rName = String.format("[%s]", csepart.substring(1));

			System.err.printf("\t\tTRACE: short ref to %s (%s)\n", rName, csepart);

			return new NormalRuleReference(rName);	
		} else {
			return new LiteralCaseElement(csepart);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (spacing ? 0 : 2);
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
		if (spacing != other.spacing)
			return false;
		return true;
	}
}
