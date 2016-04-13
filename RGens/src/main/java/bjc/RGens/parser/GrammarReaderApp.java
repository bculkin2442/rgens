package bjc.RGens.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

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
	@SuppressWarnings("null")
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(
					UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
			System.exit(1);
		}

		File gramFile = SimpleFileDialog.getOpenFile(null,
				"Choose Grammar File", ".gram");

		WeightedGrammar<String> wg = null;

		try (FileInputStream fStream = new FileInputStream(gramFile)) {
			wg = RBGrammarReader.fromStream(fStream);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		String initRule = "";

		if (!wg.hasInitialRule()) {
			wg.getRuleNames().sort((leftString, rightString) -> {
				return leftString.compareTo(rightString);
			});

			initRule = SimpleDialogs.getChoice(null, "Pick a initial rule",
					"Pick a initial rule to generate choices from",
					wg.getRuleNames().toArray(new String[0]));
		} else {
			initRule = wg.getInitialRule();
		}

		int count = SimpleDialogs.getWhole(null,
				"Enter number of repititions",
				"Enter the number of items to generate from the rule");

		File outpFile = SimpleFileDialog.getSaveFile(null,
				"Choose Grammar File");

		PrintStream ps = null;

		try {
			ps = new PrintStream(outpFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		for (int i = 0; i < count; i++) {
			String s = wg.generateListValues(initRule, " ")
					.reduceAux(new StringBuilder(),
							(strang, strangBuilder) -> strangBuilder
									.append(strang),
							t -> t.toString())
					.replaceAll("\\s+", " ");

			ps.println(s);
		}
		
		ps.close();
	}
}
