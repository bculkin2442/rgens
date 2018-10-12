package bjc.rgens.parser.elements.vars;

import bjc.utils.funcutils.StringUtils;

import bjc.rgens.parser.GenerationState;
import bjc.rgens.parser.GrammarException;

import java.util.ArrayList;
import java.util.List;

public abstract class VariableElement {
	public boolean forbidSpaces;

	protected VariableElement(boolean forbidSpacing) {
		forbidSpaces = forbidSpacing;
	}

	public abstract void generate(GenerationState state);

	public static List<VariableElement> parseElementString(String varElm) {
		boolean forbidSpaces = StringUtils.levelContains(varElm, "-", "+");

		String[] parts;

		if(forbidSpaces) {
			parts = StringUtils.levelSplit(varElm, true, "-", "+").toArray(new String[0]);
		} else {
			parts = new String[] { varElm };
		}

		return parseElementString(forbidSpaces, parts);
	}

	public static List<VariableElement> parseElementString(boolean forbidSpaces, String... parts) {
		List<VariableElement> elms = new ArrayList<>(parts.length);

		VariableElement prevElement = null;

		for (String npart : parts) {
			// @HACK
			//
			// This is so that inline refs to hypenized rule names
			// work. Not sure this is a good impl. strategy
			String part = npart.replaceAll("\\(|\\)", "");

			VariableElement elm = null;

			if(part.startsWith("$")) {
				elm = new VRefVariableElement(forbidSpaces, part.substring(1));
			} else if (part.startsWith("@")) {
				if(forbidSpaces)
					throw new GrammarException("Arrays references aren't allowed in rule names");

				elm = new ARefVariableElement(part.substring(1));
			} else if (part.startsWith("%")) {
				elm = new RRefVariableElement(forbidSpaces, String.format("[%s]", part.substring(1)));
			} else if (part.startsWith("/")) {
				throw new GrammarException("Template variables aren't implemented yet");
			} else {
				if(prevElement != null && prevElement instanceof LiteralVariableElement) {
					/* Aggregate chain literals together */
					((LiteralVariableElement)prevElement).val += part;
				} else {
					if(part.contains(" ")) {
						elm = new LiteralVariableElement(false, part);
					} else {
						elm = new LiteralVariableElement(true, part);
					}
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
