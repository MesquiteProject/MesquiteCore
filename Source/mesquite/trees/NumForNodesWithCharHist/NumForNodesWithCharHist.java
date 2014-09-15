/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.NumForNodesWithCharHist;

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
/**Suppliies numbers for each node of a tree.*/

public class NumForNodesWithCharHist extends NumbersForNodesAndMatrix {
	public String getName() {
		return "Numbers for Nodes from Character Reconstructions";
	}
	public String getExplanation() {
		return "Supplies numbers, based on a character reconstruction, for each node of a tree.";
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(CharsStatesForNodes.class, getName() + "  needs a source of character histories.",
		"The source of character histories can be selected initially");
		EmployeeNeed e2 = registerEmployeeNeed(NumbersForNodesAndHistory.class, getName() + "  needs a method to calculate values from the character histories.",
		"The method to calculate values can be selected initially");
	}
	/*.................................................................................................................*/
	CharsStatesForNodes allCharsTask;
	NumbersForNodesAndHistory numAndHistoryTask;
	MCharactersHistory ancestralStates;

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		allCharsTask = (CharsStatesForNodes)hireEmployee(CharsStatesForNodes.class, "Reconstruction method");
		if ( allCharsTask == null ) {
			return sorry(getName() + " couldn't start because no character reconstructor was obtained.");
		}
		numAndHistoryTask = (NumbersForNodesAndHistory)hireCompatibleEmployee(NumbersForNodesAndHistory.class, getCharacterClass(), "Calculator (for " + getName() + ")");
		if (numAndHistoryTask == null)
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
		temp.addLine("getReconstructor", allCharsTask);
		temp.addLine("getNumNodesSource ", numAndHistoryTask); 
		return temp;
	}

	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Returns reconstructor", null, commandName, "getReconstructor")) {
			return allCharsTask;
		}
		else if (checker.compare(this.getClass(), "Returns numbers for nodes and history task", null, commandName, "getNumNodesSource")) {
			return numAndHistoryTask;
		}
		else
			return  super.doCommand(commandName, arguments, checker);
	}

	/*.................................................................................................................*/
	public void calculateNumbers(Tree tree, MCharactersDistribution data, NumberArray result, MesquiteString resultString) {
		if (tree==null || data==null)
			return;
		if (allCharsTask!=null) {
		   	clearResultAndLastResult(result);
			//note: following doesn't pass MesquiteString for results since does character by character and would only get message from last

			ancestralStates = allCharsTask.calculateStates(tree, data, ancestralStates, null);

			if (numAndHistoryTask!=null)
				numAndHistoryTask.calculateNumbers(tree,ancestralStates,result,resultString);
			saveLastResult(result);
			saveLastResultString(resultString);
		}
	}

	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
	 happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Tree tree, MCharactersDistribution observedStates){
		if (tree == null)
			return;
	}
	/*.................................................................................................................*/
	/** Generated by an employee who quit.  The MesquiteModule should act accordingly. */
	public void employeeQuit(MesquiteModule employee) {
		if (employee == allCharsTask || employee == numAndHistoryTask)  
			iQuit();
		super.employeeQuit(employee);
	}

	/*.................................................................................................................*/
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

}

