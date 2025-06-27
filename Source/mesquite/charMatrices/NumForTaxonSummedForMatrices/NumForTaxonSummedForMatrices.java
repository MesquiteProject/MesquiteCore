/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/

package mesquite.charMatrices.NumForTaxonSummedForMatrices;

import mesquite.lib.CommandChecker;
import mesquite.lib.MesquiteCommand;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteNumber;
import mesquite.lib.MesquiteString;
import mesquite.lib.Notification;
import mesquite.lib.Snapshot;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.NumberForTaxon;
import mesquite.lib.duties.NumberForTaxonAndMatrix;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.taxa.Taxon;

public class NumForTaxonSummedForMatrices extends NumberForTaxon {
	NumberForTaxonAndMatrix numberTask;
	MesquiteCommand mc;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (arguments !=null) {
			numberTask = (NumberForTaxonAndMatrix)hireNamedEmployee(NumberForTaxonAndMatrix.class, arguments);
			if (numberTask==null) {
				return sorry("Number for taxon with matrix (for list) can't start because the requested calculator module wasn't successfully hired");
			}
		}
		else {
			numberTask = (NumberForTaxonAndMatrix)hireEmployee(NumberForTaxonAndMatrix.class, "Value to calculate for taxa (for taxa list)");
			if (numberTask==null) {
				return sorry("Number for taxon (for list) can't start because no calculator module was successfully hired");
			}
		}
		mc =makeCommand("numberTask",  this);
		numberTask.setHiringCommand(mc);
		addMenuItem("Value to Calculate...", mc);
		getProject().addListener(this);
		return true;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("numberTask", numberTask); 
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets calculator module", "[name of module]", commandName, "numberTask")) {
			NumberForTaxonAndMatrix temp = (NumberForTaxonAndMatrix)replaceEmployee(NumberForTaxonAndMatrix.class, arguments, "Number for taxon with matrix", numberTask);
			if (temp !=null){
				numberTask = temp;
				numberTask.setHiringCommand(mc);
			parametersChanged();
			return temp;
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}	
	
	public void endJob(){
		super.endJob();
		getProject().removeListener(this);
	}
	/* ---------------------------------------------------------*/
	/** passes which object changed, along with optional Notification object with details (e.g., code number (type of change) and integers (e.g. which character))*/
	public void changed(Object caller, Object obj, Notification notification){
		if (!Notification.appearsCosmetic(notification)){
		outputInvalid();
		parametersChanged();
		}
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return false;  
	}

	/** Called to provoke any necessary initialization.  This helps prevent the module's initialization queries to the user from
	   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Taxa taxa){
	}

	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 400;  
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Number of for taxon summed over stored matrices";  
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Number Summed For All Matrices";  
	}
	/*.................................................................................................................*/
	public String getVeryShortName() {
		if (numberTask != null)
			return "Summed " + numberTask.getVeryShortName();
		return "Sum over matrices";
	}

	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return false;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive() {
		return true;
	}

	/*.................................................................................................................*/

	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Sums over all matrices a value pertaining to the taxon for that matrix." ;
	}
	public void calculateNumber(Taxon taxon, MesquiteNumber result, MesquiteString resultString){
		if (result==null)
			return;
		result.setToUnassigned();
		Taxa taxa = taxon.getTaxa();
		int it = taxon.getIndex();
		clearResultAndLastResult(result);
		MesquiteNumber temp = new MesquiteNumber(0);
		result.setValue(0);
		for (int im = 0; im < getProject().getNumberCharMatrices(taxa); im++){
			CharacterData data = getProject().getCharacterMatrix(taxa, im);
			numberTask.calculateNumber(taxon, data.getMCharactersDistribution(), temp, null);
			result.add(temp);
		}

		if (resultString!=null){
				resultString.setValue(numberTask.getName());
		}
		saveLastResult(result);
		saveLastResultString(resultString);
	}
}


