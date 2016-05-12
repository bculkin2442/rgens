package bjc.RGens.parser;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;

import bjc.utils.funcdata.FunctionalStringTokenizer;
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

	static {
		setupReader();

		initPragmas();
	}

	private static void addSubgrammarPragmas() {
		reader.addPragma("new-sub-grammar", (tokenizer, state) -> {
			state.startNewSubgrammar();
		});

		reader.addPragma("load-sub-grammar",
				RBGrammarReader::loadSubGrammar);
		reader.addPragma("save-sub-grammar", RBGrammarReader::saveGrammar);

		reader.addPragma("edit-sub-grammar",
				RBGrammarReader::editSubGrammar);
		reader.addPragma("edit-parent", (tokenizer, state) -> {
			state.editParent();
		});

		reader.addPragma("promote", RBGrammarReader::promoteGrammar);
		reader.addPragma("subordinate",
				RBGrammarReader::subordinateGrammar);

		reader.addPragma("remove-sub-grammar",
				RBGrammarReader::removeSubGrammar);
	}

	private static void debugGrammar(ReaderState state) {
		System.out.println("Printing rule names: ");

		for (String currentRule : state.getRuleNames().toIterable()) {
			System.out.println("\t" + currentRule);
		}

		System.out.println();
	}

	private static void doCase(FunctionalStringTokenizer tokenizer,
			ReaderState state) {
		int ruleProbability = readOptionalProbability(tokenizer, state);

		state.addCase(ruleProbability, tokenizer.toList());
	}

	private static void editSubGrammar(FunctionalStringTokenizer tokenizer,
			ReaderState state) {
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
	public static WeightedGrammar<String> fromPath(Path inputPath)
			throws IOException {
		ReaderState initialState = new ReaderState(inputPath);

		try (FileInputStream inputStream = new FileInputStream(
				inputPath.toFile())) {
			return reader.fromStream(inputStream, initialState)
					.getGrammar();
		} catch (IOException ioex) {
			throw ioex;
		}
	}

	private static void importRule(FunctionalStringTokenizer tokenizer,
			ReaderState state) {
		String ruleName = tokenizer.nextToken();
		String subgrammarName = tokenizer.nextToken();

		state.addGrammarAlias(subgrammarName, ruleName);
	}

	private static void initialRule(FunctionalStringTokenizer tokenizer,
			ReaderState state) {
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
	}

	private static void loadSubGrammar(FunctionalStringTokenizer stk,
			ReaderState rs) {
		String subgrammarName = stk.nextToken();
		String subgrammarPath = stk.nextToken();

		rs.loadSubgrammar(subgrammarName, subgrammarPath);
	}

	private static void prefixRule(FunctionalStringTokenizer tokenizer,
			ReaderState state) {
		String ruleName = tokenizer.nextToken();
		String prefixToken = tokenizer.nextToken();

		int additionalProbability = readOptionalProbability(tokenizer,
				state);

		state.prefixRule(ruleName, prefixToken, additionalProbability);
	}

	private static void promoteGrammar(FunctionalStringTokenizer tokenizer,
			ReaderState state) {
		String subgrammarName = tokenizer.nextToken();
		String subordinateName = tokenizer.nextToken();

		state.promoteGrammar(subgrammarName, subordinateName);
	}

	private static int readOptionalProbability(
			FunctionalStringTokenizer tokenizer, ReaderState state) {
		if (state.isUniform()) {
			return 0;
		}

		return Integer.parseInt(tokenizer.nextToken());
	}

	private static void removeRule(FunctionalStringTokenizer tokenizer,
			ReaderState state) {
		String ruleName = tokenizer.nextToken();

		state.deleteRule(ruleName);
	}

	private static void removeSubGrammar(
			FunctionalStringTokenizer tokenizer, ReaderState state) {
		String subgrammarName = tokenizer.nextToken();

		state.deleteSubgrammar(subgrammarName);
	}

	private static void saveGrammar(FunctionalStringTokenizer tokenizer,
			ReaderState state) {
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

	private static void subordinateGrammar(
			FunctionalStringTokenizer tokenizer, ReaderState state) {
		String grammarName = tokenizer.nextToken();

		state.subordinateGrammar(grammarName);
	}

	private static void suffixRule(FunctionalStringTokenizer tokenizer,
			ReaderState state) {
		String ruleName = tokenizer.nextToken();
		String suffixToken = tokenizer.nextToken();

		int additionalProbability = readOptionalProbability(tokenizer,
				state);

		state.suffixRule(ruleName, suffixToken, additionalProbability);
	}
}
