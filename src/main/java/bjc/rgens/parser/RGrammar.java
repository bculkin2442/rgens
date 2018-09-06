package bjc.rgens.parser;

import bjc.utils.data.IPair;
import bjc.utils.data.Pair;
import bjc.utils.funcutils.StringUtils;

import bjc.rgens.parser.elements.*;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.text.similarity.LevenshteinDistance;

import edu.gatech.gtri.bktree.BkTreeSearcher;
import edu.gatech.gtri.bktree.BkTreeSearcher.Match;
import edu.gatech.gtri.bktree.Metric;
import edu.gatech.gtri.bktree.MutableBkTree;

import static bjc.utils.data.IPair.pair;

/**
 * Represents a randomized grammar.
 *
 * @author EVE
 */
public class RGrammar {
	public RGrammarSet belongsTo;

	public String name;

	public List<IPair<String, String>> postprocs;

	private static final List<IPair<String, String>> builtinPostprocs;
	public boolean useBuiltinPostprocs = true;

	/* The max distance between possible alternate rules. */
	private static final int MAX_DISTANCE = 6;

	/* The metric for the levenshtein distance. */
	private static final class LevenshteinMetric implements Metric<String> {
		private static LevenshteinDistance DIST;

		static {
			DIST = LevenshteinDistance.getDefaultInstance();
		}

		public LevenshteinMetric() {
		}

		@Override
		public int distance(String x, String y) {
			return DIST.apply(x, y);
		}
	}

	/* The rules of the grammar. */
	public Map<String, Rule> rules;
	/* The rules imported from other grammars. */
	private Map<String, Rule> importRules;
	/* The rules exported from this grammar. */
	private Set<String> exportRules;

	/* The initial rule of this grammar. */
	private String initialRule;

	public Map<String, CaseElement> autoVars;
	public Map<String, CaseElement> autoRlVars;

	/* The tree to use for finding rule suggestions. */
	private BkTreeSearcher<String> ruleSearcher;

	static {
		/* Collapse duplicate spaces */
		IPair<String, String> collapseDupSpaces = pair("\\s+", " ");

		/* Built-in post-processing steps */
		builtinPostprocs = Arrays.asList(
				collapseDupSpaces,

				/* 
				 * Remove extraneous spaces around punctuation
				 * marks, forced by the way the language syntax
				 * works.
				 *
				 * This can be done in grammars, but it is quite
				 * tedious to do so.
				 */


				/* Handle 's */
				pair(" 's ", "'s "),
				/* Handle opening/closing punctuation. */
				pair("([(\\[]) ", " $1"),
				pair(" ([)\\]'\"])", "$1 "),
				/* Remove spaces around series of opening/closing punctuation. */
				pair("([(\\[])\\s+([(\\[])", "$1$2"),
				pair("([)\\]])\\s+([)\\]])", "$1$2"),
				/* Handle inter-word punctuation. */
				pair(" ([,:.!])", "$1 "),
				/* Handle intra-word punctuation. */
				pair("\\s?([-/])\\s?", "$1"),

				collapseDupSpaces,

				/* Replace this once it is no longer needed. */
				pair("\\s(ish|burg|ton|ville|opolis|field|boro|dale)", "$1")
				);
	}
	/**
	 * Create a new randomized grammar using the specified set of rules.
	 *
	 * @param ruls
	 *            The rules to use.
	 */
	public RGrammar(Map<String, Rule> ruls) {
		rules = ruls;

		for(Rule rl : ruls.values()) {
			rl.belongsTo = this;
		}

		postprocs = new ArrayList<>();
	}

	/**
	 * Sets the imported rules to use.
	 *
	 * Imported rules are checked for rule definitions after local definitions are
	 * checked.
	 *
	 * @param importedRules
	 *            The set of imported rules to use.
	 */
	public void setImportedRules(Map<String, Rule> importedRules) {
		importRules = importedRules;
	}

	/**
	 * Generates the data structure backing rule suggestions for unknown rules.
	 */
	public void generateSuggestions() {
		MutableBkTree<String> ruleSuggester = new MutableBkTree<>(new LevenshteinMetric());

		ruleSuggester.addAll(rules.keySet());
		ruleSuggester.addAll(importRules.keySet());

		ruleSearcher = new BkTreeSearcher<>(ruleSuggester);
	}

	/**
	 * Generate a string from this grammar, starting from the specified rule.
	 *
	 * @param startRule
	 *            The rule to start generating at, or null to use the initial rule
	 *            for this grammar.
	 *
	 * @return A possible string from the grammar.
	 */
	public String generate(String startRule) {
		return generate(startRule, new Random(), new HashMap<>(), new HashMap<>());
	}

