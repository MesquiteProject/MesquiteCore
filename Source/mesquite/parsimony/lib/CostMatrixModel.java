/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.parsimony.lib;

import java.awt.*;
import java.util.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;

/* ======================================================================== */
/** A character model for Categorical characters to be used in parsimony calculations*/
public abstract class CostMatrixModel  extends CategParsimonyModel {
	
	public CostMatrixModel (String name, Class dataClass) {
		super(name, dataClass);
		allowUseOnDataSubclasses(true);
	}
	
	/** Returns cost to change from beginning to ending state.  Returned value may be undefined
	if either the begin or end states are not in list of states for which matrix defined.  Passed a
	MesquiteNumber so doesn't need to create one if instantiated.*/
	public abstract MesquiteNumber getTransitionValue (int beginState, int endState, MesquiteNumber result);
	
	public abstract boolean isSymmetrical();
	
	/** returns the set of states for which the values are defined*/
	public abstract long getStatesDefined ();
	
	/** To set what are the states available for transitions*/
	public int getMaxState (){
		return 0;
	}
}

