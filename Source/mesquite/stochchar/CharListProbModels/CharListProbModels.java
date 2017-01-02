/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.stochchar.CharListProbModels;
/*~~  */

import mesquite.lists.lib.*;
import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.stochchar.lib.*;


/* ======================================================================== */
public class CharListProbModels extends CharListAssistant implements MesquiteListener {
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
	public boolean isPrerelease() {
		return false;
  	 }
  	 
	/*.................................................................................................................*/
	public void disposing(Object obj){
		if (obj instanceof ProbabilityModel) {
				parametersChanged();
		}
	}
  	 MesquiteInteger pos = new MesquiteInteger(0);
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	 if (checker.compare(this.getClass(), "Sets the probability model of the selected characters", "[number of model]", commandName, "setModel")) {
    	 		if (table !=null && data!=null) {
    	 			boolean changed=false;
	      			int whichModel = MesquiteInteger.fromFirstToken(arguments, pos);
	      			if (!MesquiteInteger.isCombinable(whichModel))
	      				return null;
   	 			CharacterModel model = getProject().getCharacterModel(new ModelCompatibilityInfo(ProbabilityModel.class, data.getStateClass()), whichModel); //TODO: would be better if passed name of model
    	 			if (model != null) {
					ModelSet modelSet = (ModelSet)data.getCurrentSpecsSet(ProbabilityModelSet.class);
	    	 			if (modelSet == null) {
				 		CharacterModel defaultModel =  data.getDefaultModel("Likelihood");
				 		modelSet= new ProbabilityModelSet("Probability Model Set", data.getNumChars(), defaultModel, data);
				 		modelSet.addToFile(data.getFile(), getProject(), findElementManager(ProbabilityModelSet.class)); //THIS
						data.setCurrentSpecsSet(modelSet, ProbabilityModelSet.class);
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
    	 	else if (checker.compare(this.getClass(), "Stores current probability model set", null, commandName, "storeCurrent")) {
    	 		if (data!=null){
    	 			SpecsSetVector ssv = data.getSpecSetsVector(ProbabilityModelSet.class);
    	 			if (ssv == null || ssv.getCurrentSpecsSet() == null) {
			 		CharacterModel defaultModel =  data.getDefaultModel("Likelihood");
			 		ModelSet modelSet= new ProbabilityModelSet("Model Set", data.getNumChars(), defaultModel, data);
			 		modelSet.addToFile(data.getFile(), getProject(), findElementManager(ProbabilityModelSet.class)); //THIS
					data.setCurrentSpecsSet(modelSet, ProbabilityModelSet.class);
					ssv = data.getSpecSetsVector(ProbabilityModelSet.class);
    	 			}
    	 			if (ssv!=null) {
    	 				SpecsSet s = ssv.storeCurrentSpecsSet();
    					if (s.getFile() == null)
    						s.addToFile(data.getFile(), getProject(), findElementManager(ProbabilityModelSet.class));
    					s.setName(ssv.getUniqueName("Probability Model Set"));
    	 				String name = MesquiteString.queryString(containerOfModule(), "Name", "Name of probability model set to be stored", "Probability Model Set");
    	 				if (!StringUtil.blank(name))
    	 					s.setName(name);
    	 				ssv.notifyListeners(this, new Notification(MesquiteListener.NAMES_CHANGED));  
    	 			}
     	 			else MesquiteMessage.warnProgrammer("sorry, can't store because no specssetvector");
   	 		}
    	 		//return ((ListWindow)getModuleWindow()).getCurrentObject();
    	 	}
    	 	else if (checker.compare(this.getClass(), "Replaces stored probability model set by current one", null, commandName, "replaceWithCurrent")) {
    	 		if (data!=null){
    	 				SpecsSetVector ssv = data.getSpecSetsVector(ProbabilityModelSet.class);
  	 				if (ssv!=null) {
  	 					SpecsSet chosen = (SpecsSet)ListDialog.queryList(containerOfModule(), "Replace stored set", "Choose stored probability model set to replace by current set",MesquiteString.helpString,  ssv, 0);
		    	 			if (chosen!=null){
		    	 				SpecsSet current = ssv.getCurrentSpecsSet();
		    	 				ssv.replaceStoredSpecsSet(chosen, current);
		    	 			}
	    	 			}
    	 			
    	 		}
    	 		//return ((ListWindow)getModuleWindow()).getCurrentObject();
    	 	}
    	 	else if (checker.compare(this.getClass(), "Loads the stored probability model set to be the current one", "[number of probability set to load]", commandName, "loadToCurrent")) {
 			if (data !=null) {
 				int which = MesquiteInteger.fromFirstToken(arguments, stringPos);
 				if (MesquiteInteger.isCombinable(which)){
		 			SpecsSetVector ssv = data.getSpecSetsVector(ProbabilityModelSet.class);
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
		deleteMenuItem(mss);
		deleteMenuItem(mScs);
		deleteMenuItem(mRssc);
		deleteMenuItem(mLine);
		deleteMenuItem(mStc);
		mss = addSubmenu(null, "Probability model", makeCommand("setModel", this), getProject().getCharacterModels());
		mLine = addMenuSeparator();
		mScs = addMenuItem("Store current set...", makeCommand("storeCurrent",  this));
		mRssc = addMenuItem("Replace stored set by current...", makeCommand("replaceWithCurrent",  this));
		if (data !=null)
			mStc = addSubmenu(null, "Load set", makeCommand("loadToCurrent",  this), data.getSpecSetsVector(ProbabilityModelSet.class));
		mss.setCompatibilityCheck(new ModelCompatibilityInfo(ProbabilityModel.class, data.getStateClass()));
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public boolean okToDispose(Object obj, int queryUser){
		return true;  //TODO: respond
	}
	public void changed(Object caller, Object obj, Notification notification){
		if (Notification.appearsCosmetic(notification))
			return;
		outputInvalid();
		parametersChanged(notification);
	}
	public String getTitle() {
		return "Probability Model";
	}
	public String getStringForCharacter(int ic){
		if (data!=null){
			ModelSet modelSet = (ModelSet)data.getCurrentSpecsSet(ProbabilityModelSet.class);
			if (modelSet != null) {
				CharacterModel model = modelSet.getModel(ic);
				if (model!=null)
					return model.getName();
			}
			else {
				CharacterModel model = data.getDefaultModel("Likelihood");
				if (model!=null) {
					return model.getName();
				}
			}
		}
		return "?";
	}
	public String getWidestString(){
		return "Probability Model  ";
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Current Probability Models";
   	 }
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Supplies current stochastic models applied to characters for character list window." ;
   	 }
   	 
}

