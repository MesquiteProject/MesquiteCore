// ComparableDouble.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.util;

/** 
 * This class is unfortunate but necessary to conform to JDK 1.1
 *
 * @version $Id: ComparableDouble.java,v 1.3 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Alexei Drummond
 */
public class ComparableDouble implements Comparable {
	
	private double value;

	public ComparableDouble(double d) {
		value = d;
	}

	public int compareTo(Object o) {
		
		ComparableDouble cd = (ComparableDouble)o;

		if (value < cd.value) {
			return -1;
		} else if (value > cd.value) {
			return 1;
		} else return 0;
	}

	public boolean equals(Object o) {
	
		ComparableDouble cd = (ComparableDouble)o;
		return cd.value == value;
	}

	public double doubleValue() {
		return value;
	}

	public String toString() {
		return value + "";
	}
}

