package bjc.RGens.text.markov;

/**
 * Represents a key, value pairing.
 * 
 * @author Daniel Friedman
 *
 */
public class MyEntry<K, V> {
	protected K	key;
	protected V	value;

	@Override
	/**
	 * Gives the hashcode of a MyEntry object.
	 * 
	 * @return the hashcode of the MyEntry's key.
	 */
	public int hashCode() {
		return key.hashCode();
	}

	/**
	 * Tests whether this object is equivalent to a specified other.
	 * 
	 * @param obj
	 *            the other object.
	 * @return true if the keys are equivalent.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		boolean ret = false;

		if (obj == null) {
			ret = false;
		}

		MyEntry<K, V> other = (MyEntry<K, V>) obj;

		if (key.equals(other.key)) {
			ret = true;
		}

		return ret;
	}

	/**
	 * Constructs a MyEntry object with given key and value.
	 * 
	 * @param key
	 *            the key to be stored.
	 * @param value
	 *            the value to be stored.
	 * @param myHashMap
	 *            TODO
	 */
	public MyEntry(K key, V value) {
		this.key = key;
		this.value = value;
	}

	/**
	 * Gives a String representation of this object.
	 * 
	 * @return the String representation.
	 */
	public String toString() {
		return "(" + key + ", " + value + ")";
	}
}