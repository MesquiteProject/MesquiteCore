/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.FlagGappyRegions;
/*~~  */



import java.awt.Button;
import java.awt.Checkbox;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import mesquite.categ.lib.CategoricalData;
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
import mesquite.lib.MesquiteThread;
import mesquite.lib.RadioButtons;
import mesquite.lib.Snapshot;
import mesquite.lib.StringUtil;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.MatrixFlags;
import mesquite.lib.duties.MatrixFlagger;
import mesquite.lib.duties.MatrixFlaggerForTrimming;
import mesquite.lib.duties.MatrixFlaggerForTrimmingSites;

/* ======================================================================== */
public class FlagGappyRegions extends MatrixFlaggerForTrimmingSites implements ActionListener, ItemListener {

	/** TODO?

	 */
	/*Gappiness assessment parameters =================================*/
	//static boolean forgiveTaxaWithoutDataDEFAULT = true;
	static double siteGappinessThresholdDEFAULT = 0.5; // A site is considered good (for gappiness) if it is less gappy than this (term or non-term).
	static int gappyBlockSizeDEFAULT = 5; // If in a block of at least this many sites, the first and last site is bad,
	static double blockGappinessThresholdDEFAULT = 0.5; // and the proportion of bad sites is this high or higher,
	static int gappyBoundaryDEFAULT = 4; // and there are no stretches of this many good sites in a row,
	//static int COUNT_ALL_CURRENT_TAXA = 0;
	//static int IGNORE_TAXA_ALL_GAPS = 1;
	//static int ASSUME_SPECIFIED_NUM_TAXA = 2;
	static boolean ignoreDatalessDEFAULT = false;
	static boolean assumeSpecifiedNumberDEFAULT = false; 

	//MesquiteBoolean forgiveTaxaWithoutData = new MesquiteBoolean(forgiveTaxaWithoutDataDEFAULT);
	boolean ignoreDataless = ignoreDatalessDEFAULT;
	boolean assumeSpecifiedNumber = assumeSpecifiedNumberDEFAULT;
	int specifiedNumTaxa = MesquiteInteger.unassigned;

	double siteGappinessThreshold = siteGappinessThresholdDEFAULT; // A site is considered good (for gappiness) if it is less or as gappy than this (term or non-term).
	int gappyBlockSize = gappyBlockSizeDEFAULT; // If in a block of at least this many sites, the first and last site is bad,
	double blockGappinessThreshold = blockGappinessThresholdDEFAULT; // and the proportion of bad sites is this high or higher,
	int gappyBoundary = gappyBoundaryDEFAULT; // and there are no stretches of this many good sites in a row,
	// then select the block.

	// CCCCAA--A--A------TTTT--AACCCC
	// CCCCAAG-A--A------TTTT--AACCCC
	// CCCCAAG-A--A------TTTT--AACCCC
	// CCCCAAG----A------------AACCCC
	//        ***********
	//
	// in the above, the column after the Gs is the first bad site, 
	// and the two following As columns are too narrow to stop the block. However, the Ts
	// columns are 4 in a row, so they stop the block
	/**/



	boolean queried = false;

