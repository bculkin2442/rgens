package bjc.rgens.parser;

import bjc.rgens.parser.templates.GrammarTemplate;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

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
		URL rsc = RGrammarTest.class.getResource("/server-config-sample.gcfg");

		try {
			/* Load a grammar set. */
			Path        cfgPath = Paths.get(rsc.toURI());
			ConfigSet   cfgSet  = ConfigLoader.fromConfigFile(cfgPath);

			for(RGrammarSet gramSet : cfgSet.grammars.values()) {
				testGrammarSet(gramSet);
			}

			for(GrammarTemplate template : cfgSet.templates.values()) {
				testTemplate(template, cfgSet.grammars.get("default"));
			}
		} catch (IOException ioex) {
			ioex.printStackTrace();
		} catch (URISyntaxException urisex) {
			urisex.printStackTrace();
		}
	}

	private static void testTemplate(GrammarTemplate template, RGrammarSet set) {
		System.out.printf("Generating for template %s\n", template);

		Random rnd = new Random();

		for(int i = 0; i < 10; i++) {
			GenerationState state = GenerationState.fromGrammar(rnd, set.exportGrammar);

			template.generate(state);

			String res = state.contents.toString();

			if(res.length() > 120) {
				System.out.printf("\t\n\tContents: %s\n\t\n", res);
			} else {
				System.out.printf("\tContents: %s\n", res);
			}
		}
	}

	private static void testGrammarSet(RGrammarSet gramSet) {
		/* Generate rule suggestions for all the grammars in the set. */
		for (String gramName : gramSet.getGrammars()) {
			long startSuggTime = System.nanoTime();

			gramSet.getGrammar(gramName).generateSuggestions();

			long endSuggTime = System.nanoTime();

			long suggDur = endSuggTime - startSuggTime;

			System.err.printf("PERF: Generated rule suggestions for %s in %d ns (%f s)\n", gramName, suggDur, suggDur / 1000000000.0);
		}

		System.err.printf("\n\n");

		/* Generate for each exported rule. */
		for (String exportName : gramSet.getExportedRules()) {
			/* Where we loaded the rule from. */
			String loadSrc = gramSet.loadedFrom(gramSet.exportedFrom(exportName));

			System.out.println();
			System.out.printf("Generating for exported rule '%s' from file '%s'\n", exportName, loadSrc);

			RGrammar grammar = gramSet.getExportSource(exportName);
			long startGenTime = System.nanoTime();
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
					String fmt     = "ERROR: Exported rule %s from %s failed (loaded from '%s')\n";

					System.out.printf(fmt, exportName, grammar.name, loadSrc);
					System.out.println();
					System.out.println();

					System.err.printf(fmt, exportName, grammar.name, loadSrc);
					gex.printStackTrace();

					System.err.println();
					System.err.println();
				}
			}
			long endGenTime = System.nanoTime();

			long genDur = endGenTime - startGenTime;

			System.err.printf("PERF: Generated %s 100 times in %d ns (%f s)\n\n\n", exportName, genDur, genDur / 1000000000.0);
		}
	}
}
