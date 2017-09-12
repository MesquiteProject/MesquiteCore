/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.stochchar.StoredProbModelSim;
/*~~  */

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import mesquite.stochchar.lib.*;

/* ======================================================================== */
public class StoredProbModelSim extends ProbModelSourceSim {
	MesquiteSubmenuSpec smenu;
	ProbabilityModel currentModel;
	boolean initialized = false;
	boolean responseSuppressed = false;
	MesquiteString modelName;
	Class currentStateClass = null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (condition !=null && condition instanceof Class)
			currentStateClass = (Class)condition;
		smenu = addSubmenu(null, "Stored Probabilistic Model (for simulation)", makeCommand("setModel", this), getProject().getCharacterModels());
		smenu.setCompatibilityCheck(getCompatibilityInfo(currentStateClass));
		smenu.setListableFilter(WholeCharacterModel.class);
		if ((ProbabilityModel)getProject().getCharacterModel(getCompatibilityInfo(currentStateClass), 0)==null)
			return sorry("There are no stored character models available of the appropriate type for the simulation.");
		currentModel = chooseModel(currentStateClass);
		if (currentModel == null) {
			if (MesquiteThread.isScripting())
				logln("No model of character evolution appropriate for the simulation was obtained.");
			//if not scripting then probably by user cancel, thus no message
			return false;
		}
		modelName = new MesquiteString();
		modelName.setValue(currentModel.getName());
   		addMenuItem("About the Model (for " + getEmployer().getName() + ")...", makeCommand("aboutModel", this));
		smenu.setList(getProject().getCharacterModels());
		smenu.setSelected(modelName);
		
