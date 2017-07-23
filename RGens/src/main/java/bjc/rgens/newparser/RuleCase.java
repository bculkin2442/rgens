package bjc.rgens.newparser;

import bjc.utils.funcdata.IList;

/**
 * A case in a rule in a randomized grammar.
 *
 * @author EVE
 */
public class RuleCase {
	/**
	 * The possible types of a case.
	 *
	 * @author EVE
	 *
	 */
	public static enum CaseType {
		/**
		 * A normal case, composed from a list of elementList.
		 */
		NORMAL,
	}

	/**
	 * The type of this case.
	 */
	public final CaseType type;

	/**
	 * The list of element values for this case.
	 *
	 * <h2>Used For</h2>
	 * <dl>
	 * <dt>NORMAL</dt>
	 * <dd>Used as the list of elementList the rule is composed of.</dd>
	 * </dl>
	 */
	private IList<CaseElement> elementList;

	/**
	 * Create a new case of the specified type.
	 *
	 * @param typ
	 *                The type of case to create.
	 *
	 * @throws IllegalArgumentException
	 *                 If the type requires parameters.
	 */
	public RuleCase(CaseType typ) {
		switch (typ) {
		case NORMAL:
			throw new IllegalArgumentException("This type requires an element list parameter");

		default:
			break;
		}

		type = typ;
	}

	/**
	 * Create a new case of the specified type that takes a element list
	 * parameter.
	 *
	 * @param typ
	 *                The type of case to create.
	 *
	 * @param elements
	 *                The element list parameter of the case.
	 *
	 * @throws IllegalArgumentException
	 *                 If this type doesn't take a element list parameter.
	 */
	public RuleCase(CaseType typ, IList<CaseElement> elements) {
		switch (typ) {
		case NORMAL:
			break;

		default:
			throw new IllegalArgumentException("This type doesn't have a element list parameter");
		}

		type = typ;

		elementList = elements;
	}

	/**
	 * Get the element list value of this type.
	 *
	 * @return The element list value of this case, or null if this type
	 *         doesn't have one.
	 */
	public IList<CaseElement> getElements() {
		return elementList;
	}
}