package bjc.rgens.parser.elements;

import bjc.utils.data.IPair;
import bjc.utils.data.Pair;

import bjc.rgens.parser.GrammarException;
import bjc.rgens.parser.GenerationState;
import bjc.rgens.parser.Rule;
import bjc.rgens.parser.RGrammar;

import static bjc.rgens.parser.RGrammarLogging.*;

public class RuleVariableCaseElement extends VariableDefCaseElement {
	public final boolean exhaust;

	public RuleVariableCaseElement(String varName, String varDef, boolean exhaust) {
		super(varName, varDef);

		this.exhaust = exhaust;
	}

	public void generate(GenerationState state) {
		Rule rl = state.findRule(varDef, true);

		if(rl == null) {
			throw new GrammarException("Can't create variable referencing non-existent rule " + varDef);
		}
		
		if(exhaust) {
			rl = rl.exhaust();
		}

		state.rlVars.put(varName, rl);

		if(exhaust) {
			fine("Defined exhausted rulevar '%s' ('%s')", varName, varDef);
		} else {
			fine("Defined rulevar '%s' ('%s')", varName, varDef);
		}
	}
}
