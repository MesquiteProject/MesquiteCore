/* Mesquite source code (Rhetenor package).  Copyright 1997 and onward E. Dyreson and W. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.rhetenor.CanonicalVariates;
/*~~  */

import mesquite.cont.lib.MContinuousDistribution;
import mesquite.lib.EmployeeNeed;
import mesquite.lib.duties.TaxaPartitionSource;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.taxa.TaxaPartition;
import mesquite.rhetenor.lib.CVAOrdination;
import mesquite.rhetenor.lib.Ordination;
import mesquite.rhetenor.lib.Ordinator;

/* ======================================================================== */
public class CanonicalVariates extends Ordinator {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(TaxaPartitionSource.class, getName() + "  needs a source for a taxon partition.",
		"The source of taxon partition is selected initially");
	}
	/*.................................................................................................................*/
	CVAOrdination ord;
	TaxaPartitionSource partitionSource;
	boolean first = true;
	
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		partitionSource = (TaxaPartitionSource)hireEmployee(TaxaPartitionSource.class, "Source of Taxa Partitions");
 		if (partitionSource == null)
 			return sorry(getName() + " couldn't start because no source of taxa partition obtained.");
 		return true;
 	}
 	
	/*.................................................................................................................*/
 	public Ordination getOrdination(MContinuousDistribution matrix, int item, Taxa taxa){
		TaxaPartition part = partitionSource.getPartition(taxa);
		if (part== null) {
			if (first)
				discreetAlert( "Sorry, Canonical Variates Analysis cannot be performed because there is no available partition of the taxa");
			first = false;
			iQuit();
			return null;
		}
		double[][] x = matrix.getMatrix(item);//gets first 2Dmatrix from original
 		return new CVAOrdination(x, part);
 	}
	/*.................................................................................................................*/
    	public String getName() {
		return "Canonical Variates Analysis";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Performs canonical variates analysis on a continous-valued matrix." ;
   	 }
   	 
	/*.................................................................................................................*/
   	 public boolean showCitation(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
	/*.................................................................................................................*
    	 public String getParameters() {
   		if (ord==null)
   			return null;
   		else
   			return "Canonical Variates\n"+ ord.report();
   	 }
   	 */
}

