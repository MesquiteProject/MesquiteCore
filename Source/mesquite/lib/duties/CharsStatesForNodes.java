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
/**Assigns states to nodes based on a character source.*/

public abstract class CharsStatesForNodes extends MesquiteModule  {
	public static final int version = 2; //TODO: what's this???
	public int oldNumTaxa = 0; //TODO: are these needed????
	public int oldNumChars = 0;

   	 public Class getDutyClass() {
   	 	return CharsStatesForNodes.class;
   	 }
 	public String getDutyName() {
 		return "Assign states of characters to nodes";
   	 }
	public abstract MCharactersHistory calculateStates(Tree tree, MCharactersDistribution observedStates, MCharactersHistory resultStates, MesquiteString resultString);
}



