/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib.duties;

import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;


/* ======================================================================== */
/**Simulates characters.  */

public abstract class CharacterSimulator extends MesquiteModule  {

   	 public Class getDutyClass() {
   	 	return CharacterSimulator.class;
   	 }
 	public String getDutyName() {
 		return "Character Simulator";
   	 }
	 public String getFunctionIconPath(){
   		 return getRootImageDirectoryPath() + "functionIcons/simulate.gif";
   	 }
	/** Indicates the type of character created (e.g., "CategoricalState")*/ 
	public abstract Class getStateClass();
	
	/** Does any needed initializations. */ 
   	public void initialize(Taxa taxa){}
   	
	/** Does any needed cleanups after simulation. */ 
   	public void cleanupAfterSimulation(MAdjustableDistribution matrix){}
   	
   	//TODO: return also the states at internals!!!!
	/** Returns a character for the terminal taxa (place in passed CharacterDistribution if it is non-null, and return the same reference, or create
	new one otherwise). */ 
   	public abstract CharacterDistribution getSimulatedCharacter(CharacterDistribution statesAtTips, Tree tree, MesquiteLong seed, int ic);
	/** Returns a character for all the nodes in the tree (place in passed CharacterDistribution if it is non-null, and return the same reference, or create
	new one otherwise). */ 
   	public abstract CharacterHistory getSimulatedHistory(CharacterHistory statesAtNodes, Tree tree, MesquiteLong seed);
	/** Returns maximum number of characters to simulate. */ 
   	public int getMaximumNumChars(Taxa taxa){
   		return MesquiteInteger.infinite;
   	}
	/** Returns default number of characters to simulate. */ 
   	public int getDefaultNumChars(Taxa taxa){
   		return 100;
   	}
}


