package bjc.rgens.newparser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * Represents a set of grammars that can share rules via exports.
 *
 * @author EVE
 */
public class RGrammarSet {
	/* Contains all the grammars in this set. */
	private Map<String, RGrammar> grammars;

	/* Contains all the exported rules from grammars. */
	private Map<String, RGrammar> exportedRules;

	/* Contains which export came from which grammar. */
	private Map<String, String> exportFrom;

	/* Contains which file a grammar was loaded from. */
	private Map<String, String> loadedFrom;

	/** Create a new set of randomized grammars. */
	public RGrammarSet() {
		grammars = new HashMap<>();

		exportedRules = new HashMap<>();

		exportFrom = new HashMap<>();
		loadedFrom = new HashMap<>();
	}

	/**
	 * Add a grammar to this grammar set.
	 *
	 * @param grammarName
	 * 	The name of the grammar to add.
	 *
	 * @param gram
	 * 	The grammar to add.
	 *
	 * @throws IllegalArgumentException
	 * 	If the grammar name is invalid.
	 */
	public void addGrammar(String grammarName, RGrammar gram) {
		/* Make sure a grammar is valid. */
		if (grammarName == null) {
			throw new NullPointerException("Grammar name must not be null");
		} else if (gram == null) {
			throw new NullPointerException("Grammar must not be null");
		} else if (grammarName.equals("")) {
			throw new IllegalArgumentException("The empty string is not a valid grammar name");
		}

		grammars.put(grammarName, gram);

		/* Process exports from the grammar. */
		for (Rule export : gram.getExportedRules()) {
			exportedRules.put(export.name, gram);

			exportFrom.put(export.name, grammarName);
		}

		/* Add exports to grammar. */
		gram.setImportedRules(exportedRules);
	}

	/**
	 * Get a grammar from this grammar set.
	 *
	 * @param grammarName
	 * 	The name of the grammar to get.
	 *
	 * @return
	 * 	The grammar with that name.
	 *
	 * @throws IllegalArgumentException
	 * 	If the grammar name is invalid or not present in this set.
	 */
	public RGrammar getGrammar(String grammarName) {
		/* Check arguments. */
		if (grammarName == null) {
			throw new NullPointerException("Grammar name must not be null");
		} else if (grammarName.equals("")) {
			throw new IllegalArgumentException("The empty string is not a valid grammar name");
		} else if (!grammars.containsKey(grammarName)) {
			String msg = String.format("No grammar with name '%s' found", grammarName);

			throw new IllegalArgumentException(msg);
		}

		return grammars.get(grammarName);
	}

	/**
	 * Get the grammar a rule was exported from.
	 *
	 * @param exportName
	 * 	The name of the exported rule.
	 *
	 * @return
	 * 	The grammar the exported rule came from.
	 *
	 * @throws IllegalArgumentException
	 * 	If the export name is invalid or not present in this set.
	 */
	public RGrammar getExportSource(String exportName) {
		/* Check arguments. */
		if (exportName == null) {
			throw new NullPointerException("Export name must not be null");
		} else if (exportName.equals("")) {
			throw new IllegalArgumentException("The empty string is not a valid rule name");
		} else if (!exportedRules.containsKey(exportName)) {
			String msg = String.format("No export with name '%s' defined", exportName);
			throw new IllegalArgumentException(msg);
		}

		return exportedRules.get(exportName);
	}

	/**
	 * Get the source of an exported rule.
	 *
	 * This will often be a grammar name, but is not required to be one.
	 *
	 * @param exportName
	 * 	The name of the exported rule.
	 *
	 * @return 
	 * 	The source of an exported rule.
	 *
	 * @throws IllegalArgumentException
	 * 	If the exported rule is invalid or not present in this set.
	 */
	public String exportedFrom(String exportName) {
		/* Check arguments. */
		if (exportName == null) {
			throw new NullPointerException("Export name must not be null");
		} else if (exportName.equals("")) {
			throw new IllegalArgumentException("The empty string is not a valid rule name");
		} else if (!exportedRules.containsKey(exportName)) {
			String msg = String.format("No export with name '%s' defined", exportName);

			throw new IllegalArgumentException(msg);
		}

		return exportFrom.getOrDefault(exportName, "Unknown");
	}

	/**
	 * Get the source of an grammar
	 *
	 * This will often be a file name, but is not required to be one.
	 *
	 * @param grammarName
	 * 	The name of the exported grammar.
	 *
	 * @return
	 * 	The source of an exported grammar.
	 *
	 * @throws IllegalArgumentException
	 * 	If the exported grammar is invalid or not present in this set.
	 */
	public String loadedFrom(String grammarName) {
		/* Check arguments. */
		if (grammarName == null) {
			throw new NullPointerException("Grammar name must not be null");
		} else if (grammarName.equals("")) {
			throw new IllegalArgumentException("The empty string is not a valid grammar name");
		} else if (grammarName.equals("unknown")) {
			return grammarName;
		} else if (!grammars.containsKey(grammarName)) {
			String msg = String.format("No grammar with name '%s' defined", grammarName);
			throw new IllegalArgumentException();
		}

		return loadedFrom.getOrDefault(grammarName, "Unknown");
	}

	/**
	 * Get the names of all the grammars in this set.
	 *
	 * @return
	 * 	The names of all the grammars in this set.
	 */
	public Set<String> getGrammars() {
		return grammars.keySet();
	}

	/**
	 * Get the names of all the exported rules in this set.
	 *
	 * @return
	 * 	The names of all the exported rules in this set.
	 */
	public Set<String> getExportedRules() {
		return exportedRules.keySet();
	}

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

		/* Get the directory that contains the config file. */
		Path cfgParent = cfgFile.getParent();

		try(Scanner scn = new Scanner(cfgFile)) {
			/* Execute lines from the configuration file. */
			while (scn.hasNextLine()) {
				String ln = scn.nextLine().trim();

				/* Ignore blank/comment lines. */
				if (ln.equals("")) continue;

				if (ln.startsWith("#")) continue;

				/* Handle mixed whitespace. */
				ln = ln.replaceAll("\\s+", " ");

				/* 
				 * Get the place where the name of the grammar
				 * ends. 
				 */
				int nameIdx = ln.indexOf(" ");
				if (nameIdx == -1) {
					throw new GrammarException("Must specify a name for a loaded grammar");
				}

				/* Name and path of grammar. */
				String name = ln.substring(0, nameIdx);
				Path path   = Paths.get(ln.substring(nameIdx).trim());

				/*
				 * Convert from configuration relative path to
				 * absolute path.
				 */
				Path convPath = cfgParent.resolve(path);
				File fle      = convPath.toFile();

				if (fle.isDirectory()) {
					/* @TODO implement subset grammars */
					throw new GrammarException("Sub-grammar sets aren't implemented yet");
				} else if (fle.getName().endsWith(".gram")) {
					/* Load grammar file. */
					try {
						FileReader fis = new FileReader(fle);
						RGrammar gram = RGrammarParser.readGrammar(fis);
						fis.close();

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
				} else {
					String msg = String.format("Unrecognized file type '%s'");
					throw new GrammarException(msg);
				}
			}
		}

		return set;
	}
}
