package bjc.rgens.gui;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class GrammarStudio {
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(
					UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException
				| UnsupportedLookAndFeelException ex) {
			System.out.println(
					"WARNING: Could not use system look and feel");
			ex.printStackTrace();
		}

		GrammarStudioFrame mainFrame = new GrammarStudioFrame();

		mainFrame.setDefaultCloseOperation(
				GrammarStudioFrame.EXIT_ON_CLOSE);

		mainFrame.setVisible(true);
	}
}
