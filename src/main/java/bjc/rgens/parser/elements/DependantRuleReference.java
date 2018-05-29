package bjc.rgens.parser.elements;

import bjc.rgens.parser.GrammarException;
import bjc.rgens.parser.GenerationState;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DependantRuleReference extends RuleCaseElement {
	private static Pattern NAMEVAR_PATTERN = Pattern.compile("\\$(\\w+)");

	public DependantRuleReference(String vl) {
		super(vl, ReferenceType.DEPENDENT);
	}

	@Override
	public void generate(GenerationState state) {
		String refBody = val.substring(1, val.length() - 1);

		/* Handle dependent rule names. */
		StringBuffer nameBuffer = new StringBuffer();

		Matcher nameMatcher = NAMEVAR_PATTERN.matcher(refBody);

		while (nameMatcher.find()) {
			String var = nameMatcher.group(1);

			if (!state.vars.containsKey(var)) {
				String msg = String.format("No variable '%s' defined", var);
				throw new GrammarException(msg);
			}

			String name = state.vars.get(var);

			if (name.contains(" ")) {
				throw new GrammarException("Variables substituted into names cannot contain spaces");
			} else if (name.equals("")) {
				throw new GrammarException("Variables substituted into names cannot be empty");
			}

			nameMatcher.appendReplacement(nameBuffer, name);
		}

		nameMatcher.appendTail(nameBuffer);

		doGenerate(String.format("[%s]", nameBuffer.toString()), state);
	}
}
