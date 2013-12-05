// CoalescentException.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

 
package pal.coalescent;

/**
 * Exceptions to do with coalescent models et cetera.
 *
 * @version $Id: CoalescentException.java,v 1.2 2001/07/12 12:17:43 korbinian Exp $
 *
 * @author Alexei Drummond
 */
public class CoalescentException extends Exception {

	/**
	 * Parameterless constructor.
	 */
	public CoalescentException() {
		super();
	}

	/**
	 * Constructor taking message.
	 */
	public CoalescentException(String s) {
		super(s);
	}
}

