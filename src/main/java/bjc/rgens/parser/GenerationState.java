package bjc.rgens.parser;

import bjc.utils.esodata.MapSet;
import bjc.utils.data.IPair;
import bjc.utils.data.Pair;
import bjc.utils.ioutils.ReportWriter;

import java.io.IOException;
import java.io.StringWriter;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static bjc.rgens.parser.RGrammarLogging.*;

/**
 * The current state during generation.
 *
 * @author Ben Culkin
 */
public class GenerationState {
	/* The current output. */
	private ReportWriter contents;
	/** The RNG. */
	public Random rnd;

	/** The current grammar. */
	public RGrammar gram;

	/* The rules of the grammar. */
	private Map<String, Rule> rules;
	/* The rules imported from other grammars. */
	private Map<String, Rule> importRules;

	/** The current set of variables. */
	private MapSet<String, String> vars;
	private MapSet<String, Rule> rlVars;

	/* The base random number generator. */
	private static final Random BASE = new Random();

	/**
	 * Create a new generation state.
	 * 
	 * @param rw
	 * 	The place to write output to.
	 *
	 * @param rand
	 * 	The random number generator to use.
	 *
	 * @param vs
	 * 	The normal variables to use.
	 * 
	 * @param rvs
	 * 	The rule variables to use.
	 *
	 * @param gram
	 * 	The grammar we are generating from.
	 */
	public GenerationState(ReportWriter rw, Random rand, Map<String, String> vs,
			Map<String, Rule> rvs, RGrammar gram) {
		vars   = new MapSet<>();
		rlVars = new MapSet<>();

		contents = rw;
		rnd      = rand;

		vars.setPutMap(gram.name, vs);
		rlVars.setPutMap(gram.name, rvs);

		this.gram = gram;
	
		this.rules = gram.getRules();
		this.importRules = gram.getImportRules();
	}

	/**
	 * Create a new generation state, from a given grammar.
	 *
	 * @param gram
	 * 	The grammar to generate from.
	 *
	 * @return
	 * 	A new generation state, with the provided parameters.
	 */
	public static GenerationState fromGrammar(RGrammar gram) {
		return fromGrammar(BASE, gram);
	}

	/**
	 * Create a new generation state, using a provided random generator and
	 * grammar.
	 *
	 * @param rand
	 * 	The random number generator to use.
	 *
	 * @param gram
	 * 	The grammar to generate from.
	 *
	 * @return
	 * 	A new generation state, with the provided parameters.
	 */
	public static GenerationState fromGrammar(Random rand, RGrammar gram) {
		ReportWriter rw = new ReportWriter(new StringWriter());

		return new GenerationState(rw, rand, new HashMap<>(), new HashMap<>(), gram);
	}

	/**
	 * Swap the grammar for the state.
	 *
	 * @param gram
	 * 	The grammar to swap to.
	 */
	public void swapGrammar(RGrammar gram) {
		if(this.gram == gram) return;

		this.gram = gram;

		rules = gram.getRules();
		
		importRules = gram.getImportRules();

		vars.setCreateMap(gram.name);
		rlVars.setCreateMap(gram.name);
	}

	/**
	 * Create a copy of this generation state, writing into a fresh buffer.
	 *
	 * @return A generation state that is a copy of this one, but writes into a
	 * fresh buffer.
	 */
	public GenerationState newBuf() {
		// @NOTE 9/5/18
		//
		// Not sure if this is the right thing to do or not.
		//
		// I suppose it'll only matter once we actually start using the
		// features of ReportWriter, instead of just using the basic
		// Writer functionality
		ReportWriter rw = contents.duplicate(new StringWriter());

		return new GenerationState(rw, rnd, vars, rlVars, gram);
	}

	/*
	 * @TODO 6/5/18 Ben Culkin :ImportRefactor
	 * 
	 * Change this so that imports in almost all cases have to specify where
	 * they are importing the rule from, so as to make it clear which rules
	 * are imported, and which aren't
	 */
	/**
	 * Find an instance of a rule.
	 *
	 * @param ruleName
	 * 	The name of the rule to look for.
	 *
	 * @param allowImports
	 * 	Whether or not to look for imported rules.
	 *
	 * @return The rule instance you were looking for, or null if none by that
	 * name happen to exist.
	 */
	public Rule findRule(String ruleName, boolean allowImports) {
		if(rules.containsKey(ruleName)) {
			return rules.get(ruleName);
		}

		if(allowImports) return findImport(ruleName);

		return null;
	}

