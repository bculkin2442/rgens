package bjc.rgens.parser;

import bjc.utils.data.ITree;
import bjc.utils.data.QueuedIterator;
import bjc.utils.data.Tree;

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

/*
 * @TODO @CLEANUP Ben Culkin 7/21/2019 :LoadingInfo
 * Instead of using the same tree for both storing errors and storing other
 * things, there should be a data structure where we stuff a couple of trees
 * that track things like errors, warnings, information etc.
 *
 * For a counterpoint on the benefits we get from the integrated tree, and the
 * possibility of using our own that stores the stuff we want, see the note
 * below at :ErrorLine
 */

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
		String msg = String.format("INFO: Loading config file from path '%s'", cfgFile);

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

		int lno = 0;
		try(Scanner scn = new Scanner(cfgFile)) {

			while (scn.hasNextLine()) {
				// Execute a line from the configuration file.

				// @NOTE: Do we want to also track logical
				// lines, or just physical ones? - ben,
				// 9/21/2019
				lno += 1;

				// @NOTE: Does this replaceAll need to exist? -
				// ben, 9/21/2019
				String ln = scn.nextLine().trim().replaceAll("\\s+", " ");

				// Ignore blank/comment lines.
				if (ln.equals(""))      continue;
				if (ln.startsWith("#")) continue;

				// @TODO Ben Culkin 9/21/2019 :LineCont
				// We should support some sort of line
				// continuation ability, probably using the '\'
				// since that what UNIX uses in most places
				ITree<String> header = new Tree<>(String.format("INFO: Processing line %d", lno));

				String[] parts = StringUtils.levelSplit(ln, " ").toArray(new String[0]);

				if(parts.length < 1) {
					// Must specify a line type
					//
					// @TODO Ben Culkin 9/21/2019 :ErrorLine
					// Think about whether there is a better
					// way to do tracking of the line number
					// and adding it to the messages we
					// generate.
					//
					// We do need a better way to pass along
					// filename/line number pairs to various
					// parts of the code
					//
					// Actually, do we? As long as we stick
					// with this integrated tree design, and
					// then just print all the nodes on the
					// way to the error, folding away all
					// of the other nodes, it should be just
					// fine. In fact, if we do our own tree
					// impl. (since we don't care that much
					// about any of the features from
					// FunctionalTree, and have our own sort
					// of stuff we want to be tracking in
					// the nodes) it may actually be faster.
					header.addChild("ERROR: Must specify config line type");
				} else {
					String type = parts[0];

					switch(type) {
					case "load": 
						loadConfigLine(parts, lopts, header);
						break;
					default:
						String fmt = String.format("ERROR: Unknown config line type '%s'", type);

						header.addChild(fmt);
					}
				}

				errs.addChild(header);
			}
		}

		long endCFGTime = System.nanoTime();

		long cfgDur = endCFGTime - startCFGTime;

		if (lopts.doPerf) {
			String fmt = String.format("PERF: Read config file (%d lines) from path '%s' in %d ns (%f s)", lno, cfgFile, cfgDur, cfgDur / 1000000000.0);

			errs.addChild(fmt);
		}

		return lopts.cfgSet;
	}

	// Load a line from a config file.
	private static void loadConfigLine(String[] parts, LoadOptions lopts, ITree<String> errs) throws IOException {
		if(parts.length < 2) {
			// Must specify the type of config object you wish to
			// load.
			//
			// @TODO Ben Culkin 9/21/2019 :AutoType
			// Maybe add some sort of support for determining the
			// type of the config object based of the file we want
			// to load it from?
			errs.addChild("ERROR: Must specify type for config object");

			return;
		}

		String tag = parts[1];

		if (parts.length < 3) {
			// All loaded config objects require that a name be
			// bound to them.
			//
			// @TODO Ben Culkin 9/21/2019 :AutoName
			// Perhaps do a thing that auto-picks a plausible name
			// for the object based off of the file name we're
			// loading from?
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

	// Load a 'directory' config object, by recursively attempting to
	// auto-load all of the items in the directory as objects
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
			// provided directory. This will also have the files for
			// the sub-directories added to it as well
			QueuedIterator<File> dirItr = new QueuedIterator<>(dirPath.toFile().listFiles());

			ITree<String> header = new Tree<>(String.format("INFO: Bulk-loading objects from directory '%s'", lopts.parent));

			while (dirItr.hasNext()) {
				File curFile = dirItr.next();

				String fName = curFile.toString();

				ITree<String> kid = new Tree<>(String.format("INFO: Processing object from path '%s'", fName));

				Path oldPar = lopts.parent;

				// Reset the parent to be the directory for the
				// current file.
				lopts.parent = curFile.toPath().getParent();

				try {
					// @NOTE: This can probably be reused
					// for :AutoType and :AutoName, if we
					// abstract parts of it out in some
					// way...
					if (curFile.isDirectory()) {
						dirItr.last(curFile.listFiles());
					} else if (fName.endsWith(".gram")) {
						Reader rdr = new FileReader(curFile);

						doLoadGrammar(rdr, null, lopts, kid, curFile.toPath());
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
					// Reset the parent to its actual value.
					lopts.parent = oldPar;
				}

				header.addChild(kid);
			}

			errs.addChild(header);
		}
	}

	// Actually do the work of loading a 'template' object
	private static void doLoadTemplate(Reader rdr, String name, LoadOptions lopts, ITree<String> errs) throws IOException {
		String actName;
		
		long startFileTime = System.nanoTime();

		GrammarTemplate template = GrammarTemplate.readTemplate(rdr, errs);
		template.belongsTo = lopts.cfgSet;

		if(template.name == null) {
			if(name == null) {
				String fmt = String.format("WARN: Using default name for template");

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

		/* Add template to the set. */
		lopts.cfgSet.templates.put(template.name, template);

		/*
		 * @NOTE
		 *
		 * Do we need to do this for templates?
		 *
		 * Probably, if only to allow for some better diagnostics when
		 * we need them - ben, 9/21/2019
		 */
		//Mark where the template came from. 
		//set.loadedFrom.put(template.name, path.toString());
	}

	// Load a 'template' type grammar object
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

	// Actually load a 'grammar' object
	private static void doLoadGrammar(Reader rdr, String name, LoadOptions lopts, ITree<String> errs, Path convPath) throws IOException {
		String actName;

		long startFileTime = System.nanoTime();

		RGrammar gram = RGrammarParser.readGrammar(rdr, lopts, errs);
		if(gram.name == null) {
			if(name == null) {
				errs.addChild("WARN: Using default name for grammar");

				actName = "default-name";
			} else {
				actName = name;
			}

			//String fmt = String.format("INFO: Naming unnamed grammar off config name '%s'", actName);

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
		lopts.gramSet.loadedFrom.put(gram.name, convPath.toString());
	}

	// Load a 'grammar' object
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
			String fmt = String.format("ERROR: '%s' is not a valid grammar file", convPath);

			errs.addChild(fmt);
		} else {
			Path oldPar = lopts.parent;
			lopts.parent = convPath.getParent();

			try {
				/* Load grammar file. */
				@SuppressWarnings("unused")
				long startFileTime = System.nanoTime();

				Reader rdr = new FileReader(convPath.toFile());

				ITree<String> kid = new Tree<>(String.format("INFO: Loading grammar '%s' from '%s'", name, convPath));

				doLoadGrammar(rdr, name, lopts, kid, convPath);

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
