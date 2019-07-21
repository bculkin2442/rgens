package bjc.rgens.parser.elements;

import bjc.rgens.parser.GenerationState;
import bjc.rgens.parser.RGrammarParser;

import bjc.utils.data.IPair;
import bjc.utils.funcdata.IList;


import bjc.utils.data.ITree;
import bjc.utils.data.Tree;
import bjc.utils.funcdata.FunctionalList;
import bjc.utils.funcutils.StringUtils;

import bjc.utils.gen.WeightedRandom;
import bjc.utils.ioutils.LevelSplitter;

import java.util.ArrayList;
import java.util.List;

public class InlineRuleCaseElement extends CaseElement {
	public final WeightedRandom<CaseElement> elements;

	public InlineRuleCaseElement(String... parts) {
		this(new Tree<>(), parts);
	}

	public InlineRuleCaseElement(ITree<String> errs, String... parts) {
		super(true);

		this.elements = new WeightedRandom<>();

		for(String part : parts) {
			String[] partArr;

			if(LevelSplitter.def.levelContains(part, "|")) {
				partArr = LevelSplitter.def.levelSplit(part, "||").toArray(new String[0]);
			} else {
				partArr = new String[] {part};
			}

			List<CaseElement> elms = new ArrayList<>();
			int prob = RGrammarParser.parseElementString(partArr, elms, errs);

			for(CaseElement elm : elms) {
				elements.addProbability(prob, elm);
			}
		}
	}

	public void generate(GenerationState state) {
		elements.generateValue(state.rnd).generate(state);
	}
}
