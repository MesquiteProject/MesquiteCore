/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.NumForTreeWith2Chars;

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class NumForTreeWith2Chars extends NumberForTree {
	public String getName() {
		return "Tree value using 2 characters";
	}

	public String getVeryShortName() {
		if (numberTask ==null)
			return "Tree value using 2 characters";
		else
			return numberTask.getVeryShortName();
	}

	public String getNameForMenuItem() {
		return "Tree value using 2 characters....";
	}

	public String getExplanation() {
		return "Coordinates the calculation of a number for a tree based on 2 characters." ;
	}

	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(NumberFor2CharAndTree.class, getName() + "  needs a method to calculate values for trees using characters.",
		"The method to calculate values can be seslected initially or using the Values submenu");
		EmployeeNeed e2 = registerEmployeeNeed(CharSourceCoordObed.class, getName() + "  needs a source of characters.",
		"");
	}
	/*.................................................................................................................*/
	public  Class getHireSubchoice(){
		return NumberFor2CharAndTree.class;
	}
	/*.................................................................................................................*/
	NumberFor2CharAndTree numberTask;
	CharSourceCoordObed characterSourceTask;
	Taxa taxa;
	MesquiteString numberTaskName;
	MesquiteString charSourceName;
	MesquiteCommand ntC;
	MesquiteCommand cstC;
	int currentCharX = 0;
	int currentCharY = 1;
	MesquiteNumber lastValue;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (arguments !=null) {
			numberTask = (NumberFor2CharAndTree)hireNamedEmployee(NumberFor2CharAndTree.class, arguments);
			if (numberTask == null)
				return sorry(getName() + " couldn't start because the requested calculator module wasn't successfully hired.");
		}
		else {
			numberTask = (NumberFor2CharAndTree)hireEmployee(NumberFor2CharAndTree.class, "Value to calculate for tree with two characters");
			if (numberTask == null)
				return sorry(getName() + " couldn't start because no calculator module obtained.");
		}

		ntC =makeCommand("setNumberTask",  this);
		numberTask.setHiringCommand(ntC);
		numberTaskName = new MesquiteString();
		numberTaskName.setValue(numberTask.getName());
		if (numModulesAvailable(NumberFor2CharAndTree.class)>1) {
			MesquiteSubmenuSpec mss = addSubmenu(null, "Values", ntC, NumberFor2CharAndTree.class);
			mss.setSelected(numberTaskName);
		}
		characterSourceTask = (CharSourceCoordObed)hireCompatibleEmployee(CharSourceCoordObed.class, numberTask.getCompatibilityTest(), "Source of Characters (for " + numberTask.getName() + ")");
		if (characterSourceTask == null)
			return sorry(getName() + " couldn't start because no source of characters was obtained.");
		addMenuItem( "Next Character (X)", makeCommand("nextCharacterX",  this));
		addMenuItem( "Previous Character (X)", makeCommand("previousCharacterX",  this));
		addMenuItem( "Choose Character (X)...", makeCommand("chooseCharacterX",  this));

		addMenuItem( "Next Character (Y)", makeCommand("nextCharacterY",  this));
		addMenuItem( "Previous Character (Y)", makeCommand("previousCharacterY",  this));
		addMenuItem( "Choose Character (Y)...", makeCommand("chooseCharacterY",  this));
		lastValue = new MesquiteNumber();
		return true;
	}
	/* ................................................................................................................. */
	/** Returns the purpose for which the employee was hired (e.g., "to reconstruct ancestral states" or "for X axis"). */
	public String purposeOfEmployee(MesquiteModule employee) {
		if (employee == characterSourceTask)
			return "for " + numberTask.getName(); // to be overridden
		return "";
	}		//mb.setExplanationByWhichHired

	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	public boolean returnsMultipleValues(){
		return numberTask.returnsMultipleValues();
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	public void employeeQuit(MesquiteModule m){
		iQuit();
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("setNumberTask ", numberTask);  
		temp.addLine("getCharacterSource ",characterSourceTask);
		temp.addLine("setCharacterX " + CharacterStates.toExternal(currentCharX));
		temp.addLine("setCharacterY " + CharacterStates.toExternal(currentCharY));
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the module that calculates numbers for characters with the current tree", "[name of module]", commandName, "setNumberTask")) {
			NumberFor2CharAndTree temp =  (NumberFor2CharAndTree)replaceEmployee(NumberFor2CharAndTree.class, arguments, "Number for character and tree", numberTask);
			if (temp!=null) {
				numberTask = temp;
				numberTask.setHiringCommand(ntC);
				numberTaskName.setValue(numberTask.getName());
				if (characterSourceTask != null)
					characterSourceTask.setHiringCondition(numberTask.getCompatibilityTest());
				resetContainingMenuBar();
				parametersChanged();
				return numberTask;
			}
		}
		else if (checker.compare(this.getClass(), "Sets module supplying characters", "[name of module]", commandName, "setCharacterSource")) {//temporary, for data files using old system without coordinators
			return characterSourceTask.doCommand(commandName, arguments, checker);
		}
		else if (checker.compare(this.getClass(), "Returns module supplying characters", null, commandName, "getCharacterSource")) {
			return characterSourceTask;
		}
		else if (checker.compare(this.getClass(), "Returns most recent value calculated", null, commandName, "getMostRecentNumber")) {
			return lastValue;
		}
		else if (checker.compare(this.getClass(), "Goes to next character (X)", null, commandName, "nextCharacterX")) {
			if (currentCharX>=characterSourceTask.getNumberOfCharacters(taxa)-1)
				currentCharX=0;
			else
				currentCharX++;
			//charStates = null;
			parametersChanged(); //?
		}
		else if (checker.compare(this.getClass(), "Goes to previous character (X)", null, commandName, "previousCharacterX")) {
			if (currentCharX<=0)
				currentCharX=characterSourceTask.getNumberOfCharacters(taxa)-1;
			else
				currentCharX--;
			//charStates = null;
			parametersChanged(); //?
		}
		else if (checker.compare(this.getClass(), "Queries the user about what character to use (X)", null, commandName, "chooseCharacterX")) {
			int ic=characterSourceTask.queryUserChoose(taxa, " to calculate value for tree (X, currently " + (currentCharX+1) + ")");
			if (MesquiteInteger.isCombinable(ic)) {
				currentCharX = ic;
				//charStates = null;
				parametersChanged(); //?
			}
		}
		else if (checker.compare(this.getClass(), "Sets the character to use (X)", "[character number]", commandName, "setCharacterX")) {
			int icNum = MesquiteInteger.fromFirstToken(arguments, stringPos);
			if (!MesquiteInteger.isCombinable(icNum))
				return null;
			int ic = CharacterStates.toInternal(icNum);
			if ((ic>=0) && characterSourceTask.getNumberOfCharacters(taxa)==0) {
				currentCharX = ic;
				//charStates = null;
			}
			else if ((ic>=0) && (ic<=characterSourceTask.getNumberOfCharacters(taxa)-1)) {
				currentCharX = ic;
				//charStates = null;
				parametersChanged(); //?
			}
		}
		else if (checker.compare(this.getClass(), "Goes to next character (Y)", null, commandName, "nextCharacterY")) {
			if (currentCharY>=characterSourceTask.getNumberOfCharacters(taxa)-1)
				currentCharY=0;
			else
				currentCharY++;
			//charStates = null;
			parametersChanged(); //?
		}
		else if (checker.compare(this.getClass(), "Goes to previous character (Y)", null, commandName, "previousCharacterY")) {
			if (currentCharY<=0)
				currentCharY=characterSourceTask.getNumberOfCharacters(taxa)-1;
			else
				currentCharY--;
			//charStates = null;
			parametersChanged(); //?
		}
		else if (checker.compare(this.getClass(), "Queries the user about what character to use (Y)", null, commandName, "chooseCharacterY")) {
			int ic=characterSourceTask.queryUserChoose(taxa, " to calculate value for tree (Y, currently " + (currentCharY+1) + ")");
			if (MesquiteInteger.isCombinable(ic)) {
				currentCharY = ic;
				//charStates = null;
				parametersChanged(); //?
			}
		}
		else if (checker.compare(this.getClass(), "Sets the character to use", "[character number]", commandName, "setCharacterY")) {
			int icNum = MesquiteInteger.fromFirstToken(arguments, stringPos);
			if (!MesquiteInteger.isCombinable(icNum))
				return null;
			int ic = CharacterStates.toInternal(icNum);
			if ((ic>=0) && characterSourceTask.getNumberOfCharacters(taxa)==0) {
				currentCharY = ic;
				//charStates = null;
			}
			else if ((ic>=0) && (ic<=characterSourceTask.getNumberOfCharacters(taxa)-1)) {
				currentCharY = ic;
				//charStates = null;
				parametersChanged(); //?
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Tree tree){
		taxa = tree.getTaxa();
		CharacterDistribution charStatesX  = characterSourceTask.getCharacter(tree, currentCharX);
		CharacterDistribution charStatesY  = characterSourceTask.getCharacter(tree, currentCharX);
		numberTask.initialize(tree, charStatesX, charStatesY);
		if (taxa==null)
			taxa = getProject().chooseTaxa(containerOfModule(), "Taxa"); 
	}
	public void endJob(){
		if (taxa!=null)
			taxa.removeListener(this);
		super.endJob();
	}
	MesquiteString rs = new MesquiteString();
	/*.................................................................................................................*/
	public void calculateNumber(Tree tree, MesquiteNumber result, MesquiteString resultString) {
		lastValue.setToUnassigned();
		if (result==null || tree == null)
			return;
		clearResultAndLastResult(result);

		if (taxa==null){
			initialize(tree);
		}
		CharacterDistribution charStatesX = characterSourceTask.getCharacter(tree, currentCharX);
		CharacterDistribution charStatesY = characterSourceTask.getCharacter(tree, currentCharY);
		rs.setValue("");
		numberTask.calculateNumber(tree, charStatesX, charStatesY, result, rs);
		if (resultString!=null) {
			resultString.setValue("For characters " + (currentCharX+1) + " and " + (currentCharY+1) + ", ");
			resultString.append(rs.toString());
		}
		lastValue.setValue(result);
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		if (employee==characterSourceTask) {
			currentCharX = 0;
			currentCharY = 1;
			parametersChanged(notification);
		}
		else
			super.employeeParametersChanged(employee, source, notification);
	}
	/*.................................................................................................................*/
	public String getParameters(){
		return "Calculator: " + numberTask.getName() + " with characters " + (currentCharX+1) + " and " + (currentCharY+1); 
	}
	/*.................................................................................................................*/
	public String getNameAndParameters(){
		return numberTask.getName() + " with characters " + (currentCharX+1) + " and " + (currentCharY+1); 
	}
	/*.................................................................................................................*/


}

