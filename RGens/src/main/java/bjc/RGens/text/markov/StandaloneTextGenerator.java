package bjc.RGens.text.markov;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class StandaloneTextGenerator {

	/**
	 * Build a markov generator from a provided source
	 * 
	 * @param k
	 * @param M
	 * @param text
	 * @param reader
	 * @return The markov generator for the provided text
	 */
	public static StandaloneMarkov generateMarkovMap(int k, int M,
			StringBuilder text, Reader reader) {
		Map<String, Markov> hash = new HashMap<>();

		Character next = null;

		try {
			next = (char) reader.read();
		} catch (IOException e1) {
			System.out
					.println("IOException in stepping through the reader");
			e1.printStackTrace();
			System.exit(1);
		}

		StringBuilder origFileBuffer = new StringBuilder();

		while (next != null && Character.isDefined(next)) {
			Character.toString(next);
			origFileBuffer.append(next);

			try {
				next = (char) reader.read();
			} catch (IOException e) {
				System.out.println(
						"IOException in stepping through the file");
				e.printStackTrace();
			}

		}

		String origFile = origFileBuffer.toString();
		String firstSub = origFile.substring(0, k);

		for (int i = 0; i < origFile.length() - k; i++) {
			String sub = origFile.substring(i, i + k);
			Character suffix = origFile.charAt(i + k);

			if (hash.containsKey(sub)) {
				Markov marvin = hash.get(sub);
				marvin.add(suffix);
				hash.put(sub, marvin);
			} else {
				Markov marvin = new Markov(sub, suffix);
				hash.put(sub, marvin);
			}
		}

		StandaloneMarkov markovGen = new StandaloneMarkov(k, hash, firstSub);
		
		text.append(markovGen.generateTextFromMarkov(M));

		return markovGen;
	}

}
