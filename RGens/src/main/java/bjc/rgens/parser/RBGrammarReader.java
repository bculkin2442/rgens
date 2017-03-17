package bjc.rgens.parser;

import com.mifmif.common.regex.Generex;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import bjc.utils.funcdata.FunctionalStringTokenizer;
import bjc.utils.funcdata.FunctionalList;
import bjc.utils.funcdata.FunctionalMap;
import bjc.utils.funcdata.IList;
import bjc.utils.funcdata.IMap;
import bjc.utils.funcutils.ListUtils;
import bjc.utils.gen.WeightedGrammar;
import bjc.utils.parserutils.RuleBasedConfigReader;

/**
 * Read a grammar from a stream
 * 
 * @author ben
 *
 */
public class RBGrammarReader {
	private static RuleBasedConfigReader<ReaderState> reader;

	private static Random numgen = new Random();

	static {
		setupReader();

		initPragmas();
	}

	private static void addSubgrammarPragmas() {
		reader.addPragma("new-sub-grammar", (tokenizer, state) -> {
			state.startNewSubgrammar();
		});

		reader.addPragma("load-sub-grammar", RBGrammarReader::loadSubGrammar);
		reader.addPragma("save-sub-grammar", RBGrammarReader::saveGrammar);
		reader.addPragma("edit-sub-grammar", RBGrammarReader::editSubGrammar);
		reader.addPragma("remove-sub-grammar", RBGrammarReader::removeSubGrammar);

		reader.addPragma("edit-parent", (tokenizer, state) -> {
			state.editParent();
		});

		reader.addPragma("promote", RBGrammarReader::promoteGrammar);
		reader.addPragma("subordinate", RBGrammarReader::subordinateGrammar);

	}

	private static void debugGrammar(ReaderState state) {
		System.out.println("Printing rule names: ");

		for (String currentRule : state.getRuleNames().toIterable()) {
			System.out.println("\t" + currentRule);
		}

		System.out.println();
	}

	private static void doCase(FunctionalStringTokenizer tokenizer, ReaderState state) {
		int ruleProbability = readOptionalProbability(tokenizer, state);

		state.addCase(ruleProbability, tokenizer.toList());
	}

	private static void editSubGrammar(FunctionalStringTokenizer tokenizer, ReaderState state) {
		String subgrammarName = tokenizer.nextToken();

		state.editSubgrammar(subgrammarName);
	}

	/**
	 * Read a grammar from a path
	 * 
	 * @param inputPath
	 *            The path to load the grammar from
	 * 
	 * @return A grammar read from the stream
	 * 
	 * @throws IOException
	 *             If something goes wrong during file reading
	 * 
	 */
	public static WeightedGrammar<String> fromPath(Path inputPath) throws IOException {
		ReaderState initialState = new ReaderState(inputPath);

		try (FileInputStream inputStream = new FileInputStream(inputPath.toFile())) {
			WeightedGrammar<String> gram = reader.fromStream(inputStream, initialState).getGrammar();

			IMap<String, IList<String>> vars = new FunctionalMap<>();

			Predicate<String> specialPredicate = (strang) -> {
				if(strang.matches("\\{\\S+\\}") || strang.matches("\\[\\S+\\}")) {
					return true;
				}

				return false;
			};

			BiFunction<String, WeightedGrammar<String>, IList<String>> 
				specialAction = (strang, gramm) -> {
					IList<String> retList = new FunctionalList<>();

					if(strang.matches("\\{\\S+\\}")) {
						if(strang.matches("\\{\\S+:=\\S+\\}")) {
							String[] varParts = strang.split(":=");

							String varName = varParts[0].substring(1);
							String ruleName = varParts[1].substring(0, varParts[1].length());

							IList<String> varValue = gramm.generateGenericValues(
									ruleName, (s) -> s, " ");

							vars.put(varName, varValue);
						} else if(strang.matches("\\{\\S+=\\S+\\}")) {
							String[] varParts = strang.split("=");

							String varName = varParts[0].substring(1);
							String varValue = varParts[1].substring(0, varParts[1].length());

							vars.put(varName, new FunctionalList<>(varValue));
						} else {
							// @FIXME notify the user they did something wrong
							retList.add(strang);
						}
					} else {
						if(strang.matches("\\[\\$\\S+\\]")) {
							String varName = strang.substring(2, strang.length());

							retList = vars.get(varName);
						} else if(strang.matches("\\[\\$\\S+\\-\\S+\\]")) {
							String[] varParts = strang.substring(1, strang.length()).split("-");

							StringBuilder actualName = new StringBuilder("[");

							for(String varPart : varParts) {
								if(varPart.startsWith("$")) {
									IList<String> varName = vars.get(varPart.substring(1));

									if(varName.getSize() != 1) {
										// @FIXME notify the user they did something wrong
									}

									actualName.append(varName.first() + "-");
								} else {
									actualName.append(varPart + "-");
								}
							}

							// Trim trailing -
							actualName.deleteCharAt(actualName.length() - 1);
							actualName.append("]");

							retList = gramm.generateGenericValues(actualName.toString(), (s) -> s, " ");
						} else {
							// @FIXME notify the user they did something wrong
							retList.add(strang);
						}
					}

					return retList;
				};

			gram.configureSpecial(specialPredicate, specialAction);

			return gram;
		} catch (IOException ioex) {
			throw ioex;
		}
	}

