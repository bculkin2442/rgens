package bjc.rgens.newparser;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

/**
 * Test for new grammar syntax.
 *
 * @author EVE
 *
 */
public class RGrammarTest {
	/**
	 * Main method.
	 *
	 * @param args
	 *                Unused CLI args.
	 */
	public static void main(String[] args) {
		URL rsc = RGrammarTest.class.getResource("/server-config-sample.cfg");

		try {
			RGrammarSet gramSet = RGrammarSet.fromConfigFile(Paths.get(rsc.toURI()));

			for (String gramName : gramSet.getGrammars()) {
				gramSet.getGrammar(gramName).generateSuggestions();
			}

			for (String exportName : gramSet.getExportedRules()) {
				RGrammar grammar = gramSet.getExportSource(exportName);

				for (int i = 0; i < 10; i++) {
					try {
						grammar.generate(exportName);
					} catch (GrammarException gex) {
						System.out.println("Error in exported rule " + exportName
						                   + " (loaded from "
						                   + gramSet.loadedFrom(gramSet.exportedFrom(exportName)));

						System.out.println();

						gex.printStackTrace();

						System.out.println();
						System.out.println();
					}
				}
			}
		} catch (IOException ioex) {
			ioex.printStackTrace();
		} catch (URISyntaxException urisex) {
			urisex.printStackTrace();
		}
	}
}
