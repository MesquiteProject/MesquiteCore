// HeapSort.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

 
package pal.util;

import pal.math.*;

import java.util.*;

/**
 * sorts numbers and comparable objects by treating contents of array as a binary tree.
 * KNOWN BUGS: There is a horrible amount of code duplication here!
 *
 * @version $Id: HeapSort.java,v 1.9 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Alexei Drummond
 * @author Korbinian Strimmer
 */
public class HeapSort {
	
	//
	// Public stuff
	//

	/**
	 * Sorts an array of indices to vector of comparable objects 
	 * into increasing order.
	 */
	public static void sort(Vector array, int[] indices) {
	
		// ensures we are starting with valid indices
		for (int i = 0; i < indices.length; i++) {
			indices[i] = i;
		}
	
		int temp;
		int j, n = array.size();
		
		// turn input array into a heap
		for (j = n/2; j > 0; j--) {
			adjust(array, indices, j, n);
		}

		// remove largest elements and put them at the end
		// of the unsorted region until you are finished
		for (j = n-1; j > 0; j--) {
			temp = indices[0];
			indices[0] = indices[j];
			indices[j] = temp;
			adjust(array, indices, 1, j);
		}
	}	

	/**
	 * Sorts a vector of comparable objects into increasing order.
	 */
	public static void sort(Vector array) {
	
		Object temp;
		int j, n = array.size();
		
		// turn input array into a heap
		for (j = n/2; j > 0; j--) {
			adjust(array, j, n);
		}

		// remove largest elements and put them at the end
		// of the unsorted region until you are finished
		for (j = n-1; j > 0; j--) {
			temp = array.elementAt(0);
			array.setElementAt(array.elementAt(j), 0);
			array.setElementAt(temp, j);
			adjust(array, 1, j);
		}
	}	

	/**
	 * Sorts an array of comparable objects into increasing order.
	 */
	public static void sort(pal.util.Comparable[] array) {
	
		pal.util.Comparable temp;
		int j, n = array.length;
		
		// turn input array into a heap
		for (j = n/2; j > 0; j--) {
			adjust(array, j, n);
		}

		// remove largest elements and put them at the end
		// of the unsorted region until you are finished
		for (j = n-1; j > 0; j--) {
			temp = array[0];
			array[0] = array[j];
			array[j] = temp;
			adjust(array, 1, j);
		}
	}

	/**
	 * Sorts an array of objects into increasing order given a comparator.
	 */
	public static void sort(Object[] array, Comparator c) {
	
		Object temp;
		int j, n = array.length;
		
		// turn input array into a heap
		for (j = n/2; j > 0; j--) {
			adjust(array, c, j, n);
		}

		// remove largest elements and put them at the end
		// of the unsorted region until you are finished
		for (j = n-1; j > 0; j--) {
			temp = array[0];
			array[0] = array[j];
			array[j] = temp;
			adjust(array, c, 1, j);
		}
	}	

	/**
	 * Sorts an array of doubles into increasing order.
	 */
	public static void sort(double[] array) {
	
		double temp;
		int j, n = array.length;
		
		// turn input array into a heap
		for (j = n/2; j > 0; j--) {
			adjust(array, j, n);
		}

		// remove largest elements and put them at the end
		// of the unsorted region until you are finished
		for (j = n-1; j > 0; j--) {
			temp = array[0];
			array[0] = array[j];
			array[j] = temp;
			adjust(array, 1, j);
		}
	}

	/**
	 * Sorts an array of indices into an array of doubles
	 * into increasing order.
	 */
	public static void sort(double[] array, int[] indices)
	{
	
		// ensures we are starting with valid indices
		for (int i = 0; i < indices.length; i++)
		{
			indices[i] = i;
		}
	
		int temp;
		int j, n = array.length;
		
		// turn input array into a heap
		for (j = n/2; j > 0; j--) {
			adjust(array, indices, j, n);
		}

		// remove largest elements and put them at the end
		// of the unsorted region until you are finished
		for (j = n-1; j > 0; j--) {
			temp = indices[0];
			indices[0] = indices[j];
			indices[j] = temp;
			adjust(array, indices, 1, j);
		}
	}


	/** test harness for heapsort algorithm */
	public static void main(String[] args) {
	
		MersenneTwisterFast m = new MersenneTwisterFast();

		int testSize = 100;

		// test array of Comparable objects
		
		pal.util.ComparableDouble[] test = new pal.util.ComparableDouble[testSize];
		
		for (int i = 0; i < test.length; i++) {
			test[i] = new pal.util.ComparableDouble(m.nextInt(testSize * 10));
		}

		sort(test);
		for (int i = 0; i < test.length; i++) {
			System.out.print(test[i] + " ");
		}
		System.out.println();

		// test index to Vector of Comparable objects
		
		Vector testv = new Vector();
		int[] indices = new int[testSize];
		
		for (int i = 0; i < testSize; i++) {
			testv.addElement(new pal.util.ComparableDouble(m.nextInt(testSize * 10)));
		}

		sort(testv, indices);
		for (int i = 0; i < test.length; i++) {
			System.out.print(testv.elementAt(indices[i]) + " ");
		}
		System.out.println();
		
		
		// test index to array of doubles
		
		double[] testd = new double[testSize];
		//int[] indices = new int[testSize];
		
		for (int i = 0; i < testSize; i++)
		{
			testd[i] = m.nextInt(testSize * 10);
		}

		sort(testd, indices);
		for (int i = 0; i < test.length; i++)
		{
			System.out.print(testd[indices[i]] + " ");
		}
		System.out.println();

	}

