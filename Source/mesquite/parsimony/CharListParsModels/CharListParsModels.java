/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.parsimony.CharListParsModels;
/*~~  */

import mesquite.lists.lib.*;
import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.parsimony.lib.*;

/* ======================================================================== */
public class CharListParsModels extends CharListAssistant {
	public String getName() {
		return "Current Parsimony Models ";
	}
	public String getExplanation() {
		return "Shows current parsimony models applied to characters in character list window." ;
	}
	/*.................................................................................................................*/
	CharacterData data=null;
	MesquiteTable table=null;
	MesquiteSubmenuSpec mss;
	MesquiteMenuItemSpec mScs, mStc, mRssc, mLine;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		getProject().getCentralModelListener().addListener(this);
		return true;
	}
	/*.................................................................................................................*/
	public void endJob() {
		getProject().getCentralModelListener().removeListener(this);
		super.endJob();
	}
	/*.................................................................................................................*/
	public void disposing(Object obj){
		if (obj instanceof ParsimonyModel) {
			parametersChanged();
		}
	}
	MesquiteInteger pos = new MesquiteInteger(0);
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the parsimony model of the selected characters", "[number of model]", commandName, "setModel")) {
			if (table !=null && data!=null) {
				boolean changed=false;
				int whichModel = MesquiteInteger.fromFirstToken(arguments, pos);
				if (!MesquiteInteger.isCombinable(whichModel))
					return null;
				CharacterModel model = getProject().getCharacterModel(new ModelCompatibilityInfo(ParsimonyModel.class, data.getStateClass()), whichModel);
				if (model != null) {
					ModelSet modelSet = (ModelSet) data.getCurrentSpecsSet(ParsimonyModelSet.class);
					if (modelSet == null) {
						CharacterModel defaultModel =  data.getDefaultModel("Parsimony");
						modelSet= new ParsimonyModelSet("Parsimony Model Set", data.getNumChars(), defaultModel, data);
						modelSet.addToFile(data.getFile(), getProject(), findElementManager(ParsimonyModelSet.class)); 
						data.setCurrentSpecsSet(modelSet, ParsimonyModelSet.class);
					}
					if (modelSet != null) {
						if (employer!=null && employer instanceof ListModule) {
							int c = ((ListModule)employer).getMyColumn(this);
							for (int i=0; i<data.getNumChars(); i++) {
								if (table.isCellSelectedAnyWay(c, i)) {
									modelSet.setModel(model, i);
									if (!changed)
										outputInvalid();

									changed = true;
								}
							}
						}
					}
				}

				if (changed)
					data.notifyListeners(this, new Notification(AssociableWithSpecs.SPECSSET_CHANGED));  
				//not quite kosher; HOW TO HAVE MODEL SET LISTENERS??? -- modelSource
				parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Stores current parsimony model set (TYPESET)", null, commandName, "storeCurrent")) {
			if (data!=null){
				SpecsSetVector ssv = data.getSpecSetsVector(ParsimonyModelSet.class);
				if (ssv == null || ssv.getCurrentSpecsSet() == null) {
					CharacterModel defaultModel =  data.getDefaultModel("Parsimony");
					ModelSet modelSet= new ParsimonyModelSet("Parsimony Model Set", data.getNumChars(), defaultModel, data);
					modelSet.addToFile(data.getFile(), getProject(), findElementManager(ParsimonyModelSet.class)); 
					data.setCurrentSpecsSet(modelSet, ParsimonyModelSet.class);
					ssv = data.getSpecSetsVector(ParsimonyModelSet.class);
				}
				if (ssv!=null) {
					SpecsSet s = ssv.storeCurrentSpecsSet();
					if (s.getFile() == null)
						s.addToFile(data.getFile(), getProject(), findElementManager(ParsimonyModelSet.class));
					s.setName(ssv.getUniqueName("Parsimony Model Set"));
					String name = MesquiteString.queryString(containerOfModule(), "Name", "Name of parsimony model set to be stored", "Parsimony Model Set");
					if (!StringUtil.blank(name))
						s.setName(name);
					ssv.notifyListeners(this, new Notification(MesquiteListener.NAMES_CHANGED));  
				}
				else MesquiteMessage.warnProgrammer("sorry, can't store because no specssetvector");
			}
		}
		else if (checker.compare(this.getClass(), "Replace stored parsimony model set (TYPESET) by the current one", null, commandName, "replaceWithCurrent")) {
			if (data!=null){
				SpecsSetVector ssv = data.getSpecSetsVector(ParsimonyModelSet.class);
				if (ssv!=null) {
					SpecsSet chosen = (SpecsSet)ListDialog.queryList(containerOfModule(), "Replace stored set", "Choose stored parsimony model set to replace by current set", MesquiteString.helpString, ssv, 0);
					if (chosen!=null){
						SpecsSet current = ssv.getCurrentSpecsSet();
						ssv.replaceStoredSpecsSet(chosen, current);
					}
				}

			}
		}
		else if (checker.compare(this.getClass(), "Loads the stored parsimony model set to be the current one", "[number of parsimony set to load]", commandName, "loadToCurrent")) {
			if (data !=null) {
				int which = MesquiteInteger.fromFirstToken(arguments, stringPos);
				if (MesquiteInteger.isCombinable(which)){
					SpecsSetVector ssv = data.getSpecSetsVector(ParsimonyModelSet.class);
					if (ssv!=null) {
						SpecsSet chosen = ssv.getSpecsSet(which);
						if (chosen!=null){
							ssv.setCurrentSpecsSet(chosen.cloneSpecsSet()); 
							data.notifyListeners(this, new Notification(AssociableWithSpecs.SPECSSET_CHANGED)); //should notify via specs not data???
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
		/* hire employees here */
		deleteMenuItem(mss);
		deleteMenuItem(mScs);
		deleteMenuItem(mRssc);
		deleteMenuItem(mLine);
		deleteMenuItem(mStc);
		mss = addSubmenu(null, "Parsimony model", makeCommand("setModel", this), getProject().getCharacterModels());
		mLine = addMenuSeparator();
		mScs = addMenuItem("Store current set...", makeCommand("storeCurrent",  this));
		mRssc = addMenuItem("Replace stored set by current...", makeCommand("replaceWithCurrent",  this));
		mss.setCompatibilityCheck(new ModelCompatibilityInfo(ParsimonyModel.class, data.getStateClass()));
		if (data !=null)
			mStc = addSubmenu(null, "Load parsimony model set", makeCommand("loadToCurrent",  this), data.getSpecSetsVector(ParsimonyModelSet.class));
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
		return "Parsimony Model";
	}
	public String getStringForCharacter(int ic){
		if (data!=null) {
			ModelSet modelSet = (ModelSet)data.getCurrentSpecsSet(ParsimonyModelSet.class);
			if (modelSet != null) {
				CharacterModel model = modelSet.getModel(ic);
				if (model!=null) {
					return model.getName();
				}
			}
			else
				MesquiteMessage.warnProgrammer("model set null in gsfc");
		}
		return "?";
	}
	public String getWidestString(){
		return "Parsimony Model  ";
	}
	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return false;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}

}

