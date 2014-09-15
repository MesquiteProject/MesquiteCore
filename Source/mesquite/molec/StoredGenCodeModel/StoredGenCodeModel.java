/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.StoredGenCodeModel;
/*~~  */

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.parsimony.lib.*;
import mesquite.molec.lib.*;

/* ======================================================================== */
public class StoredGenCodeModel extends GenCodeModelSource {
	MesquiteSubmenuSpec smenu;
	GenCodeModel currentModel;
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
		smenu = addSubmenu(null, "Stored Genetic Code", makeCommand("setModel", this), getProject().getCharacterModels());
		mci = new GenCodeModelCompatInfo(currentStateClass);
		smenu.setCompatibilityCheck(mci);
		smenu.setListableFilter(WholeCharacterModel.class);
		if ((GenCodeModel)getProject().getCharacterModel(mci, 0)==null)
			return sorry("There are no suitable stored character models available");
		modelName = new MesquiteString();
		addMenuItem("About the Model (for " + getEmployer().getName() + ")...", makeCommand("aboutModel", this));
		smenu.setList(getProject().getCharacterModels());
		smenu.setSelected(modelName);

		getProject().getCentralModelListener().addListener(this);//to listen for static changes to class of current model
		return true;
	}

	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 110;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}

	/*.................................................................................................................*/
	GenCodeModel chooseModel(Class stateClass){
		if (!MesquiteThread.isScripting()){
			return (GenCodeModel)CharacterModel.chooseExistingCharacterModel(this, new GenCodeModelCompatInfo(stateClass), "Choose probability model (for " + getEmployer().getName() + ").  To make additional models, select New Character Model from the Characters menu.");
		}
		else
			return (GenCodeModel)getProject().getCharacterModel(new GenCodeModelCompatInfo(stateClass), 0);
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
			mci = new GenCodeModelCompatInfo(currentStateClass);
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
			mci = new GenCodeModelCompatInfo(currentStateClass);
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
		if (checker.compare(this.getClass(), "Sets the genetic code", "[number of model]", commandName, "setModel")) {
			int whichModel = MesquiteInteger.fromFirstToken(arguments, pos);
			String name = ParseUtil.getToken(arguments, pos);
			GenCodeModel model = null;
			if (MesquiteInteger.isCombinable(whichModel))
				model = (GenCodeModel)getProject().getCharacterModel(mci, whichModel);

			if ((model !=null || !MesquiteInteger.isCombinable(whichModel)) && currentStateClass == null && name !=null && !(model.getName().equals(name))){ // not restricted state class; could be scripting without
				model = (GenCodeModel)getProject().getCharacterModel(name);
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
	public String getName() {
		return "Stored Genetic Code";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Supplies a user-specified genetic code, stored in the file." ;
	}

}

class GenCodeModelCompatInfo extends ModelCompatibilityInfo {
	public GenCodeModelCompatInfo(Class targetStateClass){
		super(GenCodeModel.class, targetStateClass);
	}
	//obj to be passed here is model, so that requester of model can check for compatibility as well as vice versa
	public boolean isCompatible(Object obj, MesquiteProject project, EmployerEmployee prospectiveEmployer){
		if (obj instanceof DolloModel || obj instanceof IrreversibleModel) //since these can't yet be used in Mesquite calculations
			return false;
		return super.isCompatible(obj, project, prospectiveEmployer);
	}

}


