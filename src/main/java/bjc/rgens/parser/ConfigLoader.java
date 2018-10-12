package bjc.rgens.parser;

import bjc.utils.funcutils.FileUtils;
import bjc.utils.funcutils.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import bjc.rgens.parser.templates.GrammarTemplate;

import static bjc.rgens.parser.RGrammarLogging.*;

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
		RGrammarSet set = cfgSet.createGSet("default");

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
					System.out.printf("ERROR: Line %s of config set %s (%s)\n", lno, cfgFile, gex.getRootMessage());

					error(gex, "Line %s of config set %s (%s)", lno, cfgFile, gex.getRootMessage());
					gex.printStackTrace();

					System.out.println();
					System.out.println();
				}
			}
		}

		long endCFGTime = System.nanoTime();

		long cfgDur = endCFGTime - startCFGTime;

		perf("Read config file %s in %d ns (%f s)", cfgFile, cfgDur, cfgDur / 1000000000.0);

		return cfgSet;
	}

	private static void loadConfigLine(String[] parts, ConfigSet cfgSet, RGrammarSet set, Path cfgParent) throws IOException {
		/*
		 * Get the place where the tag ID ends
		 */
		if(parts.length < 2) {
			throw new GrammarException("Must specify object tag");
		}
		String tag = parts[1];

		/* 
		 * Get the place where the name of the grammar
		 * ends. 
		 */
		if (parts.length < 3) {
			throw new GrammarException("Must specify a name for a loaded " + tag);
		}
		String name = parts[2]; 

		switch(tag) {
			case "template":
				loadTemplate(name, parts, cfgSet, set, cfgParent);
				break;
			case "subset":
				{
					/* 
					 *@TODO Ben Culkin 9/8/17 :SubsetGrammar
					 *
					 * Implement subset grammars.
					 */
					throw new GrammarException("Sub-grammar sets aren't implemented yet");
				}
			case "gram":
			case "grammar":
				loadGrammar(name, parts, cfgSet, set, cfgParent);
				break;
			case "directory":
				loadDirectory(name, parts, cfgSet, set, cfgParent);
				break;
			default:
				throw new GrammarException(String.format("Unrecognized tag type '%s'", tag));
		}
	}

	private static void loadDirectory(String name, String[] parts, ConfigSet cfgSet, RGrammarSet set, Path cfgParent) throws IOException {
		if(parts.length < 4) {
			throw new GrammarException(String.format("Must specify a path to load directory '%s' from"));
		}

		Path path = Paths.get(parts[3]);

		/*
		 * Convert from configuration relative path to
		 * absolute path.
		 */
		Path dirPath = cfgParent.resolve(path.toString());

		if(!Files.isDirectory(dirPath)) {
			throw new GrammarException(String.format("%s is not a valid directory", dirPath));
		} else {
			FileUtils.traverseDirectory(dirPath, (fle, atts) -> {
				// We want to consider all the files
				return true;
			}, (fle, atts) -> {
				Path normFle = fle.normalize();

				String fleName = normFle.toString();
				
				try {
					if(fleName.endsWith(".gram")) {
						BufferedReader rdr = Files.newBufferedReader(normFle);

						doLoadGrammar(rdr, null, cfgSet, set, dirPath, normFle);
					} else if(fleName.endsWith(".gtpl")) {
						BufferedReader rdr = Files.newBufferedReader(normFle);

						doLoadTemplate(rdr, null, cfgSet, set, dirPath);
					} else if(fleName.endsWith(".class")) {
						// Ignore these
					} else {
						info("Ignoring file '%s' of unknown type", fleName);
					}
				} catch (GrammarException gex) {
					error(gex, "Error loading file %s (%s)", normFle, gex.getRootMessage());
				} catch (IOException ioex) {
					error(ioex, "Error loading file %s", normFle);
				}

				return true;
			});
		}
	}

	private static void doLoadTemplate(BufferedReader rdr, String name, ConfigSet cfgSet, RGrammarSet set, Path convPath) throws IOException {
		String actName;
		
		long startFileTime = System.nanoTime();

		GrammarTemplate template = GrammarTemplate.readTemplate(rdr);
		template.belongsTo = cfgSet;

		if(template.name == null) {
			if(name == null) {
				info("Using default name for template from path '%s'", convPath);

				actName = "default-name";
			} else {
				actName = name;
			}

			info("Naming unnamed template loaded from path %s off config name '%s'",
					convPath, actName);

			template.name = actName;
		}

		rdr.close();

		long endFileTime = System.nanoTime();

		long fileTime = endFileTime - startFileTime;

		perf("Read template %s (from %s) in %d ns (%f s)",
				template.name, convPath, fileTime, fileTime  / 1000000000.0);

		/* Add grammar to the set. */
		cfgSet.templates.put(template.name, template);

		/*
		 * @NOTE
		 *
		 * Do we need to do this for templates?
		 *
		 */
		//Mark where the template came from. 
		//set.loadedFrom.put(template.name, path.toString());
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
			throw new GrammarException(String.format("%s is not a valid grammar file", convPath));
		} else {
			/* Load template file. */
			try {
				BufferedReader fis = Files.newBufferedReader(convPath);
				doLoadTemplate(fis, name, cfgSet, set, convPath);
			} catch (GrammarException gex) {
				String msg = String.format("Error loading template file '%s'", path);
				throw new GrammarException(msg, gex, gex.getRootMessage());
			}
		}
	}

	private static void doLoadGrammar(BufferedReader rdr, String name, ConfigSet cfgSet, RGrammarSet set, Path convPath, Path pth) throws IOException {
		String actName;

		long startFileTime = System.nanoTime();

		RGrammar gram = RGrammarParser.readGrammar(rdr);
		if(gram.name == null) {
			if(name == null) {
				info("Using default name from grammar for '%s'", convPath);

				actName = "default-name";
			} else {
				actName = name;
			}

			info("Naming unnamed grammar loaded from %s off config name '%s'",
					pth, actName);

			gram.name = actName;
		}

		rdr.close();

		long endFileTime = System.nanoTime();

		long fileTime = endFileTime - startFileTime;

		perf("Read grammar %s (from %s) in %d ns (%f s)",
				gram.name, convPath, fileTime, fileTime  / 1000000000.0);

		/* Add grammar to the set. */
		set.addGrammar(gram.name, gram);

		/*
		 * Mark where the grammar came
		 * from. 
		 */
		set.loadedFrom.put(gram.name, pth.toString());
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
			throw new GrammarException(String.format("%s is not a valid grammar file", convPath));
		} else {
			/* Load grammar file. */
			try {
				long startFileTime = System.nanoTime();

				BufferedReader fis = Files.newBufferedReader(convPath);
				doLoadGrammar(fis, name, cfgSet, set, convPath, path);
			} catch(GrammarException gex) {
				String msg = String.format("Error loading grammar '%s'", path);
				throw new GrammarException(msg, gex, gex.getRootMessage());
			} catch (IOException ioex) {
				String msg = String.format("Error loading grammar '%s'", path);
				throw new GrammarException(msg, ioex);
			}
		}
	}
}
