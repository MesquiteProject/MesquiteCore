/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.stochchar.LikelihoodInCharacter;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.stochchar.lib.*;


/* ======================================================================== */
public class LikelihoodInCharacter extends NumberForTree implements Incrementable {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(CharacterLikelihood.class, getName() + "  needs a method to calculate likelihoods.",
		"The method to calculate likelihoods can be selected initially");
		EmployeeNeed e2 = registerEmployeeNeed(CharSourceCoordObed.class, getName() + "  needs a source of characters.",
		"The source of characters is arranged initially");
	}
	/*.................................................................................................................*/
	MesquiteNumber steps;
	CharSourceCoordObed characterSourceTask;
	CharacterLikelihood charLikelihoodTask;
	Taxa oldTaxa = null;
	long oldTreeID = 0;
	long oldTreeVersion = 0;
	CharacterDistribution charStates;
	int currentChar = 0;
	MesquiteCommand cltC;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
    	 	steps=new MesquiteNumber();
 		characterSourceTask = (CharSourceCoordObed)hireEmployee(CharSourceCoordObed.class, "Source of character");
 		if (characterSourceTask == null)
 			return sorry(getName() + " couldn't start because no source of characters was obtained.");
 		charLikelihoodTask = (CharacterLikelihood)hireEmployee(CharacterLikelihood.class, null);
 		if (charLikelihoodTask == null)
 			return sorry(getName() + " couldn't start because no likelihood calculator module was obtained.");
		addMenuItem( "Next Character", makeCommand("nextCharacter",  this));
		addMenuItem( "Previous Character", makeCommand("previousCharacter",  this));
		addMenuItem( "Choose Character...", makeCommand("chooseCharacter",  this));
  		return true;
  	 }
  	 public void employeeQuit(MesquiteModule m){
  	 	if (m==charLikelihoodTask || m == characterSourceTask)
  	 		iQuit();
  	 }
  	 
   	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
   	public void initialize(Tree tree){
   		characterSourceTask.initialize(tree.getTaxa());
   	}
   	/*.................................................................................................................*/
   	public void calculateNumber(Tree tree, MesquiteNumber result, MesquiteString resultString) { 
   		if (result==null || tree == null || charLikelihoodTask == null)
   			return;
	   	clearResultAndLastResult(result);
  		steps.setValue((int)0);
   		Taxa taxa = tree.getTaxa();
   		if (taxa != oldTaxa || (characterSourceTask.usesTree() && (tree.getID() != oldTreeID || tree.getVersionNumber() != oldTreeVersion))) {
   			currentChar = 0;
   			charStates = characterSourceTask.getCharacter(tree, currentChar);
   			oldTreeID = tree.getID();
   			oldTreeVersion = tree.getVersionNumber();
   		}
   		else if (charStates == null) {
   			charStates = characterSourceTask.getCharacter(tree, currentChar);
   		}

   		MesquiteNumber num=new MesquiteNumber();
   		charLikelihoodTask.calculateNumber(tree, charStates, num, resultString);
   		steps.setValue(num);
   		oldTaxa = taxa;
   		result.setValue(steps);
   		if (resultString!=null) {
   			resultString.append(" (char. " + CharacterStates.toExternal(currentChar) + ")");
   		}
		saveLastResult(result);
		saveLastResultString(resultString);
  	}
   	/*.................................................................................................................*/
   	public boolean biggerIsBetter() {
		return true;
	}
	/*.................................................................................................................*/
	/*.................................................................................................................*/
   	 public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
   	 	if (employee==characterSourceTask) {
   			currentChar = 0;
   			charStates = null;
			parametersChanged(notification);
   	 	}
   	 	else 
   	 		parametersChanged(notification);
   	 }
	/*.................................................................................................................*/
   	 public void setCurrent(long i){
 		if (characterSourceTask==null || oldTaxa==null)
 			return;
 		if ((i>=0) && (i<=characterSourceTask.getNumberOfCharacters(oldTaxa)-1)) {
 			currentChar = (int)i;
			charStates=null;
			//parametersChanged();
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
 		if (characterSourceTask==null || oldTaxa==null)
 			return 0;
 		return characterSourceTask.getNumberOfCharacters(oldTaxa)-1;
 	}
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) { 
   	 	Snapshot temp = new Snapshot();
  	 	temp.addLine("getCharacterSource ", characterSourceTask); 
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
    	 	else if (checker.compare(this.getClass(), "Goes to next character", null, commandName, "nextCharacter")) { 
    	 		if (currentChar>=characterSourceTask.getNumberOfCharacters(oldTaxa)-1)
    	 			currentChar=0;
    	 		else
    	 			currentChar++;
			charStates = null;
 			parametersChanged(); 
    	 	}
    	 	else if (checker.compare(this.getClass(), "Goes to previous character", null, commandName, "previousCharacter")) {
    	 		if (currentChar<=0)
    	 			currentChar=characterSourceTask.getNumberOfCharacters(oldTaxa)-1;
    	 		else
    	 			currentChar--;
   			charStates = null;
 			parametersChanged(); 
    	 	}
    	 	else if (checker.compare(this.getClass(), "Queries the user about which character to use", null, commandName, "chooseCharacter")) {
    	 		int ic=characterSourceTask.queryUserChoose(oldTaxa, " for likelihood calculation ");
    	 		if (MesquiteInteger.isCombinable(ic)) {
	   			currentChar = ic;
	   			charStates = null;
	 			parametersChanged();
 			}
    	 	}
    	 	else if (checker.compare(this.getClass(), "Sets the character to be used", "[number of character]", commandName, "setCharacter")) {
    	 		int ic = MesquiteInteger.fromString(arguments, new MesquiteInteger(0));
    	 		if (!MesquiteInteger.isCombinable(ic))
    	 			return null;
    	 		if ((ic>=0) && (ic<=characterSourceTask.getNumberOfCharacters(oldTaxa)-1)) {
    	 			currentChar = ic; //shouldn't this be internal????
	   			charStates = null;
	 			parametersChanged(); 
 			}
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
		return null;
   	 }
   
	/*.................................................................................................................*/
    	 public String getParameters() {
		return "Character Source: " + characterSourceTask.getNameAndParameters();
   	 }
	/*.................................................................................................................*/
    	 public String getVeryShortName() {
		return "-ln Likelihood";
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Likelihood in Character";
   	 }
	/*.................................................................................................................*/
    	 public boolean showCitation() {
		return true;
   	 }
	/*.................................................................................................................*/
    	 public boolean isPrerelease() {
		return false;
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Calculates the negative log likelihood of a tree with respect to a single character." ;
   	 }
}

