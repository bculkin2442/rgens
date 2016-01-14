package bjc.RGens.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import bjc.utils.gen.WeightedGrammar;
import bjc.utils.gui.SimpleDialogs;
import bjc.utils.gui.awt.SimpleFileDialog;

public class GrammarReaderApp {

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

		try {
			wg = GrammarReader.fromStream(new FileInputStream(gramFile));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}

		String initRule = SimpleDialogs.getChoice(null,
				"Pick a initial rule",
				"Pick a initial rule to generate choices from",
				wg.ruleNames().stream().sorted().toArray(String[]::new));

		int count = SimpleDialogs.getWhole(null,
				"Enter number of repititions",
				"Enter the number of items to generate from the rule");

		for (int i = 0; i < count; i++) {
			String s = wg.genList(initRule, " ")
					.reduceAux(new StringBuilder(),
							(strang, strangBuilder) -> strangBuilder
									.append(strang),
							t -> t.toString())
					.replaceAll("\\s+", " ");

			System.out.println(s);
		}
	}
}
