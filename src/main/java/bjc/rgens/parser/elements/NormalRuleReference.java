package bjc.rgens.parser.elements;

import bjc.rgens.parser.GenerationState;

public class NormalRuleReference extends RuleCaseElement {
	public NormalRuleReference(String vl) {
		super(vl, ReferenceType.NORMAL);
	}

	@Override
	public void generate(GenerationState state) {
		String refersTo = val;

		GenerationState newState = state.newBuf();

		doGenerate(refersTo, state);
	}
}
