package bjc.rgens.newparser;

import bjc.utils.funcdata.IList;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RegexRuleCase extends RuleCase {
	private Pattern patt;

	public RegexRuleCase(IList<CaseElement> elements, String pattern) {
		super(RuleCase.CaseType.REGEX);

		elementList = elements;

		try {
			patt = Pattern.compile(pattern);
		} catch (PatternSyntaxException psex) {
			IllegalArgumentException iaex = 
				new IllegalArgumentException("This type requires a valid regular expression parameter");

			iaex.initCause(psex);

			throw iaex;
		}
	}

	public Pattern getPattern() {
		return patt;
	}
}
