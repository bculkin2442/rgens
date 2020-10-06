package bjc.rgens.parser.elements;

import bjc.rgens.parser.GenerationState;

/**
 * Case element which is generated one or more times.
 * 
 * @author Ben Culkin
 *
 */
public class SerialCaseElement extends CaseElement {
	/**
	 * The case element to repeat.
	 */
	public final CaseElement rep;

	/**
	 * The lower bound of times to repeat.
	 */
	public final int lower;
	
	/**
	 * The upper bound of times to repeat.
	 */
	public final int upper;

	/**
	 * Create a new repeating case element.
	 * 
	 * @param rep The case element to repeat.
	 * @param lower The lower bound of times to repeat.
	 * @param upper The upper bound of times to repeat.
	 */
	public SerialCaseElement(CaseElement rep, int lower, int upper)  {
		super(rep.spacing);

		this.rep = rep;

		this.lower = lower;
		this.upper = upper;
	}

	@Override
	public void generate(GenerationState state) {
		int num = state.rnd.nextInt(upper - lower) + lower;

		for(int i = 0; i < num; i++) {
			rep.generate(state);

			if(rep.spacing)
				state.appendContents(" ");
		}
	}
}
