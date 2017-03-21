package bjc.rgens.newparser;

import java.io.InputStream;

/**
 * Test for new grammar syntax.
 * 
 * @author EVE
 *
 */
public class RGrammarTest {
	/**
	 * Main method.
	 * 
	 * @param args
	 *                Unused CLI args.
	 */
	public static void main(String[] args) {
		InputStream stream = RGrammarTest.class.getResourceAsStream("/sample-grammars/insults.gram");

		RGrammarParser parse = new RGrammarParser();

		RGrammar grammar = parse.readGrammar(stream);

		System.out.println(grammar.generate(null));
	}
}
