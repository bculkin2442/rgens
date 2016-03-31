package bjc.RGens.parser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import bjc.utils.gen.WeightedGrammar;

public class GrammarReaderCLI {
	private static WeightedGrammar<String> wg = null;

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

			try {
				wg = GrammarReader.fromStream(new FileInputStream(fName));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			if (rName.equalsIgnoreCase("--list-rules")) {
				for (String rn : wg.getRuleNames()) {
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
