package bjc.rgens.parser.elements;

import static bjc.rgens.parser.RGrammarLogging.fine;

import bjc.rgens.parser.GenerationState;
import bjc.rgens.parser.GrammarException;
import bjc.rgens.parser.Rule;

/**
 * Case element which creates a rule variable.
 * 
 * @author Ben Culkin
 *
 */
public class RuleVariableCaseElement extends VariableDefCaseElement {
	/**
	 * Whether or not this is an exhaustive variable.
	 */
	public final boolean exhaust;

	/**
	 * Create a case element which creates a rule variable.
	 * 
	 * @param varName The name of the variable.
	 * @param varDef The definition of the variable.
	 * @param exhaust Whether or not this is an exhaustive variable.
	 */
	public RuleVariableCaseElement(String varName, String varDef, boolean exhaust) {
		super(varName, varDef);

		this.exhaust = exhaust;
	}

	@Override
	public void generate(GenerationState state) {
		Rule rl = state.findRule(varDef, true);

		if(rl == null) {
			throw new GrammarException("Can't create variable referencing non-existent rule " + varDef);
		}
		
		if(exhaust) {
			rl = rl.exhaust();
		}

		state.defineRuleVar(varName, rl);

		if(exhaust) {
			fine("Defined exhausted rulevar '%s' ('%s')", varName, varDef);
		} else {
			fine("Defined rulevar '%s' ('%s')", varName, varDef);
		}
	}
}
