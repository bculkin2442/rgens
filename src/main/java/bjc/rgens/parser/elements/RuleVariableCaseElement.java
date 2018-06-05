package bjc.rgens.parser.elements;

import bjc.utils.data.IPair;
import bjc.utils.data.Pair;

import bjc.rgens.parser.GrammarException;
import bjc.rgens.parser.GenerationState;
import bjc.rgens.parser.Rule;
import bjc.rgens.parser.RGrammar;

public class RuleVariableCaseElement extends VariableCaseElement {
	public final boolean exhaust;

	public RuleVariableCaseElement(String varName, String varDef, boolean exhaust) {
		super(varName, varDef, VariableType.RULE);

		this.exhaust = exhaust;
	}

	public void generate(GenerationState state) {
		Rule rl;
		RGrammar grm;

		if(state.rules.containsKey(varDef)) {
			rl  = state.rules.get(varDef);
			grm = state.gram;
		} else if(state.importRules.containsKey(varDef)) {
			grm = state.importRules.get(varDef);
			rl  = grm.getRules().get(varDef);
		} else {
			throw new GrammarException("Can't create variable referencing non-existent rule " + varDef);
		}


		if(exhaust) rl = rl.exhaust();

		if(state.rlVars.containsKey(varName)) {
			IPair<RGrammar, Rule> par = state.rlVars.get(varName);

			System.err.printf("WARN: Shadowing rule variable '%s' (%s with %s)\n",
					varName, par.getRight().name, rl.name);
		}

		state.rlVars.put(varName, new Pair<>(grm, rl));

		if(exhaust) {
			System.err.printf("\t\tTRACE: Defined exhausted rulevar '%s' ('%s')\n", varName, varDef);
		} else {
			System.err.printf("\t\tTRACE: Defined rulevar '%s' ('%s')\n", varName, varDef);
		}
	}
}
