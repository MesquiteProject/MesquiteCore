/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.stochchar.StoredProbModel;
/*~~  */

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import mesquite.stochchar.lib.*;

/* ======================================================================== */
public class StoredProbModel extends ProbModelSourceLike {
	MesquiteSubmenuSpec smenu;
	ProbabilityModel currentModel;
	boolean initialized = false;
	boolean responseSuppressed = false;
	MesquiteString modelName;
	Class currentStateClass = null;
	int setModelNumber = MesquiteInteger.unassigned;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (condition !=null && condition instanceof Class)
			currentStateClass = (Class)condition;
		smenu = addSubmenu(null, "Stored Probabilistic Model", makeCommand("setModel", this), getProject().getCharacterModels());
		smenu.setCompatibilityCheck(getCompatibilityInfo(currentStateClass));
		smenu.setListableFilter(WholeCharacterModel.class);
		if ((ProbabilityModel)getProject().getCharacterModel(getCompatibilityInfo(currentStateClass), 0)==null)
			return sorry("There are no suitable stored character models available");
		modelName = new MesquiteString();
   		addMenuItem("About the Model (for " + getEmployer().getName() + ")...", makeCommand("aboutModel", this));
		smenu.setList(getProject().getCharacterModels());
		smenu.setSelected(modelName);
		
		getProject().getCentralModelListener().addListener(this);//to listen for static changes to class of current model
		return true;
  	 }
  	 
	/*.................................................................................................................*/
  	 public boolean isPrerelease(){
  	 	return false;
  	 }
   	 public boolean modelFromModelSet() {
 	 	return true;    	 
 }

	/*.................................................................................................................*/
  	 ProbabilityModel chooseModel(Class stateClass){
		if (!MesquiteThread.isScripting()){
			return (ProbabilityModel)CharacterModel.chooseExistingCharacterModel(this, getCompatibilityInfo(stateClass), "Choose probability model (for " + getEmployer().getName() + ").  To make additional models, select New Character Model from the Characters menu.");
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
		else if (obj == currentModel) {
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
   		return new LikeModelCompatInfo(ProbabilityModel.class, stateClass);
   	}
   	
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) {
  	 	if (currentModel==null)
  	 		return null;
   	 	Snapshot temp = new Snapshot();
  	 	temp.addLine("setModel " + getProject().getWhichCharacterModel(getCompatibilityInfo(currentStateClass), currentModel) + "   " + ParseUtil.tokenize(currentModel.getName()));  //TODO:!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!should say which model
  	 	return temp;
  	 }
	/*.................................................................................................................*/
	MesquiteInteger pos = new MesquiteInteger();
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Sets the probabilistic model of character evolution", "[number of model]", commandName, "setModel")) {
      			int whichModel = MesquiteInteger.fromFirstToken(arguments, pos);
      			String name = ParseUtil.getToken(arguments, pos);
 			ProbabilityModel model = null;
 			if (MesquiteInteger.isCombinable(whichModel))
 				model = (ProbabilityModel)getProject().getCharacterModel(getCompatibilityInfo(currentStateClass), whichModel);
 			
 			if (((model !=null && model instanceof ProbabilityModel) || !MesquiteInteger.isCombinable(whichModel)) && currentStateClass == null && name !=null && !(model.getName().equals(name))){ // not restricted state class; could be scripting without
 				model = (ProbabilityModel)getProject().getCharacterModel(name);
 			}
 			
	     	 	if (model!=null) {
	     	 		//if (currentModel!=null)
	     	 		//	currentModel.removeListener(this);
	     	 		currentModel = model;
		 		modelName.setValue(currentModel.getName());
	     	 		//currentModel.addListener(this);
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
		return "Stored Probability Model";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Supplies a user-specified model of character evolution stored in the file." ;
   	 }
   	 
}
/* ======================================================================== */
class LikeModelCompatInfo extends ModelCompatibilityInfo {
	public LikeModelCompatInfo(Class targetModelSubclass, Class targetStateClass){
		super(targetModelSubclass, targetStateClass);
	}
	 //obj to be passed here is model, so that requester of model can check for compatibility as well as vice versa; added Apr 02
 	public boolean isCompatible(Object obj, MesquiteProject project, EmployerEmployee prospectiveEmployer){
		if (obj instanceof ProbabilityModel && !(((ProbabilityModel)obj).isFullySpecified() || obj instanceof CModelEstimator)){
 			return false;
 		}
		return  super.isCompatible(obj, project, prospectiveEmployer);
 	}
	
}



