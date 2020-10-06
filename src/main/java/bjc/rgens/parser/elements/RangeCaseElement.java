package bjc.rgens.parser.elements;

import bjc.rgens.parser.GenerationState;

/**
 * Case element which generates a range of random numbers.
 * 
 * @author Ben Culkin
 *
 */
public class RangeCaseElement extends CaseElement {
	/**
	 * The beginning point for this range.
	 */
	public final int begin;
	
	/**
	 * The ending point for this range.
	 */
	public final int end;

	/**
	 * Create a new range case element.
	 * @param beg The beginning point for the range.
	 * @param en The ending point for the range.
	 */
	public RangeCaseElement(int beg, int en) {
		super(true);

		begin = beg;
		end = en;
	}

	@Override
	public void generate(GenerationState state) {
		int val  = state.rnd.nextInt(end - begin);
		val     += begin;

		state.appendContents(Integer.toString(val));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + begin;
		result = prime * result + end;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		RangeCaseElement other = (RangeCaseElement) obj;
		if (begin != other.begin)
			return false;
		if (end != other.end)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("[%d..%d]", begin, end);
	}
}
