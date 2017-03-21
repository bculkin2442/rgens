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
		InputStream stream = RGrammarTest.class.getResourceAsStream("/sample-grammars/24hr-rpg.gram");

		RGrammarSet grammarSet = new RGrammarSet();

		RGrammarParser parse = new RGrammarParser();

		RGrammar grammar = parse.readGrammar(stream);

		grammarSet.addGrammar("rpg", grammar);

		for(int i = 0; i < 10; i++) {
			System.out.println(grammar.generate(null));
		}
	}
}