	public CompatibilityTest getCompatibilityTest(){
		return new RequiresAnyMolecularData();
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		if (!MesquiteThread.isScripting()) {
			if (!queryOptions())
				return false;
		}
		addMenuItem(null, "Set Gappy Regions options...", makeCommand("setOptions", this));
		return true;
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "ignoreDataless", ignoreDataless);  
		StringUtil.appendXMLTag(buffer, 2, "siteGappinessThreshold", siteGappinessThreshold);  
		StringUtil.appendXMLTag(buffer, 2, "gappyBlockSize", gappyBlockSize);  
		StringUtil.appendXMLTag(buffer, 2, "blockGappinessThreshold", blockGappinessThreshold);  
		StringUtil.appendXMLTag(buffer, 2, "gappyBoundary", gappyBoundary);  
		StringUtil.appendXMLTag(buffer, 2, "assumeSpecifiedNumber", assumeSpecifiedNumber);  
		return buffer.toString();
	}
	public void processSingleXMLPreference (String tag, String flavor, String content){
		processSingleXMLPreference(tag, null, content);
	}

	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("blockGappinessThreshold".equalsIgnoreCase(tag))
			blockGappinessThreshold = MesquiteDouble.fromString(content);
		if ("siteGappinessThreshold".equalsIgnoreCase(tag))
			siteGappinessThreshold = MesquiteDouble.fromString(content);
		if ("gappyBlockSize".equalsIgnoreCase(tag))
			gappyBlockSize = MesquiteInteger.fromString(content);
		if ("gappyBoundary".equalsIgnoreCase(tag))
			gappyBoundary = MesquiteInteger.fromString(content);
		if ("ignoreDataless".equalsIgnoreCase(tag))
			ignoreDataless=  MesquiteBoolean.fromTrueFalseString(content);
		if ("assumeSpecifiedNumber".equalsIgnoreCase(tag))
			assumeSpecifiedNumber= MesquiteBoolean.fromTrueFalseString(content);
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		if (getProject().isProcessDataFilesProject){
			temp.addLine("setAssumeSpecifiedNumber " + assumeSpecifiedNumber);
			if (assumeSpecifiedNumber)
				temp.addLine("setSpecifiedNumTaxa " + MesquiteInteger.toString(specifiedNumTaxa));
		}
		else
			temp.addLine("ignoreDataless " + ignoreDataless);
		temp.addLine("setSiteGappinessThreshold " + siteGappinessThreshold);
		temp.addLine("setgappyBlockSize " + gappyBlockSize);
		temp.addLine("setBlockGappinessThreshold " + blockGappinessThreshold);
		temp.addLine("setgappyBoundary " + gappyBoundary);
		return temp;
	}


	DoubleField pgSField;
	Checkbox fIGS;
	Checkbox fG;
	IntegerField gBS;
	IntegerField gB, sNT;
	DoubleField pgBField;
	Checkbox specifNumCB, ignoreDatalessCB;
	private boolean queryOptions() {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(),  "Criteria for Gappy Regions Filter",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()

		pgSField = dialog.addDoubleField("Maximum permitted proportion of gaps (above which site is considered too gappy)", siteGappinessThreshold, 4);
		String s = "<b>Gappy Regions filter</b> selects regions of an alignment with high levels of gaps. The algorithm is simple and untested, but feel free to explore!"
				+ " It is not intended to identify regions that are unreliable or poorly aligned; it is intended simply to find regions"
				+ " where the amount of available data is too sparse to justify inclusion, just as one filters loci for occupancy.<hr>" 
				+"The choice of how to count taxa is relevant especially for data with multiple loci. A locus might have data for only some taxa."
				+ " This could result in different countings of the proportion of gaps depending on whether the trimming is done on individual files for each locus (because each file will know only"
				+" about the number of taxa that have data for that locus) versus in the compiled file (which will know about all of the taxa).";




		gBS = dialog.addIntegerField("Minimum length of region assessed", gappyBlockSize, 4);
		gB = dialog.addIntegerField("Stretch of non-gappy sites that resets gappy region", gappyBoundary, 4);
		pgBField = dialog.addDoubleField("Proportion of gappy sites for region to be judged as gappy", blockGappinessThreshold, 4);
		dialog.addHorizontalLine(1);
		dialog.addBlankLine();
		if (getProject().isProcessDataFilesProject){
			specifNumCB = dialog.addCheckBox("Assume specified total number of taxa (see Help button)", assumeSpecifiedNumber);
			specifNumCB.addItemListener(this);
			sNT = dialog.addIntegerField("                           Specified total number of taxa", specifiedNumTaxa, 4);
			sNT.setEnabled(assumeSpecifiedNumber);
			s += "<p>When processing separate files for each locus, if you want the proportion of gaps to be assessed over the final set of all taxa," 
					+ " choose \"Assume specified total\", and indicate the total number of taxa in the final compilation."
					+ " Doing so will generally result in a harsher trimming, and can mimic trimming done later in a file of all loci compiled."
					+ "<p><b>Recommendation</b>: if you want to treat this trimming as an occupancy criterion (just like filtering loci for occupancy) then DO select \"Assume specified\" and indicate the total number, OR "
					+ "delay the gappiness trimming until after the loci are compiled into a single file.";
		}
		else {
			ignoreDatalessCB = dialog.addCheckBox("Ignore gaps in taxa with no data in matrix", ignoreDataless);
			s += "<p>To mimic the results you would obtain were you to process the loci individually in separate files (e.g. in a scripted pipeline), choose \"Ignore gaps in taxa with no data in matrix\"." 
					+ " This will result in a more permissive trimming."
					+ "<p><b>Recommendation</b>: if you want to treat this trimming as an occupancy criterion (just like filtering loci for occupancy) then DON'T select \"Ignore\".";
		}
		dialog.appendToHelpString(s);
		dialog.addHorizontalLine(1);
		dialog.addBlankLine();
		Button useDefaultsButton = null;
		useDefaultsButton = dialog.addAListenedButton("Set to Defaults", null, this);
		useDefaultsButton.setActionCommand("setToDefaults");
		dialog.addHorizontalLine(1);
		dialog.addBlankLine();


		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			siteGappinessThreshold = pgSField.getValue();
			gappyBlockSize = gBS.getValue();
			gappyBoundary = gB.getValue();
			if (ignoreDatalessCB!=null){
				ignoreDataless = ignoreDatalessCB.getState();
			}
			if (specifNumCB!=null){
				assumeSpecifiedNumber = specifNumCB.getState();
			}
			if (assumeSpecifiedNumber && sNT != null)
				specifiedNumTaxa = sNT.getValue();
			blockGappinessThreshold = pgBField.getValue();

			storePreferences();
		}
		dialog.dispose();
		return (buttonPressed.getValue()==0);
	}

	public void itemStateChanged(ItemEvent e) {
		if (sNT != null)
			sNT.setEnabled(specifNumCB.getState());

	}
	/*.................................................................................................................*/
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equalsIgnoreCase("setToDefaults")) {
			pgSField.setValue(siteGappinessThresholdDEFAULT);
			if (ignoreDatalessCB!=null){
				ignoreDatalessCB.setState(ignoreDatalessDEFAULT);
			}
			if (sNT != null){
				specifNumCB.setState(assumeSpecifiedNumberDEFAULT);
				sNT.setValue(MesquiteInteger.unassigned);
				sNT.setEnabled(assumeSpecifiedNumberDEFAULT);
			}
			gBS.setValue(gappyBlockSizeDEFAULT);
			gB.setValue(gappyBoundaryDEFAULT);
			pgBField.setValue(blockGappinessThresholdDEFAULT);

		} 
	}
	public void queryLocalOptions () {
		if (queryOptions())
			storePreferences();
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets options for gappiness", "", commandName, "setOptions")) {
			if (queryOptions()) {
				storePreferences();
				parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Sets whether to count taxa with no data, to count all taxa in current file, or to count as if there were a specified number.", "[0, 1, 2]", commandName, "ignoreDataless")) {
			boolean s = MesquiteBoolean.fromTrueFalseString(parser.getFirstToken(arguments));
				ignoreDataless = s;
				if (!MesquiteThread.isScripting())
					parametersChanged(); 
			
		}
		else if (checker.compare(this.getClass(), "Sets specified number of taxa to count.", "[integer]", commandName, "setSpecifiedNumTaxa")) {
			int s = MesquiteInteger.fromString(parser.getFirstToken(arguments));
			if (MesquiteInteger.isCombinable(s)){
				specifiedNumTaxa = s;
				if (!MesquiteThread.isScripting())
					parametersChanged(); 
			}
		}
		else if (checker.compare(this.getClass(), "Sets minimum size of bad gappy block.", "[integer]", commandName, "setgappyBlockSize")) {
			int s = MesquiteInteger.fromString(parser.getFirstToken(arguments));
			if (MesquiteInteger.isCombinable(s)){
				gappyBlockSize = s;
				if (!MesquiteThread.isScripting())
					parametersChanged(); 
			}

		}
		else if (checker.compare(this.getClass(), "Sets required size to form a non-gappy boundary.", "[integer]", commandName, "setgappyBoundary")) {
			int s = MesquiteInteger.fromString(parser.getFirstToken(arguments));
			if (MesquiteInteger.isCombinable(s)){
				gappyBoundary = s;
				if (!MesquiteThread.isScripting())
					parametersChanged(); 
			}

		}
		else if (checker.compare(this.getClass(), "Sets proportion of taxa with gaps above which site is considered gappy.", "[proportion]", commandName, "setSiteGappinessThreshold")) {
			double s = MesquiteDouble.fromString(parser.getFirstToken(arguments));
			if (MesquiteDouble.isCombinable(s)){
				siteGappinessThreshold = s;
				if (!MesquiteThread.isScripting())
					parametersChanged(); 

			}
		}
		else if (checker.compare(this.getClass(), "Sets proportion of sites in span that are gappy to trigger selection.", "[proportion]", commandName, "setBlockGappinessThreshold")) {
			double s = MesquiteDouble.fromString(parser.getFirstToken(arguments));
			if (MesquiteDouble.isCombinable(s)){
				blockGappinessThreshold = s;
				if (!MesquiteThread.isScripting())
					parametersChanged(); 

			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/*======================================================*/
	double[] siteGappiness;
	boolean[] taxonHasData;
	int numTaxaCounted = 0;
	/*======================================================*/
	boolean isGapBlockBoundary(int k) {
		for (int i = 0; k+i<siteGappiness.length && i<gappyBoundary; i++)
			if (gappySite(k+i))
				return false;
		return true;
	}
	boolean gappySite(int k) {
		return siteGappiness[k]>siteGappinessThreshold;
	}
	
	boolean ignoreDatalessTaxa(){
		return ignoreDataless && !getProject().isProcessDataFilesProject; //this option available only for stable project open to user
	}
	boolean assumeSpecifiedNumberOfTaxa(){
		return assumeSpecifiedNumber && getProject().isProcessDataFilesProject; //this option available only for stable project open to user
	}

	boolean countTaxon(int it) {
		if (ignoreDatalessTaxa())
			return taxonHasData[it];
		return true;
	}

	int numNegWarnings = 0;
	/*======================================================*/
	public MatrixFlags flagMatrix(CharacterData data, MatrixFlags flags) {
		if (data!=null && data.getNumChars()>0 && data instanceof CategoricalData){
			if (flags == null)
				flags = new MatrixFlags(data);
			else 
				flags.reset(data);

			Bits charFlags = flags.getCharacterFlags();		
			int numTaxa = data.getNumTaxa();
			int numChars = data.getNumChars();

			if (ignoreDatalessTaxa()) {  //count how many taxa are all gaps
				numTaxaCounted = 0;
				if (taxonHasData == null || taxonHasData.length!= numTaxa)
					taxonHasData = new boolean[numTaxa];
				for (int it=0; it<numTaxa; it++){
					taxonHasData[it] = false;
					for (int ic=0; ic<numChars; ic++)
						if (!data.isInapplicable(ic,it)){
							taxonHasData[it] = true;
							break;
						}
					if (taxonHasData[it])
						numTaxaCounted++;
				}
			}
			else if (assumeSpecifiedNumberOfTaxa()){
				if (specifiedNumTaxa < numTaxa){
					if (numNegWarnings<10)
						discreetAlert("ERROR: Specified number of taxa in Flag Gappy Regions (" + specifiedNumTaxa + ") is smaller than current number of taxa in the file (" + numTaxa + ")");
					else if (numNegWarnings<11)
						logln("No more warnings will be given about ERROR that specified number of taxa in Flag Gappy Regions is smaller than current number of taxa in the file");
					numNegWarnings++;
				}
				if (MesquiteInteger.isPositive(specifiedNumTaxa))
					numTaxaCounted = specifiedNumTaxa;
				else
					numTaxaCounted = numTaxa;
			}
			else
				numTaxaCounted = numTaxa;

			if (siteGappiness == null || siteGappiness.length != numChars) {
				siteGappiness = new double[numChars];
			}
			for (int ic=0; ic<numChars; ic++) {
				int gapCount = 0;
				if (assumeSpecifiedNumberOfTaxa() && MesquiteInteger.isPositive(specifiedNumTaxa)) {
					gapCount = specifiedNumTaxa - numTaxa;  //start with a gap count that is number of taxa not included in file!
					if (gapCount <0)
						gapCount = 0;
				}
				for (int it = 0; it<numTaxa; it++) {
					if (countTaxon(it) && data.isInapplicable(ic,it)) 
						gapCount++;
				}
				siteGappiness[ic] = 1.0*gapCount/numTaxaCounted;

				if (gapCount == numTaxaCounted)//if all gaps, delete regardless 
					charFlags.setBit(ic, true);
			}

				for (int blockStart=0; blockStart<numChars; blockStart++) { //let's look for gappy blocks
					if (gappySite(blockStart)) { // possible start of gappy block

						boolean boundaryFound = false;
						for (int candidateNextBoundary = blockStart+1; candidateNextBoundary<numChars+1 && !boundaryFound; candidateNextBoundary++) {  // go ahead until next boundary reached
							if (isGapBlockBoundary(candidateNextBoundary) || candidateNextBoundary == numChars-1) {
								boundaryFound = true;
								int blockEnd = candidateNextBoundary-1;

								//blockStart is the potential start of a block; blockEnd is a possible end. If the block is long enough, ask if its blockGappiness is bad
								if (blockEnd-blockStart+1 >= gappyBlockSize){
									//block is big enough, but is it bad enough?
									int badSiteCount = 0;
									for (int k = blockStart; k <= blockEnd; k++)
										if (gappySite(k))  // stored as double[] in case criterion shifts, e.g., to average
											badSiteCount++;
									double blockGappiness = 1.0*badSiteCount/(blockEnd-blockStart+1);
									if (blockGappiness >=blockGappinessThreshold)
										for (int k = blockStart; k <= blockEnd; k++)
											charFlags.setBit(k, true);
								}

							}
						}
					}
				
			}
		}
		return flags;

	}

	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return true;
	}

	/*.................................................................................................................*/
	public boolean showCitation(){
		return false;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Gappy Regions";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Flags regions of an alignment whose proportion of gaps is above a threshold." ;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}


}


