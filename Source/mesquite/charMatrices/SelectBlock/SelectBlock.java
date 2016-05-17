/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charMatrices.SelectBlock;
import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/*   to do:
	- deal with Option cursor, Shift cursor
	- pass taxaAreRows into this
 */


/* ======================================================================== */
public class SelectBlock extends DataWindowAssistantI {
	TableTool magicWandTool;
	CharacterData data;
	MesquiteTable table;
	boolean defaultWholeMatrix = false;
	MesquiteBoolean wholeMatrix;
	MesquiteMenuItemSpec eItem, gItem, lItem;
	MesquiteCollator collator;
	 public String getFunctionIconPath(){
   		 return getPath() + "magicWand.gif";
   	 }
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		if (containerOfModule() instanceof MesquiteWindow) {
			magicWandTool = new TableTool(this, "magicWand", getPath(), "magicWand.gif", 4,4,"Select cells with same", "This tool selects cells according to criteria of being the same, greater than, or less than the value in the cell touched.  The criteria can be set using the menu that appears when you click on the tool button", MesquiteModule.makeCommand("magicWandTouch",  this) , null, null);
			((MesquiteWindow)containerOfModule()).addTool(magicWandTool);
			magicWandTool.setPopUpOwner(this);
			setUseMenubar(false); //menu available by touching oning button
		}
		else return false;
		wholeMatrix = new MesquiteBoolean(defaultWholeMatrix);
		addCheckMenuItem(null, "Whole Matrix", makeCommand("toggleWholeMatrix",  this), wholeMatrix);
		collator = new MesquiteCollator();
		return true;
	}
	/*.................................................................................................................*/
	public boolean loadModule(){
		return false;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}
	public void setTableAndData(MesquiteTable table, CharacterData data){
		this.table = table;
		this.data = data;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		if (wholeMatrix.getValue()!=defaultWholeMatrix)
			temp.addLine("toggleWholeMatrix " + wholeMatrix.toOffOnString());
		return temp;
	}
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(),  "Touches on a cell with the wand to select cells similar as defined by the options", "[column touched][row touched]", commandName, "magicWandTouch")) {
			MesquiteInteger io = new MesquiteInteger(0);
			int column= MesquiteInteger.fromString(arguments, io);
			int row= MesquiteInteger.fromString(arguments, io);
			if (MesquiteInteger.isNonNegative(column)&& (MesquiteInteger.isNonNegative(row))) {
				boolean commandSelected =  (arguments.indexOf("command")>=0);
				boolean subtractFromSelection = commandSelected && table.isCellSelected(column, row);
				if ((arguments.indexOf("shift")<0) && !commandSelected)
					table.deselectAll();
				


				table.offAllEdits();
				if (arguments.indexOf("option")>=0 && getEmployer() instanceof mesquite.charMatrices.BasicDataWindowMaker.BasicDataWindowMaker){
					mesquite.charMatrices.BasicDataWindowMaker.BasicDataWindowMaker mb = (mesquite.charMatrices.BasicDataWindowMaker.BasicDataWindowMaker)getEmployer();
					mb.selectDataBlockInTaxon(column, row);
				}
				else {
					if (getEmployer() instanceof mesquite.charMatrices.BasicDataWindowMaker.BasicDataWindowMaker){
						mesquite.charMatrices.BasicDataWindowMaker.BasicDataWindowMaker mb = (mesquite.charMatrices.BasicDataWindowMaker.BasicDataWindowMaker)getEmployer();
//						mb.selectSameColor(column, row, singleCharacter.getValue(), singleTaxon.getValue(), contiguous.getValue(), subtractFromSelection);
					}
				}
				data.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));

			}
		}
		else if (checker.compare(this.getClass(), "Sets whether the wand selects through the matrix", "[on = whole matrix; off]", commandName, "toggleWholeMatrix")) {
			boolean current = wholeMatrix.getValue();
			if (current && StringUtil.blank(arguments)) //from menu; reselecting; don't change
				return null;
			wholeMatrix.toggleValue(parser.getFirstToken(arguments));
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	boolean[][] contigSel;
	int numContigFound = 0;
	void selectContiguous(int seedColumn, int seedRow, String text, boolean subtractFromSelection, boolean horizontal, boolean forwards, int level){
		if (seedColumn<0 || seedRow < 0 || seedColumn >= table.getNumColumns() || seedRow >= table.getNumRows() || contigSel[seedColumn][seedRow])
			return;
		level++;
		boolean done = false;
		if (horizontal){
			if (forwards) {
				int ic;
				for (ic=seedColumn; ic< table.getNumColumns() && !done; ic++)
					done = contigSel[ic][seedRow] || !checkCell(ic, seedRow, text, subtractFromSelection);

				int end = ic-1;
				for (ic=seedColumn; ic< end; ic++){
					selectContiguous(ic, seedRow+1, text, subtractFromSelection, !horizontal, true, level);
					selectContiguous(ic, seedRow-1, text, subtractFromSelection, !horizontal, false, level);
				}
			}
			else {
				int ic;
				for (ic=seedColumn; ic>=0 && !done; ic--)
					done = contigSel[ic][seedRow] || !checkCell(ic, seedRow, text, subtractFromSelection);

				int end = ic+1;
				for (ic=seedColumn; ic>end; ic--){
					selectContiguous(ic, seedRow+1, text, subtractFromSelection, !horizontal, true, level);
					selectContiguous(ic, seedRow-1, text, subtractFromSelection, !horizontal, false, level);
				}
			}
		}
		else {
			if (forwards) {
				int it;
				for (it=seedRow; it< table.getNumRows() && !done; it++)
					done = contigSel[seedColumn][it] || !checkCell(seedColumn, it, text, subtractFromSelection);
				int end = it-1;
				for (it=seedRow; it< end; it++){
					selectContiguous(seedColumn+1, it, text, subtractFromSelection, !horizontal, true, level);
					selectContiguous(seedColumn-1, it, text, subtractFromSelection, !horizontal, false, level);
				}
			}
			else {
				int it;
				for ( it=seedRow; it>=0 && !done; it--)
					done = contigSel[seedColumn][it] || !checkCell(seedColumn, it, text, subtractFromSelection);
				int end = it+1;
				for ( it=seedRow; it>end; it--){
					selectContiguous(seedColumn+1, it, text, subtractFromSelection, !horizontal, true, level);
					selectContiguous(seedColumn-1, it, text, subtractFromSelection, !horizontal, false, level);
				}
			}
		}
	}
	boolean checkCell(int ic, int it, String text, boolean subtractFromSelection){
		if (contigSel[ic][it])
			return false;
		contigSel[ic][it] = true;
		//table.selectCell(ic, it); //select cell
//		if (satisfiesCriteria(text, table.getMatrixText(ic, it))) {
			numContigFound++;
			if (numContigFound % 100 == 0)
				CommandRecord.tick(Integer.toString(numContigFound) + " cells found");
			if (subtractFromSelection){
				table.deselectCell(ic, it); //deselect cell
			}
			else 
				table.selectCell(ic, it); //select cell
			//table.repaintAll();
			return true;
//	}   
//		return false;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Select Block (data)";
	}

	/*.................................................................................................................*/
	public String getExplanation() {
		return "Provides a tool with which to select cells in a block in a matrix.";
	}
}





