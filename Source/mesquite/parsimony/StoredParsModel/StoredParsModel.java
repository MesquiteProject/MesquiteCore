/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.parsimony.StoredParsModel;
/*~~  */

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.parsimony.lib.*;

/* ======================================================================== */
public class StoredParsModel extends ParsModelSource {
  	 public String getName() {
 		return "Stored Parsimony Model";
    	 }
  	public String getExplanation() {
  		return "Supplies a user-specified model of character evolution, for parsimony calculations, stored in the file.  This can be different from the model assigned to the character as current in the List of Characters window." ;
    	 }
	/*.................................................................................................................*/
  	 	MesquiteSubmenuSpec smenu;
	ParsimonyModel currentModel;
	boolean initialized = false;
	boolean responseSuppressed = false;
	MesquiteString modelName;
	Class currentStateClass = null;
	int setModelNumber = MesquiteInteger.unassigned;
	ModelCompatibilityInfo mci;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (condition !=null && condition instanceof Class)
			currentStateClass = (Class)condition;
		smenu = addSubmenu(null, "Stored Parsimony Model", makeCommand("setModel", this), getProject().getCharacterModels());
		mci = new ParsModelCompatInfo(currentStateClass);
		smenu.setCompatibilityCheck(mci);
		smenu.setListableFilter(WholeCharacterModel.class);
		if ((ParsimonyModel)getProject().getCharacterModel(mci, 0)==null)
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
  	 
	/*.................................................................................................................*/
  	 ParsimonyModel chooseModel(Class stateClass){
		if (!MesquiteThread.isScripting()){
			return (ParsimonyModel)CharacterModel.chooseExistingCharacterModel(this, new ParsModelCompatInfo(stateClass), "Choose probability model (for " + getEmployer().getName() + ").  To make additional models, select New Character Model from the Characters menu.");
		}
 		else
 			return (ParsimonyModel)getProject().getCharacterModel(new ParsModelCompatInfo(stateClass), 0);
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
			currentStateClass = stateClass;
			mci = new ParsModelCompatInfo(currentStateClass);
			smenu.setCompatibilityCheck(mci);
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
			currentStateClass = stateClass;
			mci = new ParsModelCompatInfo(currentStateClass);
			smenu.setCompatibilityCheck(mci);
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
   	
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) {
  	 	if (currentModel==null)
  	 		return null;
   	 	Snapshot temp = new Snapshot();
  	 	temp.addLine("setModel " + getProject().getWhichCharacterModel(mci, currentModel) + "   " + ParseUtil.tokenize(currentModel.getName()));  //TODO:!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!should say which model
  	 	return temp;
  	 }
	/*.................................................................................................................*/
	MesquiteInteger pos = new MesquiteInteger();
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Sets the parsimony model of character evolution", "[number of model]", commandName, "setModel")) {
      			int whichModel = MesquiteInteger.fromFirstToken(arguments, pos);
      			String name = ParseUtil.getToken(arguments, pos);
 			ParsimonyModel model = null;
 			if (MesquiteInteger.isCombinable(whichModel))
 				model = (ParsimonyModel)getProject().getCharacterModel(mci, whichModel);
 			
 			if ((model !=null || !MesquiteInteger.isCombinable(whichModel)) && currentStateClass == null && name !=null && !(model.getName().equals(name))){ // not restricted state class; could be scripting without
 				model = (ParsimonyModel)getProject().getCharacterModel(name);
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
		return "Current model \"" + currentModel.getName();
   	 }
	/*.................................................................................................................*/
    	 
}

class ParsModelCompatInfo extends ModelCompatibilityInfo {
	public ParsModelCompatInfo(Class targetStateClass){
		super(ParsimonyModel.class, targetStateClass);
	}
	 //obj to be passed here is model, so that requester of model can check for compatibility as well as vice versa
 	public boolean isCompatible(Object obj, MesquiteProject project, EmployerEmployee prospectiveEmployer){
 		if (obj instanceof DolloModel || obj instanceof IrreversibleModel) //since these can't yet be used in Mesquite calculations
 			return false;
 		return super.isCompatible(obj, project, prospectiveEmployer);
 	}
	
}


