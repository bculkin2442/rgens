package bjc.rgens.parser.elements;

import bjc.rgens.parser.GenerationState;

import bjc.utils.funcdata.FunctionalList;
import bjc.utils.funcdata.IList;

public class InlineRuleCaseElement extends CaseElement {
	public final IList<CaseElement> elements;

	public InlineRuleCaseElement(String... elements) {
		this(new FunctionalList<>(elements).map(CaseElement::createElement));
	}

	public InlineRuleCaseElement(IList<CaseElement> elements) {
		super(ElementType.RULEREF);

		this.elements = elements;
	}

	public void generate(GenerationState state) {
		elements.randItem(state.rnd::nextInt).generate(state);
	}
}
