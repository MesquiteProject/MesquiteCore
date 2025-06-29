/* Mesquite source code (Rhetenor package).  Copyright 1997 and onward E. Dyreson and W. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.rhetenor.MatricesFromOrdinations;
/*~~  */

import mesquite.cont.lib.ContinuousData;
import mesquite.cont.lib.ContinuousState;
import mesquite.cont.lib.ContinuousStateTest;
import mesquite.cont.lib.MContinuousAdjustable;
import mesquite.cont.lib.MContinuousDistribution;
import mesquite.cont.lib.MContinuousStates;
import mesquite.lib.CommandChecker;
import mesquite.lib.CompatibilityTest;
import mesquite.lib.Double2DArray;
import mesquite.lib.EmployeeNeed;
import mesquite.lib.MesquiteCommand;
import mesquite.lib.MesquiteDouble;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteString;
import mesquite.lib.MesquiteThread;
import mesquite.lib.Snapshot;
import mesquite.lib.StringUtil;
import mesquite.lib.characters.MCharactersDistribution;
import mesquite.lib.duties.CharMatrixSource;
import mesquite.lib.duties.MatrixSourceCoord;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.tree.Tree;
import mesquite.lib.ui.MesquiteSubmenuSpec;
import mesquite.rhetenor.lib.Ordination;
import mesquite.rhetenor.lib.OrdinationAssistant;
import mesquite.rhetenor.lib.Ordinator;

/* ======================================================================== */
/** this is a quick and dirty intermediary module which uses Ordinators to supply new character matrices.  It currently
doesn't allow you to choose the source matrix, or to choose which Ordinator is used.  */

