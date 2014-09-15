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
/**Simulates trees.*/

public abstract class TreeSimulate extends MesquiteModule  {

   	 public Class getDutyClass() {
   	 	return TreeSimulate.class;
   	 }
	 public String getFunctionIconPath(){
   		 return getRootImageDirectoryPath() + "functionIcons/simulate.gif";
   	 }
	public String getDutyName() {
 		return "Tree Simulate";
   	 }
   	 public String[] getDefaultModule() {
   	 	return new String[] {"#EqualRatesMarkovSp"};
   	 }
   	 
   	public abstract int getNumberOfTrees(Taxa taxa);
   	 
   	public abstract void initialize(Taxa taxa);
   	public abstract Tree getSimulatedTree(Taxa taxa, Tree tree, int treeNumber, ObjectContainer extra, MesquiteLong seed);
}


