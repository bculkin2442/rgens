package bjc.rgens.parser.templates;

import bjc.utils.data.BooleanToggle;
import bjc.utils.funcdata.FunctionalList;

import bjc.rgens.parser.GenerationState;
import bjc.rgens.parser.RGrammarParser;
import bjc.rgens.parser.elements.CaseElement;
import bjc.rgens.parser.elements.LiteralCaseElement;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LiveTemplateElement extends TemplateElement {
	private static final Pattern INSERT_PAT = Pattern.compile("\\$@(.+?)@\\$");

	public final List<List<CaseElement>> elements;

	public LiveTemplateElement(String val) {
		super(true);

		elements = new ArrayList<>();

		Matcher mat = INSERT_PAT.matcher(val);
		StringBuffer sb = new StringBuffer();

		while(mat.find()) {
			mat.appendReplacement(sb, "");
			String body = mat.group(1);

			FunctionalList<CaseElement> elms = (FunctionalList<CaseElement>)RGrammarParser.parseElementString(body).getLeft();

			elements.add(Arrays.asList(new LiteralCaseElement(sb.toString())));
			elements.add(elms.getInternal());

			sb = new StringBuffer();
		}

		mat.appendTail(sb);
		elements.add(Arrays.asList(new LiteralCaseElement(sb.toString())));
	}

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
