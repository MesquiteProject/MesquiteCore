package mesquite.charMatrices.DataUtilityFileProcessor;

/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 



Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/

import java.util.*;
import java.awt.*;
import java.awt.image.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.lib.ui.MesquiteSubmenuSpec;

/* ======================================================================== */
public class DataUtilityFileProcessor extends FileProcessor {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e2 = registerEmployeeNeed(DataUtility.class, getName() + " needs a particular method to act upon data.", null);
		e2.setPriority(2);
	}
	MesquiteTable table;
	CharacterData data;
	MesquiteSubmenuSpec mss= null;
	DataUtility utilityTask;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (arguments !=null) {
			utilityTask = (DataUtility)hireNamedEmployee(DataUtility.class, arguments);
			if (utilityTask == null)
				return sorry(getName() + " couldn't start because the requested data utility wasn't successfully hired.");
		}
		else {
			utilityTask = (DataUtility)hireEmployee(DataUtility.class, "Data utility");
			if (utilityTask == null)
				return sorry(getName() + " couldn't start because no utility module obtained.");
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
		return 302;  
	}
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
	/*.................................................................................................................*/
   	 public boolean isSubstantive(){
   	 	return true; //not really, but to force checking of prerelease
   	 }
 	/*.................................................................................................................*/
 	public Snapshot getSnapshot(MesquiteFile file) { 
 		Snapshot temp = new Snapshot();
 		temp.addLine("setDataUtility ", utilityTask);  
 		return temp;
 	}
 	/*.................................................................................................................*/
 	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
 		if (checker.compare(this.getClass(), "Sets the data utility module", "[name of module]", commandName, "setDataUtility")) {
 			DataUtility temp =  (DataUtility)replaceEmployee(DataUtility.class, arguments, "Data utility", utilityTask);
 			if (temp!=null) {
 				utilityTask = temp;
 				return utilityTask;
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
   	public int processFile(MesquiteFile file){
   		MesquiteProject proj = file.getProject();
   		if (proj == null)
   			return -1;
   		boolean success = false;
   		CompatibilityTest test = utilityTask.getCompatibilityTest();
   		for (int im = 0; im < proj.getNumberCharMatrices(file); im++){
   			CharacterData data = proj.getCharacterMatrix(file, im);
   			if (test.isCompatible(data.getStateClass(), getProject(), this)) {
   				success = true;
   				utilityTask.operateOnData(data);  // do not measure success based upon whether data were altered.
   			}
   		}
   		if (success)
   			return 0;
   		return 1;
   	}
	/*.................................................................................................................*/
	 public String getName() {
	return "Process using Data Utility";
	 }
		/*.................................................................................................................*/
	 public String getNameAndParameters() {
		 if (utilityTask==null)
			 return "Process using Data Utility";
		 else
			 return "Process using Data Utility (" + utilityTask.getName() + ")";
	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Manages data-transforming Data Utility modules to transform all matrices in a file." ;
   	 }
   	 
}


