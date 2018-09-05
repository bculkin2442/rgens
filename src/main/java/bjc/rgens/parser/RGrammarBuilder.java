package bjc.rgens.parser;

import bjc.rgens.parser.elements.CaseElement;

import bjc.utils.data.IPair;
import bjc.utils.data.Pair;
import bjc.utils.funcdata.FunctionalList;
import bjc.utils.funcdata.IList;
import bjc.utils.funcutils.ListUtils;
import bjc.utils.funcutils.SetUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import static bjc.rgens.parser.RGrammarLogging.*;

/**
 * Construct randomized grammars piece by piece.
 *
 * @author EVE
 */
public class RGrammarBuilder {
	/* The rules being built. */
	private Map<String, Rule> rules;
	/* The current set of exported rules. */
	private Set<String> exportedRules;
	/* The current initial rule. */
	private String initialRule;
	/* The current grammar name. */
	public String name;

	/** Create a new randomized grammar builder. */
	public RGrammarBuilder() {
		rules = new HashMap<>();

		exportedRules = new HashSet<>();
	}

	/**
	 * Get or create a rule by the given name.
	 *
	 * @param rName
	 * 	The name of the rule.
	 *
	 * @return
	 * 	The rule by that name, or a new one if none existed.
	 */
	public Rule getOrCreateRule(String rName) {
		if(rName == null)
			throw new NullPointerException("Rule name must not be null");
		else if(rName.equals(""))
			throw new IllegalArgumentException("The empty string is not a valid rule name");

		if(rules.containsKey(rName)) {
			return rules.get(rName);
		} else {
			Rule ret = new Rule(rName);

			rules.put(rName, ret);

			return ret;
		}
	}

	/**
	 * Convert this builder into a grammar.
	 *
	 * @return 
	 * 	The grammar built by this builder
	 */
	public RGrammar toRGrammar() {
		RGrammar grammar = new RGrammar(rules);
		grammar.name = name;

		if(initialRule != null) {
			if(!rules.containsKey(initialRule)) {
				throw new GrammarException(String.format("Rule '%s' doesn't exist", initialRule));
			}
		}

		grammar.setInitialRule(initialRule);

		for(String export : exportedRules) {
			if(!rules.containsKey(export)) {
				throw new GrammarException(String.format("Rule '%s' doesn't exist", export));
			}
		}

		grammar.setExportedRules(exportedRules);

		return grammar;
	}

	/**
	 * Set the initial rule of the grammar.
	 *
	 * @param init
	 * 	The initial rule of the grammar.
	 *
	 * @throws IllegalArgumentException
	 * 	If the rule is either not valid or not defined in the grammar.
	 */
	public void setInitialRule(String init) {
		if (init == null) {
			throw new NullPointerException("init must not be null");
		} else if (init.equals("")) {
			throw new IllegalArgumentException("The empty string is not a valid rule name");
		}

		initialRule = init;
	}

	/**
	 * Add an exported rule to this grammar.
	 *
	 * @param export
	 * 	The name of the rule to export.
	 *
	 * @throws IllegalArgumentException
	 * 	If the rule is either not valid or not defined in the grammar.
	 */
	public void addExport(String export) {
		if (export == null) {
			throw new NullPointerException("Export name must not be null");
		} else if (export.equals("")) {
			throw new NullPointerException("The empty string is not a valid rule name");
		}

		exportedRules.add(export);
	}

	/**
	 * Suffix a given case element to every case of a specific rule.
	 *
	 * @param ruleName
	 * 	The rule to suffix.
	 *
	 * @param suffix
	 * 	The suffix to add.
	 *
	 * @throws IllegalArgumentException
	 * 	If the rule name is either invalid or not defined by this
	 * 	grammar, or if the suffix is invalid.
	 */
	public void suffixWith(String ruleName, IList<CaseElement> suffixes) {
		affixWith(ruleName, suffixes, AffixType.SUFFIX);
	}


	/**
	 * Prefix a given case element to every case of a specific rule.
	 *
	 * @param ruleName
	 * 	The rule to prefix.
	 *
	 * @param prefix
	 * 	The prefix to add.
	 *
	 * @throws IllegalArgumentException
	 * 	If the rule name is either invalid or not defined by this
	 * 	grammar, or if the prefix is invalid.
	 */
	public void prefixWith(String ruleName, IList<CaseElement> prefixes) {
		affixWith(ruleName, prefixes, AffixType.PREFIX);
	}

	private static enum AffixType {
		SUFFIX,
		PREFIX;

		public boolean isSuffix() {
			return this == SUFFIX;
		}

		public boolean isPrefix() {
			return this == PREFIX;
		}
	}

	private void affixWith(String ruleName, IList<CaseElement> affixes, AffixType type) {
		if (ruleName == null) {
			throw new NullPointerException("Rule name must not be null");
		} else if (ruleName.equals("")) {
			throw new IllegalArgumentException("The empty string is not a valid rule name");
		} else if(!rules.containsKey(ruleName)) {
			String msg = String.format("Rule '%s' is not a valid rule name");

			throw new IllegalArgumentException(msg);
		}

		Set<CaseElement> elements = new HashSet<>(affixes.getSize());
		for(CaseElement affix : affixes) {
			elements.add(affix);
		}
		
		List<List<CaseElement>> affixLists = powerList(elements);

		FunctionalList<IPair<Integer, RuleCase>> newCases = new FunctionalList<>();

		IList<IPair<Integer, RuleCase>> caseList = rules.get(ruleName).getCases();
		for (IPair<Integer, RuleCase> ruleCase : caseList) {
			RuleCase cas = ruleCase.getRight();

			for(List<CaseElement> affixList : affixLists) {
				FunctionalList<CaseElement> newCase = new FunctionalList<>();

				if(type.isPrefix()) {
					for(CaseElement element : affixList) {
						newCase.add(element);
					}
				}

				for(CaseElement elm : cas.elementList) {
					newCase.add(elm);
				}
				
				if(type.isSuffix()) {
					for(CaseElement element : affixList) {
						newCase.add(element);
					}
				}


				newCases.add(new Pair<>(ruleCase.getLeft(), cas.withElements(newCase)));
			}
		}


		for (IPair<Integer, RuleCase> newCase : newCases) {
			rules.get(ruleName).addCase(newCase.getRight(), newCase.getLeft());
		}
	}

