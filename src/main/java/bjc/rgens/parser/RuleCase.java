package bjc.rgens.parser;

import bjc.rgens.parser.elements.CaseElement;
import bjc.utils.funcdata.IList;

/*
 * @NOTE
 * 	If at some point we add new case types, they should go into subclasses,
 * 	not into this class.
 */
/**
 * A case in a rule in a randomized grammar.
 *
 * @author EVE
 */
public abstract class RuleCase {
	/**
	 * The possible types of a case.
	 *
	 * @author EVE
	 */
	public static enum CaseType {
		/** A normal case, composed from a list of elements. */
		NORMAL,
		/** A case that doesn't insert spaces. */
		SPACEFLATTEN,
		/** A case that applies a regex after generation. */
		REGEX;
	}

	/** The type of this case. */
	public final CaseType type;

	/**
	 * The list of element values for this case.
	 *
	 * <h2>Used For</h2>
	 * <dl>
	 * <dt>NORMAL, SPACEFLATTEN</dt>
	 * <dd>Used as the list of elementList the rule is composed of.</dd>
	 * </dl>
	 */
	protected IList<CaseElement> elementList;

	protected RuleCase(CaseType typ) {
		type = typ;
	}

	/**
	 * Create a new case of the specified type that takes a element list
	 * parameter.
	 *
	 * @param typ
	 * 	The type of case to create.
	 *
	 * @param elements
	 * 	The element list parameter of the case.
	 *
	 * @throws IllegalArgumentException
	 * 	If this type doesn't take a element list parameter.
	 */
	public RuleCase(CaseType typ, IList<CaseElement> elements) {
		this(typ);

		switch (typ) {
		case NORMAL:
		case SPACEFLATTEN:
			break;
		case REGEX:
			throw new IllegalArgumentException("This type requires an element list and a pattern");
		default:
			throw new IllegalArgumentException("This type doesn't have a element list parameter");
		}

		elementList = elements;
	}

	public abstract void generate(GenerationState state);

	/**
	 * Get the element list value of this type.
	 *
	 * @return 
	 * 	The element list value of this case, or null if this type
	 * 	doesn't have one.
	 */
	public IList<CaseElement> getElements() {
		return elementList;
	}
}
