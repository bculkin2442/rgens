package bjc.rgens.parser;

import bjc.rgens.parser.elements.CaseElement;

import java.util.List;

public class FlatRuleCase extends RuleCase {
	public FlatRuleCase(List<CaseElement> elms) {
		super(elms);
	}

	@Override
	public void generate(GenerationState state) {
		for(CaseElement elm : elementList) {
			elm.generate(state);
		}
	}

	public FlatRuleCase withElements(List<CaseElement> elms) {
		return new FlatRuleCase(elms);
	}
}

