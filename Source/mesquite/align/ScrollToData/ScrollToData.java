/* Mesquite source code.  Copyright 1997-2010 W. Maddison and D. Maddison.
Version 2.74, October 2010.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.align.ScrollToData;

import mesquite.categ.lib.*;
import mesquite.lib.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.DataWindowAssistantI;
import mesquite.lib.table.MesquiteTable;
import mesquite.lib.table.TableTool;

public class ScrollToData extends DataWindowAssistantI {
	protected MesquiteTable table;
	protected CharacterData  data;
	protected ScrollToDataTool scrollTool;

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		getCompatibilityTest();
		if (containerOfModule() instanceof MesquiteWindow) {
			MesquiteCommand dragCommand = MesquiteModule.makeCommand("scrollDragCell",  this);
			dragCommand.setSuppressLogging(true);
			scrollTool = new ScrollToDataTool(this, "scrollerRight", getPath(), "scrollerRight.gif", 8,8,"Scrolls to next data cell","This tool scrolls to the next cell in the sequence with data.", MesquiteModule.makeCommand("scrollTouchCell",  this) , dragCommand, MesquiteModule.makeCommand("scrollDropCell",  this));
			scrollTool.setDeselectIfOutsideOfCells(false);
			scrollTool.setOptionImageFileName( "scrollerLeft.gif", 8, 8);

			((MesquiteWindow)containerOfModule()).addTool(scrollTool);
		}
		else return sorry(getName() + " couldn't start because the window with which it would be associated is not a tool container.");
		return true;
	}
	public CompatibilityTest getCompatibilityTest(){
		return new RequiresAnyMolecularData();
	}
	public boolean isPrerelease() {
		return false;
	}
	/*.................................................................................................................*/
	public int getVersionOfFirstRelease(){
		return 250;  
	}
	/*.................................................................................................................*/
	public void setTableAndData(MesquiteTable table, CharacterData data){
		if (!(data instanceof CategoricalData))
			return;
		this.table = table;
		this.data = data;
	}

	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Touched.", "[column touched] [row touched] [percent horizontal] [percent vertical] [modifiers]", commandName, "scrollTouchCell")) {
		}
		else if (checker.compare(this.getClass(), "Dragging", "[column dragged] [row dragged] [percent horizontal] [percent vertical] [modifiers]", commandName, "scrollDragCell")) {
		}
		else if (checker.compare(this.getClass(), "Dropping.", "[column dropped] [row dropped] [percent horizontal] [percent vertical] [modifiers]", commandName, "scrollDropCell")) {
			if (table!=null && data !=null){
				boolean optionDown = arguments.indexOf("option")>=0;
				MesquiteInteger io = new MesquiteInteger(0);
				int firstColumnTouched= MesquiteInteger.fromString(arguments, io);
				int firstRowTouched= MesquiteInteger.fromString(arguments, io);

				if (data.isInapplicable(firstColumnTouched, firstRowTouched)) {
					if (optionDown) {
						for (int ic = firstColumnTouched-1; ic>=0; ic--) {
							if (!data.isInapplicable(ic, firstRowTouched)) {
								table.scrollToColumn(ic);
								break;
							}
						}
					} else {
						for (int ic = firstColumnTouched+1; ic<table.numColumnsTotal; ic++) {
							if (!data.isInapplicable(ic, firstRowTouched)) {
								table.scrollToColumn(ic);
								break;
							}
						}

					}
				}
			}
		}
		else
			return super.doCommand(commandName, arguments, checker);
		return null;
	}

	public String getName() {
		return "Scroll To Data";
	}
	public String getExplanation() {
		return "Provides a tool that will scroll the data matrix to the next cell that contains data (i.e., that contains something other than a gap/inapplicable)";
	}


	public class ScrollToDataTool extends TableTool {

		public ScrollToDataTool (Object initiator, String name, String imageDirectoryPath, String imageFileName, int hotX, int hotY,  String fullDescription, String explanation, MesquiteCommand touchedCommand, MesquiteCommand dragCommand, MesquiteCommand droppedCommand) {
			super(initiator, name, imageDirectoryPath, imageFileName, hotX, hotY, fullDescription, explanation, touchedCommand, dragCommand, droppedCommand);
			this.initiator = initiator;
			this.name = name;
			setDeselectIfOutsideOfCells(true);
		}
	}

}
