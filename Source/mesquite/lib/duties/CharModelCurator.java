/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lib.duties;

import java.awt.*;
import java.util.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;


/* ======================================================================== */
/**
This class of modules curates a subclass of character models for use in calculation routines.*/

public abstract class CharModelCurator extends MesquiteModule  {
	Vector modelsToEdit= new Vector();  //provided to keep track of modules being edited (management methods also provided)
	Vector windowServers= new Vector();//provided in case window holder modules used

	public Class getDutyClass() {
		return CharModelCurator.class;
	}
	public String getDutyName() {
		return "Model Curator";
	}


	/**Edit model.  Editing must be done in a modal dialog box becuase the results are needed immediately.*
   	public abstract void editModelModal(CharacterModel model);  //NOT YET IMPLEMENTED
	 */
	public abstract String getNameOfModelClass();
	public abstract String getNEXUSNameOfModelClass();
	public abstract boolean curatesModelClass(Class modelClass);
	public abstract Class getModelClass();
	/**Make new model and return (don't add to file or show to edit)*/
	public abstract CharacterModel makeNewModel(String name);
	public abstract CharacterModel readCharacterModel(String name, MesquiteInteger stringPos, String description, int format);
	public boolean isSubstantive(){
		return false;  
	}

	public void endJob(){
		for (int i=0; i<modelsToEdit.size(); i++) {
			CharacterModel model = getModel(i);
			if (model!=null)
				model.removeListener(this);
		}
		super.endJob();
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	protected MesquiteWindow getWindow(int i){
		if (i>=0 && i< windowServers.size()) {
			Object obj = windowServers.elementAt(i);
			if (obj instanceof MesquiteModule)
				return (MesquiteWindow)((MesquiteModule)windowServers.elementAt(i)).getModuleWindow();
		}
		return null;
	}
	protected MesquiteModule getWindowHolder(int i){
		if (i>=0 && i< windowServers.size()) {
			Object obj = windowServers.elementAt(i);
			if (obj instanceof MesquiteModule)
				return (MesquiteModule)windowServers.elementAt(i);
		}
		return null;
	}
	protected CharacterModel getModel(int i){
		if (i>=0 && i< modelsToEdit.size()) {
			Object obj = modelsToEdit.elementAt(i);
			if (obj instanceof CharacterModel)
				return (CharacterModel)obj;
		}
		return null;
	}
	protected int getModelNumber(CharacterModel model){
		return modelsToEdit.indexOf(model);
	}
	/*.................................................................................................................*/
	public void employeeQuit(MesquiteModule m){
		int i = windowServers.indexOf(m);
		if (i>=0) {
			CharacterModel model = getModel(i);
			if (model!=null) {
				model.removeListener(this);
				modelsToEdit.removeElement(model);
			}
			windowServers.removeElement(m);
		}
	}
	/** passes which object was disposed*/
	public void disposing(Object obj){
		if (obj instanceof CharacterModel){
			int i = modelsToEdit.indexOf(obj);
			if (i>=0) {
				fireEmployee(getWindowHolder(i));
				windowServers.removeElement(obj);
				CharacterModel m = getModel(i);
				if (m!=null)
					m.removeListener(this);
				modelsToEdit.removeElementAt(i);
			}
		}
	}
	/** Asks whether it's ok to delete the object as far as the listener is concerned (e.g., is it in use?)*/
	public boolean okToDispose(Object obj, int queryUser){
		return true;
	}
	/*.................................................................................................................*/
	public void disposeModel(CharacterModel model) {
		getProject().removeFileElement(model);//must remove first, before disposing
		model.dispose();
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		if (modelsToEdit.size() ==0)
			return null;
		Snapshot temp = new Snapshot();
		for (int i=0; i<modelsToEdit.size(); i++) {
			temp.addLine("editModel " + ParseUtil.tokenize(getModel(i).getName()), getWindowHolder(i));
		}
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Edits the character model", "[name of character model]", commandName, "editModel")) {
			CharacterModel model = getProject().getCharacterModel(parser.getFirstToken(arguments));
			if (model !=null) {
				return showEditor(model);
			}
			return null;
		}
		else if (checker.compare(this.getClass(), "Makes new character model", "[name of character model]", commandName, "newModel")) {
			String name = MesquiteString.queryShortString(containerOfModule(), "New "+getName(), "Name of new "+getName()+":", getProject().getCharacterModels().getUniqueName(getName()));
			if (name==null)
				return null;
			CharacterModel model = makeNewModel(name);
			model.addToFile(null, getProject(), null); //TODO:ok to add to homefile, or query user?
			//MesquiteModule[] curators = null;
			//curators = CharacterModel.findCurators(this, model.getClass());
			showEditor(model);
			if (model.getEditCancel()) {
				disposeModel(model);
				return null;
			}
			resetAllMenuBars();
			return model;
		}
		else
			return  super.doCommand(commandName, arguments, checker);
	}
	/**Edit model.  Editing to be done in non-modal window, which is to be returned in the ObjectContainer.
	Returns the module to whom commands are to be sent (in case WindowServer used).  Methods using window holder service should override this*/
	public MesquiteModule editModelNonModal(CharacterModel model, ObjectContainer window){
		return this;
	}

	/*.................................................................................................................*/
	/**Edit model.  Editing to be done in non-modal window.  Methods not using window holder service could override this.  Returns the module that owns the window produced.*/
	public MesquiteModule showEditor(CharacterModel model){
		if (model == null)
			return null;
		int i = getModelNumber(model);
		if (i>=0) {
			MesquiteModule windowServer = getWindowHolder(i);
			if (windowServer == null) {
				alert("error: model found but window holder not found");
				return null;
			}
			MesquiteWindow win = windowServer.getModuleWindow();
			if (win!=null)
				win.show();
			return windowServer;
		}
		ObjectContainer w = new ObjectContainer();
		MesquiteModule mod = editModelNonModal(model, w);
		MesquiteWindow window = (MesquiteWindow)w.getObject();
		modelsToEdit.addElement(model);
		model.addListener(this);

		if (mod !=null) {
			windowServers.addElement(mod);
			if (window!=null)
				mod.setModuleWindow(window);
		}
		if (window!=null)
			window.setDefaultAnnotatable(model);


		if (mod != null)
			mod.resetContainingMenuBar();
		resetContainingMenuBar();

		if (window !=null){ 
			if (!MesquiteThread.isScripting())
				window.setVisible(true);
			resetAllWindowsMenus();
		}

		return mod;
	}
	/*.................................................................................................................*/
}


