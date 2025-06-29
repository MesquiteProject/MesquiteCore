/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charMatrices.ColorCells; 

import java.awt.Color;

import mesquite.lib.CommandChecker;
import mesquite.lib.FunctionExplanation;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteLong;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteThread;
import mesquite.lib.NameReference;
import mesquite.lib.Snapshot;
import mesquite.lib.StringUtil;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.CellColorer;
import mesquite.lib.duties.CellColorerCharacters;
import mesquite.lib.duties.CellColorerMatrix;
import mesquite.lib.duties.CellColorerTaxa;
import mesquite.lib.duties.DataWindowAssistantID;
import mesquite.lib.duties.DataWindowMaker;
import mesquite.lib.table.MesquiteTable;
import mesquite.lib.table.TableTool;
import mesquite.lib.ui.ColorDistribution;
import mesquite.lib.ui.ColorRecord;
import mesquite.lib.ui.MesquiteSubmenuSpec;
import mesquite.lib.ui.MesquiteWindow;

/*   to do:
	- deal with Option cursor, Shift cursor


 *new in 1.02*
 */


/* ======================================================================== */
public class ColorCells extends DataWindowAssistantID implements CellColorer, CellColorerCharacters, CellColorerTaxa, CellColorerMatrix {
	TableTool colorTool; 
	MesquiteTable table;
	long currentColor = ColorDistribution.numberOfRed;
	String colorString = "Color Red";
	long savedColor = currentColor;
	MesquiteBoolean removeColor;
	CharacterData data;
	public void getSubfunctions(){
		registerSubfunction(new FunctionExplanation("Assign color to cell", "(A tool in the character matrix editor) Assigns color to the cell touched", null, getPath() + "color.gif"));
		super.getSubfunctions();
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		if (containerOfModule() instanceof MesquiteWindow) {
			colorTool = new TableTool(this, "ColorCells", getPath(), "color.gif", 1,1,colorString, "This tool colors the cells of a matrix.  This has cosmetic effect only.. ", MesquiteModule.makeCommand("colorCell", this), null, null);
			colorTool.setWorksOnColumnNames(true);
			colorTool.setWorksOnRowNames(true);
			((MesquiteWindow)containerOfModule()).addTool(colorTool);
			colorTool.setPopUpOwner(this);
			setUseMenubar(false); //menu available by touching button

		}
		else return false;
		MesquiteSubmenuSpec mss = addSubmenu(null, "Cell paint color", makeCommand("setColor",  this), ColorDistribution.standardColorNames);
		removeColor = new MesquiteBoolean(false);
		addCheckMenuItem(null, "Remove color", makeCommand("removeColor",  this), removeColor);
		addMenuItem(null, "Remove all color", makeCommand("removeAllColor",  this));
		addMenuSeparator();
		addMenuItem(null, "Set Color of Selected", makeCommand("colorSelected",  this));
		return true;
	}
	public boolean setActiveColors(boolean active){
		return true; 
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}
	private void removeColor(int ic, int it, boolean notify){
		setColor(ic, it, -1);
		if (notify)
			table.redrawCell(ic,it);
	}
	private void colorSelected(){
		if (data == null)
			return;
		long color = -1;
		if (MesquiteLong.isCombinable(currentColor))
			color = currentColor;
		for (int ic = -1; ic<data.getNumChars(); ic++)
			for (int it = -1; it<data.getNumTaxa(); it++)
				if (table.isCellSelectedAnyWay(ic, it))
					setColor(ic, it, (int)currentColor);
	}
	private void removeAllColor(boolean notify){
		if (data == null)
			return;
		for (int ic = -1; ic<data.getNumChars(); ic++)
			for (int it = -1; it<data.getNumTaxa(); it++)
				setColor(ic, it, -1);
		if (notify)
			table.repaintAll();
	}
	public String getCellString(int ic, int it){
		if (!isActive())
			return null;
		return "Colors assigned by user";
	}
	
