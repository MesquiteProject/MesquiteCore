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

import mesquite.lib.CompatibilityTest;
import mesquite.lib.characters.CharacterStateTest;


/* ======================================================================== */
/**This is superclass of modules to assist data matrix editor.  These are INITs.*/

public abstract class DataWindowAssistantI extends DataWindowAssistant  {
   	 public Class getDutyClass() {
   	 	return DataWindowAssistantI.class;
   	 }
 	public String getDutyName() {
 		return "INIT Assistant for Data Window";
   	}
   	 public String[] getDefaultModule() {
   	 	return new String[] {"#AddDeleteData", "#AlterData"};
   	 }

	/*.................................................................................................................*/
	/** Returns CompatibilityTest so other modules know if this is compatible with some object. */
	public CompatibilityTest getCompatibilityTest(){
		return new CharacterStateTest();
	}
}


