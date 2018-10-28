package bjc.rgens.parser;

import bjc.rgens.parser.elements.CaseElement;
import bjc.utils.funcdata.IList;

public class FlatRuleCase extends RuleCase {
	public FlatRuleCase(IList<CaseElement> elms) {
		super(elms);
	}

	@Override
	public void generate(GenerationState state) {
		for(CaseElement elm : elementList) {
			elm.generate(state);
		}
	}

	public FlatRuleCase withElements(IList<CaseElement> elms) {
		return new FlatRuleCase(elms);
	}
}

