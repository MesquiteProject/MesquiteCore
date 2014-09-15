/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.distance.AverageOfDistances;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.distance.lib.*;

/* ======================================================================== */
public class AverageOfDistances extends TaxaDistanceSource {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(IncTaxaDistanceSource.class, getName() + "  needs a source of distances to average.",
		"The source of distances can be selected initially");
	}
	/*.................................................................................................................*/
	IncTaxaDistanceSource distanceTask;
	
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		distanceTask = (IncTaxaDistanceSource)hireEmployee(IncTaxaDistanceSource.class, "Source of distances to be averaged");
 		if (distanceTask == null) {
 			return sorry(getName() + " couldn't start because no source of distances to be averaged was obtained.");
 		}
 		return true;
  	 }
  	 
   	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
   	public void initialize(Taxa taxa){
   		if (taxa ==null)
   			return;
        	distanceTask.initialize(taxa);
   	}

	public TaxaDistance getTaxaDistance(Taxa taxa){
		if (distanceTask instanceof Incrementable)
			return new AvgDistances( taxa, this, distanceTask);
		return distanceTask.getTaxaDistance(taxa);
	}
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) { 
   	 	Snapshot temp = new Snapshot();
		temp.addLine("setDistanceSource ", distanceTask);
  	 	return temp;
  	 }
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	 if (checker.compare(this.getClass(), "Sets the source of distances for use in " + getName(), "[name of module]", commandName, "setDistanceSource")) { 
    	 		IncTaxaDistanceSource temp=  (IncTaxaDistanceSource)replaceEmployee(IncTaxaDistanceSource.class, arguments, "Source of distances to be averaged", distanceTask);
 			if (temp!=null) {
 				distanceTask= temp;
 				if (!MesquiteThread.isScripting())
 					parametersChanged(); 
 			}
 			return distanceTask;
 		}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
	}
	/*.................................................................................................................*/
   	public String getParameters() {
		return "Average from " +  distanceTask.getName() + ", with parameters: " + distanceTask.getParameters();
   	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Average of Several Distances";  
   	 }
   	 
	/*.................................................................................................................*/
   	 
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Average of several distances among taxa, e.g. average from several data matrices." ;
   	 }
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
   	 
}

class AvgDistances extends TaxaDistance {
	double[][] distances;
	int numTaxa;
 	IncTaxaDistanceSource distanceTask;
 	
 	
	public AvgDistances(Taxa taxa, MesquiteModule owner, IncTaxaDistanceSource distanceTask){
		super(taxa);
 		numTaxa = taxa.getNumTaxa();

		distances = new double[numTaxa][numTaxa];
		Double2DArray.deassignArray(distances);
		long min = distanceTask.getMin();
		long max = distanceTask.getMax();
		if (!MesquiteLong.isCombinable(min) || !MesquiteLong.isCombinable(max)) {
			min = 0;
			max = 9;
			owner.discreetAlert( "WARNING Average of distances will use default 10 items");
		}
		for (long d = min; d<=max; d++) {
			distanceTask.setCurrent(d);
			TaxaDistance dist = distanceTask.getTaxaDistance(taxa);
			if (dist != null) {
				for (int i=0; i<numTaxa; i++)
					for (int j=0; j<numTaxa; j++) {
						double dd = dist.getDistance(i,j);
						if (MesquiteDouble.isCombinable(dd)){
							distances[i][j] = MesquiteDouble.add(distances[i][j], dd);
						}
					}
			}
		}
		long n = max-min +1;
		for (int i=0; i<numTaxa; i++)
			for (int j=0; j<numTaxa; j++)
					if (MesquiteDouble.isCombinable(distances[i][j]))
						distances[i][j] /= n;
			
	}

	public double getDistance(int taxon1, int taxon2){
		if (taxon1>=0 && taxon1<numTaxa && taxon2>=0 && taxon2<numTaxa)
			return distances[taxon1][taxon2];
		else
			return MesquiteDouble.unassigned;
		
	}
	public double[][] getMatrix(){
		return distances;
	}
	public boolean isSymmetrical(){
		return true;
	}
}





