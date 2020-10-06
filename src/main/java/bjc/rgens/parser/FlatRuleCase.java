package bjc.rgens.parser;

import bjc.rgens.parser.elements.CaseElement;

import java.util.List;

/**
 * A rule case that inserts nothing in between case elements.
 *
 * @author Ben Culkin
 */
public class FlatRuleCase extends RuleCase {
	/**
	 * Create a new flat rule case.
	 *
	 * @param elms
	 * 	The case elements that make up this case.
	 */
	public FlatRuleCase(List<CaseElement> elms) {
		super(elms);
	}

	@Override
	public void generate(GenerationState state) {
		for(CaseElement elm : elementList) {
			elm.generate(state);
		}
	}

	/**
	 * Create a new flat rule case with the given case elements.
	 *
	 * @param elms
	 * 	The elements to use for the rule case.
	 *
	 * @return A flat rule case, with the given elements.
	 */
	@Override
	public FlatRuleCase withElements(List<CaseElement> elms) {
		return new FlatRuleCase(elms);
	}
}

