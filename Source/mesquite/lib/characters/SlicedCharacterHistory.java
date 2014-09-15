/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib.characters; 

import mesquite.lib.*;


/* ======================================================================== */
/**A CharacterHistory that can indicate changes within branches, e.g.for stochastic character mapping*/
public interface SlicedCharacterHistory extends CharacterHistory {

	/** gets the sequence of character states along the branch below node it, starting with the ancestral node and going up.  
	The first state is the first change since the ancestral node.   
	The particular data types (e.g. continuous, categorical) can have additional more efficient methods, e.g. returning a double[] or long[]*/
	public CharacterState[] getStateSequenceOnBranch(int it);
	/** gets the timing of the changes of character states along the branch below node it.  
	Element i is the time of acquisition of the state of element i from the array returned by getStateSequenceOnBranch.
	This should be in fractions of the branch's length.  Any elements not corresponding to a state (e.g. extra spaces in the array) should be filled with -1*/
	public double[] getTimeSequenceOnBranch(int it);
}