	NameReference oldColourNameRef = NameReference.getNameReference("color");
	private void setColor(int ic, int it, int c){
		if (data == null)
			return;
		if (ic<0 && it<0){
		}
		else if (ic<0) { //taxon
			if (c == -1)
				data.getTaxa().setColor(it, (String)null); 
			else
				data.getTaxa().setColor(it, ColorDistribution.getStandardColorAsHex(c));
		}
		else if (it < 0){ //character
			if (c == -1)
				data.setColor(ic, (String)null); 
			else
				data.setColor(ic, ColorDistribution.getStandardColorAsHex(c));

		}
		else if (!MesquiteLong.isCombinable(c) || c<0){
			data.setCellObject(oldColourNameRef, ic, it, null);
		}
		else {
			MesquiteInteger ms = new MesquiteInteger(c);
			data.setCellObject(oldColourNameRef, ic, it, ms);
		}
		table.redrawCell(ic,it);
	}
	private Color getColor(int ic, int it){
		if (data == null)
			return null;
		if (ic<0){  //taxon
			return data.getTaxa().getColor(it);
		}
		else if (it<0){ //character
			return data.getColor(ic);
		}
		else {
			Object obj = data.getCellObject(oldColourNameRef, ic, it);
			if (obj != null && obj instanceof MesquiteInteger) {
				int col = ((MesquiteInteger)obj).getValue();
				return ColorDistribution.getStandardColor(col);
			}
		}
		return null;
	}
	/*.................................................................................................................*/
	public void viewChanged(){
	}
	public ColorRecord[] getLegendColors(){
		return null;
	}
	public String getColorsExplanation(){
		return null;
	}
	public Color getCellColor(int ic, int it){
		return getColor(ic, it);
	}
	public void setTableAndData(MesquiteTable table, CharacterData data){
		this.table = table;
		this.data = data;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("setColor " + ColorDistribution.getStandardColorName(ColorDistribution.getStandardColor((int)currentColor)));
		temp.addLine("removeColor " + removeColor.toOffOnString());
		return temp;
	}
	MesquiteInteger pos = new MesquiteInteger();
	boolean askedAlreadyCells = false;
	boolean askedAlreadyRowNames = false;
	boolean askedAlreadyColumnNames = false;
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(),  "Colors cell", "[column touched][row touched]", commandName, "colorCell")) {
			if (data == null)
				return null;
			MesquiteInteger io = new MesquiteInteger(0);
			int column= MesquiteInteger.fromString(arguments, io);
			int row= MesquiteInteger.fromString(arguments, io);

			if (MesquiteInteger.isCombinable(column)&& (MesquiteInteger.isCombinable(row))) {
				if (table != null && table.isCellSelectedAnyWay(column, row)){
					colorSelected();
				}
				else if (!MesquiteLong.isCombinable(currentColor))
					removeColor(column, row, true);
				else
					setColor(column, row, (int)currentColor);
			}
			if (!MesquiteThread.isScripting()){
				MesquiteModule mb = findEmployerWithDuty(DataWindowMaker.class);
				if (mb != null && mb instanceof DataWindowMaker) {
					String message = null;

					if (column >=0 && row >= 0) { 
						if (!askedAlreadyCells)
							message = "Do you want the cells to be colored using the colors you are assigning?";
						askedAlreadyCells = true;
					}
					else if (column < 0 && row >= 0){
						if (!askedAlreadyRowNames)
							message = "Do you want the taxon name cells to be colored using the colors you are assigning?";
						askedAlreadyRowNames = true;
					}
					else if (column < 0 && row >= 0){
						if (!askedAlreadyColumnNames)
							message = "Do you want the character name cells to be colored using the colors you are assigning?";
						askedAlreadyColumnNames = true;
					}
					((DataWindowMaker)mb).requestCellColorer(this,column, row, message);
				}
			}
		}
		else	if (checker.compare(this.getClass(), "Sets the color to be used to paint cells", "[name of color]", commandName, "setColor")) {
			int bc = ColorDistribution.standardColorNames.indexOf(parser.getFirstToken(arguments)); 
			if (bc >=0 && MesquiteLong.isCombinable(bc)){
				removeColor.setValue(false);
				currentColor = bc;
				savedColor = bc;
				colorString = "Color " + ColorDistribution.standardColorNames.getValue(bc);
			}
		}
		else if (checker.compare(this.getClass(), "Removes color from all the cells", null, commandName, "removeAllColor")) {
			removeAllColor(true);
		}
		else if (checker.compare(this.getClass(), "Colors selected cells", null, commandName, "colorSelected")) {
			colorSelected();
			table.repaintAll();
		}
		else if (checker.compare(this.getClass(), "Sets the paint brush so that it removes colors from any cells touched", null, commandName, "removeColor")) {
			if (StringUtil.blank(arguments))
				removeColor.setValue(!removeColor.getValue());
			else
				removeColor.toggleValue(parser.getFirstToken(arguments));

			if (removeColor.getValue()) {
				colorString = "Remove color";
				currentColor = MesquiteLong.unassigned;
			}
			else {
				colorString = "Color " + ColorDistribution.standardColorNames.getValue((int)currentColor);
				currentColor = savedColor;
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Assigned Colors";
	}

	/*.................................................................................................................*/
	public String getExplanation() {
		return "Provides a tool with which to color cells of a matrix.";
	}
}





