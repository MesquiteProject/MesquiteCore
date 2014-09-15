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
/**Supplies a number for a tree.*/

public abstract class NumberForTreeWContext extends MesquiteModule  {

   	 public Class getDutyClass() {
   	 	return NumberForTreeWContext.class;
   	 }
 	public String getDutyName() {
 		return "Number for Tree with Context";
   	 }
	 public String getFunctionIconPath(){
   		 return getRootImageDirectoryPath() + "functionIcons/treeNumber.gif";
   	 }

 	//superclass for numberForTree.  If modules subclasses NumberForTreeWContext but not NumberForTree, it will have this method called
 	//otherwise, if it subclasses NumberForTree, the following method may or may not get called.  The rule should be however that
 	//if it was hired as a NumberForTreeWContext then this method will get called; if hired as a NumberForTree then calculateNumber will get called instead.
	public  abstract void calculateNumberInContext(Tree tree, TreeSource source, int whichTree, MesquiteNumber result, MesquiteString resultString);
}

