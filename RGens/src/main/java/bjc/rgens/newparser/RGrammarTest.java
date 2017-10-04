package bjc.rgens.newparser;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
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
			/* Load a grammar set. */
			Path cfgPath        = Paths.get(rsc.toURI());
			RGrammarSet gramSet = RGrammarSet.fromConfigFile(cfgPath);

			/* Generate rule suggestions for all the grammars in the set. */
			for (String gramName : gramSet.getGrammars()) {
				gramSet.getGrammar(gramName).generateSuggestions();
			}

			/* Generate for each exported rule. */
			for (String exportName : gramSet.getExportedRules()) {
				RGrammar grammar = gramSet.getExportSource(exportName);

				for (int i = 0; i < 10; i++) {
					try {
						System.out.printf("Generating for exported rule '%s'\n", exportName);
						String res = grammar.generate(exportName);
						System.out.printf("\tContents: %s\n", res);
					} catch (GrammarException gex) {
						/* 
						 * Print out errors with generation.
						 */
						String fmt     = "Error in exported rule '%s' (loaded from '%s')\n";
						String loadSrc = gramSet.loadedFrom(gramSet.exportedFrom(exportName));
						
						System.out.printf(fmt, exportName, loadSrc);

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
