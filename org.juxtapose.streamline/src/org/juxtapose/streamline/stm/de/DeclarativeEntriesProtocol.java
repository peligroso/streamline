package org.juxtapose.streamline.stm.de;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import org.juxtapose.streamline.producer.ISTMEntryKey;
import org.juxtapose.streamline.producer.ProducerUtil;
import org.juxtapose.streamline.producer.STMEntryKey;
import org.juxtapose.streamline.stm.osgi.DataProducerService;
import org.juxtapose.streamline.util.producerservices.ProducerServiceConstants;

import com.sun.org.apache.xerces.internal.impl.dv.ValidatedInfo;

public class DeclarativeEntriesProtocol 
{
	static String TYPE_INT = "I";
	static String TYPE_STRING = "S";
	static String TYPE_DECIMAL = "D";
	static String TYPE_LONG = "L";
	
	static void parseFile( String inUri )throws IOException
	{
		File file = new File( inUri );
		
		try 
		{
			Scanner scan = new Scanner( file );
			int[] keyCols = getKeyColumns( scan.nextLine().split( ",\\s*?" ) );
			
			String typeLine = scan.nextLine();
			
			String cols[] = scan.nextLine().split( ",\\s*?" );
			
			String dataTypeLine = scan.nextLine();
			String dataTypes[] = dataTypeLine.split( ",\\s*?" );
			
			if( cols.length != dataTypes.length )
			{
				throw new IOException("Length of columns does not match number of data type definitions");
			}
			if( !validateDataTypes( dataTypes ) )
				throw new IOException("Invalid data type definition: "+dataTypeLine);
			
			while( scan.hasNextLine() )
			{
				String line = scan.nextLine();
				String split[] = line.split( ",\\s*?" );
				
				ISTMEntryKey key = createKey( typeLine, keyCols, dataTypes, cols, split );
				
				if( split.length != dataTypes.length )
				{
					throw new IOException("Length of line does not match number of data type definitions, Line: "+line);
				}
			}
		} 
		catch ( FileNotFoundException e ) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param inType
	 * @param inKeyCols
	 * @param inDataTypes
	 * @param inKeys
	 * @param inLine
	 * @return
	 */
	public static ISTMEntryKey createKey( String inType, int[] inKeyCols, String[] inDataTypes, String inColumns[], String[] inLine )
	{
		ArrayList<String> keys= new ArrayList<String>();
		ArrayList<String> values= new ArrayList<String>();
		
		for( int i : inKeyCols )
		{
			String key = inColumns[i-1].trim();
			String value = inLine[i-1].trim();
			
			keys.add( key );
			values.add( value );
		}
		
		return ProducerUtil.createDataKey( ProducerServiceConstants.DE_SERVICE_KEY, inType, keys.toArray( new String[]{} ), values.toArray( new String[]{} ) );
	}
	
	public static int[] getKeyColumns( String[] inKeys )
	{
		int keys[] = new int[inKeys.length];
		
		int i = 0;
		for( String k : inKeys )
		{
			keys[i] = Integer.parseInt( k.trim() );
			i++;
		}
		
		return keys;
	}
	
	public static boolean validateDataTypes( String inTypes[] )
	{
		for( String type : inTypes )
		{
			type = type.trim();
			if( TYPE_INT.equals( type ) )
				continue;
			else if( TYPE_DECIMAL.equals( type ))
				continue;
			else if( TYPE_LONG.equals( type ))
				continue;
			else if( TYPE_STRING.equals( type ))
				continue;
			else
				return false;
		}
		return true;
	}
}
