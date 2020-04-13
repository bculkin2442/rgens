package bjc.rgens.parser;

import java.util.HashMap;
import java.util.Map;

import bjc.rgens.parser.templates.GrammarTemplate;

/**
 * Represents a collection of grammar sets, templates and subcollections of the same
 * that are logically grouped together.
 *
 * In many cases, there will be a one-to-one mapping between grammar sets and
 * config sets, but that doesn't have to be the case. In addition, the config
 * set provides a convenient place to set the non-grammar objects that are
 * defined via config files, such as templates, or Markov generators.
 *
 * @author Ben Culkin
 */
public class ConfigSet {
	// @NOTE Should these fields be public, or do we want to create accessor
	// methods for them, since at least grammars have some extra stuff that
	// needs to be done when they are associated/unassociated with a config
	// set.
	/**
	 * Represents all of the grammar sets that are mapped into this config
	 * set.
	 */
	public final Map<String, RGrammarSet>     grammars;

	/**
	 * Represents all of the templates that are mapped into this config set.
	 */
	public final Map<String, GrammarTemplate> templates;

	/**
	 * Represents all of the sub-configurations that are mapped into this
	 * config set.
	 */
	public final Map<String, ConfigSet>       subconfigs;

	/**
	 * Create a new blank config set.
	 */
	public ConfigSet() {
		grammars   = new HashMap<>();
		templates  = new HashMap<>();
		subconfigs = new HashMap<>();
	}

	/**
	 * Create a grammar set with the given name in this config set.
	 *
	 * @param name
	 * 	The name of the grammar set to create.
	 *
	 * @return The grammar set, properly bound and initialized for this
	 * config set.
	 */
	public RGrammarSet createGSet(String name) {
		RGrammarSet st = new RGrammarSet();
		
		// Init. the properties of the grammar set
		st.belongsTo = this;
		st.name      = name;

		// Add it to ourselves
		grammars.put(name, st);

		return st;
	}
}
