package bjc.rgens.parser;

import bjc.utils.data.IPair;
import bjc.utils.data.Pair;

import java.util.Map;
import java.util.Random;

/* 
 * The current state during generation.
 *
 */
public class GenerationState {
	/** The current string. */
	public StringBuilder contents;
	/** The RNG. */
	public Random rnd;

	/*
	 * @NOTE
	 *
	 * Once the planned refactor for importing rules happens, this will need
	 * to be changed.
	 */
	/** The current grammar. */
	public RGrammar gram;
	/** The rules of the grammar. */
	public Map<String, Rule> rules;
	/** The rules imported from other grammars. */
	public Map<String, RGrammar> importRules;

	/*
	 * @NOTE
	 *
	 * For planned features, this will need to be refactored.
	 */
	/** The current set of variables. */
	public Map<String, String> vars;
	public Map<String, IPair<RGrammar, Rule>> rlVars;

	/**
	 * Create a new generation state.
	 * 
	 * @param cont
	 *            The string being generated.
	 *
	 * @param rand
	 *            The RNG to use.
	 *
	 * @param vs
	 *            The variables to use.
	 */
	public GenerationState(StringBuilder cont, Random rand, Map<String, String> vs,
			Map<String, IPair<RGrammar, Rule>> rvs, RGrammar gram) {
		contents = cont;
		rnd      = rand;
		vars     = vs;
		rlVars   = rvs;

		this.gram = gram;
	
		this.rules = gram.getRules();
		this.importRules = gram.getImportRules();
	}

	public void swapGrammar(RGrammar gram) {
		this.gram = gram;

		rules = gram.getRules();
		
		importRules = gram.getImportRules();
	}

	public GenerationState newBuf() {
		return new GenerationState(new StringBuilder(), rnd, vars, rlVars, gram);
	}
}
