/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.treefarm.CurrentTree;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.tree.Tree;

/** Supplies a tree from a current tree window.*/
public class CurrentTree extends TreeSource {
	 public String getName() {
	return "Current Tree";
	 }
	 public String getExplanation() {
	return "Supplies a single tree currently shown in a tree window.";
	 }
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(OneTreeSource.class, getName() + "  needs a source of the current tree.",
		"");
	}
	OneTreeSource currentTreeSource;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		currentTreeSource = (OneTreeSource)hireEmployee(OneTreeSource.class, "Source of current tree");
 		if (currentTreeSource == null) {
 			return sorry(getName() + " couldn't start because no source of a current tree was obtained.");
 		}
 		if (currentTreeSource.getUltimateSource() == this)
 			return sorry(getName() + " couldn't start because it would be attempting to obtained its own tree, resulting in an infinite recursion.");
  		return true;
  	 }
  	 public void employeeQuit(MesquiteModule m){
  	 	iQuit();
  	 }
	 public boolean permitSeparateThreadWhenFilling(){
		 return false;
	 }
	    /**Returns whether or not the source can handle asking for the last tree, i.e. for what the source says is maxTrees - 1, even if that is unassigned or infinite, i.e., is not a combinable number. 
	     * If asked, and the source has an indefinite number, it will supply a tree (e.g. from a live file) rather than just trying forever. 
	     * Used for Pin to Last Tree in BasicTreeWindow.*/
	   	public boolean permitsRequestForLastTree(Taxa taxa){
	   		return true;
	   	}
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) { 
   	 	Snapshot temp = new Snapshot();
  	 	temp.addLine("setTreeSource ", currentTreeSource); 
  	 	return temp;
  	 }
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	 if (checker.compare(this.getClass(), "Sets the source of the tree", "[name of module]", commandName, "setTreeSource")) {
			OneTreeSource temp = (OneTreeSource)replaceEmployee(OneTreeSource.class, arguments, "Source of tree", currentTreeSource);
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
 		if (currentTreeSource.getUltimateSource() == this) {
 			alert("A tree source had to quit because it would be attempting to obtain its own trees, resulting in an infinite recursion.");
 			iQuit();
 		}
   		if (currentTreeSource!=null)
   			currentTreeSource.initialize(taxa);
   	}
	/*.................................................................................................................*/
	/*.................................................................................................................*/
   	public Tree getTree(Taxa taxa, int ic) {  
   		if (ic != 0)
   			return null;
   		Tree t =  currentTreeSource.getTree(taxa);
 		if (currentTreeSource.getUltimateSource() == this) {
 			alert("A tree source had to quit because it would be attempting to obtain its own trees, resulting in an infinite recursion.");
 			iQuit();
 			return null;
 		}
 		return t;
   	}
	/*.................................................................................................................*/
   	public int getNumberOfTrees(Taxa taxa) {
   		return 1;
   	}
   
	/*.................................................................................................................*/
   	public String getTreeNameString(Taxa taxa, int itree) {
   		return "Tree from " + currentTreeSource.getParameters();
   	}
	/*.................................................................................................................*/
   	public String getParameters() {
   		return currentTreeSource.getParameters();
   	}
	/*.................................................................................................................*/
   	public boolean isPrerelease(){
   		return false;
   	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}

}

