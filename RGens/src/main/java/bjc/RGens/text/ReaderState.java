package bjc.RGens.text;

import java.util.Stack;

import bjc.utils.gen.WeightedGrammar;

public class ReaderState {
	private Stack<WeightedGrammar<String>> wg = new Stack<WeightedGrammar<String>>();

	private String currRule = "";
	
	// Pragma settings
	private boolean isUniform = false;
	
	public void toggleUniformity() {
		isUniform = !isUniform;
	}
	
	public boolean isUniform() {
		return isUniform;
	}
	
	public WeightedGrammar<String> getRules() {
		return wg.peek();
	}
	
	public void setRule(String r) {
		currRule = r;
	}
	
	public String getRule() {
		return currRule;
	}
	
	public void setRules(WeightedGrammar<String> nwg) {
		wg.pop();
		wg.push(nwg);
	}
	
	public void pushGrammar(WeightedGrammar<String> nwg) {
		wg.push(nwg);
	}
	
	public WeightedGrammar<String> popGrammar() {
		return wg.pop();
	}
}
