package bjc.rgens.parser;

import bjc.utils.funcdata.IList;

import bjc.rgens.parser.elements.CaseElement;

public class NormalRuleCase extends RuleCase {
	public NormalRuleCase(IList<CaseElement> elms) {
		super(CaseType.NORMAL, elms);
	}

	@Override
	public void generate(GenerationState state) {
		for(CaseElement elm : elementList) {
			elm.generate(state);

			if(elm.type.spacing) {
				state.contents.append(" ");
			}
		}
	}
}
