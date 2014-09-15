/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.stochchar.lib;

import java.awt.*;
import java.util.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.stochchar.lib.*;

/* ======================================================================== */
/** A character model for Categorical characters OTHER THAN MOLECULAR to be used in stochastic simulations and in likelihood calculations.
It must serve both for calculating probabilities (via the transitionProbability method) and for simulating character evolution
(via the evolveState method).*/
public abstract class ProbPhenCategCharModel  extends ProbabilityCategCharModel {
	public ProbPhenCategCharModel (String name, Class dataClass) {
		super(name, dataClass);
	}
	//this should be pushed deeper into the superclasses
	public String[] parameterNames(){
		return null;
	}
 
    // This is supported by both current subclasses and is needed for stochastic character mapping
    // PEM 6-Jan-2006 
    /** Returns instanteous rate from beginning to ending state in given tree on the given node */
   public abstract double instantaneousRate(int beginState, int endState, Tree tree, int node);
	
	/* NOT READY YET; depends on instantaneousRate
	public String showMatrix(){
		String s = "";
		for (int iFrom = 0; iFrom<=maxState; iFrom++){
			s += "[";
			for (int iTo = 0; iTo<=maxState; iTo++){
				if (iTo == iFrom)
					s += " -";
				else
					s += " " + MesquiteDouble.toString(instantaneousRate(iFrom, iTo, null, 0));
			}
			s += " ]\n";
		}
		return s;
	}
	*/
	public String[] estimatedParameterNames(){
		return null;
	}
	public abstract void deassignParameters();
}

