package org.juxtapose.streamline.util;


/**
 * @author Pontus
 *
 * @param <T>
 */
public class PersistentArrayList<T> 
{
	private final T[] arr;
	
	public PersistentArrayList()
	{
		arr = (T[])new Object[0];
	}
	
	public PersistentArrayList( T[] inArr )
	{
		arr = inArr;
	}
	
	public int size()
	{
		return arr.length;
	}
	
	public T get( int i )
	{
		return arr[i];
	}
	
	/**
	 * @param inEl
	 * @return
	 */
	public PersistentArrayList<T> add( T inEl )
	{
		T[] newArr = (T[]) new Object[ arr.length +1 ];
		System.arraycopy( arr, 0, newArr, 0, arr.length );
	
		newArr[arr.length] = inEl;
		
		return new PersistentArrayList( newArr );
	}
	
	public PersistentArrayList<T> add(int index, T e)
	{
		T[] newData = (T[]) new Object[arr.length + 1];
		System.arraycopy(arr, 0, newData, 0, index);
		newData[index] = e;
		System.arraycopy(arr, index, newData, index + 1, arr.length - index);
		
		return new PersistentArrayList( newData );
	}


	/**
	 * @param inEl
	 * @return
	 */
	public PersistentArrayList<T> remove( T inEl )
	{
		T[] newData = (T[]) new Object[arr.length - 1];

		// search the element to remove while filling the backup array
		// this way we can run this method in O(n)
		int elementIndex = -1;
		for (int i = 0; i < arr.length; i++)
		{
			if ( inEl.equals(arr[i]) )
			{
				elementIndex = i;
				break;
			}

			if (i < newData.length)
				newData[i] = arr[i];
		}

		if (elementIndex < 0)
			return this;

		System.arraycopy(arr, elementIndex + 1, newData, elementIndex,arr.length - elementIndex - 1);
		return new PersistentArrayList<T>( newData );
	}
	
	/**
	 * @param index
	 * @return
	 */
	public PersistentArrayList<T> remove(int index)
	{
		if (index < 0 || index >= arr.length)
			throw new IndexOutOfBoundsException("index = " +  index);

		T[] newData = (T[]) new Object[arr.length - 1];

		T result = arr[index];

		if (index > 0)
			System.arraycopy(arr, 0, newData, 0, index);

		System.arraycopy(arr, index + 1, newData, index,
				arr.length - index - 1);

		return new PersistentArrayList<T>( newData );

	}

}
