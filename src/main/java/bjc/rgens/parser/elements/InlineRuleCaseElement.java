package bjc.rgens.parser.elements;

import bjc.rgens.parser.GenerationState;
import bjc.rgens.parser.RGrammarParser;


import bjc.data.Tree;
import bjc.data.SimpleTree;

import bjc.utils.gen.WeightedRandom;
import bjc.utils.ioutils.LevelSplitter;

import java.util.ArrayList;
import java.util.List;

/**
 * Case element for an inline rule.
 * 
 * @author Ben Culkin
 *
 */
public class InlineRuleCaseElement extends CaseElement {
	/**
	 * The elements for this case element.
	 */
	public final WeightedRandom<CaseElement> elements;

	/**
	 * Create a new inline rule case element.
	 * 
	 * @param parts The parts of this case element.
	 */
	public InlineRuleCaseElement(String... parts) {
		this(new SimpleTree<>(), parts);
	}

	/**
	 * Create a new inline rule case element.
	 * 
	 * @param errs The place to store errors in.
	 * @param parts The parts of this case element.
	 */
	public InlineRuleCaseElement(Tree<String> errs, String... parts) {
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

	@Override
	public void generate(GenerationState state) {
		elements.generateValue(state.rnd).generate(state);
	}
}