	private static void importRule(FunctionalStringTokenizer tokenizer, ReaderState state) {
		String ruleName = tokenizer.nextToken();
		String subgrammarName = tokenizer.nextToken();

		state.addGrammarAlias(subgrammarName, ruleName);
	}

	private static void initialRule(FunctionalStringTokenizer tokenizer, ReaderState state) {
		String initialRuleName = tokenizer.nextToken();

		state.setInitialRule(initialRuleName);
	}

	private static void initPragmas() {
		addSubgrammarPragmas();

		reader.addPragma("debug", (tokenizer, state) -> {
			debugGrammar(state);
		});

		reader.addPragma("uniform", (tokenizer, state) -> {
			state.toggleUniformity();
		});

		reader.addPragma("initial-rule", RBGrammarReader::initialRule);

		reader.addPragma("import-rule", RBGrammarReader::importRule);

		reader.addPragma("remove-rule", RBGrammarReader::removeRule);

		reader.addPragma("prefix-with", RBGrammarReader::prefixRule);
		reader.addPragma("suffix-with", RBGrammarReader::suffixRule);

		reader.addPragma("regex-rule", (tokenizer, state) -> {
			String ruleName = tokenizer.nextToken();

			IList<String> regx = tokenizer.toList();
			Generex regex = new Generex(ListUtils.collapseTokens(regx));

			state.addSpecialRule(ruleName, () -> {
				return new FunctionalList<>(regex.random().split(" "));
			});
		});

		reader.addPragma("range-rule", (tokenizer, state) -> {
			String ruleName = tokenizer.nextToken();

			int start = Integer.parseInt(tokenizer.nextToken());
			int end = Integer.parseInt(tokenizer.nextToken());
			
			state.addSpecialRule(ruleName, () -> {
				return new FunctionalList<>(Integer.toString(
							numgen.nextInt((end - start) + 1) + start));
			});
		});
	}

	private static void loadSubGrammar(FunctionalStringTokenizer stk, ReaderState rs) {
		String subgrammarName = stk.nextToken();
		String subgrammarPath = stk.nextToken();

		rs.loadSubgrammar(subgrammarName, subgrammarPath);
	}

	private static void prefixRule(FunctionalStringTokenizer tokenizer, ReaderState state) {
		String ruleName = tokenizer.nextToken();
		String prefixToken = tokenizer.nextToken();

		int additionalProbability = readOptionalProbability(tokenizer, state);

		state.prefixRule(ruleName, prefixToken, additionalProbability);
	}

	private static void promoteGrammar(FunctionalStringTokenizer tokenizer, ReaderState state) {
		String subgrammarName = tokenizer.nextToken();
		String subordinateName = tokenizer.nextToken();

		state.promoteGrammar(subgrammarName, subordinateName);
	}

	private static int readOptionalProbability(FunctionalStringTokenizer tokenizer, ReaderState state) {
		if (state.isUniform()) {
			return 1;
		}

		return Integer.parseInt(tokenizer.nextToken());
	}

	private static void removeRule(FunctionalStringTokenizer tokenizer, ReaderState state) {
		String ruleName = tokenizer.nextToken();

		state.deleteRule(ruleName);
	}

	private static void removeSubGrammar(FunctionalStringTokenizer tokenizer, ReaderState state) {
		String subgrammarName = tokenizer.nextToken();

		state.deleteSubgrammar(subgrammarName);
	}

	private static void saveGrammar(FunctionalStringTokenizer tokenizer, ReaderState state) {
		String subgrammarName = tokenizer.nextToken();

		state.saveSubgrammar(subgrammarName);
	}

	private static void setupReader() {
		reader = new RuleBasedConfigReader<>(null, null, null);

		reader.setStartRule((tokenizer, stateTokenPair) -> {
			stateTokenPair.doWith((initToken, state) -> {
				state.startNewRule(initToken);

				doCase(tokenizer, state);
			});
		});

		reader.setContinueRule((tokenizer, state) -> {
			doCase(tokenizer, state);
		});

		reader.setEndRule((tokenizer) -> {
			tokenizer.setCurrentRule(null);
		});
	}

	private static void subordinateGrammar(FunctionalStringTokenizer tokenizer, ReaderState state) {
		String grammarName = tokenizer.nextToken();

		state.subordinateGrammar(grammarName);
	}

	private static void suffixRule(FunctionalStringTokenizer tokenizer, ReaderState state) {
		String ruleName = tokenizer.nextToken();
		String suffixToken = tokenizer.nextToken();

		int additionalProbability = readOptionalProbability(tokenizer, state);

		state.suffixRule(ruleName, suffixToken, additionalProbability);
	}
}
