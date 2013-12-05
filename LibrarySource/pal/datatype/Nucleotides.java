// NucleotideData.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.datatype;


/**
 * implements DataType for nucleotides
 *
 * @version $Id: Nucleotides.java,v 1.8 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Korbinian Strimmer
 * @author Alexei Drummond
 */
public class Nucleotides extends SimpleDataType {
	public static final int A_STATE = 0;
  public static final int C_STATE = 1;
	public static final int G_STATE = 2;
	public static final int UT_STATE = 3;


	boolean isRNA_;


	public Nucleotides() {
  	this(false);
  }

  /** If isRNA is true than getChar(state) will return a U instead of a T */
  public Nucleotides(boolean isRNA) {
  	this.isRNA_ = isRNA;
  }

	// Get number of bases
	public int getNumStates()
	{
		return 4;
	}

		/**
		* @retrun true if this state is an unknown state
		*/
	public boolean isUnknownState(int state) {
		return(state>=4);
	}
	// Get state corresponding to character c
	// NOTE: IF YOU CHANGE THIS IT MAY STOP THE NUCLEOTIDE TRANSLATOR FROM WORKING!
	//  - It relies on the fact that all the states for 'ACGTU' are between [0, 3]
	public int getState(char c)
	{
		switch (c)
		{
 			case 'A':
				return A_STATE;
			case 'C':
				return C_STATE;
			case 'G':
				return G_STATE;
			case 'T':
				return UT_STATE;
			case 'U':
				return UT_STATE;

			case UNKNOWN_CHARACTER:
				return 4;

			default:
				return 4;
		}
	}

	// Get character corresponding to a given state
	public char getChar(int state) {
		switch (state)
		{
			case A_STATE:
				return 'A';
			case C_STATE:
				return 'C';
			case G_STATE:
				return 'G';
			case UT_STATE:
				return (isRNA_ ? 'U' :'T');

			case 4:
				return UNKNOWN_CHARACTER;

			default:
				return UNKNOWN_CHARACTER;
		}
	}

	// String describing the data type
	public String getDescription()	{
		return "Nucleotide";
	}

	// Get numerical code describing the data type
	public int getTypeID() {
		return 0;
	}

	/**
		Returns true if A->G, G->A, C->T, or T->C
		if firstState equals secondState returns FALSE!
	*/
	public final boolean isTransistionByState(int firstState, int secondState) {
		switch(firstState) {
			case A_STATE: {
				if(secondState==G_STATE) {
					return true;
				}
				return false;
			}
			case C_STATE : {
				if(secondState==UT_STATE) {
					return true;
				}
				return false;
			}
			case G_STATE : {
				if(secondState==A_STATE) {
					return true;
				}
				return false;
			}
			case UT_STATE : {
				if(secondState==C_STATE) {
					return true;
				}
				return false;
			}
		}
		return false;
	}
	/**
		Returns true if A->G, G->A, C->T, or T->C
		if firstState equals secondState returns FALSE!
		(I've renamed things to avoid confusion between java typing of ints and chars)
	*/
	public final boolean isTransistionByChar(char firstChar, char secondChar) {
		//I'm leaving it open to a possible optimisation if anyone cares.
		return isTransistionByState(getState(firstChar), getState(secondChar));
	}
}

