package org.juxtapose.streamline.laboration.prime;

public class PrimeGenerator
{
	public static void main( String... inArgs )
	{
		calcRatio();
//		calcZeta();
//		int primes = 2000;
//		int primeIndex = 1;
//		
//		for( int i = 2; ; i++ )
//		{
//			if( isPrime( i ) )
//			{
//				System.out.println( "public static final int PRIME_"+primeIndex+" = "+i+";" );
//				primeIndex++;
//				
//				if( primeIndex > primes )
//				{
//					break;
//				}
//			}
//		}
	}
	
	public static boolean isPrime( int inNum )
	{
		for( int i = 2; i < inNum; i++ ){
			int n = inNum%i;
			if (n==0)
			{
				return false;
			}
		}
		return true;
	}
	
	public static void calcZeta()
	{
		int primes = 2000000;
		int primeIndex = 1;
		
		double sum = 0d;
		
		for( int i = 2; ; i++ )
		{
			if( isPrime( i ) )
			{
				double res = 1d/(double)i;
				sum += res;
				
				primeIndex++;
				
				if( primeIndex > primes )
				{
					break;
				}
			}
		}
		
		System.out.println( sum );
	}
	
	public static void calcRatio()
	{
		int primes = 1000000;
		int primeIndex = 1;
		int regIndex = 1;
		
		for( int i = 2; i < primes; i++ )
		{
			if( isPrime( i ) )
			{
				primeIndex++;
			}
			else
				regIndex++;
			
			if( i % 200 == 0 )
			{
				System.out.println( (regIndex / primeIndex) );
			}
		}
		
		
	}
}
