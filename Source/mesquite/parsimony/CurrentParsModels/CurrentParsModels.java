/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.parsimony.CurrentParsModels;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.parsimony.lib.*;

/* ======================================================================== */
/* Returns parsimony model currently assigned to a character. If none assigned, returns the default model. */
public class CurrentParsModels extends ParsModelSource implements MesquiteListener {
	public String getName() {
		return "Current Parsimony Models";
	}
	public String getExplanation() {
		return "Supplies the currently assigned parsimony model (e.g., \"unordered\") for a character." ;
	}
	/*.................................................................................................................*/
	ModelSet currentModelSet;
	String preferred = "Parsimony";
	Class defaultStateClass;
	CharacterData currentData = null;
	int currentChar;
	boolean reassignable = false;
	CharacterModel lastModel;
	MesquiteSubmenuSpec smenu;
	MesquiteMenuItemSpec aboutItem;
	Class oldStateClass;
	CharacterModel defaultModel;
	MesquiteString modelName;
	LongArray ids;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		getProject().getCentralModelListener().addListener(this);
		ids = new LongArray(20);
		modelName = new MesquiteString();
		return true;
	}

	/*.................................................................................................................*/
	public void endJob() {
		getProject().getCentralModelListener().removeListener(this);
		super.endJob();
	}
	/*.................................................................................................................*/
	public void changed(Object caller, Object obj, Notification notification){
		if (Notification.appearsCosmetic(notification))
			return;
		int code = Notification.getCode(notification);
		if (obj instanceof ParsimonyModel && code != MesquiteListener.ANNOTATION_CHANGED && code != MesquiteListener.ANNOTATION_DELETED && code != MesquiteListener.ANNOTATION_ADDED) {
			if (!reassignable || obj == lastModel || (ids.indexOf(((ParsimonyModel)obj).getID())>=0)) {
				outputInvalid();
				parametersChanged(notification);
			}
		}
	}
	/*.................................................................................................................*/
	public void disposing(Object obj){
		if (obj instanceof ParsimonyModel) {
			if (!reassignable || obj == lastModel) {
				outputInvalid();
				parametersChanged();
			}
		}
	}
	/*.................................................................................................................*/
	/** returns model for character ic in data */
	public CharacterModel getCharacterModel(CharacterData data, int ic) {

		currentData = data;
		currentChar = ic;
		if (data==null) {
			MesquiteMessage.warnProgrammer("DATA NULL in Current Parsimony Models;  getModelCurrentCharacter (a)");
			lastModel = null;
			modelName.setValue("");
			if (reassignable)
				MesquiteTrunk.resetCheckMenuItems();
			return null;
		}
		else {

			if (data.getStateClass()!=oldStateClass)
				resetCurrent(data.getStateClass());
			CharacterModel cm=null;
			currentModelSet = (ModelSet)data.getCurrentSpecsSet(ParsimonyModelSet.class);
			if (currentModelSet == null) {
				cm= data.getDefaultModel(preferred);
				if (cm==null)
					MesquiteMessage.warnProgrammer("MODEL NULL in Current Parsimony Models; getModelCurrentCharacter (b)");
			}
			else {
				cm = currentModelSet.getModel(ic);
				if (cm==null)
					MesquiteMessage.warnProgrammer("MODEL NULL in Current Parsimony Models; getModelCurrentCharacter (c)");
			}
			if (reassignable && lastModel != cm)
				MesquiteTrunk.resetCheckMenuItems();
			lastModel = cm;
			modelName.setValue(lastModel.getName());
			if (ids.indexOf(cm.getID())<0)
				if (!ids.fillNextUnassigned(cm.getID())){
					ids.resetSize(ids.getSize()+10);
					ids.fillNextUnassigned(cm.getID());
				}
			return cm;
		}
	}

	/** returns model for character */
	public CharacterModel getCharacterModel(CharacterStatesHolder states){
		if (states==null) {
			MesquiteMessage.warnProgrammer("STATES NULL in Current Parsimony Models; getModelCurrentCharacter (d)");
			lastModel = null;
			modelName.setValue("");
			if (reassignable)
				MesquiteTrunk.resetCheckMenuItems();
			return null;
		}
		else {
			if (states.getStateClass()!=oldStateClass)
				resetCurrent(states.getStateClass());

			CharacterModel cm=null;
			CharacterData data = states.getParentData();
			currentData = data;
			if (data == null) {
				if (defaultModel == null || !defaultModel.getStateClass().isAssignableFrom(states.getStateClass()))
					defaultModel = states.getDefaultModel(getProject(), preferred);
				cm = defaultModel;

				if (cm==null)
					MesquiteMessage.warnProgrammer("MODEL NULL in Current Parsimony Models; getModelCurrentCharacter (e)");
			}
			else {
				int ic =  states.getParentCharacter(); 
				currentChar = ic;
				currentModelSet = (ModelSet)data.getCurrentSpecsSet(ParsimonyModelSet.class);
				if (currentModelSet == null) {
					if (states!=null) {
						cm= states.getDefaultModel(getProject(), preferred);
						if (cm==null)
							MesquiteMessage.warnProgrammer("MODEL NULL in Current Parsimony Models; getModelCurrentCharacter (f)");
					}
					else
						MesquiteMessage.warnProgrammer("MODEL NULL in Current Parsimony Models; getModelCurrentCharacter (g)");
				}
				else {
					cm = currentModelSet.getModel(ic);
					if (cm==null && states!=null)
						cm= states.getDefaultModel(getProject(), preferred);
					if (cm==null) {
						MesquiteMessage.warnProgrammer("MODEL NULL in Current Parsimony Models; getModelCurrentCharacter (h) " + ic);
						MesquiteMessage.warnProgrammer("data " + data.getName() + "  " + data);
					}
				}
			}
			if (reassignable && lastModel != cm)
				MesquiteTrunk.resetCheckMenuItems();
			lastModel = cm;
			if (lastModel!=null)
				modelName.setValue(lastModel.getName());
			if (cm == null)
				return null;
			if (ids.indexOf(cm.getID())<0)
				if (!ids.fillNextUnassigned(cm.getID())){
					ids.resetSize(ids.getSize()+10);
					ids.fillNextUnassigned(cm.getID());
				}
			return cm;
		}
	}
	void resetCurrent(Class stateClass){
		oldStateClass = stateClass;
		if (smenu!=null) {
			deleteMenuItem(smenu);
			smenu=null;
		}   		
		if (reassignable) {
			smenu = addSubmenu(null, "Parsimony model", makeCommand("setModel", this), getProject().getCharacterModels());
			smenu.setCompatibilityCheck(new ModelCompatibilityInfo(ParsimonyModel.class, stateClass));
			smenu.setSelected(modelName);
		}
		resetContainingMenuBar();
		MesquiteTrunk.resetCheckMenuItems();
	}
	/*.................................................................................................................*/
	/* If this is called passing true, module will assume that there will be an implicit "current character" (e.g., in trace character) and thus will allow user
	to set model of this current character.  The "current" will be assumed the last character for which model requested. */
	public void setOneCharacterAtATime(boolean chgbl){
		if (!chgbl)
			deleteMenuItem(aboutItem);
		else if (aboutItem == null)
			aboutItem = addMenuItem("About the Model (for " + getEmployer().getName() + ")...", makeCommand("aboutModel", this));

		reassignable = chgbl;
		Class stateClass = null;
		if (currentData!=null)
			stateClass = currentData.getStateClass();
		resetCurrent(stateClass);
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		if (defaultModel !=null)
			temp.addLine("setDefaultModel "+ getProject().getWhichCharacterModel(null, defaultModel));
		return temp;
	}
	/*.................................................................................................................*
    	 public String getParameters() {
		return "CurrentParsModels timer " + timer.getAccumulatedTime() + " timer2 " + timer2.getAccumulatedTime() + " timer3 " + timer3.getAccumulatedTime() + " timer4 " + timer4.getAccumulatedTime() + " timer5 " + timer5.getAccumulatedTime();
   	 }
	/*.................................................................................................................*/
	MesquiteInteger pos = new MesquiteInteger();
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the parsimony character model, subject to compatibility with current state class", "[number of model]", commandName, "setModel")) {
			if (currentData == null){
				boolean changed=false;
				int whichModel = MesquiteInteger.fromFirstToken(arguments, pos);
				if (!MesquiteInteger.isCombinable(whichModel))
					return null;
				defaultModel = getProject().getCharacterModel(new ModelCompatibilityInfo(ParsimonyModel.class, oldStateClass), whichModel);
				parametersChanged();
				return defaultModel;
			}
			else {
				boolean changed=false;
				int whichModel = MesquiteInteger.fromFirstToken(arguments, pos);
				if (!MesquiteInteger.isCombinable(whichModel))
					return null;
				CharacterModel model = getProject().getCharacterModel(new ModelCompatibilityInfo(ParsimonyModel.class, currentData.getStateClass()), whichModel);

				ModelSet modelSet  = (ModelSet)currentData.getCurrentSpecsSet(ParsimonyModelSet.class);
				if (model!=null){
					if (modelSet == null) {
						CharacterModel defaultModel =  currentData.getDefaultModel("Parsimony");
						modelSet= new ParsimonyModelSet("Parsimony Model Set", currentData.getNumChars(), defaultModel, currentData);
						currentData.storeSpecsSet(modelSet, ParsimonyModelSet.class);
						currentData.setCurrentSpecsSet(modelSet, ParsimonyModelSet.class);
						modelSet.addToFile(currentData.getFile(), getProject(), findElementManager(ParsimonyModelSet.class)); 
					}

					if (modelSet != null) {
						modelSet.setModel(model, currentChar); 
						currentData.notifyListeners(this, new Notification(AssociableWithSpecs.SPECSSET_CHANGED));  //should notify via specs not data???
					}
					parametersChanged();
				}
				return model;
			}
		}
		else if (checker.compare(this.getClass(), "Sets the parsimony character model", "[number of model]", commandName, "setDefaultModel")) {
			boolean changed=false;
			int whichModel = MesquiteInteger.fromFirstToken(arguments, pos);
			if (!MesquiteInteger.isCombinable(whichModel))
				return null;
			defaultModel = getProject().getCharacterModel(whichModel);
			parametersChanged();
			return defaultModel;
		}
		else if (checker.compare(this.getClass(), "Displays a dialog about the last model returned", null, commandName, "aboutModel")) {
			String s = "";
			if (lastModel == null)
				s = "Sorry, no reference to the last used-model was found";
			else
				s = "The most recently used model is \"" + lastModel.getName() + "\".\nExplanation: " + lastModel.getExplanation();
			discreetAlert( s);
			return null;
		}
		else
			return  super.doCommand(commandName, arguments, checker);
	}

	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return false;
	}

}

