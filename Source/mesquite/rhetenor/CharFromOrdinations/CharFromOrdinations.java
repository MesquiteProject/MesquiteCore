/* Mesquite source code (Rhetenor package).  Copyright 1997 and onward E. Dyreson and W. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.rhetenor.CharFromOrdinations;
/*~~  */

import mesquite.cont.lib.ContinuousState;
import mesquite.cont.lib.ContinuousStateTest;
import mesquite.cont.lib.MContinuousAdjustable;
import mesquite.cont.lib.MContinuousDistribution;
import mesquite.cont.lib.MContinuousStates;
import mesquite.lib.CommandChecker;
import mesquite.lib.CompatibilityTest;
import mesquite.lib.Double2DArray;
import mesquite.lib.EmployeeNeed;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteCommand;
import mesquite.lib.MesquiteDouble;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteString;
import mesquite.lib.MesquiteThread;
import mesquite.lib.Notification;
import mesquite.lib.Snapshot;
import mesquite.lib.StringUtil;
import mesquite.lib.characters.CharacterDistribution;
import mesquite.lib.characters.MCharactersDistribution;
import mesquite.lib.duties.CharacterSource;
import mesquite.lib.duties.MatrixSourceCoord;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.tree.Tree;
import mesquite.lib.ui.MesquiteSubmenuSpec;
import mesquite.rhetenor.lib.MatrixUtil;
import mesquite.rhetenor.lib.Ordination;
import mesquite.rhetenor.lib.OrdinationAssistant;
import mesquite.rhetenor.lib.Ordinator;

/* ======================================================================== */
/** this is a quick and dirty intermediary module which uses Ordinators to supply new character matrices.  It currently
doesn't allow you to choose the source matrix, or to choose which Ordinator is used.  */

