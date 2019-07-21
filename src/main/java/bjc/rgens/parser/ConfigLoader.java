package bjc.rgens.parser;

import static bjc.rgens.parser.RGrammarLogging.error;
import static bjc.rgens.parser.RGrammarLogging.info;
import static bjc.rgens.parser.RGrammarLogging.perf;

import bjc.utils.data.ITree;
import bjc.utils.data.QueuedIterator;
import bjc.utils.data.Tree;

import bjc.utils.funcutils.FileUtils;
import bjc.utils.funcutils.IteratorUtils;
import bjc.utils.funcutils.StringUtils;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Scanner;

import bjc.rgens.parser.templates.GrammarTemplate;
import bjc.utils.funcutils.FileUtils;
import bjc.utils.ioutils.LevelSplitter;

/**
 * Class that performs loading of grammar sets from config files.
 *
 * @author Ben Culkin
 */
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
	public static ConfigSet fromConfigFile(Path cfgFile, LoadOptions lopts) throws IOException {
		String msg = String.format("INFO: Loading config file %s", cfgFile);

		ITree<String> errTree = new Tree<>(msg);

		return fromConfigFile(cfgFile, lopts, errTree);
	}

	/**
	 * Load a grammar set from a configuration file.
	 *
	 * @param cfgFile
	 * 	The configuration file to load from.
	 *
	 * @param errs
	 *	A place to add errors that occur during loading.
	 *
	 * @return
	 * 	The grammar set created by the configuration file.
	 *
	 * @throws IOException
	 * 	If something goes wrong during configuration loading.
	 */
	public static ConfigSet fromConfigFile(Path cfgFile, LoadOptions lopts, ITree<String> errs) throws IOException {
		lopts.cfgFile = cfgFile;
		lopts.cfgSet  = new ConfigSet();

		/* The grammar set we're parsing into. */
		lopts.gramSet = lopts.cfgSet.createGSet(lopts.defName);

		long startCFGTime = System.nanoTime();

		// Get the directory that contains the config file.
		if (lopts.parent == null) lopts.parent = cfgFile.getParent();

		try(Scanner scn = new Scanner(cfgFile)) {
			int lno = 0;

			while (scn.hasNextLine()) {
				// Execute a line from the configuration file.
				lno += 1;

				String ln = scn.nextLine().trim().replaceAll("\\s+", " ");

				// Ignore blank/comment lines.
				if (ln.equals(""))      continue;
				if (ln.startsWith("#")) continue;

				ITree<String> header = new Tree<>(String.format("INFO: Processed line %d", lno));

				String[] parts = StringUtils.levelSplit(ln, " ").toArray(new String[0]);

				if(parts.length < 1) {
					// Must specify a line type
					header.addChild("ERROR: Must specify config line type");
				} else {
					String type = parts[0];

					switch(type) {
					case "load": 
						loadConfigLine(parts, lopts, header);
						break;
					default:
						String fmt = String.format("ERROR: Unknown config line type %s", type);

						header.addChild(fmt);
					}
				}

				errs.addChild(header);
			}
		}

		long endCFGTime = System.nanoTime();

		long cfgDur = endCFGTime - startCFGTime;

		if (lopts.doPerf) {
			String fmt = String.format("PERF: Read config file %s in %d ns (%f s)", cfgFile, cfgDur, cfgDur / 1000000000.0);

			errs.addChild(fmt);
		}

		return lopts.cfgSet;
	}

	private static void loadConfigLine(String[] parts, LoadOptions lopts, ITree<String> errs) throws IOException {
		if(parts.length < 2) {
			// Must specify an object type
			errs.addChild("ERROR: Must specify type for config object");

			return;
		}

		String tag = parts[1];

		if (parts.length < 3) {
			// Must specify an object name
			String fmt = String.format("ERROR: Must specify a name for config object of type '%s'", tag);

			errs.addChild(fmt);

			return;
		}

		String name = parts[2]; 

		switch(tag) {
			case "template":
				loadTemplate(name, parts, lopts, errs);
				break;
			case "subset":
				{
					String fmt = String.format("ERROR: Sub-grammar sets aren't implemented yet");

					/* 
					 * @TODO Ben Culkin 9/8/17 :SubsetGrammar
					 * Implement subset grammars.
					 *
					 */
					errs.addChild(fmt);
				}
			case "gram":
			case "grammar":
				loadGrammar(name, parts, lopts, errs);
				break;
			case "directory":
				loadDirectory(name, parts, lopts, errs);
				break;
			default:
				String fmt = String.format("ERROR: Unrecognized config object type '%s'", tag);

				errs.addChild(fmt);
		}
	}

	private static void loadDirectory(String name, String[] parts, LoadOptions lopts, ITree<String> errs) throws IOException {
		if(parts.length < 4) {
			String fmt = String.format("ERROR: Must specify a path to load directory '%s' from", name);

			errs.addChild(fmt);

			return;
		}

		Path path = Paths.get(parts[3]);

		/*
		 * Convert from configuration relative path to
		 * absolute path.
		 */
		Path dirPath = lopts.parent.resolve(path.toString());

		if(!Files.isDirectory(dirPath)) {
			String fmt = String.format("ERROR: '%s' is not a valid directory", dirPath);

			errs.addChild(fmt);
		} else {
			// Create an iterator over all of the files in the
			// provided directory
			QueuedIterator<File> dirItr = new QueuedIterator<>(dirPath.toFile().listFiles());

			ITree<String> header = new Tree<>(String.format("INFO: Bulk-loading files from directory '%s'", lopts.parent));

			while (dirItr.hasNext()) {
				File curFile = dirItr.next();

				String fName = curFile.toString();

				ITree<String> kid = new Tree<>(String.format("INFO: Processing file '%s'", fName));

				Path oldPar = lopts.parent;
				lopts.parent = curFile.toPath().getParent();

				try {
					if (curFile.isDirectory()) {
						dirItr.last(curFile.listFiles());
					} else if (fName.endsWith(".gram")) {
						Reader rdr = new FileReader(curFile);

						doLoadGrammar(rdr, null, lopts, kid);
					} else if (fName.endsWith(".gtpl")) {
						Reader rdr = new FileReader(curFile);

						doLoadTemplate(rdr, null, lopts, kid);
					} else if (fName.endsWith(".class")) {
						// These get ignored
					} else {
						String fmt = String.format("WARN: Ignoring unknown type of file '%s'");

						kid.addChild(fmt);

					}
				} catch (IOException ioex) {
					kid.addChild("ERROR: " + ioex.getMessage());
				} finally {
					lopts.parent = oldPar;
				}

				header.addChild(kid);
			}

			errs.addChild(header);
		}
	}

	private static void doLoadTemplate(Reader rdr, String name, LoadOptions lopts, ITree<String> errs) throws IOException {
		String actName;
		
		long startFileTime = System.nanoTime();

		GrammarTemplate template = GrammarTemplate.readTemplate(rdr, errs);
		template.belongsTo = lopts.cfgSet;

		if(template.name == null) {
			if(name == null) {
				String fmt = String.format("INFO: Using default name for template");

				errs.addChild(fmt);

				actName = "default-name";
			} else {
				actName = name;
			}

			String fmt = String.format("INFO: Naming unnamed template off name '%s' specified in config", actName);
			errs.addChild(fmt);

			template.name = actName;
		}

		rdr.close();

		long endFileTime = System.nanoTime();

		long fileTime = endFileTime - startFileTime;

		if (lopts.doPerf) {
			String fmt = String.format("PERF: Read template %s in %d ns (%f s)", template.name, fileTime, fileTime  / 1000000000.0);

			errs.addChild(fmt);
		}

		/* Add grammar to the set. */
		lopts.cfgSet.templates.put(template.name, template);

		/*
		 * @NOTE
		 *
		 * Do we need to do this for templates?
		 *
		 */
		//Mark where the template came from. 
		//set.loadedFrom.put(template.name, path.toString());
	}

	private static void loadTemplate(String name, String[] parts, LoadOptions lopts, ITree<String> errs) throws IOException {
		if(parts.length < 4) {
			String fmt = String.format("ERROR: Must specify a path to load template '%s' from", name);

			errs.addChild(fmt);

			return;
		}

		Path path = Paths.get(parts[3]);

		/*
		 * Convert from configuration relative path to
		 * absolute path.
		 */
		Path convPath = lopts.parent.resolve(path.toString());

		if(!Files.exists(convPath) || Files.isDirectory(convPath)) {
			String fmt = String.format("ERROR: '%s' is not a valid grammar file", convPath);

			errs.addChild(fmt);
		} else {
			/* Load template file. */
			Reader rdr = new FileReader(convPath.toFile());

			String fmt        = String.format("INFO: Loading template '%s' from '%s'", name, convPath);
			ITree<String> kid = new Tree<>(fmt);

			Path oldPar  = lopts.parent;
			lopts.parent = convPath.getParent();

			try {
				doLoadTemplate(rdr, name, lopts, kid);
			} finally {
				lopts.parent = oldPar;
			}

			errs.addChild(kid);
		}
	}

	private static void doLoadGrammar(Reader rdr, String name, LoadOptions lopts, ITree<String> errs) throws IOException {
		String actName;

		long startFileTime = System.nanoTime();

		RGrammar gram = RGrammarParser.readGrammar(rdr, lopts, errs);
		if(gram.name == null) {
			if(name == null) {
				errs.addChild("INFO: Using default name for grammar");

				actName = "default-name";
			} else {
				actName = name;
			}

			String fmt = String.format("Naming unnamed grammar off config name '%s'", actName);

			gram.name = actName;
		}

		rdr.close();

		long endFileTime = System.nanoTime();

		long fileTime = endFileTime - startFileTime;

		if (lopts.doPerf) {
			String fmt = String.format("PERF: Read grammar %s in %d ns (%f s)", gram.name, fileTime, fileTime  / 1000000000.0);

			errs.addChild(fmt);
		}

		/* Add grammar to the set. */
		lopts.gramSet.addGrammar(gram.name, gram);

		/*
		 * Mark where the grammar came
		 * from. 
		 */
		lopts.gramSet.loadedFrom.put(gram.name, lopts.parent.toString());
	}

	private static void loadGrammar(String name, String[] parts, LoadOptions lopts, ITree<String> errs) throws IOException {
		if(parts.length < 4) {
			String fmt = String.format("ERROR: Must provide a path to load grammar '%s' from", name);

			errs.addChild(fmt);

			return;
		}

		Path path = Paths.get(parts[3]);

		/*
		 * Convert from configuration relative path to
		 * absolute path.
		 */
		Path convPath = lopts.parent.resolve(path.toString());

		if(!Files.exists(convPath) || Files.isDirectory(convPath)) {
			String fmt = String.format("ERROR: %s is not a valid grammar file", convPath);

			errs.addChild(fmt);
		} else {
			Path oldPar = lopts.parent;
			lopts.parent = convPath.getParent();

			try {
				/* Load grammar file. */
				long startFileTime = System.nanoTime();

				Reader rdr = new FileReader(convPath.toFile());

				ITree<String> kid = new Tree<>(String.format("INFO: Loading grammar '%s' from '%s'", name, convPath));
				doLoadGrammar(rdr, name, lopts, kid);

				errs.addChild(kid);
			} catch (IOException ioex) {
				String msg = String.format("ERROR: %s", ioex.getMessage());

				errs.addChild(msg);
			} finally {
				lopts.parent = oldPar;
			}
		}
	}
}
