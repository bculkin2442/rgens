package bjc.rgens.parser.elements;

import java.util.List;

import bjc.rgens.parser.GenerationState;
import bjc.rgens.parser.GrammarException;
import bjc.rgens.parser.RGrammar;
import bjc.rgens.parser.Rule;
import bjc.rgens.parser.elements.vars.VariableElement;

public class RuleCaseElement extends CaseElement {
	public List<VariableElement> elements;

	public RuleCaseElement(String vl) {
		super(true);

		this.elements = VariableElement.parseElementString(vl);
	}

	public RuleCaseElement(String vl, List<VariableElement> elements) {
		super(true);

		this.elements = elements;
	}

	public void generate(GenerationState state) {
		GenerationState newState = state.newBuf();

		boolean inName = false;

		for(VariableElement elm : elements) {
			elm.generate(newState);

			if(inName == false) inName = elm.forbidSpaces;
		}

		String body = newState.getContents();

		if(inName) {
			doGenerate(String.format("[%s]", body), state);
		} else {
			state.appendContents(body);
		}
	}

	protected void doGenerate(String acName, GenerationState state) {
		GenerationState newState = state.newBuf();

		Rule rl;

		String actName = acName;
		
		if (actName.startsWith("[^")) {
			actName = "[" + actName.substring(2);

			rl = state.findImport(actName);
		} else {
			rl = state.findRule(actName, true);
		}

		if(rl != null) {
			RGrammar destGrammar = rl.belongsTo;
			newState.swapGrammar(destGrammar);
			/* 
			 * Don't postprocess the string, we should only do that
			 * once.
			 */
			String res = destGrammar.generate(actName, newState, false);
			newState.setContents(res);
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

		String res = newState.getContents();

		if (actName.contains("+")) {
			/* Rule names with pluses in them get space-flattened */
			state.appendContents(res.replaceAll("\\s+", ""));
		} else {
			state.appendContents(res);
		}
	}
}
