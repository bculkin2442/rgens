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
import java.util.function.Supplier;

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
					System.out.println("\tl: Load grammar from file");
					System.out.println("\ts: Show loaded grammars");
					System.out.println("\tg: Generate text");
					break;
				case 'g':
					serv.generateText();
					break;
				case 's':
					// @TODO expand to general show stuff method
					System.out.printf("Currently loaded grammars (%d total):\n",
							serv.loadedGrammars.getSize());

					serv.loadedGrammars.forEachKey(key -> {
						System.out.println("\t" + key);
					});

					break;
				case 'l':
					serv.loadGrammar();
					break;
				default:
					System.out.println("? Unrecognized Command");
			}

			System.out.print("Enter a command (m for help): ");

			comm = scn.nextLine().charAt(0);
		}

		System.out.println("GrammarServer exiting");
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
							System.out.println(ruleResult.replaceAll("\\s+", " "));

							System.out.println("Generate again from this rule? (yes/no)");

							String resp = scn.nextLine().trim();

							if(resp.equalsIgnoreCase("yes")) {
								continue;
							}
						} else {
							System.out.println("? Unrecognized external rule");
						}

						System.out.print("Enter the name of the rule to generate"
							+ " (l to list, enter to cancel): ");

						ruleName = scn.nextLine().trim();
					}
					break;
				default:
					System.out.println("? Unrecognized command");
			}

			System.out.print("(Generate Mode) Enter a command (m for help): ");

			comm = scn.nextLine().charAt(0);
		}

		System.out.println("Exiting Generate Mode");
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

		return;
	}
}
