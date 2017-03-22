package bjc.rgens.newparser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a randomized grammar.
 * 
 * @author EVE
 *
 */
public class RGrammar {
	private static class GenerationState {
		public StringBuilder	contents;
		public Random		rnd;

		public Map<String, String> vars;

		public GenerationState(StringBuilder contents, Random rnd, Map<String, String> vs) {
			this.contents = contents;
			this.rnd = rnd;
			this.vars = vs;
		}
	}

	private static Pattern NAMEVAR_PATTERN = Pattern.compile("\\$(\\w+)");

	private Map<String, Rule> rules;

	private Map<String, RGrammar> importRules;

	private Set<String> exportRules;

	private String initialRule;

	/**
	 * Create a new randomized grammar using the specified set of rules.
	 * 
	 * @param rules
	 *                The rules to use.
	 */
	public RGrammar(Map<String, Rule> rules) {
		this.rules = rules;
	}

	/**
	 * Sets the imported rules to use.
	 * 
	 * Imported rules are checked for rule definitions after local
	 * definitions are checked.
	 * 
	 * @param exportedRules
	 *                The set of imported rules to use.
	 */
	public void setImportedRules(Map<String, RGrammar> exportedRules) {
		importRules = exportedRules;
	}

	/**
	 * Generate a string from this grammar, starting from the specified
	 * rule.
	 * 
	 * @param startRule
	 *                The rule to start generating at, or null to use the
	 *                initial rule for this grammar.
	 * 
	 * @return A possible string from the grammar.
	 */
	public String generate(String startRule) {
		return generate(startRule, new Random());
	}

	/**
	 * Generate a string from this grammar, starting from the specified
	 * rule.
	 * 
	 * @param startRule
	 *                The rule to start generating at, or null to use the
	 *                initial rule for this grammar.
	 * 
	 * @param rnd
	 *                The random number generator to use.
	 * 
	 * @return A possible string from the grammar.
	 */
	public String generate(String startRule, Random rnd) {
		String fromRule = startRule;

		if(startRule == null) {
			if(initialRule == null) {
				throw new GrammarException(
						"Must specify a start rule for grammars with no initial rule");
			} else {
				fromRule = initialRule;
			}
		} else {
			if(startRule.equals("")) {
				throw new GrammarException("The empty string is not a valid rule name");
			}
		}

		RuleCase start = rules.get(fromRule).getCase(rnd);

		StringBuilder contents = new StringBuilder();

		HashMap<String, String> scope = new HashMap<>();

		generateCase(start, new GenerationState(contents, rnd, scope));

		return contents.toString();
	}

	/*
	 * Generate a rule case.
	 */
	private void generateCase(RuleCase start, GenerationState state) {
		try {
			switch(start.type) {
			case NORMAL:
				for(CaseElement elm : start.getElements()) {
					generateElement(elm, state);
				}
				break;
			default:
				throw new GrammarException(String.format("Unknown case type '%s'", start.type));
			}
		} catch(GrammarException gex) {
			throw new GrammarException(String.format("Error in generating case (%s)", start), gex);
		}
	}

	/*
	 * Generate a case element.
	 */
	private void generateElement(CaseElement elm, GenerationState state) {
		try {
			switch(elm.type) {
			case LITERAL:
				state.contents.append(elm.getLiteral());
				state.contents.append(" ");
				break;
			case RULEREF:
				generateRuleReference(elm, state);
				break;
			case RANGE:
				int start = elm.getStart();
				int end = elm.getEnd();

				int val = state.rnd.nextInt(end - start);
				val += start;

				state.contents.append(val);
				state.contents.append(" ");
				break;
			case VARDEF:
				generateVarDef(elm.getName(), elm.getDefn(), state);
				break;
			case EXPVARDEF:
				generateExpVarDef(elm.getName(), elm.getDefn(), state);
				break;
			default:
				throw new GrammarException(String.format("Unknown element type '%s'", elm.type));
			}
		} catch(GrammarException gex) {
			throw new GrammarException(String.format("Error in generating case element (%s)", elm), gex);
		}
	}