	// PRIVATE STUFF

	/**
	 * helps sort an array of indices into a vector of comparable objects.
	 * Assumes that array[lower+1] through to array[upper] is
	 * already in heap form and then puts array[lower] to
	 * array[upper] in heap form.
	 */
	private static void adjust(Vector array, int[] indices, int lower, int upper) {
	
		int j, k;
		int temp;
		
		j = lower;
		k = lower * 2;

		while (k <= upper) {
			if ((k < upper) && (((pal.util.Comparable)array.elementAt(indices[k-1])).compareTo(array.elementAt(indices[k])) < 0)) {
				k += 1;
			}
			if (((pal.util.Comparable)array.elementAt(indices[j-1])).compareTo(array.elementAt(indices[k-1])) < 0) {
				temp = indices[j-1];
				indices[j-1] = indices[k-1];
				indices[k-1] = temp;
			}
			j = k;
			k *= 2;
		}
	} 

	/**
	 * helps sort an vector of comparable objects.
	 * Assumes that array[lower+1] through to array[upper] is
	 * already in heap form and then puts array[lower] to
	 * array[upper] in heap form.
	 */
	private static void adjust(Vector array, int lower, int upper) {
	
		int j, k;
		Object temp;
		
		j = lower;
		k = lower * 2;

		while (k <= upper) {
			if ((k < upper) && (((pal.util.Comparable)array.elementAt(k-1)).compareTo(array.elementAt(k)) < 0)) {
				k += 1;
			}
			if (((pal.util.Comparable)array.elementAt(j-1)).compareTo(array.elementAt(k-1)) < 0) {
				temp = array.elementAt(j-1);
				array.setElementAt(array.elementAt(k-1), j-1);
				array.setElementAt(temp, k-1);
			}
			j = k;
			k *= 2;
		}
	} 

	/**
	 * Assumes that array[lower+1] through to array[upper] is
	 * already in heap form and then puts array[lower] to
	 * array[upper] in heap form.
	 */
	private static void adjust(pal.util.Comparable[] array, int lower, int upper) {
	
		int j, k;
		pal.util.Comparable temp;
		
		j = lower;
		k = lower * 2;

		while (k <= upper) {
			if ((k < upper) && (array[k-1].compareTo(array[k]) < 0)) {
				k += 1;
			}
			if (array[j-1].compareTo(array[k-1]) < 0) {
				temp = array[j-1];
				array[j-1] = array[k-1];
				array[k-1] = temp;
			}
			j = k;
			k *= 2;
		}
	}

	/**
	 * Assumes that array[lower+1] through to array[upper] is
	 * already in heap form and then puts array[lower] to
	 * array[upper] in heap form.
	 */
	private static void adjust(Object[] array, Comparator c, int lower, int upper) {
	
		int j, k;
		Object temp;
		
		j = lower;
		k = lower * 2;

		while (k <= upper) {
			if ((k < upper) && (c.compare(array[k-1], array[k]) < 0)) {
				k += 1;
			}
			if (c.compare(array[j-1], array[k-1]) < 0) {
				temp = array[j-1];
				array[j-1] = array[k-1];
				array[k-1] = temp;
			}
			j = k;
			k *= 2;
		}
	}

	/**
	 * helps sort an array of doubles.
	 * Assumes that array[lower+1] through to array[upper] is
	 * already in heap form and then puts array[lower] to
	 * array[upper] in heap form.
	 */
	private static void adjust(double[] array, int lower, int upper) {
	
		int j, k;
		double temp;
		
		j = lower;
		k = lower * 2;

		while (k <= upper) {
			if ((k < upper) && (array[k-1] < array[k])) {
				k += 1;
			}
			if (array[j-1] < array[k-1]) {
				temp = array[j-1];
				array[j-1] = array[k-1];
				array[k-1] = temp;
			}
			j = k;
			k *= 2;
		}
	} 
	
	/**
	 * helps sort an array of indices into an array of doubles.
	 * Assumes that array[lower+1] through to array[upper] is
	 * already in heap form and then puts array[lower] to
	 * array[upper] in heap form.
	 */
	private static void adjust(double[] array, int[] indices, int lower, int upper) {
	
		int j, k;
		int temp;
		
		j = lower;
		k = lower * 2;

		while (k <= upper)
		{
			if ((k < upper) && (array[indices[k-1]] < array[indices[k]]))
			{
				k += 1;
			}
			if (array[indices[j-1]] < array[indices[k-1]])
			{
				temp = indices[j-1];
				indices[j-1] = indices[k-1];
				indices[k-1] = temp;
			}
			j = k;
			k *= 2;
		}
	} 
}


