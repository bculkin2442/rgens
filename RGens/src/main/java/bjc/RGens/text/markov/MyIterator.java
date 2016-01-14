package bjc.RGens.text.markov;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

public class MyIterator<K, V> implements Iterator<K> {
	private final MyHashMap<K, V> attachedMap;

	public MyIterator(MyHashMap<K, V> myHashMap) {
		attachedMap = myHashMap;
		bucket = attachedMap.table[bucketIndex];
	}

	int bucketIndex = 0;

	LinkedList<MyEntry<K, V>>	bucket;
	Iterator<MyEntry<K, V>>		listIt	= bucket.iterator();

	MyEntry<K, V> cur;

	/**
	 * Finds whether or not there is a next MyEntry object in the array.
	 * 
	 * @return
	 */
	@Override
	public boolean hasNext() {
		if (listIt.hasNext()) {
			return true;
		} else if (bucketIndex == (attachedMap.table.length - 1)) {
			return false;
		} else {
			for (int i = bucketIndex
					+ 1; i < attachedMap.table.length; i++) {
				if (!attachedMap.table[i].isEmpty()) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Gives the key associated with the next MyEntry object in the array.
	 * Throws a NoSuchElementException if the array has been completely
	 * iterated.
	 * 
	 * @return the key associated with the next MyEntry object.
	 */
	@Override
	public K next() {
		K ret = null;

		if (listIt.hasNext()) {
			cur = listIt.next();
			ret = cur.key;
		} else if (hasNext()) {
			for (int i = bucketIndex
					+ 1; i < attachedMap.table.length; i++) {
				if (!attachedMap.table[i].isEmpty()) {
					bucketIndex = i;

					bucket = attachedMap.table[bucketIndex];
					listIt = bucket.iterator();
					cur = listIt.next();

					ret = cur.key;
					break;
				}
			}
		} else
			throw new NoSuchElementException("All buckets iterated.");
		return ret;
	}

	/**
	 * Removes the current MyEntry.
	 */
	@Override
	public void remove() {
		bucket.remove(cur);
	}
}