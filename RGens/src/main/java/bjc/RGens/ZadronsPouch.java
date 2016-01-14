package bjc.RGens;

import bjc.utils.funcdata.FunctionalStringTokenizer;
import bjc.utils.funcdata.FunctionalList;
import bjc.utils.gen.RandomGrammar;

public class ZadronsPouch {
	public static void main(String[] args) {
		ZadronsPouch zp = new ZadronsPouch();

		for (int i = 0; i < 100; i++) {
			FunctionalList<String> ls = zp.wg.genList("<item>", " ");

			StringBuilder sb = new StringBuilder();

			ls.forEach(sp -> sb.append(sp));

			System.out.println(sb.toString().replaceAll("\\s+", " "));
		}
		
		System.out.println();
		System.out.println();
		System.out.println();
	}

	private RandomGrammar<String> wg;

	public ZadronsPouch() {
		wg = new RandomGrammar<>();

		addRule("<item>", "<egg>", "<glove>", "<crys-sphere>", "<rock>",
				"<figurine>", "<vial>", "<mini-weapon>", "<bag>", "<card>",
				"<rope>", "<box>", "<wand>");

		addEggRules();

		addGloveRules();

		addCrysSphereRules();

		addRockRules();

		addFigurineRules();

		addVialRules();

		addMiniWeaponRules();

		addBagRules();

		addCardRules();

		addRopeRules();

		addBoxRules();

		addWandRules();
	}

	private void addBagRules() {
		addRule("<bag>", "bag of <bag-type>", "<sack-type> sack",
				"<purse-type> purse");
		addRule("<bag-type>", "holding", "tricks", "useful items",
				"devouring", "dwarf-kind", "invisible cloth",
				"monster summoning");
		addRule("<sack-type>", "lunch", "recursive");
		addRule("<purse-type>", "everfull");
	}

	private void addBoxRules() {
		addRule("<box>", "<box-type> box", "cube of <box-type>");
		addRule("<box-type>", "limited-force", "frost-resisting",
				"morphing", "self-destructing", "pandora", "panicking");
	}

	private void addCardRules() {
		addRule("<card>", "card of <card-type>", "<card-type> card");
		addRule("<card-type>", "fate", "teleporting", "elusive treasure",
				"spell-storing", "many-things", "imprisoning", "messaging",
				"bounty");
	}

	private void addCrysSphereRules() {
		addRule("<crys-sphere>", "<sphere-type> spheres",
				"<sphere-type> sphere", "lens of <lens-type>",
				"<crystal-type> crystal", "crystal of <crystal-type>",
				"crystal ball", "crystal ball of <crys-suffix>");
		addRule("<sphere-type>", "microphonic", "seeing-eye");
		addRule("<lens-type>", "detection");
		addRule("<crystal-type>", "prison", "radar");
		addRule("<crys-suffix>", "jumping");
	}

	private void addEggRules() {
		addRule("<egg>", "<egg-type> egg");
		addRule("<egg-type>", "copper", "stone", "golden", "white",
				"white/pink", "glass");
	}

	private void addFigurineRules() {
		addRule("<figurine>", "<fig-material> <fig-animal>");
		addRule("<fig-material>", "golden", "onyx", "serpentine", "ivory",
				"marble", "bronze", "jade", "limestone");
		addRule("<fig-animal>", "lion", "dog", "owl", "goat", "elephant",
				"warrior", "palace", "leprechaun");
	}

	private void addGloveRules() {
		addRule("<glove>", "gauntlets of <gauntlet-type>",
				"gloves of <glove-type>", "<glove-type> gloves");
		addRule("<gauntlet-type>", "dexterity", "power");
		addRule("<glove-type>", "pushing", "choking", "bigby", "stunning");
	}

	private void addMiniWeaponRules() {
		addRule("<mini-weapon>", "minature <weapon-type>",
				"small <weapon-type>", "tiny <weapon-type>",
				"<sling-type> sling", "<weapon-type>");
		addRule("<weapon-type>", "boomerang", "arrow", "net", "catapult",
				"hammer", "sword", "club");
		addRule("<sling-type>", "seeking");
	}

	private void addRockRules() {
		addRule("<rock>", "<pebble-type> pebble", "stone of <stone-type>",
				"<stone-type> stone", "brick of <brick-type>",
				"<geode-type> geode");
		addRule("<pebble-type>", "inscribed", "elemental control");
		addRule("<stone-type>", "good-luck", "weight", "blind-defense",
				"metal-clinging");
		addRule("<brick-type>", "flying");
		addRule("<geode-type>", "ioun");
	}

	private void addRopeRules() {
		addRule("<rope>", "<rope-type> rope", "rope of <rope-type>",
				"ball of <string-type> <string-kind>");
		addRule("<rope-type>", "trick", "entangling", "climbing",
				"dancing", "tripping", "snaring", "levitating",
				"self-entangling");
		addRule("<string-type>", "endless");
		addRule("<string-kind>", "string", "yarn");
	}

	private void addRule(String rule, String... cases) {
		FunctionalList<FunctionalList<String>> cses = new FunctionalList<>();

		for (String strang : cases) {
			cses.add(FunctionalStringTokenizer.fromString(strang)
					.toList(s -> s));
		}

		wg.makeRule(rule, cses);
	}

	private void addVialRules() {
		addRule("<vial>", "vial of <vial-type>", "<vial-type> vial",
				"<bottle-type> bottle", "<flask-type> flask");
		addRule("<vial-type>", "holding", "trapping", "experience",
				"unnatural regeneration");
		addRule("<bottle-type>", "ever-smoking", "wheezing",
				"blank potion");
		addRule("<flask-type>", "iron");
	}

	private void addWandRules() {
		addRule("<wand>", "<wand-type> wand", "wand of <wand-type>",
				"canceling <wand-type> wand");
		addRule("<wand-type>", "magic missile", "<spell-1>", "<spell-2>",
				"gusting", "life-detecting", "zadron");
		addRule("<spell-1>", "frost", "fire", "lightning", "fear",
				"illumination", "polymorphing", "conjuration",
				"paralyzing");
		addRule("<spell-2>", "<spell2-type> detecting");
		addRule("<spell2-type>", "magic", "enemy", "secret door/trap");
	}
}
