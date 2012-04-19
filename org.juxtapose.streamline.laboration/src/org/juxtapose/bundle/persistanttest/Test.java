package org.juxtapose.bundle.persistanttest;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import com.trifork.clj_ds.IPersistentMap;
import com.trifork.clj_ds.PersistentHashMap;


public class Test 
{
	static boolean SYNC = false;
	
	public static class STM
	{
		private AtomicReference<IPersistentMap<Integer, String>> m_theMap = new AtomicReference<IPersistentMap<Integer,String>>();
		
		public volatile int m_retrys = 0;
		
		public STM()
		{
			IPersistentMap<Integer, String> map = PersistentHashMap.emptyMap();
			m_theMap.set(map);
		}
		
		public IPersistentMap<Integer, String> getData()
		{
			return m_theMap.get();
		}
		
		public void addValue( Integer inKey, String inValue )
		{
			if( SYNC )
			{
				synchronized (m_theMap) 
				{
					m_theMap.set( getData().assoc( inKey, inValue ) );
				}
			}
			else
			{
				IPersistentMap<Integer, String> oldMap;
				IPersistentMap<Integer, String> newMap;

				int retry = 0;

				do
				{
					if( retry > 0 )
						m_retrys++;

					oldMap = getData();
					newMap = oldMap.assoc(inKey, inValue);

					retry++;
				}
				while( !m_theMap.compareAndSet(oldMap, newMap));
			}
		}
		
	}
	
	static int ENTRYS = 10000;
	static Random RANDOM = new Random();
	
	static STM STM = new STM();
	
	public class Agent
	{
		volatile Test m_test;
		volatile public int m_id;
		
		public Agent( final Test inTest, final int inId )
		{
			m_test = inTest;
			m_id = inId;
		}
		
		public void start()
		{
			Thread myThread = new Thread(new Runnable()
			{
				@Override
				public void run() 
				{
					
					
					for( int i = 0; i < ENTRYS; i++ )
					{
						Integer key = RANDOM.nextInt( Integer.MAX_VALUE );
						String value = key.toString();
						
						STM.addValue( key, value );
						
						if( !STM.getData().containsKey( key ) )
						{
							throw new NullPointerException("THE STM DOESN'T CONTAIN THE NEW KEY");
						}
					}
					m_test.imDone( m_id );
				}

			});
			
			myThread.start();
		}
	}

	Set<Integer> m_agentIds = new HashSet<Integer>();
	volatile long m_start;
	
	int THREADS = 4;
	
	public Test()
	{
		synchronized( m_agentIds )
		{
			m_start = System.nanoTime();
			
			for( int i = 0; i < THREADS; i++ )
			{
				Agent agent = new Agent( this, i);
				m_agentIds.add( i );
				
				agent.start();
			}
		}
	}
	
	public void imDone( int inAgentId )
	{
		synchronized( m_agentIds )
		{
			m_agentIds.remove( inAgentId );
			if( m_agentIds.isEmpty() )
			{
				long time = System.nanoTime() - m_start;
				
				long timePerEntry = time / (ENTRYS * THREADS);
				
				time /= 1000;
				
				System.out.println("It took: "+time+" us   retrys: "+STM.m_retrys+" time per entry: "+timePerEntry+" ns");
			}
		}
	}
	
	public static void main( String... inArgs )
	{
		Test test = new Test();
	}
	
}
