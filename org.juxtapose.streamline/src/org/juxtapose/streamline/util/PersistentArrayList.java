package org.juxtapose.streamline.util;


/**
 * @author Pontus
 *
 * @param <T>
 */
public class PersistentArrayList<T> 
{
	public static final int DEFAULT_SIZE = 32;
	
	private final int size;
	private final T[] arr;
	
	public PersistentArrayList()
	{
		arr = (T[])new Object[DEFAULT_SIZE];
		size = DEFAULT_SIZE;
	}
	
	public PersistentArrayList( T[] inArr )
	{
		arr = inArr;
		size = inArr.length;
	}
	
	private PersistentArrayList( T[] inArr, int inSize )
	{
		arr = inArr;
		size = inArr.length;
	}
	
	public int size()
	{
		return size;
	}
	
	public T get( int i )
	{
		if( i > size-1 )
			throw new ArrayIndexOutOfBoundsException();
		
		return arr[i];
	}
	
	/**
	 * @param inEl
	 * @return
	 */
	public PersistentArrayList<T> add( T inEl )
	{
		if( size < arr.length )
		{
			arr[size] = inEl;
			return new PersistentArrayList<T>( arr, size+1 );
		}
		else
		{
			T[] newArr = (T[]) new Object[ size + DEFAULT_SIZE ];
			System.arraycopy( arr, 0, newArr, 0, size );

			newArr[size] = inEl;

			return new PersistentArrayList<T>( newArr, size+1 );
		}
	}
	
	/**
	 * @param index
	 * @param e
	 * @return
	 */
	public PersistentArrayList<T> add(int index, T e)
	{
		T[] newData = (T[]) new Object[size + 1];
		System.arraycopy(arr, 0, newData, 0, index);
		newData[index] = e;
		System.arraycopy(arr, index, newData, index + 1, size - index);
		
		return new PersistentArrayList<T>( newData, size+1 );
	}


	/**
	 * @param inEl
	 * @return
	 */
	public PersistentArrayList<T> remove( T inEl )
	{
		T[] newData = (T[]) new Object[size - 1];

		// search the element to remove while filling the backup array
		// this way we can run this method in O(n)
		int elementIndex = -1;
		for (int i = 0; i < size; i++)
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

		System.arraycopy(arr, elementIndex + 1, newData, elementIndex, size - elementIndex - 1);
		
		return new PersistentArrayList<T>( newData, size-1 );
	}
	
	/**
	 * @param index
	 * @return
	 */
	public PersistentArrayList<T> remove(int index)
	{
		if (index < 0 || index >= size)
			throw new IndexOutOfBoundsException("index = " +  index);

		T[] newData = (T[]) new Object[size - 1];

		T result = arr[index];

		if (index > 0)
			System.arraycopy(arr, 0, newData, 0, index);

		System.arraycopy(arr, index + 1, newData, index,
				size - index - 1);

		return new PersistentArrayList<T>( newData, size-1 );

	}

}
