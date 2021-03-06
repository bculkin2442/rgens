package bjc.rgens.parser;

import static bjc.rgens.parser.RGrammarLogging.fine;
import static bjc.data.Pair.pair;

import bjc.data.Pair;
import bjc.data.Tree;
import bjc.data.SimpleTree;
import bjc.funcdata.ListEx;
import bjc.utils.gen.WeightedRandom;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * A rule in a randomized grammar.
 *
 * @author EVE
 */
public class Rule {
	/**
	 * The grammar this rule belongs to.
	 */
	public RGrammar belongsTo;

	/** The name of this grammar rule. */
	public String name;

	/* The cases for this rule. */
	private WeightedRandom<RuleCase> cases;

	/*
	 * @NOTE
	 *
	 * Perhaps this should be split into subclasses along prob type? I'm not
	 * sure as to whether or not that would be a useful thing to do.
	 */
	/**
	 * Type of probability to use for this rule.
	 * 
	 * @author Ben Culkin
	 *
	 */
	public static enum ProbType {
		/**
		 * Normal-type probability.
		 */
		NORMAL,
		/**
		 * Descent-type probability.
		 */
		DESCENDING,
		/**
		 * Binomial-type probability.
		 */
		BINOMIAL
	}
	
	/**
	 * Type of probability to use for this rule.
	 */
	public ProbType prob;

	// Probability vars
	/* Descent vars */
	/**
	 * Factor for probability to descend by.
	 */
	public int descentFactor;
	
	/* Binomial vars */
	/**
	 * Target for the binomial probability.
	 */
	public int target;
	/**
	 * Bound for the binomial probability.
	 */
	public int bound;
	/**
	 * Trials for the binomial probability.
	 */
	public int trials;

	private List<String> rejectionPreds;
	private List<Pair<String, String>> findReplaces;

	// @TODO This default should be configurable in some way
	/**
	 * Number of times this rule can recur.
	 */
	public int recurLimit = 5;
	private int currentRecur;

	private final static Random BASE = new Random();

	private int caseCount = 0;

	@SuppressWarnings("unused")
	private int serial;
	private static int nextSerial = 0;

	/**
	 * Create a new grammar rule.
	 *
	 * @param ruleName
	 * 	The name of the grammar rule.
	 *
	 * @throws IllegalArgumentException
	 * 	If the rule name is invalid.
	 */
	public Rule(String ruleName) {
		serial = ++nextSerial;

		if (ruleName == null) {
			throw new NullPointerException("Rule name must not be null");
		} else if (ruleName.equals("")) {
			throw new IllegalArgumentException("The empty string is not a valid rule name");
		}

		name = ruleName;

		cases = new WeightedRandom<>();

		prob = ProbType.NORMAL;

		rejectionPreds = new ArrayList<>();
		findReplaces = new ArrayList<>();
	}

	/**
	 * Adds a case to the rule.
	 *
	 * @param cse
	 * 	The case to add.
	 */
	public void addCase(RuleCase cse) {
		addCase(cse, 1);
	}

	/**
	 * Adds a case to the rule.
	 *
	 * @param cse
	 * 	The case to add.
	 * @param weight The weight for this case.
	 */
	public void addCase(RuleCase cse, int weight) {
		if (cse == null) {
			throw new NullPointerException("Case must not be null");
		}

		cse.belongsTo = this;
		cse.debugName = String.format("%s-%d", name, ++caseCount);

		cases.addProbability(weight, cse);
	}

	/**
	 * Add a rejection pattern to this rule.
	 * 
	 * @param reject The rejection pattern.
	 */
	public void addRejection(String reject) {
		addRejection(reject, new SimpleTree<>());
	}

	/**
	 * Add a rejection pattern to this rule.
	 * 
	 * @param reject The rejection pattern.
	 * @param errs The place to put errors.
	 */
	public void addRejection(String reject, Tree<String> errs) {
		try {
			Pattern.compile(reject);
		} catch (PatternSyntaxException psex) {
		//	String msg = String.format("ERROR: '%s' is not a valid regex for rejection (%s)", reject, psex.getMessage());
		}

		rejectionPreds.add(reject);
	}

	/**
	 * Add a find/replace pattern to this rule.
	 * 
	 * @param find The find string.
	 * @param replace The replace string.
	 */
	public void addFindReplace(String find, String replace) {
		addFindReplace(find, replace, new SimpleTree<>());
	}

