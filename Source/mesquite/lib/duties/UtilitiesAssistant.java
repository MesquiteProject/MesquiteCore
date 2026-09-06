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

import mesquite.lib.MesquiteCommand;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.ui.MesquiteMenuItemSpec;
import mesquite.lib.ui.MesquiteMenuSpec;
import mesquite.lib.ui.MesquiteSubmenuSpec;


/* ======================================================================== */
/**Serves to hold a window for an employer.*/

public abstract class UtilitiesAssistant extends MesquiteModule   {

   	 public Class getDutyClass() {
   	 	return UtilitiesAssistant.class;
   	 }
 	public String getDutyName() {
 		return "Utilities Assistant";
   	 }

   	public boolean isSubstantive(){
   		return false;  
   	}
   	
   	/*NOTE: Utilities assistants should use these instead of the standard ways to make menu items
   	*/
   	public MesquiteMenuItemSpec addMenuItemToUtilitiesSubmenu(String label, MesquiteCommand command){
   		return addItemToSubmenu(MesquiteTrunk.mesquiteTrunk.fileMenu, MesquiteTrunk.mesquiteTrunk.utilitiesSubmenu, label, command);
   	}
   	public MesquiteSubmenuSpec addSubmenuToUtilitiesSubmenu(String label, MesquiteCommand command){
   		return addSubmenu(MesquiteTrunk.mesquiteTrunk.utilitiesSubmenu, label, command);
   	}

}