	/**
	 * Find an instance of an imported rule.
	 *
	 * @param ruleName
	 * 	The name of the rule to look for.
	 *
	 * @return The rule instance you were looking for, or null if none by that
	 * name happen to exist.
	 */
	public Rule findImport(String ruleName) {
		if(importRules.containsKey(ruleName)) {
			return  importRules.get(ruleName);
		}

		return null;
	}

	/**
	 * Define a normal variable.
	 *
	 * @param name
	 * 	The name to give the variable.
	 *
	 * @param val
	 * 	The value to give the variable.
	 */
	public void defineVar(String name, String val) {
		if(vars.containsKey(name)) 
			warn("Shadowing variable %s with value %s (old value %s)",
					name, val, vars.get(name));
		else if (gram.autoVars.containsKey(name))
			warn("Shadowing autovariable %s with value %s (defn. %s)",
					name, val, gram.autoVars.get(name));

		vars.put(name, val);
	}

	/**
	 * Define a rule variable.
	 *
	 * @param name
	 * 	The name to give the variable.
	 *
	 * @param rle
	 * 	The value to give the variable.
	 */
	public void defineRuleVar(String name, Rule rle) {
		if(rlVars.containsKey(name))
			warn("Shadowing rule variable %s with value %s (old value %s)",
					name, rlVars.get(name), rle);
		else if (gram.autoRlVars.containsKey(name))
			warn("Shadowing rule autovariable %s with value %s (defn. %s)",
					name, rle, gram.autoRlVars.get(name));

		rlVars.put(name, rle);
	}

	/**
	 * Find a variable.
	 *
	 * @param name
	 * 	The variable to look for.
	 *
	 * @return The value of the variable.
	 *
	 * @throws GrammarException If the variable isn't found, or if it was an
	 * auto-variable that failed to generate succesfully.
	 */
	public String findVar(String name) {
		if(!vars.containsKey(name)) 
			if(gram.autoVars.containsKey(name)) {
				gram.autoVars.get(name).generate(this);
			} else {
				throw new GrammarException(String.format("Variable %s not defined", name));
			}

		return vars.get(name);
	}

	/**
	 * Find a rule variable.
	 *
	 * @param name
	 * 	The variable to look for.
	 *
	 * @return The value of the variable.
	 *
	 * @throws GrammarException If the variable isn't found, or if it was an
	 * auto-variable that failed to generate succesfully.
	 */
	public Rule findRuleVar(String name) {
		if(!rlVars.containsKey(name))
			if(gram.autoRlVars.containsKey(name)) {
				gram.autoRlVars.get(name).generate(this);
			} else {
				throw new GrammarException(String.format("Rule variable %s not defined", name));
			}

		return rlVars.get(name);
	}

	/**
	 * Append the given string to our output.
	 */
	public void appendContents(String strang) {
		try {
			contents.write(strang);
		} catch (IOException ioex) {
			throw new GrammarException("I/O Error", ioex);
		}
	}

	/**
	 * Replace the current contents of our output with the given string.
	 *
	 * @param strang
	 * 	The string to replace the output with.
	 */
	public void setContents(String strang) {
		// @NOTE 9/5/18
		//
		// This raises some interesting questions as to what the
		// appropriate behavior is.
		//
		// For now, I'm simply going to say to go with a StringWriter
		// and then write the contents to that, but I am not sure that
		// that is the right way to go about it.
		contents = contents.duplicate(new StringWriter());

		try {
			contents.write(strang);
		} catch (IOException ioex) {
			throw new GrammarException("I/O Error", ioex);
		}
	}

	/**
	 * Get the report writer that we use for our input.
	 *
	 * @return The report writer that we write stuff &amp; things to.
	 */
	public ReportWriter getWriter() {
		return contents;
	}

	/**
	 * Get our output as a string.
	 */
	public String getContents() {
		return contents.toString();
	}

	/**
	 * Execute a find/replace on our output.
	 *
	 * @param find
	 * 	The pattern to look for
	 *
	 * @param replace
	 * 	The string to replace occurances of 'find' with.
	 */
	public void findReplaceContents(String find, String replace) {
		setContents(getContents().replaceAll(find, replace));
	}

	/**
	 * Clear out our contents.
	 */
	public void clearContents() {
		contents = contents.duplicate(new StringWriter());
	}
}
