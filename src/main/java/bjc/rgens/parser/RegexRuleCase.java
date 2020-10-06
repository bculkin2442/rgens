package bjc.rgens.parser;

import bjc.rgens.parser.elements.CaseElement;

import java.util.List;

/*
 * @TODO
 *
 * Actually implement this
 */
public class RegexRuleCase extends RuleCase {
	public RegexRuleCase(List<CaseElement> elements) {
		super(elements);

	}

	public void generate(GenerationState state) {
		// TODO
	}

	@Override
	public RegexRuleCase withElements(List<CaseElement> elements) {
		return new RegexRuleCase(elements);
	}
}
