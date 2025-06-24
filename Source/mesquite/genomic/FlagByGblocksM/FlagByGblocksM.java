/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.genomic.FlagByGblocksM;
/*~~  */



import java.awt.Button;
import java.awt.Checkbox;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import mesquite.categ.lib.CategoricalData;
import mesquite.categ.lib.CategoricalState;
import mesquite.categ.lib.DNAData;
import mesquite.categ.lib.DNAState;
import mesquite.categ.lib.ProteinData;
import mesquite.categ.lib.ProteinState;
import mesquite.categ.lib.RequiresAnyMolecularData;
import mesquite.lib.Bits;
import mesquite.lib.CommandChecker;
import mesquite.lib.CompatibilityTest;
import mesquite.lib.IntegerField;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteDouble;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteThread;
import mesquite.lib.Snapshot;
import mesquite.lib.StringUtil;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.MatrixFlags;
import mesquite.lib.duties.MatrixFlaggerForTrimmingSites;
import mesquite.lib.ui.DoubleField;
import mesquite.lib.ui.ExtensibleDialog;
import mesquite.lib.ui.RadioButtons;

/* ======================================================================== */
public class FlagByGblocksM extends MatrixFlaggerForTrimmingSites implements ActionListener {

	static final double defaultISMesquite = 0.20;   
	static final double defaultFSMesquite = 0.4;  
	static final int defaultCPMesquite=4;  
	static final int defaultBLMesquite=4;  
	static final double defaultGapThresholdMesquite = 0.5;
	static final boolean defaultCountWithinApplicableMesquite = true;

	static final double defaultIS = 0.50;   
	static final double defaultFS = 0.85;  
	static final int defaultCP=8;  
	static final int defaultBL=10;  
	static final double defaultGapThreshold = 0.0;
	static final boolean defaultCountWithinApplicable = false;
	static final boolean defaultIgnoreTaxaWithoutSequence = true;

	double IS = defaultISMesquite;   // proportion of identical residues that is upper boundary for non-conserved sequences
	double FS = defaultFSMesquite;  // proportion of identical residues that is upper boundary for conserved sequences
	int CP=defaultCPMesquite;  //block size limit for non-conserved blocks
	int BL=defaultBLMesquite;  //  small region block size limit 
	double gapThreshold = defaultGapThresholdMesquite;   // the proportion of gaps allowed at a site

