package bjc.rgens.parser;

import bjc.rgens.parser.elements.CaseElement;
import bjc.utils.funcdata.IList;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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

	}

	public RegexRuleCase withElements(IList<CaseElement> elements) {
		return new RegexRuleCase(elements);
	}
}
