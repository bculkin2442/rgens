package bjc.rgens.parser;

import bjc.utils.funcutils.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import bjc.rgens.parser.templates.GrammarTemplate;

public class ConfigLoader {
	/**
	 * Load a grammar set from a configuration file.
	 *
	 * @param cfgFile
	 * 	The configuration file to load from.
	 *
	 * @return
	 * 	The grammar set created by the configuration file.
	 *
	 * @throws IOException
	 * 	If something goes wrong during configuration loading.
	 */
	public static ConfigSet fromConfigFile(Path cfgFile) throws IOException {
		ConfigSet cfgSet = new ConfigSet();

		/* The grammar set we're parsing into. */
		RGrammarSet set = new RGrammarSet();
		cfgSet.grammars.put("default", set);
		set.belongsTo = cfgSet;
		set.name = "default";

		long startCFGTime = System.nanoTime();

		/* Get the directory that contains the config file. */
		Path cfgParent = cfgFile.getParent();

		try(Scanner scn = new Scanner(cfgFile)) {
			int lno = 0;

			/* Execute lines from the configuration file. */
			while (scn.hasNextLine()) {
				String ln = scn.nextLine().trim();

				lno += 1;

				try {
					/* Ignore blank/comment lines. */
					if (ln.equals("")) continue;

					if (ln.startsWith("#")) continue;

					ln = ln.replaceAll("\\s+", " ");
					String[] parts = StringUtils.levelSplit(ln, " ").toArray(new String[0]);

					/* Get line type */
					if(parts.length < 1) {
						throw new GrammarException("Must specify config line type");
					}
					String type = parts[0];

					switch(type) {
					case "load":
						loadConfigLine(parts, cfgSet, set, cfgParent);
						break;
					default:
						throw new GrammarException("Unknown config line type " + type);
					}
				} catch(GrammarException gex) {
					System.out.printf("ERROR: Line %s of config set %s\n", lno, cfgFile);

					System.err.printf("ERROR: Line %s of config set %s\n", lno, cfgFile);
					gex.printStackTrace();

					System.out.println();
					System.out.println();

					System.err.println();
					System.err.println();
				}
			}
		}

		long endCFGTime = System.nanoTime();

		long cfgDur = endCFGTime - startCFGTime;

		System.err.printf("\n\nPERF: Read config file %s in %d ns (%f s)\n", cfgFile, cfgDur, cfgDur / 1000000000.0);

		return cfgSet;
	}

	private static void loadConfigLine(String[] parts, ConfigSet cfgSet, RGrammarSet set, Path cfgParent) throws IOException {
		/*
		 * Get the place where the tag ID ends
		 */
		if(parts.length < 2) {
			throw new GrammarException("Must specify a tag as to what a line is");
		}
		String tag = parts[1];

		/* 
		 * Get the place where the name of the grammar
		 * ends. 
		 */
		if (parts.length < 3) {
			throw new GrammarException("Must specify a name for a loaded object");
		}
		String name = parts[2]; 

		switch(tag) {
			case "template":
				loadTemplate(name, parts, cfgSet, set, cfgParent);
				break;
			case "subset":
				{
					/* @TODO implement subset grammars */
					throw new GrammarException("Sub-grammar sets aren't implemented yet");
				}
			case "gram":
			case "grammar":
				loadGrammar(name, parts, cfgSet, set, cfgParent);
				break;
			default:
				String msg = String.format("Unrecognized tag type '%s'", tag);
				throw new GrammarException(msg);
		}
	}

	private static void loadTemplate(String name, String[] parts, ConfigSet cfgSet, RGrammarSet set, Path cfgParent) throws IOException {
		if(parts.length < 4) {
			throw new GrammarException(String.format("Must specify a path to load template '%s' from", name));
		}

		Path path = Paths.get(parts[3]);

		/*
		 * Convert from configuration relative path to
		 * absolute path.
		 */
		Path convPath = cfgParent.resolve(path.toString());

		if(Files.isDirectory(convPath)) {
			throw new GrammarException("Can't load grammar from directory" + convPath.toString());
		} else {
			/* Load template file. */
			try {
				long startFileTime = System.nanoTime();

				BufferedReader fis = Files.newBufferedReader(convPath);
				GrammarTemplate template = GrammarTemplate.readTemplate(fis);
				template.belongsTo = cfgSet;

				if(template.name == null) {
					System.err.printf("\tINFO: Naming unnamed template loaded from %s off config name '%s'\n",
							convPath, name);

					template.name = name;
				}

				fis.close();

				long endFileTime = System.nanoTime();

				long fileTime = endFileTime - startFileTime;

				System.err.printf("\tPERF: Read template %s (from %s) in %d ns (%f s)\n",
						template.name, convPath, fileTime, fileTime  / 1000000000.0);

				/* Add grammar to the set. */
				cfgSet.templates.put(name, template);

				/*
				 * @NOTE
				 *
				 * Do we need to do this
				 * for templates?
				 *
				 * Mark where the
				 * template came
				 * from. 
				 */
				//set.loadedFrom.put(name, path.toString());
			} catch (GrammarException gex) {
				String msg = String.format("Error loading template file '%s'", path);
				throw new GrammarException(msg, gex);
			}
		}
	}

	private static void loadGrammar(String name, String[] parts, ConfigSet cfgSet, RGrammarSet set, Path cfgParent) throws IOException {
		if(parts.length < 4) {
			throw new GrammarException(String.format("Must provide a path to load grammar '%s' from", name));
		}

		Path path = Paths.get(parts[3]);

		/*
		 * Convert from configuration relative path to
		 * absolute path.
		 */
		Path convPath = cfgParent.resolve(path.toString());

		if(Files.isDirectory(convPath)) {
			throw new GrammarException("Can't load grammar from directory" + convPath.toString());
		} else {
			/* Load grammar file. */
			try {
				long startFileTime = System.nanoTime();

				BufferedReader fis = Files.newBufferedReader(convPath);
				RGrammar gram = RGrammarParser.readGrammar(fis);
				if(gram.name == null) {
					System.err.printf("\tINFO: Naming unnamed grammar loaded from %s off config name '%s'\n",
							convPath, name);

					gram.name = name;
				}

				fis.close();

				long endFileTime = System.nanoTime();

				long fileTime = endFileTime - startFileTime;

				System.err.printf("\tPERF: Read grammar %s (from %s) in %d ns (%f s)\n",
						gram.name, convPath, fileTime, fileTime  / 1000000000.0);

				/* Add grammar to the set. */
				set.addGrammar(name, gram);

				/*
				 * Mark where the grammar came
				 * from. 
				 */
				set.loadedFrom.put(name, path.toString());
			} catch (IOException | GrammarException ex) {
				String msg = String.format("Error loading grammar '%s'", path);
				throw new GrammarException(msg, ex);
			}
		}
	}
}
