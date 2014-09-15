/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.assoc.StoredAssociations;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.assoc.lib.*;

/* ======================================================================== */
public class StoredAssociations extends AssociationSource implements MesquiteListener {
	int currentAssociationIndex=MesquiteInteger.unassigned;
	int currentAssociationID=MesquiteInteger.unassigned;
	AssociationsManager manager;
	TaxaAssociation currentAssociation = null;
	Taxa currentTaxa = null;
	Taxa currentTaxa2 = null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		manager = (AssociationsManager)findElementManager(TaxaAssociation.class);
		if (manager==null) {
			return sorry(getName() + " couldn't start because no associations manager was found.");
		}
		if (manager.getNumberOfAssociations()==0) {
			return sorry("No stored associations between taxa are available.");
		}
		addMenuItem("Choose Taxa Association...", makeCommand("chooseAssociation", this));
		return true;
	}
 	public String getKeywords(){
 		return "genes species";
 	}

	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	public void changed(Object caller, Object obj, Notification notification){
		if (Notification.appearsCosmetic(notification))
			return;
		parametersChanged(notification);
	}
	/*.................................................................................................................*/
	public void disposing(Object obj) {
		if (obj == currentAssociation || obj == currentTaxa) {
			parametersChanged();
		}
	}
	/*.................................................................................................................*/
	public boolean okToDispose(Object obj, int i) {
		//?? what to do here?
		return true;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		if (!MesquiteInteger.isCombinable(currentAssociationIndex))
			return null;
		Snapshot temp = new Snapshot();
			temp.addLine("setCurrentAssociation " + currentAssociationIndex); 
		return temp;
	}
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the current association number", "[number of current association]", commandName, "setCurrentAssociation")) {
			int c= MesquiteInteger.fromFirstToken(arguments, pos);
			if (MesquiteInteger.isCombinable(c)) {
				currentAssociationIndex = c;
				parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Sets the current association number by ID", "[ID of current association]", commandName, "setCurrentAssociationID")) {
			int c= MesquiteInteger.fromFirstToken(arguments, pos);
			if (MesquiteInteger.isCombinable(c)) {
				currentAssociationID = c;
				currentAssociationIndex = MesquiteInteger.unassigned;
				parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Choose the current association", null, commandName, "chooseAssociation")) {
			if (currentTaxa !=null) {
				TaxaAssociation t = chooseAssociation(containerOfModule(), currentTaxa);
				if (t!=null)
					parametersChanged();
				return t;
			}
			else
				discreetAlert( "Sorry, a taxa association cannot be chosen unless a set of taxa has been previously indicated.  The fact that you get this message probably means that there has been a programming error.");
		}
		else {
			return  super.doCommand(commandName, arguments, checker);
		}
		return null;
	}
	/*.................................................................................................................*/
	/*.................................................................................................................*/
	public int getNumberOfAssociations(Taxa taxa) {
		currentTaxa = taxa;
		return manager.getNumberOfAssociations(taxa);
	}
	/*.................................................................................................................*/
	public TaxaAssociation getAssociation(Taxa taxa, int ic) {
		currentAssociationIndex=ic;
		currentTaxa = taxa;
		return getCurrentAssociation(taxa);
	}
	/*.................................................................................................................*/
	public TaxaAssociation getCurrentAssociation(Taxa taxa) { 
		TaxaAssociation oldAssociation = currentAssociation;
		currentTaxa = taxa;
		boolean done = false;
		if (MesquiteInteger.isCombinable(currentAssociationID)&& !MesquiteInteger.isCombinable(currentAssociationIndex)){
			currentAssociation  =  manager.findAssociationByID(currentAssociationID, taxa);
			currentAssociationIndex = manager.getWhichAssociation(taxa, currentAssociation);
			if (currentAssociation != null){
				done = true;
			}
		}
 		if (!done){
			if (!MesquiteThread.isScripting() && !MesquiteInteger.isCombinable(currentAssociationIndex) && getNumberOfAssociations(taxa)>1)
				currentAssociation  =  chooseAssociation(containerOfModule(), taxa);
			else {
				if (getNumberOfAssociations(taxa) ==1)
					currentAssociationIndex = 0;
				else if (currentAssociationIndex>=getNumberOfAssociations(taxa) || currentAssociationIndex<0)
					return  null;
				currentAssociation = manager.getAssociation(taxa, currentAssociationIndex);
			}
		}
		if (currentAssociation!=null && currentAssociation!=oldAssociation){
			if (oldAssociation!=null)
				oldAssociation.removeListener(this);
			currentAssociation.addListener(this);
		}
		return currentAssociation;
	}
	/*.................................................................................................................*/
	public TaxaAssociation chooseAssociation(MesquiteWindow frame, Taxa taxa){
		int numAssoc = getNumberOfAssociations(taxa);
		String[] assocNames = new String[numAssoc];
		currentAssociation = manager.getAssociation(taxa, currentAssociationIndex);
		for (int i=0; i<numAssoc;i++) 
			assocNames[i] = manager.getAssociation(taxa, i).getName();
		int whichAssoc = ListDialog.queryList(frame, "Select Association", "Select an association between two taxa blocks", MesquiteString.helpString, assocNames, -1);
		if (!MesquiteInteger.isCombinable(whichAssoc)) 
			return null;
		if (whichAssoc>=0) { 
			currentAssociationIndex = whichAssoc;
			return  manager.getAssociation(taxa, whichAssoc);
		}
		else
			return manager.getAssociation(taxa, 0);
	}

	/*.................................................................................................................*/
	public int getNumberOfAssociations(Taxa taxa1, Taxa taxa2) {
		return manager.getNumberOfAssociations(taxa1, taxa2);
	}
	/*.................................................................................................................*/
	public TaxaAssociation getAssociation(Taxa taxa1, Taxa taxa2, int ic) {
		if (ic >= manager.getNumberOfAssociations(taxa1, taxa2))			return null;
		return manager.getAssociation(taxa1, taxa2, ic);
	}
	/*.................................................................................................................*/
	public String getAssociationNameString(Taxa taxa, int index) {
		currentTaxa = taxa;
		return manager.getAssociation(taxa, index).getName();
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Stored Taxa Associations";
	}

	/*.................................................................................................................*/
	public String getParameters() {
		if (currentAssociation !=null)
			return "Current association: " + currentAssociation.getName();
		return null;
	}
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Supplies associations between taxa that are stored, for instance in a file.";
	}
}

