package bjc.rgens.parser.elements;

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
		return String.format("Unknown type '%s'", type);
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

		if (csepart.matches("\\{[^}]+\\}")) {
			/*
			 * Handle special case elements.
			 *
			 */
			String specialBody = csepart.substring(1, csepart.length() - 1);

			//System.out.printf("\t\tTRACE: special body is '%s'\n", specialBody);

			if (specialBody.matches("\\$\\S+:=\\S+")) {
				/* Handle expanding variable definitions. */
				String[] parts = specialBody.split(":=");

				if (parts.length != 2) {
					String msg = "Expanded variables must be a name and a definition, seperated by :=";

					throw new GrammarException(msg);
				}

				/* Trim $ */
				return new ExpVariableCaseElement(parts[0].substring(1), parts[1]);
			} else if (specialBody.matches("\\$\\S+=\\S+")) {
				/* Handle regular variable definitions. */
				String[] parts = specialBody.split("=");

				if (parts.length != 2) {
					String msg = "Variables must be a name and a definition, seperated by =";

					throw new GrammarException(msg);
				}

				/* Trim $ */
				return new LitVariableCaseElement(parts[0].substring(1), parts[1]);
			} else if (specialBody.matches("\\@\\S+=\\S+")) {
				/* Handle rule variable definitions. */
				String[] parts = specialBody.split("=");

				if (parts.length != 2) {
					String msg = "Rule variables must be a name and a definition, seperated by =";

					throw new GrammarException(msg);
				}

				/* Trim $ */
				return new RuleVariableCaseElement(parts[0].substring(1), parts[1], false);
			} else if (specialBody.matches("\\@\\S+:=\\S+")) {
				/* Handle exhaustible rule variable definitions. */
				String[] parts = specialBody.split("=");

				if (parts.length != 2) {
					String msg = "Rule variables must be a name and a definition, seperated by =";

					throw new GrammarException(msg);
				}

				/* Trim $ */
				return new RuleVariableCaseElement(parts[0].substring(1), parts[1], true);
			} else if (specialBody.matches("empty")) {
				/* Literal blank, for empty cases. */
				return new BlankCaseElement();
			} else {
				throw new IllegalArgumentException(String.format("Unknown special case part '%s'", specialBody));
			}
		} else if (csepart.matches("\\[[^\\]]+\\]")) {
			String rawCase = csepart.substring(1, csepart.length() - 1);

			if (rawCase.matches("\\d+\\.\\.\\d+")) {
				int firstNum = Integer.parseInt(rawCase.substring(0, rawCase.indexOf('.')));
				int secondNum = Integer.parseInt(rawCase.substring(rawCase.lastIndexOf('.') + 1));

				return new RangeCaseElement(firstNum, secondNum);
			} else if(rawCase.contains("|")) {
				String[] elms = rawCase.split("\\|");

				System.err.printf("\tTRACE: Split inline cases %s to ", rawCase);
				for(String elm : elms) {
					System.err.printf("%s, ", elm);
				}
				System.err.println();

				return new InlineRuleCaseElement(elms);
			} else if(csepart.contains("$")) {
				/*
				 * @NOTE
				 *
				 * Once the rule element execution has been refactored,
				 * pass rawCase instead.
				 */
				if(csepart.contains("-")) {
					return new DependantRuleReference(csepart);
				}

				return new VariableRuleReference(csepart);
			} else if(csepart.contains("@")) {
				// Trim @
				return new RuleVarRefCaseElement(csepart.substring(1));
			} else {
				return new NormalRuleReference(csepart);
			}
		} else if(csepart.startsWith("%")) {
			String rName = String.format("[%s]", csepart.substring(1));

			System.err.printf("\tTRACE: short ref to %s (%s)\n", rName, csepart);

			return new NormalRuleReference(rName);	
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
