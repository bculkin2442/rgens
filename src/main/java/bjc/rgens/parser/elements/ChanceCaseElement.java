package bjc.rgens.parser.elements;

import bjc.rgens.parser.GenerationState;

public class ChanceCaseElement extends CaseElement {
	public final CaseElement elm;

	public int chance;

	public ChanceCaseElement(CaseElement elm, int chance) {
		super(elm.spacing);

		this.elm    = elm;
		this.chance = chance;
	}

	public void generate(GenerationState state) {
		if(state.rnd.nextInt(chance) == 0) elm.generate(state);
	}
}
