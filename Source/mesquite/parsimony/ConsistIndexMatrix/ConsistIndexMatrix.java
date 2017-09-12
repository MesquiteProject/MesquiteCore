/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.parsimony.ConsistIndexMatrix;
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.categ.lib.MCategoricalDistribution;
import mesquite.categ.lib.RequiresAnyCategoricalData;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.parsimony.lib.*;


/* ======================================================================== */
public class ConsistIndexMatrix extends NumberForMatrixAndTree {
	public String getName() {
		return "Consistency Index for Matrix ";
	}
	public String getExplanation() {
		return "Calculates the Consistency Index (CI) for a tree and matrix.";
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e2 = registerEmployeeNeed(mesquite.parsimony.TreelengthForMatrix.TreelengthForMatrix.class, getName() + " uses a module to calculate treelength.",
		"The treelength module is employed automatically; you don't have to do anything to choose it.");
		//e.setAsEntryPoint(true);
	}
	public int getVersionOfFirstRelease(){
		return 270;  
	}
	/*.................................................................................................................*/
	NumberForMatrixAndTree lengthTask;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		lengthTask = (NumberForMatrixAndTree)hireNamedEmployee(NumberForMatrixAndTree.class, "#TreelengthForMatrix");
		if (lengthTask == null)
			return sorry(getName() + " couldn't start because no calculator of treelength could be obtained.");
		return true;
	}
	public void employeeQuit(MesquiteModule m){
		iQuit();
	}

	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Tree tree, MCharactersDistribution matrix){
	}
	MesquiteTree bush;
	MesquiteNumber onTree = new MesquiteNumber();
	MesquiteNumber onSoftBush = new MesquiteNumber();
	/*.................................................................................................................*/
	public void calculateNumber(Tree tree, MCharactersDistribution matrix, MesquiteNumber result, MesquiteString resultString) {
		if (result==null || tree == null || matrix == null)
			return;
		if (resultString !=null)
			resultString.setValue("");
	   	clearResultAndLastResult(result);
	   	if (!(matrix instanceof MCategoricalDistribution)){
			if (resultString != null)
				resultString.setValue("C.I. is calculated only for categorical matrices.");
	   		return;
	   	}
		if (matrix==null || tree == null || !(tree instanceof MesquiteTree)) {
			return;
		}
		onTree.setToUnassigned();
		onSoftBush.setToUnassigned();

		lengthTask.calculateNumber(tree, matrix, onTree, resultString);

		if (bush == null)
			bush = new MesquiteTree(matrix.getTaxa());
		bush.setToClone((MesquiteTree)tree);  //this used rather than setToDefaultBush in case incoming tree doesn't include all taxa
		bush.collapseAllBranches(bush.getRoot(), false, false);
		/** Sets the polytomies assumption for this tree; 0= hard; 1 = soft; 2 = unassigned.*/

		bush.setPolytomiesAssumption(1, false);  //soft
		lengthTask.calculateNumber(bush, matrix, onSoftBush, resultString);

		result.setValue(onSoftBush);
		if (onTree.isZero())
			result.setValue(0);
		else
			result.divideBy(onTree);

		//(minimum)/(steps)
		saveLastResult(result);
		if (resultString != null)
			resultString.setValue("C.I.: " + result);
		saveLastResultString(resultString);
	}
	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return false;
	}
	public boolean showCitation(){
		return true;
	}
	public CompatibilityTest getCompatibilityTest(){
		return new RequiresAnyCategoricalData();
	}
}

