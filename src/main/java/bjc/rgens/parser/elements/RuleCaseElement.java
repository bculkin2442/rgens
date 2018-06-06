package bjc.rgens.parser.elements;

import bjc.utils.data.IPair;
import bjc.utils.data.Pair;

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

		Rule rl;

		if (actName.startsWith("[^")) {
			actName = "[" + actName.substring(2);

			rl = state.findImport(actName);
		} else {
			rl = state.findRule(actName, true);
		}

		if(rl != null) {
			RGrammar destGrammar = rl.belongsTo;
			newState.swapGrammar(destGrammar);
			String res = destGrammar.generate(actName, newState);

			/*
			 * @NOTE
			 *
			 * :Postprocessing
			 *
			 * This is because generate() returns a processed
			 * string, but modifies the passed in StringBuilder.
			 */
			newState.contents = new StringBuilder(res);
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
