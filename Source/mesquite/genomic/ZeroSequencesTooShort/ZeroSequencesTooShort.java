/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.genomic.ZeroSequencesTooShort;
/*~~  */

import java.util.*;
import java.lang.*;
import java.awt.*;
import java.awt.event.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import mesquite.lib.table.*;
import mesquite.lib.ui.ExtensibleDialog;



/* ======================================================================== */
public class ZeroSequencesTooShort extends MolecularDataAlterer  implements AltererAlignShift {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		return true;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return false;  
	}
	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("longEnough".equalsIgnoreCase(tag))
			longEnough= MesquiteInteger.fromString(content);
		else if ("removeGapsOnly".equalsIgnoreCase(tag))
			removeGapsOnly= MesquiteBoolean.fromOffOnString(content);
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(60);	
		StringUtil.appendXMLTag(buffer, 2, "longEnough",longEnough);
		StringUtil.appendXMLTag(buffer, 2, "removeGapsOnly",MesquiteBoolean.toOffOnString(removeGapsOnly));

		return buffer.toString();
	}

	boolean lengthButNotEnough(MolecularData data, int it, int enough, CharacterState cs){
		int seqLen = 0;
		for (int ic=0; ic<data.getNumChars(); ic++) {
			cs = data.getCharacterState(cs, ic, it);
			if (!cs.isInapplicable())
				seqLen++;
			if (seqLen>=enough)
				return true;
		}
		return seqLen == 0;  // if all inapplicable, treat it as enough!
	}  	
	
	int longEnough = 100;
	boolean removeGapsOnly = true;
	/*.................................................................................................................*/
	public boolean queryOptions() {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), "Remove Very Short Sequences",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
		dialog.addLabel("Delete data for taxon from matrix if its sequence length is less than the threshold");
		IntegerField numField = dialog.addIntegerField("Threshold", longEnough, 8, 0, MesquiteInteger.unassigned);
		Checkbox cb = dialog.addCheckBox("Remove gaps-only characters also", removeGapsOnly);
		
		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			longEnough = numField.getValue();
			removeGapsOnly = cb.getState();
			storePreferences();
		}
		dialog.dispose();
		return (buttonPressed.getValue()==0);
	}
	/*.................................................................................................................*/
	/** Called to alter data in those cells selected in table*/
	public int alterData(CharacterData cData, MesquiteTable table,  UndoReference undoReference){
		if (!(cData instanceof MolecularData))
			return ResultCodes.INCOMPATIBLE_DATA;
		if (okToInteractWithUser(CAN_PROCEED_ANYWAY, "Querying about options")){ //need to check if can proceed
			if (!queryOptions())
				return ResultCodes.USER_STOPPED;
		}
		boolean found = false;
		MolecularData data = (MolecularData)cData;
		CharacterState cs = null;
		boolean changed = false;
		for (int it = 0; it<data.getNumTaxa(); it++) {
			if (!lengthButNotEnough(data, it, 100, cs)){
				data.setToInapplicable(it);
				logln("Sequence too short in " + data.getTaxa().getTaxonName(it) + " for matrix " + data.getName());
				changed = true;
			}
		}
		if (removeGapsOnly) {
			int oldNum = data.getNumChars();
			data.removeCharactersThatAreEntirelyGaps(false);
			changed = changed || oldNum!=data.getNumChars();
		}

		if (changed){
			data.notifyListeners(this, new Notification(CharacterData.DATA_CHANGED));
			data.notifyInLinked(new Notification(MesquiteListener.DATA_CHANGED));
		}
		return ResultCodes.SUCCEEDED;
	}
	/*.................................................................................................................*/
	public boolean showCitation() {
		return false;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return true;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Remove Very Short Sequences...";
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Remove Very Short Sequences";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Removes data for a taxon in a matrix if the sequence length is too short (but without deleting taxon). Does not consider what is selected." ;
	}

}


