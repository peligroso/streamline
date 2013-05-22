package org.juxtapose.streamline.util.net;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.juxtapose.streamline.producer.ISTMEntryKey;

/**
 * @author Pontus
 *
 */
public abstract class ReferenceStore
{
	HashMap<Integer, ISTMEntryKey> referenceToKey = new HashMap<Integer, ISTMEntryKey>();
	HashMap< ISTMEntryKey, Integer> keyToReference = new HashMap<ISTMEntryKey, Integer>();
	
	AtomicInteger referenceIncrement = new AtomicInteger( 0 );
	
	public abstract int createReference();
	
	/**
	 * @param inRef
	 * @param inKey
	 */
	protected void addReference( int inRef, ISTMEntryKey inKey )
	{
		referenceToKey.put( inRef, inKey );
		keyToReference.put( inKey, inRef );
	}
	
	public int addReference( ISTMEntryKey inKey )
	{
		Integer ref = createReference();
		addReference( ref, inKey );
		
		return ref;
	}
	
	/**
	 * @param inRef
	 */
	public void removeReference( int inRef )
	{
		referenceToKey.remove( inRef );
	}
	
	/**
	 * @param inRef
	 * @return
	 */
	public ISTMEntryKey getKeyFromRef( Integer inRef )
	{
		return referenceToKey.get( inRef );
	}
	
	/**
	 * @param inKey
	 * @return
	 */
	public Integer getRefFromKey( ISTMEntryKey inKey )
	{
		return keyToReference.get( inKey );
	}
	
	public Set<ISTMEntryKey> getAllKeys()
	{
		return keyToReference.keySet();
	}
	
	public void clear()
	{
		keyToReference.clear();
		referenceToKey.clear();
	}
}
