package bjc.RGens.parser;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;

import bjc.utils.funcutils.ListUtils;
import bjc.utils.gen.WeightedGrammar;

/**
 * App that reads a grammar from a file and generates results
 * 
 * @author ben
 *
 */
public class GrammarReaderCLI {
	private static WeightedGrammar<String> grammar = null;

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

			String ruleName = args[1];

			try (FileInputStream fStream = new FileInputStream(fName)) {
				grammar = RBGrammarReader.fromPath(Paths.get(fName, ""));
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (ruleName.equalsIgnoreCase("--list-rules")) {
				grammar.getRuleNames().forEach(System.out::println);

				System.exit(0);
			}

			int rCount = Integer.parseInt(args[2]);

			for (int i = 0; i < rCount; i++) {
				String ruleResult = ListUtils.collapseTokens(
						grammar.generateListValues(ruleName, " "));

				System.out.println(ruleResult.replaceAll("\\s+", " "));
			}
		}
	}
}
