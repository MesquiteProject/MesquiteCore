/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.charMatrices.DifferenceInNumsForChar;

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class DifferenceInNumsForChar extends NumberForCharacter {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(NumberForCharacter.class, getName() + " compares two different values for each character.",
				"You can request what values to compare initially, or later under the First Value for Difference submenu and the Second Value for Difference submenu.");
	}
	NumberForCharacter numberTask1, numberTask2;
	MesquiteString numberTask1Name, numberTask2Name;
	MesquiteCommand ntC1, ntC2;
	MesquiteNumber[] numbers = new MesquiteNumber[2];

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		numberTask1 = (NumberForCharacter)hireEmployee(NumberForCharacter.class, "First value to calculate for character");
		if (numberTask1 == null)
			return sorry(getName() + " couldn't start because no calculator module obtained.");
		numberTask2 = (NumberForCharacter)hireEmployee(NumberForCharacter.class, "Second value to calculate for character");
		if (numberTask2 == null)
			return sorry(getName() + " couldn't start because no calculator module obtained.");
		numbers[0] = new MesquiteNumber();
		numbers[1] = new MesquiteNumber();
		
		ntC1 =makeCommand("setNumberTask1",  this);
		ntC2 =makeCommand("setNumberTask2",  this);
 		numberTask1.setHiringCommand(ntC1);
 		numberTask1Name = new MesquiteString();
 		numberTask1Name.setValue(numberTask1.getName());
 		numberTask2.setHiringCommand(ntC2);
 		numberTask2Name = new MesquiteString();
 		numberTask2Name.setValue(numberTask2.getName());
		if (numModulesAvailable(NumberForCharacter.class)>1) {
			MesquiteSubmenuSpec mss = addSubmenu(null, "First Value for Difference", ntC1, NumberForCharacter.class);
 			mss.setSelected(numberTask1Name);
			mss = addSubmenu(null, "Second Value for Difference", ntC2, NumberForCharacter.class);
 			mss.setSelected(numberTask2Name);
		}
		return true;
  	 }
	/*.................................................................................................................*/
   	 public boolean showCitation(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
 	 public void employeeQuit(MesquiteModule m){
  	 	iQuit();
  	 }
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) { 
   	 	Snapshot temp = new Snapshot();
 	 	temp.addLine("setNumberTask1 ", numberTask1);  
 	 	temp.addLine("setNumberTask2 ", numberTask2);  
  	 	return temp;
  	 }
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
   	 	if (checker.compare(this.getClass(), "Sets the module that calculates the first number for characters", "[name of module]", commandName, "setNumberTask1")) {
    	 		NumberForCharacter temp =  (NumberForCharacter)replaceEmployee(NumberForCharacter.class, arguments, "Number for character (first in difference)", numberTask1);
 			if (temp!=null) {
 				numberTask1 = temp;
		 		numberTask1.setHiringCommand(ntC1);
		 		numberTask1Name.setValue(numberTask1.getName());
				parametersChanged();
	 			return numberTask1;
	 		}
    	 	}
   	 	else if (checker.compare(this.getClass(), "Sets the module that calculates the second number for characters", "[name of module]", commandName, "setNumberTask2")) {
    	 		NumberForCharacter temp =  (NumberForCharacter)replaceEmployee(NumberForCharacter.class, arguments, "Number for character (second in difference)", numberTask2);
 			if (temp!=null) {
 				numberTask2 = temp;
		 		numberTask2.setHiringCommand(ntC2);
		 		numberTask2Name.setValue(numberTask2.getName());
				parametersChanged();
	 			return numberTask2;
	 		}
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
    	 	return null;
    	 }
  	 
   	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
   	public void initialize(CharacterDistribution charStates){
   		if (numberTask1 !=null)
   			numberTask1.initialize(charStates);
   		if (numberTask2 !=null)
   			numberTask2.initialize(charStates);
  	}
   	public boolean returnsMultipleValues(){
   	return true;
}
    	 MesquiteString rs1 = new MesquiteString();
    	 MesquiteString rs2 = new MesquiteString();
	/*.................................................................................................................*/
	public void calculateNumber(CharacterDistribution charStates, MesquiteNumber result, MesquiteString resultString) {
    	 	if (result==null || charStates == null)
    	 		return;
    	clearResultAndLastResult(result);
		rs1.setValue("");
		rs2.setValue("");
		MesquiteNumber r = new MesquiteNumber();
		numberTask1.calculateNumber(charStates, r, rs1);
		numberTask2.calculateNumber(charStates, result, rs2);
		numbers[0].setName(r.getName());
		numbers[0].setValue(r);
		numbers[1].setName(result.getName());
		numbers[1].setValue(result);
		
		result.subtract(r);
		result.copyAuxiliaries(numbers);
		if (resultString!=null) {
			resultString.setValue("Difference: " + result + " [" + rs1 + "] - [" + rs2 + "]");
		}
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	/*.................................................................................................................*/
	public String getParameters(){
		return "Difference between: " + numberTask1.getName() + " and " + numberTask2.getName(); 
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Difference in two values for character";
   	 }
	/*.................................................................................................................*/
    	 public String getVeryShortName() {
    	 	if (numberTask1 ==null | numberTask2 == null)
			return "Difference in two values";
		else
			return "Difference [" + numberTask1.getVeryShortName() + "] - [" + numberTask2.getVeryShortName() + "]";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Coordinates the calculation of the difference in two numbers for a character." ;
   	 }
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
   	 
   	 
}

