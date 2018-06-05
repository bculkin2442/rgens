package bjc.rgens.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

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
	public static RGrammarSet fromConfigFile(Path cfgFile) throws IOException {
		/* The grammar set to hand back. */
		RGrammarSet set = new RGrammarSet();

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
					if (ln.equals("")) return;

					if (ln.startsWith("#")) return;

					/* Handle mixed whitespace. */
					ln = ln.replaceAll("\\s+", " ");

					/* Get line type */
					int typeIdx = ln.indexOf(" ');
					if(typeIdx == -1) {
						throw new GrammarException("Must specify config line type");
					}
					String type = ln.substring(0, typeIdx).trim();
					ln          = ln.substring(typeIdx).trim();

					switch(type) {
					case "load":
						loadConfigLine(ln, set, cfgParent);
						break;
					default:
						throw new GrammarException("Unknown config line type " + type);
					}
				} catch(GrammarException gex) {
					System.out.printf("ERROR: Line %s of grammar set %s\n", lno, cfgFile);

					System.err.printf("ERROR: Line %s of grammar set %s\n", lno, cfgFile);
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

		return set;
	}

	private static void loadConfigLine(String ln, RGrammarSet set, Path cfgParent) throws IOException {

		/*
		 * Get the place where the tag ID ends
		 */
		int tagIdx = ln.indexOf(" ");
		if(tagIdx == -1) {
			throw new GrammarException("Must specify a tag as to what a line is");
		}
		String tag = ln.substring(0, tagIdx).trim();
		ln         = ln.substring(tagIdx).trim();

		/* 
		 * Get the place where the name of the grammar
		 * ends. 
		 */
		int nameIdx = ln.indexOf(" ");
		if (nameIdx == -1) {
			throw new GrammarException("Must specify a name for a loaded object");
		}
		String name = ln.substring(0, nameIdx);
		ln          = ln.substring(nameIdx).trim();

		switch(tag) {
			case "template":
				{
					throw new GrammarException("Templates aren't implemented yet");
				}
			case "subset":
				{
					/* @TODO implement subset grammars */
					throw new GrammarException("Sub-grammar sets aren't implemented yet");
				}
			case "gram":
			case "grammar":
				{
					Path path = Paths.get(ln);

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
								System.err.printf("\tINFO: Naming unnamed grammar loaded from %s off config name %s\n",
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
						} catch (GrammarException gex) {
							String msg = String.format("Error loading file '%s'", path);
							throw new GrammarException(msg, gex);
						}
					}
				}
				break;
			default:
				String msg = String.format("Unrecognized tag type '%s'", tag);
				throw new GrammarException(msg);
		}
	}
}
