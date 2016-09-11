/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.NumForTreeWithChar;

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class NumForTreeWithChar extends NumberForTreeM implements Incrementable{
	public String getName() {
		return "Tree value using character";
	}
	public String getVeryShortName() {
		if (numberTask ==null)
			return "Tree value using character";
		else
			return numberTask.getVeryShortName();
	}
	public String getNameForMenuItem() {
		return "Tree value using character....";
	}
	public String getExplanation() {
		return "Coordinates the calculation of a number for a tree based on a character." ;
	}	
	
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(NumberForCharAndTree.class, getName() + "  needs a method to calculate values for trees using characters.",
		"The method to calculate values can be seslected initially or using the Values submenu");
		e.setPriority(1);
		EmployeeNeed e2 = registerEmployeeNeed(CharSourceCoordObed.class, getName() + "  needs a source of characters.",
		"");
	}
	/*.................................................................................................................*/
	public  Class getHireSubchoice(){
		return NumberForCharAndTree.class;
	}
	/*.................................................................................................................*/
	NumberForCharAndTree numberTask;
	CharSourceCoordObed characterSourceTask;
	Taxa taxa;
	Tree tree;
	MesquiteString numberTaskName;
	MesquiteCommand ntC;
	int currentChar = -1;
	Class taskClass = NumberForCharAndTree.class;
	MesquiteNumber lastValue;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (getHiredAs() == NumberForTreeM.class)
			taskClass = NumberForCharAndTreeM.class;
		else
			taskClass = NumberForCharAndTree.class;
		if (arguments !=null) {
			numberTask = (NumberForCharAndTree)hireNamedEmployee(taskClass, arguments);
			if (numberTask == null)
				return sorry(getName() + " couldn't start because the requested calculator module wasn't successfully hired.");
		}
		else {
			numberTask = (NumberForCharAndTree)hireEmployee(taskClass, "Value to calculate for tree with character");
			if (numberTask == null)
				return sorry(getName() + " couldn't start because no calculator module obtained.");
		}
		lastValue = new MesquiteNumber();
		ntC =makeCommand("setNumberTask",  this);
		numberTask.setHiringCommand(ntC);
		numberTaskName = new MesquiteString();
		numberTaskName.setValue(numberTask.getName());
		if (numModulesAvailable(taskClass)>1 && numberTask.getCompatibilityTest()==null) {
			MesquiteSubmenuSpec mss = addSubmenu(null, "Values", ntC, taskClass);
			mss.setSelected(numberTaskName);
		}
		characterSourceTask = (CharSourceCoordObed)hireCompatibleEmployee(CharSourceCoordObed.class, numberTask.getCompatibilityTest(), "Source of characters (for " + numberTask.getName() + ")");
		if (characterSourceTask == null)
			return sorry(getName() + " couldn't start because no source of characters was obtained.");
		addMenuItem( "Next Character", makeCommand("nextCharacter",  this));
		addMenuItem( "Previous Character", makeCommand("previousCharacter",  this));
		addMenuItem( "Choose Character...", makeCommand("chooseCharacter",  this));
		addMenuSeparator();
		return true;
	}
	
	public boolean returnsMultipleValues(){
		return numberTask.returnsMultipleValues();
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	public void employeeQuit(MesquiteModule m){
		iQuit();
	}
	/*.................................................................................................................*/
	public void setCurrent(long i){
		if (characterSourceTask==null || taxa==null)
			return;
		if ((i>=0) && (i<=characterSourceTask.getNumberOfCharacters(taxa)-1)) {
			currentChar = (int)i;
		}
	}
	public String getItemTypeName(){
		return "Character";
	}
	/*.................................................................................................................*/
	public long toInternal(long i){
		return(CharacterStates.toInternal((int)i));
	}
	/*.................................................................................................................*/
	public long toExternal(long i){
		return(CharacterStates.toExternal((int)i));
	}
	/*.................................................................................................................*/
	public long getCurrent(){
		return currentChar;
	}
	/*.................................................................................................................*/
	public long getMin(){
		return 0;
	}
	/*.................................................................................................................*/
	public long getMax(){
		if (characterSourceTask==null || taxa==null)
			return 0;
		return characterSourceTask.getNumberOfCharacters(taxa)-1;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("getCharacterSource ",characterSourceTask);
		temp.addLine("setCharacter " + CharacterStates.toExternal(currentChar));
		temp.addLine("setNumberTask ", numberTask);  
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the module that calculates numbers for characters with the current tree", "[name of module]", commandName, "setNumberTask")) {
			NumberForCharAndTree temp =  (NumberForCharAndTree)replaceEmployee(taskClass, arguments, "Number for character and tree", numberTask);

			if (temp!=null) {
				numberTask = temp;
				numberTask.setHiringCommand(ntC);
				numberTaskName.setValue(numberTask.getName());
				if (!MesquiteThread.isScripting())
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
		else if (checker.compare(this.getClass(), "Returns most recent value calculated", null, commandName, "getMostRecentNumber")) {
			return lastValue;
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Tree tree){
		taxa = tree.getTaxa();
		if (!MesquiteThread.isScripting() && currentChar<0){
			int ic=characterSourceTask.queryUserChoose(taxa, " to calculate value for tree ");
			if (MesquiteInteger.isCombinable(ic)) {
				currentChar = ic;
			}
			else
				currentChar = 0;
		}
		if (currentChar < 0 || !MesquiteInteger.isCombinable(currentChar)) 
			currentChar = 0;
		CharacterDistribution charStates  = characterSourceTask.getCharacter(tree, currentChar);
		numberTask.initialize(tree, charStates);
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
	public void calculateNumber(Tree tree, MesquiteNumber result, MesquiteString resultString, MesquiteTree modifiedTree) {
		lastValue.setToUnassigned();
		if (result==null || tree == null)
			return;
	   	clearResultAndLastResult(result);
		if (taxa==null){
			initialize(tree);
		}
		if (currentChar < 0 || !MesquiteInteger.isCombinable(currentChar)) 
			currentChar = 0;
		CharacterDistribution charStates = characterSourceTask.getCharacter(tree, currentChar);
		rs.setValue("");
		if (numberTask instanceof NumberForCharAndTreeM)
			((NumberForCharAndTreeM)numberTask).calculateNumber(tree, charStates, result, rs, modifiedTree);
		else
			numberTask.calculateNumber(tree, charStates, result, rs);
		if (resultString!=null) {
			resultString.setValue("For character " + (currentChar + 1) + ", ");
			resultString.append(rs.toString());
		}
		lastValue.setValue(result);
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		if (employee==characterSourceTask) {
			currentChar = 0;
			parametersChanged(notification);
		}
		else
			super.employeeParametersChanged(employee, source, notification);
	}
	/*.................................................................................................................*/
	public String getParameters(){
		if (!MesquiteInteger.isCombinable(currentChar))
			return "Calculator: " + numberTask.getName(); //of which tree??
		else
			return "Calculator: " + numberTask.getName() + " with character " + (currentChar+1); 
	}
	/*.................................................................................................................*/
	public String getNameAndParameters(){
		if (!MesquiteInteger.isCombinable(currentChar))
			return numberTask.getName();
		else
			return numberTask.getName() + " with character " +  (currentChar+1); 
	}
	/*.................................................................................................................*/



}

