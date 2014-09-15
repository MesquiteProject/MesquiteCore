/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 

 
 Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
 The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
 Perhaps with your help we can be more than a few, and make Mesquite better.
 
 Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
 Mesquite's web site is http://mesquiteproject.org
 
 This source code and its compiled class files are free and modifiable under the terms of 
 GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.TranslateToProtein;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;

/* ======================================================================== */
public class TranslateToProtein extends CharMatrixSource {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(mesquite.charMatrices.StoredMatrices.StoredMatrices.class, getName() + "  needs a source of sequences.",
		"The source of sequences is arranged initially");
	}
	CharMatrixSource dataTask;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		
		dataTask = (CharMatrixSource)hireNamedEmployee(CharMatrixSource.class, "#StoredMatrices", DNAState.class);
		if (dataTask == null) {
			return sorry(getName() + " can't be started because no source of matrices was obtained");
		}
		return true; 
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
	/*.................................................................................................................*/
	public void employeeQuit(MesquiteModule m){
		iQuit();
	}
	/*.................................................................................................................*/
	public CompatibilityTest getCompatibilityTest() {
		return new CharacterStateTest();
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("getCharacterSource ", dataTask);
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Returns employee that is matrix source", null, commandName, "getCharacterSource")) {
			return dataTask;
		}
		else 
			return  super.doCommand(commandName, arguments, checker);
		//return null;
	}
	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
	 happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Taxa taxa){
		dataTask.initialize(taxa);
	}
	
	private MCharactersDistribution translate(MCharactersDistribution mData){
		if (mData != null && mData.getParentData() != null){
			CharacterData data = mData.getParentData();
			if (data instanceof DNAData){
				DNAData dData = (DNAData)data;
				ProteinData pData = dData.getProteinData(null, true);
				if (pData!= null) {
					pData.setName("Protein translation of " + dData.getName());
					return pData.getMCharactersDistribution();
				}
			}
		}
		return null;
		
	}
	
	/** returns the number of character matrices that can be supplied for the given taxa*/
	public int getNumberOfMatrices(Taxa taxa){
		return dataTask.getNumberOfMatrices(taxa);
	}
	/** gets the current matrix.*/
	public  MCharactersDistribution getCurrentMatrix(Taxa taxa){
		MCharactersDistribution orig = dataTask.getCurrentMatrix(taxa);
		return translate(orig);
	}
	/** gets the indicated matrix.*/
	public  MCharactersDistribution getMatrix(Taxa taxa, int im){
		MCharactersDistribution orig = dataTask.getMatrix(taxa, im);
		return translate(orig);
	}
	/** gets name of the indicated matrix.*/
	public  String getMatrixName(Taxa taxa, int im){
		return "Protein translation of " + dataTask.getMatrixName(taxa, im);
	}
	/** returns the number of the current matrix*/
	public int getNumberCurrentMatrix(){
		return dataTask.getNumberCurrentMatrix();
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Translate DNA to Protein";
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Translates a stored DNA matrix to protein." ;
	}
	
}

