/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.basic.DeleteTaxonFootnotes;

import mesquite.lib.MesquiteListener;
import mesquite.lib.Notification;
import mesquite.lib.duties.TaxonUtility;
import mesquite.lib.taxa.Taxa;

public class DeleteTaxonFootnotes extends TaxonUtility {


	/* ................................................................................................................. */
	public boolean startJob(String arguments, Object condition,boolean hiredByName) {
		
		return true;
	}
	
	/* ................................................................................................................. */
	public String getName() {
		return "Delete Footnotes from Taxa";
	}

	/* ................................................................................................................. */
	public String getExplanation() {
		return "Deletes footnotes from taxa";
	}

	/* ................................................................................................................. */
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice() {
		return false;
	}

	/* ................................................................................................................. */
	public boolean isPrerelease() {
		return false;
	}

	/* ................................................................................................................. */
	public boolean isSubstantive() {
		return false;
	}
	/* ................................................................................................................. */
   	/** if returns true, then requests to remain on even after operateOnTaxa is called.  Default is false*/
   	public boolean pleaseLeaveMeOn(){
   		return false;
   	}
   	/** Called to operate on the taxa in the block.  Returns true if taxa altered*/
   	public boolean operateOnTaxa(Taxa taxa){
		for (int it = 0; it< taxa.getNumTaxa(); it++){
			if (!taxa.anySelected() || taxa.getSelected(it))
				taxa.setAnnotation(it, null);
		}
		taxa.notifyListeners(this, new Notification(MesquiteListener.ANNOTATION_CHANGED));
		return true;
  	}


}
