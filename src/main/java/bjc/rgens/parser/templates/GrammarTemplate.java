package bjc.rgens.parser.templates;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import bjc.rgens.parser.ConfigSet;
import bjc.rgens.parser.GenerationState;

public class GrammarTemplate {
	public ConfigSet belongsTo;

	public String name;

	public final List<TemplateElement> elements;

	public boolean doSpacing = true;

	public GrammarTemplate(List<TemplateElement> elements) {
		this.elements = elements;
	}

	public void generate(GenerationState state) {
		for(TemplateElement element : elements) {
			element.generate(state);

			if(doSpacing && element.spacing)
				state.appendContents("\n");
		}
	}

	public static GrammarTemplate readTemplate(Reader rdr) {
		List<TemplateElement> elements = new ArrayList<>();
		GrammarTemplate template = new GrammarTemplate(elements);

		Scanner scn = new Scanner(rdr);
		scn.useDelimiter("\\R");

		//int lno = 0;
		while(scn.hasNextLine()) {
			String ln = scn.nextLine();
			//lno += 1;

			switch(ln.charAt(0)) {
			case '#':
				// Ignore comments
				break;
			case '/':
				handlePragma(elements, template, ln.substring(1));
				break;
			default:
				handleLine(elements, template, ln);
			}
		}

		scn.close();
		
		return template;
	}

	private static void handleLine(List<TemplateElement> elements, GrammarTemplate template, String ln) {
		if(ln.matches("^.*?\\$@.+?@\\$.*$")) {
			/*
			 * Handle live templates
			 */
			elements.add(new LiveTemplateElement(ln));
		} else {
			elements.add(new LiteralTemplateElement(ln));
		}
	}

	private static void handlePragma(List<TemplateElement> elements, GrammarTemplate template, String ln) {
		// TODO
	}
}
