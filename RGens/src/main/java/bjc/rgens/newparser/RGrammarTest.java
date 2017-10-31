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
 */
public class RGrammarTest {
	/**
	 * Main method.
	 *
	 * @param args
	 * 	Unused CLI args.
	 */
	public static void main(String[] args) {
		URL rsc = RGrammarTest.class.getResource("/server-config-sample.cfg");

		try {
			/* Load a grammar set. */
			Path        cfgPath = Paths.get(rsc.toURI());
			RGrammarSet gramSet = RGrammarSet.fromConfigFile(cfgPath);

			/* Generate rule suggestions for all the grammars in the set. */
			for (String gramName : gramSet.getGrammars()) {
				gramSet.getGrammar(gramName).generateSuggestions();
			}

			/* Generate for each exported rule. */
			for (String exportName : gramSet.getExportedRules()) {
				/* Where we loaded the rule from. */
				String loadSrc = gramSet.loadedFrom(gramSet.exportedFrom(exportName));

				System.out.println();
				System.out.printf("Generating for exported rule '%s' from file '%s'\n", exportName, loadSrc);

				RGrammar grammar = gramSet.getExportSource(exportName);
				for (int i = 0; i < 100; i++) {
					try {
						String res = grammar.generate(exportName);
						if(exportName.contains("+")) res = res.replaceAll("\\s+", "");

						if(res.length() > 120) {
							System.out.printf("\t\n\tContents: %s\n\t\n", res);
						} else {
							System.out.printf("\tContents: %s\n", res);
						}
					} catch (GrammarException gex) {
						/* Print out errors with generation. */
						String fmt     = "Error in exported rule '%s' (loaded from '%s')\n";
						
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
