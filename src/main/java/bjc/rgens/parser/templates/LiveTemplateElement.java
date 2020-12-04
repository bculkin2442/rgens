package bjc.rgens.parser.templates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bjc.data.BooleanToggle;
import bjc.data.Tree;

import bjc.rgens.parser.GenerationState;
import bjc.rgens.parser.RGrammarParser;
import bjc.rgens.parser.elements.CaseElement;
import bjc.rgens.parser.elements.LiteralCaseElement;

/**
 * A template element that can contain rule elements.
 *
 * @author Ben Culkin.
 */
public class LiveTemplateElement extends TemplateElement {
	// Pattern for matching elements (any number of characters bracketed by '$@' and '@$')
	private static final Pattern INSERT_PAT = Pattern.compile("\\$@(.+?)@\\$");

	/**
	 * The sub-elements of this element.
	 */
	public final List<List<CaseElement>> elements;

	/**
	 * Create a new template element.
	 *
	 * @param val
	 * 		The string to parse this element from.
	 *
	 * @param errs
	 * 		A tree to add errors &amp; information to.
	 */
	public LiveTemplateElement(String val, Tree<String> errs) {
		super(true);

		elements = new ArrayList<>();

		Matcher mat = INSERT_PAT.matcher(val);
		StringBuffer sb = new StringBuffer();

		while(mat.find()) {
			mat.appendReplacement(sb, "");
			String body = mat.group(1);

			List<CaseElement> elms = new ArrayList<>();

			// This mutates elms. Not great design, but passable
			RGrammarParser.parseElementString(body, elms, errs);

			elements.add(Arrays.asList(new LiteralCaseElement(sb.toString())));
			elements.add(elms);

			sb = new StringBuffer();
		}

		mat.appendTail(sb);
		elements.add(Arrays.asList(new LiteralCaseElement(sb.toString())));
	}

	@Override
	public void generate(GenerationState state) {
		BooleanToggle bt = new BooleanToggle(false);

		for(List<CaseElement> elmList : elements) {
			boolean doSpacing = bt.get();

			for(CaseElement elm : elmList) {
				elm.generate(state);

				if(doSpacing && elm.spacing)
					state.appendContents(" ");
			}
		}
	}
}
