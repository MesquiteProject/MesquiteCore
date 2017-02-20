/* Mesquite.  Copyright 1997 and onward, W. Maddison, D. Maddison & Peter Midford. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lib.duties;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import mesquite.cont.lib.*;
import mesquite.stochchar.CurrentProbModelsSim.*;
import mesquite.stochchar.lib.*;

/* ======================================================================== */
public abstract class CharMapper extends MesquiteModule {
	public Class getDutyClass() {
		return CharStatesForNodes.class;
	}
	public String getDutyName() {
		return "Assign states or state changes of character to branches of tree, fully resolved, as by stochastic characterMapping";
	}
	public String[] getDefaultModule() {
		return new String[] {"#StochCharMapper"};
	}

	/*.................................................................................................................*/
	public  abstract void setObservedStates(Tree tree, CharacterDistribution observedStates);
	/*.................................................................................................................*/

	public  abstract boolean getMapping(long i, CharacterHistory resultStates, MesquiteString resultString); 
  	public  abstract void prepareForMappings(boolean permissiveOfNoSeparateMappings); 
  	
	public abstract long getNumberOfMappings();
	

	
	public String getMappingTypeName(){
		return "Mapping";
	}

	/** sets whether or not module will be requested to do one character at a time; if so, then it might know to put up menu items
	to change the model of the character, or adjust it in some other way*/
	public void setOneCharacterAtATime(boolean chgbl){}


	public abstract boolean allowsStateWeightChoice();
}


