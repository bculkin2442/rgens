package bjc.rgens.parser;

import java.util.HashMap;
import java.util.Map;

public class ConfigSet {
	public final Map<String, RGrammarSet> grammars;
	public final Map<String, GrammarTemplate> templates;
	public final Map<String, ConfigSet> subconfigs;

	public ConfigSet() {
		grammars   = new HashMap<>();
		templates  = new HashMap<>();
		subconfigs = new HashMap<>();
	}
}
