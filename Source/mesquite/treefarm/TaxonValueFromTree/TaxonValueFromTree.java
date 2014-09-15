/* Mesquite source code, Treefarm package.  Copyright 1997 and onward, W. Maddison, D. Maddison and P. Midford. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.treefarm.TaxonValueFromTree;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.treefarm.lib.*;

/** ======================================================================== */
/*
provides a superclass for NumberForTaxon that use a tree;
*/

public class TaxonValueFromTree extends NumberForTaxon {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(OneTreeSource.class, getName() + "  needs a current tree.",
		"The source of a current tree is arranged initially");
		EmployeeNeed e2 = registerEmployeeNeed(NForTaxonWithTree.class, getName() + "  needs a method to calculate values for taxa based on a tree.",
		"The method to calculate values for taxa can be chosen initially or in the Values for Taxa Using Tree submenu");
	}
	protected boolean needsRecalculation = true;
	protected Taxa currentTaxa;
	MesquiteString treeSourceName;
	MesquiteCommand cstC;
	OneTreeSource treeSourceTask;
	NumberArray results;
	MesquiteString resultsString;
	protected NForTaxonWithTree numberTask;
	MesquiteString numberTaskName;
	MesquiteCommand ntC;
	MesquiteSubmenuSpec mss;
	public Class getNumberTaskClass(){
		return NForTaxonWithTree.class;
	}
	public String getHiringString(){
		return "Value for taxa using tree";
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		ntC =makeCommand("setNumberTask",  this);
		numberTaskName = new MesquiteString();
		if (numberTask == null)
			numberTask = (NForTaxonWithTree)hireEmployee(getNumberTaskClass(), getHiringString());//shouldn't ask as this is an init and might not be needed.  "Value to calculate for character state in taxon"

		if (numberTask != null){
			numberTask.setHiringCommand(ntC);
			numberTaskName.setValue(numberTask.getName());
		}
		else 
			return false;
		if (numModulesAvailable(getNumberTaskClass())>0) {
			mss = addSubmenu(null, getHiringString(), ntC, getNumberTaskClass());
			mss.setSelected(numberTaskName);
			//mss.setEnabled(false);
		}
		treeSourceTask = (OneTreeSource)hireEmployee(OneTreeSource.class,  "Source of tree for " + getName());
 		if (treeSourceTask == null) {
 			return sorry(getName() + " couldn't start because no source of tree obtained");
 		}
		treeSourceName = new MesquiteString(treeSourceTask.getName());
		

		cstC =  makeCommand("setTreeSource",  this);
		treeSourceTask.setHiringCommand(cstC);
		if (numModulesAvailable(TreeSource.class)>1){ 
			MesquiteSubmenuSpec mss = addSubmenu(null, "Tree source",cstC, TreeSource.class);
			mss.setSelected(treeSourceName);
		}

		results = new NumberArray(1);
		resultsString = new MesquiteString();
		needsRecalculation = MesquiteThread.isScripting();
		return true;  
 	}
 	
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
 	public void employeeQuit(MesquiteModule m){
 		if (m!=treeSourceTask)
 			iQuit();
 	}
	/*.................................................................................................................*/
   	 public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
	 	//if (employee != treeSourceTask || Notification.getCode(notification) != MesquiteListener.SELECTION_CHANGED) {
				needsRecalculation = true;
				super.employeeParametersChanged(employee, source, notification);
		//	}
   	 }
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) {
   	 	Snapshot temp = new Snapshot();
  	 	temp.addLine("setTreeSource " , treeSourceTask);
		temp.addLine("setNumberTask ", numberTask);  
  	 	temp.addLine("doCalc");
  	 	return temp;
  	 }
  	 MesquiteInteger pos = new MesquiteInteger(0);
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    			if (checker.compare(this.getClass(), "Sets the module that calculates numbers using a tree", "[name of module]", commandName, "setNumberTask")) {
    				NForTaxonWithTree temp =  (NForTaxonWithTree)replaceEmployee(getNumberTaskClass(), arguments, "Module to calculate numbers using a tree", numberTask);
    				if (temp!=null) {
    					numberTask = temp;
    					numberTask.setHiringCommand(ntC);
    					numberTaskName.setValue(numberTask.getName());
    					needsRecalculation = true;
    					parametersChanged();
    					return numberTask;
    				}
    			}
    			else if (checker.compare(this.getClass(), "Sets the source of trees for taxon value", "[name of module]", commandName, "setTreeSource")) {
    	 		OneTreeSource temp =  (OneTreeSource)replaceEmployee(OneTreeSource.class, arguments, "Source of trees", treeSourceTask);
 			if (temp!=null) {
	    	 		treeSourceTask = temp;
				treeSourceTask.setHiringCommand(cstC);
				treeSourceName.setValue(treeSourceTask.getName());
				treeSourceTask.initialize(currentTaxa);
				
				if (!MesquiteThread.isScripting()) {
					needsRecalculation = true;
					parametersChanged();
				}
 			}
 			return temp;
    	 	}
    	 	else if (checker.compare(this.getClass(), "Requests calculations", null, commandName, "doCalc")) {
			needsRecalculation = true;
			parametersChanged();
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
    	 	return null;
   	 }
 	/*.................................................................................................................*/
   	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
   	public void initialize(Taxa taxa){
   		currentTaxa = taxa;
   		needsRecalculation = true;
   		treeSourceTask.initialize(currentTaxa);
   	}
	/*.................................................................................................................*/
	public void calculateNumber(Taxon taxon, MesquiteNumber result, MesquiteString resultString){
		if (result==null|| taxon == null)
			return;
	   	clearResultAndLastResult(result);
		if (taxon.getTaxa()!= currentTaxa) {
			initialize(taxon.getTaxa());
			needsRecalculation = true;
		}
		if (needsRecalculation)
			doCalcs();
		int it = currentTaxa.whichTaxonNumber(taxon);
		results.placeValue(it, result);
		
		if (resultString !=null) {
			resultString.setValue(resultsString.getValue());
		}
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	/*.................................................................................................................*/
	public void doCalcs(){
		needsRecalculation = false;
		int numTaxa = currentTaxa.getNumTaxa();
		results.resetSize(numTaxa);
		results.zeroArray();
		Tree tree = treeSourceTask.getTree(currentTaxa);
		if (resultsString != null)
			resultsString.setValue(numberTask.getName());
		
		numberTask.calculateNumbers(currentTaxa,  tree, results,  resultsString);
	}
	/*.................................................................................................................*/
	/** Explains what the module does.*/
	 public String getExplanation() {
	return "Calculates a value for each taxon using a tree.";
	 }
	 
/** Returns the name of the module in very short form, for use for column headings and other constrained places.*/
public String getVeryShortName(){
	if (numberTask != null)
		return numberTask.getVeryShortName();
	return "Value with tree";
}

	public boolean isPrerelease(){
		return false;
	}
/** Name of module*/
	 public String getName() {
	return "Value from Tree";
	 }
		/*.................................................................................................................*/
		/** returns the name of character ic*/
		public String getNameAndParameters(){
			if (numberTask != null)
				return numberTask.getNameAndParameters();
		return  getName();
		}

}


