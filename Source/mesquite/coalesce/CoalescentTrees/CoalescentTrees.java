/* Mesquite source code.  Copyright 1997 and onward, W. Maddison. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.coalesce.CoalescentTrees;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.coalesce.lib.*;

/* ======================================================================== */
public class CoalescentTrees extends TreeSimulate {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e2 = registerEmployeeNeed(Coalescer.class, getName() + " uses a coalescent simulator to simulate gene trees.",
		"The coalescent simulator is either chosen automatically or you can choose it initially.");
	}
	Coalescer coalescenceTask;
	CoalescedNode[] originals;
	int[] containing;
	int oldPopulationSize = 0;
	int numTaxa = 0;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		//todo: eventually allow UI choice and hiringCommand
		coalescenceTask = (Coalescer)hireEmployee(Coalescer.class, "Coalescence simulator");
		if (coalescenceTask == null)
			return sorry(getName() + " couldn't start because no coalescence simulator module obtained.");
 		return true;
  	 }
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
	public void employeeQuit(MesquiteModule m){
		iQuit();
	}
  	 
	/*.................................................................................................................*/
   	public void initialize(Taxa taxa){
   	}
	/*.................................................................................................................*/
   	public int getNumberOfTrees(Taxa taxa) {
   		return MesquiteInteger.infinite;
   	}
   	MesquiteTree savedBush;
	/*.................................................................................................................*/
   	public Tree getSimulatedTree(Taxa taxa, Tree tree, int treeNumber, ObjectContainer extra, MesquiteLong seed) {
		
		MesquiteTree t=null;
		if (tree==null || !(tree instanceof MesquiteTree))
			 t = new MesquiteTree(taxa);
		else
			t = (MesquiteTree)tree;
		if (savedBush==null || savedBush.getTaxa()!=t.getTaxa()) {
			t.setToDefaultBush(t.getNumTaxa(), false);
			savedBush = t.cloneTree();//no need to establish listener to Taxa, as will be remade when needed?
		}
		else
			t.setToClone(savedBush);
		if (containing == null || containing.length<t.getNumNodeSpaces()) {
			containing = new int [t.getNumNodeSpaces()];
		}
		for (int i=0; i<containing.length; i++){
			if (t.taxonNumberOfNode(i)>=0 ) //terminal node
				containing[i]=1;
			else
				containing[i]=0;
		}
		coalescenceTask.coalesce(t, containing, 1, 1.0, -1,  seed, true); 
  		return t;
   	}
   
	/*.................................................................................................................*/
    	 public String getName() {
		return "Coalescent Trees";
   	 }
   	 
	/*.................................................................................................................*/
   	 
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Generates tree by coalescence within a single panmictic population." ;
   	 }
	/*.................................................................................................................*/
 	/** returns current parameters, for logging etc..*/
 	public String getParameters() {
		if (coalescenceTask!=null)
			return "Coalescence simulator: " + coalescenceTask.getName();
		return "";
   	}
   	
	/*.................................................................................................................*/
   	 public boolean showCitation(){
   	 	return true;
   	 }
   	public boolean isPrerelease(){
   		return false;
   	}
}