	/*
	 * Generate a expanding variable definition.
	 */
	private void generateExpVarDef(String name, String defn, GenerationState state) {
		GenerationState newState = new GenerationState(new StringBuilder(), state.rnd, state.vars);

		if(rules.containsKey(defn)) {
			RuleCase destCase = rules.get(defn).getCase();

			generateCase(destCase, newState);
		} else if(importRules.containsKey(defn)) {
			RGrammar destGrammar = importRules.get(defn);
			RuleCase destCase = destGrammar.rules.get(defn).getCase();

			destGrammar.generateCase(destCase, newState);
		} else {
			throw new GrammarException(String.format("No rule '%s' defined", defn));
		}

		state.vars.put(name, newState.contents.toString());
	}

	/*
	 * Generate a variable definition.
	 */
	private void generateVarDef(String name, String defn, GenerationState state) {
		state.vars.put(name, defn);
	}

	/*
	 * Generate a rule reference.
	 */
	private void generateRuleReference(CaseElement elm, GenerationState state) {
		String refersTo = elm.getLiteral();

		GenerationState newState = new GenerationState(new StringBuilder(), state.rnd, state.vars);

		if(refersTo.contains("$")) {
			/*
			 * Parse variables
			 */
			String refBody = refersTo.substring(1, refersTo.length() - 1);

			if(refBody.contains("-")) {
				/*
				 * Handle dependant rule names.
				 */
				StringBuffer nameBuffer = new StringBuffer();

				Matcher nameMatcher = NAMEVAR_PATTERN.matcher(refBody);

				while(nameMatcher.find()) {
					String var = nameMatcher.group(1);

					if(!state.vars.containsKey(var)) {
						throw new GrammarException(String.format("No variable '%s' defined"));
					}

					String name = state.vars.get(var);

					if(name.contains(" ")) {
						throw new GrammarException(
								"Variables substituted into names cannot contain spaces");
					} else if(name.equals("")) {
						throw new GrammarException(
								"Variables substituted into names cannot be empty");
					}

					nameMatcher.appendReplacement(nameBuffer, name);
				}

				nameMatcher.appendTail(nameBuffer);

				refersTo = nameBuffer.toString();
			} else {
				/*
				 * Handle string references.
				 */
				if(refBody.equals("$")) {
					throw new GrammarException("Cannot refer to unnamed variables");
				}

				String key = refBody.substring(1);

				if(!state.vars.containsKey(key)) {
					throw new GrammarException(String.format("No variable '%s' defined", key));
				}

				state.contents.append(state.vars.get(key));
			}

			refersTo = refBody;
		}

		if(rules.containsKey(refersTo)) {
			RuleCase cse = rules.get(refersTo).getCase(state.rnd);

			generateCase(cse, newState);
		} else if(importRules.containsKey(refersTo)) {
			RGrammar dst = importRules.get(refersTo);

			newState.contents.append(dst.generate(refersTo, state.rnd));
		} else {
			throw new GrammarException(String.format("No rule '%s' defined", refersTo));
		}

		if(refersTo.contains("+")) {
			/*
			 * Rule names with pluses in them get space-flattened
			 */
			state.contents.append(newState.contents.toString().replaceAll("\\s+", ""));
		} else {
			state.contents.append(newState.contents.toString());
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
	 * @param initialRule
	 *                The initial rule of this grammar, or null to say there
	 *                is no initial rule.
	 */
	public void setInitialRule(String initialRule) {
		/*
		 * Passing null nulls our initial rule.
		 */
		if(initialRule == null) {
			this.initialRule = null;
			return;
		}

		if(initialRule.equals("")) {
			throw new GrammarException("The empty string is not a valid rule name");
		} else if(!rules.containsKey(initialRule)) {
			throw new GrammarException(
					String.format("No rule '%s' local to this grammar defined.", initialRule));
		}

		this.initialRule = initialRule;
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

		for(String rname : exportRules) {
			if(!rules.containsKey(rname)) {
				throw new GrammarException(String.format("No rule '%s' local to this grammar defined",
						initialRule));
			}

			res.add(rules.get(rname));
		}

		if(initialRule != null) {
			res.add(rules.get(initialRule));
		}

		return res;
	}

	/**
	 * Set the rules exported by this grammar.
	 * 
	 * @param exportRules
	 *                The rules exported by this grammar.
	 */
	public void setExportedRules(Set<String> exportRules) {
		this.exportRules = exportRules;
	}

	/**
	 * Get all the rules in this grammar.
	 * 
	 * @return All the rules in this grammar.
	 */
	public Map<String, Rule> getRules() {
		return rules;
	}
}
