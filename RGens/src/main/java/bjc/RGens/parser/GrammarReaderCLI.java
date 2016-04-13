package bjc.RGens.parser;

import java.io.FileInputStream;
import java.io.IOException;

import bjc.utils.gen.WeightedGrammar;

/**
 * App that reads a grammar from a file and generates results
 * 
 * @author ben
 *
 */
public class GrammarReaderCLI {
	private static WeightedGrammar<String> wg = null;

	/**
	 * Main application method
	 * 
	 * @param args
	 *            CLI args
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			GrammarReaderApp.main(args);
		} else {
			String fName = args[0];

			if (fName.equalsIgnoreCase("--help")) {
				System.out.println(
						"Usage: java -jar GrammarReader.jar <file-name> <init-rule> <num-res>");
				System.exit(0);
			}

			String rName = args[1];

			try (FileInputStream fStream = new FileInputStream(fName)) {
				wg = GrammarReader.fromStream(fStream);
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (rName.equalsIgnoreCase("--list-rules")) {
				for (String rn : wg.getRuleNames().toIterable()) {
					System.out.println(rn);
				}
				System.exit(0);
			}

			int rCount = Integer.parseInt(args[2]);

			for (int i = 0; i < rCount; i++) {
				String s = wg.generateListValues(rName, " ")
						.reduceAux(new StringBuilder(),
								(strang, strangBuilder) -> strangBuilder
										.append(strang),
								t -> t.toString())
						.replaceAll("\\s+", " ");

				System.out.println(s);
			}
		}
	}
}
