/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.CharListWeights;
/*~~  */

import mesquite.lists.lib.*;
import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class CharListWeights extends CharListAssistant {
	/*.................................................................................................................*/
	public String getName() {
		return "Current Weights";
	}
	public String getExplanation() {
		return "Supplies current weights applied to characters for character list window." ;
	}

	CharacterData data=null;
	MesquiteTable table=null;
	MesquiteSubmenuSpec mss;
	MesquiteMenuItemSpec mScs, mStc, mRssc, mLine, mwt;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	MesquiteInteger pos = new MesquiteInteger(0);
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the weight of the selected characters", "[weight]", commandName, "setWeight")) {
			if (table !=null && data!=null) {
				boolean changed=false;
				MesquiteNumber num = new MesquiteNumber();
				num.setValue(arguments);
				if (!num.isCombinable()) {
					num.setValue(1);
					num.setValue(MesquiteNumber.queryNumber(containerOfModule(), "Set Weight", "Set weight of selected characters", num));
				}
				if (!num.isCombinable())
					return null;

				CharWeightSet weightSet = (CharWeightSet) data.getCurrentSpecsSet(CharWeightSet.class);
				if (weightSet == null) {
					weightSet= new CharWeightSet("Weight Set", data.getNumChars(), data);
					weightSet.addToFile(data.getFile(), getProject(), findElementManager(CharWeightSet.class)); //THIS
					data.setCurrentSpecsSet(weightSet, CharWeightSet.class);
				}
				if (weightSet != null) {
					if (employer!=null && employer instanceof ListModule) {
						int c = ((ListModule)employer).getMyColumn(this);
						for (int i=0; i<data.getNumChars(); i++) {
							if (table.isCellSelectedAnyWay(c, i)) {
								weightSet.setValue(i, num);
								if (!changed)
									outputInvalid();

								changed = true;
							}
						}
					}
				}


				if (changed)
					data.notifyListeners(this, new Notification(AssociableWithSpecs.SPECSSET_CHANGED));  
				parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Stores current weight set (WTSET)", null, commandName, "storeCurrent")) {
			if (data!=null){
				SpecsSetVector ssv = data.getSpecSetsVector(CharWeightSet.class);
				if (ssv == null || ssv.getCurrentSpecsSet() == null) {
					CharWeightSet weightSet= new CharWeightSet("Weight Set", data.getNumChars(), data);
					weightSet.addToFile(data.getFile(), getProject(), findElementManager(CharWeightSet.class)); //THIS
					data.setCurrentSpecsSet(weightSet, CharWeightSet.class);
					ssv = data.getSpecSetsVector(CharWeightSet.class);
				}
				if (ssv!=null) {
					SpecsSet s = ssv.storeCurrentSpecsSet();
					if (s.getFile() == null)
						s.addToFile(data.getFile(), getProject(), findElementManager(CharWeightSet.class));
					s.setName(ssv.getUniqueName("Weight Set"));
					String name = MesquiteString.queryString(containerOfModule(), "Name", "Name of weight set to be stored", s.getName());
					if (!StringUtil.blank(name))
						s.setName(name);
					ssv.notifyListeners(this, new Notification(MesquiteListener.NAMES_CHANGED));  
				}
				else MesquiteMessage.warnProgrammer("sorry, can't store because no specssetvector");
			}
		}
		else if (checker.compare(this.getClass(), "Replace stored weight set (WTSET) by the current one", null, commandName, "replaceWithCurrent")) {
			if (data!=null){
				SpecsSetVector ssv = data.getSpecSetsVector(CharWeightSet.class);
				if (ssv!=null) {
					SpecsSet chosen = (SpecsSet)ListDialog.queryList(containerOfModule(), "Replace stored set", "Choose stored weight set to replace by current set", MesquiteString.helpString,ssv, 0);
					if (chosen!=null){
						SpecsSet current = ssv.getCurrentSpecsSet();
						ssv.replaceStoredSpecsSet(chosen, current);
					}
				}

			}
		}
		else if (checker.compare(this.getClass(), "Loads the stored weight set to be the current one", "[number of weight set to load]", commandName, "loadToCurrent")) {
			if (data !=null) {
				int which = MesquiteInteger.fromFirstToken(arguments, stringPos);
				if (MesquiteInteger.isCombinable(which)){
					SpecsSetVector ssv = data.getSpecSetsVector(CharWeightSet.class);
					if (ssv!=null) {
						SpecsSet chosen = ssv.getSpecsSet(which);
						if (chosen!=null){
							ssv.setCurrentSpecsSet(chosen.cloneSpecsSet()); 
							data.notifyListeners(this, new Notification(AssociableWithSpecs.SPECSSET_CHANGED)); //TODO: bogus! should notify via specs not data???
							return chosen;
						}
					}
				}
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/*.................................................................................................................*/
	public void setTableAndData(MesquiteTable table, CharacterData data){
		deleteMenuItem(mss);
		deleteMenuItem(mScs);
		deleteMenuItem(mRssc);
		deleteMenuItem(mLine);
		deleteMenuItem(mStc);
		deleteMenuItem(mwt);
		mwt = addMenuItem("Set Weight...", makeCommand("setWeight", this));
		mLine = addMenuSeparator();
		mScs = addMenuItem("Store current set", makeCommand("storeCurrent",  this));
		mRssc = addMenuItem("Replace stored set by current", makeCommand("replaceWithCurrent",  this));
		if (data !=null)
			mStc = addSubmenu(null, "Load weight set", makeCommand("loadToCurrent",  this), data.getSpecSetsVector(CharWeightSet.class));
		this.data = data;
		this.table = table;
	}
	public void changed(Object caller, Object obj, Notification notification){
		if (Notification.appearsCosmetic(notification))
			return;
		outputInvalid();
		parametersChanged(notification);
	}
	public String getTitle() {
		return "Weights";
	}
	public String getStringForCharacter(int ic){
		if (data!=null) {
			CharWeightSet weightSet = (CharWeightSet)data.getCurrentSpecsSet(CharWeightSet.class);
			if (weightSet != null) {
				return weightSet.toString(ic);
			}
			else {
				return "1";
			}
		}
		return "?";
	}
	public String getWidestString(){
		return "888888888  ";
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}

	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;  
	}
}

