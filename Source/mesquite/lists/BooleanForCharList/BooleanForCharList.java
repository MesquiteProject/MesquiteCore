/* Mesquite source code.  Copyright 1997-2009 W. Maddison and D. Maddison.
Version 2.72, December 2009.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lists.BooleanForCharList;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.lists.lib.*;

public class BooleanForCharList extends CharListAssistant implements MesquiteListener {
	CharacterData data;
	/*.................................................................................................................*/
	public String getName() {
		return "Boolean for Character (in List of Characters window)";
	}

	public String getNameForMenuItem() {
		return "Boolean for Character";
	}

	public String getExplanation() {
		return "Supplies booleans for characters for a character list window." ;
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(BooleanForCharacter.class, getName() + " needs a method to calculate a boolean (yes/no) value for each of the characters.",
		"You can select a value to show in the Boolean For Character submenu of the Columns menu of the List of Characters Window. ");
	}
	/*.................................................................................................................*/
	BooleanForCharacter booleanTask;
	boolean suppressed = false;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (arguments !=null) {
			booleanTask = (BooleanForCharacter)hireNamedEmployee(BooleanForCharacter.class, arguments);
			if (booleanTask==null) {
				return sorry("Boolean for character (for list) can't start because the requested calculator module wasn't successfully hired");
			}
		}
		else {
			booleanTask = (BooleanForCharacter)hireEmployee(BooleanForCharacter.class, "Value to calculate for character (for character list)");
			if (booleanTask==null) {
				return sorry("Boolean for character (for list) can't start because no calculator module was successfully hired");
			}
		}
		return true;
	}
	/** Returns whether or not it's appropriate for an employer to hire more than one instance of this module.  
 	If false then is hired only once; second attempt fails.*/
	public boolean canHireMoreThanOnce(){
		return true;
	}
	public void employeeQuit(MesquiteModule m){
		iQuit();
	}
	/*.................................................................................................................*/
	public Class getHireSubchoice(){
		return BooleanForCharacter.class;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("suppress"); 
		temp.addLine("setValueTask ", booleanTask); 
		temp.addLine("desuppress"); 
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets module that calculates a boolean for a character", "[name of module]", commandName, "setValueTask")) {
			BooleanForCharacter temp= (BooleanForCharacter)replaceEmployee(BooleanForCharacter.class, arguments, "Boolean for a character", booleanTask);
			if (temp!=null) {
				booleanTask = temp;
				if (!suppressed){
					doCalcs();
					parametersChanged();
				}
				return temp;
			}
		}
		else if (checker.compare(this.getClass(), "Suppresses calculation", null, commandName, "suppress")) {
			suppressed = true;
		}
		else if (checker.compare(this.getClass(), "Releases suppression of calculation", null, commandName, "desuppress")) {
			if (suppressed){
				suppressed = false;
				outputInvalid();
				doCalcs();
				parametersChanged();
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	public void setTableAndData(MesquiteTable table, CharacterData data){
		if (data != this.data){
			if (this.data != null){
				this.data.removeListener(this);
				this.data.getTaxa().removeListener(this);
			}
			data.addListener(this);
			data.getTaxa().addListener(this);
	}
		this.data = data;
	
		if (!suppressed)
			doCalcs();
		
	}
	public String getTitle() {
		if (booleanTask==null)
			return "";
		return booleanTask.getVeryShortName();
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public void disposing(Object obj){
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public boolean okToDispose(Object obj, int queryUser){
		return true;  //TODO: respond
	}
	public void changed(Object caller, Object obj, Notification notification){
		
		if (!suppressed){
			outputInvalid();
			doCalcs();
			parametersChanged(notification);
		}
	}
	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		if (!suppressed){
			outputInvalid();
			doCalcs();
			parametersChanged(notification);
		}
	}
	/*.................................................................................................................*/
	IntegerArray booleanList = new IntegerArray(0);
	StringArray explArray = new StringArray(0);
	int totalYes = 0;
	int totalNo = 0;
	/*.................................................................................................................*/
	public void doCalcs(){
		if (suppressed || booleanTask==null)
			return;
		int numChars = data.getNumChars();
		booleanList.resetSize(numChars);
		explArray.resetSize(numChars);
		MesquiteBoolean mb = new MesquiteBoolean();
		MesquiteString expl = new MesquiteString();
		totalYes = 0;
		totalNo = 0;
		for (int ic=0; ic<numChars; ic++) {
			CommandRecord.tick("Boolean for character in tree list; examining character " + ic);
			mb.setToUnassigned();
			booleanTask.calculateBoolean(data, ic, mb, expl);
			if (mb.isUnassigned())
				booleanList.setValue(ic, -1);
			else if (mb.getValue()){
				booleanList.setValue(ic, 1);
				totalYes++;
			}
			else {
				totalNo++;
				booleanList.setValue(ic, 0);
			}
			explArray.setValue(ic, expl.getValue());
		}
	}
	public String getExplanationForRow(int ic){
		if (explArray == null || explArray.getSize() <= ic)
			return null;
		String s = explArray.getValue(ic);
		if (StringUtil.blank(s))
			s = getStringForCharacter(ic);
		return s + " [Totals: Yes: " + totalYes + "; No: " + totalNo + "]";
	}
	public String getStringForCharacter(int ic){
		if (booleanList==null)
			return "";
		if (booleanList.getValue(ic)<0)
			return "-";
		else if (booleanTask!=null)
			return booleanTask.getValueString(booleanList.getValue(ic)==1);
		//return na.toString(ic);
		return "";
	}
	public String getWidestString(){
		if (booleanTask==null)
			return "888888";
		return booleanTask.getVeryShortName()+"   ";
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;  
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
}
