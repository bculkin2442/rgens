package bjc.rgens.server;

import java.io.File;

public class CLIArgsParser {
	public static void parseArgs(String[] args, GrammarServerEngine eng) {
		boolean didTerminalOp = false;
		boolean forceInteractive = false;

		// @TODO report error status
		boolean didError = false;

		if(args.length < 0) return;
		
		if(args.length == 1 && args[0].equals("--help")) {
			// @TODO show help
		} else {
			for(int i = 0; i < args.length; i++) {
				String arg = args[i];

				switch(arg) {
					case "-lc":
					case "--load-config-file":
						String configFileName = args[++i];

						eng.doLoadConfig(configFileName);
						break;
					case "-lg":
					case "--load-grammar-file":
						String grammarFileName = args[++i];

						File grammarFile = new File(grammarFileName);

						String ruleName = grammarFile.getName();
						ruleName = ruleName.substring(0, ruleName.lastIndexOf('.'));

						if(!args[i+1].startsWith("-")) {
							ruleName = args[++i];
						}

						eng.doLoadGrammar(ruleName, grammarFileName);

						break;
					case "-ll":
					case "--load-long-file":
						String longRuleFileName = args[++i];

						File longRuleFile = new File(longRuleFileName);

						String longRuleName = longRuleFile.getName();
						longRuleName = longRuleName.substring(0, longRuleName.lastIndexOf('.'));

						if(!args[i+1].startsWith("-")) {
							longRuleName = args[++i];
						}

						eng.doLoadLongRule(longRuleName, longRuleFileName);

						break;
					case "-ge":
					case "--generate-exported-rule":
						String exportedRuleName = args[++i];

						if(eng.hasExportedRule(exportedRuleName)) {
							eng.doGenerateExportedRule(exportedRuleName);
							didTerminalOp = true;
						} else {
							System.out.printf("Error: No exported rule named %s\n", exportedRuleName);
						}
						break;
					case "-gg":
					case "--generate-grammar-rule":
						String grammarName = args[++i];
						
						if(!eng.hasLoadedGrammar(grammarName)) {
							System.out.printf("Error: No grammar named %s\n", grammarName);
						} else {
							String ruleToGenerate = "";

							if(!args[i+1].startsWith("-")) {
								ruleToGenerate = args[++i];
							} else if(eng.hasInitialRule(grammarName)) {
								ruleToGenerate = eng.getInitialRule(grammarName);
							} else {
								System.out.printf("Error: Grammar %s has no initial rule. A "
										+ "rule must be provided.",grammarName);
							}

							eng.doGenerateGrammar(grammarName, ruleToGenerate);
							didTerminalOp = true;
						}
						break;
					case "-st":
					case "--stress-test":
						String thingToTest = args[++i];

						// @TODO support testing rules from grammars
						// 		 as well as a specified number of times
						if(thingToTest.equals("*")) {
							eng.doStressTest(10000);
						} else {
							eng.doStressTest(thingToTest, 1000);
						}

						didTerminalOp = true;

						break;
					case "-d":
					case "--debug":
						if(eng.debugMode) {
							System.out.println("Warning: debug mode is already on. Use -nd or --no-debug"
									+ " to turn it off");
						} else {
							eng.debugMode = true;
						}
						break;
					case "-nd":
					case "--no-debug":
						if(!eng.debugMode) {
							System.out.println("Warning: debug mode is already off. Use -d or --debug"
									+ " to turn it on");
						} else {
							eng.debugMode = false;
						}
						break;
					case "-i":
					case "--interactive":
						forceInteractive = true;
						break;
					default:
						System.out.println("Error: Unrecognized argument " + arg);
						break;
				}
			}
		}

		if(!forceInteractive && didTerminalOp) {
			System.exit(didError ? 1 : 0);
		}
	}
}
