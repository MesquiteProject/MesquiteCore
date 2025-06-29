/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.diverse.lib;

import mesquite.lib.duties.NumberForTree;


/* ======================================================================== */
/**Calculates a number for a tree that give statistics about diversification.*/

public abstract class NumberForTreeDivers extends NumberForTree  {

   	 public Class getDutyClass() {
   	 	return NumberForTreeDivers.class;
   	 }
 	public String getDutyName() {
 		return "Number for Tree (Diversification)";
   	 }
	 public String getFunctionIconPath(){
   		 return getRootImageDirectoryPath() + "functionIcons/treeNumber.gif";
   	 }
  	 public String[] getDefaultModule() {
 		String[] s= {"#SpecExtincLikelihood", "#NMHBirthDeathLikelihood"};
 		return s;
   	 }
 
}


