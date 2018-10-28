package bjc.rgens.parser.elements;

import static bjc.rgens.parser.RGrammarLogging.trace;

import bjc.rgens.parser.GenerationState;
import bjc.rgens.parser.GrammarException;
import bjc.utils.ioutils.LevelSplitter;

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

		if (csepart.matches("\\(\\S+\\)")) {
			return createElement(csepart.substring(1, csepart.length() - 1));
		} else if (csepart.matches("\\{\\S+\\}")) {
			/*
			 * Handle special case elements.
			 *
			 */
			String specialBody = csepart.substring(1, csepart.length() - 1);

			if (specialBody.matches("\\S+:\\S=\\S+")) {
				String[] parts = LevelSplitter.def.levelSplit(specialBody, "=").toArray(new String[0]);

				if(parts.length != 2) {
					throw new GrammarException("Colon variables must have a name and a definition");
				}

				String varName = parts[0];

				char op = varName.charAt(varName.length() - 1);

				trace("Colon definition w/ op %d", (int)op);

				// Remove the colon, plus any tacked on operator
				varName = varName.substring(0, varName.length() - 2);

				return VariableDefCaseElement.parseVariable(varName, parts[1], op, true);
			} else if (specialBody.matches("\\S+:=\\S+")) {
				String[] parts = LevelSplitter.def.levelSplit(specialBody, "=").toArray(new String[0]);

				if(parts.length != 2) {
					throw new GrammarException("Colon variables must have a name and a definition");
				}

				String varName = parts[0];

				varName = varName.substring(0, varName.length() - 1);

				return VariableDefCaseElement.parseVariable(varName, parts[1], ' ', true);
			} else if (specialBody.matches("\\S+=\\S+")) {
				String[] parts = LevelSplitter.def.levelSplit(specialBody, "=").toArray(new String[0]);
				if(parts.length != 2) {
					throw new GrammarException("Variables must have a name and a definition");
				}

				// Non-colon variables can't take an operator
				return VariableDefCaseElement.parseVariable(parts[0], parts[1], (char)0, false);
			} else if (specialBody.matches("empty")) {
				/* Literal blank, for empty cases. */
				return new BlankCaseElement();
			} else {
				throw new IllegalArgumentException(String.format("Unknown special case part '%s'", specialBody));
			}
		} else if (csepart.matches("\\[\\S+\\]")) {
			String rawCase = csepart.substring(1, csepart.length() - 1);

			if (rawCase.matches("\\d+\\.{2}\\d+")) {
				int firstNum = Integer.parseInt(rawCase.substring(0, rawCase.indexOf('.')));
				int secondNum = Integer.parseInt(rawCase.substring(rawCase.lastIndexOf('.') + 1));

				return new RangeCaseElement(firstNum, secondNum);
			} else if(rawCase.contains("||")) {
				String[] elms = LevelSplitter.def.levelSplit(rawCase, "||").toArray(new String[0]);

				return new InlineRuleCaseElement(elms);
			} else if(rawCase.contains("|")) {
				throw new GrammarException("Inline rule using | found, they use || now");

				// String[] elms = LevelSplitter.def.levelSplit(rawCase, "|").toArray(new String[0]);
				// return new InlineRuleCaseElement(elms);
			} else {
				return new RuleCaseElement(rawCase);
			}
		} else if(csepart.startsWith("%") && !csepart.equals("%")) {
			return new RuleCaseElement(csepart);	
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
