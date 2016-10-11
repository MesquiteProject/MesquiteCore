/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.charMatrices.WandChar; 

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/*   to do:
	- deal with Option cursor, Shift cursor
	- do toggleSimilarWhole  THIS IS BEGUN BUT NOT COMPLETE
*/

/* ======================================================================== */
public class WandChar extends DataWindowAssistantI { 
	TableTool charWandTool;
	CharacterData data;
	MesquiteTable table;
	boolean charactersAreRows = false;
	MesquiteBoolean equals, greaterthan, lessthan;
	MesquiteBoolean toggleSameInTaxon, toggleSameWhole, toggleSimilarWhole;
	boolean defaultEquals = true;
	boolean defaultGT = false;
	boolean defaultLT = false;
	boolean defaultSameInTaxon = true;
	boolean defaultSameWhole = false;
	boolean defaultSimilarWhole = false;
	MesquiteCollator collator;
	 public String getFunctionIconPath(){
   		 return getPath() + "charWand.gif";
   	 }
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		if (containerOfModule() instanceof MesquiteWindow) {
			charWandTool = new TableTool(this, "charWand", getPath(), "charWand.gif", 4,4,"Select characters with same", "This tool selects characters according to criteria of being the same, greater than, or less than the value in the cell touched.  The criteria can be set using the menu that appears when you click on the tool button", MesquiteModule.makeCommand("charWandTouch",  this) , null, null);
			((MesquiteWindow)containerOfModule()).addTool(charWandTool);
			charWandTool.setPopUpOwner(this);
			setUseMenubar(false); //menu available by touching oning button
		}
		else return false;
		equals = new MesquiteBoolean(defaultEquals);
		greaterthan = new MesquiteBoolean(defaultGT);
		lessthan = new MesquiteBoolean(defaultLT);
		addCheckMenuItem(null, "Equal", makeCommand("toggleEquals",  this), equals);
		addCheckMenuItem(null, "Greater than", makeCommand("toggleGT",  this), greaterthan);
		addCheckMenuItem(null, "Less than", makeCommand("toggleLT",  this), lessthan);
		addMenuSeparator();
		toggleSameInTaxon = new MesquiteBoolean(defaultSameInTaxon);
		toggleSameWhole = new MesquiteBoolean(defaultSameWhole);
		toggleSimilarWhole = new MesquiteBoolean(defaultSimilarWhole);
		addCheckMenuItem(null, "Same (in Taxon)", makeCommand("toggleSameInTaxon",  this), toggleSameInTaxon);
		addCheckMenuItem(null, "Same (Whole Character)", makeCommand("toggleSameWhole",  this), toggleSameWhole);
		//addCheckMenuItem(null, "Similar (Whole Character)", makeCommand("toggleSimilarWhole",  this), toggleSimilarWhole);
		collator = new MesquiteCollator();
		return true;
	}
	/*.................................................................................................................*/
   	 public boolean isSubstantive(){
   	 	return false;
   	 }
	public void setTableAndData(MesquiteTable table, CharacterData data){
		this.table = table;
		this.data = data;
		//this.charactersAreRows = charactersAreRows;
	}
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) {
  	 	Snapshot temp = new Snapshot();
		if (equals.getValue()!=defaultEquals)
			temp.addLine("toggleEquals " + equals.toOffOnString());
		if (greaterthan.getValue()!=defaultGT)
			temp.addLine("toggleGT " + greaterthan.toOffOnString());
		if (lessthan.getValue()!=defaultLT)
			temp.addLine("toggleLT " + lessthan.toOffOnString());
		if (toggleSameInTaxon.getValue()!=defaultSameInTaxon)
			temp.addLine("toggleSameInTaxon " + toggleSameInTaxon.toOffOnString());
		if (toggleSameWhole.getValue()!=defaultSameWhole)
			temp.addLine("toggleSameWhole " + toggleSameWhole.toOffOnString());
		if (toggleSimilarWhole.getValue()!=defaultSimilarWhole)
			temp.addLine("toggleSimilarWhole " + toggleSimilarWhole.toOffOnString());
  	 	return temp;
  	 }
  	 boolean satisfiesCriteria(int standardChar, int comparisonChar, String one, String two){
		if (toggleSameInTaxon.getValue()) {
			if (equals.getValue() && one.equals(two))
				return true;
			double dOne = MesquiteDouble.fromString(one);
			double dTwo = MesquiteDouble.fromString(two);
			if (MesquiteDouble.isCombinable(dOne) && MesquiteDouble.isCombinable(dTwo)) {
				if (greaterthan.getValue() && (dTwo>dOne))
					return true;
				if (lessthan.getValue() && (dTwo<dOne))
					return true;
				return false;
			}
			int order = collator.compare(one, two);
			//int order =0;
			if (greaterthan.getValue() && (order == -1))
				return true;
			if (lessthan.getValue() && (order == 1))
				return true;
			if (equals.getValue() && (order == 0))
				return true;
		}
		else if (toggleSameWhole.getValue()) {
			if (!charactersAreRows) {  // characters are column; normal orientation
				for (int i=0; i<table.getNumRows(); i++){
					if (!table.getMatrixText(standardChar, i).equals(table.getMatrixText(comparisonChar,i)))
						return false;
				}
			}
			else {
				for (int i=0; i<table.getNumColumns(); i++){
					if (!table.getMatrixText(i, standardChar).equals(table.getMatrixText(i, comparisonChar)))
						return false;
				}
			}
			return true;
		}
		else if (toggleSimilarWhole.getValue()) { //not implemented yet!!!!!!!
			
			if (!charactersAreRows) {  // characters are column; normal orientation
				for (int i=0; i<table.getNumRows(); i++){
					if (!table.getMatrixText(standardChar, i).equals(table.getMatrixText(comparisonChar,i)))
						return false;
				}
			}
			else {
				for (int i=0; i<table.getNumColumns(); i++){
					if (!table.getMatrixText(i, standardChar).equals(table.getMatrixText(i, comparisonChar)))
						return false;
				}
			}
			return true;
		}
		return false;
  	 }
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(),  "Touches on a cell with the characters wand to select characters similar as defined by the options", "[column touched][row touched]", commandName, "charWandTouch")) {
	   	 		MesquiteInteger io = new MesquiteInteger(0);
	   			int column= MesquiteInteger.fromString(arguments, io);
	   			int row= MesquiteInteger.fromString(arguments, io);
				if (MesquiteInteger.isNonNegative(column)&& (MesquiteInteger.isNonNegative(row))) {
	   				boolean commandSelected =  (arguments.indexOf("command")>=0);
	   				boolean subtractFromSelection = commandSelected && data.getSelected(column);
		   			if ((arguments.indexOf("shift")<0) && !commandSelected) {
		   				data.deselectAll();    
		   				table.deselectAll();
		   			}
		   			table.offAllEdits();
					String text = table.getMatrixText(column, row);
					if (!charactersAreRows){  //each column is a char; hence go through this row to find which columns to select
							for (int i=0; i<table.getNumColumns(); i++){  // scoot along this row, see which columns to select
								if (satisfiesCriteria(column,i,text, table.getMatrixText(i, row))) {
									if (subtractFromSelection) {
											data.setSelected(i, false); //deselect whole character
									}
									else {
											data.setSelected(i, true); //select whole character
									}
								}
							}
					}
					else {
							for (int i=0; i<table.getNumRows(); i++){
								if (satisfiesCriteria(row,i,text, table.getMatrixText(column, i))) {
									if (subtractFromSelection) {
											data.setSelected(i, false); //deselect whole character
									}
									else {
											data.setSelected(i, true); //select whole character
									}
								}
							}
					}
					data.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
						
				}
    	 	}
    	 	else if (checker.compare(this.getClass(), "Sets whether the wand selects characters with value equal to that in cell touched", "[on = selects equal; off]", commandName, "toggleEquals")) {
    	 		boolean current = equals.getValue();
    	 		equals.toggleValue(parser.getFirstToken(arguments));
    	 	}
    	 	else if (checker.compare(this.getClass(), "Sets whether the wand selects characters with value greater than that in cell touched", "[on = selects greater than; off]", commandName, "toggleGT")) {
    	 		boolean current = greaterthan.getValue();
    	 		greaterthan.toggleValue(parser.getFirstToken(arguments));
    	 	}
    	 	else if (checker.compare(this.getClass(), "Sets whether the wand selects characters with value less than that in cell touched", "[on = selects less than; off]", commandName, "toggleLT")) {
    	 		boolean current = lessthan.getValue();
    	 		lessthan.toggleValue(parser.getFirstToken(arguments));
    	 	}
    	 	else if (checker.compare(this.getClass(), "Sets whether the wand selects characters based upon values in only the single taxon touched", "[on = selects in taxon; off]", commandName, "toggleSameInTaxon")) {
    	 		boolean current = toggleSameInTaxon.getValue();
    	 		toggleSameInTaxon.toggleValue(parser.getFirstToken(arguments));
    	 		if (toggleSameInTaxon.getValue()) {
    	 			toggleSameWhole.setValue(false);
    	 			toggleSimilarWhole.setValue(false);
    	 		}
    	 	}
    	 	else if (checker.compare(this.getClass(), "Sets whether the wand selects characters that exactly match the character touched", "[on = selects same as whole character; off]", commandName, "toggleSameWhole")) {
    	 		boolean current = toggleSameWhole.getValue();
    	 		toggleSameWhole.toggleValue(parser.getFirstToken(arguments));
    	 		if (toggleSameWhole.getValue()) {
    	 			toggleSameInTaxon.setValue(false);
    	 			toggleSimilarWhole.setValue(false);
    	 		}
    	 	}
    	 	else if (checker.compare(this.getClass(), "Sets whether the wand selects characters that are similar to the character touched", "[on = selects similar to whole character; off]", commandName, "toggleSimilarWhole")) {
    	 		boolean current = toggleSimilarWhole.getValue();
    	 		toggleSimilarWhole.toggleValue(parser.getFirstToken(arguments));
    	 		if (toggleSimilarWhole.getValue()) {
    	 			toggleSameWhole.setValue(false);
    	 			toggleSameInTaxon.setValue(false);
    	 		}
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
		return null;
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Character Wand (data)";
   	 }
   	 
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Provides a magic wand tool with which to select characters automatically.";
   	 }
}


	


