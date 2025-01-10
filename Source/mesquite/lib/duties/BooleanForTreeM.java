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
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.Tree;




/* ======================================================================== */
/**Supplies a boolean for a tree.*/

public abstract class BooleanForTreeM extends BooleanForTree  {

   	 public Class getDutyClass() {
   	 	return BooleanForTreeM.class;
   	 }
 	public String getDutyName() {
 		return "Boolean for Tree; may return modified tree";
   	 }
	public void calculateBoolean(Tree tree, MesquiteBoolean result, MesquiteString resultString){
		calculateBoolean(tree, result, resultString, null);
	}
	
	public abstract void calculateBoolean(Tree tree, MesquiteBoolean result, MesquiteString resultString, MesquiteTree modifiedTree); 
	
}

