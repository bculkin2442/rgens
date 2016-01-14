package bjc.RGens.text.markov;

import java.util.*;

/**
 * A Hash table implementation. Uses an array of LinkedList<MyEntry> as
 * backing storage.
 * 
 * @author Daniel Friedman
 *
 * @param <K>
 *            generic type for Keys.
 * @param <V>
 *            generic type for Values.
 */

public class MyHashMap<K, V> {
	private int		size;
	private float	loadFactor;

	LinkedList<MyEntry<K, V>>	table[];
	private ArrayList<Integer>	primes	= new ArrayList<Integer>();

	/**
	 * Constructs a new MyHashMap object.
	 * 
	 * @param capacity
	 *            the size of the array.
	 * @param loadFactor
	 *            the specified load factor. The array will resize when the
	 *            load factor is reached.
	 */
	@SuppressWarnings("unchecked")
	public MyHashMap(int capacity, float loadFactor) {
		table = (LinkedList<MyEntry<K, V>>[]) new LinkedList[capacity];

		this.loadFactor = loadFactor;

		for (int i = 0; i < table.length; i++) {
			table[i] = new LinkedList<MyEntry<K, V>>();
		}
	}

	/**
	 * Constructs a MyHashMap with capacity 11 and load factor 0.75.
	 */
	public MyHashMap() {
		this(11, (float) 0.75);

		primes.add(11);
		primes.add(23);
		primes.add(47);
		primes.add(97);
		primes.add(197);
		primes.add(397);
		primes.add(797);
		primes.add(1597);
		primes.add(3203);
		primes.add(6421);
		primes.add(12853);
		primes.add(25717);
		primes.add(51437);
		primes.add(102877);
		primes.add(205759);
		primes.add(411527);
		primes.add(823117);
		primes.add(1646237);
		primes.add(3292489);
		primes.add(6584983);
		primes.add(13169977);
		primes.add(26339969);
		primes.add(52679969);
		primes.add(105359939);
		primes.add(210719881);
		primes.add(421439783);
		primes.add(842879579);
		primes.add(1685759167);
	}

	/**
	 * Gives the number of MyEntries in the array.
	 * 
	 * @return number of MyEntry objects in the array.
	 */
	public int size() {
		return size;
	}

