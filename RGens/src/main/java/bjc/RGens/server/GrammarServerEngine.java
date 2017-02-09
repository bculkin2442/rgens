package bjc.RGens.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

import bjc.utils.funcdata.FunctionalList;
import bjc.utils.funcdata.IList;
import bjc.utils.funcdata.IMap;
import bjc.utils.funcutils.ListUtils;
import bjc.utils.gen.WeightedGrammar;

public class GrammarServerEngine {
	private IMap<String, WeightedGrammar<String>> loadedGrammars;
	private IMap<String, WeightedGrammar<String>> exportedRules;
	
	public static boolean debugMode = false;
	
	public GrammarServerEngine(IMap<String, WeightedGrammar<String>> loadedGrammars,
			IMap<String, WeightedGrammar<String>> exportedRules) {
		this.loadedGrammars = loadedGrammars;
		this.exportedRules = exportedRules;
	}

	public String getInitialRule(String grammarName) {
		return loadedGrammars.get(grammarName).getInitialRule();
	}
	
	public boolean hasInitialRule(String grammarName) {
		return loadedGrammars.get(grammarName).hasInitialRule();
	}
	
	public boolean hasExportedRule(String ruleName) {
		return exportedRules.containsKey(ruleName);
	}
	
	public boolean hasLoadedGrammar(String grammarName) {
		return loadedGrammars.containsKey(grammarName);
	}
	
	public void doLoadConfig(String fileName) {
		File inputFile = new File(fileName);
	
		try(FileInputStream inputStream = new FileInputStream(inputFile)) {
			try(Scanner fle = new Scanner(inputStream)) {
				while(fle.hasNextLine()) {
					String line = fle.nextLine().trim();
	
					// Handle comments
					if(line.equals("") ||
							line.startsWith("#") ||
							line.startsWith("//")) {
						continue;
					}
	
					// Handle mixed whitespace in input
					line = line.replaceAll("\\s+", " ");
	
					String path;
					String name;
	
					if(line.lastIndexOf(' ') != -1) {
						path = line.substring(0, line.lastIndexOf(' '));
						name = line.substring(line.lastIndexOf(' ') + 1, line.length());
					} else {
						path = line;
	
						File pathFile = new File(path);
						String pathName = pathFile.getName();
	
						if(pathFile.isDirectory()) {
							// Load all the files in the directory recursively
							Queue<File> entries = new LinkedList<>();
							
							for (File entry : pathFile.listFiles()) {
								entries.add(entry);
							}
							
							while(!entries.isEmpty()) {
								File entry = entries.poll();
								
								String entryPath = entry.getName();
								
								if(entry.isHidden()) continue;
								if(entry.isDirectory()) {
									for (File newEntry : entry.listFiles()) {
										entries.add(newEntry);
									}
									
									continue;
								}
								
								name = entryPath.substring(0, entryPath.lastIndexOf('.'));
								
								doLoadGrammarEntry(entry.toString(), name);
							}
							
							continue;
						}
						
						name = pathName.substring(0, pathName.lastIndexOf('.'));
					}
	
					doLoadGrammarEntry(path, name);
				}
			}
		} catch(IOException ioex) {
			System.out.printf("? Error reading configuration from file"
					+ " (reason: %s)\n", ioex.getMessage());
		}
	}

	private void doLoadGrammarEntry(String path, String name) {
		if(path.endsWith(".gram")) {
			doLoadGrammar(name, path);
		} else if(path.endsWith(".template")) {
			System.out.println("Error: Templates are not supported yet");
		} else if(path.endsWith(".long")) {
			doLoadLongRule(name, path);
		} else {
			System.out.println("Error: Unknown filetype " + 
					path.substring(path.lastIndexOf("."), path.length()));
		}
	}

	public void doLoadLongRule(String ruleName, String ruleFile) {
		ruleName = "[" + ruleName + "]";
	
		if(debugMode) {
			System.out.printf("Loading long rule (named %s) from path %s\n",
					ruleName, ruleFile);
		}

		try (FileInputStream inputStream = new FileInputStream(ruleFile)) {
			try (Scanner fle = new Scanner(inputStream)) {
				IList<IList<String>> ruleParts = new FunctionalList<>();
	
				while(fle.hasNextLine()) {
					ruleParts.add(new FunctionalList<>(fle.nextLine().trim().split(" ")));
				}
	
				WeightedGrammar<String> longGram = new WeightedGrammar<>();
	
				longGram.addSpecialRule(ruleName, () -> ruleParts.randItem());
				longGram.setInitialRule(ruleName);
	
				exportedRules.put(ruleName, longGram);
	
				if(debugMode) {
					System.out.printf("Loaded long rule (named %s) from path %s\n",
							ruleName, ruleFile);
				}
			}
		} catch (IOException ioex) {
			System.out.printf("Error reading long rule (%s)\n", ioex.getMessage());
		}
	}

