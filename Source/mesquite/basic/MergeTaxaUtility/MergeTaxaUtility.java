/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.basic.MergeTaxaUtility;

import java.awt.Checkbox;
import java.awt.Label;

import mesquite.assoc.lib.AssociationsManager;
import mesquite.assoc.lib.TaxaAssociation;
import mesquite.categ.lib.CategoricalData;
import mesquite.lib.IntegerField;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteThread;
import mesquite.lib.Notification;
import mesquite.lib.ResultCodes;
import mesquite.lib.StringUtil;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.TaxonMerger;
import mesquite.lib.duties.TaxonUtility;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.ui.AlertDialog;
import mesquite.lib.ui.ExtensibleDialog;
import mesquite.lib.ui.RadioButtons;


/* ======================================================================== */
public class MergeTaxaUtility extends TaxonUtility {

	TaxonMerger mergeTask;
	
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		mergeTask = (TaxonMerger)hireEmployee(TaxonMerger.class, "Method to Merge Taxa");
		if (mergeTask == null)
			return false;
		return true;
	}
	
	
	/*.................................................................................................................*/
	/** if returns true, then requests to remain on even after operateOnTaxa is called.  Default is false*/
	public boolean pleaseLeaveMeOn(){
		return false;
	}

	/*.................................................................................................................*/
	/** Called to operate on the taxa in the block.  Returns true if taxa altered*/
	public  boolean operateOnTaxa(Taxa taxa){
		int numSelected = taxa.numberSelected();
		if (numSelected<2){
			discreetAlert( "You need to select at least two taxa before merging them.");
			return false;
		}
		int numMatrices = getProject().getNumberCharMatrices(taxa);
		boolean[] selected = new boolean[taxa.getNumTaxa()];
		for (int it = 0; it<taxa.getNumTaxa(); it++) {
			selected[it] = taxa.getSelected(it);
		}
		StringBuffer report = new StringBuffer();
		boolean ok = mergeTask.queryOptions(taxa, selected, true, "Merge Selected Taxa", true);
		if (!ok)
			return false;
		int result = mergeTask.mergeTaxa(taxa, selected, null, report);
		boolean success = false;
		if (result == ResultCodes.SUCCEEDED){
			success = true;
			taxa.notifyListeners(this, new Notification(PARTS_CHANGED));
			for (int iM = 0; iM < numMatrices; iM++){
				CharacterData data = getProject().getCharacterMatrix(taxa, iM);
				data.notifyListeners(this, new Notification(PARTS_CHANGED));
			}
		}

		String r = report.toString();
		logln(r);
		if (!StringUtil.blank(r))
			discreetAlert(r);

		return success;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Merge Selected Taxa";
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Merge Selected Taxa...";
	}

	/*.................................................................................................................*/
	public String getExplanation() {
		return "Merges selected taxa and their character states.";
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 110;  //highly modified in 401
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}

}





