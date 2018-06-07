package bjc.rgens.parser.elements;

import bjc.rgens.parser.GenerationState;
import bjc.rgens.parser.RGrammarParser;

import bjc.utils.data.IPair;
import bjc.utils.funcdata.FunctionalList;
import bjc.utils.funcdata.IList;
import bjc.utils.gen.WeightedRandom;

public class InlineRuleCaseElement extends CaseElement {
	public final WeightedRandom<CaseElement> elements;

	public InlineRuleCaseElement(String... parts) {
		super(true);

		this.elements = new WeightedRandom<>();

		for(String part : parts) {
			String[] partArr;

			if(part.contains("|")) {
				partArr = part.split("\\|");
			} else {
				partArr = new String[] {part};
			}

			IPair<IList<CaseElement>, Integer> par = RGrammarParser.parseElementString(partArr);
			int prob = par.getRight();

			for(CaseElement elm :par.getLeft()) {
				elements.addProbability(prob, elm);
			}
		}
	}

	public void generate(GenerationState state) {
		elements.generateValue(state.rnd).generate(state);
	}
}
