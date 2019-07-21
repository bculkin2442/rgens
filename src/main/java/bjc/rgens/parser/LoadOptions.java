package bjc.rgens.parser;

import java.nio.file.Path;

/**
 * Options used during the loading of config sets and config set related things.
 * 
 * @author Ben Culkin.
 */
public class LoadOptions {
	/**
	 * Should timings be tracked?
	 */
	public boolean doPerf;

	/**
	 * Should debug stats be tracked?
	 */
	public boolean doDebug;

	/**
	 * Should trace stats be tracked?
	 */
	public boolean doTrace;

	/**
	 * The default grammar set name.
	 */
	public String defName;

	/**
	 * The path the current file is in.
	 */
	public Path parent;

	/**
	 * The file the current config set is being loaded from.
	 */
	public Path cfgFile;

	/**
	 * The config set being loaded.
	 */
	public ConfigSet cfgSet;

	/**
	 * The grammar set being loaded.
	 */
	public RGrammarSet gramSet;
}
