/* Mesquite source code.  Copyright 1997 and onward, W. Maddison. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.coalesce.ContainedCoalescence;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.coalesce.lib.*;
import mesquite.assoc.lib.*;
/* ======================================================================== 
For version 1.02 the main calculations of this were split off as ContainedCoalescSim to allow alternatives to be swapped more easily in the future (e.g, with migration)*/
public class ContainedCoalescence extends TreeSimulate {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(ContainedTreeSim.class, getName() + " uses a coalescent simulator to simulate gene trees.",
		"The coalescent simulator is either chosen automatically or you can choose it initially.");
		EmployeeNeed e2 = registerEmployeeNeed(OneTreeSource.class, getName() + " uses a current tree.",
		"The source of a current tree is chosen initially.");
		EmployeeNeed e3 = registerEmployeeNeed(AssociationSource.class, getName() + " uses a current taxon association.",
		"The source of a taxon association is chosen initially.");

	}
	AssociationSource associationTask;
	TaxaAssociation association;
	OneTreeSource oneTreeSourceTask;
	ContainedTreeSim simTask;
	Tree speciesTree;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		//TODO: allow subsequent choosing from menus
		associationTask = (AssociationSource)hireEmployee(AssociationSource.class, "Source of taxon association (Contained Coalescence)");
		if (associationTask == null)
			return sorry(getName() + " couldn't start because no source of taxon associations obtained.");
		simTask = (ContainedTreeSim)hireEmployee(ContainedTreeSim.class, "Simulator of coalescence within current tree");
		if (simTask == null)
			return sorry(getName() + " couldn't start because no simulation module obtained.");
		if (oneTreeSourceTask == null) {
			oneTreeSourceTask = (OneTreeSource)hireEmployee(OneTreeSource.class, "Source of containing tree");
		}
 		return true;
  	 }
	/*.................................................................................................................*/
  	 public void employeeQuit(MesquiteModule m){
  	 	iQuit();
  	 }
   	public void initialize(Taxa taxa){
   		if (!check(taxa))
   			return;
   		if (oneTreeSourceTask!=null && association!=null)
   			oneTreeSourceTask.initialize(association.getOtherTaxa(taxa));

   	}
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) { 
   	 	Snapshot temp = new Snapshot();
  	 	temp.addLine("getAssociationTask ", associationTask); 
  	 	temp.addLine("getTreeSource ", oneTreeSourceTask); 
 	 	temp.addLine("setSimulator ", simTask); 
 	 	  	 	temp.addLine("forgetAssociation "); 
  	 	
  	 	return temp;
  	 }
  	 /*---*/
  	
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if  (checker.compare(this.getClass(), "Returns the species tree being used for contained coalescence", null, commandName, "getSpeciesTree")) {
    	 		return speciesTree;
    	 	}
    	 	else if  (checker.compare(this.getClass(), "Returns the containing tree context", null, commandName, "getTreeSource")) {
    	 		return oneTreeSourceTask;
    	 	}
       	 	else if  (checker.compare(this.getClass(), "Sets the simulator", null, commandName, "setSimulator")) {
       	 	ContainedTreeSim temp=  (ContainedTreeSim)replaceEmployee(ContainedTreeSim.class, arguments, "Simulation method", simTask);
    			if (temp!=null) {
    				simTask= temp;
    				parametersChanged();
    			}
    			return simTask;
    	 	}
   	 	else if  (checker.compare(this.getClass(), "Returns the module supplying associations", null, commandName, "getAssociationTask")) {
    	 		return associationTask;
    	 	}
    	 	else if  (checker.compare(this.getClass(), "Sets the current association to null", null, commandName, "forgetAssociation")) {
    	 		association = null;
    	 		return null;
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
   	 }
    	 boolean warnedNull = false;
	/*.................................................................................................................*/
   	private boolean check(Taxa taxa){
   		if (taxa!=null) {
	        	if (association == null || (association.getTaxa(0)!= taxa && association.getTaxa(1)!= taxa)) {
	        		association = associationTask.getCurrentAssociation(taxa);
	        	/*	MesquiteModule mb = getFileCoordinator().findEmployeeWithName("#mesquite.leaves.BestSpeciesTreeForAssoc.BestSpeciesTreeForAssoc");
	        		if (mb!=null) {
	        			association = ((NumberForAssociation)mb).getCurrentAssociation();
	        		}
	        		if (association==null)
		        		association = associationTask.getCurrentAssociation(taxa);
*/
	        		if (association==null) {
	        			String s = "Association null in Contained Coalescence (for taxa " + taxa.getName() + ")";
	        			if (warnedNull)
	        				MesquiteMessage.println(s);
	        			else
	        				discreetAlert( s);
	        			warnedNull = true;
	        			return false;
	        		}
	        	}
			if (oneTreeSourceTask == null) {
				oneTreeSourceTask = (OneTreeSource)hireEmployee(OneTreeSource.class, "Source of containing tree");
				if (oneTreeSourceTask == null) {
	        			String s = "Source for containing tree null in Contained Coalescence (for taxa " + taxa.getName() + ")";
	        			if (warnedNull)
	        				MesquiteMessage.println(s);
	        			else
	        				discreetAlert( s);
	        			warnedNull = true;
	        			return false;
				}
					
			}
   		}
   		return true;
   	}
	/*.................................................................................................................*/
   	public int getNumberOfTrees(Taxa taxa) {
   		return MesquiteInteger.infinite;
   	}
   	long oldTaxaVersion = -1;
	boolean first = true;
	/*.................................................................................................................*/
   	public Tree getSimulatedTree(Taxa taxa, Tree geneTree, int treeNumber, ObjectContainer extra, MesquiteLong seed) {
   		if (!check(taxa))   			return null;
   		if (association==null) {
   			if (first)
   				alert("No association found for use by Contained Coalescence; no tree could be made");
   			first = false;
   			return null;
   		}
		speciesTree = oneTreeSourceTask.getTree(association.getOtherTaxa(taxa));
   		if (speciesTree==null) {
   			if (first)
   				alert("No species tree found for use by Contained Coalescence.  Taxa blocks: " + taxa + " AND " + association.getOtherTaxa(taxa));
   			first = false;
   			return null;
   		}
   		
   		
		geneTree = simTask.simulateContainedTree(speciesTree, geneTree, association, taxa, seed);
		return geneTree;
   	}
	/*.................................................................................................................*/
	 public boolean requestPrimaryChoice() {
	return true;
	 }
	/*.................................................................................................................*/
    public String getName() {
		return "Coalescence Contained within Current Tree";
   	 }
   	 
	/*.................................................................................................................*/
   	 
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Generates tree by a simple coalescence model of a neutral gene with constant population size, within a current species tree from a Tree window or other tree context. "
 		+"Branch lengths are assigned according to generation of coalescence." 
 		+"The species tree used is a current tree found in a Tree Window or other tree context." ;
   	 }
	/*.................................................................................................................*/
 	/** returns current parameters, for logging etc..*/
 	public String getNameAndParameters() {
		if (speciesTree!=null)
			return simTask.getNameAndParameters() + " (Species tree: " + speciesTree.getName() + ")" ;
		return "";
   	 }
	/*.................................................................................................................*/
 	/** returns current parameters, for logging etc..*/
 	public String getParameters() {
		if (speciesTree!=null)
			return "Species tree: " + speciesTree.getName() + "; " + simTask.getParameters();
		return "";
   	 }
   	public boolean isPrerelease(){
   		return false;
   	}
	/*.................................................................................................................*/
   	 public boolean showCitation(){
   	 	return true;
   	 }
}