	boolean removeAllGaps = true;
	MesquiteBoolean countWithinApplicable  = new MesquiteBoolean(defaultCountWithinApplicableMesquite);   // count proportion of identical residues only within those taxa without gaps at a site
	boolean ignoreTaxaWithoutSequence = defaultIgnoreTaxaWithoutSequence;
	boolean[] taxonHasSequence=null;

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (!mesquite.molec.GBLOCKSSelector.GBLOCKSSelector.prefsRead) 
			transferPrefsFromOldGBLOCKSSelector();
		else
			loadPreferences();
		if (!MesquiteThread.isScripting()) {
			if (!queryOptions())
				return false;
		}
		addMenuItem("Gblocks Selection Options...",  makeCommand("setOptions",  this));
		return true;
	}
	
	public CompatibilityTest getCompatibilityTest(){
		return new RequiresAnyMolecularData();
	}
	/*.................................................................................................................*/
	//A service for those with older installations. Grabs prefs from old version, which has been converted into a MesquiteInit merely to capture the preferences.
	public void transferPrefsFromOldGBLOCKSSelector() {
		if (mesquite.molec.GBLOCKSSelector.GBLOCKSSelector.prefsTransferred)
			return;
		logln("Preferences from old Mesquite-GBlocks transferred.");
		if (MesquiteDouble.isCombinable(mesquite.molec.GBLOCKSSelector.GBLOCKSSelector.IS))
			IS = mesquite.molec.GBLOCKSSelector.GBLOCKSSelector.IS;
		if (MesquiteDouble.isCombinable(mesquite.molec.GBLOCKSSelector.GBLOCKSSelector.FS))
			FS = mesquite.molec.GBLOCKSSelector.GBLOCKSSelector.FS;
		if (MesquiteDouble.isCombinable(mesquite.molec.GBLOCKSSelector.GBLOCKSSelector.gapThreshold))
			gapThreshold = mesquite.molec.GBLOCKSSelector.GBLOCKSSelector.gapThreshold;
		if (MesquiteInteger.isCombinable(mesquite.molec.GBLOCKSSelector.GBLOCKSSelector.CP))
			CP = mesquite.molec.GBLOCKSSelector.GBLOCKSSelector.CP;
		if (MesquiteInteger.isCombinable(mesquite.molec.GBLOCKSSelector.GBLOCKSSelector.BL))
			BL = mesquite.molec.GBLOCKSSelector.GBLOCKSSelector.BL;
		if (mesquite.molec.GBLOCKSSelector.GBLOCKSSelector.countWithinApplicableRead)
			countWithinApplicable.setValue(mesquite.molec.GBLOCKSSelector.GBLOCKSSelector.countWithinApplicable);
		mesquite.molec.GBLOCKSSelector.GBLOCKSSelector.instanceOfMe.prefsCaptured();
	}
	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String flavor, String content){
		processSingleXMLPreference(tag, null, content);
	}

	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("IS".equalsIgnoreCase(tag))
			IS = MesquiteDouble.fromString(content);
		if ("FS".equalsIgnoreCase(tag))
			FS = MesquiteDouble.fromString(content);
		if ("CP".equalsIgnoreCase(tag))
			CP = MesquiteInteger.fromString(content);
		if ("BL".equalsIgnoreCase(tag))
			BL = MesquiteInteger.fromString(content);
		if ("gapThreshold".equalsIgnoreCase(tag))
			gapThreshold = MesquiteDouble.fromString(content);
		if ("countWithinApplicable".equalsIgnoreCase(tag))
			countWithinApplicable.setFromTrueFalseString(content);
	}

	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "IS", IS);  
		StringUtil.appendXMLTag(buffer, 2, "FS", FS);  
		StringUtil.appendXMLTag(buffer, 2, "CP", CP);  
		StringUtil.appendXMLTag(buffer, 2, "BL", BL);  
		StringUtil.appendXMLTag(buffer, 2, "gapThreshold", gapThreshold);  
		StringUtil.appendXMLTag(buffer, 2, "countWithinApplicable", countWithinApplicable);  

		return buffer.toString();
	}
	/*.................................................................................................................*/
	public void queryLocalOptions () {
	 		if (queryOptions())
	 			storePreferences();
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();

		temp.addLine("setIS " + IS);
		temp.addLine("setFS " + FS);
		temp.addLine("setCP " + CP);
		temp.addLine("setBL " + BL);
		temp.addLine("setGapThreshold " + gapThreshold);
		temp.addLine("setCountWithinApplicable " + countWithinApplicable.toOffOnString());
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Presents options dialog box.", "", commandName, "setOptions")) {
			boolean q = queryOptions();
			if (q)
				parametersChanged();

		}
		else if (checker.compare(this.getClass(), "Sets proportion of fraction of identical residues that is upper boundary for non-conserved sequences.", "[on or off]", commandName, "setIS")) {
			double s = MesquiteDouble.fromString(parser.getFirstToken(arguments));
			if (MesquiteDouble.isCombinable(s)){
				IS = s;
				if (!MesquiteThread.isScripting())
					parametersChanged(); 

			}
		}
		else if (checker.compare(this.getClass(), "Sets proportion of identical residues that is upper boundary for conserved sequences.", "[on or off]", commandName, "setFS")) {
			double s = MesquiteDouble.fromString(parser.getFirstToken(arguments));
			if (MesquiteDouble.isCombinable(s)){
				FS = s;
				if (!MesquiteThread.isScripting())
					parametersChanged(); 

			}
		}
		else if (checker.compare(this.getClass(), "Sets block size limit for non-conserved blocks.", "[integer]", commandName, "setCP")) {
			int s = MesquiteInteger.fromString(parser.getFirstToken(arguments));
			if (MesquiteInteger.isCombinable(s)){
				CP = s;
				if (!MesquiteThread.isScripting())
					parametersChanged(); 
			}

		}
		else if (checker.compare(this.getClass(), "Sets small region block size limit.", "[integer]", commandName, "setBL")) {
			int s = MesquiteInteger.fromString(parser.getFirstToken(arguments));
			if (MesquiteInteger.isCombinable(s)){
				BL = s;
				if (!MesquiteThread.isScripting())
					parametersChanged(); 
			}

		}
		else if (checker.compare(this.getClass(), "Sets the proportion of gaps allowed at a site.", "[on or off]", commandName, "setGapThreshold")) {
			double s = MesquiteDouble.fromString(parser.getFirstToken(arguments));
			if (MesquiteDouble.isCombinable(s)){
				gapThreshold = s;
				if (!MesquiteThread.isScripting())
					parametersChanged(); 

			}
		}

		else if (checker.compare(this.getClass(), "Sets whether or not to count proportion of identical residues only within those taxa without gaps at a site.", "[on or off]", commandName, "setCountWithinApplicable")) {
			boolean current = countWithinApplicable.getValue();
			countWithinApplicable.toggleValue(parser.getFirstToken(arguments));
			if (current!=countWithinApplicable.getValue()) {
				parametersChanged();
			}
		}


		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
		/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/


	Button useMesquiteDefaultsButton = null;
	Button useDefaultsButton = null;
	DoubleField ISfield = null;
	DoubleField FSfield=null;
	Checkbox countWithinApplicableCheckbox=null;
	IntegerField CPfield =null;
	IntegerField BLfield=null;
	DoubleField gapThresholdField=null;
	RadioButtons chooseBadSitesRadioButtons=null;
	DoubleField termGapsPropForgivenField=null;
	/*.................................................................................................................*/
	/*.................................................................................................................*/
	/**This queryOptions is provided in case the module that uses this GBLOCKSCalculator doesn't want to add extra options, and just wants to use
	 * a simple dialog box to query for options. If the ownermodule wishes, it can make its own dialog box; use this one as a template. */
	public boolean queryOptions() {
		if (!okToInteractWithUser(CAN_PROCEED_ANYWAY, "Querying Options")) 
			return true;

		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(),  "Criteria for Mesquite's GblocksM Algorithm",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
		String helpString = "This feature will choose characters using a modified version of the Gblocks algorithm (Castresana 2000). "
				+ "Mesquite's modifications are to allow the user to specify the proportion of gaps permitted, and "
				+ "to choose whether nucleotide frequencies are counted only among non-gaps (i.e., judgement of conservativeness ignores gaps)."
				+ "Mesquite does not count gaps in taxa with no data whatsoever; those taxa are treated as absent."
				+ "<p>If you use this in a publication, please cite it as GblocksM, a version of Gblocks (Castresana 2000) as implemented and modified in Mesquite."
				+ "<p><b>Reference for the original Gblocks</b>: Castresana J. 2000. Selection of conserved blocks from multiple alignments for their use in phylogenetic analysis. Molecular Biology and Evolution 17: 540–552"
				+ " <a href = \"http://doi.org/10.1093/oxfordjournals.molbev.a026334\">doi:10.1093/oxfordjournals.molbev.a026334</a>";

		dialog.appendToHelpString(helpString);

		dialog.addLabel("Extended Gblocks Algorithm");

		addToQueryOptions(dialog, "GblocksM");

		dialog.addNewDialogPanel();
		useMesquiteDefaultsButton = dialog.addAListenedButton("Set to GBlocksM Defaults", null, this);
		useMesquiteDefaultsButton.setActionCommand("setToMesquiteDefaults");
		useDefaultsButton = dialog.addAListenedButton("Set to GBlocks Defaults", null, this);
		useDefaultsButton.setActionCommand("setToDefaults");

		dialog.addLargeOrSmallTextLabel("If you use this in a publication, cite as described under the (?) help button.");
		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			processQueryOptions(dialog);
			storePreferences();
		}
		dialog.dispose();
		return (buttonPressed.getValue()==0);
	}
	/*.................................................................................................................*/
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equalsIgnoreCase("setToMesquiteDefaults")) {
			setToMesquiteDefaults();
		} else if (e.getActionCommand().equalsIgnoreCase("setToDefaults")) {
			setToDefaults();
		} 
	}

	/*.................................................................................................................*/
	public void addToQueryOptions(ExtensibleDialog dialog, String action) {
		ISfield = dialog.addDoubleField("Minimum proportion of identical residues for conserved positions (b1)", IS, 4);
		FSfield = dialog.addDoubleField("Minimum proportion of identical residues for highly-conserved positions (b2)", FS, 4);
		CPfield = dialog.addIntegerField("Maximum length of non-conserved blocks (b3)", CP, 4);
		BLfield = dialog.addIntegerField("Minimum length of a block (b4)", BL, 4);
		dialog.addHorizontalLine(1);
		dialog.addLabel("Mesquite-specific extensions:");
		countWithinApplicableCheckbox = dialog.addCheckBox("Count proportion only within taxa with non-gaps at that position", countWithinApplicable.getValue());

		gapThresholdField = dialog.addDoubleField("Proportion of gaps allowed in a character", gapThreshold, 4);
		dialog.addHorizontalLine(1);

		int c = 1;
		dialog.addHorizontalLine(1);
	}
	/*.................................................................................................................*/
	public void processQueryOptions(ExtensibleDialog dialog) {
		IS = ISfield.getValue();
		FS = FSfield.getValue();
		CP = CPfield.getValue();
		BL = BLfield.getValue();
		gapThreshold = gapThresholdField.getValue();
		countWithinApplicable.setValue(countWithinApplicableCheckbox.getState());

	}
	/*.................................................................................................................*/
	public void setToMesquiteDefaults() {
		ISfield.setValue(defaultISMesquite);	
		FSfield.setValue(defaultFSMesquite);
		CPfield.setValue(defaultCPMesquite);
		BLfield.setValue(defaultBLMesquite);
		gapThresholdField.setValue(defaultGapThresholdMesquite);
		countWithinApplicableCheckbox.setState(defaultCountWithinApplicableMesquite);
	}
	/*.................................................................................................................*/
	public void setToDefaults() {
		ISfield.setValue(defaultIS);	
		FSfield.setValue(defaultFS);
		CPfield.setValue(defaultCP);
		BLfield.setValue(defaultBL);
		gapThresholdField.setValue(defaultGapThreshold);
		countWithinApplicableCheckbox.setState(defaultCountWithinApplicable);
	}
	
	/*.................................................................................................................*/
	int maxNumberIdenticalResidues (CategoricalData data, int ic) {
		int[] numTaxaWithResidue = null;
		if (data instanceof DNAData)
			numTaxaWithResidue= new int[DNAState.maxDNAState+1];
		else if (data instanceof ProteinData)
			numTaxaWithResidue= new int[ProteinState.maxProteinState+1];

		CategoricalState cs = null;
		for (int it = 0; it<data.getNumTaxa(); it++) {   // calculate proportions
			cs = (CategoricalState)data.getCharacterState(cs, ic, it);
			if (!(cs.isUnassigned() || cs.isInapplicable() || !cs.isCombinable() || cs.cardinality()>1)) {  // single element
				int state = cs.getOnlyElement(cs.getValue());
				if (state>=0 && state<numTaxaWithResidue.length)
					numTaxaWithResidue[state]++;
			}
		}

		int maxCount=0;
		for (int is=0; is<numTaxaWithResidue.length; is++){
			if (numTaxaWithResidue[is]>maxCount)
				maxCount=numTaxaWithResidue[is];
		}

		return maxCount;
	}
	/*.................................................................................................................*/
	int numNonGaps (CategoricalData data, int ic) {
		int count=0;
		for (int it = 0; it<data.getNumTaxa(); it++) {   // calculate proportions
			if (!data.isInapplicable(ic, it)) { // doesn't need to consider terminal gaps etc. since if the base is there, it's there!
				count++;
			}
		}
		return count;
	}

	/*.................................................................................................................*/
	int[] firstBase, lastBase, numSequencesAtSite;
	int numTaxaWithSequence;
	/*.................................................................................................................*/
	int getNumTaxaCountableAtSite(int ic) {
		if (ic<0 || ic>numSequencesAtSite.length)
			return 0;
		return numSequencesAtSite[ic];
	}
	/*.................................................................................................................*/
	int getNumTaxaInTerminalGapsAtSite(int ic) {
		if (ic<0 || ic>numSequencesAtSite.length)
			return 0;
		return numSequencesAtSite[ic];
	}
	boolean isInTermGaps(int ic, int it) {
		if (it<0 || it>firstBase.length)
			return false;
		if (ic<firstBase[it] || ic>lastBase[it])
			return true;
		return false;
	}

	//A gap is forgiven if the taxon has sequence
	boolean isGapForgiven(int ic, int it) {
		if (!taxonHasSequence[it])
			return true;

		return false;
	}
	/*.................................................................................................................*/
	boolean tooManyGaps (CategoricalData data, int ic) {
		if (taxonHasSequence==null)
			return false;
		

		if (removeAllGaps) {  // if removeAllGaps is true, having a single gap is too many
			for (int it = 0; it<data.getNumTaxa(); it++) {
				if (data.isInapplicable(ic, it) && !isGapForgiven(ic, it)) //any unpermitted gap is too many //$$$ previous version didn't permit taxa to be sequenceless
					return true;
			}
		} else {
			int count = 0;
			for (int it = 0; it<data.getNumTaxa(); it++) {
				if ((data.isInapplicable(ic, it) && !isGapForgiven(ic, it)) && taxonHasSequence[it])
					count++;
			}
			if (count >(int)(gapThreshold*getNumTaxaCountableAtSite(ic)))  //more than permitted
				return true;
		}
		return false;
	}

	/*.................................................................................................................*/
	static final int STATUSUNSET=0;
	static final int NONCONSERVED = 1;
	static final int WITHGAP = 4;
	static final int CONSERVED = 2;
	static final int HIGHLYCONSERVED = 3;

	/*.................................................................................................................*/
	void setCharacterStatus(CategoricalData data, int[] status, int[] firstBase, int[] lastBase){
		if (data==null || status == null )
			return;
		for (int ic=0; ic<data.getNumChars() && ic<status.length; ic++) {
			if (tooManyGaps(data,ic))
				status[ic] = NONCONSERVED; 
			else {
				int maxIdentical = maxNumberIdenticalResidues(data, ic);
				if (!countWithinApplicable.getValue()) {
					if (maxIdentical<(int)(IS*getNumTaxaCountableAtSite(ic))+1)
						status[ic] = NONCONSERVED; 
					else if (maxIdentical<(int)(FS*getNumTaxaCountableAtSite(ic)))
						status[ic] = CONSERVED; 
					else 
						status[ic] = HIGHLYCONSERVED; 
				} else {
					int totalResidues = numNonGaps(data,ic);
					if (1.0*maxIdentical/totalResidues<IS)
						status[ic] = NONCONSERVED; 
					else if (1.0*maxIdentical/totalResidues<FS)
						status[ic] = CONSERVED; 
					else 
						status[ic] = HIGHLYCONSERVED; 
				}
			}
		}
	}

	/*.................................................................................................................*/
	void setToSelectRange(Bits setToSelect, int icStart, int icEnd){
		if (setToSelect==null)
			return;
		for (int ic=icStart; ic<=icEnd && ic<setToSelect.getSize(); ic++)
			setToSelect.setBit(ic, true);
	}
	/*.................................................................................................................*/
	void examineRemainingBlock(int[] status, Bits setToSelect, int icStart, int icEnd){
		for (int ic=icStart; ic<=icEnd && ic<setToSelect.getSize(); ic++)
			if (status[ic]!=HIGHLYCONSERVED)  // if it is not highly conserved, then need to select it
				setToSelect.setBit(ic, true);
			else  // the moment we have a highly conserved one, get out of loop
				break;
		for (int ic=icEnd; ic>=icStart; ic--)
			if (status[ic]!=HIGHLYCONSERVED)  // if it is not highly conserved, then need to select it
				setToSelect.setBit(ic, true);
			else  // the moment we have a highly conserved one, get out of loop
				break;
	}
	/*.................................................................................................................*/
	void examineRegionAroundGap(int[] status, Bits setToSelect, int icGap){
		setToSelect.setBit(icGap, true);
		for (int ic=icGap+1; ic<setToSelect.getSize(); ic++){  // look up
			if (setToSelect.isBitOn(ic))  // the moment we find one we have already excluded, break
				break;
			if (status[ic] == NONCONSERVED)  // if it is not highly conserved, then need to select it
				setToSelect.setBit(ic, true);
		}
		for (int ic=icGap-1; ic>=0; ic--) {
			if (setToSelect.isBitOn(ic))  // the moment we find one we have already excluded, break
				break;
			if (status[ic] == NONCONSERVED)  // if it is not highly conserved, then need to select it
				setToSelect.setBit(ic, true);
		}
	}

	/*.................................................................................................................*/
	/** Called to mark characters*/
	public MatrixFlags flagMatrix(CharacterData data, MatrixFlags flags) {
		if (data!=null && data.getNumChars()>0 && data instanceof CategoricalData){
			if (flags == null)
				flags = new MatrixFlags(data);
			else 
				flags.reset(data);


			Bits charFlags = flags.getCharacterFlags();

			//preparing array memory
			if (taxonHasSequence == null || taxonHasSequence.length != data.getNumTaxa()) {
				taxonHasSequence = new boolean[data.getNumTaxa()];
				firstBase = new int[data.getNumTaxa()]; // for taxon it, first base in sequence
				lastBase = new int[data.getNumTaxa()]; // for taxon ic, last base in sequence
			}			
			if (numSequencesAtSite == null || numSequencesAtSite.length != data.getNumChars())
				numSequencesAtSite = new int[data.getNumChars()];  // at char ic, the number of taxa that are "in sequence", i.e. not in terminal gap area


			// figure out which taxa have any sequence at all
			numTaxaWithSequence = 0;
			for (int it = 0; it<data.getNumTaxa() && it<taxonHasSequence.length; it++) {
				taxonHasSequence[it]=data.anyApplicableAfter(0, it);
				if (taxonHasSequence[it])
					numTaxaWithSequence++;
			}


			removeAllGaps=(gapThreshold==0.0);

			//figuring out first and last bases so that edges of applicability of sequences can be considered
			for (int it = 0; it< data.getNumTaxa(); it++) {
				int first = -1;
				for (int ic=0; ic<data.getNumChars(); ic++) {
					if (!data.isInapplicable(ic, it)) {
						first=ic;
						break;
					}
				}
				firstBase[it] = first;

				int last = -1;
				for (int ic=data.getNumChars()-1; ic>=0; ic--) {
					if (!data.isInapplicable(ic, it)) {
						last=ic;
						break;
					}
				}
				lastBase[it] = last;
			}
			//figuring out number of taxa that are "in sequence" for each site
			for (int ic=0; ic<data.getNumChars(); ic++) {
				
					numSequencesAtSite[ic]=numTaxaWithSequence;
				
			}


			//boolean[] charToMark = new boolean[data.getNumChars()];
			int[] status = new int[data.getNumChars()];
			for (int ic=0; ic<status.length; ic++) {
				status[ic]=STATUSUNSET;
			}

			setCharacterStatus((CategoricalData)data, status, firstBase, lastBase);
			// ======  first look for "stretches of contiguous nonconserved positions"
			int blockStart=-1;
			for (int ic=0; ic<data.getNumChars(); ic++) {
				if (status[ic]==NONCONSERVED){
					if (blockStart<0){  // start of block, so set the value
						blockStart = ic;
					}
					if (ic==data.getNumChars()-1) {  // end of matrix, so need to check if block is big enough
						if (ic-blockStart+1 > CP)  // block is big enough to be selected
							setToSelectRange(charFlags,blockStart,ic);
					}
				} else {  // let's check to see if we have reached the end of a non-conserved block
					if (blockStart>=0){ // we were within a conserved block, now just one past it
						if (ic-blockStart > CP)  // block is big enough to be selected
							setToSelectRange(charFlags,blockStart,ic-1);
					}	
					blockStart=-1;  // reset to make it clear we are no longer in a non-conserved block
				}
			}

			// ======  examining flanks

			blockStart=-1;

			for (int ic=0; ic<data.getNumChars() && ic<charFlags.getSize(); ic++){
				if (!charFlags.isBitOn(ic)){  // we are in a remaining block
					if (blockStart<0){  // start of block, so set the value
						blockStart = ic;
					}
					if (ic==data.getNumChars()-1) {  // end of matrix, so need to check if block is big enough
						examineRemainingBlock(status,charFlags,blockStart,ic);
					}
				} else {  // let's check to see if we have reached the end of a non-conserved block
					if (blockStart>=0){ // we were within a conserved block, now just one past it
						if (ic-blockStart > CP)  // block is big enough to be selected
							examineRemainingBlock(status, charFlags,blockStart,ic-1);
					}	
					blockStart=-1;  // reset to make it clear we are no longer in a non-conserved block
				}
			}

			// ======  checking for small blocks
			blockStart=-1;

			for (int ic=0; ic<data.getNumChars() && ic<charFlags.getSize(); ic++){
				if (!charFlags.isBitOn(ic)){
					if (blockStart<0){  // start of block, so set the value
						blockStart = ic;
					}
					if (ic==data.getNumChars()-1) {  // end of matrix, so need to check if block is big enough
						if (ic-blockStart+1 < BL){  // block is big enough to be selected
							setToSelectRange(charFlags,blockStart,ic);
						}
					}
				} else {  // let's check to see if we have reached the end of a non-conserved block
					if (blockStart>=0){ // we were within a conserved block, now just one past it
						if (ic-blockStart < BL){  // block is big enough to be selected
							setToSelectRange(charFlags,blockStart,ic-1);
						}
					}	
					blockStart=-1;  // reset to make it clear we are no longer in a non-conserved block
				}
			}

			// ======  checking for gaps and nearby non-conserved


			for (int ic=0; ic<data.getNumChars() && ic<charFlags.getSize(); ic++){
				if (!charFlags.isBitOn(ic) && tooManyGaps((CategoricalData)data,ic)){  // let's start at any gap that is not yet excluded
					examineRegionAroundGap(status, charFlags,ic);

				}
			}

			// ======  checking for small blocks
			blockStart=-1;

			for (int ic=0; ic<data.getNumChars() && ic<charFlags.getSize(); ic++){
				if (!charFlags.isBitOn(ic)){
					if (blockStart<0){  // start of block, so set the value
						blockStart = ic;
					}
					if (ic==data.getNumChars()-1) {  // end of matrix, so need to check if block is big enough
						if (ic-blockStart+1 < BL){  // block is big enough to be selected
							setToSelectRange(charFlags,blockStart,ic);
						}
					}
				} else {  // let's check to see if we have reached the end of a non-conserved block
					if (blockStart>=0){ // we were within a conserved block, now just one past it
						if (ic-blockStart < BL){  // block is big enough to be selected
							setToSelectRange(charFlags,blockStart,ic-1);
						}
					}	
					blockStart=-1;  // reset to make it clear we are no longer in a non-conserved block
				}
			}
			//if (!chooseBadSites.getValue())
			//	charFlags.invertAllBits();
			// ======  Now report the results
			boolean report = false;
			if (report){
			blockStart=-1;
			StringBuffer blocks = new StringBuffer();
			for (int ic=0; ic<data.getNumChars() && ic<charFlags.getSize(); ic++){
				if (!charFlags.isBitOn(ic)){
					if (blockStart<0){  // start of block, so set the value
						blockStart = ic;
					}
					if (ic==data.getNumChars()-1) {  // end of matrix
						blocks.append(" [" + (blockStart+1)+" " + (ic+1)+ "]");
					}
				} else {  // let's check to see if we have reached the end of a  block
					if (blockStart>=0){ // we were within the block, now just one past it
						blocks.append(" [" + (blockStart+1)+" " + (ic)+ "]");
					}	
					blockStart=-1;  // reset to make it clear we are no longer in a non-conserved block
				}
			}
			}
			

		}
		return flags;
	}

	/*.................................................................................................................*/
	
	/*.................................................................................................................*/
	/*.................................................................................................................*/
	public String getName() {
		return "GblocksM";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Flags sites according to GblocksM, an extended version of Gblocks criteria (Castresana, 2000)." ;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}
}


