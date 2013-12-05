// SimpleDataType.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


// Known bugs and limitations:
// - all states must have a non-negative value 0..getNumStates()-1
// - ? (unknown state) has value getNumStates()


package pal.datatype;

import java.io.Serializable;

/**
 * interface for sequence data types
 *
 * @version $Id: SimpleDataType.java,v 1.3 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Alexei Drummond
 */
public abstract class SimpleDataType implements DataType
{
	
	/**
	 * returns true if this state is an ambiguous state.
	 */
	public final boolean isAmbiguousState(int state) {
		return false;
	}
	
	/**
	 * returns an array containing the non-ambiguous states that this state represents.
	 */
	public final int[] getSimpleStates(int state) {
		int[] simple = new int[1];
		simple[0] = state;
		return simple;
	}

	/**
	 * get number of unique non-ambiguous states
	 *
	 * @return number of unique states
	 */
	public final int getNumSimpleStates() {
		return getNumStates();
	}

	/**
	 * Returns true if state is unknown
	 */
	public boolean isUnknownState(int state) {
		return state >= getNumStates();
	}
}

