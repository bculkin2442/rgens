package bjc.rgens.parser;

import bjc.utils.data.IPair;
import bjc.utils.funcdata.FunctionalList;
import bjc.utils.funcdata.IList;
import bjc.utils.gen.WeightedRandom;

import java.util.Random;

/**
 * A rule in a randomized grammar.
 *
 * @author EVE
 */
public class Rule {
	/** The name of this grammar rule. */
	public String name;

	/* The cases for this rule. */
	private WeightedRandom<RuleCase> cases;

	public static enum ProbType {
		NORMAL,
		DESCENDING,
		BINOMIAL
	}
	
	public ProbType prob;

	public int     descentFactor;

	public int target;
	public int bound;
	public int trials;

	public int recurLimit = 5;
	private int currentRecur;

	private final static Random BASE = new Random();

	private int serial = 1;
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
		if (ruleName == null) {
			throw new NullPointerException("Rule name must not be null");
		} else if (ruleName.equals("")) {
			throw new IllegalArgumentException("The empty string is not a valid rule name");
		}

		name = ruleName;

		cases = new WeightedRandom<>();

		prob = ProbType.NORMAL;
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
	 */
	public void addCase(RuleCase cse, int weight) {
		if (cse == null) {
			throw new NullPointerException("Case must not be null");
		}

		cse.belongsTo = name;
		cse.debugName = String.format("%s-%d", name, serial);
		serial += 1;

		cases.addProbability(weight, cse);
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
	public IList<IPair<Integer, RuleCase>> getCases() {
		return cases.getValues();
	}

	/**
	 * Replace the current list of cases with a new one.
	 *
	 * @param cases
	 * 	The new list of cases.
	 */
	public void replaceCases(IList<IPair<Integer, RuleCase>> cases) {
		this.cases = new WeightedRandom<>();

		for(IPair<Integer, RuleCase> cse : cases) {
			RuleCase cs = cse.getRight();
			cs.belongsTo = name;
			cs.debugName = String.format("%s-%d", name, serial);
			serial += 1;

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
		return String.format("Rule [ruleName='%s', ruleCases=%s]", name, cases);
	}

	public boolean doRecur() {
		if(currentRecur > recurLimit) return false;

		currentRecur += 1;

		return true;
	}

	public void endRecur() {
		if(currentRecur > 0) currentRecur -= 1;
	}
}
