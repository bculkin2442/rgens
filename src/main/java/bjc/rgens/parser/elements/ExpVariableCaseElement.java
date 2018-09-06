package bjc.rgens.parser.elements;

import bjc.utils.data.IPair;

import bjc.rgens.parser.GenerationState;
import bjc.rgens.parser.GrammarException;
import bjc.rgens.parser.RecurLimitException;
import bjc.rgens.parser.RGrammar;
import bjc.rgens.parser.Rule;
import bjc.rgens.parser.RuleCase;

public class ExpVariableCaseElement extends VariableDefCaseElement {
	public ExpVariableCaseElement(String name, String def) {
		super(name, def);
	}

	@Override
	public void generate(GenerationState state) {
		GenerationState newState = state.newBuf();

		Rule rl = state.findRule(varDef, true);

		if(rl != null) {
			RGrammar destGrammar = rl.belongsTo;
			newState.swapGrammar(destGrammar);
			/* 
			 * Don't post-process the string, we should only do that
			 * once.
			 */
			String res = destGrammar.generate(varDef, state, false);

			state.defineVar(varName, res);
		} else {
			String msg = String.format("No rule '%s' defined", varDef);
			throw new GrammarException(msg);
		}
	}
}
