/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.NumForCharMatrixList;
/*~~  */

import mesquite.lists.lib.*;
import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.characters.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class NumForCharMatrixList extends DataSetsListAssistant  {
	/*.................................................................................................................*/
	public String getName() {
		return "Number for Matrix (in List of Character Matrices window)";
	}
	public String getNameForMenuItem() {
		return "Number for Matrix";
	}
	public String getExplanation() {
		return "Supplies numbers for character matrcies for a character matrices list window." ;
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(NumberForMatrix.class, getName() + " needs a method to calculate a value for each of the character matrices.",
		"You can select a value to show in the Number For Character Matrices submenu of the Columns menu of the List of Character Matrices Window. ");
	}
	NumberForMatrix numberTask;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (arguments !=null) {
			numberTask = (NumberForMatrix)hireNamedEmployee(NumberForMatrix.class, arguments);
			if (numberTask==null) {
				return sorry("Number for character matrix (for list) can't start because the requested module was not successfully hired");
			}
			return true;
		}
		numberTask = (NumberForMatrix)hireEmployee(NumberForMatrix.class, "Value to calculate for character matrix (for List of Matrices window)");
		if (numberTask==null) {
			return sorry("Number for character matrix (for list) can't start because the no calculating module was successfully hired");
		}
		return true;
	}
	/** Returns whether or not it's appropriate for an employer to hire more than one instance of this module.  
 	If false then is hired only once; second attempt fails.*/
	public boolean canHireMoreThanOnce(){
		return true;
	}
	/*.................................................................................................................*/
	public void employeeQuit(MesquiteModule m){
		iQuit();
	}
	public void setTableAndObject(MesquiteTable table, Object obj){
		if (datas !=null)
			datas.removeListener(this);
		if (obj instanceof ListableVector)
			this.datas = (ListableVector)obj;
		datas.addListener(this);
		doCalcs();
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public void disposing(Object obj){
		//TODO: respond
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public boolean okToDispose(Object obj, int queryUser){
		return true;  //TODO: respond
	}
	public void changed(Object caller, Object obj, Notification notification){
		if (Notification.appearsCosmetic(notification))
			return;
		if (notification.getCode()!=MesquiteListener.LOCK_CHANGED)
			doCalcs();
		parametersChanged(notification);
	}
	/*.................................................................................................................*/
	public Class getHireSubchoice(){
		return NumberForMatrix.class;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("setValueTask ", numberTask); 
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets module that calculates a number for a character matrix", "[name of module]", commandName, "setValueTask")) {
			NumberForMatrix temp= (NumberForMatrix)hireNamedEmployee(NumberForMatrix.class, arguments);
			if (temp!=null) {
				numberTask = temp;
				return temp;
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	public String getTitle() {
		if (numberTask==null)
			return "";
		return numberTask.getVeryShortName();
	}
	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		if (Notification.appearsCosmetic(notification) || (notification!=null && notification.getCode()==MesquiteListener.LOCK_CHANGED)) 
			return;
		doCalcs();
		parametersChanged(notification);
	}
	/*.................................................................................................................*/
	NumberArray na = new NumberArray(0);
	StringArray explArray = new StringArray(0);
	/*.................................................................................................................*/
	public void doCalcs(){
		if (numberTask==null || datas == null)
			return;
		outputInvalid();
		int numBlocks = datas.size();
		explArray.resetSize(numBlocks);
		MesquiteString expl = new MesquiteString();
		na.resetSize(numBlocks);
		MesquiteNumber mn = new MesquiteNumber();
		for (int ic=0; ic<numBlocks; ic++) {
			CharacterData data = (CharacterData)datas.elementAt(ic);
			mn.setToUnassigned();
			numberTask.calculateNumber(data.getMCharactersDistribution(), mn, expl);
			na.setValue(ic, mn);
			explArray.setValue(ic, expl.getValue());
		}
	}
	public String getExplanationForRow(int ic){
		if (explArray == null || explArray.getSize() <= ic)
			return null;
		return explArray.getValue(ic);
	}
	public String getStringForRow(int ic){
		if (na==null)
			return "";

		return na.toString(ic);
	}
	public String getWidestString(){
		if (numberTask==null)
			return "888888";
		return numberTask.getVeryShortName()+"   ";
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
	/*.................................................................................................................*/
	public void endJob() {
		if (datas !=null)
			datas.removeListener(this);
		super.endJob();
	}

}

