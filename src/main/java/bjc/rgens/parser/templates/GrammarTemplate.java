package bjc.rgens.parser.templates;

import bjc.rgens.parser.ConfigSet;
import bjc.rgens.parser.GenerationState;

import bjc.data.ITree;
import bjc.data.Tree;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
/**
 * Represents a grammar template.
 *
 * @author Ben Culkin
 */
public class GrammarTemplate {
	/**
	 * The config set the template belongs to.
	 */
	public ConfigSet belongsTo;

	/**
	 * The name of the template.
	 */
	public String name;

	/**
	 * The elements in the template.
	 */
	public final List<TemplateElement> elements;

	/**
	 * Whether or not to do spacing of elements.
	 */
	public boolean doSpacing = true;

	/**
	 * Create a new grammar template.
	 *
	 * @param elements
	 * 		The elements that belong to the template.
	 */
	public GrammarTemplate(List<TemplateElement> elements) {
		this.elements = elements;
	}

	/**
	 * Generate the template.
	 *
	 * @param state
	 * 		The state for generating a template.
	 */
	public void generate(GenerationState state) {
		for(TemplateElement element : elements) {
			element.generate(state);

			if(doSpacing && element.spacing)
				state.appendContents("\n");
		}
	}

	/**
	 * Read a template from an input source.
	 *
	 * @param rdr
	 * 		The reader to get input from.
	 *
	 * @param errs
	 * 		The errors/information to generate during loading.
	 *
	 * @return The generated template.
	 */
	public static GrammarTemplate readTemplate(Reader rdr, ITree<String> errs) {
		List<TemplateElement> elements = new ArrayList<>();
		GrammarTemplate template = new GrammarTemplate(elements);

		Scanner scn = new Scanner(rdr);
		scn.useDelimiter("\\R");

		int lno = 0;
		while(scn.hasNextLine()) {
			String ln = scn.nextLine();
			lno += 1;

			ITree<String> kid = new Tree<>(String.format("INFO: Line %d", lno));
			switch(ln.charAt(0)) {
			case '#':
				// Ignore comments
				break;
			case '/':
				handlePragma(elements, template, ln.substring(1), kid);
				break;
			default:
				handleLine(elements, template, ln, kid);
			}

			if (kid.size() > 0) {
				errs.addChild(kid);
			}
		}

		scn.close();
		
		return template;
	}

	private static void handleLine(List<TemplateElement> elements, GrammarTemplate template, String ln, ITree<String> errs) {
		if(ln.matches("^.*?\\$@.+?@\\$.*$")) {
			/*
			 * Handle live templates
			 */
			elements.add(new LiveTemplateElement(ln, errs));
		} else {
			elements.add(new LiteralTemplateElement(ln, errs));
		}
	}

	private static void handlePragma(List<TemplateElement> elements, GrammarTemplate template, String ln, ITree<String> errs) {
		/*
		 * @TODO 2/8/2019 Ben Culkin :TemplatePragmas
		 * Implement template pragmas.
		 *
		 * Implement template pragmas. Mainly, this means that the 'choose'
		 * based ones need to be implemented based off of the provided sample
		 * template.
		 */
		errs.addChild("ERROR: Template pragmas are not yet implemented");
	}
}
