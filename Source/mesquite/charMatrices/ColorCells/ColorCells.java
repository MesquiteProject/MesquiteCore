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

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.lib.characters.*;

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
			colorNameRef = NameReference.getNameReference("color");
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
	NameReference colorNameRef = NameReference.getNameReference("color");
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
	private void setColor(int ic, int it, int c){
		if (data == null)
			return;
		if (ic<0 && it<0){
		}
		else if (ic<0) { //taxon
			if (c == -1)
				data.getTaxa().setAssociatedLong(colorNameRef, it, MesquiteLong.unassigned);
			else
				data.getTaxa().setAssociatedLong(colorNameRef, it, c);
		}
		else if (it < 0){ //character
			if (c == -1)
				data.setAssociatedLong(colorNameRef, ic, MesquiteLong.unassigned);
			else
				data.setAssociatedLong(colorNameRef, ic, c);

		}
		else if (!MesquiteLong.isCombinable(c) || c<0){
			data.setCellObject(colorNameRef, ic, it, null);
		}
		else {
			MesquiteInteger ms = new MesquiteInteger(c);
			data.setCellObject(colorNameRef, ic, it, ms);
		}
		table.redrawCell(ic,it);
	}
	private long getColor(int ic, int it){
		if (data == null)
			return 0;
		if (ic<0){  //taxon
			long c = data.getTaxa().getAssociatedLong(colorNameRef, it);
			if (MesquiteLong.isCombinable(c))
				return c;
		}
		else if (it<0){ //character
			long c = data.getAssociatedLong(colorNameRef, ic);
			if (MesquiteLong.isCombinable(c))
				return c;
		}
		else {
			Object obj = data.getCellObject(colorNameRef, ic, it);
			if (obj != null && obj instanceof MesquiteInteger)
				return ((MesquiteInteger)obj).getValue();
		}
		return MesquiteLong.unassigned;
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
		long color = getColor(ic, it);
		if (MesquiteLong.isCombinable(color))
			return ColorDistribution.getStandardColor((int)color);
		else
			return null;
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





