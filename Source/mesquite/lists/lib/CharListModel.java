/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lists.lib;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.table.*;


/* ======================================================================== */
public abstract class CharListModel extends CharListAssistant {
	CharacterData data=null;
	MesquiteTable table=null;
	MesquiteSubmenuSpec mss;
	MesquiteMenuItemSpec mScs, mStc, mRssc, mLine;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		/* hire employees here */
		return true;
  	 }
  	 public abstract String getModelTypeName();
  	public abstract Class getModelSetClass();
	public abstract Class getModelClass();
	public abstract String getParadigm();
	public abstract ModelSet getNewModelSet(String name, int numChars, CharacterModel defaultModel, CharacterData data);
  	 MesquiteInteger pos = new MesquiteInteger(0);
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	 if (checker.compare(this.getClass(), "Sets the " + getModelTypeName() + " of the selected characters", "[number of model]", commandName, "setModel")) {
    	 		if (table !=null && data!=null) {
    	 			boolean changed=false;
	      			int whichModel = MesquiteInteger.fromFirstToken(arguments, pos);
	      			if (!MesquiteInteger.isCombinable(whichModel))
	      				return null;
   	 			CharacterModel model = getProject().getCharacterModel(new ModelCompatibilityInfo(getModelClass(), data.getStateClass()), whichModel); //TODO: would be better if passed name of model
    	 			if (model != null) {
					ModelSet modelSet = (ModelSet)data.getCurrentSpecsSet(getModelSetClass());
	    	 			if (modelSet == null) {
				 		CharacterModel defaultModel =  data.getDefaultModel(getParadigm());
				 		modelSet= getNewModelSet("Model Set", data.getNumChars(), defaultModel, data);
				 		modelSet.addToFile(data.getFile(), getProject(), findElementManager(getModelSetClass())); //THIS
						data.setCurrentSpecsSet(modelSet, getModelSetClass());
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
	     	 		parametersChanged();
   	 		}
    	 	}
    	 	else if (checker.compare(this.getClass(), "Stores current " + getModelTypeName() + " set", null, commandName, "storeCurrent")) {
    	 		if (data!=null){
    	 			SpecsSetVector ssv = data.getSpecSetsVector(getModelSetClass());
    	 			if (ssv == null || ssv.getCurrentSpecsSet() == null) {
			 		CharacterModel defaultModel =  data.getDefaultModel(getParadigm());
			 		ModelSet modelSet= getNewModelSet("Model Set", data.getNumChars(), defaultModel, data);
			 		modelSet.addToFile(data.getFile(), getProject(), findElementManager(getModelSetClass())); //THIS
					data.setCurrentSpecsSet(modelSet, getModelSetClass());
					ssv = data.getSpecSetsVector(getModelSetClass());
    	 			}
    	 			if (ssv!=null) {
    	 				SpecsSet s = ssv.storeCurrentSpecsSet();
    					if (s.getFile() == null)
    						s.addToFile(data.getFile(), getProject(), findElementManager(getModelSetClass()));
  	 				s.setName(ssv.getUniqueName("Model Set"));
    	 				String name = MesquiteString.queryString(containerOfModule(), "Name", "Name of " + getModelTypeName() + " set to be stored", s.getName());
    	 				if (!StringUtil.blank(name))
    	 					s.setName(name);
    	 				ssv.notifyListeners(this, new Notification(MesquiteListener.NAMES_CHANGED));  
    	 			}
     	 			else MesquiteMessage.warnProgrammer("sorry, can't store because no specssetvector");
   	 		}
    	 		//return ((ListWindow)getModuleWindow()).getCurrentObject();
    	 	}
    	 	else if (checker.compare(this.getClass(), "Replaces stored " + getModelTypeName() + " set by current one", null, commandName, "replaceWithCurrent")) {
    	 		if (data!=null){
    	 				SpecsSetVector ssv = data.getSpecSetsVector(getModelSetClass());
  	 				if (ssv!=null) {
  	 					SpecsSet chosen = (SpecsSet)ListDialog.queryList(containerOfModule(), "Replace stored set", "Choose stored " + getModelTypeName() + " set to replace by current set",MesquiteString.helpString, ssv, 0);
		    	 			if (chosen!=null){
		    	 				SpecsSet current = ssv.getCurrentSpecsSet();
		    	 				ssv.replaceStoredSpecsSet(chosen, current);
		    	 			}
	    	 			}
    	 			
    	 		}
    	 		//return ((ListWindow)getModuleWindow()).getCurrentObject();
    	 	}
    	 	else if (checker.compare(this.getClass(), "Loads the stored " + getModelTypeName() + " set to be the current one", "[number of set to load]", commandName, "loadToCurrent")) {
 			if (data !=null) {
 				int which = MesquiteInteger.fromFirstToken(arguments, stringPos);
 				if (MesquiteInteger.isCombinable(which)){
		 			SpecsSetVector ssv = data.getSpecSetsVector(getModelSetClass());
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
	public void setTableAndData(MesquiteTable table, CharacterData data){
		this.data = data;
		this.table = table;
		if(mss !=null)
			deleteMenuItem(mss);
		if(mScs !=null)
			deleteMenuItem(mScs);
		if(mRssc !=null)
			deleteMenuItem(mRssc);
		if(mLine !=null)
			deleteMenuItem(mLine);
		if(mStc !=null)
			deleteMenuItem(mStc);
		mss = addSubmenu(null, getTitle(), makeCommand("setModel", this), getProject().getCharacterModels());
		mLine = addMenuSeparator();
		mScs = addMenuItem("Store current set", makeCommand("storeCurrent",  this));
		mRssc = addMenuItem("Replace stored set by current", makeCommand("replaceWithCurrent",  this));
		if (data !=null)
			mStc = addSubmenu(null, "Load model set", makeCommand("loadToCurrent",  this), data.getSpecSetsVector(getModelSetClass()));
		mss.setCompatibilityCheck(new ModelCompatibilityInfo(getModelClass(), data.getStateClass()));
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public void disposing(Object obj){
		//TODO: respond
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public boolean okToDispose(Object obj, int queryUser){
		return true;  //TODO: respond
	}
	public void changed(Object caller, Object obj, Notification notification){
		outputInvalid();
		parametersChanged(notification);
	}
	public String getStringForCharacter(int ic){
		if (data!=null){
			ModelSet modelSet = (ModelSet)data.getCurrentSpecsSet(getModelSetClass());
			if (modelSet != null) {
				CharacterModel model = modelSet.getModel(ic);
				if (model!=null)
					return model.getName();
			}
			else {
				CharacterModel model = data.getDefaultModel(getParadigm());
				if (model!=null) {
					return model.getName();
				}
			}
		}
		return "?";
	}
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Supplies current " + getModelTypeName() + "s applied to characters for character list window." ;
   	 }
   	 
}

