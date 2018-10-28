package bjc.rgens.parser;

import bjc.rgens.parser.elements.CaseElement;
import bjc.utils.funcdata.IList;

public class NormalRuleCase extends RuleCase {
	public NormalRuleCase(IList<CaseElement> elms) {
		super(elms);
	}

	@Override
	public void generate(GenerationState state) {
		for(CaseElement elm : elementList) {
			elm.generate(state);

			if(elm.spacing) {
				state.appendContents(" ");
			}
		}
	}

	public NormalRuleCase withElements(IList<CaseElement> elms) {
		return new NormalRuleCase(elms);
	}
}
