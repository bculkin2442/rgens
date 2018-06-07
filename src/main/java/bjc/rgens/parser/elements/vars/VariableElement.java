package bjc.rgens.parser.elements.vars;

import bjc.rgens.parser.GenerationState;
import bjc.rgens.parser.GrammarException;

import java.util.ArrayList;
import java.util.List;

public abstract class VariableElement {
	public abstract void generate(GenerationState state);

	public static List<VariableElement> parseVariableElements(String varElm) {
		boolean forbidSpaces = varElm.contains("-");

		String[] parts;

		if(forbidSpaces) {
			parts = varElm.split("(?<=[+-])|(?=[+-])");
		} else {
			parts = new String[] { varElm };
		}

		return parseVariableElements(forbidSpaces, parts);
	}

	public static List<VariableElement> parseVariableElements(boolean forbidSpaces, String... parts) {
		List<VariableElement> elms = new ArrayList<>(parts.length);

		VariableElement prevElement = null;

		for (String part : parts) {
			VariableElement elm = null;

			if(part.startsWith("$")) {
				elm = new VRefVariableElement(forbidSpaces, part.substring(1));
			} else if (part.startsWith("@")) {
				elm = new ARefVariableElement(forbidSpaces, part.substring(1));
			} else if (part.startsWith("%")) {
				elm = new RRefVariableElement(forbidSpaces, part.substring(1));
			} else if (part.startsWith("/")) {
				throw new GrammarException("Template variables aren't implemented yet");
			} else {
				if(prevElement instanceof LiteralVariableElement) {
					/* Aggregate chain literals together */
					((LiteralVariableElement)prevElement).val += elm;
				} else {
					elm = new LiteralVariableElement(part);
				}
			}

			if(elm != null) {
				elms.add(elm);
				prevElement = elm;
			}
		}

		return elms;
	}
}