public class CharFromOrdinations extends CharacterSource {
	public String getName() {
		return "Characters from Ordinations";
	}
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Supplies a character from an ordination of an existing matrix." ;
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(MatrixSourceCoord.class, getName() + "  needs a source of matrices.",
		"The source of matrices can be selected initially");
		EmployeeNeed e2 = registerEmployeeNeed(Ordinator.class, getName() + "  needs a method to ordinate matrices.",
		"The method to ordinate matrices can be selected initially or in the Ordination submenu");
	}
	/*.................................................................................................................*/
	MatrixSourceCoord dataTask;
	Ordinator ordTask;
	Ordination ord;
	Taxa oldTaxa = null;
	long oldTreeID;
	long oldTreeVersion;
	MContinuousDistribution originalMatrix = null;
	MContinuousAdjustable transformedMatrix = null;
	int currentItem = 0;
	String itemString = "";
	int currentChar = 0;
	MesquiteString ordinatorName;
	MesquiteCommand otC;
	/*.................................................................................................................*/
	/** condition passed to this module must be subclass of CharacterState */
	public boolean startJob(String arguments, Object condition, boolean hiredByName) { 
		if (condition !=null && condition instanceof CompatibilityTest)
			condition = ((CompatibilityTest)condition).getAcceptedClass();
		if (condition!=null && !(condition instanceof Class && ContinuousState.class.isAssignableFrom((Class)condition))){
			return sorry("Characters from Ordinations cannot be used because it supplies only continuous-valued characters");
		}
		otC = makeCommand("setOrdinator",  this);
		dataTask = (MatrixSourceCoord)hireCompatibleEmployee(MatrixSourceCoord.class, ContinuousState.class, "Source of matrices to be ordinated");
		if (dataTask == null) {
			return sorry("Can't start Characters from Ordinations because no source of characters found");
		}

		ordinatorName = new MesquiteString();
		if (!MesquiteThread.isScripting()) {
			firstHireOrdinator();
			if (ordTask==null) {
				return sorry("Can't start Characters from Ordinations because no ordinator module was obtained");
			}
		}
		if (numModulesAvailable(Ordinator.class)>1){
			MesquiteSubmenuSpec mss = addSubmenu(null, "Ordination ( for " + employer.getName() + ")", otC, Ordinator.class);
			mss.setSelected(ordinatorName);
		}
		addMenuItem("Item for Ordination...", MesquiteModule.makeCommand("setItem",  this));

		if (numModulesAvailable(OrdinationAssistant.class)>0) {
			addMenuSeparator();
			addModuleMenuItems(null, makeCommand("hireAssistant",  this), OrdinationAssistant.class);  //for assistants like Show Char Loadings
			addMenuSeparator();
		}
		return true; 
	}

	/*.................................................................................................................*/
	/** Generated by an employee who quit.  The MesquiteModule should act accordingly. */
	public void employeeQuit(MesquiteModule employee) {
		if (employee == ordTask || employee == dataTask)
			iQuit();
	}
	private void firstHireOrdinator(){
		ordTask = (Ordinator)hireEmployee( Ordinator.class, "Ordination ( for " + employer.getName() + ")");
		if (ordTask == null) {
			discreetAlert("Can't start Characters from Ordinations because no ordinator found");
			return;
		}
		ordTask.setHiringCommand(otC);
		ordinatorName.setValue(ordTask.getName());
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("getCharacterSource", dataTask);
		temp.addLine("setOrdinator", ordTask);
		temp.addLine("setItem " + (currentItem));
		for (int i = 0; i<getNumberOfEmployees(); i++) {
			Object e=getEmployeeVector().elementAt(i);
			if (e instanceof OrdinationAssistant) {
				temp.addLine("hireAssistant " , ((MesquiteModule)e));
			}
		}
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Returns the source of matrices on which to do ordinations", null, commandName, "setCharacterSource")) { //TEMPORARY while old files exist
			if (dataTask != null)
				return dataTask.doCommand(commandName, arguments, checker);
		}
		else if (checker.compare(this.getClass(), "Returns the source of matrices on which to do ordinations", null, commandName, "getCharacterSource")) {
			return dataTask;
		}
		else if (checker.compare(this.getClass(), "Sets the module performing the ordinations", "[name of module]", commandName, "setOrdinator")) {
			Ordinator newOrdTask=  (Ordinator)replaceEmployee(Ordinator.class, arguments,"Ordinator", ordTask);
			if (newOrdTask!=null) {
				ordTask = newOrdTask;
				ordTask.setHiringCommand(otC);
				ordinatorName.setValue(ordTask.getName());
				originalMatrix = null;
				transformedMatrix = null;
				parametersChanged(); 
				return ordTask;
			}
			else {
				discreetAlert( "Unable to activate character source \"" + arguments + "\"  for use by " + getName());
			}
		}
		else if (checker.compare(this.getClass(), "Sets the item to use (in a multi-item continuous data matrix)", "[item number]", commandName, "setItem")) {
			int ic = MesquiteInteger.fromString(arguments, new MesquiteInteger(0));
			if (!MesquiteInteger.isCombinable(ic) && originalMatrix!=null){
				ic = originalMatrix.userQueryItem("Select item for Characters from Ordination", this);
			}
			if (!MesquiteInteger.isCombinable(ic))
				return null;
			if (originalMatrix==null) {
				currentItem = ic;
			}
			else if (originalMatrix !=null) {
				if ((ic>=0) && (ic<originalMatrix.getNumItems())) {
					currentItem = ic;
					transformedMatrix = null;
					parametersChanged();
				}
			}
		}
		else if (checker.compare(this.getClass(), "Hires an ordination assistant module", "[name of module]", commandName, "hireAssistant")) {
			OrdinationAssistant assistant=  (OrdinationAssistant)hireNamedEmployee(OrdinationAssistant.class, arguments);
			if (assistant!=null) {
				if (ord!=null)
					assistant.setOrdination(ord, oldTaxa, dataTask);
				return assistant;
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public CompatibilityTest getCompatibilityTest() {
		return new ContinuousStateTest();
	}
	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Taxa taxa){
		if (dataTask!=null)
			dataTask.initialize(taxa);
	}
	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		originalMatrix = null;//to force recalculation
		transformedMatrix = null;
		super.employeeParametersChanged(employee, source, notification);
	}
	boolean firstWarning = true;
	private String allCombinable(MContinuousStates originalMatrix, int item){
		for (int ic = 0; ic < originalMatrix.getNumChars(); ic++)
			for (int it = 0; it<originalMatrix.getNumNodes(); it++) {
				if (!MesquiteDouble.isCombinable(originalMatrix.getState(ic, it, item))){
					return "State " + MesquiteDouble.toString(originalMatrix.getState(ic, it, item)) + " taxon " + (it+1) + " character " + (ic+1);
				}
			}
		return null;
	}
	/*.................................................................................................................*/
	private void getM(Taxa taxa, Tree tree){
		originalMatrix = null;
		transformedMatrix = null;
		if (dataTask==null)
			return;
		if (ordTask==null){
			if (MesquiteThread.isScripting())
				return;
			firstHireOrdinator();
			if (ordTask==null)
				return;
		}
		if (tree != null) {
			oldTreeID = tree.getID();
			oldTreeVersion = tree.getVersionNumber();
		}
		MCharactersDistribution input;
		if (tree==null)
			input = dataTask.getCurrentMatrix(taxa); 
		else
			input = dataTask.getCurrentMatrix(tree); 
		if (input==null || !(input instanceof MContinuousDistribution)) {
			return;
		}
		MesquiteBoolean wasStripped = new MesquiteBoolean(false);
		originalMatrix = MatrixUtil.stripExcluded(((MContinuousDistribution)input), wasStripped);  //new after 2. 75
		
		if (currentItem<0 || currentItem>= originalMatrix.getNumItems()) {
			discreetAlert( "Request to use item that doesn't exist for ordination.  Item to be used will be reset to 0.");
			currentItem = 0;
		}
		if (originalMatrix.getNumItems()>1) {
			if (StringUtil.blank(originalMatrix.getItemName(currentItem)))
				itemString = " using item #" + (currentItem+1);
			else
				itemString = " using item " + originalMatrix.getItemName(currentItem);
		}
		else
			itemString = "";
		String response = allCombinable(originalMatrix, currentItem);
		if (response != null) {
			if (firstWarning) {
				discreetAlert( "Matrix to be ordinated has missing data or other illegal values.  Ordination cannot be performed. " + response);
			}
			firstWarning = false;
			return;
		}
		ord = ordTask.getOrdination(originalMatrix, currentItem, taxa);
		if (ord==null) {
			return;
		}
		for (int i = 0; i<getNumberOfEmployees(); i++) {
			Object e=getEmployeeVector().elementAt(i);
			if (e instanceof OrdinationAssistant) {
				((OrdinationAssistant)e).setOrdination(ord, taxa,dataTask);
			}
		}
		transformedMatrix = new MContinuousAdjustable(taxa); //making an empty matrix to be filled
		transformedMatrix.setStates(new Double2DArray(ord.getScores()));
		String tName = "Ordination from " + originalMatrix.getName();
		if (wasStripped.getValue())
			tName += "(excluded characters deleted)";
		transformedMatrix.setName(tName);
	}
	/*.................................................................................................................*/
	private void dataCheck(Taxa taxa, Tree tree) {
		if (taxa==null)
			return;
		if (transformedMatrix == null || originalMatrix==null  || oldTaxa != taxa || (dataTask.usesTree() && tree!=null && (tree.getID()!=oldTreeID || tree.getVersionNumber()!=oldTreeVersion))) {
			originalMatrix = null;
			transformedMatrix = null;
			getM(taxa, tree);
			currentChar = 0;
			oldTaxa = taxa;

		}
	}
	/** returns character numbered ic*/
	public CharacterDistribution getCharacter(Taxa taxa, int ic){
		currentChar= ic;
		dataCheck(taxa, null);
		if (transformedMatrix==null)
			return null;
		return transformedMatrix.getCharacterDistribution(ic);
	}
	/** returns number of characters for given Taxa*/
	public int getNumberOfCharacters(Taxa taxa){
		dataCheck(taxa, null);
		if (originalMatrix==null)
			return 0;
		return originalMatrix.getNumChars();
	}
	public boolean usesTree(){
		if (dataTask==null)
			return false;
		else
			return dataTask.usesTree();
	}
	/** returns character numbered ic*/
	public CharacterDistribution getCharacter(Tree tree, int ic){
		if (tree ==null)
			return null;
		currentChar= ic;
		dataCheck(tree.getTaxa(), tree);
		if (transformedMatrix==null)
			return null;
		return transformedMatrix.getCharacterDistribution(ic);
	}
	/** returns number of characters for given Taxa*/
	public int getNumberOfCharacters(Tree tree){
		if (tree ==null)
			return 0;
		dataCheck(tree.getTaxa(), tree);
		if (originalMatrix==null)
			return 0;
		return originalMatrix.getNumChars();
	}
	/** returns the name of character ic*/
	public String getCharacterName(Taxa taxa, int ic){ //
		return "Axis " + ic;
	}
	/*.................................................................................................................*/
	public String getParameters() {
		if (dataTask==null)
			return "";
		if (ordTask == null)
			return "Matrix source: " + dataTask.getName();
		else {
			return "Ordination: " + ordTask.getName() + ";  Matrix source: " + dataTask.getName();
		}
	}
	/*.................................................................................................................*/
	public String getNameAndParameters() {
		if (ordTask == null)
			return super.getNameAndParameters();
		else 
			return "Ordination: " + ordTask.getNameAndParameters() + ";  Original matrix from: " + dataTask.getNameAndParameters() + itemString;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return false;  
	}
	/*.................................................................................................................*/

	public boolean isPrerelease(){
		return false;
	}

}