	/**
	 * Generate a string from this grammar, starting from the specified rule.
	 *
	 * @param startRule
	 *            The rule to start generating at, or null to use the initial rule
	 *            for this grammar.
	 *
	 * @param rnd
	 *            The random number generator to use.
	 *
	 * @param vars
	 *            The set of variables to use.
	 *
	 * @return A possible string from the grammar.
	 */
	public String generate(String startRule, Random rnd, Map<String, String> vars,
			Map<String, Rule> rlVars) {
		return generate(startRule, new GenerationState(new StringBuilder(), rnd, vars, rlVars, this));
	}

	/**
	 * Generate a string from this grammar, starting from the specified rule.
	 *
	 * @param startRule
	 *            The rule to start generating at, or null to use the initial rule
	 *            for this grammar.
	 *
	 * @param state
	 * 	The generation state.
	 */
	public String generate(String startRule, GenerationState state) {
		return generate(startRule, state, true);
	}

	public String generate(String startRule, GenerationState state, boolean doPostprocess) {
		String fromRule = startRule;

		if (startRule == null) {
			if (initialRule == null) {
				throw new GrammarException("Must specify a start rule for grammars with no initial rule");
			}

			fromRule = initialRule;
		} else {
			if (startRule.equals("")) {
				throw new GrammarException("The empty string is not a valid rule name");
			}
		}

		/* 
		 * We don't search imports, so it will always belong to this
		 * grammar.
		 */
		Rule rl = state.findRule(fromRule, false);

		if(rl == null)
			throw new GrammarException("Could not find rule " + rl.name);

		rl.generate(state);

		String body = state.getContents();

		if(doPostprocess) {
			body = postprocessRes(body);
		}

		return body;
	}

	private String postprocessRes(String strang) {
		String body = strang;

		if(useBuiltinPostprocs) {
			for(IPair<String, String> par : builtinPostprocs) {
				body = body.replaceAll(par.getLeft(), par.getRight());
			}
		}

		for(IPair<String, String> par : postprocs) {
			body = body.replaceAll(par.getLeft(), par.getRight());
		}

		return body.trim();
	}
	/**
	 * Generate a rule case.
	 *
	 * @param start
	 * 	The rule case to generate.
	 * @param state
	 * 	The current generation state.
	 */
	public void generateCase(RuleCase start, GenerationState state) {
		try {
			start.generate(state);
		} catch (GrammarException gex) {
			String msg = String.format("Error in generating case (%s)", start);
			throw new GrammarException(msg, gex, gex.getRootMessage());
		}
	}

	/**
	 * Get the initial rule of this grammar.
	 *
	 * @return The initial rule of this grammar.
	 */
	public String getInitialRule() {
		return initialRule;
	}

	/**
	 * Set the initial rule of this grammar.
	 *
	 * @param initRule
	 *            The initial rule of this grammar, or null to say there is no
	 *            initial rule.
	 */
	public void setInitialRule(String initRule) {
		/* Passing null, nulls our initial rule. */
		if (initRule == null) {
			this.initialRule = null;
			return;
		}

		if (initRule.equals("")) {
			throw new GrammarException("The empty string is not a valid rule name");
		} else if (!rules.containsKey(initRule)) {
			String msg = String.format("No rule '%s' local to this grammar (%s) defined.", initRule, name);

			throw new GrammarException(msg);
		}

		initialRule = initRule;
	}

	/**
	 * Gets the rules exported by this grammar.
	 *
	 * The initial rule is exported by default if specified.
	 *
	 * @return The rules exported by this grammar.
	 */
	public Set<Rule> getExportedRules() {
		Set<Rule> res = new HashSet<>();

		for (String rname : exportRules) {
			if (!rules.containsKey(rname)) {
				String msg = String.format("No rule '%s' local to this grammar (%s) defined for export",
						name, rname);

				throw new GrammarException(msg);
			}

			res.add(rules.get(rname));
		}

		if (initialRule != null) {
			res.add(rules.get(initialRule));
		}

		return res;
	}

	/**
	 * Set the rules exported by this grammar.
	 *
	 * @param exportedRules
	 *            The rules exported by this grammar.
	 */
	public void setExportedRules(Set<String> exportedRules) {
		exportRules = exportedRules;
	}

	/**
	 * Get all the rules in this grammar.
	 *
	 * @return All the rules in this grammar.
	 */
	public Map<String, Rule> getRules() {
		return rules;
	}

	public Map<String, Rule> getImportRules() {
		return importRules;
	}

	public void setAutoVars(Map<String, CaseElement> aVars, Map<String, CaseElement> aRlVars) {
		autoVars   = aVars;
		autoRlVars = aRlVars;
	}
}
