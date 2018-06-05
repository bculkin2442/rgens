package bjc.rgens.parser;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class GrammarTemplate {
	public String name;

	public final List<String> lines;

	public GrammarTemplate(List<String> lines) {
		this.lines = lines;
	}

	public static GrammarTemplate readTemplate(Reader rdr) {
		List<String> lines = new ArrayList<>();

		GrammarTemplate template = new GrammarTemplate(lines);

		return template;
	}
}
