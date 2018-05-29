package bjc.rgens.parser.elements;

import bjc.rgens.parser.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RuleCaseElement extends StringCaseElement {
	private static Pattern NAMEVAR_PATTERN = Pattern.compile("\\$(\\w+)");

	public RuleCaseElement(String vl) {
		super(vl, false);
	}

	@Override
	public void generate(GenerationState state) {
		/*
		 * @NOTE
		 *
		 * :VarRefactor
		 *
		 * Figure out if this can be refactored in some way.
		 */
		String refersTo = val;

		GenerationState newState = state.newBuf();

		if (refersTo.contains("$")) {
			/* Parse variables */
			String refBody = refersTo.substring(1, refersTo.length() - 1);

			if (refBody.contains("-")) {
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

				refersTo = "[" + nameBuffer.toString() + "]";
			} else {
				/* Handle string references. */
				if (refBody.equals("$")) {
					throw new GrammarException("Cannot refer to unnamed variables");
				}

				String key = refBody.substring(1);

				if (!state.vars.containsKey(key)) {
					String msg = String.format("No variable '%s' defined", key);
					throw new GrammarException(msg);
				}

				state.contents.append(state.vars.get(key));

				return;
			}
		}

		if (refersTo.startsWith("[^")) {
			refersTo = "[" + refersTo.substring(2);

			RGrammar dst = state.importRules.get(refersTo);

			newState.swapGrammar(dst);

			/* :Postprocessing */
			newState.contents = new StringBuilder(dst.generate(refersTo, state.rnd, state.vars));
		} else if (state.rules.containsKey(refersTo)) {
			RuleCase cse = state.rules.get(refersTo).getCase(state.rnd);

			state.gram.generateCase(cse, newState);
		} else if (state.importRules.containsKey(refersTo)) {
			RGrammar dst = state.importRules.get(refersTo);

			newState.swapGrammar(dst);

			/* :Postprocessing */
			newState.contents = new StringBuilder(dst.generate(refersTo, state.rnd, state.vars));
		} else {
			/*
			 * @TODO 5/29/18 Ben Culkin :RuleSuggesting
			 *
			 * Re-get this working again.
			 */
			/*
			if (ruleSearcher != null) {
				Set<Match<? extends String>> results = ruleSearcher.search(refersTo, MAX_DISTANCE);

				String[] resArray = results.stream().map(Match::getMatch).toArray((i) -> new String[i]);

				String msg = String.format("No rule '%s' defined (perhaps you meant %s?)", refersTo,
						StringUtils.toEnglishList(resArray, false));

				throw new GrammarException(msg);
			}
			*/

			String msg = String.format("No rule '%s' defined", refersTo);
			throw new GrammarException(msg);
		}

		if (refersTo.contains("+")) {
			/* Rule names with pluses in them get space-flattened */
			state.contents.append(newState.contents.toString().replaceAll("\\s+", ""));
		} else {
			state.contents.append(newState.contents.toString());
		}
	}
}
