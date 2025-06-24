/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charMatrices.QuickKeySelector; //DRM started April 02

import java.awt.event.KeyEvent;

import mesquite.categ.lib.CategoricalData;
import mesquite.categ.lib.RequiresAnyCategoricalData;
import mesquite.lib.CommandChecker;
import mesquite.lib.CompatibilityTest;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteCommand;
import mesquite.lib.MesquiteEvent;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteListener;
import mesquite.lib.MesquiteModule;
import mesquite.lib.Notification;
import mesquite.lib.Snapshot;
import mesquite.lib.StringUtil;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.DataWindowAssistantI;
import mesquite.lib.table.MesquiteTable;
import mesquite.lib.table.TableTool;
import mesquite.lib.ui.MesquiteTool;
import mesquite.lib.ui.MesquiteWindow;
import mesquite.lib.ui.ToolKeyListener;

/* ======================================================================== */
public class QuickKeySelector extends DataWindowAssistantI implements ToolKeyListener {
	TableTool quickKeySelectorTool;
	CategoricalData data;
	MesquiteTable table;
	MesquiteBoolean autotabOff, autotabDown, autotabRight, autotabLeft;
	MesquiteCommand keyCommand;
	public String getFunctionIconPath(){
		return getPath() + "quickKeySelector.gif";
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		if (containerOfModule() instanceof MesquiteWindow) {
			quickKeySelectorTool = new TableTool(this, "quickKeySelector", getPath(), "quickKeySelector.gif", 4,4,"Select and type", "This tool allows you to select cells; any keystrokes will then be entered into selected cells.", null, null, null);  //MesquiteModule.makeCommand("quickKeySelectorTouch",  this) 
			quickKeySelectorTool.setToolKeyListener(this);
			quickKeySelectorTool.setUseTableTouchRules(true);
			//quickKeySelectorTool.setAllowAnnotate(true);
			((MesquiteWindow)containerOfModule()).addTool(quickKeySelectorTool);
			autotabOff = new MesquiteBoolean(true);
			autotabDown = new MesquiteBoolean(false);
			autotabRight = new MesquiteBoolean(false);
			autotabLeft = new MesquiteBoolean(false);
			keyCommand = new MesquiteCommand("enter", this);
			addCheckMenuItem(null, "Autotab Off", MesquiteModule.makeCommand("autotabOff", this), autotabOff);
			addCheckMenuItem(null, "Autotab Down", MesquiteModule.makeCommand("autotabDown", this), autotabDown);
			addCheckMenuItem(null, "Autotab Right", MesquiteModule.makeCommand("autotabRight", this), autotabRight);
			addCheckMenuItem(null, "Autotab Left", MesquiteModule.makeCommand("autotabLeft", this), autotabLeft);
			quickKeySelectorTool.setPopUpOwner(this);
			setUseMenubar(false); //menu available by touching oning button
		}
		else return false;
		return true;
	}
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		if (autotabDown.getValue())
			temp.addLine("autotabDown");
		else if (autotabRight.getValue())
			temp.addLine("autotabRight");
		else if (autotabLeft.getValue())
			temp.addLine("autotabLeft");
		else
			temp.addLine("autotabOff");
		return temp;
	}
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "turns off autotab", null, commandName, "autotabOff")) {
			autotabOff.setValue(true);
			autotabDown.setValue(false);
			autotabRight.setValue(false);
			autotabLeft.setValue(false);
			quickKeySelectorTool.setImageFileName( "quickKeySelector.gif");
			MesquiteWindow w = (MesquiteWindow)containerOfModule();
			if (w != null && w.getCurrentTool() == quickKeySelectorTool)
				w.resetCursor();
		}
		else if (checker.compare(this.getClass(), "turns on autotab down", null, commandName, "autotabDown")) {
			autotabOff.setValue(false);
			autotabDown.setValue(true);
			autotabRight.setValue(false);
			autotabLeft.setValue(false);
			quickKeySelectorTool.setImageFileName( "quickKeyDown.gif");
			MesquiteWindow w = (MesquiteWindow)containerOfModule();
			if (w != null && w.getCurrentTool() == quickKeySelectorTool)
				w.resetCursor();
		}
		else if (checker.compare(this.getClass(), "turns on autotab right", null, commandName, "autotabRight")) {
			autotabOff.setValue(false);
			autotabDown.setValue(false);
			autotabRight.setValue(true);
			autotabLeft.setValue(false);
			quickKeySelectorTool.setImageFileName( "quickKeyRight.gif");
			MesquiteWindow w = (MesquiteWindow)containerOfModule();
			if (w != null && w.getCurrentTool() == quickKeySelectorTool)
				w.resetCursor();
		}
		else if (checker.compare(this.getClass(), "turns on autotab left", null, commandName, "autotabLeft")) {
			autotabOff.setValue(false);
			autotabDown.setValue(false);
			autotabRight.setValue(false);
			autotabLeft.setValue(true);
			quickKeySelectorTool.setImageFileName( "quickKeyLeft.gif");
			MesquiteWindow w = (MesquiteWindow)containerOfModule();
			if (w != null && w.getCurrentTool() == quickKeySelectorTool)
				w.resetCursor();
		}
		else if (checker.compare(this.getClass(), "Enters a keyed value", null, commandName, "enter")) {
			String value = parser.getFirstToken(arguments);
			if (!StringUtil.blank(value))
				enterValue(value);
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	/** Returns CompatibilityTest so other modules know if this is compatible with some object. */
	public CompatibilityTest getCompatibilityTest(){
		return new RequiresAnyCategoricalData();
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return true;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	public void setTableAndData(MesquiteTable table, CharacterData data){
		this.table = table;
		if (data instanceof CategoricalData)
			this.data = (CategoricalData)data;
		else
			this.data = null;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Quick Key Entry";
	}
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Provides a tool with which to quickly enter data.  If this tool is active, then typing a key will cause that value to be entered into all selected cells.";
	}
	/*.................................................................................................................*/
	public void keyTyped(KeyEvent e, MesquiteTool tool){
	}
	/*.................................................................................................................*/
	public void keyPressed(KeyEvent e, MesquiteTool tool){
	}
	private void enterValue(String s){
		if (s == null || s.length()==0)
			return;
		char c = s.charAt(0);
		if (!data.isAcceptableCharForState(c))
			return;
		boolean success = false;
		int count=0;
		int row=0;
		int column = 0;
		boolean applicableBefore = false;
		boolean applicabilitySame = false;
		if (table.anyCellSelected()) {
			for (int i=0; i<table.getNumColumns(); i++)
				for (int j=0; j<table.getNumRows(); j++)
					if (table.isCellSelected(i,j)) {
						applicableBefore = data.isInapplicable(i, j);
						data.setState(i,j, c);
						applicabilitySame = applicableBefore == data.isInapplicable(i, j);
						count++;
						row = j;
						column = i;
					}
			success = true;
		}
		if (table.anyRowSelected()) {
			for (int j=0; j<table.getNumRows(); j++) {
				if (table.isRowSelected(j))
					for (int i=0; i<table.getNumColumns(); i++) {
						applicableBefore = data.isInapplicable(i, j);
						data.setState(i,j, c);
						applicabilitySame = applicableBefore == data.isInapplicable(i, j);
						count++;
						row = j;
						column = i;
					}
			}
			success = true;
		}
		if (table.anyColumnSelected()) {
			for (int i=0; i<table.getNumColumns(); i++){
				if (table.isColumnSelected(i))
					for (int j=0; j<table.getNumRows(); j++) {
						applicableBefore = data.isInapplicable(i, j);
						data.setState(i,j, c);
						applicabilitySame = applicableBefore == data.isInapplicable(i, j);
						count++;
						row = j;
						column = i;
					}
			}
			success = true;
		}
		if (success){
			table.repaintAll();
			if (count==1) {
				int[] subcodes = new int[] {MesquiteListener.SINGLE_CELL};
				if (applicabilitySame)
					subcodes = new int[] {MesquiteListener.SINGLE_CELL, MesquiteListener.CELL_SUBSTITUTION};
				Notification notification = new Notification(MesquiteListener.DATA_CHANGED, new int[] {column, row}, null);
				notification.setSubcodes(subcodes);
				data.notifyListeners(this, notification);
			}
			else
				data.notifyListeners(this, new Notification(MesquiteListener.DATA_CHANGED));
		}
		if (autotabDown.getValue() || autotabRight.getValue() || autotabLeft.getValue()){
			if (table.singleTableCellSelected()){
				if (autotabDown.getValue())
					table.downArrowPressed(null);
				else if (autotabRight.getValue())
					table.rightArrowPressed(null);
				else if (autotabLeft.getValue())
					table.leftArrowPressed(null);
			}
		}
	}
	/*.................................................................................................................*/
	public void keyReleased(KeyEvent e, MesquiteTool tool){
		if (data == null)
			return;
		if (data.isEditInhibited()){
			discreetAlert("This matrix is marked as locked against editing. To unlock, uncheck the menu item Matrix>Current Matrix>Editing Not Permitted");
			return;
		}
		if (MesquiteEvent.commandOrControlKeyDown(MesquiteEvent.getModifiers(e)))
			return;
		if (!data.isAcceptableCharForState(e.getKeyChar()))
			return;
		keyCommand.doItMainThread("" + e.getKeyChar(), null, this); //29Mar08 put onto main thread as is standard, i.e. to avoid reentrancy issues & avoid its being treated as scripting
	}

}





