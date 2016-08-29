package bjc.RGens.gui;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import bjc.utils.gui.layout.AutosizeLayout;

public class GrammarStudioFrame extends JFrame {
	private static final long serialVersionUID = 1476431892446002428L;

	public GrammarStudioFrame() {
		super("Grammar Studio");

		setLayout(new AutosizeLayout());

		JDesktopPane mainPane = new JDesktopPane();

		JMenuBar menuBar = new JMenuBar();

		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic('m');
		
		menuBar.add(fileMenu);

		setJMenuBar(menuBar);
		add(mainPane);
	}
}