	public void doLoadGrammar(String grammarName, String grammarPath) {
		if(debugMode) {
			System.out.printf("Loading grammar (named %s) from path %s\n",
					grammarName, grammarPath);
		}

		try (FileInputStream inputStream = new FileInputStream(grammarPath)) {
			WeightedGrammar<String> newGram = 
				ServerGrammarReader.fromStream(inputStream).merge((gram, exports) -> {
					for(String export : exports.toIterable()) {
						if(debugMode) {
							System.out.printf("\tLoaded exported rule %s from grammar %s\n",
								export, grammarName);

							if(exportedRules.containsKey(export)) {
								System.out.printf("\tWarning: Exported rule %s from grammar %s" +
										" shadows a pre-existing rule\n", export, grammarName);
							}
						}

						exportedRules.put(export, gram);
					}

					return gram;
				});

			loadedGrammars.put(grammarName, newGram);
		} catch (IOException ioex) {
			System.out.printf("? Error reading grammar from file"
					+ " (reason: %s)\n", ioex.getMessage());
		}

		if(debugMode) {
			System.out.printf("Loaded grammar (named %s) from path %s\n",
					grammarName, grammarPath);
		}
	}
	
	public void doGenerateExportedRule(String ruleName) {
		String ruleResult = ListUtils.collapseTokens(
				exportedRules.get(ruleName)
				.generateListValues(ruleName, " "));
	
		System.out.println("Generated Result: ");
		System.out.println("\t" + ruleResult.replaceAll("\\s+", " "));
	}

	public void doGenerateGrammar(String currentGram, String ruleName) {
		doGenerateGrammar(loadedGrammars.get(currentGram), ruleName);	
	}

	public void doGenerateGrammar(WeightedGrammar<String> currentGram, String ruleName) {
		String ruleResult = ListUtils.collapseTokens(
				currentGram.generateListValues(ruleName, " "));
	
		System.out.println("Generated Result: ");
		System.out.println("\t" + ruleResult.replaceAll("\\s+", " "));
	}

	public void doShowExportedRules() {
		System.out.printf("Currently exported rules (%d total):\n",
				exportedRules.getSize());
	
		exportedRules.forEachKey(key -> {
			System.out.println("\t" + key);
		});
	}

	public void doShowGrammarRules(String gramName) {
		WeightedGrammar<String> gram = loadedGrammars.get(gramName);
	
		IList<String> ruleNames = gram.getRuleNames();
	
		System.out.printf("Rules for grammar %s (%d total)\n",
				gramName, ruleNames.getSize());
	
		ruleNames.forEach(rule -> {
			System.out.println("\t" + rule);
		});
	}

	public void doShowLoadedGrammars() {
		System.out.printf("Currently loaded grammars (%d total):\n",
				loadedGrammars.getSize());
	
		loadedGrammars.forEachKey(key -> {
			System.out.println("\t" + key);
		});
	}

	public void doStressTest(int count) {
		exportedRules.forEachKey(key -> {
			doStressTest(key, count);
		});
	}
	
	public void doStressTest(String ruleName, int count) {
		doStressTest(exportedRules.get(ruleName), ruleName, count);
	}

	public void doStressTest(WeightedGrammar<String> gram, String ruleName, int count) {
		if(debugMode) System.out.println("Stress-testing rule " + ruleName);
		
		IList<String> res = new FunctionalList<>();
		IList<String> foundTags = new FunctionalList<>();

		boolean foundBroken = false;

		for(int i = 0; i < count; i++) {
			res = gram.generateListValues(ruleName, " ");

			for(String tok : res.toIterable()) {
				if(tok.matches("\\[\\S+\\]") && !foundTags.contains(tok)) {
					System.out.println("\tWarning: Possible un-expanded rule " + tok + " found" 
							+ " in expansion of " + ruleName);

					doFindRule(tok);

					foundBroken = true;

					foundTags.add(tok);
				}
			}
		}

		if(debugMode) {
			if(!foundBroken) System.out.printf("Rule %s succesfully passed stress-testing\n", ruleName);
			else System.out.printf("Rule %s failed stress-testing\n", ruleName);
		}
	}
	
	private void doFindRule(String ruleName) {
		loadedGrammars.forEach((gramName, gram) -> {
			if(gram.hasRule(ruleName)) {
				System.out.printf("\t\tFound rule %s in grammar %s\n", ruleName, gramName);
			}
		});
	}
}
