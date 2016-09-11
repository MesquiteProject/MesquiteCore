/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charMatrices.Wand; //DRM started April 02

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
public class Wand extends DataWindowAssistantI {
	TableTool magicWandTool;
	CharacterData data;
	MesquiteTable table;
	MesquiteBoolean wholeMatrix, singleTaxon, singleCharacter, contiguous, equals, greaterthan, lessthan, selectByText, selectByColor;
	boolean defaultEquals = true;
	boolean defaultGT = false;
	boolean defaultLT = false;
	boolean defaultSingleTaxon = false;
	boolean defaultSingleCharacter = false;
	boolean defaultWholeMatrix = true;
	boolean defaultContiguous = false;
	boolean taxaAreRows = true;
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
		selectByText = new MesquiteBoolean(true);

		selectByColor = new MesquiteBoolean(!selectByText.getValue());
		singleTaxon = new MesquiteBoolean(defaultSingleTaxon);
		singleCharacter = new MesquiteBoolean(defaultSingleCharacter);
		wholeMatrix = new MesquiteBoolean(defaultWholeMatrix);
		contiguous = new MesquiteBoolean(defaultContiguous);
		equals = new MesquiteBoolean(defaultEquals);
		greaterthan = new MesquiteBoolean(defaultGT);
		lessthan = new MesquiteBoolean(defaultLT);
		addCheckMenuItem(null, "Select by Text", makeCommand("selectByText",  this), selectByText);
		addCheckMenuItem(null, "Select by Color", makeCommand("selectByColor",  this), selectByColor);
		addMenuSeparator();
		eItem = addCheckMenuItem(null, "Equal", makeCommand("toggleEquals",  this), equals);
		gItem = addCheckMenuItem(null, "Greater than", makeCommand("toggleGT",  this), greaterthan);
		lItem = addCheckMenuItem(null, "Less than", makeCommand("toggleLT",  this), lessthan);
		addMenuSeparator();
		addCheckMenuItem(null, "Whole Matrix", makeCommand("toggleWholeMatrix",  this), wholeMatrix);
		addCheckMenuItem(null, "Restrict to single taxon", makeCommand("toggleSingleTaxon",  this), singleTaxon);
		addCheckMenuItem(null, "Restrict to single character", makeCommand("toggleSingleCharacter",  this), singleCharacter);
		addCheckMenuItem(null, "Contiguous", makeCommand("toggleContiguous",  this), contiguous);
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
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		if (wholeMatrix.getValue()!=defaultWholeMatrix)
			temp.addLine("toggleWholeMatrix " + wholeMatrix.toOffOnString());
		if (singleTaxon.getValue()!=defaultSingleTaxon)
			temp.addLine("toggleSingleTaxon " + singleTaxon.toOffOnString());
		if (singleCharacter.getValue()!=defaultSingleCharacter)
			temp.addLine("toggleSingleCharacter " + singleCharacter.toOffOnString());
		if (contiguous.getValue()!=defaultContiguous)
			temp.addLine("toggleContiguous " + contiguous.toOffOnString());
		if (equals.getValue()!=defaultEquals)
			temp.addLine("toggleEquals " + equals.toOffOnString());
		if (greaterthan.getValue()!=defaultGT)
			temp.addLine("toggleGT " + greaterthan.toOffOnString());
		if (lessthan.getValue()!=defaultLT)
			temp.addLine("toggleLT " + lessthan.toOffOnString());
		if (!selectByText.getValue())
			temp.addLine("selectByColor");
		return temp;
	}
	boolean satisfiesCriteria(String one, String two){
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
		return false;
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
				else if (selectByText.getValue()){
					if (contiguous.getValue()){
						String text = table.getMatrixText(column, row);
						if (contigSel == null || contigSel.length != table.getNumColumns() || contigSel[0].length != table.getNumRows())
							contigSel = new boolean[table.getNumColumns()][table.getNumRows()];
						for (int i = 0; i< contigSel.length; i++)
							for (int k = 0; k<table.getNumRows(); k++)
								contigSel[i][k] = false;
						numContigFound = 0;
						selectContiguous(column+1, row, text, subtractFromSelection, true, true, 0);
						selectContiguous(column-1, row, text, subtractFromSelection, true, false, 0);
						selectContiguous(column, row+1, text, subtractFromSelection, false, true, 0);
						selectContiguous(column, row-1, text, subtractFromSelection, false, false, 0);
						checkCell(column, row, text, subtractFromSelection);
					} 
					else {
						String text = table.getMatrixText(column, row);
						int rowStart = 0;
						int rowEnd = table.getNumRows();
						int columnStart = 0;
						int columnEnd = table.getNumColumns();
						numContigFound = 0;
						if (singleTaxon.getValue()) {
							if (taxaAreRows) {
								rowStart = row;
								rowEnd = row+1;
							}
							else {
								columnStart = column;
								columnEnd = column+1;
							}
						}
						else if (singleCharacter.getValue()) {
							if (!taxaAreRows) {
								rowStart = row;
								rowEnd = row+1;
							}
							else {
								columnStart = column;
								columnEnd = column+1;
							}
						}
						for (int j=rowStart; j<rowEnd; j++){
							for (int i=columnStart; i<columnEnd; i++){
								if (satisfiesCriteria(text, table.getMatrixText(i, j))) {
									numContigFound++;
									if (numContigFound % 100 == 0)
										CommandRecord.tick(Integer.toString(numContigFound) + " cells found");
									if (subtractFromSelection)
										table.deselectCell(i, j); //deselect cell
									else
										table.selectCell(i, j); //select cell
								}
							}
						}
					}
				}
				else {
					if (getEmployer() instanceof mesquite.charMatrices.BasicDataWindowMaker.BasicDataWindowMaker){
						mesquite.charMatrices.BasicDataWindowMaker.BasicDataWindowMaker mb = (mesquite.charMatrices.BasicDataWindowMaker.BasicDataWindowMaker)getEmployer();
						mb.selectSameColor(column, row, singleCharacter.getValue(), singleTaxon.getValue(), contiguous.getValue(), subtractFromSelection);
					}
				}
				data.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));

			}
		}
		else if (checker.compare(this.getClass(), "Sets the wand to select by text", null, commandName, "selectByText")) {
			selectByText.setValue(true);
			selectByColor.setValue(false);
			eItem.setEnabled(true);
			gItem.setEnabled(true);
			lItem.setEnabled(true);
		}
		else if (checker.compare(this.getClass(), "Sets the wand to select by color", null, commandName, "selectByColor")) {
			selectByText.setValue(false);
			selectByColor.setValue(true);
			eItem.setEnabled(false);
			gItem.setEnabled(false);
			lItem.setEnabled(false);
		}
		else if (checker.compare(this.getClass(), "Sets whether the wand selects through the matrix", "[on = whole matrix; off]", commandName, "toggleWholeMatrix")) {
			boolean current = wholeMatrix.getValue();
			if (current && StringUtil.blank(arguments)) //from menu; reselecting; don't change
				return null;
			wholeMatrix.toggleValue(parser.getFirstToken(arguments));
			if (wholeMatrix.getValue()) {
				singleTaxon.setValue(false);
				singleCharacter.setValue(false);
				contiguous.setValue(false);
			}
		}
		else if (checker.compare(this.getClass(), "Sets whether the wand selects contiguously", "[on = contiguous; off]", commandName, "toggleContiguous")) {
			boolean current = contiguous.getValue();
			if (current && StringUtil.blank(arguments)) //from menu; reselecting; don't change
				return null;
			contiguous.toggleValue(parser.getFirstToken(arguments));
			if (contiguous.getValue()) {
				wholeMatrix.setValue(false);
				singleTaxon.setValue(false);
				singleCharacter.setValue(false);
			}
		}
		else if (checker.compare(this.getClass(), "Sets whether the wand selects only cells of the taxon that was touched", "[on = single taxon; off]", commandName, "toggleSingleTaxon")) {
			boolean current = singleTaxon.getValue();
			if (current && StringUtil.blank(arguments)) //from menu; reselecting; don't change
				return null;
			singleTaxon.toggleValue(parser.getFirstToken(arguments));
			if (singleTaxon.getValue()) {
				wholeMatrix.setValue(false);
				singleCharacter.setValue(false);
				contiguous.setValue(false);
			}
		}
		else if (checker.compare(this.getClass(), "Sets whether the wand selects only cells of the character that was touched", "[on = single character; off]", commandName, "toggleSingleCharacter")) {
			boolean current = singleCharacter.getValue();
			if (current && StringUtil.blank(arguments)) //from menu; reselecting; don't change
				return null;
			singleCharacter.toggleValue(parser.getFirstToken(arguments));
			if (singleCharacter.getValue()) {
				wholeMatrix.setValue(false);
				singleTaxon.setValue(false);
				contiguous.setValue(false);
			}
		}
		else if (checker.compare(this.getClass(), "Sets whether the wand selects cells with value equal to that in cell touched", "[on = selects equal; off]", commandName, "toggleEquals")) {
			boolean current = equals.getValue();
			equals.toggleValue(parser.getFirstToken(arguments));
		}
		else if (checker.compare(this.getClass(), "Sets whether the wand selects cells with value greater than that in cell touched", "[on = selects greater than; off]", commandName, "toggleGT")) {
			boolean current = greaterthan.getValue();
			greaterthan.toggleValue(parser.getFirstToken(arguments));
		}
		else if (checker.compare(this.getClass(), "Sets whether the wand selects cells with value less than that in cell touched", "[on = selects less than; off]", commandName, "toggleLT")) {
			boolean current = lessthan.getValue();
			lessthan.toggleValue(parser.getFirstToken(arguments));
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
		if (satisfiesCriteria(text, table.getMatrixText(ic, it))) {
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
		}   
		return false;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Cell Wand (data)";
	}

	/*.................................................................................................................*/
	public String getExplanation() {
		return "Provides a tool with which to select cells in a matrix automatically.";
	}
}





