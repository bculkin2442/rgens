package bjc.rgens.parser;

import bjc.utils.esodata.MapSet;
import bjc.utils.data.IPair;
import bjc.utils.data.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static bjc.rgens.parser.RGrammarLogging.*;
/* 
 * The current state during generation.
 *
 */
public class GenerationState {
	/** The current string. */
	public StringBuilder contents;
	/** The RNG. */
	public Random rnd;

	/** The current grammar. */
	public RGrammar gram;
	/** The rules of the grammar. */
	private Map<String, Rule> rules;
	/** The rules imported from other grammars. */
	private Map<String, Rule> importRules;

	/** The current set of variables. */
	private MapSet<String, String> vars;
	private MapSet<String, Rule> rlVars;

	private static final Random BASE = new Random();

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
			Map<String, Rule> rvs, RGrammar gram) {
		vars   = new MapSet<>();
		rlVars = new MapSet<>();

		contents = cont;
		rnd      = rand;

		vars.setPutMap(gram.name, vs);
		rlVars.setPutMap(gram.name, rvs);

		this.gram = gram;
	
		this.rules = gram.getRules();
		this.importRules = gram.getImportRules();
	}

	public static GenerationState fromGrammar(RGrammar gram) {
		return fromGrammar(BASE, gram);
	}

	public static GenerationState fromGrammar(Random rand, RGrammar gram) {
		return new GenerationState(new StringBuilder(), rand, new HashMap<>(), new HashMap<>(), gram);
	}

	public void swapGrammar(RGrammar gram) {
		if(this.gram == gram) return;

		this.gram = gram;

		rules = gram.getRules();
		
		importRules = gram.getImportRules();

		vars.setCreateMap(gram.name);
		rlVars.setCreateMap(gram.name);
	}

	public GenerationState newBuf() {
		return new GenerationState(new StringBuilder(), rnd, vars, rlVars, gram);
	}

	/*
	 * @TODO 6/5/18 Ben Culkin :ImportRefactor
	 * 
	 * Change this so that imports in almost all cases have to specify where
	 * they are importing the rule from, so as to make it clear which rules
	 * are imported, and which aren't
	 */
	public Rule findRule(String ruleName, boolean allowImports) {
		if(rules.containsKey(ruleName)) {
			return rules.get(ruleName);
		}

		if(allowImports) return findImport(ruleName);

		return null;
	}

	public Rule findImport(String ruleName) {
		if(importRules.containsKey(ruleName)) {
			return  importRules.get(ruleName);
		}

		return null;
	}

	public void defineVar(String name, String val) {
		if(vars.containsKey(name)) 
			warn("Shadowing variable %s with value %s (old value %s)", name, vars.get(name), val);

		vars.put(name, val);
	}

	public void defineRuleVar(String name, Rule rle) {
		if(rlVars.containsKey(name))
			warn("Shadowing rule variable %s with value %s (old value %s)", name, rlVars.get(name), rle);

		rlVars.put(name, rle);
	}

	public String findVar(String name, GenerationState stat) {
		if(!vars.containsKey(name)) 
			throw new GrammarException(String.format("Variable %s not defined", name));

		return vars.get(name);
	}

	public Rule findRuleVar(String name, GenerationState stat) {
		if(!rlVars.containsKey(name))
			throw new GrammarException(String.format("Rule variable %s not defined", name));

		return rlVars.get(name);
	}
}
