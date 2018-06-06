package bjc.rgens.parser.elements;

import bjc.utils.data.IPair;

import bjc.rgens.parser.GenerationState;
import bjc.rgens.parser.GrammarException;
import bjc.rgens.parser.RecurLimitException;
import bjc.rgens.parser.RGrammar;
import bjc.rgens.parser.Rule;
import bjc.rgens.parser.RuleCase;

public class RuleVarRefCaseElement extends StringCaseElement {
	public RuleVarRefCaseElement(String vl) {
		super(vl, false);
	}

	public void generate(GenerationState state) {
		if(!state.rlVars.containsKey(val)) {
			throw new GrammarException("No rule variable named " + val);
		}

		Rule rl = state.rlVars.get(val);

		GenerationState newState = state.newBuf();

		rl.generate(newState);

		String res = newState.contents.toString();

		state.contents.append(res);
	}
}
