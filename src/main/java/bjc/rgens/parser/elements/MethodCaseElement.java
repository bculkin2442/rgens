package bjc.rgens.parser.elements;

import bjc.rgens.parser.GenerationState;
import bjc.rgens.parser.GrammarException;
import bjc.rgens.parser.elements.CaseElement;
import bjc.rgens.parser.methods.MethodElement;

import java.util.List;
import java.util.LinkedList;

public class MethodCaseElement extends CaseElement {
	public CaseElement base;

	public List<MethodElement> methods;

	public MethodCaseElement(CaseElement base, String... rawMethods) {
		this(base, new MethodElement[0]);	

		// @TODO
		//
		// Implement this
	}

	public MethodCaseElement(CaseElement base, MethodElement... rawMethods) {
		this.base = base;

		methods = new LinkedList<>();
		
		for(MethodElement elm : rawMethods) {
			methods.add(rawMethods);
		}
	}

	public void generate(GenerationState state) {
		base.generate(state);

		for(MethodElement method : methods) {
			method.call(state);
		}
	}
}