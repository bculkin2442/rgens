package bjc.rgens.parser;

import bjc.rgens.parser.elements.CaseElement;
import bjc.utils.funcdata.IList;

/*
 * @TODO
 *
 * Actually implement this
 */
public class RegexRuleCase extends RuleCase {
	public RegexRuleCase(IList<CaseElement> elements) {
		super(elements);

	}

	public void generate(GenerationState state) {
		// TODO
	}

	public RegexRuleCase withElements(IList<CaseElement> elements) {
		return new RegexRuleCase(elements);
	}
}