	/**
	 * Tests whether or not the array is empty.
	 * 
	 * @return true if empty, false otherwise.
	 */
	public boolean isEmpty() {
		if (size == 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Removes all objects from the array.
	 */
	public void clear() {
		for (int i = 0; i < table.length; i++) {
			table[i].clear();
		}
	}

	/**
	 * Gives a String representation of the array.
	 * 
	 * @return said String representation.
	 */
	public String toString() {
		String ret = "";

		for (int i = 0; i < table.length; i++) {
			ret += ("Bucket " + i + ": " + table[i] + "\n");
		}

		return ret;
	}

	/**
	 * Adds a key, value mapping to the array. If the key already is
	 * contained, the value will be updated.
	 * 
	 * @param key
	 *            the key to be added.
	 * @param value
	 *            the value to be added.
	 * @return the previous value if the key already was contained, null
	 *         otherwise.
	 */
	public V put(K key, V value) {
		MyEntry<K, V> entry = new MyEntry<K, V>(key, value);

		int hashCode = Math.abs(key.hashCode());
		int mapping = hashCode % (table.length);

		LinkedList<MyEntry<K, V>> bucket = table[mapping];

		V ret = null;

		if (bucket.contains(entry)) {
			int index = bucket.indexOf(entry);

			ret = bucket.get(index).value;
			bucket.set(index, entry);
		} else {
			bucket.add(entry);
		}

		size++;

		if ((double) size / (double) table.length >= loadFactor) {
			resize();
		}

		return ret;
	}

	/**
	 * Gives the value associated with a specified key.
	 * 
	 * @param key
	 *            the specified key.
	 * @return the value associated. Null if the key is not found.
	 */
	public V get(K key) {
		V ret = null;

		int hashCode = Math.abs(key.hashCode());
		int mapping = hashCode % (table.length);

		LinkedList<MyEntry<K, V>> bucket = table[mapping];

		for (int i = 0; i < bucket.size(); i++) {
			MyEntry<K, V> cur = bucket.get(i);

			if (cur.key.equals(key)) {
				ret = cur.value;
			}
		}
		return ret;
	}

	/**
	 * Removes a specified object from the array.
	 * 
	 * @param key
	 *            the key corresponding to the MyEntry to be removed.
	 * @return the value associated with the removed key.
	 */
	public V remove(K key) {
		V ret = null;

		int hashCode = Math.abs(key.hashCode());
		int mapping = hashCode % (table.length);

		LinkedList<MyEntry<K, V>> bucket = table[mapping];

		for (int i = 0; i < bucket.size(); i++) {
			MyEntry<K, V> cur = bucket.get(i);

			if (cur.key.equals(key)) {
				ret = cur.value;
				bucket.remove(cur);
			}
		}

		size--;
		return ret;
	}

	/**
	 * Finds whether or not the array contains a given key.
	 * 
	 * @param key
	 *            the key to be tested.
	 * @return true if found, false if not.
	 */
	public boolean containsKey(K key) {
		boolean ret = false;

		int hashCode = Math.abs(key.hashCode());
		int mapping = hashCode % (table.length);

		LinkedList<MyEntry<K, V>> bucket = table[mapping];
		MyEntry<K, V> test = new MyEntry<K, V>(key, null);

		if (bucket.contains(test)) {
			ret = true;
		}

		return ret;
	}

	/**
	 * Finds whether or not the array contains a given value.
	 * 
	 * @param value
	 *            the value to be tested.
	 * @return true if found, false if not.
	 */
	public boolean containsValue(V value) {
		boolean ret = false;

		for (int i = 0; i < table.length; i++) {
			LinkedList<MyEntry<K, V>> bucket = table[i];

			for (int j = 0; j < bucket.size(); j++) {
				MyEntry<K, V> entry = bucket.get(j);

				if (entry.value.equals(value))
					ret = true;
			}
		}
		return ret;
	}

	/**
	 * Iterates over the MyEntry objects in the array, looking at their
	 * keys.
	 * 
	 * @return a keys iterator.
	 */
	public Iterator<K> keys() {
		return new MyIterator<K, V>(this);
	}

	/**
	 * Resizes the array to the next largest prime number that is at least
	 * double the current array size.
	 */
	public void resize() {
		MyHashMap<K, V> tmp = new MyHashMap<K, V>(helper(table.length),
				(float) 0.75);
		Iterator<K> it = keys();

		while (it.hasNext()) {
			K key = it.next();
			V value = get(key);
			tmp.put(key, value);
		}

		this.table = tmp.table;
		this.size = tmp.size;
		this.loadFactor = tmp.loadFactor;
	}

	/**
	 * Gives the next largest prime number that is at least double the
	 * current array size.
	 * 
	 * @param i
	 * @return
	 */
	public int helper(int i) {
		int ret = -1;

		for (int j = 0; j < primes.size(); j++) {
			if (primes.get(j) > i) {
				ret = primes.get(j);
				break;
			}
		}

		return ret;
	}

	/**
	 * Tests the MyHashMap class and its methods.
	 * 
	 */
	public static void main(String[] args) {
		// test1:
		/*
		 * MyHashMap<String, Integer> testMap = new MyHashMap<String,
		 * Integer>();
		 * 
		 * System.out.println("HashMap size: "+testMap.size());
		 * System.out.println("HashMap capacity: "+testMap.table.length);
		 * System.out.println("HashMap Load Factor: "+testMap.loadFactor);
		 * System.out.println("HashMap isEmpty? "+testMap.isEmpty());
		 * System.out.println("HashMap toString: "+testMap);
		 */

		// test2:
		MyHashMap<String, Integer> testMap = new MyHashMap<String, Integer>();

		for (int i = 0; i < 100; i++) {
			testMap.put("" + i, i);
			System.out.println("Hashtable:");
			System.out.println(testMap);
		}

		for (int i = 0; i < 100; i++) {
			String key = "" + i;
			System.out.println("Removing key " + key + ":");
			testMap.remove(key);
			System.out.println(testMap);
		}
	}
}
