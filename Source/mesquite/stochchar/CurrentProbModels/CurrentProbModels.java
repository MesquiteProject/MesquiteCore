/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.stochchar.CurrentProbModels;

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.stochchar.lib.*;

/* ======================================================================== */
public class CurrentProbModels extends ProbModelSourceLike {
	ModelSet currentModelSet;
	String preferred =  "Likelihood";
	boolean reassignable = false;
	MesquiteSubmenuSpec smenu;
	MesquiteMenuItemSpec aboutItem;
	CharacterData currentData = null;
	int currentChar;
	Class oldStateClass;
   	CharacterModel defaultModel, lastModel;
   	MesquiteString modelName;
   	LongArray ids;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		modelName = new MesquiteString();
		ids = new LongArray(20);
		getProject().getCentralModelListener().addListener(this);
		return true;
  	 }
	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return false;
  	 }
  	 public boolean modelFromModelSet() {
 	 	return true;    	 
 }

	/*.................................................................................................................*/
	public void endJob() {
		getProject().getCentralModelListener().removeListener(this);
		super.endJob();
  	 }
	/*.................................................................................................................*/
	public void changed(Object caller, Object obj, Notification notification){
		int code = Notification.getCode(notification);
		if (obj instanceof ProbabilityModel && code != MesquiteListener.ANNOTATION_CHANGED && code != MesquiteListener.ANNOTATION_DELETED && code != MesquiteListener.ANNOTATION_ADDED && code != MesquiteListener.NAMES_CHANGED) {
			if (!reassignable || obj == lastModel || (ids.indexOf(((ProbabilityModel)obj).getID())>=0)) {
				parametersChanged(notification);
			}
		}
		else if (obj instanceof Class && ProbabilityModel.class.isAssignableFrom((Class)obj)) {
				parametersChanged(notification);
		}
	}
	/*.................................................................................................................*/
	public void disposing(Object obj){
		if (obj instanceof ProbabilityModel) {
			if (!reassignable || obj == lastModel) {
				outputInvalid();
				parametersChanged();
			}
		}
	}
  	 
	/*.................................................................................................................*/
    	 public void chooseAndFillUnassignedCharacters(CharacterData data) {
 			Listable[] models = getProject().getCharacterModels(new ModelCompatibilityInfo(ProbabilityModel.class, data.getStateClass()), data.getStateClass()); //TODO: would be better if passed name of model
 			CharacterModel model = (CharacterModel)ListDialog.queryList(containerOfModule(), "Choose model", "Select model to assigned to all characters without an assigned model", MesquiteString.helpString, models, 0);
 			boolean ch = false;
 			if (model != null) {
				ModelSet modelSet = (ModelSet)data.getCurrentSpecsSet(ProbabilityModelSet.class);
	 			if (modelSet == null) {
			 		CharacterModel defaultModel =  data.getDefaultModel("Likelihood");
			 		modelSet= new ProbabilityModelSet("Probability Model Set", data.getNumChars(), defaultModel, data);
			 		modelSet.addToFile(data.getFile(), getProject(), findElementManager(ProbabilityModelSet.class)); 
					data.setCurrentSpecsSet(modelSet, ProbabilityModelSet.class);
	 			}
	 			if (modelSet != null) {
    	 				for (int i=0; i<data.getNumChars(); i++) {
    	 					if (modelSet.getModel(i) == null)  {
    	 						modelSet.setModel(model, i);
    	 						ch = true;
    	 					}
	 				}
	 			}
 			}
 			if (ch)
 				data.notifyListeners(this, new Notification(AssociableWithSpecs.SPECSSET_CHANGED));
 		
   	 }
   	/** returns model for character ic in data */
   	public CharacterModel getCharacterModel(CharacterData data, int ic) {
   		currentData = data;
   		currentChar = ic;
   		if (data==null) {
   			System.out.println("DATA NULL in model source file getModelCurrentCharacter (a)");
   			lastModel = null;
   			modelName.setValue("");
   			return null;
   		}
   		else {
	   		if (data.getStateClass()!=oldStateClass)
	   			resetCurrent(data.getStateClass());
  			CharacterModel cm=null;
  			currentModelSet = (ModelSet)data.getCurrentSpecsSet(ProbabilityModelSet.class);
  			if (currentModelSet == null) {
	   			cm= data.getDefaultModel(preferred);
	   			if (cm==null)
	   				System.out.println("MODEL NULL in model source file getModelCurrentCharacter (b)");
  			}
   			else {
   				cm = currentModelSet.getModel(ic);
	   			if (cm==null)
	   				System.out.println("MODEL NULL in model source file getModelCurrentCharacter (c)");
   			}
 			if (reassignable && lastModel != cm)
  				MesquiteTrunk.resetCheckMenuItems();
   			lastModel = cm;
   			if (lastModel != null && lastModel.getName() != null && !lastModel.getName().equals(modelName.getValue())){
   				modelName.setValue(lastModel.getName());
   			}
   			if (cm != null && ids.indexOf(cm.getID())<0)
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
   			System.out.println("STATES NULL in model source file getModelCurrentCharacter (d)");
   			lastModel = null;
   			modelName.setValue("");
   			return null;
   		}
   		else {
	   		if (states.getStateClass()!=oldStateClass)
	   			resetCurrent(states.getStateClass());
   			CharacterModel cm=null;
   			CharacterData data = states.getParentData();
	   		currentData = data;
   			if (data == null) {
   				cm = states.getDefaultModel(getProject(), preferred);
	   			if (cm==null)
	   				System.out.println("MODEL NULL in model source file getModelCurrentCharacter (e)");
   			}
   			else {
   				int ic =  states.getParentCharacter(); 
		   		currentChar = ic;
  				currentModelSet = (ModelSet)data.getCurrentSpecsSet(ProbabilityModelSet.class);
	  			if (currentModelSet == null) {
		   			if (states!=null) {
		   				cm= states.getDefaultModel(getProject(), preferred);
			   			if (cm==null)
			   				System.out.println("MODEL NULL in model source file getModelCurrentCharacter (f)");
		   			}
		   			else
			   			System.out.println("MODEL NULL in model source file getModelCurrentCharacter (g)");
	  			}
	   			else {
	   				
	   				cm = currentModelSet.getModel(ic);
			   		if (cm==null && states!=null)
		   				cm= states.getDefaultModel(getProject(), preferred);
		   			if (cm==null)
		   				System.out.println("MODEL NULL in model source file getModelCurrentCharacter (h) " + ic);
	   			}
   			}
 			if (reassignable && lastModel != cm)
  				MesquiteTrunk.resetCheckMenuItems();
   			lastModel = cm;
   			if (lastModel != null && lastModel.getName() != null && !lastModel.getName().equals(modelName.getValue())){
   				modelName.setValue(lastModel.getName());
   			}
   			if (cm != null && ids.indexOf(cm.getID())<0)
   				if (!ids.fillNextUnassigned(cm.getID())){
   					ids.resetSize(ids.getSize()+10);
   					ids.fillNextUnassigned(cm.getID());
   				}
   			return cm;
  		}
   	}
	/*.................................................................................................................*/
	void resetCurrent(Class stateClass){
		oldStateClass = stateClass;
		 if (smenu!=null) {
			deleteMenuItem(smenu);
			smenu=null;
		}   		
		if (reassignable) {
			smenu = addSubmenu(null, "Probability model", makeCommand("setModel", this), getProject().getCharacterModels());
			smenu.setCompatibilityCheck(new ModelCompatibilityInfo(ProbabilityModel.class, stateClass));
			smenu.setSelected(modelName);
		}
		resetContainingMenuBar();
	}
	/*.................................................................................................................*/
   	public void setOneCharacterAtATime(boolean chgbl){
   		if (aboutItem == null)
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
	/*.................................................................................................................*/
	MesquiteInteger pos = new MesquiteInteger();
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Sets the probabiliy character model ", "[number of model]", commandName, "setModel")) {
	 		if (currentData == null){
		 		boolean changed=false;
	      			int whichModel = MesquiteInteger.fromFirstToken(arguments, pos);
	      			if (!MesquiteInteger.isCombinable(whichModel))
	      				return null;
	 			defaultModel = getProject().getCharacterModel(new ModelCompatibilityInfo(ProbabilityModel.class, oldStateClass), whichModel);
		     	 	parametersChanged();
	     	 		return defaultModel;
	 		}
	      		else {
		 		boolean changed=false;
	      			int whichModel = MesquiteInteger.fromFirstToken(arguments, pos);
	      			if (!MesquiteInteger.isCombinable(whichModel))
	      				return null;
	 			CharacterModel model = getProject().getCharacterModel(new ModelCompatibilityInfo(ProbabilityModel.class, currentData.getStateClass()), whichModel);
	 			
				ModelSet modelSet = (ModelSet)currentData.getCurrentSpecsSet(ProbabilityModelSet.class);  
	 			if (model!=null){
		 			if (modelSet == null) {
				 		CharacterModel defaultModel =  currentData.getDefaultModel("Likelihood");
				 		modelSet= new ProbabilityModelSet("Probability Model Set", currentData.getNumChars(), defaultModel, currentData);
				 		currentData.storeSpecsSet(modelSet, ProbabilityModelSet.class);
						currentData.setCurrentSpecsSet(modelSet, ProbabilityModelSet.class);
				 		modelSet.addToFile(currentData.getFile(), getProject(), findElementManager(ProbabilityModelSet.class)); //THIS
		 			}

		 			if (modelSet != null) {
						modelSet.setModel(model, currentChar); 
		 				currentData.notifyListeners(this, new Notification(AssociableWithSpecs.SPECSSET_CHANGED));  //TODO: not quite kosher
		 			}
		     	 		parametersChanged();
	     	 		}
	     	 		return model;
 	 		}
    	 	}
    	 	else if (checker.compare(this.getClass(), "Sets the probability character model", "[number of model]", commandName, "setDefaultModel")) {
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
    	 public String getName() {
		return "Current probability models";
   	 }
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Supplies the currently assigned stochastic (likelihood, probability) model for a character." ;
   	 }
   	 
}

