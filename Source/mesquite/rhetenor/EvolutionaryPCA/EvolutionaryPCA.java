/* Mesquite source code (Rhetenor package).  Copyright 1997 and onward E. Dyreson and W. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.rhetenor.EvolutionaryPCA;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.cont.lib.*;
import mesquite.rhetenor.lib.*;

/* ======================================================================== */
public class EvolutionaryPCA extends Ordinator {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(OneTreeSource.class, getName() + "  needs a source of a current tree.",
		"The source of current tree is selected initially");
	}
	/*.................................................................................................................*/
	OneTreeSource treeTask;
	EPCAOrdination ord;
	String treeString;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		treeTask = (OneTreeSource)hireEmployee(OneTreeSource.class, "Source of tree for ePCA");
 		if (treeTask == null)
 			return sorry(getName() + " couldn't start because no source of trees was obtained.");
 		return true;
 	}
  	 public void employeeQuit(MesquiteModule m){
  	 	iQuit();
  	 }
	/*.................................................................................................................*/
 	public Ordination getOrdination(MContinuousDistribution matrix, int item, Taxa taxa){
 		double[][] x = matrix.getMatrix(item);//gets first 2Dmatrix from original
		Tree tree;
		ord =  new EPCAOrdination(x, tree = treeTask.getTree(taxa), true); //only one!
 		if (tree !=null)
 			treeString = tree.getName();
 		else
 			treeString = "NO TREE";
 		return ord;
 	}
	/*.................................................................................................................*/
    	public String getName() {
		return "Evolutionary Principal Components";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Performs Maddison and Dyreson's evolutionary principal components analysis on a continous-valued matrix." ;
   	 }
   	 
	/*.................................................................................................................*/
   	 public boolean showCitation(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
	/*.................................................................................................................*/
    	 public String getParameters() {
   		return "Evolutionary principal components analysis with tree: " + treeString;
   	 }
	/*.................................................................................................................*/
}


