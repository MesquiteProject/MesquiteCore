/* Mesquite source code.  Copyright 1997-2010 W. Maddison and D. Maddison.
Version 2.74, October 2010.
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
//import mesquite.lib.duties.*;


/* ======================================================================== */
/***/

public abstract class EditRawNexusBlock extends MesquiteModule  {

   	 public Class getDutyClass() {
   	 	return EditRawNexusBlock.class;
   	 }
 	public String getDutyName() {
 		return "Edit Raw Nexus Block";
   	}
   	public abstract void editNexusBlock(NexusBlockEditableRaw block, boolean recordCurrent);
   	public abstract void recordBlock(NexusBlockEditableRaw block);
   	public abstract NexusBlockEditableRaw getCurrentBlock();
   	public boolean isSubstantive(){
   		return false;  
   	}
}