	/**
	 * Add a find/replace pattern to this rule.
	 * 
	 * @param find The find string.
	 * @param replace The replace string.
	 * @param errs The place to put errors.
	 */
	public void addFindReplace(String find, String replace, Tree<String> errs) {
		try {
			Pattern.compile(find);
		} catch (PatternSyntaxException psex) {
			String msg = String.format("ERROR: '%s' is not a valid regex for finding (%s)", find, psex.getMessage());

			errs.addChild(msg);

			return;
		}

		findReplaces.add(pair(find, replace));
	}

	/**
	 * Get a random case from this rule.
	 *
	 * @return
	 * 	A random case from this rule.
	 */
	public RuleCase getCase() {
		return getCase(BASE);
	}

	/**
	 * Get a random case from this rule.
	 *
	 * @param rnd
	 * 	The random number generator to use.
	 *
	 * @return
	 * 	A random case from this rule.
	 */
	public RuleCase getCase(Random rnd) {
		switch(prob) {
		case DESCENDING:
			return cases.getDescent(descentFactor, rnd);
		case BINOMIAL:
			return cases.getBinomial(target, bound, trials, rnd);
		case NORMAL:
			return cases.generateValue(rnd);
		default:
			return cases.generateValue(rnd);
		}
	}

	/**
	 * Get all the cases of this rule.
	 *
	 * @return
	 * 	All the cases in this rule.
	 */
	public ListEx<Pair<Integer, RuleCase>> getCases() {
		return cases.getValues();
	}

	/**
	 * Replace the current list of cases with a new one.
	 *
	 * @param caseList
	 * 	The new list of cases.
	 */
	public void replaceCases(ListEx<Pair<Integer, RuleCase>> caseList) {
		this.cases = new WeightedRandom<>();

		for(Pair<Integer, RuleCase> cse : caseList) {
			RuleCase cs = cse.getRight();
			cs.belongsTo = this;
			cs.debugName = String.format("%s-%d", name, ++caseCount);

			this.cases.addProbability(cse.getLeft(), cs);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;

		int result = 1;
		result = prime * result + ((cases == null) ? 0 : cases.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());

		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;

		if (obj == null) return false;

		if (!(obj instanceof Rule)) return false;

		Rule other = (Rule) obj;

		if (cases == null) {
			if (other.cases != null) return false;
		} else if (!cases.equals(other.cases)) return false;

		if (name == null) {
			if (other.name != null) return false;
		} else if (!name.equals(other.name)) return false;

		return true;
	}

	@Override
	public String toString() {
		return String.format("Rule '%s' with %d cases", name, cases.getValues().getSize());
	}

	/**
	 * Start recurring on this rule.
	 * 
	 * @return Whether the recurrence rule has been exceeded.
	 */
	public boolean doRecur() {
		if(currentRecur > recurLimit) return false;

		currentRecur += 1;

		return true;
	}

	/**
	 * End recurring on this rule.
	 */
	public void endRecur() {
		if(currentRecur > 0) {
			currentRecur -= 1;
		} else {
			throw new IllegalStateException("endRecur without matching doRecur");
		}
	}

	/**
	 * Get an exhaustive version of this rule.
	 * 
	 * @return An exhaustive version of this rule.
	 */
	public Rule exhaust() {
		Rule rl = new Rule(name);

		rl.belongsTo = belongsTo;

		rl.cases = cases.exhaustible();

		rl.prob = prob;

		rl.descentFactor = descentFactor;

		rl.target = target;
		rl.bound  = bound;
		rl.trials = trials;

		rl.recurLimit = recurLimit;
		/* @NOTE 
		 *
		 * Is this the right thing to do?
		 *
		 * At least for now it is. I can't think of any intentional
		 * situations where this would cause issues, but it'll be kept
		 * in mind -- 8/14/18
		 */
		rl.currentRecur = 0;

		return rl;
	}

	/**
	 * Generate this rule.
	 * 
	 * @param state The generation state to use.
	 */
	public void generate(GenerationState state) {
		state.swapGrammar(belongsTo);

		boolean rejected;

		do {
			rejected = false;

			if(doRecur()) {
				RuleCase cse = getCase(state.rnd);

				fine("Generating %s (from %s)", cse, belongsTo.name);

				belongsTo.generateCase(cse, state);

				endRecur();
			}

			// Don't rebuild the builder a bunch
			String conts = state.getContents();
			if(name.contains("+")) {
				conts = conts.replaceAll("\\s+", "");
			}

			for(Pair<String, String> findRep : findReplaces) {
				conts = conts.replaceAll(findRep.getLeft(), findRep.getRight());
			}
			state.setContents(conts);

			for(String pat : rejectionPreds) {
				if(!conts.matches(pat)) {
					fine("Rejected %s by %s (from %s)", conts, pat, belongsTo.name);

					rejected = true;
					state.clearContents();

					break;
				}
			}
		} while (rejected);
	}
}
