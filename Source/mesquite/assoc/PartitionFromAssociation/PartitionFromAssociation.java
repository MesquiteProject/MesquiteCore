/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.assoc.PartitionFromAssociation;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.assoc.lib.*;

/* ======================================================================== */
public class PartitionFromAssociation extends TaxaPartitionSource {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e2 = registerEmployeeNeed(AssociationSource.class, getName() + " needs a source of taxa associations, from which to derive a taxa partition (grouping).",
		"The source of assocation is chosen initially");
	}
	AssociationSource associationTask;
	TaxaAssociation association = null;
	Taxa containingTaxa;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		associationTask = (AssociationSource)hireEmployee(AssociationSource.class, "Source of taxon associations");
		if (associationTask == null)
			return sorry(getName() + " couldn't start because no source of taxon associations was found.");
		return true;
  	 }
  	 
	/*.................................................................................................................*/
   	/** returns partition for taxa */
   	public TaxaPartition getPartition(Taxa taxa){
        	if (association == null || (association.getTaxa(0)!= taxa && association.getTaxa(1)!= taxa)) {
        		association = associationTask.getCurrentAssociation(taxa);
        		if (association == null)
        			association = associationTask.getAssociation(taxa, 0);
        		if (association == null)
        			return null;
        		if (association.getTaxa(0)== taxa)
        			containingTaxa = association.getTaxa(1);
        		else
        			containingTaxa = association.getTaxa(0);
        	}
        	/*go through association.  Make as many partitions as there are containing taxa.
        	If a contained taxon has more than one associate, don't assign it to 
        	*/
        	TaxaPartition partition = new TaxaPartition("Partition from Association", taxa.getNumTaxa(), null, taxa);
        	for (int i= 0; i<containingTaxa.getNumTaxa(); i++){
        		Taxon containingTaxon = containingTaxa.getTaxon(i);
        		Taxon[] contained = association.getAssociates(containingTaxon);
        		if (contained !=null && contained.length>0){
				TaxaGroup group = new TaxaGroup();
				group.setName("In " + containingTaxon.getName());
				for (int j = 0; j< contained.length; j++){
					if (association.getNumAssociates(contained[j])<=1)
						partition.setProperty(group,taxa.whichTaxonNumber(contained[j]));
				}
			}
		}
		return partition;
   	}
   	
	/*.................................................................................................................*/
    	 public String getName() {
		return "Taxa partition from Taxa associations";
   	 }
   	 
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Constructs a taxa partition from an association." ;
   	 }
	/*.................................................................................................................*/
  	 public CompatibilityTest getCompatibilityTest() { //getCompatibilityTest
  	 	return new PartCompatibilityTest();
  	 }
}
class PartCompatibilityTest extends CompatibilityTest{
//should find out if available association of chosen sort
	public  boolean isCompatible(Object obj, MesquiteProject project, EmployerEmployee prospectiveEmployer){
		AssociationsManager manager = null;
		if (prospectiveEmployer == null) {
			if (project!=null){
				MesquiteModule coord = project.getCoordinatorModule();
				if (coord!=null){
					manager = (AssociationsManager)coord.findElementManager(TaxaAssociation.class);
				}
			}
		}	
		else
			manager = (AssociationsManager)prospectiveEmployer.findElementManager(TaxaAssociation.class);
		if (manager==null) {
			return true;
		}
		if (obj !=null && obj instanceof Taxa)
			return (manager.getNumberOfAssociations((Taxa)obj)>0);
		else
			return (manager.getNumberOfAssociations()>0);
		
	}
}

