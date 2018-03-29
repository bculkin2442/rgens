package bjc.rgens.parser;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Get access to the included grammars.
 *
 * @author Ben Culkin
 */
public class RGrammars {
	private static RGrammarSet gramSet;

	private static void loadSet() {
		try {
			URI rsc = RGrammarTest.class.getResource("/server-config-sample.cfg").toURI();

			Map<String, String> env = new HashMap<>();
			env.put("create", "true");
			FileSystem zipfs = FileSystems.newFileSystem(rsc, env);

			Path cfgPath = Paths.get(rsc);

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
