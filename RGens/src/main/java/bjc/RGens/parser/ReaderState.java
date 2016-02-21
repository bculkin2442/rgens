package bjc.RGens.parser;

import java.util.Stack;

import bjc.utils.gen.WeightedGrammar;

public class ReaderState {
	private String currRule = "";

	// Pragma settings
	private boolean isUniform = false;
	
	private Stack<WeightedGrammar<String>> wg;
	
	public ReaderState() {
		wg = new Stack<WeightedGrammar<String>>();
		wg.push(new WeightedGrammar<>());
	}
	
	public WeightedGrammar<String> currGrammar() {
		return wg.peek();
	}
	
	public String getRule() {
		return currRule;
	}
	
	public WeightedGrammar<String> getRules() {
		return wg.peek();
	}
	
	public boolean isUniform() {
		return isUniform;
	}
	
	public WeightedGrammar<String> popGrammar() {
		return wg.pop();
	}
	
	public void pushGrammar(WeightedGrammar<String> nwg) {
		wg.push(nwg);
	}
	
	public void setRule(String r) {
		currRule = r;
	}
	
	public void setRules(WeightedGrammar<String> nwg) {
		wg.pop();
		wg.push(nwg);
	}
	
	public void toggleUniformity() {
		isUniform = !isUniform;
	}
	
	public void setInitialRule(String rName) {
		wg.peek().setInitRule(rName);
	}
}
