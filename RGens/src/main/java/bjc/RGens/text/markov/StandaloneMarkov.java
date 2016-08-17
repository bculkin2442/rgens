package bjc.RGens.text.markov;

import java.util.Map;

public class StandaloneMarkov {
	private int					k;

	private Map<String, Markov>	markovHash;
	private String				firstSub;

	public StandaloneMarkov(int k, Map<String, Markov> markovHash,
			String firstSub) {
		this.k = k;
		this.markovHash = markovHash;
		this.firstSub = firstSub;
	}

	public String generateTextFromMarkov(int M) {
		StringBuilder text = new StringBuilder();
		for (int i = k; i < M; i++) {
			if (i == k) {
				text.append(firstSub);

				if (text.length() > k)
					i = text.length();
			}

			String sub = text.substring((i - k), (i));
			Markov tmp = markovHash.get(sub);

			if (tmp != null) {
				Character nextChar = tmp.random();
				text.append(nextChar);
			} else {
				i = k - 1;
			}
		}

		return text.toString();
	}

}
