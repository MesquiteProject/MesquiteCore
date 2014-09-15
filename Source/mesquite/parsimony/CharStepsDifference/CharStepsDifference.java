/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.parsimony.CharStepsDifference;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.pairwise.lib.PairMaker;
import mesquite.parsimony.lib.*;


/* ======================================================================== */
public class CharStepsDifference extends NumberForTree {
	public String getName() {
		return "Difference in steps in two characters";
	}
	public String getExplanation() {
		return "Calculates the difference between two characters in parsimony steps for a given tree.";
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(CharSourceCoordObed.class, getName() + " needs a source of characters from which to calculate a difference in parsimony steps.",
				"You can request a source of characters when " + getName() + " starts, or later under the Source of Characters submenu.");
		//e.setAsEntryPoint(true);
		EmployeeNeed e2 = registerEmployeeNeed(CharacterSteps.class, getName() + " uses a module to calculate parsimony steps.",
				"The parsimony steps module is employed automatically; you don't have to do anything to choose it.");
		//e.setAsEntryPoint(true);
	}
	/*.................................................................................................................*/
	MesquiteNumber stepsA, stepsB;
	CharSourceCoordObed characterSourceTask;
	CharacterSteps charStepsTask;
	Taxa oldTaxa = null;
	CharacterDistribution charStatesA, charStatesB;
	int currentCharA = 0;
	int currentCharB = 1;
	long oldTreeID;
	long oldTreeVersion;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		stepsA=new MesquiteNumber();
		stepsB=new MesquiteNumber();
		characterSourceTask = (CharSourceCoordObed)hireEmployee(CharSourceCoordObed.class, "Source of characters");
		if (characterSourceTask == null)
			return sorry(getName() + " couldn't start because no source of characters was obtained.");
		charStepsTask = (CharacterSteps)hireEmployee(CharacterSteps.class, null);
		if (charStepsTask == null)
			return sorry(getName() + " couldn't start because no steps counting module was obtained.");
		addMenuItem( "Choose First Character...", makeCommand("chooseCharacterA",  this));
		addMenuItem( "Choose Second Character...", makeCommand("chooseCharacterB",  this));
		return true;
	}

	public void employeeQuit(MesquiteModule m){
		if (m==charStepsTask || m == characterSourceTask)
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
	   	clearResultAndLastResult(result);
		if (resultString!=null)
			resultString.setValue("");
		stepsA.setValue((int)0);
		stepsB.setValue((int)0);
		Taxa taxa = tree.getTaxa();
		if (taxa != oldTaxa && oldTaxa!=null || (characterSourceTask.usesTree() && (tree.getID() != oldTreeID || tree.getVersionNumber() != oldTreeVersion))) {
			currentCharA = 0;
			charStatesA = null;
			currentCharB = 1;
			charStatesB = null;
			oldTreeID = tree.getID();
			oldTreeVersion = tree.getVersionNumber();
		}
		if (charStatesA == null) {
			if (currentCharA<0 || currentCharA>=characterSourceTask.getNumberOfCharacters(tree))
				currentCharA = 0;
			charStatesA = characterSourceTask.getCharacter(tree, currentCharA);
		}
		result.setToUnassigned();
		charStepsTask.calculateNumber(tree, charStatesA, result, resultString);
		if (charStatesB == null) {
			if (currentCharB<0 || currentCharB>=characterSourceTask.getNumberOfCharacters(tree))
				currentCharB = 0;
			charStatesB = characterSourceTask.getCharacter(tree, currentCharB);
		}
		stepsA.setToUnassigned();
		stepsB.setToUnassigned();
		charStepsTask.calculateNumber(tree, charStatesA, stepsA, resultString);
		charStepsTask.calculateNumber(tree, charStatesB, stepsB, resultString);
		oldTaxa = taxa;
		result.setValue(stepsA);
		result.subtract(stepsB);
		if (resultString!=null)
			resultString.append("Difference in steps: " + result + " (char. " + CharacterStates.toExternal(currentCharA) + " - " + CharacterStates.toExternal(currentCharB) + ")");
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	/*.................................................................................................................*/
	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		if (employee==characterSourceTask) {
			//currentCharA = 0;
			charStatesA = null;
			//currentCharB = 1;
			charStatesB = null;
			parametersChanged(notification);
		}
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("getCharacterSource ",characterSourceTask);
		temp.addLine("setCharacters " + CharacterStates.toExternal(currentCharA) + " " + CharacterStates.toExternal(currentCharB));
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets module supplying characters", "[name of module]", commandName, "setCharacterSource")) {//temporary, for data files using old system without coordinators
			return characterSourceTask.doCommand(commandName, arguments, checker);
		}
		else if (checker.compare(this.getClass(), "Returns module supplying characters", null, commandName, "getCharacterSource")) {
			return characterSourceTask;
		}
		else if (checker.compare(this.getClass(), "Queries the user about what first character to use", null, commandName, "chooseCharacterA")) {
			int ic=characterSourceTask.queryUserChoose(oldTaxa, " to count steps ");
			if (MesquiteInteger.isCombinable(ic)) {
				currentCharA = ic;
				charStatesA = null;
				parametersChanged(); 
			}
		}
		else if (checker.compare(this.getClass(), "Queries the user about what second character to use", null, commandName, "chooseCharacterB")) {
			int ic=characterSourceTask.queryUserChoose(oldTaxa, " to count steps ");
			if (MesquiteInteger.isCombinable(ic)) {
				currentCharB = ic;
				charStatesB = null;
				parametersChanged(); 
			}
		}
		else if (checker.compare(this.getClass(), "Sets the characters to use", "[first character number] [second character number]", commandName, "setCharacters")) {
			String a = parser.getFirstToken(arguments);
			String b = parser.getNextToken();
			int icNumA = MesquiteInteger.fromString(a);
			int icNumB = MesquiteInteger.fromString(b);
			if (!MesquiteInteger.isCombinable(icNumA) || !MesquiteInteger.isCombinable(icNumB))
				return null;
			int icA = CharacterStates.toInternal(icNumA);
			if ((icA>=0) && characterSourceTask.getNumberOfCharacters(oldTaxa)==0) {
				currentCharA = icA;
				charStatesA = null;
			}
			else if ((icA>=0) && (icA<=characterSourceTask.getNumberOfCharacters(oldTaxa)-1)) {
				currentCharA = icA;
				charStatesA = null;
				parametersChanged(); 
			}
			int icB = CharacterStates.toInternal(icNumB);
			if ((icB>=0) && characterSourceTask.getNumberOfCharacters(oldTaxa)==0) {
				currentCharB = icB;
				charStatesB = null;
			}
			else if ((icB>=0) && (icB<=characterSourceTask.getNumberOfCharacters(oldTaxa)-1)) {
				currentCharB = icB;
				charStatesB = null;
				parametersChanged(); 
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/*.................................................................................................................*/
	public String getParameters() {
		if (characterSourceTask==null)
			return "Char. " + CharacterStates.toExternal(currentCharA) + " - char. " + CharacterStates.toExternal(currentCharB);
		else
			return "Char. " + CharacterStates.toExternal(currentCharA) + " - char. " + CharacterStates.toExternal(currentCharB) + " from " + characterSourceTask.getNameAndParameters();
	}
	/*.................................................................................................................*/
	public String getVeryShortName() {
		return "Steps difference";
	}
	public boolean showCitation(){
		return true;
	}
	public boolean isPrerelease(){
		return false;
	}
}

