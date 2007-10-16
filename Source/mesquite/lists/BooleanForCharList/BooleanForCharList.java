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
		if (Notification.appearsCosmetic(notification))
			return;
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
	/*.................................................................................................................*/
	public void doCalcs(){
		if (suppressed || booleanTask==null)
			return;
		int numChars = data.getNumChars();
		booleanList.resetSize(numChars);
		explArray.resetSize(numChars);
		MesquiteBoolean mb = new MesquiteBoolean();
		MesquiteString expl = new MesquiteString();
		for (int ic=0; ic<numChars; ic++) {
			CommandRecord.tick("Boolean for character in tree list; examining character " + ic);
			mb.setToUnassigned();
			booleanTask.calculateBoolean(data, ic, mb, expl);
			if (mb.isUnassigned())
				booleanList.setValue(ic, -1);
			else if (mb.getValue())
				booleanList.setValue(ic, 1);
			else 
				booleanList.setValue(ic, 0);
			explArray.setValue(ic, expl.getValue());
		}
	}
	public String getExplanationForRow(int ic){
		if (explArray == null || explArray.getSize() <= ic)
			return null;
		return explArray.getValue(ic);
	}
	public String getStringForCharacter(int ic){
		if (booleanList==null)
			return "";
		if (booleanList.getValue(ic)<0)
			return "-";
		else if (booleanList.getValue(ic)==1)
			return "Yes";
		else
			return "No";
		//return na.toString(ic);
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
