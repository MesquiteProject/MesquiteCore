// DataTypeUtils.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.datatype;

import java.io.Serializable;

/**
 * helper class for sequence data types.
 *
 * @version $Id: DataTypeUtils.java,v 1.3 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Korbinian Strimmer
 * @author Alexei Drummond
 */
public class DataTypeUtils implements Serializable
{
	//
	// Public stuff
	//

	/**
	 * create object according to this code
	 *
	 * @param typeID selected data type
	 *
	 * @return DataType object
	 */
	public static DataType getInstance(int typeID)
	{
		if (typeID == DataType.NUCLEOTIDES)
		{
			return new Nucleotides();
		}
		else if (typeID == DataType.AMINOACIDS)
		{
			return new AminoAcids();
		}
		else if (typeID == DataType.TWOSTATES)
		{
			return new TwoStates();
		}
		else if (typeID == DataType.CODONS)
		{
			return new Codons();
		}
		else if (typeID == DataType.GENERALIZEDCODONS)
		{
			return new Codons();
		}
		else
		{
			throw new IllegalArgumentException("Wrong typeID");
		}
	}

	/**
	 * Return states of a sequence.
	 */
	public final static int[] getSequenceStates(DataType d, String sequence) {
		
		int[] states = new int[sequence.length()];
		
		for (int i = 0; i < sequence.length(); i++) {
			states[i] = d.getState(sequence.charAt(i));
		}
		return states;
	}

	/**
	 * returns true if the character represents a gap in the sequence.
	 */
	public final static boolean isGap(DataType d, char c) {
		return d.getState(c) == d.getNumStates();
	}
}

