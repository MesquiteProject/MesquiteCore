/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.TaxaBlocksListMerge;

import mesquite.lists.lib.*;

import java.util.*;
import java.awt.*;

import mesquite.charMatrices.BasicDataWindowCoord.BasicDataWindowCoord;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.TreeVector;
import mesquite.lib.ui.AlertDialog;
import mesquite.lib.ui.ListDialog;

/* ======================================================================== */
public class TaxaBlocksListMerge extends TaxaBlocksListUtility {
	/*.................................................................................................................*/
	public String getName() {
		return "Merge Taxa Blocks into Other";
	}
	public String getNameForMenuItem() {
		return "Merge Selected Taxa Blocks into Other...";
	}

	public String getExplanation() {
		return "Merges selected taxa blocks into a chosen one." ;
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	/*.................................................................................................................*/
	public boolean queryOptions() {
		if (!MesquiteThread.isScripting()){

			//	duplicateExcludedCharacters = !AlertDialog.query(containerOfModule(), "Remove excluded characters?","Remove excluded characters?", "Yes", "No");
		}
		return true;
	}

	/** if returns true, then requests to remain on even after operateOnTaxas is called.  Default is false*/
	public boolean pleaseLeaveMeOn(){
		return false;
	}
	/* - - - - - - - - - - - - - - - - - - -- - - - - - - - - - - - - - - - - - -- - - - - - - - - - - - - - - - - - - - */
	boolean mergeTaxaBlocks(Taxa donor, Taxa recipient){
		//Equalize the taxa blocks in taxa and in order
		boolean donorAdded = false;//donor.notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED));
		boolean recipientAdded = false;
		boolean reorderedDonor  = false; //donor.notifyListeners(this, new Notification(MesquiteListener.PARTS_MOVED));

		MesquiteProject recipientProject = recipient.getProject();
		MesquiteProject donorProject = donor.getProject();

		// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 
		//closing down windows of things to be deleted so as not to have graphics issues
		FileCoordinator bfc = donorProject.getCoordinatorModule();
		BasicDataWindowCoord bdwc = (BasicDataWindowCoord)bfc.findEmployeeWithDuty(BasicDataWindowCoord.class);
		for (int i =bdwc.getNumberOfEmployees()-1; i>=0; i--) {
			Object e=bdwc.getEmployeeVector().elementAt(i);
			if (e instanceof DataWindowMaker) {
				DataWindowMaker dwm = (DataWindowMaker)e;
				if (dwm.getCharacterData().getTaxa() == donor) {
					dwm.windowGoAway(dwm.getModuleWindow());
				}
			}
		}
		CharactersManager cManager = (CharactersManager)bfc.findEmployeeWithDuty(CharactersManager.class);
		for (int i =cManager.getNumberOfEmployees()-1; i>=0; i--) {
			Object e=cManager.getEmployeeVector().elementAt(i);
			if (e instanceof ListModule) {
				ListModule listModule = (ListModule)e;
				for (int iM = 0; iM < donorProject.getNumberCharMatrices(donor); iM++){
					CharacterData donorData = donorProject.getCharacterMatrix(donor, iM);
					if (listModule.showing(donorData))
						listModule.windowGoAway(listModule.getModuleWindow());
				}
			}
		}
		TaxaManager tManager = (TaxaManager)bfc.findEmployeeWithDuty(TaxaManager.class);
		for (int i =tManager.getNumberOfEmployees()-1; i>=0; i--) {
			Object e=tManager.getEmployeeVector().elementAt(i);
			if (e instanceof ListModule) {
				ListModule listModule = (ListModule)e;
				if (listModule.showing(donor))
					listModule.windowGoAway(listModule.getModuleWindow());
			}
		}
		// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 


		//adding taxa from donor that aren't in recipient
		for (int it = 0; it<donor.getNumTaxa(); it++){
			String donorName = donor.getTaxonName(it);
			if (recipient.whichTaxonNumber(donorName) <0){
				int rNumTaxa = recipient.getNumTaxa();
				recipient.addTaxa(rNumTaxa, 1, false);
				recipient.setTaxonName(rNumTaxa, donorName);
				recipient.equalizeParts(donor, it, rNumTaxa);
				recipientAdded = true;
			}
		}
		if (recipientAdded)
			recipient.notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED));

