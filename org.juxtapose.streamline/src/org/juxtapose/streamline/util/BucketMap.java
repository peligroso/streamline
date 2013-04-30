package org.juxtapose.streamline.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

public class BucketMap<K, V>
{
	private final HashMap<K, Set<V>> m_keyToBucket;

	public BucketMap()
	{
		m_keyToBucket = new HashMap<K, Set<V>>();
	}

	public BucketMap(BucketMap<K, V> inBucketMap)
	{
		m_keyToBucket = new HashMap<K, Set<V>>();

		Set<Entry<K, Set<V>>> entries = inBucketMap.m_keyToBucket.entrySet();
		for(Entry<K, Set<V>> entry:entries)
		{
			HashSet<V> values = new HashSet<V>(entry.getValue());
			m_keyToBucket.put(entry.getKey(), values);
		}
	}

	/**
	 * @param inKey
	 * @param inValue
	 */
	public Set<V> put(K inKey, V inValue)
	{
		Set<V> bucket = m_keyToBucket.get(inKey);
		if(bucket != null)
		{
			bucket.add(inValue);
			return bucket;
		}

		bucket = new HashSet<V>();
		bucket.add(inValue);

		m_keyToBucket.put( inKey, bucket );

		return null;
	}

	/**
	 * @param inKey
	 * @return
	 */
	public Set<V> get(K inKey)
	{
		return m_keyToBucket.get( inKey );
	}

	public Set<K> keySet()
	{
		return m_keyToBucket.keySet();
	}

	/**
	 * Removes the mapping for the specified key from this map if present.
	 *
	 * @param  inKey key whose mapping is to be removed from the map
	 * @return the previous value associated with <tt>key</tt>, or
	 *         <tt>null</tt> if there was no mapping for <tt>key</tt>.
	 *         (A <tt>null</tt> return can also indicate that the map
	 *         previously associated <tt>null</tt> with <tt>key</tt>.)
	 */
	public Set<V> remove(K inKey)
	{
		return m_keyToBucket.remove(inKey);
	}

	/**
	 * @param inKey
	 * @param inValue
	 * @return true if the value was removed from the bucket
	 */
	public boolean remove(K inKey,V inValue)
	{
		Set<V> bucket = get(inKey);

		if(bucket != null)
		{
			boolean success = bucket.remove(inValue);

			if(bucket.size() == 0)
			{
				m_keyToBucket.remove(inKey);
			}

			return success;
		}

		return false;
	}

	/**
	 * Returns the number of key-bucket mappings in this bucket map.
	 *
	 * @return the number of key-bucket mappings in this bucket map
	 */
	public int size()
	{
		return m_keyToBucket.size();
	}

	/**
	 * @return
	 */
	public Vector<V> values()
	{
		Vector<V> ret = new Vector<V>();

		Collection<Set<V>> coll = m_keyToBucket.values();

		Iterator<Set<V>> iter = coll.iterator();
		while( iter.hasNext() )
		{
			Set<V> set = iter.next();
			ret.addAll(set);
		}

		return ret;
	}

	/**
	 * 
	 */
	public void clear()
	{
		m_keyToBucket.clear();
	}

	public boolean isEmpty()
	{
		return m_keyToBucket.isEmpty();
	}

}
