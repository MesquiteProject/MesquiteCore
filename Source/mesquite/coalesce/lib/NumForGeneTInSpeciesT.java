/* Mesquite source code.  Copyright 1997-2007 W. Maddison. 
Version 2.01, December 2007.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.coalesce.lib;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.coalesce.lib.*;
import mesquite.assoc.lib.*;


/* ======================================================================== */
/** This evaluates a gene tree in the context of a containing species tree. */
public abstract class NumForGeneTInSpeciesT extends NumberForTree {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e2 = registerEmployeeNeed(OneTreeSource.class, getName() + " needs a source for the containing tree (e.g. a species tree) to assess the fit of contained trees (e.g. gene trees).",
		"The source of other trees can be indicated initially or later under the Species Tree Source submenu.");
		EmployeeNeed e3 = registerEmployeeNeed(AssociationSource.class, getName() + " needs a taxa association to indicate how contained taxa fit within containing taxa.",
		"The source of associations may be arranged automatically or is chosen initially.");
		EmployeeNeed e4 = registerEmployeeNeed(ReconstructAssociation.class, getName() + "needs to fit or reconstruct the contained tree within the containing tree in order to measure its fit.",
		"The reconstruction method can be specified initially.");
	}
	protected OneTreeSource treeSourceTask;
	protected AssociationSource associationTask;
	protected ReconstructAssociation reconstructTask;
	protected MesquiteString treeSourceName;
	protected TaxaAssociation association;
	protected Taxa containingTaxa;
	protected Tree lastTree, lastContaining;
	protected MesquiteCommand tstC;
	/*.................................................................................................................*/
	public boolean superStartJob(String arguments, Object condition, boolean hiredByName) {
		//todo: allow choices && put in setHiringCommand
		associationTask = (AssociationSource)hireEmployee(AssociationSource.class, "Source of taxon associations");
		if (associationTask == null)
			return sorry(getName() + " couldn't start because no source of taxon associatons obtained.");
		
		//todo: if this is going to be for deep coalescences, should hire just that reconstructor.  Otherwise change name of this throughout to neutral
		reconstructTask = (ReconstructAssociation)hireEmployee(ReconstructAssociation.class, "Method to reconstruct association history");
		if (reconstructTask == null)
			return sorry(getName() + " couldn't start because module to reconstruct association histories obtained.");
		treeSourceTask = (OneTreeSource)hireEmployee(OneTreeSource.class, "Source of containing trees");
		if (treeSourceTask == null)
			return sorry(getName() + " couldn't start because source of containing trees obtained.");
		tstC =  makeCommand("setTreeSource",  this);
		treeSourceTask.setHiringCommand(tstC);
		treeSourceName = new MesquiteString(treeSourceTask.getName());
		if (numModulesAvailable(TreeSource.class)>1) {
			MesquiteSubmenuSpec mss = addSubmenu(null, "Species Tree Source",tstC, OneTreeSource.class);
			mss.setSelected(treeSourceName);
		}
 		return true;
  	 }
	public void employeeQuit(MesquiteModule m){
		if (m != treeSourceTask)
			iQuit();
	}  	 
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) { 
   	 	Snapshot temp = new Snapshot();
  	 	temp.addLine("setTreeSource ", treeSourceTask); 
  	 	return temp;
  	 }
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Sets the source of the species tree", "[name of module]", commandName, "setTreeSource")) {
			OneTreeSource temp = (OneTreeSource)replaceEmployee(OneTreeSource.class, arguments, "Source of trees", treeSourceTask);
			if (temp !=null){
				treeSourceTask = temp;
				treeSourceTask.setHiringCommand(tstC);
				treeSourceName.setValue(treeSourceTask.getName());
				parametersChanged();
    	 			return treeSourceTask;
    	 		}
    	 	}
 		else {
 			return  super.doCommand(commandName, arguments, checker);
		}
		return null;
   	 }
   	 
   	 
   	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
   	public void initialize(Tree tree){
   		if (tree ==null)
   			return;
   		Taxa taxa = tree.getTaxa();
        	if (association == null || (association.getTaxa(0)!= taxa && association.getTaxa(1)!= taxa)) {
        		association = associationTask.getCurrentAssociation(taxa); 
        		if (association.getTaxa(0)== taxa)
        			containingTaxa = association.getTaxa(1);
        		else
        			containingTaxa = association.getTaxa(0);
        	}
   	}
	/*.................................................................................................................*/
    	 public String getParameters() {
		if (lastContaining !=null)
			return "Species tree " + lastContaining.getName();
		return "";
   	 }
   
}
