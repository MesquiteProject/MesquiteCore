/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.minimal.StoredTaxa;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/** Supplies taxa from taxa blocks stored in the project.*/
public class StoredTaxa extends TaxonSource implements MesquiteListener {
	public String getName() {
		return "Stored Taxa";
	}
	public String getExplanation() {
		return "Supplies taxa stored, for instance in a file.";
	}
	/*.................................................................................................................*/
	int currentTaxon=0;
	Taxa currentTaxa = null;
	TreesManager manager;
	MesquiteSubmenuSpec listSubmenu;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (getProject().getNumberTaxas()==0)
			return sorry(getName() + " couldn't start because no blocks of taxa are available.");
		//listSubmenu = addSubmenu(null, "Taxa", makeCommand("setTaxaInt",  this), getProject().getTaxas());
		return true;
	}
	public void resetCurrentTaxa(Taxa taxa){
		if (currentTaxa!=null)
			currentTaxa.removeListener(this);
		currentTaxa = taxa;
		if (currentTaxa!=null)
			currentTaxa.addListener(this);
	}
	public void initialize(Taxa taxa){
		if (taxa !=currentTaxa || currentTaxa == null)
			resetCurrentTaxa(taxa);
	}
	public Selectionable getSelectionable(){
		return currentTaxa;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("setTaxa " + getProject().getTaxaReferenceExternal(currentTaxa)); 
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		/*
disabled in 1. 06 because wasn't working (too many functions made early commitment to taxa which couldn't be changed
    	 	if (checker.compare(this.getClass(), "Sets which block of taxa to use (0 based, for internal use)", "[block number]", commandName, "setTaxaInt")) { //need separate from setTreeBlock since this is used internally with 0-based menu response
    	 		int whichList = MesquiteInteger.fromString(arguments, new MesquiteInteger(0));
    	 		if (MesquiteInteger.isCombinable(whichList)) {
	    	 		if (currentTaxa!=null)
	    	 			currentTaxa.removeListener(this);
	    	 		currentTaxa = getProject().getTaxa(checker.getFile(), whichList);
	    	 		if (currentTaxa!=null)
	    	 			currentTaxa.addListener(this);
	    	 		parametersChanged();
	 			return currentTaxa;
 			}
    	 	}
    	 	else */
		if (checker.compare(this.getClass(), "Sets which block of taxa to use", "[block reference, number, or name]", commandName, "setTaxa")) {
			Taxa t = getProject().getTaxa(checker.getFile(), parser.getFirstToken(arguments));
			if (t!=null){
				resetCurrentTaxa(t);
				parametersChanged();
				return currentTaxa;
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	public void endJob(){
		if (currentTaxa !=null)
			currentTaxa.removeListener(this);
		super.endJob();
	}
	/** passes which object changed, along with optional integer (e.g. for character)*/
	public void changed(Object caller, Object obj, Notification notification){
		int code = Notification.getCode(notification);
		if (!doomed && obj==currentTaxa && !(code == MesquiteListener.SELECTION_CHANGED || code == MesquiteListener.ANNOTATION_CHANGED || code == MesquiteListener.ANNOTATION_ADDED || code == MesquiteListener.ANNOTATION_DELETED))
			parametersChanged(notification);
	}
	/** passes which object was disposed*/
	public void disposing(Object obj){
		if (obj instanceof Taxa && (Taxa)currentTaxa == obj)
			iQuit();
	}
	/** Asks whether it's ok to delete the object as far as the listener is concerned (e.g., is it in use?)*/
	public boolean okToDispose(Object obj, int queryUser){
		return true;
	}
	boolean first = true;


	/*.................................................................................................................*/
	public Taxon getCurrentTaxon(Taxa taxa) {
		if (taxa==null)
			return null;
		if (taxa !=currentTaxa || taxa == null){
			resetCurrentTaxa(taxa);
		}
		if (currentTaxon<0 || currentTaxon>taxa.getNumTaxa())
			return null;
		return taxa.getTaxon(currentTaxon);
	}
	/*.................................................................................................................*/
	public Taxon getFirstTaxon(Taxa taxa) {
		currentTaxon=0;
		return getCurrentTaxon(taxa);
	}
	/*.................................................................................................................*/
	public Taxon getTaxon(Taxa taxa, int iTaxon) {
		currentTaxon=iTaxon;
		return getCurrentTaxon(taxa);
	}
	/*.................................................................................................................*/
	public Taxon getNextTaxon(Taxa taxa) {
		currentTaxon++;
		return getCurrentTaxon(taxa);
	}
	/*.................................................................................................................*/
	public int getNumberOfTaxa(Taxa taxa) {
		if (taxa==null)
			return 0;
		else
			return taxa.getNumTaxa();
	}

	/*.................................................................................................................*/
	public String getTaxonName(Taxa taxa, int iTaxon) {
		if (taxa==null)
			return null;
		else {
			return taxa.getTaxonName(iTaxon);
		}
	}
}

