/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.NCBLOCKSSelector;
/*~~  */

import mesquite.categ.lib.CategoricalData;
import mesquite.categ.lib.CategoricalState;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class NCBLOCKSSelector extends CharacterSelector {


	MesquiteString xmlPrefs= new MesquiteString();
	String xmlPrefsString = null;
	double proportionIncomp = 0.4;
	int spanSize = 12;
	boolean treatGapAsState = false;

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences(xmlPrefs);
		xmlPrefsString = xmlPrefs.getValue();
		return true;
	}
	/*.................................................................................................................*/
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "proportionIncomp", proportionIncomp);  
		StringUtil.appendXMLTag(buffer, 2, "spanSize", spanSize);  

		return buffer.toString();
	}
	public void processSingleXMLPreference (String tag, String flavor, String content){
		processSingleXMLPreference(tag, null, content);
	}

	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("proportionIncomp".equalsIgnoreCase(tag))
			proportionIncomp = MesquiteDouble.fromString(content);
		if ("spanSize".equalsIgnoreCase(tag))
			spanSize = MesquiteInteger.fromString(content);

	}
	DoubleField PIField=null;
	IntegerField SSField =null;

	public boolean queryOptions(MesquiteModule mb, String action) {
		if (!mb.okToInteractWithUser(mb.CAN_PROCEED_ANYWAY, "Querying Options")) 
			return true;
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(mb.containerOfModule(),  "Select using NCBLOCKS Algorithm",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()

		PIField = dialog.addDoubleField("Proportion of neighbouring sites in conflict", proportionIncomp, 4);
		SSField = dialog.addIntegerField("Length of blocks", spanSize, 4);



		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			proportionIncomp = PIField.getValue();
			spanSize = SSField.getValue();
			storePreferences();
		}
		dialog.dispose();
		return (buttonPressed.getValue()==0);
	}

	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return true;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	boolean[] siteStatus;
	boolean[] toSelect;
	int NUMSTATES = 4;


	int[][] counts;
	int getState(long stateSet) {
		if (treatGapAsState && CategoricalState.isUnassigned(stateSet))
			return NUMSTATES-1;
		if (CategoricalState.isCombinable(stateSet) && !CategoricalState.hasMultipleStates(stateSet))
			return CategoricalState.getOnlyElement(stateSet);
		return -1;
	}

	boolean nextIsIncompatible(CategoricalData data, int ic) {
		if (ic+1>=data.getNumChars() || ic <0)
			return false;
		NUMSTATES = data.getMaxState()+2; //one extra for gaps as state
		if (counts == null || counts.length != NUMSTATES)
			counts = new int[NUMSTATES][NUMSTATES];
		for (int i =0; i<NUMSTATES; i++) for (int k =0; k<NUMSTATES;k++) counts[i][k]= 0;

		//first, harvest all patterns between the two columns
		for (int it = 0; it <= data.getNumTaxa(); it++) {
			int state1 = getState(data.getState(ic, it));
			int state2 = getState(data.getState(ic+1, it));
			if (state1 >=0 && state1<NUMSTATES && state2 >=0 && state2<NUMSTATES)
				counts[state1][state2]++;
		}
		//Now, for all pairs of states, see if all 4 patterns are present (00, 01, 10, 11)
		for (int i =0; i<NUMSTATES; i++) 
			for (int k =0; k<NUMSTATES;k++) {
				if (i != k && counts[i][i]>0 && counts[i][k]>0 && counts[k][i]>0 && counts[k][k]>0) {
					return true;
				}
			}
		return false;
	}
	void selectSpanByProportion(int ic, boolean[] siteStatus, boolean[] toSelect, double proportionIncomp, int spanSize) {
		if (!siteStatus[ic])
			return;
		int count = 0;
		int lastHit = -1;
		for (int k = 0; k<spanSize && ic+k<siteStatus.length; k++) {
			if (siteStatus[ic+k]) {
				count++;
				lastHit = ic+k;
			}
		}
		if (1.0*count/spanSize >= proportionIncomp) {
			for (int k = ic; k<=lastHit && k<toSelect.length; k++) {
				toSelect[k]=true;
			}
		}

	}

	/*.................................................................................................................*/
	/** Called to select characters*/
	public void selectCharacters(CharacterData data){
		if (data!=null && data.getNumChars()>0 && data instanceof CategoricalData){
			if (!queryOptions(this, "Select"))
				return;
			if (siteStatus == null || siteStatus.length != data.getNumChars()) {
				siteStatus = new boolean[data.getNumChars()];
				toSelect = new boolean[data.getNumChars()];
			}
			for (int ic=0; ic<siteStatus.length; ic++) {
				siteStatus[ic] = false;
				toSelect[ic] = false;
			}


			for (int ic=0; ic<data.getNumChars(); ic++)
				if (nextIsIncompatible((CategoricalData)data, ic)) {
					siteStatus[ic] = true;
					if (ic+1<siteStatus.length)
						siteStatus[ic+1] = true;
				}

			for (int ic=0; ic<siteStatus.length; ic++) {
				selectSpanByProportion(ic, siteStatus, toSelect, proportionIncomp, spanSize);
			}
			for (int ic=0; ic<data.getNumChars(); ic++) {
				if (toSelect[ic])
					data.setSelected(ic, true);
			}

			data.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
		}
	}

	/*.................................................................................................................*/
	public String getName() {
		return "NCBLOCKS Selectior";
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "NCBLOCKS Selector...";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Neighbour-Conflicts Blocks: Selects blocks of characters in which many are incompatible with the site before or after (in a clique analysis sense)." ;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}

}

