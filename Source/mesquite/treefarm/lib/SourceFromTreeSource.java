/* Mesquite source code, Treefarm package.  Copyright 1997 and onward, W. Maddison, D. Maddison and P. Midford. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.treefarm.lib;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public abstract class SourceFromTreeSource extends TreeSource {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(TreeSource.class, getName() + "  needs a source of trees.",
		"The source of trees can be selected initially");
	}
	/*.................................................................................................................*/
	protected TreeSource currentTreeSource;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		currentTreeSource = (TreeSource)hireEmployee(TreeSource.class, "Source of trees to be transformed or filtered (" + whatIsMyPurpose() + ")");
 		if (currentTreeSource == null) {
 			return sorry(getName() + " couldn't start because no source of trees to serve as a basis for modification or filtering was obtained.");
 		}
  		return true;
  	 }
  	 public void employeeQuit(MesquiteModule m){
  	 	iQuit();
  	 }
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) { 
   	 	Snapshot temp = new Snapshot();
  	 	temp.addLine("setTreeSource ", currentTreeSource); 
  	 	return temp;
  	 }
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	 if (checker.compare(this.getClass(), "Sets the source of the tree to be transformed or filtered", "[name of module]", commandName, "setTreeSource")) {
			TreeSource temp = (TreeSource)replaceEmployee(TreeSource.class, arguments, "Source of tree to be transformed or filtered", currentTreeSource);
			if (temp !=null){
				currentTreeSource = temp;
				parametersChanged();
    	 			return currentTreeSource;
    	 		}
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
    	 	return null;
    	 }
	/*.................................................................................................................*/
  	public void setPreferredTaxa(Taxa taxa){
  	}
   	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
   	public void initialize(Taxa taxa){
   		if (currentTreeSource!=null)
   			currentTreeSource.initialize(taxa);
   	}
	/*.................................................................................................................*/
	protected Tree getBasisTree(Taxa taxa, int i){
   		Tree t =  currentTreeSource.getTree(taxa, i);
 		return t;
	}
	/*.................................................................................................................*/
	protected TreeSource getBasisTreeSource(){
   		return currentTreeSource;
	}
}