		//adding taxa from recipient that aren't in donor
		for (int it = 0; it<recipient.getNumTaxa(); it++){
			String recipientName = recipient.getTaxonName(it);
			if (donor.whichTaxonNumber(recipientName) <0){
				int dNumTaxa = donor.getNumTaxa();
				donor.addTaxa(dNumTaxa, 1, false);
				donor.setTaxonName(dNumTaxa, recipientName);
				donor.equalizeParts(recipient, it, dNumTaxa);
				donorAdded = true;
			}
		}
		if (donorAdded)
			donor.notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED));

		//reordering donor to match recipient
		reorderedDonor = donor.matchOrderIfEqual(recipient);
		if (reorderedDonor)
			donor.notifyListeners(this, new Notification(MesquiteListener.PARTS_MOVED));
		// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 


		//Now copying matrices from donor to recipient
		boolean setTaxaMatrixSucceeded = true;
		for (int iM = 0; iM < donorProject.getNumberCharMatrices(donor); iM++){
			CharacterData data = donorProject.getCharacterMatrix(donor, iM);
			CharacterData cloned = data.cloneData();
			cloned.addToFile(recipientProject.getHomeFile(),recipientProject, findElementManager(CharacterData.class));  
			data.copyMetadataTo(cloned); //this has to be after file has been set
			for (int ic = 0; ic<cloned.getNumChars(); ic++)
				cloned.equalizeParts(data, ic, ic);
			boolean sTMS = cloned.setTaxa(recipient, true);
			setTaxaMatrixSucceeded = setTaxaMatrixSucceeded && sTMS;
			cloned.setName(data.getName());
			cloned.setAnnotation(data.getAnnotation(), false);
			cloned.resetTaxaIDs(true);
			data.resetTaxaIDs(true);
		}
		if (!setTaxaMatrixSucceeded) {
			alert("Merging failed: the taxa block of a matrix could not be reset. The data file may be left in an unstable state. If you save it, use Save As so as not to overwrite the previous version.");
			return false;
		}
		// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 
		//Now copying tree blocks from donor to recipient
		boolean setTaxaTreesSucceeded = true;
		for (int iM = 0; iM < donorProject.getNumberTreeVectors(donor); iM++){
			TreeVector donorTrees = donorProject.getTreesByNumber(donor, iM);
			TreeVector receivingTrees = new TreeVector(recipient);
			receivingTrees.setWriteWeights(donorTrees.getWriteWeights());
			for (int i=0; i<donorTrees.size(); i++){
				MesquiteTree t = (MesquiteTree)donorTrees.elementAt(i);
				MesquiteTree cloned = t.cloneTree();
				boolean sTTS = cloned.setTaxa(recipient, true);
				for (int ic = 0; ic<cloned.getNumNodeSpaces() &&  ic<t.getNumNodeSpaces(); ic++){
					cloned.equalizeParts(t, ic, ic);
				}
				setTaxaTreesSucceeded = setTaxaTreesSucceeded && sTTS;
				receivingTrees.addElement(cloned, false);

			}
			receivingTrees.addToFile(recipientProject.getHomeFile(),recipientProject, findElementManager(TreeVector.class));  
			receivingTrees.setName(donorTrees.getName());
			receivingTrees.setAnnotation(donorTrees.getAnnotation(), false);
		}
		// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 
		if (!setTaxaTreesSucceeded) {
			alert("Merging failed: the taxa block of a tree block could not be reset. The data file may be left in an unstable state. If you save it, use Save As so as not to overwrite the previous version.");
			return false;
		}
		donor.deleteMe(false);
		return true;
	}
	/* - - - - - - - - - - - - - - - - - - - - */

	Taxa chooseOther(Taxa taxa, String expl){
		MesquiteProject project = taxa.getProject();
		Taxa other = null;
		ListableVector v = new ListableVector();
		for (int iT = 0; iT < project.getNumberTaxas(); iT++){
			Taxa c = taxa.getProject().getTaxa(iT);
			if (c != taxa && c.equals(taxa, true, true))
				v.addElement(c, false);
		}
		if (v.size() == 0)
			return null;
		else if (v.size() == 1){
			other = (Taxa)v.elementAt(0);
		}
		else {
			Listable result = ListDialog.queryList(containerOfModule(), "Choose taxa block", expl, null, v, 0); 
			if (result == null)
				return null;
			other = (Taxa)result;
		}
		return other;
	}
	/* - - - - - - - - - - - - - - - - - - - - */


	/** Called to operate on the Taxa blocks.  Returns true if taxa altered*/
	public boolean operateOnTaxas(ListableVector taxas, MesquiteTable table){
		Taxa recipient = (Taxa)taxas.elementAt(0);
		int numRowsSelected = table.numRowsSelected();
		if (taxas.size() == 1){
			alert("There is only one taxa block in the project, and hence you cannot merge taxa blocks.");
			return false;
		}
		else if (numRowsSelected == 0){
			alert("To merge taxa blocks, you need to first select one or more source taxa blocks that will be merged into a recipient taxa block.");
			return false;
		}
		else if (numRowsSelected == taxas.size()){
			alert("To merge taxa blocks, you need to first select one or more source taxa blocks that will be merged into a recipient taxa block. "
					+"You cannot select all of the taxa blocks, because then there will be no available recipient into which to merge them.");
			return false;
		}
		// next, to choose which of the unselected blocks to be the recipient
		ListableVector vv = new ListableVector();
		for (int iT = 0; iT < taxas.size(); iT++){
			if (!table.isRowSelected(iT))
				vv.addElement(taxas.elementAt(iT), false);
		}
		if (vv.size() == 1){
			recipient = (Taxa)vv.elementAt(0);
		}
		else {
			Listable result = ListDialog.queryList(containerOfModule(), "Into which block?", "Into which block would you like to merge the selected blocks?", null, vv, 0); 
			if (result == null)
				return false;
			recipient = (Taxa)result;
		}
		String message = "The selected taxa blocks will be deleted after their taxa, matrices, and trees have been merged into the taxa block \"" + recipient.getName() + "\"."
				+ " This cannot be undone.\n\nYou are strongly advised to save a copy of the file before doing this, because some information such as important metadata, footnotes or colors might be lost. Also, some ongoing analyses might be disrupted. "
				+ " Would you like to continue with the merger?";
		boolean contn = AlertDialog.query(containerOfModule(), "Merge and delete taxa blocks?", message, "Merge", "Cancel", 1);
		if (!contn)
			return false;
		getProject().incrementProjectWindowSuppression();
		Vector v = pauseAllPausables();
		for (int im = taxas.size()-1; im >=0; im--){
			if (table.isRowSelected(im)){
				Taxa donor = (Taxa)taxas.elementAt(im);
				mergeTaxaBlocks(donor, recipient);
			}
		}
		unpauseAllPausables(v);
		getProject().decrementProjectWindowSuppression();
		//projectWindow.projPanel.refresh();

		resetAllMenuBars();
		return true;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
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
	public void endJob() {
		super.endJob();
	}


}

