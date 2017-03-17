package bjc.rgens.server;

import bjc.utils.funcdata.FunctionalMap;
import bjc.utils.funcdata.IMap;
import bjc.utils.gen.WeightedGrammar;

import java.io.File;

import java.util.Scanner;

public class GrammarServer {
	private Scanner scn;

	private IMap<String, WeightedGrammar<String>> loadedGrammars;
	private IMap<String, WeightedGrammar<String>> exportedRules;
	
	private GrammarServerEngine eng;
	
	public GrammarServer(Scanner scn) {
		this.scn = scn;

		this.loadedGrammars = new FunctionalMap<>();
		this.exportedRules  = new FunctionalMap<>();

		eng = new GrammarServerEngine(loadedGrammars, exportedRules);
		
		ServerGrammarReader.setExportedRules(exportedRules);
	}

	public static void main(String[] args) {
		System.out.println("GrammarServer 1.0");

		Scanner scn = new Scanner(System.in);

		GrammarServer serv = new GrammarServer(scn);

		CLIArgsParser.parseArgs(args, serv.eng);

		System.out.print("Enter a command (m for help): ");

		char comm = scn.nextLine().charAt(0);

		while(comm != 'e') {
			switch(comm) {
				case 'm':
					System.out.println("GrammarServer Commands:");
					System.out.println("\tm: Print command help");
					System.out.println("\te: Exit GrammarServer");
					System.out.println("\tl: Load from file");
					System.out.println("\ts: Show information");
					System.out.println("\tg: Generate text");
					break;
				case 'g':
					serv.generateMode();
					break;
				case 's':
					serv.showMode();
					break;
				case 'l':
					serv.loadMode();
					break;
				default:
					System.out.println("? Unrecognized Command");
			}

			System.out.print("Enter a command (m for help): ");

			comm = scn.nextLine().charAt(0);
		}

		System.out.println("GrammarServer exiting");
	}

	

	private void loadMode() {
		System.out.println("Entering Load Mode");

		System.out.print("(Load Mode) Enter a command (m for help): ");
		char comm = scn.nextLine().charAt(0);

		while(comm != 'e') {
			switch(comm) {
				case 'm':
					System.out.println("GrammarServer Load Mode Commands");
					System.out.println("\tm: Show command help");
					System.out.println("\te: Exit Load Mode");
					System.out.println("\tg: Load grammar from a file");
					System.out.println("\tl: Load long rule from a file");
					System.out.println("\tc: Load configuration from a file");
					break;
				case 'g':
					loadGrammar();
					break;
				case 'c':
					loadConfig();
					break;
				case 'l':
					loadLongRule();
					break;
				default:
					System.out.println("? Unrecognized Command");
			}

			System.out.print("(Load Mode) Enter a command (m for help): ");
			comm = scn.nextLine().charAt(0);
		}

		System.out.println("Exiting Load Mode");
	}

	private void loadGrammar() {
		System.out.print("Enter path to load grammar from: ");
	
		String grammarPath = scn.nextLine().trim();
	
		File grammarFile = new File(grammarPath);
	
		String grammarName = grammarFile.getName().trim();
	
		grammarName = grammarName.substring(0, grammarName.lastIndexOf("."));
	
		System.out.printf("Enter grammar name or press enter for"
				+ " the default (%s): ", grammarName);
	
		String inputName = scn.nextLine();
	
		if(!inputName.equals("")) {
			grammarName = inputName;
		}
	
		eng.doLoadGrammar(grammarName, grammarPath);
	
		return;
	}

	private void loadLongRule() {
		System.out.print("Enter the file to load a long rule from: ");

		String fileName = scn.nextLine().trim();
		File ruleFile = new File(fileName);

		String tempName = ruleFile.getName();
		tempName = tempName.substring(0, tempName.lastIndexOf('.'));

		System.out.printf("Enter the name for the long rule (default %s): ", tempName);

		String ruleName = scn.nextLine().trim();

		if(ruleName.equals("")) {
			ruleName = tempName;
		}

		eng.doLoadLongRule(ruleName, fileName);
	}

	private void loadConfig() {
		System.out.print("Enter the file to load configuration from: ");
		
		String fileName = scn.nextLine().trim();

		eng.doLoadConfig(fileName);
	}

