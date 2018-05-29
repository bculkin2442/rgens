package bjc.rgens.parser.elements;

import bjc.rgens.parser.GenerationState;
import bjc.rgens.parser.GrammarException;
import bjc.rgens.parser.RGrammar;
import bjc.rgens.parser.RuleCase;

public class ExpVariableCaseElement extends VariableCaseElement {
	public ExpVariableCaseElement(String name, String def) {
		super(name, def, true);
	}

	@Override
	public void generate(GenerationState state) {
		GenerationState newState = state.newBuf();

		if (state.rules.containsKey(varDef)) {
			RuleCase destCase = state.rules.get(varDef).getCase();

			state.gram.generateCase(destCase, newState);
		} else if (state.importRules.containsKey(varDef)) {
			RGrammar destGrammar = state.importRules.get(varDef);

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
