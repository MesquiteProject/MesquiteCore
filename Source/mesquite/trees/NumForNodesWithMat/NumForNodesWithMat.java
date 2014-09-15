/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.NumForNodesWithMat;

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
/**Supplies numbers for each node of a tree.*/

public class NumForNodesWithMat extends NumbersForNodes {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(MatrixSourceCoord.class, getName() + "  needs a source of matrices.",
		"");
		EmployeeNeed e2 = registerEmployeeNeed(NumbersForNodesAndMatrix.class, getName() + "  needs a method to calculate values for the nodes using matrices.",
		"The method to calculate values can be chosen initially");
	}
	/*.................................................................................................................*/
	private MatrixSourceCoord matrixSourceTask;
	private NumbersForNodesAndMatrix numAndMatrixTask;
	Taxa taxa;
	protected MCharactersDistribution observedStates;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		if (NumForNodesWithMat.class.isAssignableFrom(getHiredAs()))
			return true;
		//assume hired as NumbersForNodes; thus responsible for getting matrices
		matrixSourceTask = (MatrixSourceCoord)hireCompatibleEmployee(MatrixSourceCoord.class, getCharacterClass(), "Source of matrix (for " + getName() + ")");
		if (matrixSourceTask == null)
			return sorry(getName() + " couldn't start because no source of matrix (for " + getName() + ") was obtained");


		numAndMatrixTask = (NumbersForNodesAndMatrix)hireCompatibleEmployee(NumbersForNodesAndMatrix.class, getCharacterClass(), "Calculator (for " + getName() + ")");
		if (numAndMatrixTask == null)
			return sorry(getName() + " couldn't start because no calculator (for " + getName() + ") was obtained");
		return true;
	}
	/*.................................................................................................................*/
	public Class getCharacterClass() {
		return null;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("getMatrixSource ", matrixSourceTask); 
		temp.addLine("getNumNodesSource ", numAndMatrixTask); 
		return temp;
	}
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets module supplying matrices", "[name of module]", commandName, "setMatrixSource")) {//temporary, for data files using old system without coordinators
			if (matrixSourceTask!=null)
				return matrixSourceTask.doCommand(commandName, arguments, checker);
		}
		else if (checker.compare(this.getClass(), "Returns module supplying matrices", null, commandName, "getMatrixSource")) {
			return matrixSourceTask;
		}
		else if (checker.compare(this.getClass(), "Returns module supplying numbers", null, commandName, "getNumNodesSource")) {
			return numAndMatrixTask;
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		if (employee instanceof MatrixSourceCoord) {
			observedStates = null;
			parametersChanged(notification);
		}

	}
	public Class getDutyClass() {
		return NumForNodesWithMat.class;
	}
	public String getDutyName() {
		return "Numbers for Nodes of Tree using a matrix";
	}
	public void calculateNumbers(Tree tree, NumberArray result, MesquiteString resultString){
	   	clearResultAndLastResult(result);
		if (tree!=null)
			taxa = tree.getTaxa();
		if (observedStates ==null)
			observedStates = matrixSourceTask.getCurrentMatrix(tree);
		numAndMatrixTask.calculateNumbers(tree, observedStates, result, resultString);
		saveLastResult(result);
		saveLastResultString(resultString);
	}


	/*.................................................................................................................*/
	/** Generated by an employee who quit.  The MesquiteModule should act accordingly. */
	public void employeeQuit(MesquiteModule employee) {
		if (employee == matrixSourceTask || employee == numAndMatrixTask)  // character source quit and none rehired automatically
			iQuit();
 		super.employeeQuit(employee);
	}
	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
	 happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Tree tree){
		if (tree == null)
			return;
		taxa = tree.getTaxa();
		if (matrixSourceTask!=null) {
			matrixSourceTask.initialize(tree.getTaxa());
			if (observedStates ==null)
				observedStates = matrixSourceTask.getCurrentMatrix(tree);
		}
		if (numAndMatrixTask!=null)
			numAndMatrixTask.initialize(tree, observedStates);
	}


	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 110;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Supplies numbers, based on a matrix, for each node of a tree.";
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Number for Nodes using Matrix";
	}
	public String getNameAndParameters(){
		if (numAndMatrixTask != null)
			return numAndMatrixTask.getNameAndParameters();
		return super.getNameAndParameters();
	}

}


