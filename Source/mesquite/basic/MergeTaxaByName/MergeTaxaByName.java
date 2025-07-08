/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.basic.MergeTaxaByName;

import java.awt.Checkbox;
import java.awt.Label;
import java.util.Vector;

import mesquite.assoc.lib.AssociationsManager;
import mesquite.assoc.lib.TaxaAssociation;
import mesquite.categ.lib.CategoricalData;
import mesquite.lib.IntegerField;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteThread;
import mesquite.lib.NameParser;
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
public class MergeTaxaByName extends TaxonUtility {

	TaxonMerger mergeTask;
	NameParser nameParser;

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		mergeTask = (TaxonMerger)hireEmployee(TaxonMerger.class, "Method to Merge Taxa");
		if (mergeTask == null)
			return false;
		nameParser = new NameParser(this, "taxon");
		loadPreferences();
		return true;
	}



	/*.................................................................................................................*/
	/** if returns true, then requests to remain on even after operateOnTaxa is called.  Default is false*/
	public boolean pleaseLeaveMeOn(){
		return false;
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer();
		if (nameParser!=null){
			String s = nameParser.preparePreferencesForXML(); 
			if (StringUtil.notEmpty(s))
				buffer.append(s);
		}
		return buffer.toString();
	}

	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if (nameParser!=null)
			nameParser.processSingleXMLPreference(tag,content);
	}

	/*.................................................................................................................*/
	/** Called to operate on the taxa in the block.  Returns true if taxa altered*/
	public  boolean operateOnTaxa(Taxa taxa){

		if (taxa.getNumTaxa() < 2){
			logln("There must be at least two taxa to merge.");
			return false;
		}
		nameParser.setExamples(new String[]{taxa.getTaxonName(0), taxa.getTaxonName(taxa.getNumTaxa()-1)});
		if (nameParser.queryOptions("Options for Matching Taxon Names", "Taxon Name Components for Matching.", "In choosing what parts of the taxon name need to match for the taxa to be merged",null)){
			storePreferences();
		}
		else return false;

		boolean atLeastOneMerger = false;
		StringBuffer report = new StringBuffer();
		boolean ok = mergeTask.queryOptions(taxa, null, false, "Merge Taxa With Matching Name Components", false);
		if (!ok)
			return false;
		boolean[] selected = new boolean[taxa.getNumTaxa()];
		for (int it = 0; it< taxa.getNumTaxa(); it++){
			for (int s = 0; s<selected.length; s++)
				selected[s] = false;
			selected[it] = true;
			String namePart = nameParser.extractPart(taxa.getTaxonName(it));
			report.setLength(0);
			if (StringUtil.notEmpty(namePart)){
				boolean otherFound = false;
				for (int itOther = it+1; itOther<taxa.getNumTaxa(); itOther++){
					String namePartOther = nameParser.extractPart(taxa.getTaxonName(itOther));
					if (namePart.equals(namePartOther)) {
						selected[itOther] = true;
						otherFound = true;
					}
				}
				if (otherFound){
					logln("\nTaxa being merged:");
					for (int iMg = 0; iMg<selected.length; iMg++){
						if (selected[iMg]){
							logln(" " + taxa.getTaxonName(iMg));
						}
					}
					logln("\n");
					
					//======= MERGING =======
					int result = mergeTask.mergeTaxa(taxa, selected, namePart+ " (merged)", report);
					//======================
					if (result == ResultCodes.SUCCEEDED) {
						atLeastOneMerger = true;
						logln(report.toString());
					}
				}
			}

		}
		if (atLeastOneMerger){
			taxa.notifyListeners(this, new Notification(PARTS_CHANGED));
			int numMatrices = getProject().getNumberCharMatrices(taxa);
			for (int iM = 0; iM < numMatrices; iM++){
				CharacterData data = getProject().getCharacterMatrix(taxa, iM);
				data.notifyListeners(this, new Notification(PARTS_CHANGED));
			}
		}
		else
		discreetAlert("Sorry, no matching taxa were found to merge.");

		//String r = report.toString();
	//	logln(r);
		//if (!StringUtil.blank(r))
		//	discreetAlert(r);

		return atLeastOneMerger;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Merge Taxa by Name Matching";
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Merge Taxa by Name Matching...";
	}

	/*.................................................................................................................*/
	public String getExplanation() {
		return "Merges taxa that match in a component of their names, and their character states.";
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}

}