		getProject().getCentralModelListener().addListener(this); //to listen for static changes to class of current model
		return true;
  	 }
  	 
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
  	 	return false;
  	 }
	/*.................................................................................................................*/
 	 ProbabilityModel chooseModel(Class stateClass){
		if (!MesquiteThread.isScripting()){
			return (ProbabilityModel)CharacterModel.chooseExistingCharacterModel(this, getCompatibilityInfo(stateClass), "Choose probability model (for " + getEmployer().getName() + ").  To make additional models, select New Character Model from the Characters menu");
		}
 		else
 			return (ProbabilityModel)getProject().getCharacterModel(getCompatibilityInfo(stateClass), 0);
  	 }
	/*.................................................................................................................*/
  	 public void endJob(){
		getProject().getCentralModelListener().removeListener(this);
  	 	super.endJob();
  	 }
   	 
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
	/*.................................................................................................................*/
	public void changed(Object caller, Object obj, Notification notification){
		if (currentModel !=null && obj instanceof Class && ((Class)obj).isAssignableFrom(currentModel.getClass())) {
			parametersChanged(notification);
		}
		super.changed(caller, obj, notification);
	}
	/** passes which object was disposed*/
	public void disposing(Object obj){
		if (obj == currentModel) {
			currentModel = null;
			parametersChanged();
		}
	}
  	 
   	/** returns model for character ic in data */
   	public CharacterModel getCharacterModel(CharacterData data, int ic) {
		Class stateClass = currentStateClass;
		if (data !=null) 
			stateClass = data.getStateClass();
		if (stateClass !=null && stateClass != currentStateClass && oneAtATime){
			smenu.setCompatibilityCheck(getCompatibilityInfo(stateClass));
			currentStateClass = stateClass;
			resetContainingMenuBar();
			
		}
		if (currentModel == null)
			currentModel = chooseModel(stateClass);
		if (currentModel == null)
			return null;
		return currentModel;
   	}
   	/** returns model for character */
   	public CharacterModel getCharacterModel(CharacterStatesHolder states){
		Class stateClass = currentStateClass;
		if (states !=null)
			stateClass = states.getStateClass();
		if (stateClass !=null && stateClass != currentStateClass && oneAtATime){
			smenu.setCompatibilityCheck(getCompatibilityInfo(stateClass));
			currentStateClass = stateClass;
			resetContainingMenuBar();
			
		}
		if (currentModel == null)
			currentModel = chooseModel(stateClass);
		if (currentModel == null)
			return null;
		return currentModel;
   	}
	/*.................................................................................................................*/
   	boolean oneAtATime= false;
   	public void setOneCharacterAtATime(boolean chgbl){
  		oneAtATime = chgbl;
   	}
   	
   	private ModelCompatibilityInfo getCompatibilityInfo(Class stateClass){
   		return new SimModelCompatInfo(ProbabilityModel.class, stateClass);
   	}
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) {
  	 	if (currentModel==null)
  	 		return null;
   	 	Snapshot temp = new Snapshot();
   	 	if (currentModel != null)
  	 	temp.addLine("setModelInt " + getProject().getWhichCharacterModel(getCompatibilityInfo(currentStateClass), currentModel) + " " + ParseUtil.tokenize(currentModel.getName()));  //TODO:!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!should say which model
  	 	return temp;
  	 }
	/*.................................................................................................................*/
	MesquiteInteger pos = new MesquiteInteger();
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Sets the probabilistic model of character evolution", "[number of model]", commandName, "setModel")) {
      			int whichModel = MesquiteInteger.fromFirstToken(arguments, pos);
      			String expectedName  = ParseUtil.getToken(arguments, pos);
      			ProbabilityModel model = null;
      			if (!MesquiteInteger.isCombinable(whichModel)) {
 				model = (ProbabilityModel)getProject().getCharacterModel(parser.getFirstToken(arguments));
 			}
 			else {
 				//prefer the name if available
	 			if (expectedName !=null) {
	 				model = (ProbabilityModel)getProject().getCharacterModel(expectedName);
	 				if (!model.isCompatible(getCompatibilityInfo(currentStateClass), getProject(), this))
	 					model = null;
	 			}
	 			
	 			if (model == null)
	 				model = (ProbabilityModel)getProject().getCharacterModel(getCompatibilityInfo(currentStateClass), whichModel);
 			}
 			
	     	 	if (model!=null) {
	     	 		if (model.getName() != null && !StringUtil.blank(expectedName) && !model.getName().equals(expectedName))
	     	 			MesquiteMessage.warnProgrammer("Error: model set not one expected (set: " + model.getName() + "; expected: " + expectedName + ")");
	     	 		if (currentModel!=null)
	     	 			currentModel.removeListener(this);
	     	 		currentModel = model;
		 		modelName.setValue(currentModel.getName());
	     	 		currentModel.addListener(this);
	     	 		parametersChanged();
     	 			return model;
     	 		}
    	 	}
    	 	else if (checker.compare(this.getClass(), "Sets the probabilistic model of character evolution (internal, for scripting use)", "[number of model]", commandName, "setModelInt")) {
      			int whichModel = MesquiteInteger.fromFirstToken(arguments, pos);
      			String expectedName  = ParseUtil.getToken(arguments, pos);
      			ProbabilityModel model = null;
 			if (expectedName !=null) { //choose the name first whether or not compatible
 				model = (ProbabilityModel)getProject().getCharacterModel(expectedName);
 			}
      			if (model == null ){
	      			if (!MesquiteInteger.isCombinable(whichModel)) { //just the name
	 				model = (ProbabilityModel)getProject().getCharacterModel(parser.getFirstToken(arguments));
	 			}
	 			else if (whichModel >= 0) {
		 			model = (ProbabilityModel)getProject().getCharacterModel(getCompatibilityInfo(currentStateClass), whichModel);
	 			}
 			}
 			
	     	 	if (model!=null) {
	     	 		if (model.getName() != null && !StringUtil.blank(expectedName) && !model.getName().equals(expectedName))
	     	 			MesquiteMessage.warnProgrammer("Error: model set not one expected (set: " + model.getName() + "; expected: " + expectedName + ")");
	     	 		if (currentModel!=null)
	     	 			currentModel.removeListener(this);
	     	 		currentModel = model;
		 		modelName.setValue(currentModel.getName());
	     	 		currentModel.addListener(this);
	     	 		parametersChanged();
     	 			return model;
     	 		}
    	 	}
    	 	else if (checker.compare(this.getClass(), "Displays a dialog about the last model returned", null, commandName, "aboutModel")) {
				String s = "";
				if (currentModel == null)
					s = "Sorry, no reference to the current model was found";
				else
					s = "The current model is \"" + currentModel.getName() + "\".\nExplanation: " + currentModel.getExplanation();
				discreetAlert( s);
				return null;
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
    	 	return null;
   	 }
	/*.................................................................................................................*/
    	 public String getParameters() {
		if (currentModel==null)
			return "Model NULL";
		return "Current model \"" + currentModel.getName() + "\": "+currentModel.getParameters() ;
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Single Stored Probability Model for Simulation";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Supplies a user-specified model of character evolution stored in the file for simulation." ;
   	 }
   	 
}

