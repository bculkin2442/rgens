package bjc.RGens.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import bjc.utils.funcutils.ListUtils;
import bjc.utils.gen.WeightedGrammar;
import bjc.utils.gui.SimpleDialogs;
import bjc.utils.gui.awt.SimpleFileDialog;

/**
 * App that reads a grammar from a file and generates results
 * 
 * @author ben
 *
 */
public class GrammarReaderApp {
	/**
	 * Main method of class
	 * 
	 * @param args
	 *            CLI args
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException
				| InstantiationException
				| IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
			System.exit(1);
		}

		doSingleFile();
	}

	private static void doSingleFile() {
		File gramFile = SimpleFileDialog.getOpenFile(null, "Choose Grammar File", ".gram");

		WeightedGrammar<String> grammar = null;

		try {
			grammar = RBGrammarReader.fromPath(gramFile.toPath());
		} catch (IOException ioex) {
			ioex.printStackTrace();

			System.exit(1);
		}

		String initRule = "";

		if (!grammar.hasInitialRule()) {
			grammar.getRuleNames().sort((leftString, rightString) -> {
				return leftString.compareTo(rightString);
			});

			initRule = SimpleDialogs.getChoice(null,
					"Pick a initial rule",
					"Pick a initial rule to generate choices from",
					grammar.getRuleNames().toArray(new String[0]));
		} else {
			initRule = grammar.getInitialRule();
		}

		int count = SimpleDialogs.getWhole(null,
				"Enter number of repetitions",
				"Enter the number of items to generate from the rule");

		File outputFile = SimpleFileDialog.getSaveFile(null, "Choose Grammar File");

		PrintStream outputStream = null;

		try {
			outputStream = new PrintStream(outputFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		for (int i = 0; i < count; i++) {
			String ruleResult = ListUtils.collapseTokens(grammar.generateListValues(initRule, " "));

			outputStream.println(ruleResult.replaceAll("\\s+", " "));
		}

		outputStream.close();
	}
}
