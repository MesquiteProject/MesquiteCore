/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib.characters; 

import java.awt.*;
import java.util.*;
import mesquite.lib.duties.*;
import mesquite.lib.*;

/*Last documented:  May 2000 */
/* ======================================================================== */
/** Models of character evolution are represented by objects of subclasses of CharacterModel.  
Subclasses include parsimony and probability models, which in turn may be subclassed for particular types of data (e.g. CategParsimonyModel)
which in turn may be further subclassed for specific types of model (e.g., step matrix).
Usually in calculatins, a given MesquiteModule more or less promises to handle any model belonging to a subclass, e.g. any
stepmatrix.  By having subclasses of subclasses of CharacterModel, modules can indicate they specialize
on a particular sort of step matrix, eg. symmetrical ones.
 */
public abstract class CharacterModel extends FileElement implements CompatibilityChecker, Explainable, MesquiteListener {
	static int numModels = 0;
	Class stateClass;
	Vector listeners;
	boolean editCancel = false;
	String stateClassName = "undetermined";
	boolean builtIn = false;
	boolean disposed = false;
	CharacterModel mother = null;
	boolean allowSubclass = false;
	boolean userVisible = true;
	
	public CharacterModel (String name, Class stateClass) {
		this.name = name;
		this.stateClass = stateClass;
		
		if (stateClass != null && stateClass != CharacterState.class && CharacterState.class.isAssignableFrom(stateClass)){
			try {
				CharacterState s = (CharacterState)stateClass.newInstance();
				if (s!=null) {
					stateClassName = s.getDataTypeName();
				}
			}
			catch (IllegalAccessException e){MesquiteTrunk.mesquiteTrunk.alert("iae csmmm");e.printStackTrace(); }
			catch (InstantiationException e){MesquiteTrunk.mesquiteTrunk.alert("ie csmmm"); e.printStackTrace();}
		}
		numModels++;
		if (name == null)
			this.name = "Character Model" + numModels;
	}
	/*.................................................................................................................*/
	/* For cloned models used locally, returns the original model from which they were cloned */
	public CharacterModel getMother(){
		return mother;
	}
	public String getDefaultIconFileName(){ //for small 16 pixel icon at left of main bar
		return "charModelSmall.gif";
	}
	/*.................................................................................................................*/
	public boolean isUserVisible() {
		return userVisible;
	}
	public void setUserVisible(boolean userVisible) {
		this.userVisible = userVisible;
	}
/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) {
  	 	return null;
  	 }
  	/*.................................................................................................................*/
 	/** Performs command (for Commandable interface) */
 	public Object doCommand(String commandName, String arguments, CommandChecker checker){
 		if (checker.compare(this.getClass(), "Edits the character model", null, commandName, "editMe")){
				ElementManager manager = getManager();
				if (manager instanceof mesquite.charMatrices.ManageCharModels.ManageCharModels){
					mesquite.charMatrices.ManageCharModels.ManageCharModels mn = (mesquite.charMatrices.ManageCharModels.ManageCharModels)manager;
					CharModelCurator curator = mn.findReader(getClass());
					if (curator==null)
						mn.discreetAlert("Sorry, no curator module was found for that sort of character model (category: " + getTypeName() + ")");
					else 
						curator.showEditor(this);
				}
		
 	}
 	else if (checker.compare(this.getClass(), "Renames the file element", null, commandName, "renameMe")) {
 			String s = MesquiteString.queryString(MesquiteTrunk.mesquiteTrunk.containerOfModule(), "Rename " + getTypeName(), "Rename " + getTypeName() +" \"" + getName() + "\"", getName());
 			if (s!=null) {
 				setName(s);
 				MesquiteWindow.resetAllTitles();
 				ElementManager manager = getManager();

 				if (manager!=null && manager instanceof MesquiteModule)
 					((MesquiteModule)manager).resetAllMenuBars();
 				else
 					MesquiteTrunk.mesquiteTrunk.resetAllMenuBars();
 				MesquiteProject proj = getProject();
				if (proj != null){
 					proj.getCentralModelListener().notifyListeners(this, this, new Notification(MesquiteListener.NAMES_CHANGED));
 				}

 			}
 		}
 		else
 			return  super.doCommand(commandName, arguments, checker);

 		return null;
 	}
 	/* copy information from this to model passed (used in cloneModelWithMotherLink to ensure that superclass info is copied); should call super.copyToClone(pm) */
	public void copyToClone(CharacterModel pm){
		if (pm == null)
			return;
		pm.name = name;
		pm.stateClass = stateClass;
		pm.setProject(getProject());
	}
 	/* establish pm as daughter clone, including add listener to know if original changed */
	public void completeDaughterClone(CharacterModel formerClone, CharacterModel pm){
		if (pm == null)
			return;
		copyToClone(pm);
		if (formerClone!=null)
			formerClone.dispose();
		pm.mother = this;
		addListener(pm); //so that pm, the daughter clone, can be informed if changes
	}
	/*.................................................................................................................*/
	/** Mother from which cloned has changed*/
	public void originalChanged(){
		if (mother == null)
			return;
		mother.copyToClone(this);
		notifyListeners(this, new Notification(MesquiteListener.UNKNOWN));
	}
	/*.................................................................................................................*/
	/** must be called for any cloned daughter models when being discarded!!!!*/
	public void dispose(){
		CentralModelListener cml = null;
		disposed = true;
		if (getProject() !=null)
			cml = getProject().getCentralModelListener();
		super.dispose();
		if (mother !=null)
			mother.removeListener(this);
		if (cml !=null)
			cml.notifyDisposing(this);
		super.dispose();
	}
	/*.................................................................................................................*/
	public void changed(Object caller, Object obj, Notification notification){
		if (obj == mother) {
			originalChanged();
		}
	}
	/** passes which object was disposed*/
	public void disposing(Object obj){
	}
	/** Asks whether it's ok to delete the object as far as the listener is concerned (e.g., is it in use?)*/
	public boolean okToDispose(Object obj, int queryUser){
		return true;
	}
	public boolean isDisposed(){
		return disposed;
	}
	/*.................................................................................................................*/
	public boolean getEditCancel() {
		return editCancel;
	}
	/*.................................................................................................................*/
	public void setEditCancel(boolean cancel) {
		editCancel = cancel;
	}
	/*.................................................................................................................*/
	public String getTypeName(){
		return "Character model";
	}

	/*.................................................................................................................*/
	public abstract String getModelTypeName();

	/** returns the paradigm (e.g. "parsimony") of the model.  Mostly used for user's information*/
	public abstract String getParadigm();
	
	/** returns the name of the data type (state class) to which this model applies*/
	public String getStateClassName(){
		return stateClassName;
	}
	/** sets name of model*/
	public void setName(String name) {
		this.name = name;
		if (name == null)
			this.name = "Character Model" + numModels;
		//TODO: notify listeners of name change
	}
	/** returns name of model*/
	public String getName() {
		return name;
	}
	
	/** returns name of model for writing into NEXUS file*/
	public String getNEXUSName() {
		return getName();
	}
	/** returns name of model class (e.g. "stepmatrix")*/
	public String getNEXUSClassName() {
		return "";
	}
	/** returns nexus command introducing this model (e.g. "USERTYPE" or "CHARMODEL")*/
	public String getNEXUSCommand() {
		return "CharModel";
	}
	/** returns whether or not model is default (unordered, initial Jukes-Cantor; if so, then doesn't need to be written to file*/
	public boolean isBuiltIn() {
		return builtIn;
	}
	/** returns whether or not model is default (unordered, initial Jukes-Cantor; if so, then doesn't need to be written to file*/
	public void setBuiltIn(boolean is) {
		builtIn=is;
	}
	
	/** read the passed string for specification of the model; string may include extraneous information and
	hence stringPos is passed to indicate at what string position the description (at the start of the modelSpecification in
	"USERTYPE modelName (modelClassName) =  modelSpecification;"    Format parameter indicates the format of the description, with
	0 = XML; 1 = public NEXUS; 2 = private Mesquite NEXUS.*/
	public static final int XMLFormat = 0;
	public static final int NEXUSFormat = 1;
	public static final int MesquiteNEXUSFormat = 2;
	public void fromString (String description, MesquiteInteger stringPos, int format) {
	}
	
	/** return string description of model */
	public String toString (){
		return getName() + " (model of class " + getTypeName() + ", id " + getID() + ")";
	}
	/** return the NEXUS-format string describing the model.  The string begins at the point of the modelSpecification
	in "USERTYPE modelName (modelClassName) =  modelSpecification;" */
	public String getNexusSpecification (){
		return null;
	}
	
	/** return an explanation of the model. */
	public abstract String getExplanation ();
	
	/** Return an explanation of the settings used in calculations (e.g., optimization settings).  These are not the parameters of the model. */
	public String getSettingsString(){
		return null;
	}
	
	/** returns subclass of CharacterState to which model can apply (i.e., categorical versus continuous data)*/
	public Class getStateClass() {  
		return stateClass;
	}
	
 	/*.................................................................................................................*/
	public void allowUseOnDataSubclasses(boolean allow){
		this.allowSubclass = allow;
	}
	/*.................................................................................................................*/
 	public boolean isCompatible(Object obj, MesquiteProject project, EmployerEmployee prospectiveEmployer){
 		return isCompatible(obj, project, prospectiveEmployer, null);
 	}
 	/*.................................................................................................................*/
 	public boolean isCompatible(Object obj, MesquiteProject project, EmployerEmployee prospectiveEmployer, MesquiteString report){
 		if (obj instanceof ModelCompatibilityInfo) {
 
 			Class targetModelSubclass = ((ModelCompatibilityInfo)obj).targetModelSubclass;
 			Class targetStateClass =  ((ModelCompatibilityInfo)obj).targetStateClass;
			boolean c = (targetModelSubclass.isAssignableFrom(getClass()) && (targetStateClass==null || (allowSubclass && getStateClass().isAssignableFrom(targetStateClass)) || (!allowSubclass && getStateClass() == targetStateClass))); //used to allow "is assignable from"
 			return c && ((ModelCompatibilityInfo)obj).isCompatible(this, project, prospectiveEmployer); 
 		}
 		return true;
 	}
 	/*.................................................................................................................*/
 	/** returns whether changes are equalent in both directions, i.e. a change from 0 to 1 costs as much or
 	is as probably as a change from 1 to 0.  Default is true; subclasses must override.  Helps modules know if
 	rooting of tree matters, for example*/
	public boolean isReversible() {
		return true;
	}
		
 	/*.................................................................................................................*/
	public static CharacterModel chooseExistingCharacterModel(MesquiteModule m, Class modelClass, String explanation){
		Listable[] models = m.getProject().getCharacterModels(modelClass, null);
		if (models ==null || models.length==0)
			return null;
		Listable wh = ListDialog.queryList(m.containerOfModule(), "Choose model", explanation, MesquiteString.helpString, models, 0);
		return (CharacterModel)wh; 
	}
	public static CharacterModel chooseExistingCharacterModel(MesquiteModule m, ModelCompatibilityInfo mci, String explanation){
		Listable[] models = m.getProject().getCharacterModels(mci, null);
		if (models ==null || models.length==0)
			return null;
		String help = "";
		boolean wizardInEffect = CommandRecord.wizardInEffect();
		if (wizardInEffect)
			help = "<h3>Choose Character Model</h3>";
		help += "Please choose a model.";
		
		Listable wh = ListDialog.queryList(m.containerOfModule(), "Choose model", explanation, help, models, 0);
		return (CharacterModel)wh; 
	}
	public static CharacterModel chooseNewCharacterModel(MesquiteModule m, Class modelClass, String explanation){
		if (m == null || modelClass == null)
			return null;
		String help = "";
		boolean wizardInEffect = CommandRecord.wizardInEffect();
		if (wizardInEffect)
			help = "<h3>Choose Character Model</h3>";
		help += "Please choose a model.";
		if (explanation == null)
			explanation = "Choose model";
		MesquiteModule cme = (MesquiteModule)m.findElementManager(CharacterModel.class);
		MesquiteModule[] curators = cme.getImmediateEmployeesWithDuty(CharModelCurator.class);
		if (curators!=null) {
			int count=0;
			for (int i=0; i<curators.length; i++)
				if (modelClass.isAssignableFrom(((CharModelCurator)curators[i]).getModelClass()))
					count++;
			if (count>0){
				String[] s = new String[count];
				count=0;
				for (int i=0; i<curators.length; i++) {
					if (modelClass.isAssignableFrom(((CharModelCurator)curators[i]).getModelClass())) {
						s[count]=((CharModelCurator)curators[i]).getNameOfModelClass();
						count++;
					}
				}
				int choice = ListDialog.queryList(m.containerOfModule(), "Choose model", explanation,help,  s, 0);
				if (MesquiteInteger.isCombinable(choice)) {
					count =0;
					for (int i=0; i<curators.length; i++) {
						if (modelClass.isAssignableFrom(((CharModelCurator)curators[i]).getModelClass())) {
							if (count==choice) {
								String name = "Character Model";
								if (m.getProject()!=null) {
									name = m.getProject().getCharacterModels().getUniqueName(name);
								}
								return ((CharModelCurator)curators[i]).makeNewModel(name);
							}
							count++;
						}
					}
				}
			}
		}
		return null;

	}
	public static MesquiteModule[] findCurators(MesquiteModule m, Class modelClass){
		if (m == null || modelClass == null)
			return null;
		MesquiteModule cme = (MesquiteModule)m.findElementManager(CharacterModel.class);
		if (cme == null)
			return null;
		MesquiteModule[] curators = cme.getImmediateEmployeesWithDuty(CharModelCurator.class);
		if (curators!=null) {
			int count=0;
			for (int i=0; i<curators.length; i++) {
				Class mC = ((CharModelCurator)curators[i]).getModelClass();
				
				if (mC != null && modelClass.isAssignableFrom(mC))
					count++;
			}
			if (count>0){
				MesquiteModule[] s = new MesquiteModule[count];
				count=0;
				for (int i=0; i<curators.length; i++) {
					Class mC = ((CharModelCurator)curators[i]).getModelClass();
					if (mC != null && modelClass.isAssignableFrom(mC)) {
						s[count]=curators[i];
						count++;
					}
				}
				return s;
			}
		}
		return null;

	}

}