	public void despaceRule(String ruleName) {
		if (ruleName == null) {
			throw new NullPointerException("ruleName must not be null");
		} else if (ruleName.equals("")) {
			throw new IllegalArgumentException("The empty string is not a valid rule name");
		} else if (!rules.containsKey(ruleName)) {
			throw new IllegalArgumentException(String.format("The rule '%s' doesn't exist", ruleName));
		}

		IList<IPair<Integer, RuleCase>> caseList = rules.get(ruleName).getCases();

		IList<IPair<Integer, RuleCase>> newCaseList = new FunctionalList<>();

		for(IPair<Integer, RuleCase> cse : caseList) {
			newCaseList.add(new Pair<>(cse.getLeft(), new FlatRuleCase(cse.getRight().elementList)));
		}

		trace("Despacing %d cases of rule %s", caseList.getSize(), ruleName);

		rules.get(ruleName).replaceCases(newCaseList);
	}

	public void setWeight(String ruleName) {
		if (ruleName == null) {
			throw new NullPointerException("ruleName must not be null");
		} else if (ruleName.equals("")) {
			throw new IllegalArgumentException("The empty string is not a valid rule name");
		} else if (!rules.containsKey(ruleName)) {
			throw new IllegalArgumentException(String.format("The rule '%s' doesn't exist", ruleName));
		}

		rules.get(ruleName).prob = Rule.ProbType.NORMAL;
	}

	public void setRuleRecur(String ruleName, int recurLimit) {
		if (ruleName == null) {
			throw new NullPointerException("ruleName must not be null");
		} else if (ruleName.equals("")) {
			throw new IllegalArgumentException("The empty string is not a valid rule name");
		} else if (!rules.containsKey(ruleName)) {
			throw new IllegalArgumentException(String.format("The rule '%s' doesn't exist", ruleName));
		}

		rules.get(ruleName).recurLimit = recurLimit;
	}

	public void setDescent(String ruleName, int descentFactor) {
		if (ruleName == null) {
			throw new NullPointerException("ruleName must not be null");
		} else if (ruleName.equals("")) {
			throw new IllegalArgumentException("The empty string is not a valid rule name");
		} else if (!rules.containsKey(ruleName)) {
			throw new IllegalArgumentException(String.format("The rule '%s' doesn't exist", ruleName));
		}

		Rule rl = rules.get(ruleName);

		rl.prob          = Rule.ProbType.DESCENDING;
		rl.descentFactor = descentFactor;
	}

	public void setBinomial(String ruleName, int target, int bound, int trials) {
		if (ruleName == null) {
			throw new NullPointerException("ruleName must not be null");
		} else if (ruleName.equals("")) {
			throw new IllegalArgumentException("The empty string is not a valid rule name");
		} else if (!rules.containsKey(ruleName)) {
			throw new IllegalArgumentException(String.format("The rule '%s' doesn't exist", ruleName));
		}

		Rule rl = rules.get(ruleName);

		rl.prob = Rule.ProbType.BINOMIAL;

		rl.target = target;
		rl.bound  = bound;
		rl.trials = trials;
	}

	/*
	 * @TODO
	 *
	 * Figure out how this should work, and get this working.
	 */
	/*
	public void regexizeRule(String rule, String pattern) {
		if (rule == null) {
			throw new NullPointerException("rule must not be null");
		} else if(pattern == null) {
			throw new NullPointerException("pattern must not be null");	
		} else if (rule.equals("")) {
			throw new IllegalArgumentException("The empty string is not a valid rule name");
		} else if(!rules.containsKey(rule)) {
			throw new IllegalArgumentException(String.format("The rule '%s' doesn't exist", rule));
		}

		IList<RuleCase> caseList = rules.get(rule).getCases();

		IList<RuleCase> newCaseList = new FunctionalList<>();

		for(RuleCase cse : caseList) {
			newCaseList.add(new RegexRuleCase(cse.getElements(), pattern));
		}

		rules.get(rule).replaceCases(newCaseList);
	}*/

	/*
	 * @NOTE
	 *
	 * This should be moved into its own class somewhere, as it is general
	 * enough.
	 */
	private static <T> List<List<T>> powerList(Set<T> elements) {
		/*
		 * Fast-case the most common usage
		 */
		if(elements.size() == 1) {
			List<List<T>> ret = new LinkedList<>();

			List<T> curr = new ArrayList<>(elements.size());
			for(T elem : elements) {
				curr.add(elem);
			}

			ret.add(curr);

			return ret;
		}

		Set<Set<T>> powerSet = SetUtils.powerSet(elements);

		List<List<T>> list = new LinkedList<>();

		for(Set<T> set : powerSet) {
			/*
			 * Skip empty sets
			 */
			if(set.size() == 0) continue;

			List<T> stor = new ArrayList<>(set.size());

			for(T elm : set) {
				stor.add(elm);
			}

			for(List<T> permute : ListUtils.permuteList(stor)) {
				//System.err.printf("\t\tTRACE: generated permute ");
				for(T elm : permute) {
					System.err.printf("%s ", elm);
				}
				System.err.println();

				list.add(permute);
			}
		}

		return list;
	}
}
