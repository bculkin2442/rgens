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
		IPair<RGrammar, Rule> par = state.findRule(varDef, true);

		if(par == null) {
			throw new GrammarException("Can't create variable referencing non-existent rule " + varDef);
		}
		
		if(exhaust) {
			par = new Pair<>(par.getLeft(), par.getRight().exhaust());
		}

		state.rlVars.put(varName, par);

		if(exhaust) {
			System.err.printf("\t\tFINE: Defined exhausted rulevar '%s' ('%s')\n", varName, varDef);
		} else {
			System.err.printf("\t\tFINE: Defined rulevar '%s' ('%s')\n", varName, varDef);
		}
	}
}
