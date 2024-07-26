/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 



Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.charMatrices.AlterAllMatrices; 

import java.util.*;
import java.awt.*;
import java.awt.image.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class AlterAllMatrices extends FileProcessor {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e2 = registerEmployeeNeed(DataAlterer.class, getName() + " needs a particular method to alter data, e.g. in the Character Matrix Editor.",
				null);
		e2.setPriority(2);
	}
	MesquiteTable table;
	CharacterData data;
	MesquiteSubmenuSpec mss= null;
	DataAlterer alterTask;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (arguments !=null) {
			alterTask = (DataAlterer)hireNamedEmployee(DataAlterer.class, arguments);
			if (alterTask == null)
				return sorry(getName() + " couldn't start because the requested data alterer wasn't successfully hired.");
		}
		else if (!MesquiteThread.isScripting()) {
			alterTask = (DataAlterer)hireEmployee(DataAlterer.class, "Alterer/Transformer of matrices");
			if (alterTask == null)
				return sorry(getName() + " couldn't start because no tranformer module obtained.");
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
		return 300;  
	}
 	public String getNameForProcessorList() {
 		if (alterTask != null)
 			return getName() + "(" + alterTask.getName() + ")";
 		return getName();
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
 		temp.addLine("setDataAlterer ", alterTask);  
 		return temp;
 	}
 	/*.................................................................................................................*/
 	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
 		if (checker.compare(this.getClass(), "Sets the module that alters data", "[name of module]", commandName, "setDataAlterer")) {
 			DataAlterer temp =  (DataAlterer)replaceEmployee(DataAlterer.class, arguments, "Data alterer", alterTask);
 			if (temp!=null) {
				alterTask = temp;
 				return alterTask;
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
   		if (alterTask == null)
   			return 2;
   		MesquiteProject proj = file.getProject();
   		if (proj == null)
   			return 0;
   		boolean success = false;
   		CompatibilityTest test = alterTask.getCompatibilityTest();
   		for (int im = 0; im < proj.getNumberCharMatrices(file); im++){
   			CharacterData data = proj.getCharacterMatrix(file, im);
   			if (test.isCompatible(data.getStateClass(), getProject(), this)) {
   				success = true;
   				logln("Altering matrix \"" + data.getName() + "\"");
   				alterTask.alterData(data, null, null);  // do not measure success based upon whether data were altered.
   			}
   		}
   		if (success)
   			return 0;
   		logln("Alter matrix failed: " + data.getName() + " by " + alterTask.getName());
   		return 1;
   	}
	/*.................................................................................................................*/
	 public String getName() {
	return "Alter Matrices";
	 }
		/*.................................................................................................................*/
	 public String getNameAndParameters() {
		 if (alterTask==null)
			 return "Alter Matrices";
		 else
			 return alterTask.getNameAndParameters();
	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Manages data-transforming modules to transform all matrices in a file." ;
   	 }
   	 
}