	private void showMode() {
		System.out.println("Entering Show Mode");

		System.out.print("(Show Mode) Enter a command (m for help): ");

		char comm = scn.nextLine().charAt(0);

		while(comm != 'e') {
			switch(comm) {
				case 'm':
					System.out.println("GrammarServer Show Mode Commands: ");
					System.out.println("\tm: Show command help");
					System.out.println("\tl: Show loaded grammars");
					System.out.println("\tr: Show rules from a grammar");
					System.out.println("\tx: Show exported rules");
					System.out.println("\te: Exit Show Mode");
					break;
				case 'r':
					showGrammarRules();
					break;
				case 'x':
					eng.doShowExportedRules();
					break;
				case 'l':
					eng.doShowLoadedGrammars();
					break;
				default:
					System.out.println("? Unrecognized command");
					break;
			}

			System.out.print("(Show Mode) Enter a command (m for help): ");

			comm = scn.nextLine().charAt(0);
		}



		System.out.println("Exiting Show Mode");
	}

	private void generateMode() {
		System.out.println("Entering Generate Mode");

		System.out.print("(Generate Mode) Enter a command (m for help): ");

		char comm = scn.nextLine().charAt(0);

		while(comm != 'e') {
			switch(comm) {
				case 'm':
					System.out.println("GrammarServer Generate Mode Commands: ");
					System.out.println("\tm: Show command help");
					System.out.println("\tx: Generate from exported rules");
					System.out.println("\tr: Generate from a grammar");
					System.out.println("\te: Exit Generate Mode");
					break;
				case 'x':
					generateExportedRule();
					break;
				case 'r':
					generateGrammar();
					break;
				default:
					System.out.println("? Unrecognized command");
			}

			System.out.print("(Generate Mode) Enter a command (m for help): ");

			comm = scn.nextLine().charAt(0);
		}

		System.out.println("Exiting Generate Mode");
	}

	private void generateExportedRule() {
		System.out.print("Enter the name of the rule to generate"
				+ " (l to list, enter to cancel): ");

		String ruleName = scn.nextLine().trim();

		while(true) {
			if(ruleName.equals("")) break;

			if(ruleName.equals("l")) {
				eng.doShowExportedRules();
			} else if (exportedRules.containsKey(ruleName)) {
				eng.doGenerateExportedRule(ruleName);

				System.out.print("Generate again from this rule? (yes/no) (yes by default): ");

				String resp = scn.nextLine().trim();

				if(resp.equalsIgnoreCase("yes") || resp.equals("")) {
					continue;
				}
			} else {
				System.out.println("? Unrecognized external rule");
			}

			System.out.print("Enter the name of the rule to generate"
					+ " (l to list, enter to cancel): ");

			ruleName = scn.nextLine().trim();
		}
	}

	private void generateGrammar() {
		System.out.print("Enter the name of the grammar to generate from (l to list, enter to cancel): ");

		String grammarName = scn.nextLine().trim();

		while(true) {
			if(grammarName.equals("")) break;

			if(grammarName.equals("l")) {
				eng.doShowLoadedGrammars();
			} else if(loadedGrammars.containsKey(grammarName)) {
				WeightedGrammar<String> currentGram = loadedGrammars.get(grammarName);

				System.out.print("Enter the name of the rule to generate"
						+ " (l to list, enter to cancel): ");

				String ruleName = scn.nextLine().trim();

				while(true) {
					if(ruleName.equals("")) break;

					if(ruleName.equals("l")) {
						eng.doShowGrammarRules(grammarName);
					} else if (currentGram.hasRule(ruleName)) {
						eng.doGenerateGrammar(currentGram, ruleName);

						System.out.print("Generate again from this rule? (yes/no) (yes by default): ");

						String resp = scn.nextLine().trim();

						if(resp.equalsIgnoreCase("yes") || resp.equals("")) {
							continue;
						}
					} else {
						System.out.println("? Unrecognized grammar rule");
					}

					System.out.print("Enter the name of the rule to generate"
							+ " (l to list, enter to cancel): ");

					ruleName = scn.nextLine().trim();
				}

			} else {
				System.out.println("? Unrecognized grammar name");
			}

			System.out.print("Enter the name of the grammar to generate from "
					+ "(l to list, enter to cancel): ");

			grammarName = scn.nextLine().trim();
		}
	}

	private void showGrammarRules() {
		System.out.print("Enter the name of the grammar (l to list): ");
		String gramName = scn.nextLine().trim();
	
		do {
			if(gramName.equals("")) break;
	
			if(gramName.equals("l")) {
				eng.doShowLoadedGrammars();
			} else if (loadedGrammars.containsKey(gramName)) {
				eng.doShowGrammarRules(gramName);
				break;
			} else {
				System.out.println("? Unrecognized grammar name");
			}
	
			System.out.print("Enter the name of the grammar (l to list): ");
			gramName = scn.nextLine().trim();
		} while(true);
	}
}
