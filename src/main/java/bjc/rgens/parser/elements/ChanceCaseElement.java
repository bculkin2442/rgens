package bjc.rgens.parser.elements;

import bjc.rgens.parser.GenerationState;

/**
 * A case element that has a '1 in n' chance to generate something.
 * @author Ben Culkin
 *
 */
public class ChanceCaseElement extends CaseElement {
	/**
	 * The case element to generate.
	 */
	public final CaseElement elm;

	/**
	 * The 'rarity' of generating output.
	 */
	public int chance;

	/**
	 * Create a new chance case element.
	 * @param elm The element to generate.
	 * @param chance The 'n' in the '1 in n' chance to generate the element.
	 */
	public ChanceCaseElement(CaseElement elm, int chance) {
		super(elm.spacing);

		this.elm    = elm;
		this.chance = chance;
	}

	@Override
	public void generate(GenerationState state) {
		if(state.rnd.nextInt(chance) == 0) elm.generate(state);
	}
}
