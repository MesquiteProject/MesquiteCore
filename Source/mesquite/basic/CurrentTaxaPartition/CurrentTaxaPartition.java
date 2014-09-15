/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.basic.CurrentTaxaPartition;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/** Supplies the current partition of taxa. */
public class CurrentTaxaPartition extends TaxaPartitionSource implements MesquiteListener {
	Taxa currentTaxa = null;
	TaxaPartition currentPartition = null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
  	 }
  	 
	/*.................................................................................................................*/
   	 public boolean isSubstantive(){
   	 	return false;
   	 }
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
	/*.................................................................................................................*/
	public void endJob() {
		if (currentTaxa!=null)
			currentTaxa.removeListener(this);
		if (currentPartition!=null)
			currentPartition.removeListener(this);
		super.endJob();
  	 }
	/** passes which object was disposed*/
	public void disposing(Object obj){
		if (obj == currentTaxa || obj == currentPartition)
			parametersChanged();
	}
	/*.................................................................................................................*/
	public void changed(Object caller, Object obj, Notification notification){
		if (Notification.appearsCosmetic(notification))
			return;
		if (obj == currentTaxa || obj == currentPartition)
			parametersChanged(notification);
	}
   	/** returns partition for taxa */
   	public TaxaPartition getPartition(Taxa taxa){
		if (currentTaxa != taxa) {
			if (currentTaxa !=null)
				currentTaxa.removeListener(this);
			taxa.addListener(this);
			currentTaxa = taxa;
		}
		TaxaPartition partition = (TaxaPartition)taxa.getCurrentSpecsSet(TaxaPartition.class);
		if (currentPartition != partition) {
			if (currentPartition !=null)
				currentPartition.removeListener(this);
			partition.addListener(this);
			currentPartition = partition;
		}
		return currentPartition;
   	}
   	
	/*.................................................................................................................*/
    	 public String getName() {
		return "Current taxa partition";
   	 }
   	 
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Supplies the current taxa partition." ;
   	 }
}

