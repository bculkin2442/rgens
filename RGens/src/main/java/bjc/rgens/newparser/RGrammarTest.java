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

			for(String exportName : gramSet.getExportedRules()) {
				RGrammar grammar = gramSet.getExportSource(exportName);

				grammar.generate(exportName);
			}
		} catch(IOException ioex) {
			ioex.printStackTrace();
		} catch(URISyntaxException urisex) {
			urisex.printStackTrace();
		}
	}
}
