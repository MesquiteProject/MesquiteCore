/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.parsimony.Treelength;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.parsimony.lib.*;


/* ======================================================================== */
public class Treelength extends NumberForTree {
	public String getName() {
		return "Treelength";
	}
	public String getExplanation() {
		return "Calculates the parsimony length of a tree.";
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed

		EmployeeNeed e = registerEmployeeNeed(mesquite.parsimony.TreelengthForMatrix.TreelengthForMatrix.class, getName() + " coordinates treelength calculation, and uses another module to help.",
				"You don't need to do anything to arrange this.");
		//e.setAsEntryPoint(true);
		EmployeeNeed e2 = registerEmployeeNeed(MatrixSourceCoord.class, getName() + " needs a character matrix on which to calculate treelength.",
				"You can request the source of character matrices when " + getName() + " starts, or later under the submenu \"Matrix Source\" or the submenu \"Source of character matrices\".");
		//e.setAsEntryPoint(true);

	}
	/*.................................................................................................................*/
	MesquiteNumber treelength;
	NumberForMatrixAndTree lengthTask;
	MatrixSourceCoord characterSourceTask;
	Taxa oldTaxa = null;

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		treelength=new MesquiteNumber();
		lengthTask = (NumberForMatrixAndTree)hireNamedEmployee(NumberForMatrixAndTree.class, "#TreelengthForMatrix");
		if (lengthTask == null)
			return sorry(getName() + " couldn't start because no treelength calculator was obtained.");
		characterSourceTask = (MatrixSourceCoord)hireEmployee(MatrixSourceCoord.class, "Source of characters (for treelength)");
		if (characterSourceTask == null)
			return sorry(getName() + " couldn't start because no source of characters was obtained.");
		return true;
	}
	public void employeeQuit(MesquiteModule m){
		if (m==lengthTask || m == characterSourceTask)
			iQuit();
	}

	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Tree tree){
		characterSourceTask.initialize(tree.getTaxa());
	}
	/*.................................................................................................................*/
	public void calculateNumber(Tree tree, MesquiteNumber result, MesquiteString resultString) {
		if (result==null)
			return;
		if (tree == null)
			return;
	   	clearResultAndLastResult(result);
		if (resultString!=null)
			resultString.setValue("");

		Taxa taxa = tree.getTaxa();

		MCharactersDistribution matrix = characterSourceTask.getCurrentMatrix(tree);


		if (matrix == null) {
			if (resultString!=null)
				resultString.setValue("Treelength not calculated; no matrix supplied");
			return;
		}
		lengthTask.calculateNumber(tree, matrix, result, resultString);
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets module supplying characters", "[name of module]", commandName, "setCharacterSource")) { //temporary, for data files using old system without coordinators
			return characterSourceTask.doCommand(commandName, arguments, checker);
		}
		else
			return  super.doCommand(commandName, arguments, checker);
	}
	/*.................................................................................................................*/
	public boolean biggerIsBetter() {
		return false;
	}

	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		if (employee==characterSourceTask) {
			parametersChanged(notification);
		}
		else if (employee==lengthTask) {
			parametersChanged(notification);
		}
	}
	/*.................................................................................................................*/
	public String getParameters() {
		return characterSourceTask.getParameters();
	}
	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return false;
	}
	/*.................................................................................................................*/
	public boolean showCitation(){
		return true;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
}

