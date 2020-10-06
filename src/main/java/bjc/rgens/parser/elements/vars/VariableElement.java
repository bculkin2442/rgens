package bjc.rgens.parser.elements.vars;

import java.util.ArrayList;
import java.util.List;

import bjc.rgens.parser.GenerationState;
import bjc.rgens.parser.GrammarException;
import bjc.utils.ioutils.LevelSplitter;

/**
 * Case element which references a variable.
 * 
 * @author Ben Culkin
 *
 */
public abstract class VariableElement {
	/**
	 * Whether or not to forbid spaces in this element.
	 */
	public boolean forbidSpaces;

	/**
	 * Create a new variable element.
	 * 
	 * @param forbidSpacing
	 *                      Whether spacing should be forbidden in this element.
	 */
	protected VariableElement(boolean forbidSpacing) {
		forbidSpaces = forbidSpacing;
	}

	/**
	 * Generate this element.
	 * 
	 * @param state
	 *              The state of generation.
	 */
	public abstract void generate(GenerationState state);

	/**
	 * Parse a variable element from a string.
	 * 
	 * @param varElm
	 *               The string to parse.
	 * 
	 * @return The variable elements which make up the string.
	 */
	public static List<VariableElement> parseElementString(String varElm) {
		boolean forbidSpaces = LevelSplitter.def.levelContains(varElm, "-", "+");

		String[] parts;

		if (forbidSpaces) {
			parts = LevelSplitter.def.levelSplit(varElm, true, "-", "+")
					.toArray(new String[0]);
		} else {
			parts = new String[] {
					varElm
			};
		}

		return parseElementString(forbidSpaces, parts);
	}

	/**
	 * Parse a string of variable elements.
	 * 
	 * @param forbidSpaces
	 *                     Whether or not to forbid spacing in this variable.
	 * @param parts
	 *                     The parts to parse into variable elements.
	 * 
	 * @return The variable elements from the string.
	 */
	public static List<VariableElement> parseElementString(boolean forbidSpaces,
			String... parts) {
		List<VariableElement> elms = new ArrayList<>(parts.length);

		VariableElement prevElement = null;

		for (String npart : parts) {
			// @HACK
			//
			// This is so that inline refs to hypenized rule names
			// work. Not sure this is a good impl. strategy
			String part = npart.replaceAll("\\(|\\)", "");

			VariableElement elm = null;

			if (part.startsWith("$")) {
				elm = new VRefVariableElement(forbidSpaces, part.substring(1));
			} else if (part.startsWith("@")) {
				if (forbidSpaces)
					throw new GrammarException(
							"Arrays references aren't allowed in rule names");

				elm = new ARefVariableElement(part.substring(1));
			} else if (part.startsWith("%")) {
				elm = new RRefVariableElement(forbidSpaces,
						String.format("[%s]", part.substring(1)));
			} else if (part.startsWith("/")) {
				throw new GrammarException("Template variables aren't implemented yet");
			} else {
				if (prevElement != null
						&& prevElement instanceof LiteralVariableElement) {
					/* Aggregate chain literals together */
					((LiteralVariableElement) prevElement).val += part;
				} else {
					if (part.contains(" ")) {
						elm = new LiteralVariableElement(false, part);
					} else {
						elm = new LiteralVariableElement(true, part);
					}
				}
			}

			if (elm != null) {
				elms.add(elm);

				prevElement = elm;
			}
		}

		return elms;
	}
}
