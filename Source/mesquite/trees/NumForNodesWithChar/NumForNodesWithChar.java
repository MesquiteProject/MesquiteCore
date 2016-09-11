/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.NumForNodesWithChar;

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
/**Suppliies numbers for each node of a tree.*/

public class NumForNodesWithChar extends NumbersForNodes {
	public String getName() {
		return "Number for Nodes using Character";
	}
	public String getExplanation() {
		return "Supplies numbers, based on a character, for each node of a tree.";
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(CharSourceCoordObed.class, getName() + "  needs a source of characters.",
		"");
	}
	/*.................................................................................................................*/
	CharSourceCoordObed characterSourceTask;
	private NumbersForNodesAndChar numAndCharTask;
	Object dataCondition;
	Taxa taxa;
	MesquiteString numberTaskName;
	MesquiteCommand ntC;

	int currentChar = 0;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		numAndCharTask = (NumbersForNodesAndChar)hireCompatibleEmployee(NumbersForNodesAndChar.class, getCharacterClass(), "Calculator (for " + getName() + ")");
		if (numAndCharTask == null)
			return sorry(getName() + " couldn't start because no calculator (for " + getName() + ") was obtained");
		//assume hired as NumbersForNodes; thus responsible for getting characters
		characterSourceTask = (CharSourceCoordObed)hireCompatibleEmployee(CharSourceCoordObed.class, numAndCharTask.getCompatibilityTest(), "Source of characters (for " + numAndCharTask.getName() + ")");
		if (characterSourceTask == null)
			return sorry(getName() + " couldn't start because no source of characters was obtained.");
		ntC =makeCommand("setNumberTask",  this);
		numAndCharTask.setHiringCommand(ntC);
		numberTaskName = new MesquiteString();
		numberTaskName.setValue(numAndCharTask.getName());
		if (numModulesAvailable(NumberForCharAndTree.class)>1) {
			MesquiteSubmenuSpec mss = addSubmenu(null, "Values (for nodes with char.)", ntC, NumberForCharAndTree.class);
			mss.setSelected(numberTaskName);
		}
		addMenuItem( "Next Character", makeCommand("nextCharacter",  this));
		addMenuItem( "Previous Character", makeCommand("previousCharacter",  this));
		addMenuItem( "Choose Character...", makeCommand("chooseCharacter",  this));
		addMenuSeparator();


		return true;
	}
	/*.................................................................................................................*/
	public Class getCharacterClass() {
		return null;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("getCharacterSource ", characterSourceTask); 
		temp.addLine("setNumNodesSource ", numAndCharTask); 
		temp.addLine("setCharacter " + CharacterStates.toExternal(currentChar));
		return temp;
	}
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Returns module supplying matrices", null, commandName, "getCharacterSource")) {
			return characterSourceTask;
		}
		else if (checker.compare(this.getClass(), "Returns module supplying numbers", null, commandName, "setNumNodesSource")) {
			NumbersForNodesAndChar temp =  (NumbersForNodesAndChar)replaceEmployee(NumbersForNodesAndChar.class, arguments, "Number for nodes", numAndCharTask);
			if (temp!=null) {
				numAndCharTask = temp;
				numAndCharTask.setHiringCommand(ntC);
				numberTaskName.setValue(numAndCharTask.getName());
				parametersChanged();
				return numAndCharTask;
			}
			return numAndCharTask;
		}
		else if (checker.compare(this.getClass(), "Goes to next character", null, commandName, "nextCharacter")) {
			if (currentChar>=characterSourceTask.getNumberOfCharacters(taxa)-1)
				currentChar=0;
			else
				currentChar++;
			//charStates = null;
			parametersChanged(); //?
		}
		else if (checker.compare(this.getClass(), "Goes to previous character", null, commandName, "previousCharacter")) {
			if (currentChar<=0)
				currentChar=characterSourceTask.getNumberOfCharacters(taxa)-1;
			else
				currentChar--;
			//charStates = null;
			parametersChanged(); //?
		}
		else if (checker.compare(this.getClass(), "Queries the user about what character to use", null, commandName, "chooseCharacter")) {
			int ic=characterSourceTask.queryUserChoose(taxa, " to calculate value for tree ");
			if (MesquiteInteger.isCombinable(ic)) {
				currentChar = ic;
				//charStates = null;
				parametersChanged(); //?
			}
		}
		else if (checker.compare(this.getClass(), "Sets the character to use", "[character number]", commandName, "setCharacter")) {
			int icNum = MesquiteInteger.fromFirstToken(arguments, stringPos);
			if (!MesquiteInteger.isCombinable(icNum))
				return null;
			int ic = CharacterStates.toInternal(icNum);
			if ((ic>=0) && characterSourceTask.getNumberOfCharacters(taxa)==0) {
				currentChar = ic;
				//charStates = null;
			}
			else if ((ic>=0) && (ic<=characterSourceTask.getNumberOfCharacters(taxa)-1)) {
				currentChar = ic;
				//charStates = null;
				parametersChanged(); //?
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public void calculateNumbers(Tree tree, NumberArray result, MesquiteString resultString){
	   	clearResultAndLastResult(result);
		CharacterDistribution observedStates = characterSourceTask.getCharacter(tree, currentChar);
		numAndCharTask.calculateNumbers(tree, observedStates, result, resultString);
		saveLastResult(result);
		saveLastResultString(resultString);
	}


	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
	 happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Tree tree){
		if (tree == null)
			return;
		taxa = tree.getTaxa();
		if (characterSourceTask!=null) {
			characterSourceTask.initialize(tree.getTaxa());
		}
	}

	/*.................................................................................................................*/
	public String getParameters(){
		return "Calculator: " + numAndCharTask.getName() ; 
	}
	/*.................................................................................................................*/
	public String getNameAndParameters(){
		return numAndCharTask.getName(); 
	}
	/*.................................................................................................................*/
	/** Generated by an employee who quit.  The MesquiteModule should act accordingly. */
	public void employeeQuit(MesquiteModule employee) {
		if (employee == characterSourceTask || employee == numAndCharTask)  
			iQuit();
		super.employeeQuit(employee);
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
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}

}

