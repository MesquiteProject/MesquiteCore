/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 



Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.charMatrices.NumberForMatricesFP; 

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class NumberForMatricesFP extends FileProcessor {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e2 = registerEmployeeNeed(NumberForMatrix.class, getName() + " needs a particular method to calculate a number for the matrix.",
				null);
		e2.setPriority(2);
	}
	CharacterData data;
	MesquiteSubmenuSpec mss= null;
	NumberForMatrix numberTask;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (arguments !=null) {
			numberTask = (NumberForMatrix)hireNamedEmployee(NumberForMatrix.class, arguments);
			if (numberTask == null)
				return sorry(getName() + " couldn't start because the requested number for matrix module wasn't successfully hired.");
		}
		else {
			numberTask = (NumberForMatrix)hireEmployee(NumberForMatrix.class, "Number for Matrix");
			if (numberTask == null)
				return sorry(getName() + " couldn't start because no number for matrix module obtained.");
		}
		return true;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return false;  
   	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 310;  
	}
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
	/*.................................................................................................................*/
   	 public boolean isSubstantive(){
   	 	return false; 
   	 }
 	/*.................................................................................................................*/
 	public Snapshot getSnapshot(MesquiteFile file) { 
 		Snapshot temp = new Snapshot();
 		temp.addLine("setNumberForMatrix ", numberTask);  
 		return temp;
 	}
 	/*.................................................................................................................*/
 	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
 		if (checker.compare(this.getClass(), "Sets the module that alters data", "[name of module]", commandName, "setNumberForMatrix")) {
 			NumberForMatrix temp =  (NumberForMatrix)replaceEmployee(NumberForMatrix.class, arguments, "Number for matrix", numberTask);
 			if (temp!=null) {
 				numberTask = temp;
 				return numberTask;
 			}
 
 		}
 		else
 			return  super.doCommand(commandName, arguments, checker);
 		return null;
 	}

   	/** if returns true, then requests to remain on even after alterFile is called.  Default is false*/
   	public boolean pleaseLeaveMeOn(){
   		return false;
   	}
	/*.................................................................................................................*/
   	/** Called to alter file. */
   	public boolean processFile(MesquiteFile file, MesquiteString notice){
   		if (notice == null)
   			return false;
   		MesquiteProject proj = file.getProject();
   		if (proj == null)
   			return false;
   		boolean success = false;
   		CompatibilityTest test = numberTask.getCompatibilityTest();
   		MesquiteNumber result = new MesquiteNumber();
   		boolean first = true;
   		for (int im = 0; im < proj.getNumberCharMatrices(file); im++){
   			CharacterData data = proj.getCharacterMatrix(file, im);
   			if (test == null || test.isCompatible(data.getStateClass(), getProject(), this)) {
   				success = true;
   				result.setToUnassigned();
  				numberTask.calculateNumber(data.getMCharactersDistribution(), result, null); 
  				if (!first)
  					notice.append(" ");
  				notice.append(result.toString());
  				first = false;
  				
   			}
   		}
   			
   		return success;
   	}
	/*.................................................................................................................*/
	 public String getName() {
	return "Calculate Number for Matrices";
	 }
		/*.................................................................................................................*/
	 public String getNameAndParameters() {
		 if (numberTask==null)
			 return "Calculate Number for Matrices";
		 else
			 return numberTask.getNameAndParameters();
	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Manages number calculating modules for all matrices in a file." ;
   	 }
   	 
}


