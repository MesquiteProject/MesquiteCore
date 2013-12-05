// Comparable.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.util;

/**
 * interface for an object that is comparable.
 * This interface is analogous to the Comparable interface in
 * Java 1.2, and it should be superceded by the JDK 1.2 collections
 * framework when PAL is moved to 1.2.
 *
 * @version $Id: Comparable.java,v 1.2 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Alexei Drummond
 */
public interface Comparable
{
	/**
	 * Returns a number representing the ordering relationship that
	 * the object has with the given object.
	 * A negative number indicates that the object is "smaller" than
	 * the parameter, a positive number means it is "larger" and zero
	 * indicates that the objects are equal.
	 */
	int compareTo(Object o);

	/**
	 * Returns true if this object is equal to the given object.
	 */
	boolean equals(Object o);
}

