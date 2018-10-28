package bjc.rgens.parser;

import static bjc.rgens.parser.RGrammarLogging.trace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bjc.rgens.parser.elements.CaseElement;
import bjc.utils.data.IPair;
import bjc.utils.data.Pair;
import bjc.utils.funcdata.FunctionalList;
import bjc.utils.funcdata.IList;
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
	/* The current grammar name. */
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
	 * @param suffixes
	 * 	The suffixes to add.
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
	 * @param prefixes
	 * 	The prefixes to add.
	 *
	 * @throws IllegalArgumentException
	 * 	If the rule name is either invalid or not defined by this
	 * 	grammar, or if the prefix is invalid.
	 */
	public void prefixWith(String ruleName, IList<CaseElement> prefixes) {
		affixWith(ruleName, prefixes, AffixType.PREFIX);
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
	public void circumfixWith(String ruleName, IList<CaseElement> prefixes) {
		affixWith(ruleName, prefixes, AffixType.CIRCUMFIX);
	}

	public static enum AffixType {
		CIRCUMFIX,
		SUFFIX,
		PREFIX;

		public boolean isSuffix() {
			return this != PREFIX;
		}

		public boolean isPrefix() {
			return this != SUFFIX;
		}
	}

	public void affixWith(String ruleName, IList<CaseElement> affixes, AffixType type) {
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

	public void addAutoVar(String name, CaseElement elm) {
		autoVars.put(name, elm);
	}

	public void addAutoRlVar(String name, CaseElement elm) {
		autoRlVars.put(name, elm);
	}

	public void rejectRule(String rule, String reject) {
		if (rule == null) {
			throw new NullPointerException("rule must not be null");
		} else if(reject == null) {
			throw new NullPointerException("reject must not be null");	
		} else if (rule.equals("")) {
			throw new IllegalArgumentException("The empty string is not a valid rule name");
		} else if(!rules.containsKey(rule)) {
			throw new IllegalArgumentException(String.format("The rule '%s' doesn't exist", rule));
		}

		Rule rl = rules.get(rule);

		rl.addRejection(reject);
	}

	public void findReplaceRule(String rule, String find, String replace) {
		if (rule == null) {
			throw new NullPointerException("rule must not be null");
		} else if(find == null) {
			throw new NullPointerException("find must not be null");	
		} else if(replace == null) {
			throw new NullPointerException("replace must not be null");	
		} else if (rule.equals("")) {
			throw new IllegalArgumentException("The empty string is not a valid rule name");
		} else if(!rules.containsKey(rule)) {
			throw new IllegalArgumentException(String.format("The rule '%s' doesn't exist", rule));
		}

		Rule rl = rules.get(rule);

		rl.addFindReplace(find, replace);
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
