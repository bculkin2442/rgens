package bjc.rgens.parser.elements;

import bjc.rgens.parser.GenerationState;

public class SerialCaseElement extends CaseElement {
	public final CaseElement rep;

	public final int lower;
	public final int upper;

	public SerialCaseElement(CaseElement rep, int lower, int upper)  {
		super(rep.spacing);

		this.rep = rep;

		this.lower = lower;
		this.upper = upper;
	}

	public void generate(GenerationState state) {
		int num = state.rnd.nextInt(upper - lower) + lower;

		for(int i = 0; i < num; i++) {
			rep.generate(state);

			if(rep.spacing)
				state.appendContents(" ");
		}
	}
}
