package bjc.rgens.newparser;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Represents a set of grammars that can share rules via exports.
 * 
 * @author EVE
 *
 */
public class RGrammarSet {
	private Map<String, RGrammar> grammars;

	private Map<String, RGrammar> exportedRules;

	/**
	 * Create a new set of randomized grammars.
	 */
	public RGrammarSet() {
		grammars = new HashMap<>();

		exportedRules = new HashMap<>();
	}

	/**
	 * Add a grammar to this grammar set.
	 * 
	 * @param grammarName
	 *                The name of the grammar to add.
	 * 
	 * @param gram
	 *                The grammar to add.
	 * 
	 * @throws IllegalArgumentException
	 *                 If the grammar name is invalid.
	 */
	public void addGrammar(String grammarName, RGrammar gram) {
		if(grammarName == null) {
			throw new NullPointerException("Grammar name must not be null");
		} else if(gram == null) {
			throw new NullPointerException("Grammar must not be null");
		} else if(grammarName.equals("")) {
			throw new IllegalArgumentException("The empty string is not a valid grammar name");
		}

		grammars.put(grammarName, gram);

		for(Rule export : gram.getExportedRules()) {
			exportedRules.put(export.ruleName, gram);
		}

		gram.setImportedRules(exportedRules);
	}

	/**
	 * Get a grammar from this grammar set.
	 * 
	 * @param grammarName
	 *                The name of the grammar to get.
	 * 
	 * @return The grammar with that name.
	 * 
	 * @throws IllegalArgumentException
	 *                 If the grammar name is invalid or not present in this
	 *                 set.
	 */
	public RGrammar getGrammar(String grammarName) {
		if(grammarName == null) {
			throw new NullPointerException("Grammar name must not be null");
		} else if(grammarName.equals("")) {
			throw new IllegalArgumentException("The empty string is not a valid grammar name");
		} else if(!grammars.containsKey("")) {
			throw new IllegalArgumentException(
					String.format("No grammar with name '%s' found", grammarName));
		}

		return grammars.get(grammarName);
	}

	/**
	 * Get the grammar a rule was exported from.
	 * 
	 * @param exportName
	 *                The name of the exported rule.
	 * 
	 * @return The grammar the exported rule came from.
	 * 
	 * @throws IllegalArgumentException
	 *                 If the export name is invalid or not present in this
	 *                 set.
	 */
	public RGrammar getExportSource(String exportName) {
		if(exportName == null) {
			throw new NullPointerException("Export name must not be null");
		} else if(exportName.equals("")) {
			throw new IllegalArgumentException("The empty string is not a valid rule name");
		} else if(!exportedRules.containsKey(exportName)) {
			throw new IllegalArgumentException(String.format("No export with name '%s' found", exportName));
		}
		
		return exportedRules.get(exportName);
	}

	/**
	 * Get the names of all the grammars in this set.
	 * 
	 * @return The names of all the grammars in this set.
	 */
	public Set<String> getGrammars() {
		return grammars.keySet();
	}

	/**
	 * Get the names of all the exported rules in this set.
	 * 
	 * @return The names of all the exported rules in this set.
	 */
	public Set<String> getExportedRules() {
		return exportedRules.keySet();
	}
}
