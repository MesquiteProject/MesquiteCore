/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.molec.FindSequence;
/*~~  */

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.categ.lib.*;
import java.awt.event.*;
import mesquite.molec.lib.*;
import java.awt.datatransfer.*;
/* ======================================================================== *

*new in 1.05*

/* ======================================================================== */
public class FindSequence extends DataWindowAssistantI {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(FindSequenceCriterion.class, getName() + "  needs a criterion for finding sequences.",
		"The criterion for finding sequences is chosen in the Find Sequence or Final All Sequences submenus of the Edit menu of the Character Matrix Editor");
	}
	/*.................................................................................................................*/
	MesquiteTable table;
	CharacterData data;
	int firstTaxon = 0;
	int firstChar = 0;
	boolean findAll = false;
	FindSequenceCriterion criterionTask;
	MesquiteSubmenuSpec mss, mss3;
	MesquiteString criterionName;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		criterionName = new MesquiteString();
		if (numModulesAvailable(FindSequenceCriterion.class)>0) {
			mss3 = addSubmenu(MesquiteTrunk.editMenu, "Find Sequence", makeCommand("findSequence", this), FindSequenceCriterion.class);
 			mss = addSubmenu(MesquiteTrunk.editMenu, "Find All Sequences", makeCommand("findAllSequences", this), FindSequenceCriterionG.class);
			mss.setSelected(criterionName);
 			mss3.setSelected(criterionName);
			MesquiteMenuItemSpec mm2 =addMenuItem(MesquiteTrunk.editMenu, "Find Sequence Again", makeCommand("findSequenceAgain", this));
			mm2.setShortcut(KeyEvent.VK_6);
			MesquiteMenuItemSpec mm3 =addMenuItem(MesquiteTrunk.editMenu, "Copy Sequence", makeCommand("copySequence", this));
			mm3.setShortcut(KeyEvent.VK_C);
			mm3.setShortcutNeedsShift(true);
			return true;
		}
		return false;
	}
	/*.................................................................................................................*/
	/** Returns CompatibilityTest so other modules know if this is compatible with some object. */
	public CompatibilityTest getCompatibilityTest(){
		return new RequiresAnyMolecularData();
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
	public void setTableAndData(MesquiteTable table, CharacterData data){
		this.table = table;
		this.data = data;
	}
	public void employeeQuit(MesquiteModule employee){
		if (employee == criterionTask)
			criterionTask = null;
	}
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Finds sequence",null, commandName, "findSequence")) {
			//hire assistant
			if (criterionTask == null || !criterionTask.nameMatches(parser.getFirstToken(arguments)))
				criterionTask = (FindSequenceCriterion)replaceEmployee(FindSequenceCriterion.class, arguments, "Criterion", criterionTask);
			boolean proceed= criterionTask.showOptions( data, table);
			if (criterionTask == null || !proceed)
				return null;
			criterionName.setValue(criterionTask.getName());
			firstChar = 0;
			firstTaxon = 0;
			if (criterionTask instanceof FindSequenceCriterionG)
				seek("No instances found");
   	 		
		}
    	 	else if (checker.compare(this.getClass(), "Finds all sequences matching a criterion",null, commandName, "findAllSequences")) {
			//hire assistant
			if (criterionTask == null || !criterionTask.nameMatches(parser.getFirstToken(arguments)))
				criterionTask = (FindSequenceCriterionG)replaceEmployee(FindSequenceCriterionG.class, arguments, "Criterion", criterionTask);
			boolean proceed= ((FindSequenceCriterionG)criterionTask).showOptions(true, data, table);
			if (criterionTask == null || !proceed)
				return null;
			criterionName.setValue(criterionTask.getName());
			firstChar = 0;
			firstTaxon = 0;
			seekAll("No instances found");
   	 		
		}
    	 	else if (checker.compare(this.getClass(), "Finds sequence",null, commandName, "findSequenceAgain")) {
    			if (criterionTask instanceof FindSequenceCriterionG)
    				seek("No more instances found");
    			else
    				discreetAlert("Find Sequence Again is not applicable to the sequence finder you are currently using.  The sequence finder can be chosen under Edit>Find Sequence>");
		}
    	 	else if (checker.compare(this.getClass(), "Copies sequence without tabs",null, commandName, "copySequence")) {
				StringBuffer sb = new StringBuffer(100);
				table.copyIt(sb, false, false, true);
				Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection ss = new StringSelection(sb.toString());
				clip.setContents(ss, ss);
		}
   	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
		return null;
   	
	}
	
	void seekAll(String failureMessage){
			if (criterionTask == null)
				return;
			MesquiteInteger charFound = new MesquiteInteger(0);
			MesquiteInteger length = new MesquiteInteger();
			MesquiteInteger taxonFound = new MesquiteInteger(0);
  	 		table.deselectAll();
  	 		int numFound = 0;
   	 		while (((FindSequenceCriterionG)criterionTask).findNext(data, table, charFound, length, taxonFound)){
   	 			numFound++;
				table.setFocusedSequence(charFound.getValue(), charFound.getValue() + length.getValue(), taxonFound.getValue());
   	 			charFound.increment();
   	 		}
   	 		if (numFound==0) {
   	 			alert(failureMessage);
   	 		}
   	 		else if (numFound > 1)
   	 			logln(Integer.toString(numFound) + " instances found.");
   	 		else logln("One instance found.");
			table.repaintAll();
 	}
	void seek(String failureMessage){
			if (criterionTask == null)
				return;
			MesquiteInteger charFound = new MesquiteInteger(firstChar);
			MesquiteInteger length = new MesquiteInteger();
			MesquiteInteger taxonFound = new MesquiteInteger(firstTaxon);
  	 		table.deselectAll();
   	 		if (((FindSequenceCriterionG)criterionTask).findNext(data, table, charFound, length, taxonFound)){
   	 			firstChar = charFound.getValue();
   	 			firstTaxon = taxonFound.getValue();
				table.setFocusedSequence(charFound.getValue(), charFound.getValue() + length.getValue(), taxonFound.getValue());
   	 			firstChar++;
   	 		}
   	 		else {
   	 			firstChar = 0;
   	 			firstTaxon = 0;
   	 			alert(failureMessage);
   	 		}
			table.repaintAll();
 	}
   	
	/*.................................................................................................................*/
    	 public String getName() {
		return "Find Sequence";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Finds the next occurrence of a sequence in a matrix of molecular data." ;
   	 }
   	 
}


