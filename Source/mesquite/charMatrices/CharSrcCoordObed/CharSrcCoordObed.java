/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charMatrices.CharSrcCoordObed;
/*~~  */

import java.awt.Color;

import mesquite.lib.CommandChecker;
import mesquite.lib.EmployeeNeed;
import mesquite.lib.MesquiteCommand;
import mesquite.lib.MesquiteDouble;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteModuleInfo;
import mesquite.lib.MesquiteString;
import mesquite.lib.MesquiteThread;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.NameHolder;
import mesquite.lib.Selectionable;
import mesquite.lib.Snapshot;
import mesquite.lib.characters.CharacterDistribution;
import mesquite.lib.duties.CharSourceCoordObed;
import mesquite.lib.duties.CharacterObedSource;
import mesquite.lib.duties.CharacterSource;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.ui.MesquiteDialog;
import mesquite.lib.ui.MesquiteSubmenuSpec;

public class CharSrcCoordObed extends CharSourceCoordObed implements NameHolder {
	public String getName() {
		return "Character Source";  
	}
	public String getExplanation() {
		return "Coordinates the supply of characters from various sources of characters." ;
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(CharacterObedSource.class, getName() + " needs a source of characters.",
				"You can request a source of characters when " + getName() + " starts, or later under the Source of Characters submenu.");
		e.setSuppressListing(true);
		//e.setAsEntryPoint(true);
	}
	public EmployeeNeed findEmployeeNeed(Class dutyClass) {
		return getEmployer().findEmployeeNeed(CharSourceCoordObed.class);
	}

	/*.................................................................................................................*/
	CharacterObedSource characterSourceTask;
	MesquiteString charSourceName;
	MesquiteCommand cstC;
	Object hiringCondition;
	MesquiteSubmenuSpec mss;
	/*.................................................................................................................*/
	/** condition passed to this module must be subclass of CharacterState */
	public boolean startJob(String arguments, Object condition, boolean hiredByName) { 
		hiringCondition = condition;
		String exp, mexp;
		if (getExplanationByWhichHired()!=null) {
			exp = getExplanationByWhichHired();
			mexp = exp;
		}
		else {
			exp = "Source of Characters  (for " + getEmployer().getName() + ")";
			mexp =  exp;
		}
		MesquiteModuleInfo mmi = MesquiteTrunk.mesquiteModulesInfoVector.findModule(mesquite.charMatrices.StoredCharacters.StoredCharacters.class);
		if (mmi != null && !mmi.isCompatible(condition, getProject(), this)) {
			if (!MesquiteDialog.useWizards){
				exp += "\n\nNOTE: The choice Stored Characters does not appear because there are no appropriate matrices currently defined and stored in the data file or project.  ";
			}
		}
		else if (CharacterSource.useStoredAsDefault()){
			characterSourceTask = (CharacterObedSource)hireNamedEmployee(CharacterObedSource.class, "#mesquite.charMatrices.StoredCharacters.StoredCharacters", condition);
		}
		if (characterSourceTask == null){
			if (arguments == null)
				arguments = MesquiteThread.retrieveAndDeleteHint(this);
			if (arguments != null)
				characterSourceTask = (CharacterObedSource)hireNamedEmployee(CharacterObedSource.class, arguments, condition);
			if (characterSourceTask == null){
				if (condition!=null)
					characterSourceTask = (CharacterObedSource)hireCompatibleEmployee(CharacterObedSource.class, condition, exp);
				else
					characterSourceTask = (CharacterObedSource)hireEmployee(CharacterObedSource.class, exp);
			}
		}
		if (characterSourceTask == null)
			return sorry(getName() + " couldn't start because no source of characters was obtained.");
		charSourceName = new MesquiteString(characterSourceTask.getName());
		cstC = makeCommand("setCharacterSource",  this);
		characterSourceTask.setHiringCommand(cstC);
		if (numModulesAvailable(CharacterObedSource.class)>1){
			mss = addSubmenu(null, mexp, cstC, CharacterObedSource.class);
			mss.setNameHolder(this);
			if (condition!=null)
				mss.setCompatibilityCheck(hiringCondition);
			mss.setSelected(charSourceName);
		}
		return true;
	}
	public String getMyName(Object obj){
		if (obj == mss)
			return "Source of Characters (" + whatIsMyPurpose() + ")";
		return null;
	}
	public void setHiringCondition(Object obj){
		hiringCondition = obj;
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
	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Taxa taxa){
		characterSourceTask.initialize(taxa);
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("setCharacterSource", characterSourceTask);
		return temp;
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
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets module supplying characters", "[name of module]", commandName, "setCharacterSource")) {
			CharacterObedSource newCharacterSourceTask;
			if (hiringCondition!=null)
				newCharacterSourceTask =  (CharacterObedSource)replaceCompatibleEmployee(CharacterObedSource.class, arguments, characterSourceTask, hiringCondition);//, "Source of characters"
			else
				newCharacterSourceTask =  (CharacterObedSource)replaceEmployee(CharacterObedSource.class, arguments, "Source of characters", characterSourceTask);
			if (newCharacterSourceTask!=null) {
				characterSourceTask = newCharacterSourceTask;
				characterSourceTask.setHiringCommand(cstC);
				charSourceName.setValue(characterSourceTask.getName());
				resetContainingMenuBar();
				parametersChanged(); 
				return characterSourceTask;
			}
			else {
				discreetAlert( "Unable to activate character source \"" + arguments + "\"  for use by " + employer.getName());
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
	public  int getNumberOfCharacters(Taxa taxa){
		CharacterObedSource oldSource = characterSourceTask;
		int num = characterSourceTask.getNumberOfCharacters(taxa);
		if (oldSource == characterSourceTask) //i.e., old source hasn't quit and been replaced during above call!
			return num;
		return characterSourceTask.getNumberOfCharacters(taxa); 
	}
	/*.................................................................................................................*/
	public String getCharacterName(Taxa taxa, int ic) {
		CharacterObedSource oldSource = characterSourceTask;
		String name = characterSourceTask.getCharacterName(taxa, ic);
		if (oldSource == characterSourceTask) //i.e., old source hasn't quit and been replaced during above call!
			return name;
		return characterSourceTask.getCharacterName(taxa, ic); 
	}
	/*.................................................................................................................*/
	public  CharacterDistribution getCharacter(Taxa taxa, int ic){
		CharacterObedSource oldSource = characterSourceTask;
		CharacterDistribution matrix = characterSourceTask.getCharacter(taxa, ic);
		if (oldSource == characterSourceTask) //i.e., old source hasn't quit and been replaced during above call!
			return matrix;
		return characterSourceTask.getCharacter(taxa, ic); 
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/

	public String getParameters() {
		if (characterSourceTask==null)
			return null;
		return characterSourceTask.getName() + " (" + characterSourceTask.getParameters() + ")";
	}
	public String getNameAndParameters() {
		return characterSourceTask.getNameAndParameters();
	}
}


