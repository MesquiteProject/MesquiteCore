/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.genomic.ValueForMatrixLinkedToTree;

import mesquite.lib.EmployeeNeed;

import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteNumber;
import mesquite.lib.MesquiteString;
import mesquite.lib.Notification;
import mesquite.lib.characters.MCharactersDistribution;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.NumberForMatrix;
import mesquite.lib.duties.NumberForTree;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.Tree;


/* ======================================================================== */
public class ValueForMatrixLinkedToTree extends NumberForTree {
	/*.................................................................................................................*/
	public  Class getHireSubchoice(){
		return NumberForMatrix.class;
	}
	/*.................................................................................................................*/
	NumberForMatrix numberTask;
	Taxa oldTaxa = null;
    	 MCharactersDistribution matrix;

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (arguments !=null) {
			numberTask = (NumberForMatrix)hireNamedEmployee(NumberForMatrix.class, arguments);
			if (numberTask == null)
				return sorry(getName() + " couldn't start because the requested calculator module wasn't successfully hired.");
		}
		else {
		numberTask = (NumberForMatrix)hireEmployee(NumberForMatrix.class, "Value to calculate for linked matrices");
 		if (numberTask == null)
 			return sorry(getName() + " couldn't start because no counting module was obtained.");
		}
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
   	}
	MesquiteString cs = new MesquiteString();
	/*.................................................................................................................*/
	public void calculateNumber(Tree tree, MesquiteNumber result, MesquiteString resultString) {
    	 	if (result==null || tree == null)
    	 		return;
    	clearResultAndLastResult(result);
   	 	int count=0;
    	Taxa taxa = tree.getTaxa();
 		CharacterData data = ((MesquiteTree)tree).findLinkedMatrix(getProject());
		
		if (data == null) {
			if (resultString!=null)
				resultString.setValue("No linked matrix found");
			return;
		}
		matrix = data.getMCharactersDistribution();
		cs.setValue("");
		numberTask.calculateNumber(matrix, result, cs);
		if (resultString!=null)
			resultString.setValue(cs.getValue() + " (for matrix " + data.getName() + ")");
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	public boolean biggerIsBetter() {
		return false;
	}
	/*.................................................................................................................*/

	/*.................................................................................................................*/
   	 public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
   	 	if (employee==numberTask) {
			parametersChanged(notification);
   	 	}
   	 }
	/*.................................................................................................................*/
    	 public String getParameters() {
		return "Value calculated for linked matrix: " + numberTask.getName();
   	 }
	/*.................................................................................................................*/
    	 public String getNameAndParameters() {
    		 return numberTask.getNameAndParameters();
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Value for matrix linked to tree";
   	 }
    		/*.................................................................................................................*/
    	 public String getVeryShortName() {
		return "Linked matrix: " + numberTask.getVeryShortName();
   	 }
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Calculates a value for the matrix linked to the tree.";
   	 }
  	 
 	/*.................................................................................................................*/
 	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
 	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
 	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
 	public int getVersionOfFirstRelease(){
 		return NEXTRELEASE;  
 	}

}

