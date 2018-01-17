package bjc.rgens.newparser;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Get access to the included grammars.
 *
 * @author Ben Culkin
 */
public class RGrammars {
	private static RGrammarSet gramSet;

	private static void loadSet() {
		URL rsc = RGrammarTest.class.getResource("/server-config-sample.cfg");

		try {
			Path cfgPath = Paths.get(rsc.toURI());

			gramSet = RGrammarSet.fromConfigFile(cfgPath);
		} catch (IOException | URISyntaxException ex) {
			RuntimeException rtex = new RuntimeException("Could not load grammars");

			rtex.initCause(ex);

			throw rtex;
		}
	}

	public static String generateExport(String exportName) throws GrammarException {
		if(gramSet == null) loadSet();

		if(!gramSet.getExportedRules().contains(exportName)) {
			throw new GrammarException(String.format("No built-in rule named %s", exportName));
		}

		RGrammar gram = gramSet.getExportSource(exportName);

		String res = gram.generate(exportName);
		if(exportName.contains("+")) res = res.replaceAll("\\s+", "");

		return res;
	}
}
