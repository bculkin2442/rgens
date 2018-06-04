package bjc.rgens.parser;

import bjc.rgens.parser.elements.CaseElement;

import bjc.utils.data.IPair;
import bjc.utils.data.Pair;
import bjc.utils.funcdata.FunctionalList;
import bjc.utils.funcdata.IList;
import bjc.utils.funcutils.ListUtils;
import bjc.utils.funcutils.SetUtils;

import static bjc.rgens.parser.RuleCase.CaseType.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

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

		if(initialRule != null) {
			if(!rules.containsKey(initialRule)) {
				throw new GrammarException(String.format("Rule '%s' doesn't exist\n", initialRule));
			}
		}

		grammar.setInitialRule(initialRule);

		for(String export : exportedRules) {
			if(!rules.containsKey(export)) {
				throw new GrammarException(String.format("Rule '%s' doesn't exist\n", export));
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
	public void suffixWith(String ruleName, String... suffixes) {
		if (ruleName == null) {
			throw new NullPointerException("Rule name must not be null");
		} else if (ruleName.equals("")) {
			throw new IllegalArgumentException("The empty string is not a valid rule name");
		} else if(!rules.containsKey(ruleName)) {
			String msg = String.format("Rule '%s' is not a valid rule name");

			throw new IllegalArgumentException(msg);
		}

		Set<CaseElement> elements = new HashSet<>(suffixes.length);
		for(String suffix : suffixes) {
			elements.add(CaseElement.createElement(suffix));
		}
		
		List<List<CaseElement>> suffixLists = powerList(elements);

		FunctionalList<IPair<Integer, RuleCase>> newCases = new FunctionalList<>();

		IList<IPair<Integer, RuleCase>> caseList = rules.get(ruleName).getCases();
		for (IPair<Integer, RuleCase> ruleCase : caseList) {
			for(List<CaseElement> suffixList : suffixLists) {
				FunctionalList<CaseElement> newCase = new FunctionalList<>();

				for(CaseElement elm : ruleCase.getRight().getElements()) {
					newCase.add(elm);
				}

				for(CaseElement element : suffixList) {
					newCase.add(element);
				}

				/*
				 * @NOTE :AffixCasing
				 *
				 * Is this correct, or should we be mirroring the
				 * existing case type?
				 */
				newCases.add(new Pair<>(ruleCase.getLeft(), new NormalRuleCase(newCase)));
			}
		}


		for (IPair<Integer, RuleCase> newCase : newCases) {
			rules.get(ruleName).addCase(newCase.getRight(), newCase.getLeft());
		}
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
	public void prefixWith(String ruleName, String... prefixes) {
		if (ruleName == null) {
			throw new NullPointerException("Rule name must not be null");
		} else if (ruleName.equals("")) {
			throw new IllegalArgumentException("The empty string is not a valid rule name");
		} else if(!rules.containsKey(ruleName)) {
			String msg = String.format("Rule '%s' is not a valid rule name");

			throw new IllegalArgumentException(msg);
		}

		Set<CaseElement> elements = new HashSet<>(prefixes.length);
		for(String prefix : prefixes) {
			elements.add(CaseElement.createElement(prefix));
		}
		
		List<List<CaseElement>> prefixLists = powerList(elements);

		FunctionalList<IPair<Integer, RuleCase>> newCases = new FunctionalList<>();

		IList<IPair<Integer, RuleCase>> caseList = rules.get(ruleName).getCases();
		for (IPair<Integer, RuleCase> ruleCase : caseList) {
			for(List<CaseElement> prefixList : prefixLists) {
				FunctionalList<CaseElement> newCase = new FunctionalList<>();

				for(CaseElement elm: prefixList) {
					newCase.add(elm);
				}

				for(CaseElement elm : ruleCase.getRight().getElements()) {
					newCase.add(elm);
				}

				/*
				 * @NOTE :AffixCasing
				 *
				 * Is this correct, or should we be mirroring the
				 * existing case type?
				 */
				newCases.add(new Pair<>(ruleCase.getLeft(), new NormalRuleCase(newCase)));
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
			newCaseList.add(new Pair<>(cse.getLeft(), new FlatRuleCase(cse.getRight().getElements())));
		}

		rules.get(ruleName).replaceCases(newCaseList);
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
	/*
	 * @TODO
	 *
	 * Actually get this working
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
				System.err.printf("\tTRACE: generated permute ");
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
