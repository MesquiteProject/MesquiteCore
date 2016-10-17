/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charMatrices.CharMatrixCoordIndep;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;


/* ======================================================================== */
public class CharMatrixCoordIndep extends MatrixSourceCoord  implements NameHolder {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(CharMatrixOneSource.class, getName() + " needs a source of matrices.",
		"You can usually request a source of matrices when the calculation starts, or later under the submenu \"Matrix Source\" or the submenu \"Source of character matrices\".");
		e.setAlternativeEmployerLabel("Calculations making use of character matrices.");
		e.setSuppressListing(true);
		/*		EmployeeNeed e = registerEmployeeNeed(CharMatrixOneSource.class, getName() + " coordinates the supply of character matrices.",
				"You can request a source of matrices when " + getName() + " starts, or later under the submenu \"Matrix Source\" or the submenu \"Source of character matrices\".");
		e.setSuppressListing(true);*/
		//e.setAsEntryPoint(true);
	}
	public EmployeeNeed findEmployeeNeed(Class dutyClass) {
		return getEmployer().findEmployeeNeed(MatrixSourceCoord.class);
	}
	CharMatrixOneSource characterSourceTask;
	MesquiteString charSourceName;
	MesquiteCommand cstC;
	Object hiringCondition;
	MesquiteSubmenuSpec mss;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		this.hiringCondition = condition;
