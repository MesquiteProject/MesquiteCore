/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.charMatrices.lib;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public abstract class SourceModRespectiveMatrix extends CharMatrixSource {
	MatrixSourceCoordObed dataTask;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (condition!=null) 
			dataTask = (MatrixSourceCoordObed)hireCompatibleEmployee( MatrixSourceCoordObed.class, condition, "Source of matrices to modify");
		else 
			dataTask = (MatrixSourceCoordObed)hireEmployee( MatrixSourceCoordObed.class, "Source of matrices to modify");
		if (dataTask == null) {
			return sorry(getName() + " can't be started because not source of matrices was obtained");
		}
  	 	return true; 
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
    	 	 if (checker.compare(this.getClass(), "Returns the source of matrices on which to do ordinations", null, commandName, "setCharacterSource")) { //TEMPORARY
    	 		if (dataTask != null)
    	 			return dataTask.doCommand(commandName, arguments, checker);
    	 	}
    	 	else if (checker.compare(this.getClass(), "Returns employee that is matrix source", null, commandName, "getCharacterSource")) {
    	 		return dataTask;
    	 	}
    	 	else 
    	 		return  super.doCommand(commandName, arguments, checker);
    	 	return null;
   	 }
   	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
   	public void initialize(Taxa taxa){
   		dataTask.initialize(taxa);
   	}
  	 
	/*.................................................................................................................*/
	protected MCharactersDistribution getBasisMatrix(Taxa taxa, int i){
   		return dataTask.getMatrix(taxa, i);
	}
	/*.................................................................................................................*/
    	public  int getNumberOfMatrices(Taxa taxa){
    		return dataTask.getNumberOfMatrices(taxa);
    	}
	/*.................................................................................................................*/
	protected MatrixSourceCoordObed getBasisMatrixSource(){
   		return dataTask;
	}
}

