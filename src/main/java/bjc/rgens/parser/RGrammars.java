package bjc.rgens.parser;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
	private static ConfigSet cfgSet;

	private static void loadSet() {
		try {
			URI rsc = RGrammarTest.class.getResource("/server-config-sample.gcfg").toURI();

			Map<String, String> env = new HashMap<>();
			env.put("create", "true");
			/* Ensure we can get at the file we need */
			@SuppressWarnings("unused")
			FileSystem zipfs = FileSystems.newFileSystem(rsc, env);

			Path cfgPath = Paths.get(rsc);

			LoadOptions lopts = new LoadOptions();

			lopts.doPerf  = false;
			lopts.defName = "default";

			cfgSet = ConfigLoader.fromConfigFile(cfgPath, lopts);
		} catch (IOException | URISyntaxException ex) {
			RuntimeException rtex = new RuntimeException("Could not load grammars");

			rtex.initCause(ex);

			throw rtex;
		}
	}

	/**
	 * Generate an exported rule.
	 * 
	 * @param exportName
	 *            The rule to generate.
	 * @return The generated rule
	 * @throws GrammarException
	 *             If something went wrong.
	 */
	public static String generateExport(String exportName) throws GrammarException {
		if (cfgSet == null)
			loadSet();

		for(RGrammarSet gramSet : cfgSet.grammars.values()) {
			if (!gramSet.getExportedRules().contains(exportName)) {
				continue;
			}

			RGrammar gram = gramSet.getExportSource(exportName);

			String res = gram.generate(exportName);
			if (exportName.contains("+"))
				res = res.replaceAll("\\s+", "");

			return res;
		}

		throw new GrammarException(String.format("No exported rule named %s", exportName));
	}
}
