package bjc.rgens.parser;

import java.util.List;

import bjc.rgens.parser.elements.CaseElement;

/**
 * A rule case that inserts spaces in between elements, where appropriate.
 *
 * @author Ben Culkin
 */
public class NormalRuleCase extends RuleCase {
	/**
	 * Create a new normal rule case.
	 *
	 * @param elms
	 * 	The elements of this case.
	 */
	public NormalRuleCase(List<CaseElement> elms) {
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

	/**
	 * Create a new normal rule case.
	 *
	 * @param elms
	 * 	The elements of this case.
	 *
	 * @return A normal rule case with those elements.
	 */
	public NormalRuleCase withElements(List<CaseElement> elms) {
		return new NormalRuleCase(elms);
	}
}
