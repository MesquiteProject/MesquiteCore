/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.PhyINSelector;
/*~~  */

import java.awt.Checkbox;
import mesquite.categ.lib.CategoricalData;
import mesquite.categ.lib.CategoricalState;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class PhyINSelector extends CharacterSelectorPersistent {


	MesquiteString xmlPrefs= new MesquiteString();
	String xmlPrefsString = null;
	double proportionIncompat = 0.4;
	int spanSize = 12;
	int neighbourDistance = 1;
	MesquiteBoolean treatGapAsState = new MesquiteBoolean(true);

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences(xmlPrefs);
		xmlPrefsString = xmlPrefs.getValue();
		if (!queryOptions(this, "PhyIN"))
			return false;
		//If persistent, add setOptions menu item
		return true;
	}
	/*.................................................................................................................*/
	/** Tells module it will be used in a persistent way, so add menu items etc.*/
	public void pleasePersist() {
		addMenuItem("PhyIN Selection Options...",  makeCommand("setOptions",  this));
	}


	/*.................................................................................................................*/
	/*.................................................................................................................*/
	// IN preparation for "live" selecting
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();

		temp.addLine("setProportionIncompat " + proportionIncompat);
		temp.addLine("setSpanSize " + spanSize);
		temp.addLine("setSpanSize " + spanSize);
		temp.addLine("setTreatGapAsState " + treatGapAsState.toOffOnString());
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Presents options dialog box.", "[on or off]", commandName, "setOptions")) {
			queryOptions(this, "PhyIN");
		}
		else if (checker.compare(this.getClass(), "Sets proportion of sites in span incompatible to trigger selection.", "[on or off]", commandName, "setProportionIncompat")) {
			double s = MesquiteDouble.fromString(parser.getFirstToken(arguments));
			if (MesquiteDouble.isCombinable(s)){
				proportionIncompat = s;
				if (!MesquiteThread.isScripting())
					parametersChanged(); 

			}
		}
		else if (checker.compare(this.getClass(), "Sets block size of PhyIN selection.", "[on or off]", commandName, "setSpanSize")) {
			int s = MesquiteInteger.fromString(parser.getFirstToken(arguments));
			if (MesquiteInteger.isCombinable(s)){
				spanSize = s;
				if (!MesquiteThread.isScripting())
					parametersChanged(); 
			}

		}
		else if (checker.compare(this.getClass(), "Sets whether or not to treat gaps as an extra state.", "[on or off]", commandName, "setTreatGapAsState")) {
			boolean current = treatGapAsState.getValue();
			treatGapAsState.toggleValue(parser.getFirstToken(arguments));
			if (current!=treatGapAsState.getValue()) {
				parametersChanged();
			}
		}

		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	/*.................................................................................................................*/
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "proportionIncompat", proportionIncompat);  
		StringUtil.appendXMLTag(buffer, 2, "spanSize", spanSize);  
		StringUtil.appendXMLTag(buffer, 2, "neighbourDistance", neighbourDistance);  
		StringUtil.appendXMLTag(buffer, 2, "treatGapAsState", treatGapAsState);  

		return buffer.toString();
	}
	public void processSingleXMLPreference (String tag, String flavor, String content){
		processSingleXMLPreference(tag, null, content);
	}

	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("proportionIncompat".equalsIgnoreCase(tag))
			proportionIncompat = MesquiteDouble.fromString(content);
		if ("spanSize".equalsIgnoreCase(tag))
			spanSize = MesquiteInteger.fromString(content);
		if ("neighbourDistance".equalsIgnoreCase(tag))
			neighbourDistance = MesquiteInteger.fromString(content);
		if ("treatGapAsState".equalsIgnoreCase(tag))
			treatGapAsState.setValue(MesquiteBoolean.fromTrueFalseString(content));

	}

	public boolean queryOptions(MesquiteModule mb, String action) {
		if (!mb.okToInteractWithUser(mb.CAN_PROCEED_ANYWAY, "Querying Options")) 
			return true;
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(mb.containerOfModule(),  "Select using PhyIN Algorithm",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()

		IntegerField SSField = dialog.addIntegerField("Length of blocks (-b)", spanSize, 4);
		IntegerField NDField = dialog.addIntegerField("Distance surveyed for conflict among neighbours (-d)", neighbourDistance, 4);
		DoubleField PIField = dialog.addDoubleField("Proportion of neighbouring sites in conflict to trigger block selection (-p)", proportionIncompat, 4);
		Checkbox tGAS = dialog.addCheckBox("Treat non-terminal gaps as extra state (-e)", treatGapAsState. getValue());


		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			proportionIncompat = PIField.getValue();
			spanSize = SSField.getValue();
			neighbourDistance = NDField.getValue();
			treatGapAsState.setValue(tGAS.getState());
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
	boolean[] hasConflict;
	boolean[] toSelect;
	int[] taxonSequenceStart, taxonSequenceEnd;
	int NUMSTATES = 4;


	boolean[][] statePairs;

	/*.................................................................................................................*/
	int getEffectiveState(long stateSet) {
		if (treatGapAsState.getValue() && CategoricalState.isInapplicable(stateSet))
			return NUMSTATES-1; 
		if (CategoricalState.isCombinable(stateSet) && !CategoricalState.hasMultipleStates(stateSet))
			return CategoricalState.getOnlyElement(stateSet);
		return -1;
	}
	int getFirstInSequence(CategoricalData data, int it) {
		for (int ic=0; ic<data.getNumChars(); ic++)
			if (!data.isInapplicable(ic, it))
				return ic;
		return -1;
	}
	int getLastInSequence(CategoricalData data, int it) {
		for (int ic=data.getNumChars()-1; ic>=0; ic--)
			if (!data.isInapplicable(ic, it))
				return ic;
		return -1;
	}

	boolean anyOtherConnectForward(int i, int k) {
		for (int m =0; m<NUMSTATES;m++) 
			if (k != m && statePairs[i][m]) {
				return true;
			}
		return false;
	}
	boolean anyOtherConnectBackward(int i, int k) {
		for (int m =0; m<NUMSTATES;m++) 
			if (i != m && statePairs[m][k]) {
				return true;
			}
		return false;
	}
	boolean anyOfGraphLeft() {
		for (int i =0; i<NUMSTATES; i++) 
			for (int k =0; k<NUMSTATES;k++) {
				if (statePairs[i][k]) {
					return true;
				}
			}
		return false;
	}
	/*.................................................................................................................*/
	boolean trimSingleConnects() {
		//turn a statePairs[i][k] to false if it is the only pairing/edge among statePairs[i]
		for (int i =0; i<NUMSTATES; i++) {
			for (int k =0; k<NUMSTATES;k++) {
				if (statePairs[i][k]) { //edge is in graph
					if (!anyOtherConnectForward(i, k)) {
						statePairs[i][k] = false;
						return true;
					}
					if (!anyOtherConnectBackward(i, k)) {
						statePairs[i][k] = false;
						return true;
					}
				}
			}
		}
		return false;
	}
	/*.................................................................................................................*/
	boolean areIncompatible(CategoricalData data, int ic, int ic2) {
		if (ic>=data.getNumChars() || ic <0 || ic2>=data.getNumChars() || ic2 <0)
			return false;

		for (int i =0; i<NUMSTATES; i++) for (int k =0; k<NUMSTATES;k++) statePairs[i][k]= false;

		//first, harvest all patterns between the two columns
		for (int it = 0; it < data.getNumTaxa(); it++) {
			// only look at taxa for which ic and ic2 are within their sequence (i.e. not in terminal gap region)
			if (taxonSequenceStart[it]>=0 && ic>= taxonSequenceStart[it] && ic <= taxonSequenceEnd[it] && ic2>= taxonSequenceStart[it] && ic2 <= taxonSequenceEnd[it]) {
				int state1 = getEffectiveState(data.getState(ic, it));
				int state2 = getEffectiveState(data.getState(ic2, it));
				if (state1 >=0 && state1<NUMSTATES && state2 >=0 && state2<NUMSTATES) {
					statePairs[state1][state2] = true;
				}
			}
		}

		/*Quick Filter: for all pairs of states, see if all 4 patterns are present (00, 01, 10, 11). This seems to save a tiny bit of time.
		for (int i =0; i<NUMSTATES; i++) 
			for (int k =i+1; k<NUMSTATES;k++) {
				if (statePairs[i][i] && statePairs[i][k] && statePairs[k][i] && statePairs[k][k]) {
					return true;
				}
			}
		 */

		//Test of compatibility: Look for cycles in state to state occupancy graph (M. Steel)
		int stopper = 1000; //merely to prevent infinite loop in case of bug
		while (trimSingleConnects() && (stopper--) > 0) {
			if (stopper == 1)
				MesquiteMessage.warnUser("ALERT SOMETHING WENT WRONG WITH THE PhyIN calculations (trimSingleConnects too many iterations)");
		}
		// if anything is left of the graph, then it's incompatible
		if (anyOfGraphLeft()) 
			return true;


		return false;
	}

	/*.................................................................................................................*/
	/** Marks those characters in conflict with others*/
	public void markThoseWithConflict(CategoricalData data){
		if (taxonSequenceStart == null || taxonSequenceStart.length != data.getNumTaxa()) {
			taxonSequenceStart = new int[data.getNumTaxa()];
			taxonSequenceEnd = new int[data.getNumTaxa()];
		}
		if (hasConflict == null || hasConflict.length != data.getNumChars()) {
			hasConflict = new boolean[data.getNumChars()];
			toSelect = new boolean[data.getNumChars()];
		}
		for (int ic=0; ic<hasConflict.length; ic++) {
			hasConflict[ic] = false;
			toSelect[ic] = false;
		}

		for (int it=0; it<data.getNumTaxa(); it++) {
			taxonSequenceStart[it] = getFirstInSequence(data, it);
			taxonSequenceEnd[it] = getLastInSequence(data, it);
		}

		NUMSTATES = data.getMaxState()+1; 
		if (treatGapAsState.getValue())
			NUMSTATES++;//one extra for gaps as state
		if (statePairs == null || statePairs.length != NUMSTATES)
			statePairs = new boolean[NUMSTATES][NUMSTATES];

		for (int ic=0; ic<data.getNumChars(); ic++) {
			for (int d = 1; d<neighbourDistance+1; d++)
				if (areIncompatible(data, ic, ic+d)) {
					hasConflict[ic] = true;
					if (ic+d<hasConflict.length)
						hasConflict[ic+d] = true;
				}
		}
	}
	/*.................................................................................................................*/
	void selectSpanByProportion(int ic, boolean[] hasConflict, boolean[] toSelect, double proportionIncomp, int spanSize) {
		if (!hasConflict[ic])
			return;
		int count = 0;
		int lastHit = -1;
		for (int k = 0; k<spanSize && ic+k<hasConflict.length; k++) {
			if (hasConflict[ic+k]) {
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
			MesquiteTimer timer = new MesquiteTimer();
			timer.start();
			markThoseWithConflict((CategoricalData)data);
			timer.end();
			Debugg.println(" time to assess " + timer.getAccumulatedTime());
			timer.reset();
			timer.start();
			for (int ic=0; ic<hasConflict.length; ic++) {
				selectSpanByProportion(ic, hasConflict, toSelect, proportionIncompat, spanSize);
			}
			timer.end();
			Debugg.println(" time to selectSpan " + timer.getAccumulatedTime());
			timer.reset();
			timer.start();
			for (int ic=0; ic<data.getNumChars(); ic++) {
				if (toSelect[ic])
					data.setSelected(ic, true);
			}
			timer.end();
			Debugg.println(" time to select " + timer.getAccumulatedTime());
			timer.reset();
			timer.start();

			data.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
			timer.end();
			Debugg.println(" time to notify " + timer.getAccumulatedTime());
		}
	}

	/*.................................................................................................................*/
	public String getName() {
		return "PhyIN Selectior";
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "PhyIN Selector...";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Phylogenetic Incompatibility Among Neighbours (PhyIN): Selects blocks of characters in which many are incompatible with the site before or after (in a clique analysis sense)." ;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}

}

