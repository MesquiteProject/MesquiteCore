/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charMatrices.BooleanForCharInfoStrip;

import java.awt.Color;
import java.awt.Graphics;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.DataColumnNamesAssistant;
import mesquite.lib.table.ColumnNamesPanel;
import mesquite.lib.table.MesquiteTable;

public class BooleanForCharInfoStrip extends DataColumnNamesAssistant {
	MesquiteTable table;
	CharacterData data;
	BooleanForCharacter booleanTask;
	MesquiteMenuItemSpec closeMenuItem, toggleDirectionItem, selectMatchesItem, moveToNextMatchItem,moveToPrevMatchItem;
	MesquiteBoolean standardDirection = new MesquiteBoolean(true);

	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(BooleanForCharacter.class, getName() + " needs a method to calculate a boolean (yes/no) value for each of the characters.",
		"You can select a value to show in an information strip of a Data Matrix Editor. ");
	}

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition,boolean hiredByName) {
		if (arguments !=null) {
			booleanTask = (BooleanForCharacter)hireNamedEmployee(BooleanForCharacter.class, arguments);
			if (booleanTask==null) {
				return sorry("Boolean for character (for info strip) can't start because the requested calculator module wasn't successfully hired");
			}
		}
		else {
			booleanTask = (BooleanForCharacter)hireEmployee(BooleanForCharacter.class, "Value to calculate for character (for character list)");
			if (booleanTask==null) {
				return sorry("Boolean for character (for info strip) can't start because no calculator module was successfully hired");
			}
		}
		return true;
	}

	/*.................................................................................................................*/
	public void setBooleanTask(BooleanForCharacter booleanTask) {
		this.booleanTask = booleanTask;
		deleteRemoveMenuItem();
		addRemoveMenuItem();
	}
	/*.................................................................................................................*/
	public void drawInCell(int ic, Graphics g, int x, int y, int w, int h, boolean selected) {
		if (data == null || booleanTask==null)
			return;
		MesquiteBoolean result = new MesquiteBoolean();
		MesquiteString resultString = new MesquiteString();
		booleanTask.calculateBoolean( data,  ic,  result,  resultString);
		
		if (booleanTask.displayTrueAsDark()==standardDirection.getValue()) {
			if (result.getValue())
				g.setColor(Color.gray); 
			else {
				g.setColor(Color.white);
			}		
		}
		else if (result.getValue())
			g.setColor(Color.white); 
		else {
			g.setColor(Color.gray);
		}
		g.fillRect(x, y, w, h);
	}
	
	/** Returns string to be displayed in explanation area on mouseover of cell */
	public String getStringForExplanation(int ic){
		if (data == null || booleanTask==null)
			return null;
		MesquiteBoolean result = new MesquiteBoolean();
		MesquiteString resultString = new MesquiteString();
		booleanTask.calculateBoolean( data,  ic,  result,  resultString);

		return booleanTask.getValueString(result.getValue());
	}


	/*.................................................................................................................*/
	public void deleteRemoveMenuItem() {
		deleteMenuItem(closeMenuItem);
		deleteMenuItem(toggleDirectionItem);
		deleteMenuItem(selectMatchesItem);
		deleteMenuItem(moveToNextMatchItem);
		deleteMenuItem(moveToPrevMatchItem);
	}
	public void addRemoveMenuItem() {
		if (booleanTask!=null)
			closeMenuItem= addMenuItem(null,"Remove " + booleanTask.getName() +  " Info Strip", makeCommand("remove", this));
		else 
			closeMenuItem= addMenuItem(null,"Remove Info Strip", makeCommand("remove", this));
		toggleDirectionItem= addMenuItem(null,"Flip Dark/Light", makeCommand("toggleDirection", this));
		selectMatchesItem= addMenuItem(null,"Select Characters Satisfying Criterion", makeCommand("selectMatches", this));
		moveToNextMatchItem= addMenuItem(null,"Move to Next Character Satisfying Criterion", makeCommand("moveToNextMatch", this));
		moveToPrevMatchItem= addMenuItem(null,"Move to Previous Character Satisfying Criterion", makeCommand("moveToPrevMatch", this));
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
		temp.addLine("setValueTask ", booleanTask); 
		temp.addLine("toggleDirection " + standardDirection.toOffOnString());
		return temp;
	}

	/*.................................................................................................................*/
	public void selectSame(boolean selectTrue) { 
		if (data == null || booleanTask==null || table ==null)
			return;
		boolean selectionChange = false;
		for (int ic=0; ic<data.getNumChars(); ic++) {
			MesquiteBoolean result = new MesquiteBoolean();
			MesquiteString resultString = new MesquiteString();
			booleanTask.calculateBoolean( data,  ic,  result,  resultString);
			
			if (result.getValue()==selectTrue){
				if (!table.isColumnSelected(ic))
					selectionChange=true;
				table.selectColumn(ic);
			}
		}
		if (selectionChange)
   			data.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
	}

	/*.................................................................................................................*/
	public void moveToNext(boolean next) { 
		if (data == null || booleanTask==null || table ==null)
			return;
		boolean selectionChange = false;
		int icCurrent = getColumnTouched();
		if (next) {
			if (!MesquiteInteger.isCombinable(icCurrent))
				icCurrent = -1;
			for (int ic=icCurrent+1; ic<data.getNumChars(); ic++) {
				MesquiteBoolean result = new MesquiteBoolean();
				MesquiteString resultString = new MesquiteString();
				booleanTask.calculateBoolean( data,  ic,  result,  resultString);

				if (result.getValue()){
					table.scrollToColumn(ic);
					break;
				}
			}
		} else {
			if (!MesquiteInteger.isCombinable(icCurrent))
				icCurrent = data.getNumChars();
			for (int ic=icCurrent-1; ic>=0; ic--) {
				MesquiteBoolean result = new MesquiteBoolean();
				MesquiteString resultString = new MesquiteString();
				booleanTask.calculateBoolean( data,  ic,  result,  resultString);

				if (result.getValue()){
					table.scrollToColumn(ic);
					break;
				}
			}
		}
		clearColumnTouched();
	}

	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets module that calculates a boolean for a character", "[name of module]", commandName, "setValueTask")) {
			BooleanForCharacter temp= (BooleanForCharacter)replaceEmployee(BooleanForCharacter.class, arguments, "Boolean for a character", booleanTask);
			if (temp!=null) {
				setBooleanTask(temp);
				parametersChanged();
				return temp;
			}
		}
		else if (checker.compare(this.getClass(), "Removes the Info Strip", null, commandName, "remove")) {
			iQuit();
		}
		else if (checker.compare(this.getClass(), "Selects characters with a value of true for this boolean", null, commandName, "selectMatches")) {
			selectSame(true);
		}
		else if (checker.compare(this.getClass(), "Moves to next character with value of true for this boolean", null, commandName, "moveToNextMatch")) {
			moveToNext(true);
		}
		else if (checker.compare(this.getClass(), "Moves to previous character with value of true for this boolean", null, commandName, "moveToPrevMatch")) {
			moveToNext(false);
		}
		else if (checker.compare(this.getClass(), "Toggles the dark/light display", "[on, off]", commandName, "toggleDirection")) {
			standardDirection.toggleValue(parser.getFirstToken(arguments));
			table.getColumnNamesPanel().repaint();
			parametersChanged();
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	public String getTitle() {
		return "Boolean Character Info Strip";
	}

	public void setTableAndData(MesquiteTable table, CharacterData data) {
		deleteRemoveMenuItem();
		addRemoveMenuItem();

		this.table = table;
		this.data = data;
	}

	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false; 
	}

	/*.................................................................................................................*/
	public void endJob() {
		if (table!=null) {
			((ColumnNamesPanel)table.getColumnNamesPanel()).decrementInfoStrips();
			table.resetTableSize(false);
		}
		super.endJob();
	}

	public String getName() {
		return "Boolean Info Strip";
	}
	public String getExplanation() {
		return "Shows an strip of boxes near the top of each a character column that shows the value of a boolean for that character in gray or white.";
	}

	/*.................................................................................................................*/
	public int getVersionOfFirstRelease(){
		return 260;  
	}


}
