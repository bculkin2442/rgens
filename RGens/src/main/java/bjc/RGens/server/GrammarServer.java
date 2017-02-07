package bjc.RGens.server;

import bjc.utils.funcdata.FunctionalMap;
import bjc.utils.funcdata.IMap;
import bjc.utils.funcdata.IList;
import bjc.utils.funcutils.ListUtils;
import bjc.utils.gen.WeightedGrammar;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;

import java.util.Scanner;

public class GrammarServer {
	private Scanner scn;

	public final IMap<String, WeightedGrammar<String>> loadedGrammars;

	public final IMap<String, WeightedGrammar<String>> exportedRules;

	public GrammarServer(Scanner scn) {
		this.scn = scn;

		this.loadedGrammars = new FunctionalMap<>();
		this.exportedRules  = new FunctionalMap<>();

		ServerGrammarReader.setExportedRules(exportedRules);
	}

	public static void main(String[] args) {
		System.out.println("GrammarServer 1.0");

		Scanner scn = new Scanner(System.in);

		GrammarServer serv = new GrammarServer(scn);

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
					serv.generateText();
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

		System.out.print("(Load Mode) Enter a command (m for help):");
		char comm = scn.nextLine().charAt(0);

		while(comm != 'e') {
			switch(comm) {
				case 'm':
					System.out.println("GrammarServer Load Mode Commands");
					System.out.println("\tm: Show command help");
					System.out.println("\te: Exit Generate Mode");
					System.out.println("\tg: Load grammar from a file");
					System.out.println("\tc: Load configuration from a file");
					break;
				case 'g':
					loadGrammar();
					break;
				case 'c':
					loadConfig();
					break;
				default:
					System.out.println("? Unrecognized Command");
			}

			System.out.print("(Load Mode) Enter a command (m for help):");
			comm = scn.nextLine().charAt(0);
		}

		System.out.println("Exiting Load Mode");
	}

	private void loadConfig() {
		System.out.print("Enter the file to load configuration from: ");
		
		String fileName = scn.nextLine().trim();

		File inputFile = new File(fileName);

		try(FileInputStream inputStream = new FileInputStream(inputFile)) {
			try(Scanner fle = new Scanner(inputStream)) {
				while(fle.hasNextLine()) {
					String line = fle.nextLine().trim();

					String gramName = line.substring(0, line.indexOf(' '));
					String gramPath = line.substring(line.indexOf(' ') + 1, line.length());

					loadGrammar(gramName, gramPath);
				}
			}
		} catch(IOException ioex) {
			System.out.printf("? Error reading configuration from file"
					+ " (reason: %s)", ioex.getMessage());
		}
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
					System.out.printf("Currently exported rules (%d total):\n",
							exportedRules.getSize());

					exportedRules.forEachKey(key -> {
						System.out.println("\t" + key);
					});
					break;
				case 'l':
					System.out.printf("Currently loaded grammars (%d total):\n",
							loadedGrammars.getSize());

					loadedGrammars.forEachKey(key -> {
						System.out.println("\t" + key);
					});
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

	private void showGrammarRules() {
		System.out.print("Enter the name of the grammar (l to list): ");
		String gramName = scn.nextLine().trim();

		do {
			if(gramName.equals("")) break;

			if(gramName.equals("l")) {
				System.out.printf("Currently loaded grammars (%d total):\n",
						loadedGrammars.getSize());

				loadedGrammars.forEachKey(key -> {
					System.out.println("\t" + key);
				});
			} else if (loadedGrammars.containsKey(gramName)) {
				WeightedGrammar<String> gram = loadedGrammars.get(gramName);

				IList<String> ruleNames = gram.getRuleNames();

				System.out.printf("Rules for grammar %s (%d total)\n",
						gramName, ruleNames.getSize());

				ruleNames.forEach(rule -> {
					System.out.println("\t" + rule);
				});
				break;
			} else {
				System.out.println("? Unrecognized grammar name");
			}

			System.out.print("Enter the name of the grammar (l to list): ");
			gramName = scn.nextLine().trim();
		} while(true);
	}

	private void generateText() {
		System.out.println("Entering Generate Mode");

		System.out.print("(Generate Mode) Enter a command (m for help): ");

		char comm = scn.nextLine().charAt(0);

		while(comm != 'e') {
			switch(comm) {
				case 'm':
					System.out.println("GrammarServer Generate Mode Commands: ");
					System.out.println("\tm: Show command help");
					System.out.println("\tx: Generate from exported rules");
					System.out.println("\te: Exit Generate Mode");
					break;
				case 'x':
					generateExportedRule();
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
				System.out.println("Current exported rules: ");
				exportedRules.forEachKey(key -> {
					System.out.println("\t" + key);
				});
			} else if (exportedRules.containsKey(ruleName)) {
				String ruleResult = ListUtils.collapseTokens(
						exportedRules.get(ruleName)
						.generateListValues(ruleName, " "));

				System.out.println("Generated Result: ");
				System.out.println("\t" + ruleResult.replaceAll("\\s+", " "));

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

		doLoadGrammar(grammarName, grammarPath);

		return;
	}

	private void doLoadGrammar(String grammarName, String grammarPath) {
		System.out.printf("Loading grammar (named %s) from path %s\n",
				grammarName, grammarPath);

		try (FileInputStream inputStream = new FileInputStream(grammarPath)) {
			WeightedGrammar<String> newGram = 
				ServerGrammarReader.fromStream(inputStream).merge((gram, exports) -> {
					for(String export : exports.toIterable()) {
						exportedRules.put(export, gram);
					}

					return gram;
				});

			loadedGrammars.put(grammarName, newGram);
		} catch (IOException ioex) {
			System.out.printf("? Error reading grammar from file"
					+ " (reason: %s)", ioex.getMessage());
		}

		System.out.printf("Loaded grammar (named %s) from path %s\n",
				grammarName, grammarPath);
	}
}
