package bjc.rgens.parser.elements;

import bjc.rgens.parser.GenerationState;
import bjc.rgens.parser.RGrammarParser;
import bjc.utils.data.IPair;
import bjc.utils.funcdata.IList;
import bjc.utils.gen.WeightedRandom;
import bjc.utils.ioutils.LevelSplitter;

public class InlineRuleCaseElement extends CaseElement {
	public final WeightedRandom<CaseElement> elements;

	public InlineRuleCaseElement(String... parts) {
		super(true);

		this.elements = new WeightedRandom<>();

		for(String part : parts) {
			String[] partArr;

			if(LevelSplitter.def.levelContains(part, "|")) {
				partArr = LevelSplitter.def.levelSplit(part, "||").toArray(new String[0]);
			} else {
				partArr = new String[] {part};
			}

			IPair<IList<CaseElement>, Integer> par = RGrammarParser.parseElementString(partArr);
			int prob = par.getRight();

			for(CaseElement elm : par.getLeft()) {
				elements.addProbability(prob, elm);
			}
		}
	}

	public void generate(GenerationState state) {
		elements.generateValue(state.rnd).generate(state);
	}
}
