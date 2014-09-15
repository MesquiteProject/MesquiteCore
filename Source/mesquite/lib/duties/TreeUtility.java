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
/**This should NOT alter the tree.  New in 1.04*/

public abstract class TreeUtility extends MesquiteModule  {

   	 public Class getDutyClass() {
   	 	return TreeUtility.class;
   	 }
 	public String getDutyName() {
 		return "Tree Utility";
   	 }
	 public String getFunctionIconPath(){
   		 return getRootImageDirectoryPath() + "functionIcons/treeWindowUtil.gif";
   	 }
  	 
   	 public abstract void useTree(Tree tree);
   	 
   	 /* for now it's assumed that the utility is used once then shut down.
   	 public boolean pleaseLeaveMeOn() {
   	 	return false;
   	 }
   	 */
}



