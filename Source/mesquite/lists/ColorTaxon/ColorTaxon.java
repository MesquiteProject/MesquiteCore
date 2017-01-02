/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.ColorTaxon;
/*~~  */

import mesquite.lists.lib.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class ColorTaxon extends TaxaListAssistantI  {
	Taxa taxa;
	MesquiteTable table;
	TableTool tool;
	long currentColor = ColorDistribution.numberOfRed;
	String colorString = "Color Red";
	long savedColor = currentColor;
	MesquiteBoolean removeColor;
	NameReference colorNameRef = NameReference.getNameReference("color");
	public String getName() {
		return "Color Taxa";
	}
	public String getExplanation() {
		return "Provides paintbrush to color taxon names.";
	}

	/*.................................................................................................................*/
	public int getVersionOfFirstRelease(){
		return 260;  
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		MesquiteSubmenuSpec mss = addSubmenu(null, "Cell paint color", makeCommand("setColor",  this), ColorDistribution.standardColorNames);
		removeColor = new MesquiteBoolean(false);
		addCheckMenuItem(null, "Remove color", makeCommand("removeColor",  this), removeColor);
		addMenuItem(null, "Remove all color", makeCommand("removeAllColor",  this));
		MesquiteSubmenuSpec mss2 = addSubmenu(null, "Set Color of Selected", makeCommand("setColorSelected",  this), ColorDistribution.standardColorNames);
		//addMenuSeparator();
		//addMenuItem(null, "Color Selected", makeCommand("colorSelected",  this));
		setUseMenubar(false); //menu available by touching button
		return true;
	}
	private void removeColor(int it, boolean notify){
		setColor( it, MesquiteLong.unassigned);
		if (notify)
			table.redrawCell(-1,it);
	}
	private void removeAllColor(boolean notify){
			for (int it = 0; it<taxa.getNumTaxa(); it++)
				setColor(it, MesquiteLong.unassigned);
		if (notify)
			table.repaintAll();
	}
	private void setColor(int it, long c){
		if ( it<0)
			return;
		taxa.setAssociatedLong(colorNameRef, it, c);
		table.redrawCell(-1,it);
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("setColor " + ColorDistribution.getStandardColorName(ColorDistribution.getStandardColor((int)currentColor)));
		temp.addLine("removeColor " + removeColor.toOffOnString());
		return temp;
	}
	/*.................................................................................................................*/
	/** A request for the MesquiteModule to perform a command.  It is passed two strings, the name of the command and the arguments.
	This should be overridden by any module that wants to respond to a command.*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) { 
		if (checker.compare(MesquiteModule.class, null, null, commandName, "paint")) {
			MesquiteInteger io = new MesquiteInteger(0);
			int column= MesquiteInteger.fromString(arguments, io);
			int row= MesquiteInteger.fromString(arguments, io);
			if (MesquiteInteger.isCombinable(row)) {
				if (!MesquiteLong.isCombinable(currentColor))
					removeColor(row, true);
				else
					setColor(row, (int)currentColor);
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
		else	if (checker.compare(this.getClass(), "Sets the color of selected taxa", "[name of color]", commandName, "setColorSelected")) {
			int bc = ColorDistribution.standardColorNames.indexOf(parser.getFirstToken(arguments)); 
			if (bc >=0 && MesquiteLong.isCombinable(bc)){
				for (int it = 0; it<taxa.getNumTaxa(); it++)
					if (taxa.getSelected(it))
						setColor(it, bc);
			}
		}
		else if (checker.compare(this.getClass(), "Removes color from all the cells", null, commandName, "removeAllColor")) {
			removeAllColor(true);
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
	public boolean isSubstantive(){
		return false;
	}
	/*.................................................................................................................*/
	public void setTableAndTaxa(MesquiteTable table, Taxa taxa){
		this.table = table;
		this.taxa = taxa;
		if (containerOfModule() instanceof ListWindow && tool == null){
			String sortExplanation = "Assigns colors to taxa. ";
			tool = new TableTool(this, "sort", getPath(),"color.gif", 0,0,"Color taxon", sortExplanation, MesquiteModule.makeCommand("paint",  this) , null, null);
			tool.setWorksOnRowNames(true);
			((ListWindow)containerOfModule()).addTool(tool);
			tool.setPopUpOwner(this);
		}
	}

}
