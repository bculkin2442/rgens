package bjc.rgens.parser.elements;

import bjc.rgens.parser.GrammarException;
import bjc.rgens.parser.GenerationState;

public class VariableRuleReference extends RuleCaseElement {
	public VariableRuleReference(String vl) {
		super(vl, ReferenceType.VARIABLE);
	}

	public void generate(GenerationState state) {
		String refBody = val.substring(1, val.length() - 1);

		/* Handle string references. */
		if (refBody.equals("$")) {
			throw new GrammarException("Cannot refer to unnamed variables");
		}

		String key = refBody.substring(1);

		if (!state.vars.containsKey(key)) {
			String msg = String.format("No variable '%s' defined", key);

			throw new GrammarException(msg);
		}

		state.contents.append(state.vars.get(key));

		return;
	}
}
