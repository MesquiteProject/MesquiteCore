/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charMatrices.CharSelCoordinator; 


import mesquite.lists.lib.*;

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class CharSelCoordinator extends CharSelectCoordinator {
	CharacterData data=null;
	MesquiteTable table = null;
	boolean rows = true;
	Object dataCondition;
	MesquiteSubmenuSpec mss = null;
	MesquiteSubmenuSpec mssc = null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		addMenuItem("Select All Characters", makeCommand("selectAll",  this));
		addMenuItem("Deselect All Characters", makeCommand("deselectAll",  this));
		addMenuItem("Reverse Character Selection", makeCommand("reverse",  this));
		addMenuItem("Report Characters Selected...", makeCommand("report",  this));
		return true;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}
	/*.................................................................................................................*/
	public void employeeQuit(MesquiteModule m){
		iQuit();
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Selects all characters", null, commandName, "selectAll")) {
			if (data !=null && table!=null) {
				for (int i=0; i<data.getNumChars(); i++)
					data.setSelected(i, true);
				data.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
				table.repaintAll();
			}
		}
		else if (checker.compare(this.getClass(), "Deselects all characters", null, commandName, "deselectAll")) {
			if (data !=null && table!=null) {
				for (int i=0; i<data.getNumChars(); i++)
					data.setSelected(i, false);
				data.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
				table.repaintAll();
			}
		}
		else if (checker.compare(this.getClass(), "Reverses character selection", null, commandName, "reverse")) {
			if (data !=null && table!=null) {
				for (int i=0; i<data.getNumChars(); i++)
					data.setSelected(i, !data.getSelected(i));
				data.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
				table.repaintAll();
			}
		}
		else if (checker.compare(this.getClass(), "Reports number of characters selected", null, commandName, "report")) {
			if (data !=null && table!=null) {
				int count =0;
				for (int i=0; i<data.getNumChars(); i++)
					if (data.getSelected(i))
						count++;
				showLogWindow(true);
				logln("Number of characters selected: "+count+ "\nNumber of characters unselected: " + (data.getNumChars()-count));
			}
		}
		else if (checker.compare(this.getClass(), "Selects characters in the set", null, commandName, "selectSet")) {
			if (data !=null && table!=null) {
				int whichSet = MesquiteInteger.fromString(parser.getFirstToken(arguments));
				SpecsSetVector ssv = data.getSpecSetsVector(CharSelectionSet.class);
				if (ssv!=null) {
					CharSelectionSet chosen;
					if (!MesquiteInteger.isCombinable(whichSet))
						chosen = (CharSelectionSet)ListDialog.queryList(containerOfModule(), "Set character selection", "Select characters of which set?", MesquiteString.helpString,ssv, 0);
					else 
						chosen = (CharSelectionSet)ssv.getSpecsSet(whichSet);
					if (chosen!=null){
						for (int i=0; i<data.getNumChars(); i++)
							data.setSelected(i, chosen.isBitOn(i));
						data.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
						table.repaintAll();
						return chosen;
					}
				}
			}
		}
		else if (checker.compare(this.getClass(), "Hires character selection module to operate on the data", "[name of module]", commandName, "doSelectChars")) {
			if (table!=null && data !=null){
				CharacterSelector tda= (CharacterSelector)hireNamedEmployee(CharacterSelector.class, arguments);
				if (tda!=null) {
					tda.selectCharacters(data);
					table.repaintAll();
					if (!tda.pleaseLeaveMeOn())
						fireEmployee(tda);
				}
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public void setTableAndObject(MesquiteTable table, Object obj, boolean rows){
		if (obj==null || !(obj instanceof CharacterData))
			return;
		this.data = (CharacterData)obj;
		data.addListener(this);  // for Character Set submenu
		this.table = table;
		this.rows = rows;
		resetMenus();
	}
	/*.................................................................................................................*/
	public void resetMenus(){
		if (mss!=null)
			deleteMenuItem(mss);
		mss = addSubmenu(null, "Character Set", makeCommand("selectSet",  this), data.getSpecSetsVector(CharSelectionSet.class));
		if (mssc!=null)
			deleteMenuItem(mssc);
		mssc = addSubmenu(null, "Select Characters", MesquiteModule.makeCommand("doSelectChars",  this));
		mssc.setList(CharacterSelector.class);
		mssc.setCompatibilityCheck(data.getStateClass());
	}
	/* ................................................................................................................. */
	/** passes which object changed, along with optional integer (e.g. for character) (from MesquiteListener interface) */
	public void changed(Object caller, Object obj, Notification notification) {
		int code = Notification.getCode(notification);
		int[] parameters = Notification.getParameters(notification);
		if (obj instanceof CharacterData && (CharacterData) obj == data) {
			if (code == AssociableWithSpecs.SPECSSET_CHANGED) {
				resetMenus();   
				resetContainingMenuBar();
				parametersChanged();
			}
		}
		super.changed(caller, obj, notification);
	}


	/*.................................................................................................................*/
	public String getName() {
		return "Character selection coordinator";
	}

	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Coordinates character selection." ;
	}
}

