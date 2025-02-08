/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charMatrices.MatrixNavigationTool; 

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.ui.MesquitePopup;
import mesquite.lib.ui.MesquiteWindow;
import mesquite.categ.lib.*;

/* TODO: 
 * 	- emphasize rows and columns on mousedowns
 * 	- convert to tool tip
 * 	- have info for things other than data cells; e.g., for whole taxa
 */

/* ======================================================================== */
public class MatrixNavigationTool extends DataWindowAssistantI {
	CMTable table;
	CharacterData  data;
	Taxa taxa;
	protected TableTool matrixNavigationTool;
	MesquiteWindow window;
	MesquitePopup popup;
	MesquiteCommand respondCommand, moveToPrevCommand, moveToNextCommand;
	MesquiteCommand moveToFirstDataCommand, moveToLastDataCommand;
	MesquiteCommand moveToFirstRowCommand, moveToLastRowCommand;
	MesquiteCommand scrollToCharacterCommand, scrollToBaseNumberCommand;
	


	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (containerOfModule() instanceof MesquiteWindow) {
			matrixNavigationTool = new TableTool(this, "matrixNavigation", getPath(), "matrixNavigation.gif", 8, 8,"Matrix navigation", "This tool allows you to navigate quickly around the matrix", MesquiteModule.makeCommand("matrixNavigation",  this) , null, null);
			matrixNavigationTool.setWorksOnColumnNames(false);
			matrixNavigationTool.setWorksOnRowNames(false);
			matrixNavigationTool.setWorksOnMatrixPanel(true);
			matrixNavigationTool.setWorksOnCornerPanel(false);
			window = (MesquiteWindow)containerOfModule();
			window.addTool(matrixNavigationTool);
			respondCommand = makeCommand("respond", this);
			moveToPrevCommand = makeCommand("moveToPrev", this);
			moveToNextCommand = makeCommand("moveToNext", this);
			moveToFirstDataCommand = makeCommand("moveToFirstData", this);
			moveToLastDataCommand = makeCommand("moveToLastData", this);
			moveToFirstRowCommand = makeCommand("moveToFirstRow", this);
			moveToLastRowCommand = makeCommand("moveToLastRow", this);
			scrollToCharacterCommand = makeCommand("scrollToCharacter", this);
			scrollToBaseNumberCommand = makeCommand("scrollToBaseNumber", this);
		}
		else return sorry(getName() + " couldn't start because the window with which it would be associated is not a tool container.");
		return true;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}
	/*.................................................................................................................*/
	public void setTableAndData(MesquiteTable table, CharacterData data){
		this.table = (CMTable)table;
		this.data = data;
		taxa = data.getTaxa();
	}

	/*.................................................................................................................*/
	void addToPopup(String s,int response){
		if (popup==null)
			return;
		popup.addItem(s, this, respondCommand, Integer.toString(response));
	}
	/*.................................................................................................................*/
	void addToPopup(String s,MesquiteCommand command, int response){
		if (popup==null)
			return;
		popup.addItem(s, this, command, Integer.toString(response));
	}
	/*.................................................................................................................*/
	public void moveToBaseInSequence() { 
		if (data == null || table ==null || !MesquiteInteger.isCombinable(column) || !MesquiteInteger.isCombinable(row))
			return;
		int num = MesquiteInteger.queryInteger(table.getWindow(), "Base within sequence to move to", "Base within sequence to move to", 1, 1, data.getNumberApplicableInTaxon(row, false));
		if (!MesquiteInteger.isCombinable(num)) 	
				return;
		int count=0;
		for (int ic=0; ic<data.getNumChars(); ic++) {
			if (!data.isInapplicable(ic, row)){
				count++;
				if (count>num) {
					table.scrollToColumn(ic);
					break;
				}
			}
		}
		column = MesquiteInteger.unassigned;
		row = MesquiteInteger.unassigned;
	}
	/*.................................................................................................................*/
	public void moveToNext(boolean next) { 
		if (data == null || table ==null || !MesquiteInteger.isCombinable(column) || !MesquiteInteger.isCombinable(row))
			return;
		int icCurrent = column;
		if (next) {
			if (!MesquiteInteger.isCombinable(icCurrent))
				icCurrent = -1;
			for (int ic=icCurrent+1; ic<data.getNumChars(); ic++) {
				if (!data.isInapplicable(ic, row)){
					table.scrollToColumn(ic);
					break;
				}
			}
		} else {
			if (!MesquiteInteger.isCombinable(icCurrent))
				icCurrent = data.getNumChars();
			for (int ic=icCurrent-1; ic>=0; ic--) {
				if (!data.isInapplicable(ic, row)){
					table.scrollToColumn(ic);
					break;
				}
			}
		}
		column = MesquiteInteger.unassigned;
		row = MesquiteInteger.unassigned;
	}
	/*.................................................................................................................*/
	public void moveToStart(boolean start) { 
		if (data == null || table ==null || !MesquiteInteger.isCombinable(column) || !MesquiteInteger.isCombinable(row))
			return;
		if (start) {
			for (int ic=0; ic<data.getNumChars(); ic++) {
				if (!data.isInapplicable(ic, row)){
					table.scrollToColumn(ic);
					break;
				}
			}
		} else {
			for (int ic=data.getNumChars()-1; ic>=0; ic--) {
				if (!data.isInapplicable(ic, row)){
					table.scrollToColumn(ic);
					break;
				}
			}
		}
		column = MesquiteInteger.unassigned;
		row = MesquiteInteger.unassigned;
	}
	/*.................................................................................................................*/
	public void moveToRow(boolean firstRow) { 
		if (data == null || table ==null || !MesquiteInteger.isCombinable(column) || !MesquiteInteger.isCombinable(row))
			return;
		if (firstRow) 
			table.scrollToRow(1);
		else
			table.scrollToRow(data.getNumTaxa()-1);
		column = MesquiteInteger.unassigned;
		row = MesquiteInteger.unassigned;
	}
	int column = MesquiteInteger.unassigned;
	int row = MesquiteInteger.unassigned;
	/*.................................................................................................................*/
	void makePopupMenu (String arguments) {
		if (table!=null && data !=null && taxa!=null){
			MesquiteInteger io = new MesquiteInteger(0);
			column= MesquiteInteger.fromString(arguments, io);
			row= MesquiteInteger.fromString(arguments, io);
			int responseNumber = 0;
			if (popup==null)
				popup = new MesquitePopup(window.getGraphicsArea());
			popup.removeAll();
			popup.setEnabled(true);
			addToPopup("Taxon: " + taxa.getTaxonName(row)+", character: " + (column+1), responseNumber++);
			addToPopup("-", responseNumber++);

			addToPopup("Scroll to character...", scrollToCharacterCommand, responseNumber++);
			if (data instanceof MolecularData)
				addToPopup("Scroll to base number in sequence...", scrollToBaseNumberCommand, responseNumber++);
			addToPopup("-", responseNumber++);

			addToPopup("Scroll to start of data", moveToFirstDataCommand, responseNumber++);
			addToPopup("Scroll to end of data", moveToLastDataCommand, responseNumber++);
			addToPopup("Scroll to previous data", moveToPrevCommand, responseNumber++);
			addToPopup("Scroll to next data", moveToNextCommand, responseNumber++);
			addToPopup("-", responseNumber++);
			addToPopup("Scroll to first row", moveToFirstRowCommand, responseNumber++);
			addToPopup("Scroll to last row", moveToLastRowCommand, responseNumber++);
			popup.showPopup(table.getColumnX(column), table.getRowY(row));
		}
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "navigate", "[column touched] [row touched] [percent horizontal] [percent vertical] [modifiers]", commandName, "matrixNavigation")) {
			makePopupMenu(arguments);
		}
		else if (checker.compare(this.getClass(), "Responds to choice of popup menu", "[choice number]", commandName, "respond")) {
		}
		else if (checker.compare(this.getClass(), "Move to Character", "", commandName, "scrollToCharacter")) {
			if (data!=null && table!=null) {
				int num = MesquiteInteger.queryInteger(table.getWindow(), "Number of character to move to", "Number of character to move to", 1, 1, data.getNumChars());
				if (MesquiteInteger.isCombinable(num))
					table.scrollToColumn(num);
			}
		}
		else if (checker.compare(this.getClass(), "Move to base in sequence", "", commandName, "scrollToBaseNumber")) {
			if (data!=null && table!=null) {
					moveToBaseInSequence();
			}
		}
		else if (checker.compare(this.getClass(), "Move to Previous Data", "", commandName, "moveToPrev")) {
			moveToNext(false);
		}
		else if (checker.compare(this.getClass(), "Move to Next Data", "", commandName, "moveToNext")) {
			moveToNext(true);
		}
		else if (checker.compare(this.getClass(), "Move to First Data", "", commandName, "moveToFirstData")) {
			moveToStart(true);
		}
		else if (checker.compare(this.getClass(), "Move to Last Data", "", commandName, "moveToLastData")) {
			moveToStart(false);
		}
		else if (checker.compare(this.getClass(), "Move to First Row", "", commandName, "moveToFirstRow")) {
			moveToRow(true);
		}
		else if (checker.compare(this.getClass(), "Move to Last Row", "", commandName, "moveToLastRow")) {
			moveToRow(false);
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Matrix Navigation";
	}
	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return true;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Allows one to navigate quickly around the matrix." ;
	}

}



