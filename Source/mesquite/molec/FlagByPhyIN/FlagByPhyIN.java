/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.FlagByPhyIN;
/*~~  */



import java.awt.Button;
import java.awt.Checkbox;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import mesquite.categ.lib.CategoricalData;
import mesquite.categ.lib.CategoricalState;
import mesquite.categ.lib.RequiresAnyDNAData;
import mesquite.categ.lib.RequiresAnyMolecularData;
import mesquite.lib.Bits;
import mesquite.lib.CommandChecker;
import mesquite.lib.CompatibilityTest;
import mesquite.lib.Debugg;
import mesquite.lib.DoubleField;
import mesquite.lib.ExtensibleDialog;
import mesquite.lib.IntegerField;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteDouble;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteListener;
import mesquite.lib.MesquiteMessage;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteThread;
import mesquite.lib.Notification;
import mesquite.lib.Snapshot;
import mesquite.lib.StringUtil;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.MatrixFlags;
import mesquite.lib.duties.MatrixFlagger;
import mesquite.lib.duties.MatrixFlaggerForTrimming;
import mesquite.lib.duties.MatrixFlaggerForTrimmingSites;

/* ======================================================================== */
public class FlagByPhyIN extends MatrixFlaggerForTrimmingSites implements ActionListener {

	//Primary PhyIN parameters =================================
	static double proportionIncompatDEFAULT = 0.5; //(-p)
	static int blockSizeDEFAULT = 10; //(-b)
	static int neighbourDistanceDEFAULT = 2; //(-d)
	static boolean treatGapAsStateDEFAULT = true; //(-e)
	static boolean examineOnlySelectedTaxaDEFAULT = false;

	double proportionIncompat = proportionIncompatDEFAULT; //(-p)
	int blockSize = blockSizeDEFAULT; //(-b)
	int neighbourDistance = neighbourDistanceDEFAULT; //(-d)
	MesquiteBoolean treatGapAsState = new MesquiteBoolean(treatGapAsStateDEFAULT); //(-e)
	MesquiteBoolean examineOnlySelectedTaxa = new MesquiteBoolean(examineOnlySelectedTaxaDEFAULT); //(no equivalent in python script)

