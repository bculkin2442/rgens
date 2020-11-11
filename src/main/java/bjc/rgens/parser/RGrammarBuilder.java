package bjc.rgens.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bjc.rgens.parser.elements.CaseElement;
import bjc.data.IPair;
import bjc.data.ITree;
import bjc.data.Pair;
import bjc.data.Tree;

import bjc.funcdata.FunctionalList;
import bjc.funcdata.IList;

import bjc.utils.funcutils.ListUtils;
import bjc.utils.funcutils.SetUtils;

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
	
	/**
	 *  The current grammar name.
	 */
	public String name;

	/* Autovivify variables */
	private Map<String, CaseElement> autoVars;
	private Map<String, CaseElement> autoRlVars;

	/** Create a new randomized grammar builder. */
	public RGrammarBuilder() {
		rules = new HashMap<>();

		exportedRules = new HashSet<>();

		autoVars = new HashMap<>();
		autoRlVars = new HashMap<>();
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
		return getOrCreateRule(rName, new Tree<>());
	}
	
	/**
	 * Get or create a rule by the given name.
	 *
	 * @param rName
	 * 	The name of the rule.
	 * 
	 * @param errs The place to put errors.
	 *
	 * @return
	 * 	The rule by that name, or a new one if none existed.
	 */
	public Rule getOrCreateRule(String rName, ITree<String> errs) {
		if(rName == null) {
			errs.addChild("ERROR: Rule name must not be null");

			return null;
		} else if(rName.equals("")) {
			errs.addChild("ERROR: The empty string is not a valid rule name");

			return null;
		}

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

		grammar.setAutoVars(autoVars, autoRlVars);

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
		ITree<String> errs = new Tree<>();
		
		setInitialRule(init, errs);
		
		// FIXME do something if errs has a result.
	}

	/**
	 * Set the initial rule of the grammar.
	 *
	 * @param init
	 * 	The initial rule of the grammar.
	 *
	 * @param errs The place to put errors.
	 * 
	 * @throws IllegalArgumentException
	 * 	If the rule is either not valid or not defined in the grammar.
	 */
	public void setInitialRule(String init, ITree<String> errs) {
		if (init == null) {
			errs.addChild("init must not be null");

			return;
		} else if (init.equals("")) {
			errs.addChild("The empty string is not a valid rule name");

			return;
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
		addExport(export, new Tree<>());
	}

	/**
	 * Add an exported rule to this grammar.
	 *
	 * @param export
	 * 	The name of the rule to export.
	 *
	 * @param errs The place to put errors.
	 * 
	 * @throws IllegalArgumentException
	 * 	If the rule is either not valid or not defined in the grammar.
	 */
	public void addExport(String export, ITree<String> errs) {
		if (export == null) {
			errs.addChild("ERROR: Export name must not be null");

			return;
		} else if (export.equals("")) {
			errs.addChild("ERROR: The empty string is not a valid rule name");

			return;
		}

		exportedRules.add(export);
	}

	/**
	 * Suffix a given case element to every case of a specific rule.
	 *
	 * @param ruleName
	 * 	The rule to suffix.
	 *
	 * @param suffixes
	 * 	The suffixes to add.
	 *
	 * @throws IllegalArgumentException
	 * 	If the rule name is either invalid or not defined by this
	 * 	grammar, or if the suffix is invalid.
	 */
	public void suffixWith(String ruleName, List<CaseElement> suffixes) {
		affixWith(ruleName, suffixes, AffixType.SUFFIX, new Tree<>());
	}


	/**
	 * Prefix a given case element to every case of a specific rule.
	 *
	 * @param ruleName
	 * 	The rule to prefix.
	 *
	 * @param prefixes
	 * 	The prefixes to add.
	 *
	 * @throws IllegalArgumentException
	 * 	If the rule name is either invalid or not defined by this
	 * 	grammar, or if the prefix is invalid.
	 */
	public void prefixWith(String ruleName, List<CaseElement> prefixes) {
		affixWith(ruleName, prefixes, AffixType.PREFIX, new Tree<>());
	}

	/**
	 * Prefix and suffix a given case element to every case of a specific rule.
	 *
	 * @param ruleName
	 * 	The rule to prefix and suffix.
	 *
	 * @param prefixes
	 * 	The prefixes/suffixes to add.
	 *
	 * @throws IllegalArgumentException
	 * 	If the rule name is either invalid or not defined by this
	 * 	grammar, or if the prefix/suffix is invalid.
	 */
	public void circumfixWith(String ruleName, List<CaseElement> prefixes) {
		affixWith(ruleName, prefixes, AffixType.CIRCUMFIX, new Tree<>());
	}

	/**
	 * Affix a given case element to every case of a specific rule.
	 *
	 * @param ruleName
	 * 	The rule to affix.
	 *
	 * @param affixes
	 * 	The affixes to add.
	 * 
	 * @param type The type of affixing to perform.
	 *
	 * @throws IllegalArgumentException
	 * 	If the rule name is either invalid or not defined by this
	 * 	grammar, or if the prefix/suffix is invalid.
	 */
	public void affixWith(String ruleName, List<CaseElement> affixes, AffixType type) {
		affixWith(ruleName, affixes, type, new Tree<>());
	}

	/**
	 * Affix a given case element to every case of a specific rule.
	 *
	 * @param ruleName
	 * 	The rule to affix.
	 *
	 * @param affixes
	 * 	The affixes to add.
	 * 
	 * @param type The type of affixing to perform.
	 * 
	 * @param errs The place to put errors.
	 *
	 * @throws IllegalArgumentException
	 * 	If the rule name is either invalid or not defined by this
	 * 	grammar, or if the prefix/suffix is invalid.
	 */
	public void affixWith(String ruleName, List<CaseElement> affixes, AffixType type, ITree<String> errs) {
		if (ruleName == null) {
			errs.addChild("ERROR: Rule name must not be null");

			return;
		} else if (ruleName.equals("")) {
			errs.addChild("ERROR: The empty string is not a valid rule name");

			return;
		} else if(!rules.containsKey(ruleName)) {
			String msg = String.format("ERROR: Rule '%s' is not a valid rule name");

			errs.addChild(msg);

			return;
		}

		Set<CaseElement> elements = new HashSet<>(affixes);
		
		List<List<CaseElement>> affixLists = powerList(elements);

		FunctionalList<IPair<Integer, RuleCase>> newCases = new FunctionalList<>();
		IList<IPair<Integer, RuleCase>> caseList = rules.get(ruleName).getCases();

		for (IPair<Integer, RuleCase> ruleCase : caseList) {
			RuleCase cas = ruleCase.getRight();

			for(List<CaseElement> affixList : affixLists) {
				List<CaseElement> newCase = new ArrayList<>();

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

	/**
	 * Perform despacing on a rule.
	 * 
	 * @param ruleName The rule to perform despacing on.
	 */
	public void despaceRule(String ruleName) {
		despaceRule(ruleName, new Tree<>(), false);
	}
	
	/**
	 * Perform despacing on a rule.
	 * 
	 * @param ruleName The rule to perform despacing on.
	 * @param errs The place to put errors.
	 * @param doTrace Whether or not to do tracing.
	 */
	public void despaceRule(String ruleName, ITree<String> errs, boolean doTrace) {
		if (ruleName == null) {
			 errs.addChild("ERROR: Rule name must not be null");

			 return;
		} else if (ruleName.equals("")) {
			errs.addChild("ERROR: The empty string is not a valid rule name");

			return;
		} else if (!rules.containsKey(ruleName)) {
			String msg = String.format("ERROR: The rule '%s' doesn't exist", ruleName);

			errs.addChild(msg);

			return;
		}

		IList<IPair<Integer, RuleCase>> caseList = rules.get(ruleName).getCases();

		IList<IPair<Integer, RuleCase>> newCaseList = new FunctionalList<>();

		for(IPair<Integer, RuleCase> cse : caseList) {
			newCaseList.add(new Pair<>(cse.getLeft(), new FlatRuleCase(cse.getRight().elementList)));
		}

		if (doTrace) {
			String msg = String.format("TRACE: Despacing %d cases of rule %s", caseList.getSize(), ruleName);

			errs.addChild(msg);
		}

		rules.get(ruleName).replaceCases(newCaseList);
	}

	/**
	 * Set the weight for a rule.
	 * 
	 * @param ruleName The rule to set the weight for.
	 */
	public void setWeight(String ruleName) {
		setWeight(ruleName, new Tree<>());
	}

	/**
	 * Set the weight for a rule.
	 * 
	 * @param ruleName The rule to set the weight for.
	 * @param errs The place to put errors.
	 */
	public void setWeight(String ruleName, ITree<String> errs) {
		if (ruleName == null) {
			errs.addChild("ERROR: Rule name must not be null");

			return;
		} else if (ruleName.equals("")) {
			errs.addChild("ERROR: The empty string is not a valid rule name");

			return;
		} else if (!rules.containsKey(ruleName)) {
			String msg = String.format("ERROR: The rule '%s' doesn't exist", ruleName);

			errs.addChild(msg);

			return;
		}

		rules.get(ruleName).prob = Rule.ProbType.NORMAL;
	}

	/**
	 * Set the rule recurrence limit.
	 * 
	 * @param ruleName The name of the rule.
	 * @param recurLimit The limit of the number of times to recur.
	 */
	public void setRuleRecur(String ruleName, int recurLimit) {
		setRuleRecur(ruleName, recurLimit, new Tree<>());
	}

	/**
	 * Set the rule recurrence limit.
	 * 
	 * @param ruleName The name of the rule.
	 * @param recurLimit The limit of the number of times to recur.
	 * @param errs The place to put errors.
	 */
	public void setRuleRecur(String ruleName, int recurLimit, ITree<String> errs) {
		if (ruleName == null) {
			errs.addChild("ERROR: Rule name must not be null");

			return;
		} else if (ruleName.equals("")) {
			errs.addChild("ERROR: The empty string is not a valid rule name");

			return;
		} else if (!rules.containsKey(ruleName)) {
			String msg = String.format("ERROR: The rule '%s' doesn't exist", ruleName);

			errs.addChild(msg);

			return;
		}

		rules.get(ruleName).recurLimit = recurLimit;
	}

	/**
	 * Set probability descent on the rule.
	 * 
	 * @param ruleName The name of the rule.
	 * @param descentFactor The amount to descend by every time.
	 */
	public void setDescent(String ruleName, int descentFactor) {
		setDescent(ruleName, descentFactor, new Tree<>());
	}

	/**
	 * Set probability descent on the rule.
	 * 
	 * @param ruleName The name of the rule.
	 * @param descentFactor The amount to descend by every time.
	 * @param errs Place to put errors.
	 */
	public void setDescent(String ruleName, int descentFactor, ITree<String> errs) {
		if (ruleName == null) {
			errs.addChild("ERROR: Rule name must not be null");

			return;
		} else if (ruleName.equals("")) {
			errs.addChild("ERROR: The empty string is not a valid rule name");

			return;
		} else if (!rules.containsKey(ruleName)) {
			String msg = String.format("ERROR: The rule '%s' doesn't exist", ruleName);

			errs.addChild(msg);

			return;
		}

		Rule rl = rules.get(ruleName);

		rl.prob          = Rule.ProbType.DESCENDING;
		rl.descentFactor = descentFactor;
	}

	/**
	 * Set the binomial distribution on a rule.
	 * 
	 * @param ruleName The name of the rule.
	 * @param target The target weighting.
	 * @param bound The bound for the distribution.
	 * @param trials The number of trials for the distribution.
	 */
	public void setBinomial(String ruleName, int target, int bound, int trials) {
		setBinomial(ruleName, target, bound, trials, new Tree<>());
	}

	/**
	 * Set the binomial distribution on a rule.
	 * 
	 * @param ruleName The name of the rule.
	 * @param target The target weighting.
	 * @param bound The bound for the distribution.
	 * @param trials The number of trials for the distribution.
	 * @param errs The place to put errors.
	 */
	public void setBinomial(String ruleName, int target, int bound, int trials, ITree<String> errs) {
		if (ruleName == null) {
			errs.addChild("ERROR: Rule name must not be null");

			return;
		} else if (ruleName.equals("")) {
			errs.addChild("ERROR: The empty string is not a valid rule name");

			return;
		} else if (!rules.containsKey(ruleName)) {
			String msg = String.format("ERROR: The rule '%s' doesn't exist", ruleName);
			errs.addChild(msg);

			return;
		}

		Rule rl = rules.get(ruleName);

		rl.prob = Rule.ProbType.BINOMIAL;

		rl.target = target;
		rl.bound  = bound;
		rl.trials = trials;
	}

	/**
	 * Add an auto-variable.
	 * 
	 * @param nam The name of the variable to add.
	 * @param elm The definition of the variable.
	 */
	public void addAutoVar(String nam, CaseElement elm) {
		autoVars.put(nam, elm);
	}

	/**
	 * Add an auto-rule-variable.
	 * 
	 * @param nam The name of the rule variable to add.
	 * @param elm The definition of the rule variable.
	 */
	public void addAutoRlVar(String nam, CaseElement elm) {
		autoRlVars.put(nam, elm);
	}

	/**
	 * Add a rule rejection.
	 * 
	 * @param rule The name of the rule.
	 * @param reject The rejection for the rule.
	 */
	public void rejectRule(String rule, String reject) {
		rejectRule(rule, reject, new Tree<>());
	}

	/**
	 * Add a rule rejection.
	 * 
	 * @param rule The name of the rule.
	 * @param reject The rejection for the rule.
	 * @param errs The place to put errors.
	 */
	public void rejectRule(String rule, String reject, ITree<String> errs) {
		if (rule == null) {
			errs.addChild("ERROR: Rule must not be null");

			return;
		} else if(reject == null) {
			errs.addChild("ERROR: Reject must not be null");	

			return;
		} else if (rule.equals("")) {
			errs.addChild("ERROR: The empty string is not a valid rule name");

			return;
		} else if(!rules.containsKey(rule)) {
			String msg = String.format("ERROR: The rule '%s' doesn't exist", rule);

			errs.addChild(msg);

			return;
		}

		Rule rl = rules.get(rule);

		rl.addRejection(reject, errs);
	}

	/**
	 * Add a find/replace to a rule.
	 * 
	 * @param rule The name of the rule
	 * @param find The find string.
	 * @param replace The replace string.
	 */
	public void findReplaceRule(String rule, String find, String replace) {
		findReplaceRule(rule, find, replace, new Tree<>());
	}

	/**
	 * Add a find/replace to a rule.
	 * 
	 * @param rule The name of the rule
	 * @param find The find string.
	 * @param replace The replace string.
	 * @param errs The place to put errors.
	 */
	public void findReplaceRule(String rule, String find, String replace, ITree<String> errs) {
		if (rule == null) {
			errs.addChild("ERROR: Rule must not be null");

			return;
		} else if(find == null) {
			errs.addChild("ERROR: Find must not be null");	

			return;
		} else if(replace == null) {
			errs.addChild("ERROR: Replace must not be null");	

			return;
		} else if (rule.equals("")) {
			errs.addChild("ERROR: The empty string is not a valid rule name");

			return;
		} else if(!rules.containsKey(rule)) {
			String msg = String.format("ERROR: The rule '%s' doesn't exist", rule);

			errs.addChild(msg);

			return;
		}

		Rule rl = rules.get(rule);

		rl.addFindReplace(find, replace, errs);
	}

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
