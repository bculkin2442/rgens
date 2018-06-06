package bjc.rgens.parser.elements;

import bjc.utils.data.IPair;

import bjc.rgens.parser.GenerationState;
import bjc.rgens.parser.GrammarException;
import bjc.rgens.parser.RecurLimitException;
import bjc.rgens.parser.RGrammar;
import bjc.rgens.parser.Rule;
import bjc.rgens.parser.RuleCase;

public class ExpVariableCaseElement extends VariableCaseElement {
	public ExpVariableCaseElement(String name, String def) {
		super(name, def, VariableType.EXPAND);
	}

	@Override
	public void generate(GenerationState state) {
		GenerationState newState = state.newBuf();

		Rule rl = state.findRule(varDef, true);

		if(rl != null) {
			RGrammar destGrammar = rl.belongsTo;
			newState.swapGrammar(destGrammar);
			String res = destGrammar.generate(varDef, state);

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
			String msg = String.format("No rule '%s' defined", varDef);
			throw new GrammarException(msg);
		}

		state.vars.put(varName, newState.contents.toString());
	}
}
