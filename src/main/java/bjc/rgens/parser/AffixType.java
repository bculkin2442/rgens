package bjc.rgens.parser;

/**
 * The type of ways to affix rules.
 * @author Ben Culkin
 *
 */
public enum AffixType {
	/**
	 * Prefix and suffix the rules.
	 */
	CIRCUMFIX,
	/**
	 * Suffix the rules.
	 */
	SUFFIX,
	/**
	 * Prefix the rules.
	 */
	PREFIX;

	/**
	 * Does this affix type perform suffixing?
	 * 
	 * @return Whether this affix type performs suffixing.
	 */
	public boolean isSuffix() {
		return this != PREFIX;
	}

	/**
	 * Does this affix type perform prefixing?
	 * 
	 * @return Whether this affix type performs prefixing.
	 */
	public boolean isPrefix() {
		return this != SUFFIX;
	}
}