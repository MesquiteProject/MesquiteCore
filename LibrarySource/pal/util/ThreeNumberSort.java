// ThreeNumberSort.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

 
package pal.util;

import pal.math.*;

/**
 * sorts three numbers (doubles) and choose randomly among the
 * minimum/maximum values
 *
 * @version $Id: ThreeNumberSort.java,v 1.4 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Korbinian Strimmer
 */
public class ThreeNumberSort
{
	//
	// Public stuff
	//

	/** the three numbers */
	public double[] numbers;

	/** order of the numbers (0-2) */
	public int first, second, third;

	/** constructor */
	public ThreeNumberSort()
	{
		numbers = new double[3];
		rng = new MersenneTwisterFast();
	}

	/**
	 * get input index of largest number
	 *
	 * @return index (if two or even all three numbers are
	 *               of equally large value the index is drawn at
	 *               random from that subset
	 */
	public int getIndexOfLargestNumber()
	{
		int numBest = 1;
		double valBest = numbers[first];
		
		if (numbers[second] == valBest)
		{
			numBest++;
			if (numbers[third] == valBest)
			{
				numBest++;
			}
		}
		
		if (numBest == 1)
		{
			return first;
		}
		else if (numBest == 2)
		{
			double rand = 2.0*rng.nextDouble();
			
			if (rand < 1.0)
			{
				return first;
			}
			else
			{
				return second;
			}
		}
		else
		{	
			double rand = 3.0*rng.nextDouble();
			
			if (rand < 1.0)
			{
				return first;
			}
			else if (rand < 2.0)
			{
				return second;
			}
			else
			{
				return third;
			}
		}
	}

	/**
	 * get input index of smallest number
	 *
	 * @return index (if two or even all three numbers are
	 *               of equally small value the index is drawn at
	 *               random from that subset
	 */
	public int getIndexOfSmallestNumber()
	{
		int numBest = 1;
		double valBest = numbers[third];
		
		if (numbers[second] == valBest)
		{
			numBest++;
			if (numbers[first] == valBest)
			{
				numBest++;
			}
		}
		
		if (numBest == 1)
		{
			return third;
		}
		else if (numBest == 2)
		{
			double rand = 2.0*rng.nextDouble();
			
			if (rand < 1.0)
			{
				return third;
			}
			else
			{
				return second;
			}
		}
		else
		{	
			double rand = 3.0*rng.nextDouble();
			
			if (rand < 1.0)
			{
				return third;
			}
			else if (rand < 2.0)
			{
				return second;
			}
			else
			{
				return first;
			}
		}
	}

	/**
	 * get smallest number
	 *
	 * @return number
	 */
	public double getSmallestNumber()
	{
		return numbers[getIndexOfSmallestNumber()];
	}

	/**
	 * get largest number
	 *
	 * @return number
	 */
	public double getLargestNumber()
	{
		return numbers[getIndexOfLargestNumber()];
	}

	/**
	 * sort three doubles
	 *
	 * @param n0 first double
	 * @param n1 second double
	 * @param n2 third double
	 */
	public void sort(double n0, double n1, double n2)
	{	
		numbers[0] = n0;
		numbers[1] = n1;
		numbers[2] = n2;
	
		if (n0 > n1)
		{
			if(n2 > n0)
			{
				first = 2;
				second = 0;
				third = 1;		
			}
			else if (n2 < n1)
			{
				first = 0;
				second = 1;
				third = 2;		
			}
			else
			{
				first = 0;
				second = 2;
				third = 1;		
			}
		}
		else
		{
			if(n2 > n1)
			{
				first = 2;
				second = 1;
				third = 0;		
			}
			else if (n2 < n0)
			{
				first = 1;
				second = 0;
				third = 2;		
			}
			else
			{
				first = 1;
				second = 2;
				third = 0;		
			}
		}
	}
	
	//
	// Private stuff
	//
	
	MersenneTwisterFast rng;
}

