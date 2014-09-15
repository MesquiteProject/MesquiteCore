/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.charMatrices.ManageCharModels;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/** Manages models of character evolution, including coordinating file reading and writing*/
public class ManageCharModels extends FileInit implements ElementManager {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(CharModelCurator.class, getName() + " needs curators to create and edit the various types of character models.",
				"You can make new or edit previously made character models using the menu items in the Characters menu.");
		e.setAlternativeEmployerLabel("Editors for character models");
	}
	ModelNamesLister modelNames, submodelNames;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		hireAllEmployees(CharModelCurator.class);
		
 		return true;
	}
	public void elementsReordered(ListableVector v){
	}
/*.................................................................................................................*/
   	 public boolean isSubstantive(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
	/*.................................................................................................................*/
	public MesquiteModule showElement(FileElement e){
		if (e instanceof CharacterModel){
			CharacterModel t = (CharacterModel)e;
			CharModelCurator curator = findReader(t.getClass());
			if (curator !=null)
		   		curator.showEditor(t);
		}
		return null;
	}
	/*.................................................................................................................*/
	public void deleteElement(FileElement e){
		if (e instanceof CharacterModel){
			CharacterModel t = (CharacterModel)e;
			t.doom();
			getProject().removeFileElement(t);//must remove first, before disposing
			t.dispose();
		}
	}
	public void endJob(){
		if (submodelNames !=null)
			submodelNames.dispose();
		submodelNames = null;
		if (modelNames !=null)
			modelNames.dispose();
		modelNames = null;
		super.endJob();
	}
	public NexusBlock elementAdded(FileElement e){
		return null;
	}
	public void elementDisposed(FileElement e){
		//nothing needs doing since separate reference not stored locally
	}
	public Class getElementClass(){
		return CharacterModel.class;
	}
	public void projectEstablished() {
		getFileCoordinator().addMenuItem(MesquiteTrunk.charactersMenu, "List of Character Models", makeCommand("showModels",  this));
		modelNames = new ModelNamesLister(getProject(), WholeCharacterModel.class);
		MesquiteSubmenuSpec ncm = getFileCoordinator().addSubmenu(MesquiteTrunk.charactersMenu,"New Character Model", makeCommand("newModel", this), WholeCharModelCurator.class);
		ncm.setListableFilter(EditingCurator.class);
		getFileCoordinator().addSubmenu(MesquiteTrunk.charactersMenu, "Edit Character Model", makeCommand("editModel", this), modelNames);
		submodelNames = new ModelNamesLister(getProject(), CharacterSubmodel.class);
		ncm = getFileCoordinator().addSubmenu(MesquiteTrunk.charactersMenu,"New Character Submodel", makeCommand("newSubModel", this), CharSubmodelCurator.class);
		ncm.setListableFilter(EditingCurator.class);
		getFileCoordinator().addSubmenu(MesquiteTrunk.charactersMenu, "Edit Character Submodel", makeCommand("editSubmodel", this), submodelNames);
		MesquiteSubmenuSpec mset = getFileCoordinator().addSubmenu(MesquiteTrunk.charactersMenu,"Model Settings", makeCommand("modelSettings", this), WholeCharModelCurator.class);
		mset.setListableFilter(CuratorWithSettings.class);
		getFileCoordinator().addMenuItem(MesquiteTrunk.charactersMenu,"-", null);
		super.projectEstablished();
	}
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) { 
   	 	Snapshot temp = new Snapshot();
		for (int i = 0; i<getNumberOfEmployees(); i++) {
			MesquiteModule e=(MesquiteModule)getEmployeeVector().elementAt(i);
			if (e instanceof ManagerAssistant && (e.getModuleWindow()!=null) && e.getModuleWindow().isVisible()) {
  	 				temp.addLine("showModels ", e); 
  	 		}
		}
  	 	return temp;
  	 }
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Edits the character model", "[name of character model]", commandName, "editModel")) {
    	 		String name = parser.getFirstToken(arguments);
    	 		int num = MesquiteInteger.fromString(parser.getNextToken());
			CharacterModel model = null;
    	 		if (MesquiteInteger.isCombinable(num))
    	 			model = getProject().getCharacterModel(WholeCharacterModel.class, false, num);
			if (model == null)
				model = getProject().getCharacterModel(name);
			if (model !=null) {
				CharModelCurator curator = findReader(model.getClass());
				if (curator==null)
					alert("Sorry, no curator module was found for that sort of character model (category: " + model.getTypeName() + ")");
				else 
					curator.showEditor(model);
			}
			return model;
		}
    	 	else if (checker.compare(this.getClass(), "Edits the character submodel", "[name of character submodel]", commandName, "editSubmodel")) {
    	 		String name = parser.getFirstToken(arguments);
    	 		int num = MesquiteInteger.fromString(parser.getNextToken());
			CharacterModel model = null;
    	 		if (MesquiteInteger.isCombinable(num))
    	 			model = getProject().getCharacterModel(CharacterSubmodel.class, false, num);
			if (model == null)
				model = getProject().getCharacterModel(name);
			if (model !=null) {
				CharModelCurator curator = findReader(model.getClass());
				if (curator==null)
					alert("Sorry, no curator module was found for that sort of character model (category: " + model.getTypeName() + ")");
				else 
					curator.showEditor(model);
			}
			return model;
		}
    	 	else if (checker.compare(this.getClass(), "Shows a list of the available character models", null, commandName, "showModels")) {
    	 		//Check to see if already has lister for this
    	 		boolean found = false;
			for (int i = 0; i<getNumberOfEmployees(); i++) {
				Object e=getEmployeeVector().elementAt(i);
				if (e instanceof ManagerAssistant)
					if (((ManagerAssistant)e).getName().equals("Character Models List")) {
						((ManagerAssistant)e).getModuleWindow().setVisible(true);
						return e;
					}
			}
			ManagerAssistant lister= (ManagerAssistant)hireNamedEmployee(ManagerAssistant.class, StringUtil.tokenize("Character Models List"));
 			if (lister==null){
 				alert("Sorry, no module was found to present a list of character models");
 				return null;
 			}
 			lister.showListWindow(null);
 			if (!MesquiteThread.isScripting() && lister.getModuleWindow()!=null)
 				lister.getModuleWindow().setVisible(true);
 			return lister;
   	 	}
    	 	else if (checker.compare(this.getClass(), "Makes a new character model", "[Class of model]", commandName, "newModel")) {
			CharModelCurator curator= (CharModelCurator)findEmployee(parser.getFirstToken(arguments));
			if (curator !=null) {
				String name = MesquiteString.queryShortString(containerOfModule(), "New "+curator.getName(), "Name of new "+curator.getName()+":", getProject().getCharacterModels().getUniqueName(curator.getName()));
				if (name==null)
					return null;
				CharacterModel model = curator.makeNewModel(name);
		   		model.addToFile(null, getProject(), null); //TODO:ok to add to homefile, or query user?
		   		curator.showEditor(model);
		   		if (model.getEditCancel()) {
		   			curator.disposeModel(model);
		   			return null;
		   		}
				resetAllMenuBars();
				return model;
			}
		}
    	 	else if (checker.compare(this.getClass(), "Makes a new character submodel", "[Class of model]", commandName, "newSubmodel")) {
			CharModelCurator curator= (CharModelCurator)findEmployee(parser.getFirstToken(arguments));
			if (curator !=null) {
				String name = MesquiteString.queryShortString(containerOfModule(), "New "+curator.getName(), "Name of new "+curator.getName()+":", getProject().getCharacterModels().getUniqueName(curator.getName()));
				if (name==null)
					return null;
				CharacterModel model = curator.makeNewModel(name);
		   		model.addToFile(null, getProject(), null); //TODO:ok to add to homefile, or query user?
		   		curator.showEditor(model);
		   		if (model.getEditCancel()) {
		   			curator.disposeModel(model);
		   			return null;
		   		}
				resetAllMenuBars();
				return model;
			}
		}
    	 	else if (checker.compare(this.getClass(), "Presents means to edit the universal settings for a type of model (e.g., settings for optimization)", "[Class of model]", commandName, "modelSettings")) {
			CharModelCurator curator= (CharModelCurator)findEmployee(parser.getFirstToken(arguments));
			if (curator !=null && curator instanceof CuratorWithSettings) {
				((CuratorWithSettings)curator).editSettings();
			}
		}
    	 	else
 			return  super.doCommand(commandName, arguments, checker);
		return null;
   	 }
	/*.................................................................................................................*/
	public String getNexusCommands(MesquiteFile file, String blockName){ 
		if (blockName.equalsIgnoreCase("MESQUITECHARMODELS")) {
			String s= "";
			Enumeration enumeration = file.getProject().getCharacterModels().elements();
			while (enumeration.hasMoreElements()){   // write the submodels
				Object obj = enumeration.nextElement();
				CharacterModel cm = (CharacterModel)obj;
				if (!cm.isBuiltIn() && !("USERTYPE".equalsIgnoreCase(cm.getNEXUSCommand())) &&cm.getFile()==file && cm instanceof CharacterSubmodel) {
					s += "\t"+ cm.getNEXUSCommand() + " ";  
					s += StringUtil.tokenize(cm.getName()) + " (" ;  
					s += StringUtil.tokenize(cm.getNEXUSClassName()) + ") = " + StringUtil.lineEnding();
					s += "\t\t"+ cm.getNexusSpecification()+";" + StringUtil.lineEnding(); 
				}
				//else if (!cm.isBuiltIn() && cm.getFile()!=file)
				//	MesquiteMessage.println("Character model (" + cm.getName() + ") not in file");				
			}
			enumeration = file.getProject().getCharacterModels().elements();
			while (enumeration.hasMoreElements()){   // write everything else
				Object obj = enumeration.nextElement();
				CharacterModel cm = (CharacterModel)obj;
				if (!cm.isBuiltIn() && !("USERTYPE".equalsIgnoreCase(cm.getNEXUSCommand())) &&cm.getFile()==file && (!( cm instanceof CharacterSubmodel))) {
					s += "\t"+ cm.getNEXUSCommand() + " ";  
					s += StringUtil.tokenize(cm.getName()) + " (" ;  
					s += StringUtil.tokenize(cm.getNEXUSClassName()) + ") = " + StringUtil.lineEnding();
					s += "\t\t"+ cm.getNexusSpecification()+";" + StringUtil.lineEnding(); 
				}
				//else if (!cm.isBuiltIn() && cm.getFile()!=file)
				//	MesquiteMessage.println("Character model (" + cm.getName() + ") not in file");				
			}
			return s;
		}
		else if (blockName.equalsIgnoreCase("ASSUMPTIONS")) {
			String s= "";
			Enumeration enumeration = file.getProject().getCharacterModels().elements();
			while (enumeration.hasMoreElements()){
				Object obj = enumeration.nextElement();
				CharacterModel cm = (CharacterModel)obj;
				if (!cm.isBuiltIn() && "USERTYPE".equalsIgnoreCase(cm.getNEXUSCommand()) && cm.getFile()==file) {
					s += "\t"+ cm.getNEXUSCommand() + " ";  
					s += StringUtil.tokenize(cm.getName()) + " (" ;  
					s += StringUtil.tokenize(cm.getNEXUSClassName()) + ") = " + StringUtil.lineEnding();
					s += "\t\t"+ cm.getNexusSpecification()+";" + StringUtil.lineEnding(); 
				}
				//else if (!cm.isBuiltIn() && cm.getFile()!=file)
				//	MesquiteMessage.println("Character model (" + cm.getName() + ") not in file.");				
			}
			return s;
		}
		return null;
	}
	public CharModelCurator findReader(Class modelType) {
		CharModelCurator readerTask=null;
		for (int i = 0; i<getNumberOfEmployees() && readerTask==null; i++) {
			Object e=getEmployeeVector().elementAt(i);
			if (e instanceof CharModelCurator)
				if (((CharModelCurator)e).getModelClass().isAssignableFrom(modelType)) {
					readerTask=(CharModelCurator)e;
				}
		}
		return readerTask;
	}
	public CharModelCurator findReader(String modelType) {
		CharModelCurator readerTask=null;
		for (int i = 0; i<getNumberOfEmployees() && readerTask==null; i++) {
			Object e=getEmployeeVector().elementAt(i);
			if (e instanceof CharModelCurator)
				if (((CharModelCurator)e).getNEXUSNameOfModelClass().equalsIgnoreCase(modelType)) {
					readerTask=(CharModelCurator)e;
				}
		}
		return readerTask;
	}
	/*.................................................................................................................*/
	public boolean readNexusCommand(MesquiteFile file, NexusBlock nBlock, String blockName, String command, MesquiteString comment){ 
		if (blockName.equalsIgnoreCase("ASSUMPTIONS") || blockName.equalsIgnoreCase("MESQUITECHARMODELS")) {
			MesquiteInteger startCharT = new MesquiteInteger(0);
			int format;
			if (blockName.equalsIgnoreCase("MESQUITECHARMODELS"))
				format = CharacterModel.MesquiteNEXUSFormat;
			else
				format = CharacterModel.NEXUSFormat;
			String commandName = ParseUtil.getToken(command, startCharT);
			if (commandName.equalsIgnoreCase("USERTYPE") || commandName.equalsIgnoreCase("CHARMODEL")) {
				String token = ParseUtil.getToken(command, startCharT);
				String nameOfModel = (token); // name of model
				token = ParseUtil.getToken(command, startCharT); //parenthesis
				String nameOfModelClass = "";
				if ("(".equalsIgnoreCase(token) || commandName.equalsIgnoreCase("CHARMODEL")) {
					nameOfModelClass = ParseUtil.getToken(command, startCharT); //name of model class
					token = ParseUtil.getToken(command, startCharT); //parenthesis
					token = ParseUtil.getToken(command, startCharT); //=
				}
				else {
					nameOfModelClass = "Stepmatrix";
					if (!"=".equalsIgnoreCase(token))
						token = ParseUtil.getToken(command, startCharT); //=
				}
				CharModelCurator readerTask = findReader(nameOfModelClass);
				if (readerTask!=null){
					CharacterModel model = readerTask.readCharacterModel(nameOfModel, startCharT, command, format);
		   			if (model!=null) {
		   				model.addToFile(file, getProject(), this);
		   				if (comment !=null &&  !comment.isBlank()) {
		   					String s = comment.toString();
		   					model.setAnnotation(s.substring(1, s.length()), false);
		   				}
		   				return true;
		   			}
		   		}
		   		else {
		   			
					String specification = StringUtil.stripLeadingWhitespace(command.substring(startCharT.getValue(), command.length()-1)); //strip leading added 22 Dec 01
		   			ForeignModel model = new ForeignModel(nameOfModel, nameOfModelClass, commandName, specification);
		   			model.addToFile(file, getProject(), this);
		   			return true;
		   		}
			}
		}
		return false;
	}
	/*.................................................................................................................*/
	public NexusCommandTest getNexusCommandTest(){ 
		return new CharModelsNexusCmdTest();
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Manage Character Models";
   	 }
   	 
	/*.................................................................................................................*/
   	 
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Manages character models." ;
   	 }
	/*.................................................................................................................*/
   	 
}
/*======================================*/
class CharModelsNexusCmdTest  extends NexusCommandTest{
	public boolean readsWritesCommand(String blockName, String commandName, String command){  //returns whether or not can deal with command
		return ((blockName.equalsIgnoreCase("ASSUMPTIONS")||blockName.equalsIgnoreCase("MESQUITECHARMODELS")) && (commandName.equalsIgnoreCase("USERTYPE")|| commandName.equalsIgnoreCase("CHARMODEL")));
	}
}

