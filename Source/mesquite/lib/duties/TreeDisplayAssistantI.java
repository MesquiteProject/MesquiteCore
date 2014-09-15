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


/* ======================================================================== */
/** This abstract class is for modules that are TreeDisplayAssistants that are INITS.  Used by Basic Tree Window for tool modules.
 * Note: this should be renamed since these permit editing of the tree and thus are not for any tree display. TreeDisplayAssistantDEI*/
public abstract class TreeDisplayAssistantI extends TreeDisplayAssistant  {
	
   	 public Class getDutyClass() {
   	 	return TreeDisplayAssistantI.class;
   	 }
 	public String getDutyName() {
 		return "INIT Assistant for Tree Display";
   	 }

}


