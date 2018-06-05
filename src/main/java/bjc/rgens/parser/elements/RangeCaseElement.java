package bjc.rgens.parser.elements;

import bjc.rgens.parser.GenerationState;

public class RangeCaseElement extends CaseElement {
	public final int begin;
	public final int end;

	public RangeCaseElement(int beg, int en) {
		super(ElementType.RANGE);

		begin = beg;
		end = en;
	}

	public void generate(GenerationState state) {
		int val  = state.rnd.nextInt(end - begin);
		val     += begin;

		state.contents.append(val);
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
