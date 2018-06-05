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

		IPair<RGrammar, Rule> par = state.rlVars.get(val);

		GenerationState newState = state.newBuf();
		newState.swapGrammar(par.getLeft());

		if(par.getRight().doRecur()) {
			RuleCase cse = par.getRight().getCase(state.rnd);
			System.err.printf("\tFINE: Generating %s (from %s)\n", cse, par.getRight().name);

			par.getLeft().generateCase(cse, newState);

			par.getRight().endRecur();
		} else {
			throw new RecurLimitException("Rule recurrence limit exceeded");
		}

		String res = newState.contents.toString();

		if (par.getRight().name.contains("+")) {
			/* Rule names with pluses in them get space-flattened */
			state.contents.append(res.replaceAll("\\s+", ""));
		} else {
			state.contents.append(res);
		}
	}
}
