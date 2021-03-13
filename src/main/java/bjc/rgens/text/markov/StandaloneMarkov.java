package bjc.rgens.text.markov;

import java.io.*;
import java.util.*;

/**
 * A standalone Markov generator.
 *
 * @author bjculkin
 */
public class StandaloneMarkov {
	/* The order of the generator. */
	private int ord;

	/* The generators to use. */
	private Map<String, Markov>     hash;
	/* The initial string.    */
	private String                  first;

	/**
	 * Create a new standalone Markov generator.
	 *
	 * @param order
	 * 	The order of this generator.
	 *
	 * @param markovHash
	 * 	The generators to use.
	 *
	 * @param firstSub
	 * 	The string to start out with.
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
	 * 	The number of characters of text to generate.
	 *
	 * @return
	 * 	The randomly generate text.
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

    /**
     * Build a markov generator from a provided source.
     *
     * @param order
     * 	The markov order to use.
     *
     * @param reader
     * 	The source to seed the generator from.
     *
     * @return
     * 	The markov generator for the provided text.
     */
    public static StandaloneMarkov generateMarkovMap(int order, Reader reader) {
    	Map<String, Markov> hash = new HashMap<>();
    
    	Character next = null;
    
    	try {
    		next = (char) reader.read();
    	} catch (IOException e1) {
    		System.out.println("IOException in stepping through the reader");
    
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
    			System.out.println("IOException in stepping through the reader");
    
    			e.printStackTrace();
    		}
    
    	}
    
    	String origFile = origFileBuffer.toString();
    	String firstSub = origFile.substring(0, order);
    
    	for (int i = 0; i < origFile.length() - order; i++) {
    		String sub = origFile.substring(i, i + order);
    		Character suffix = origFile.charAt(i + order);
    
    		if (hash.containsKey(sub)) {
    			Markov marvin = hash.get(sub);
    			marvin.add(suffix);
    			hash.put(sub, marvin);
    		} else {
    			Markov marvin = new Markov(sub, suffix);
    			hash.put(sub, marvin);
    		}
    	}
    
    	return new StandaloneMarkov(order, hash, firstSub);
    }
}
