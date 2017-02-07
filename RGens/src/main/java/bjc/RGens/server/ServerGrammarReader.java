package bjc.RGens.server;

import com.mifmif.common.regex.Generex;

import java.io.InputStream;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

import bjc.utils.data.IPair;
import bjc.utils.data.Pair;
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
public class ServerGrammarReader {
	private static RuleBasedConfigReader<ReaderState> reader;

	private static Random numgen = new Random();

	private static IMap<String, WeightedGrammar<String>> exportedRules;

	public static void setExportedRules(IMap<String, WeightedGrammar<String>> rules) {
		exportedRules = rules;
	}

	static {
		setupReader();

		initPragmas();
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

	/**
	 * Read a grammar from a stream
	 * 
	 * @param inputStream
	 *            The stream to load the grammar from
	 * 
	 * @return A grammar read from the stream
	 * 
	 */
	public static IPair<WeightedGrammar<String>, IList<String>>
		fromStream(InputStream inputStream) {
			ReaderState initialState = new ReaderState();

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
						} else if (exportedRules.containsKey(strang)) {
							return exportedRules.get(strang)
								.generateGenericValues(strang, (s) -> s, " ");
						} else {
							// @FIXME notify the user they did something wrong
							retList.add(strang);
						}
					}

					return retList;
				};

			gram.configureSpecial(specialPredicate, specialAction);

			return new Pair<>(gram, initialState.getExports());
	}

	private static void initialRule(FunctionalStringTokenizer tokenizer, ReaderState state) {
		String initialRuleName = tokenizer.nextToken();

		state.setInitialRule(initialRuleName);
	}

	private static void initPragmas() {
		reader.addPragma("debug", (tokenizer, state) -> {
			debugGrammar(state);
		});

		reader.addPragma("uniform", (tokenizer, state) -> {
			state.toggleUniformity();
		});

		reader.addPragma("initial-rule", ServerGrammarReader::initialRule);

		reader.addPragma("remove-rule", ServerGrammarReader::removeRule);

		reader.addPragma("prefix-with", ServerGrammarReader::prefixRule);
		reader.addPragma("suffix-with", ServerGrammarReader::suffixRule);

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

		reader.addPragma("export-rule", (tokenizer, state) -> {
			String ruleName = tokenizer.nextToken();

			state.addExport(ruleName);
		});
	}

	private static void prefixRule(FunctionalStringTokenizer tokenizer, ReaderState state) {
		String ruleName = tokenizer.nextToken();
		String prefixToken = tokenizer.nextToken();

		int additionalProbability = readOptionalProbability(tokenizer, state);

		state.prefixRule(ruleName, prefixToken, additionalProbability);
	}

	private static int readOptionalProbability(FunctionalStringTokenizer tokenizer, ReaderState state) {
		if (state.isUniform()) {
			return 0;
		}

		return Integer.parseInt(tokenizer.nextToken());
	}

	private static void removeRule(FunctionalStringTokenizer tokenizer, ReaderState state) {
		String ruleName = tokenizer.nextToken();

		state.deleteRule(ruleName);
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

	private static void suffixRule(FunctionalStringTokenizer tokenizer, ReaderState state) {
		String ruleName = tokenizer.nextToken();
		String suffixToken = tokenizer.nextToken();

		int additionalProbability = readOptionalProbability(tokenizer, state);

		state.suffixRule(ruleName, suffixToken, additionalProbability);
	}
}
