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

import java.util.*;
import java.awt.*;

import mesquite.lib.characters.*;
import mesquite.lib.characters.CharacterData;
import mesquite.categ.lib.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.basic.MergeTaxa.*;


/* ======================================================================== */
public class MergeTaxaByName extends MergeTaxa {
	static String boundaryString = "";
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		return super.startJob(arguments, condition, hiredByName);
	}
	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("startLengthToKeep".equalsIgnoreCase(tag)) {
			//startLengthToKeep = MesquiteInteger.fromString(content);
		} 
		super.processSingleXMLPreference ( tag,  content);
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(super.preparePreferencesForXML());
		StringUtil.appendXMLTag(buffer, 2, "startLengthToKeep", 0);  
		return buffer.toString();
	}
	protected String getDialogTitle(){
		return "Merge Taxa with Matching Names";
	}
	protected void harvestOther(){
		boundaryString = searchField.getText();

	}
	protected String getHelpStringStart() {
		return "This will merge taxa whose names start with the same text.  The part of the names that is compared is the portion in front of the specified delimitation text string. If a name does not contain the delimitation text, then the entire name is compared. " ;
	}
	SingleLineTextField searchField;
	protected void addQueryItems(ExtensibleDialog queryDialog){
		queryDialog.addHorizontalLine(1);
		searchField = queryDialog.addTextField("Text that forms the boundary between the start and end of the taxon name:", boundaryString, 12, true);
		queryDialog.addLabel("(Taxa whose names have the same start will be merged)");
	}
	protected boolean permitRetainOriginal(){
		return false;
	}

	/*.................................................................................................................*/
	/** if returns true, then requests to remain on even after operateOnTaxa is called.  Default is false*/
	public boolean pleaseLeaveMeOn(){
		return false;
	}


	/*.................................................................................................................*/
	/** Called to operate on the taxa in the block.  Returns true if taxa altered*/
	public  boolean operateOnTaxa(Taxa taxa){
		//Debugg.println: constrain by selected taxa

		int numMatrices = getProject().getNumberCharMatrices(taxa);
		boolean nonCategFound = false;

		for (int iM = 0; iM < numMatrices; iM++){
			CharacterData data = getProject().getCharacterMatrix(taxa, iM);
			if (!(data instanceof CategoricalData)){
				nonCategFound = true;
			} 
		}
		if (nonCategFound)
			discreetAlert( "Some character matrices are neither categorical nor molecular (e.g. are continuous, meristic). For these matrices, if more than one of the merged taxa have states in some characters, only the first taxon's states will be kept.");

		if (!MesquiteThread.isScripting()){
			boolean OK = AlertDialog.query(containerOfModule(), "Merge?", "Are you sure you want to merge taxa whose names start the same?  "
					+ " We recommend you save a version of the data file before you do this.  You will not be able to undo this.  " 
					+ "Their character states in their character matrices will be merged.  " 
					+ "Other associated information like footnotes, attachments, and so forth WILL NOT be merged and will be lost from all but the first taxon.");
			if (!OK)
				return false;
			if (!queryOptions(0, null))
				return false;
		}

		boolean success = true; 
		StringBuffer report = new StringBuffer();
		for (int it=0; it< taxa.getNumTaxa(); it++){
			String baseName = getBaseName(taxa.getTaxonName(it));
			boolean[] selected = new boolean[taxa.getNumTaxa()];
			selected[it] = true;
			for (int itt = it+1; itt<taxa.getNumTaxa(); itt++) {
				String baseNameThis = getBaseName(taxa.getTaxonName(itt));
				selected[itt] = baseNameThis != null && baseNameThis.equals(baseName);
			}
			int numSelected = 0;
			for (int i = 0; i< selected.length; i++)
				if (selected[i]) numSelected++;
			if (numSelected>1){
				boolean successThis = doMerge(taxa, selected, report);
				taxa.notifyListeners(this, new Notification(PARTS_DELETED));
				success = successThis || success;
			}
		}
		taxa.notifyListeners(this, new Notification(PARTS_DELETED));
		for (int iM = 0; iM < numMatrices; iM++){
			CharacterData data = getProject().getCharacterMatrix(taxa, iM);
			data.notifyListeners(this, new Notification(PARTS_DELETED));
		}

		if (report.length()>0){
			logln(report.toString());
		}
		return success;
	}
	String getBaseName(String name){
		if (name == null)
			return null;
		int bpos = name.indexOf(boundaryString);
		if (bpos>=0)
			return name.substring(0,bpos);
		return name;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Merge Taxa With Same Start To Name";
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Merge Taxa With Same Start To Name...";
	}

	/*.................................................................................................................*/
	public String getExplanation() {
		return "Finds taxa whose names are the same in the first part (before some target string), and merges them and their character states.";
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
		return true;
	}

	/*.................................................................................................................*/
	/**Returns true if the module is to appear in menus and other places in which users can choose, and if can be selected in any way other than by direct request*/
	public boolean loadModule(){
		return false; 		//Debugg.println: not ready for prime time

	}

}