/*======================================*/
class ModelNamesLister implements StringLister{
	MesquiteProject proj;
	Class subclass;
	public ModelNamesLister(MesquiteProject proj, Class subclass){
		this.proj = proj;
		this.subclass = subclass;
	}
	private boolean correctSubclass(CharacterModel c){
		if (subclass == null)
			return true;
		else return (subclass == CharacterModel.class || subclass.isAssignableFrom(c.getClass()));
	}
	public String[] getStrings() {
		int numModels = 0;
		for (int i=0; i<proj.getNumModels(); i++) {
			if (!proj.getCharacterModel(i).isBuiltIn() && correctSubclass(proj.getCharacterModel(i)))
				numModels++;
		}
		String[] result = new String[numModels];
		for (int i=0; i<numModels; i++) {
			result[i]= "test";
		}
		int modelNum=0;
		for (int i=0; i<proj.getNumModels(); i++) {
			if (!proj.getCharacterModel(i).isBuiltIn() && correctSubclass(proj.getCharacterModel(i))) {
				result[modelNum]=proj.getCharacterModel(i).getName();
				modelNum++;
			}
		}
		return result;
	}
	public void dispose(){
		proj = null;
	}
}

/*======================================*/
class ForeignModel extends CharacterModel{
	String command;
	String commandName;
	String NEXUSClassName;
	public ForeignModel(String name, String NEXUSClassName, String commandName, String command){
		super(name, CharacterState.class);
		this.command = command;
		this.commandName = commandName;
		this.NEXUSClassName = NEXUSClassName;
		setBuiltIn(false);
	}
	public String getNEXUSName(){
		return getName();
	}
	
	public String getNEXUSCommand() {
		return commandName;
	}
	public String getModelTypeName() {
		return "Unrecognized";
	}
	public String getNEXUSClassName(){
		return NEXUSClassName;
	}
	public String getParadigm(){
		return "unknown";
	}
	public String getNexusSpecification(){
		return command;
	}
	/** return an explanation of the model. */
	public String getExplanation (){
		return "A unidentified character model (no module available to read or process it)";
	}
}


