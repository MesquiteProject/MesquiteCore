/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.NumberStopsInTaxonT;
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.taxa.Taxon;
import mesquite.categ.lib.*;

/* ======================================================================== */
public class NumberStopsInTaxonT extends NumberForTaxon {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(MatrixSourceCoord.class, getName() + "  needs a source of sequences.",
		"The source of characters is arranged initially");
	}
	MatrixSourceCoord matrixSourceTask;
	Taxa currentTaxa = null;
	MCharactersDistribution observedStates =null;
	MesquiteBoolean countEvenIfOthers;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		matrixSourceTask = (MatrixSourceCoord)hireCompatibleEmployee(MatrixSourceCoord.class, DNAState.class, "Source of character matrix (for number of stops)"); 
		if (matrixSourceTask==null)
			return sorry(getName() + " couldn't start because no source of character matrices was obtained.");
		countEvenIfOthers = new MesquiteBoolean(false);
		addCheckMenuItem(null, "Count for Ambiguous Sites even if Other AAs ", makeCommand("toggleCountEvenIfOthers", this), countEvenIfOthers);
		return true;
	}
	/*.................................................................................................................*/
	public boolean loadModule() { //To delete module; Debugg.println (replaced by NumberForTaxonAndMatrix version)
		return false;
	}


	/*.................................................................................................................*/
	/** Generated by an employee who quit.  The MesquiteModule should act accordingly. */
	public void employeeQuit(MesquiteModule employee) {
		if (employee == matrixSourceTask)  // character source quit and none rehired automatically
			iQuit();
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}

	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Taxa taxa){
		currentTaxa = taxa;
		matrixSourceTask.initialize(currentTaxa);
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("getMatrixSource", matrixSourceTask);
		temp.addLine("toggleCountEvenIfOthers " + countEvenIfOthers.toOffOnString());
		return temp;
	}
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets whether or not to count stops in regions with ambiguous nucleotides that imply a stop OR an amino acid", "[on; off]", commandName, "toggleCountEvenIfOthers")) {
			countEvenIfOthers.toggleValue(parser.getFirstToken(arguments));
			parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Returns the matrix source", null, commandName, "getMatrixSource")) {
			return matrixSourceTask;
		}
		else return  super.doCommand(commandName, arguments, checker);
		return null;
	}


	public void calculateNumber(Taxon taxon, MesquiteNumber result, MesquiteString resultString){
		if (result==null)
			return;
	   	clearResultAndLastResult(result);
		Taxa taxa = taxon.getTaxa();
		int it = taxa.whichTaxonNumber(taxon);
		if (taxa != currentTaxa || observedStates == null ) {
			observedStates = matrixSourceTask.getCurrentMatrix(taxa);
			currentTaxa = taxa;
		}
		if (observedStates==null || !(observedStates.getParentData() instanceof DNAData))
			return;
		DNAData data = (DNAData)observedStates.getParentData();
		int count = data.getAminoAcidNumbers(it,ProteinData.TER,countEvenIfOthers.getValue());
		if (result !=null)
			result.setValue(count);
		if (resultString!=null)
			resultString.setValue("Number of stop codons in taxon "+ taxon.getName() + " in matrix " + observedStates.getName() + ": " + count);
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	/*.................................................................................................................*/
	/** Returns CompatibilityTest so other modules know if this is compatible with some object. */
	public CompatibilityTest getCompatibilityTest(){
		return new RequiresAnyDNAData();
	}
	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		observedStates = null;
		super.employeeParametersChanged(employee, source, notification);
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Number of Stops";  
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
	public String getParameters() {
		return "Number of stops coded by nucleotides in taxon in matrix from: " + matrixSourceTask.getParameters();
	}
	/*.................................................................................................................*/

	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Reports the number of stops coded by nucleotides in a taxon for a data matrix." ;
	}

}