//		pass in arguments the ID of taxon to be used; passed also into 
		String exp, mexp;
		if (getExplanationByWhichHired()!=null) {
			exp = getExplanationByWhichHired();
			mexp = exp;
		}
		else {
			exp = "Source of character matrices  (for " + getEmployer().getName() + ")";
			mexp = "Matrix Source  (for " + getEmployer().getName() + ")";
		}
		MesquiteModuleInfo mmi = MesquiteTrunk.mesquiteModulesInfoVector.findModule(mesquite.charMatrices.StoredCharacters.StoredCharacters.class);
		if (mmi != null && !mmi.isCompatible(condition, getProject(), this)) {
			if (!MesquiteDialog.useWizards){
				exp += "\n\nNOTE: The choice Stored Matrices does not appear because there are no appropriate matrices currently defined and stored in the data file or project.  ";
			}
		}
		else {
			if (!MesquiteThread.isScripting() && CharacterSource.useStoredAsDefault()) //if not scripting, then use stored matrices as default
				characterSourceTask = (CharMatrixOneSource)hireNamedEmployee(CharMatrixOneSource.class, "#mesquite.charMatrices.StoredMatrices.StoredMatrices", condition);
		}
		if (characterSourceTask == null){
			if (condition!=null)
				characterSourceTask = (CharMatrixOneSource)hireCompatibleEmployee(CharMatrixOneSource.class, condition, exp);
			else
				characterSourceTask = (CharMatrixOneSource)hireEmployee(CharMatrixOneSource.class, exp);
		}
		if (characterSourceTask == null)
			return sorry(getName() + " couldn't start because no source of character matrices was obtained.");
		charSourceName = new MesquiteString(characterSourceTask.getName());
		cstC = makeCommand("setCharacterSource",  this);
		if (!CharacterSource.closeIfMatrixDeleted.getValue())
			characterSourceTask.setHiringCommand(cstC);  
		if (numModulesAvailable(CharMatrixOneSource.class)>1){
			 mss = addSubmenu(null, mexp, cstC, CharMatrixOneSource.class);
				mss.setNameHolder(this);
			if (condition!=null)
				mss.setCompatibilityCheck(hiringCondition);
			mss.setSelected(charSourceName);
		}
		return true;
	}
	public String getMyName(Object obj){
		if (obj == mss)
			return "Source of Matrices (" + whatIsMyPurpose() + ")";
		return null;
	}
	/** Returns the purpose for which the employee was hired (e.g., "to reconstruct ancestral states" or "for X axis").*/
	public String purposeOfEmployee(MesquiteModule employee){
		return whatIsMyPurpose(); //transfers info to employer, as ithis is coordinator
	}
	/*.................................................................................................................*/
	/** Generated by an employee who quit.  The MesquiteModule should act accordingly. */
	public void employeeQuit(MesquiteModule employee) {
		if (employee == characterSourceTask)  // character source quit and none rehired automatically
			iQuit();
	}
	public Selectionable getSelectionable() {
		if (characterSourceTask!=null)
			return characterSourceTask.getSelectionable();
		else
			return null;
	}
	public void setEnableWeights(boolean enable){
		if (characterSourceTask!=null)
			characterSourceTask.setEnableWeights(enable);
	}
	public boolean itemsHaveWeights(Taxa taxa){
		if (characterSourceTask!=null)
			return characterSourceTask.itemsHaveWeights(taxa);
		return false;
	}
	public double getItemWeight(Taxa taxa, int ic){
		if (characterSourceTask!=null)
			return characterSourceTask.getItemWeight(taxa, ic);
		return MesquiteDouble.unassigned;
	}
	public void prepareItemColors(Taxa taxa){
		if (characterSourceTask!=null)
			characterSourceTask.prepareItemColors(taxa);
	}
	public Color getItemColor(Taxa taxa, int ic){
		if (characterSourceTask!=null)
			return characterSourceTask.getItemColor(taxa, ic);
		return null;
	}
	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Taxa taxa){
		characterSourceTask.initialize(taxa);
	}
	/*.................................................................................................................*/
	/** gets the current matrix.*/
	public MCharactersDistribution getCurrentMatrix(Taxa taxa){
		CharMatrixOneSource oldSource = characterSourceTask;
		MCharactersDistribution matrix = characterSourceTask.getCurrentMatrix(taxa);
		if (oldSource == characterSourceTask) //i.e., old source hasn't quit and been replaced during above call!
			return matrix;
		return characterSourceTask.getCurrentMatrix(taxa); //try again
	}
	/*.................................................................................................................*/
	public MCharactersDistribution getCurrentMatrix(Tree tree){
		CharMatrixOneSource oldSource = characterSourceTask;
		MCharactersDistribution matrix = characterSourceTask.getCurrentMatrix(tree);
		if (oldSource == characterSourceTask) //i.e., old source hasn't quit and been replaced during above call!
			return matrix;
		return characterSourceTask.getCurrentMatrix(tree); //try again
	}
	/*.................................................................................................................*/
	public String getCurrentMatrixName(Taxa taxa){
		CharMatrixOneSource oldSource = characterSourceTask;
		String name = characterSourceTask.getMatrixName(taxa, characterSourceTask.getNumberCurrentMatrix());
		if (oldSource == characterSourceTask) //i.e., old source hasn't quit and been replaced during above call!
			return name;
		return characterSourceTask.getMatrixName(taxa, characterSourceTask.getNumberCurrentMatrix()); //try again
	}
	public boolean usesTree(){
		if (characterSourceTask == null)
			return false;
		return characterSourceTask.usesTree();
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("setCharacterSource", characterSourceTask);
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets module supplying character matrices", "[name of module]", commandName, "setCharacterSource")) {
			CharMatrixOneSource newCharacterSourceTask;
			if (hiringCondition!=null)
				newCharacterSourceTask =  (CharMatrixOneSource)replaceCompatibleEmployee(CharMatrixOneSource.class, arguments, characterSourceTask, hiringCondition);//, "Source of characters"
			else
				newCharacterSourceTask =  (CharMatrixOneSource)replaceEmployee(CharMatrixOneSource.class, arguments, "Source of character matrices", characterSourceTask);
			if (newCharacterSourceTask!=null) {
				characterSourceTask = newCharacterSourceTask;
				if (!CharacterSource.closeIfMatrixDeleted.getValue())
					characterSourceTask.setHiringCommand(cstC);  
				charSourceName.setValue(characterSourceTask.getName());
				parametersChanged(); 
				return characterSourceTask;
			}
			else {
				discreetAlert( "Unable to activate character matrix source \"" + arguments + "\"  for use by " + employer.getName());
			}
		}
		else if (characterSourceTask!=null) { //todo: temporary, for snapshot conversions
			return characterSourceTask.doCommand(commandName, arguments, checker);
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/*.................................................................................................................*/
	public String getParameters() {
		if (characterSourceTask==null)
			return null;
		return characterSourceTask.getName() + " (" + characterSourceTask.getParameters() +")";
	}
	public String getNameAndParameters() {
		return characterSourceTask.getNameAndParameters();
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Matrix Source (indep.)";
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Coordinates the supply of matrices to other modules.  It acts independently, in that it decides what matrix to supply (e.g., by presenting an interface to choose).";
	}
}

