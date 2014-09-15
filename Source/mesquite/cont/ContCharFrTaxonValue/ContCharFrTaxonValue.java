/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.cont.ContCharFrTaxonValue;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.cont.lib.*;

/* ======================================================================== */
public class ContCharFrTaxonValue extends CharacterSource {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e2 = registerEmployeeNeed(NumberForTaxon.class, getName() + " needs a method to calculate values for the taxa.",
		"You can select the value calculator initially, or in the Values submenu.");
	}
	public NumberForTaxon numberTask;
	MesquiteString numberTaskName;
	MesquiteSubmenuSpec msNT;
	MesquiteCommand ntC;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		numberTask=(NumberForTaxon)hireEmployee(NumberForTaxon.class, "Value to calculate for taxa");
		if (numberTask == null)
			return false;
		ntC = makeCommand("setCalculator",  this);
		numberTask.setHiringCommand(ntC);
		numberTaskName = new MesquiteString(numberTask.getName());
		msNT = addSubmenu(null, "Values", ntC, NumberForTaxon.class);
		msNT.setSelected(numberTaskName);
		return true;
	}

	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("setCalculator ", numberTask); 
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the module calculating the numbers for the taxa", "[name of module]", commandName, "setCalculator")) {
			NumberForTaxon temp =  (NumberForTaxon)replaceEmployee(NumberForTaxon.class, arguments, "Value to calculate for taxa", numberTask);
			//((TreesChartWindow)window).setNumberTask(numberTask);
			if (temp!=null) {
				numberTask = temp;
				numberTask.setHiringCommand(ntC);
				numberTaskName.setValue(numberTask.getName());
				if (!MesquiteThread.isScripting())
					parametersChanged();
				return numberTask;
			}
		}

		else 
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}


	/*.................................................................................................................*/
	public CharacterDistribution getCharacter(Taxa taxa, int ic) {
		ContinuousAdjustable states = new ContinuousAdjustable(taxa, taxa.getNumTaxa());
		states.setName(getCharacterName(taxa, 0));
		MesquiteNumber result = new MesquiteNumber();
		for (int it = 0; it<taxa.getNumTaxa(); it++){
			result.setToUnassigned();
			numberTask.calculateNumber(taxa.getTaxon(it), result, null);
			states.setState(it, result.getDoubleValue());
		}
		return states;
	}
	/*.................................................................................................................*/
	public int getNumberOfCharacters(Taxa taxa) {
		return 1;
	}
	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		super.employeeParametersChanged( employee,  source, notification);
	}
	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Taxa taxa){
	}
	/*.................................................................................................................*/
	/** returns the name of character ic*/
	public String getCharacterName(Taxa taxa, int ic){
		return  "Character from " + numberTask.getNameAndParameters();
	}
	/*.................................................................................................................*/
	public String getParameters() {
		return numberTask.getNameAndParameters() + " (reinterpreted as a continous character)";
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 200;  
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Calculated value for taxon";
	}

	/*.................................................................................................................*/
	public boolean showCitation() {
		return false;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return false;
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Supplies values for taxa reinterpreted as if characters." ;
	}
	/*.................................................................................................................*/
	public CompatibilityTest getCompatibilityTest() {
		return new ContinuousStateTest();
	}
}

