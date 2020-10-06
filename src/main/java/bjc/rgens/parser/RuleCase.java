package bjc.rgens.parser;

import bjc.rgens.parser.elements.CaseElement;

import java.util.List;

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
	 * Debugging name for this case.
	 */
	public String debugName;

	/**
	 * Serial number for this case.
	 */
	public final int serial;
	private static int nextSerial = 0;

	/**
	 * The rule this case belongs to.
	 */
	public Rule belongsTo;

	/**
	 * The elements that make up this case.
	 */
	public List<CaseElement> elementList;

	/**
	 * Create a new case of the specified type that takes a element list
	 * parameter.
	 *
	 * @param elements
	 * 	The element list parameter of the case.
	 *
	 */
	protected RuleCase(List<CaseElement> elements) {
		elementList = elements;

		serial      = nextSerial;
		nextSerial += 1;
	}

	/**
	 * Generate this case.
	 * 
	 * @param state The state to use.
	 */
	public abstract void generate(GenerationState state);

	/**
	 * Create a new case with a different set of elements.
	 * 
	 * @param elements The elements for this case.
	 * 
	 * @return The case with the same settings, but a different set of elements.
	 */
	public abstract RuleCase withElements(List<CaseElement> elements);

	@Override
	public String toString() {
		if(debugName != null) {
			return String.format("Case %s (#%d) of %s", debugName, serial, belongsTo);
		} else {
			return String.format("Case #%d of %s", serial, belongsTo, serial, belongsTo);
		}
	}

}
