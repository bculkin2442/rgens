package bjc.rgens.parser.elements;

import bjc.rgens.parser.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class RuleCaseElement extends StringCaseElement {
	public static enum ReferenceType {
		DEPENDENT,
		VARIABLE,
		NORMAL
	}

	public final ReferenceType ref;

	protected RuleCaseElement(String vl, ReferenceType ref) {
		super(vl, false);

		this.ref = ref;
	}

	protected void doGenerate(String actName, GenerationState state) {
		GenerationState newState = state.newBuf();

		if (actName.startsWith("[^")) {
			actName = "[" + actName.substring(2);

			RGrammar dst = state.importRules.get(actName);

			newState.swapGrammar(dst);

			/* :Postprocessing */
			newState.contents = new StringBuilder(dst.generate(actName, state.rnd, state.vars, state.rlVars));
		} else if (state.rules.containsKey(actName)) {
			Rule rl = state.rules.get(actName);

			if(rl.doRecur()) {
				RuleCase cse = rl.getCase(state.rnd);
				System.err.printf("\tFINE: Generating %s (from %s)\n", cse, actName);

				state.gram.generateCase(cse, newState);

				rl.endRecur();
			} else {
				throw new RecurLimitException("Rule recurrence limit exceeded");
			}
		} else if (state.importRules.containsKey(actName)) {
			RGrammar dst = state.importRules.get(actName);

			newState.swapGrammar(dst);

			/* :Postprocessing */
			newState.contents = new StringBuilder(dst.generate(actName, state.rnd, state.vars));
		} else {
			/*
			 * @TODO 5/29/18 Ben Culkin :RuleSuggesting
			 *
			 * Re-get this working again.
			 */
			/*
			   if (ruleSearcher != null) {
			   Set<Match<? extends String>> results = ruleSearcher.search(actName, MAX_DISTANCE);

			   String[] resArray = results.stream().map(Match::getMatch).toArray((i) -> new String[i]);

			   String msg = String.format("No rule '%s' defined (perhaps you meant %s?)", actName,
			   StringUtils.toEnglishList(resArray, false));

			   throw new GrammarException(msg);
			   }
			   */

			String msg = String.format("No rule '%s' defined", actName);
			throw new GrammarException(msg);
		}

		String res = newState.contents.toString();

		if (actName.contains("+")) {
			/* Rule names with pluses in them get space-flattened */
			state.contents.append(res.replaceAll("\\s+", ""));
		} else {
			state.contents.append(res);
		}
	}
}