	// Example with parameters b=10, d=1, p = 0.5
	// AAATAAAAAACCAAAAAAAAAAA
	// AAATTAAAACCAAAAAAAAAAAA
	// AAAATAAAAAACAAAAAAAAAAA
	// AAAAAAAAACAAAAAAAAAAAAA
	//    ><
	//          ><
	//           ><
	//    ©©    ©©©
	//    ssssssssss
	//    *********
	// in the above, there are three pairs of conflicting neighbours (><). 
	// These pass the simple binary version of the incompatibility criterion,
	// in that all for combinations of states are present.
	// The default is to look as well at two-away neighbours (d=2), but in this example
	// there are no two-away neighbours that conflict.
	//
	// Thus, there are 5 conflicted characters (©).
	// Thus, within the stretch of 10 shown by s, there are 5/10 conflicted characters, thus > 0.5 proportion.
	// Thus, the stretch within that from first to last of the conflicted characters is selected (*).




	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		if (!MesquiteThread.isScripting()) {
			if (!queryOptions())
				return false;
		}
		addMenuItem("PhyIN Options...",  makeCommand("setOptions",  this));
		return true;
	}
	public CompatibilityTest getCompatibilityTest(){
		return new RequiresAnyDNAData();
	}
	/*.................................................................................................................*/
	public void queryLocalOptions () {
		if (queryOptions())
			storePreferences();
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();

		temp.addLine("setProportionIncompat " + proportionIncompat);
		temp.addLine("setSpanSize " + blockSize);
		temp.addLine("setNeighbourDistance " + neighbourDistance);
		temp.addLine("setTreatGapAsState " + treatGapAsState.toOffOnString());
		temp.addLine("setExamineOnlySelectedTaxa " + examineOnlySelectedTaxa.toOffOnString());
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Presents options dialog box.", "[on or off]", commandName, "setOptions")) {
			boolean q = queryOptions();
			if (q)
				parametersChanged();

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
				blockSize = s;
				if (!MesquiteThread.isScripting())
					parametersChanged(); 
			}

		}
		else if (checker.compare(this.getClass(), "Sets neighbour distance of PhyIN selection.", "[on or off]", commandName, "setNeighbourDistance")) {
			int s = MesquiteInteger.fromString(parser.getFirstToken(arguments));
			if (MesquiteInteger.isCombinable(s)){
				neighbourDistance = s;
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
		else if (checker.compare(this.getClass(), "Sets whether or not to examine only the selected taxa for conflict.", "[on or off]", commandName, "setExamineOnlySelectedTaxa")) {
			boolean current = examineOnlySelectedTaxa.getValue();
			examineOnlySelectedTaxa.toggleValue(parser.getFirstToken(arguments));
			if (current!=examineOnlySelectedTaxa.getValue()) {
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
		StringUtil.appendXMLTag(buffer, 2, "spanSize", blockSize);  
		StringUtil.appendXMLTag(buffer, 2, "neighbourDistance", neighbourDistance);  
		StringUtil.appendXMLTag(buffer, 2, "treatGapAsState", treatGapAsState);  
		StringUtil.appendXMLTag(buffer, 2, "examineOnlySelectedTaxa", examineOnlySelectedTaxa);  
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
			blockSize = MesquiteInteger.fromString(content);
		if ("neighbourDistance".equalsIgnoreCase(tag))
			neighbourDistance = MesquiteInteger.fromString(content);
		if ("treatGapAsState".equalsIgnoreCase(tag))
			treatGapAsState.setValue(MesquiteBoolean.fromTrueFalseString(content));
		if ("examineOnlySelectedTaxa".equalsIgnoreCase(tag))
			examineOnlySelectedTaxa.setValue(MesquiteBoolean.fromTrueFalseString(content));


	}

	IntegerField SSField;
	IntegerField NDField;
	DoubleField PIField;
	Checkbox tGAS, eOST;
	public boolean queryOptions() {
		if (!okToInteractWithUser(CAN_PROCEED_ANYWAY, "Querying Options")) 
			return true;
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(),  "Criteria for PhyIN",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()

		dialog.addLabel("PhyIN criteria for incompatible sites:");
		SSField = dialog.addIntegerField("Length of blocks (-b)", blockSize, 4);
		NDField = dialog.addIntegerField("Distance surveyed for conflict among neighbours (-d)", neighbourDistance, 4);
		PIField = dialog.addDoubleField("Proportion of neighbouring sites in conflict to trigger block selection (-p)", proportionIncompat, 4);
		tGAS = dialog.addCheckBox("Treat non-terminal gaps as extra state (-e)", treatGapAsState.getValue());
		eOST = dialog.addCheckBox("Examine conflict only among any selected taxa.", examineOnlySelectedTaxa.getValue());

		dialog.addHorizontalLine(1);
		dialog.addBlankLine();
		Button useDefaultsButton = null;
		useDefaultsButton = dialog.addAListenedButton("Set to Defaults", null, this);
		useDefaultsButton.setActionCommand("setToDefaults");


		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			proportionIncompat = PIField.getValue();
			blockSize = SSField.getValue();
			neighbourDistance = NDField.getValue();
			treatGapAsState.setValue(tGAS.getState());
			examineOnlySelectedTaxa.setValue(eOST.getState());

			storePreferences();
		}
		dialog.dispose();
		return (buttonPressed.getValue()==0);
	}
	/*.................................................................................................................*/
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equalsIgnoreCase("setToDefaults")) {
			PIField.setValue(proportionIncompatDEFAULT);
			SSField.setValue(blockSizeDEFAULT);
			NDField.setValue(neighbourDistanceDEFAULT);
			tGAS.setState(treatGapAsStateDEFAULT);
			eOST.setState(examineOnlySelectedTaxaDEFAULT);

		} 
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
	int numTaxaWithSequence = 0;

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
	boolean anyTaxaSelected = false;
	boolean includeTaxon(int it, CategoricalData data) {
		return !anyTaxaSelected || (!examineOnlySelectedTaxa.getValue() || data.getTaxa().getSelected(it));
	}
	/*.................................................................................................................*/
	boolean areIncompatible(CategoricalData data, int ic, int ic2) {
		if (ic>=data.getNumChars() || ic <0 || ic2>=data.getNumChars() || ic2 <0)
			return false;

		for (int i =0; i<NUMSTATES; i++) for (int k =0; k<NUMSTATES;k++) statePairs[i][k]= false;
	
		//first, harvest all patterns between the two columns
		for (int it = 0; it < data.getNumTaxa(); it++) {
			// only look at taxa for which ic and ic2 are within their sequence (i.e. not in terminal gap region)
			if (includeTaxon(it, data) && taxonSequenceStart[it]>=0 && ic>= taxonSequenceStart[it] && ic <= taxonSequenceEnd[it] && ic2>= taxonSequenceStart[it] && ic2 <= taxonSequenceEnd[it]) {
				int state1 = getEffectiveState(data.getState(ic, it));
				int state2 = getEffectiveState(data.getState(ic2, it));
				if (state1 >=0 && state1<NUMSTATES && state2 >=0 && state2<NUMSTATES) {
					statePairs[state1][state2] = true;
				}
			}
		}


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
		anyTaxaSelected = data.getTaxa().anySelected();
		if (hasConflict == null || hasConflict.length != data.getNumChars()) {
			hasConflict = new boolean[data.getNumChars()];
			toSelect = new boolean[data.getNumChars()];
		}
		for (int ic=0; ic<hasConflict.length; ic++) {
			hasConflict[ic] = false;
			toSelect[ic] = false;
		}
		numTaxaWithSequence = 0;
		for (int it=0; it<data.getNumTaxa(); it++) {
			if (includeTaxon(it, data)){
				taxonSequenceStart[it] = getFirstInSequence(data, it);
				taxonSequenceEnd[it] = getLastInSequence(data, it);
				if (taxonSequenceStart[it]>=0)
					numTaxaWithSequence++;
			}
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


	/*.................................................................................................................*/
	/*.................................................................................................................*/
	public MatrixFlags flagMatrix(CharacterData data, MatrixFlags flags) {
		if (data!=null && data.getNumChars()>0 && data instanceof CategoricalData){
			if (flags == null)
				flags = new MatrixFlags(data);
			else {
				flags.reset(data);
			}
			markThoseWithConflict((CategoricalData)data);
			for (int ic=0; ic<hasConflict.length; ic++) {
				selectSpanByProportion(ic, hasConflict, toSelect, proportionIncompat, blockSize);
			}


			for (int ic=0; ic<data.getNumChars(); ic++) {
				if (toSelect[ic])
					flags.setCharacterFlag(ic, true);
			}
		}
		return flags;
	}
	/*.................................................................................................................*/
	/*.................................................................................................................*/
	public String getName() {
		return "PhyIN";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Flags sites according to PhyIN criteria." ;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}
}


