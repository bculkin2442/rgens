package bjc.rgens.parser;

import bjc.utils.funcdata.IList;

import bjc.rgens.parser.elements.CaseElement;

public class FlatRuleCase extends RuleCase {
	public FlatRuleCase(IList<CaseElement> elms) {
		super(CaseType.SPACEFLATTEN, elms);
	}

	@Override
	public void generate(GenerationState state) {
		for(CaseElement elm : elementList) {
			elm.generate(state);
		}
	}
}

