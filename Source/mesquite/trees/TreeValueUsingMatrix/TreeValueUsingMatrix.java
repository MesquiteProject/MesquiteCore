/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.trees.TreeValueUsingMatrix;

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;


/* ======================================================================== */
public class TreeValueUsingMatrix extends NumberForTree {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(MatrixSourceCoord.class, getName() + "  needs a source of matrices.",
		"");
		EmployeeNeed e2 = registerEmployeeNeed(NumberForMatrixAndTree.class, getName() + "  needs a method to calculate values for the trees using a matrix.",
		"The method to calculate values can be selected initially");
	}
	/*.................................................................................................................*/
	public  Class getHireSubchoice(){
		return NumberForMatrixAndTree.class;
	}
	/*.................................................................................................................*/
	MatrixSourceCoord characterSourceTask;
	NumberForMatrixAndTree numberTask;
	Taxa oldTaxa = null;
    	 MCharactersDistribution matrix;

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (arguments !=null) {
			numberTask = (NumberForMatrixAndTree)hireNamedEmployee(NumberForMatrixAndTree.class, arguments);
			if (numberTask == null)
				return sorry(getName() + " couldn't start because the requested calculator module wasn't successfully hired.");
		}
		else {
		numberTask = (NumberForMatrixAndTree)hireEmployee(NumberForMatrixAndTree.class, "Value to calculate for trees");
 		if (numberTask == null)
 			return sorry(getName() + " couldn't start because no steps counting module was obtained.");
		}
		characterSourceTask = (MatrixSourceCoord)hireCompatibleEmployee(MatrixSourceCoord.class, numberTask.getCompatibilityTest(), "Source of characters (for " + numberTask.getName() + ")");
 		if (characterSourceTask == null)
 			return sorry(getName() + " couldn't start because no source of characters was obtained.");
  		return true;
  	 }
  	 public void employeeQuit(MesquiteModule m){
  	 		iQuit();
  	 }
 	/*===== For NumberForItem interface ======*/
  	public boolean returnsMultipleValues(){
  		if (numberTask == null)
  			return false;
   		return numberTask.returnsMultipleValues();
   	}
  	 
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
	/*.................................................................................................................*/
   	 public boolean isSubstantive(){
   	 	return false;
   	 }
   	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
   	public void initialize(Tree tree){
   		characterSourceTask.initialize(tree.getTaxa());
   	}
	MesquiteString cs = new MesquiteString();
	/*.................................................................................................................*/
	public void calculateNumber(Tree tree, MesquiteNumber result, MesquiteString resultString) {
    	 	if (result==null || tree == null)
    	 		return;
    	clearResultAndLastResult(result);
   	 	int count=0;
    	Taxa taxa = tree.getTaxa();
 		matrix = characterSourceTask.getCurrentMatrix(tree);
		
		if (matrix == null) {
			if (resultString!=null)
				resultString.setValue("Value for tree not calculated; no matrix supplied");
			return;
		}
		cs.setValue("");
		numberTask.calculateNumber(tree, matrix, result, cs);
		if (resultString!=null)
			resultString.setValue(cs.getValue() + " (for matrix " + characterSourceTask.getCurrentMatrixName(tree.getTaxa()) + ")");
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	public boolean biggerIsBetter() {
		return false;
	}
	/*.................................................................................................................*/

	/*.................................................................................................................*/
   	 public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
   	 	if (employee==characterSourceTask) {
			parametersChanged(notification);
   	 	}
   	 	else if (employee==numberTask) {
			parametersChanged(notification);
   	 	}
   	 }
	/*.................................................................................................................*/
    	 public String getParameters() {
		return "Value calculated: " + numberTask.getName() + "(Source of matrices: " + characterSourceTask.getNameAndParameters() + ")";
   	 }
	/*.................................................................................................................*/
    	 public String getNameAndParameters() {
		return numberTask.getName() + "(Source of matrices: " + characterSourceTask.getNameAndParameters() + ")";
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Tree value using character matrix";
   	 }
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Calculates a value for the tree using a character data matrix.";
   	 }
}

