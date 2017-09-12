/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.distance.ClusterAnalysis;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.distance.lib.*;

/* ======================================================================== */
//eventually have DistanceTree as treeblock filler, which hires TreeClusterer and TaxaDistanceSource
public class ClusterAnalysis extends TreeInferer implements Incrementable {  //Incrementable just in case distance task is
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(TaxaDistanceSource.class, getName() + "  needs a source of distances.",
		"The source of distances can be selected initially");
		EmployeeNeed ew= registerEmployeeNeed(TreeClusterer.class, getName() + "  needs a method to cluster taxa by distances..",
		"The method to cluster taxa can be selected initially");
	}
	TaxaDistanceSource distanceTask;
	TreeClusterer clusterer;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		distanceTask = (TaxaDistanceSource)hireEmployee(TaxaDistanceSource.class, "Source of distance for Cluster Analysis");
 		if (distanceTask == null) {
 			return sorry(getName() + " couldn't start because no source of distances was obtained.");
 		}
 		clusterer = (TreeClusterer)hireEmployee(TreeClusterer.class, "Clustering method");
 		if (clusterer == null) {
 			return sorry(getName() + " couldn't start because no clusterer was obtained.");
 		}
  		return true;
  	 }
	
	public boolean isReconnectable(){
		return false;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return false;  
   	}
 	 public void employeeQuit(MesquiteModule m){
  	 	iQuit();
  	 }
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) { 
   	 	Snapshot temp = new Snapshot();
		temp.addLine("setDistanceSource ", distanceTask);
		temp.addLine("setClusterer ", clusterer);
  	 	return temp;
  	 }
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	 if (checker.compare(this.getClass(), "Sets the source of distances for use in cluster analysis", "[name of module]", commandName, "setDistanceSource")) { 
    	 		TaxaDistanceSource temp=  (TaxaDistanceSource)replaceEmployee(TaxaDistanceSource.class, arguments, "Source of distance for cluster analysis", distanceTask);
 			if (temp!=null) {
 				distanceTask= temp;
 			}
 			return distanceTask;
 		}
    	 	else if (checker.compare(this.getClass(), "Sets the clustering module for use in cluster analysis", "[name of module]", commandName, "setClusterer")) { 
    	 		TreeClusterer temp=  (TreeClusterer)replaceEmployee(TreeClusterer.class, arguments, "Clustering routine for cluster analysis", clusterer);
 			if (temp!=null) {
 				clusterer= temp;
 			}
 			return clusterer;
 		}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
	}
	/*.................................................................................................................*/
 	public void setCurrent(long i){  //SHOULD NOT notify (e.g., parametersChanged)
 		if (distanceTask instanceof Incrementable)
 			((Incrementable)distanceTask).setCurrent(i);
 	}
 	public long getCurrent(){
 		if (distanceTask instanceof Incrementable)
 			return ((Incrementable)distanceTask).getCurrent();
 		return 0;
 	}
 	public String getItemTypeName(){
 		if (distanceTask instanceof Incrementable)
 			return ((Incrementable)distanceTask).getItemTypeName();
 		return "";
 	}
 	public long getMin(){
 		if (distanceTask instanceof Incrementable)
 			return ((Incrementable)distanceTask).getMin();
		return 0;
 	}
 	public long getMax(){
 		if (distanceTask instanceof Incrementable)
 			return ((Incrementable)distanceTask).getMax();
		return 0;
 	}
 	public long toInternal(long i){
 		if (distanceTask instanceof Incrementable)
 			return ((Incrementable)distanceTask).toInternal(i);
 		return i-1;
 	}
 	public long toExternal(long i){ //return whether 0 based or 1 based counting
 		if (distanceTask instanceof Incrementable)
 			return ((Incrementable)distanceTask).toExternal(i);
 		return i+1;
 	}
 	
	/*.................................................................................................................*/
   	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
   	public void initialize(Taxa taxa){
   		distanceTask.initialize(taxa);

   	}
   	
	/*.................................................................................................................*/
  	public void fillTreeBlock(TreeVector treeList, int numberIfUnlimited){
  		if (treeList==null)
  			return;
   		Taxa taxa = treeList.getTaxa();
   		distanceTask.initialize(taxa);
 		TaxaDistance dist = distanceTask.getTaxaDistance(taxa);

		clusterer.getTrees(taxa, dist, treeList);
  		
		treeList.setName("Trees from " + clusterer.getName() + " cluster analysis (Distance: " + distanceTask.getName() + ")");
		treeList.setAnnotation ("Parameters: "  + getParameters() + ";  " + clusterer.getParameters() + "  Distance: " + distanceTask.getParameters(), false);
  	}
	/*.................................................................................................................*/
   	public String getParameters() {
		return "Clustering via  " + clusterer.getName() + " from " +  distanceTask.getName();
   	}
	/*.................................................................................................................*/
   	 public boolean hasLimitedTrees(Taxa taxa){
   	 	return true;
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Cluster Analysis";
   	 }
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Supplies trees obtained from cluster analysis on distance matrices.";
   	 }
   	 
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
	/*.................................................................................................................*/
   	 public boolean showCitation(){
   	 	return true;
   	 }
}


