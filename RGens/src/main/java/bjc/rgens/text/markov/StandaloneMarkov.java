package bjc.rgens.text.markov;

import java.util.Map;

/**
 * A standalone Markov generator.
 *
 * @author bjculkin
 *
 */
public class StandaloneMarkov {
	private int ord;

	private Map<String, Markov>     hash;
	private String                  first;

	/**
	 * Create a new standalone Markov generator.
	 *
	 * @param order
	 *                The order of this generator.
	 *
	 * @param markovHash
	 *                The generators to use.
	 *
	 * @param firstSub
	 *                The string to start out with.
	 */
	public StandaloneMarkov(int order, Map<String, Markov> markovHash, String firstSub) {
		ord = order;
		hash = markovHash;
		first = firstSub;
	}

	/**
	 * Generate random text from the markov generator.
	 *
	 * @param charsToGenerate
	 *                The number of characters of text to generate.
	 *
	 * @return The randomly generate text.
	 */
	public String generateTextFromMarkov(int charsToGenerate) {
		StringBuilder text = new StringBuilder();

		for (int i = ord; i < charsToGenerate; i++) {
			if (i == ord) {
				text.append(first);

				if (text.length() > ord) i = text.length();
			}

			String sub = text.substring(i - ord, i);
			Markov tmp = hash.get(sub);

			if (tmp != null) {
				Character nextChar = tmp.random();

				text.append(nextChar);
			} else {
				i = ord - 1;
			}
		}

		return text.toString();
	}
}
