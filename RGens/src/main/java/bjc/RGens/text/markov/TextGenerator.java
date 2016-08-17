package bjc.RGens.text.markov;

import java.io.*;

/**
 * Generate text from a markov model of an input text
 * 
 * @author ben
 *
 */
public class TextGenerator {
	/**
	 * @param args
	 *            when used with three arguments, the first represents the
	 *            k-order of the Markov objects. The second represents the
	 *            number of characters to print out. The third represents
	 *            the file to be read.
	 * 
	 *            When used with two arguments, the first represents the
	 *            k-order of the Markov objects, and the second represents
	 *            the file to be read. The generated text will be the same
	 *            number of characters as the original file.
	 */
	public static void main(String[] args) {
		int k = 0;
		int M = 0;

		String file = "";
		StringBuilder text = new StringBuilder();

		if (args.length == 3) {
			k = Integer.parseInt(args[0]);
			M = Integer.parseInt(args[1]);
			file = args[2];
		} else if (args.length == 2) {
			k = Integer.parseInt(args[0]);
			file = args[1];
		} else {
			System.out
					.println("\n" + "Usage: java TextGenerator k M file");
			System.out.println(
					"where k is the markov order, M is the number");
			System.out.println(
					"of characters to be printed, and file is the");
			System.out.println(
					"name of the file to print from. M may be left out."
							+ "\n");
			System.exit(1);
		}

		try (FileReader reader = new FileReader(file)) {
			StandaloneTextGenerator.generateMarkovMap(k, M, text, reader);
		} catch (FileNotFoundException e) {
			System.out.println("File not found.");
			e.printStackTrace();
			System.exit(1);
		} catch (IOException ioex) {
			System.out.println("IOException");
			ioex.printStackTrace();
			System.exit(1);
		}

		System.out.println(
				text.toString().substring(0, Math.min(M, text.length())));
	}
}