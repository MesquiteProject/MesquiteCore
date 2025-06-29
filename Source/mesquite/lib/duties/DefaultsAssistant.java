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

import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteCommand;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.ui.MesquiteCMenuItemSpec;
import mesquite.lib.ui.MesquiteMenuItemSpec;
import mesquite.lib.ui.MesquiteMenuSpec;
import mesquite.lib.ui.MesquiteSubmenuSpec;


/* ======================================================================== */
/**Serves to hold a window for an employer.*/

public abstract class DefaultsAssistant extends MesquiteModule   {

   	 public Class getDutyClass() {
   	 	return DefaultsAssistant.class;
   	 }
 	public String getDutyName() {
 		return "Defaults Assistant";
   	 }

   	public boolean isSubstantive(){
   		return false;  
   	}
   	public MesquiteCMenuItemSpec addCheckMenuItemToDefaults(MesquiteMenuSpec mms, String label, MesquiteCommand command, MesquiteBoolean b){
   		return MesquiteTrunk.mesquiteTrunk.addCheckMenuItemToSubmenu(MesquiteTrunk.fileMenu, MesquiteTrunk.defaultsSubmenu, label, command, b);
   	}
	public MesquiteMenuItemSpec addMenuItemToDefaults(MesquiteMenuSpec whichMenu, String label, MesquiteCommand command){
  		return addMenuItem(label, command);
   	}
	public MesquiteMenuItemSpec addMenuItemToDefaults(String label, MesquiteCommand command){
  		return MesquiteTrunk.mesquiteTrunk.addItemToSubmenu(MesquiteTrunk.fileMenu, MesquiteTrunk.defaultsSubmenu, label, command);
   	}
	public MesquiteSubmenuSpec addSubmenuToDefaults(String submenuName){
 		MesquiteSubmenuSpec mss = MesquiteTrunk.mesquiteTrunk.addSubmenu(MesquiteTrunk.defaultsSubmenu, submenuName);
 		return mss;
 	}
}


