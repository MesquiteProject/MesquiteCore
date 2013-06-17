package mesquite.basic.DeleteTaxonFootnotes;

import mesquite.lib.*;
import mesquite.lib.duties.TaxonUtility;

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