public class MatricesFromOrdinations extends CharMatrixSource {
	public String getName() {
		return "Matrices from Ordinations";
	}
	public String getExplanation() {
		return "Supplies character matrices from ordinations of existing matrices." ;
	}

	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(MatrixSourceCoord.class, getName() + "  needs a source of matrices.",
		"The source of matrices can be selected initially");
		EmployeeNeed e2 = registerEmployeeNeed(Ordinator.class, getName() + "  needs a method to ordinate matrices.",
		"The method to ordinate matrices can be selected initially or in the Ordination submenu");
	}
	MatrixSourceCoord dataTask;
	Ordinator ordTask;
	MesquiteString ordinatorName;
	Ordination ord;
	Taxa oldTaxa;
	MesquiteCommand  otC;
	MContinuousDistribution originalMatrix = null;
	int currentItem = 0;
	String itemString = "";
	/*.................................................................................................................*/
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) { //todo: if tries to hire for non-continous, reject
		if (condition!=null && condition!= ContinuousData.class && condition!=ContinuousState.class){
			return sorry("Matrices from Ordinations cannot be used because it supplies only continuous-valued characters");
		}
		ordinatorName = new MesquiteString();
		otC = makeCommand("setOrdinator",  this);

		dataTask = (MatrixSourceCoord)hireCompatibleEmployee(MatrixSourceCoord.class, ContinuousState.class, "Source of matrices to be ordinated");
		if (dataTask == null) 
			return sorry("Matrices from ordination could not start because not source of characters was obtained");

		if (!MesquiteThread.isScripting()) {
			ordTask = (Ordinator)hireEmployee( Ordinator.class, "Ordination ( for " + employer.getName() + ")");
			if (ordTask == null) {
				return sorry("Matrices from Ordinations cannot be used because no ordinator module was hired successfully");
			}
			ordTask.setHiringCommand(otC);
		}
		if (numModulesAvailable(Ordinator.class)>1){
			MesquiteSubmenuSpec mss = addSubmenu(null, "Ordination ( for " + employer.getName() + ")", otC, Ordinator.class);
			mss.setSelected(ordinatorName);
		}
		addMenuItem("Item for Ordination...", MesquiteModule.makeCommand("setItem",  this));
		if (numModulesAvailable(OrdinationAssistant.class)>0) {
			addMenuSeparator();
			addModuleMenuItems(null, makeCommand("hireAssistant",  this), OrdinationAssistant.class);
			addMenuSeparator();
		}
		return true; 
	}
	/*.................................................................................................................*/
	/** Generated by an employee who quit.  The MesquiteModule should act accordingly. */
	public void employeeQuit(MesquiteModule employee) {
		iQuit();
	}

	/*.................................................................................................................*/
	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Taxa taxa){
		oldTaxa = taxa;
		if (dataTask!=null)
			dataTask.initialize(taxa);
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("getCharacterSource ", dataTask);
		temp.addLine("setOrdinator ", ordTask);
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
		if (checker.compare(this.getClass(), "Returns source of matrices for ordination", null, commandName, "getCharacterSource")) {
			return dataTask;
		}
		else if (checker.compare(this.getClass(), "Returns the source of matrices on which to do ordinations", null, commandName, "setCharacterSource")) { //TEMPORARY while old files exist
			if (dataTask != null)
				return dataTask.doCommand(commandName, arguments, checker);
		}
		else if (checker.compare(this.getClass(), "Sets the module that performs the ordinations", "[name of module]", commandName, "setOrdinator")) {
			Ordinator newOrdTask=  (Ordinator)replaceEmployee(Ordinator.class, arguments,"Ordinator", ordTask);
			if (newOrdTask!=null) {
				ordTask = newOrdTask;
				ordTask.setHiringCommand(otC);
				ordinatorName.setValue(ordTask.getName());
				parametersChanged(); //?
				return ordTask;
			}
			else {
				discreetAlert( "Unable to activate character source \"" + arguments + "\"  for use by " + getName());
			}
		}
		else if (checker.compare(this.getClass(), "Sets the item to use (in a multi-item continuous data matrix)", "[item number]", commandName, "setItem")) {
			int ic = MesquiteInteger.fromString(arguments, new MesquiteInteger(0));
			if (!MesquiteInteger.isCombinable(ic) && originalMatrix!=null){
				ic = originalMatrix.userQueryItem("Select item for Matrices from Ordination", this);
			}
			if (!MesquiteInteger.isCombinable(ic))
				return null;
			if (originalMatrix==null) {
				currentItem = ic;
			}
			else if (originalMatrix !=null) {
				if ((ic>=0) && (ic<=originalMatrix.getNumItems()-1)) {
					currentItem = ic;
					parametersChanged();
				}
			}
		}
		else if (checker.compare(this.getClass(), "Hires an ordination assistant module", "name of module", commandName, "hireAssistant")) {
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
	private MCharactersDistribution getM(Taxa taxa, Tree tree){
		if (dataTask==null)
			return null;
		if (ordTask==null) {
			if (MesquiteThread.isScripting())
				return null;
			ordTask = (Ordinator)hireEmployee( Ordinator.class, "Ordination ( for " + employer.getName() + ")");
			ordTask.setHiringCommand(otC);
			if (ordTask == null)
				return null;
		}
		oldTaxa =taxa;

		MCharactersDistribution input;
		if (tree==null)
			input = dataTask.getCurrentMatrix(taxa); 
		else
			input = dataTask.getCurrentMatrix(tree); 

		if (input==null || !(input instanceof MContinuousDistribution))
			return null;
		originalMatrix = (MContinuousDistribution)input;
		if (currentItem<0 || currentItem>= originalMatrix.getNumItems()) {
			discreetAlert( "Request to use item that doesn't exist for ordination.  Item to be used will be reset to 0.");
			currentItem = 0;
		}
		if (originalMatrix.getNumItems()>1){
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
			return null;
		}
		ord = ordTask.getOrdination(originalMatrix, currentItem, taxa);
		if (ord==null)
			return null;
		for (int i = 0; i<getNumberOfEmployees(); i++) {
			Object e=getEmployeeVector().elementAt(i);
			if (e instanceof OrdinationAssistant) {
				((OrdinationAssistant)e).setOrdination(ord, taxa, dataTask);
			}
		}
		MContinuousAdjustable transformedMatrix = new MContinuousAdjustable(taxa); //making an empty matrix to be filled
		if (originalMatrix.getName()!=null)
			transformedMatrix.setName( "Ordination " + ordTask.getName() + " of  matrix " + originalMatrix.getName());
		else
			transformedMatrix.setName( "Ordination " + ordTask.getName() + " of  unknown matrix ");
		transformedMatrix.setStates(new Double2DArray(ord.getScores()));
		return  transformedMatrix;
	}
	/*.................................................................................................................*/
	public String getMatrixName(Taxa taxa, int ic) {
		return "Ordination " + ordTask.getName() + " of  matrix ";
	}
	/*.................................................................................................................*/
	public MCharactersDistribution getCurrentMatrix(Taxa taxa){
		return getM(taxa, null);
	}
	/*.................................................................................................................*/
	public MCharactersDistribution getMatrix(Taxa taxa, int im){
		return getM(taxa, null);
	}
	/*.................................................................................................................*/
	public  int getNumberOfMatrices(Taxa taxa){
		return 1; 
	}
	public boolean usesTree(){
		if (dataTask==null)
			return false;
		else
			return dataTask.usesTree();
	}
	/*.................................................................................................................*/
	public MCharactersDistribution getCurrentMatrix(Tree tree){
		if (tree==null)
			return null;
		return getM(tree.getTaxa(), tree);
	}
	/*.................................................................................................................*/
	public MCharactersDistribution getMatrix(Tree tree, int im){
		if (tree==null)
			return null;
		return getM(tree.getTaxa(), tree);
	}
	/*.................................................................................................................*/
	public  int getNumberOfMatrices(Tree tree){
		return 1; 
	}
	/*.................................................................................................................*/
	/** returns the number of the current matrix*/
	public int getNumberCurrentMatrix(){
		return 0;
	}
	/*.................................................................................................................*/
	public String getParameters() { 
		if (ordTask==null || dataTask==null)
			return "";
		return "Ordination " + ordTask.getName() + " of  matrix from " + dataTask.getName() + itemString;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	/*.................................................................................................................*/
	public CompatibilityTest getCompatibilityTest() {
		return new ContinuousStateTest();
	}
	public boolean isPrerelease(){
		return false;
	}
}

