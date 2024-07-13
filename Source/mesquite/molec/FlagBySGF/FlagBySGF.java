/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.FlagBySGF;
/*~~  */



import java.awt.Button;
import java.awt.Checkbox;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import mesquite.categ.lib.CategoricalData;
import mesquite.lib.Bits;
import mesquite.lib.CommandChecker;
import mesquite.lib.Debugg;
import mesquite.lib.DoubleField;
import mesquite.lib.ExtensibleDialog;
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
import mesquite.lib.duties.MatrixFlagger;

/* ======================================================================== */
public class FlagBySGF extends MatrixFlagger implements ActionListener {

	/** TODO?
	 * --  call this SGF for simple gappiness filter? Or Gapsi?
	 * --  toggle to ignore terminal gaps?
	 * --  toggle to ignore taxa without data?
	 */
	/*Gappiness assessment parameters =================================*/
	static boolean filterSiteGappinessDEFAULT = true;
	static boolean filterBlockGappinessDEFAULT = true;
	static boolean forgiveTaxaWithoutDataDEFAULT = true;
	static double siteGappinessThresholdDEFAULT = 0.5; // A site is considered good (for gappiness) if it is less gappy than this (term or non-term).
	static int gappyBlockSizeDEFAULT = 5; // If in a block of at least this many sites, the first and last site is bad,
	static double blockGappinessThresholdDEFAULT = 0.5; // and the proportion of bad sites is this high or higher,
	static int gappyBoundaryDEFAULT = 4; // and there are no stretches of this many good sites in a row,

	MesquiteBoolean filterSiteGappiness = new MesquiteBoolean(filterSiteGappinessDEFAULT);
	MesquiteBoolean filterBlockGappiness = new MesquiteBoolean(filterBlockGappinessDEFAULT);
	MesquiteBoolean forgiveTaxaWithoutData = new MesquiteBoolean(forgiveTaxaWithoutDataDEFAULT);

	double siteGappinessThreshold = siteGappinessThresholdDEFAULT; // A site is considered good (for gappiness) if it is less gappy than this (term or non-term).
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

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		if (!MesquiteThread.isScripting()) {
			if (!queryOptions())
				return false;
		}
		addMenuItem(null, "Set Simple Gappiness Filter options...", makeCommand("setOptions", this));
		return true;
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "filterBlockGappiness", filterBlockGappiness);  
		StringUtil.appendXMLTag(buffer, 2, "filterSiteGappiness", filterSiteGappiness);  
		StringUtil.appendXMLTag(buffer, 2, "forgiveTaxaWithoutData", forgiveTaxaWithoutData);  
		StringUtil.appendXMLTag(buffer, 2, "siteGappinessThreshold", siteGappinessThreshold);  
		StringUtil.appendXMLTag(buffer, 2, "gappyBlockSize", gappyBlockSize);  
		StringUtil.appendXMLTag(buffer, 2, "blockGappinessThreshold", blockGappinessThreshold);  
		StringUtil.appendXMLTag(buffer, 2, "gappyBoundary", gappyBoundary);  
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
		if ("filterBlockGappiness".equalsIgnoreCase(tag))
			filterBlockGappiness.setValue(MesquiteBoolean.fromTrueFalseString(content));
		if ("filterSiteGappiness".equalsIgnoreCase(tag))
			filterSiteGappiness.setValue(MesquiteBoolean.fromTrueFalseString(content));
		if ("forgiveTaxaWithoutData".equalsIgnoreCase(tag))
			forgiveTaxaWithoutData.setValue(MesquiteBoolean.fromTrueFalseString(content));
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("setfilterBlockGappiness " + filterBlockGappiness.toOffOnString());
		temp.addLine("setfilterSiteGappiness " + filterSiteGappiness.toOffOnString());
		temp.addLine("setforgiveTaxaWithoutData " + forgiveTaxaWithoutData.toOffOnString());
		temp.addLine("setSiteGappinessThreshold " + siteGappinessThreshold);
		temp.addLine("setgappyBlockSize " + gappyBlockSize);
		temp.addLine("setBlockGappinessThreshold " + blockGappinessThreshold);
		temp.addLine("setgappyBoundary " + gappyBoundary);
		return temp;
	}


	DoubleField pgSField;
	Checkbox fIGS;
	Checkbox fG, fTT;
	IntegerField gBS;
	IntegerField gB;
	DoubleField pgBField;
	private boolean queryOptions() {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(),  "Criteria for Simple Gappiness Filter",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()

		pgSField = dialog.addDoubleField("Proportion of gaps that marks site as too gappy (\"bad\")", siteGappinessThreshold, 4);
		fTT = dialog.addCheckBox("Ignore gaps in taxa with no data", forgiveTaxaWithoutData.getValue());
		dialog.addHorizontalLine(1);
		dialog.addLabel("Selecting individual sites");
		fIGS = dialog.addCheckBox("Select individual gappy sites (whether or not part of gappy block)", filterSiteGappiness.getValue());
		dialog.addHorizontalLine(1);
		dialog.addLabel("Selecting blocks");
		fG = dialog.addCheckBox("Select gappy blocks", filterBlockGappiness.getValue());
		gBS = dialog.addIntegerField("Minimum length of bad block", gappyBlockSize, 4);
		gB = dialog.addIntegerField("Stretch of good that resets block", gappyBoundary, 4);
		pgBField = dialog.addDoubleField("Proportion of bad sites for block to be bad", blockGappinessThreshold, 4);

		dialog.addHorizontalLine(1);
		dialog.addBlankLine();
		Button useDefaultsButton = null;
		useDefaultsButton = dialog.addAListenedButton("Set to Defaults", null, this);
		useDefaultsButton.setActionCommand("setToDefaults");
		dialog.addHorizontalLine(1);
		dialog.addBlankLine();


		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			filterBlockGappiness.setValue(fG.getState());
			filterSiteGappiness.setValue(fIGS.getState());
			forgiveTaxaWithoutData.setValue(fTT.getState());
			siteGappinessThreshold = pgSField.getValue();
			gappyBlockSize = gBS.getValue();
			gappyBoundary = gB.getValue();
			blockGappinessThreshold = pgBField.getValue();

			storePreferences();
		}
		dialog.dispose();
		return (buttonPressed.getValue()==0);
	}

	/*.................................................................................................................*/
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equalsIgnoreCase("setToDefaults")) {
			pgSField.setValue(siteGappinessThresholdDEFAULT);
			fIGS.setState(filterSiteGappinessDEFAULT);
			fG.setState(filterBlockGappinessDEFAULT);
			fTT.setState(forgiveTaxaWithoutDataDEFAULT);
			gBS.setValue(gappyBlockSizeDEFAULT);
			gB.setValue(gappyBoundaryDEFAULT);
			pgBField.setValue(blockGappinessThresholdDEFAULT);

		} 
	}
	public void queryOptionsOtherThanEmployees () {
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
		else if (checker.compare(this.getClass(), "Sets whether or not to forgive gaps in taxa with no data.", "[on or off]", commandName, "setforgiveTaxaWithoutData")) {
			boolean current = forgiveTaxaWithoutData.getValue();
			forgiveTaxaWithoutData.toggleValue(parser.getFirstToken(arguments));
			if (current!=forgiveTaxaWithoutData.getValue()) {
				parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Sets whether or not to filter blocks by gappiness also.", "[on or off]", commandName, "setfilterBlockGappiness")) {
			boolean current = filterBlockGappiness.getValue();
			filterBlockGappiness.toggleValue(parser.getFirstToken(arguments));
			if (current!=filterBlockGappiness.getValue()) {
				parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Sets whether or not to filter individual sites by gappiness also.", "[on or off]", commandName, "setfilterSiteGappiness")) {
			boolean current = filterSiteGappiness.getValue();
			filterSiteGappiness.toggleValue(parser.getFirstToken(arguments));
			if (current!=filterSiteGappiness.getValue()) {
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
		else if (checker.compare(this.getClass(), "Sets proportion of taxa than need to have gaps for site to be gappy.", "[proportion]", commandName, "setSiteGappinessThreshold")) {
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
		return siteGappiness[k]>=siteGappinessThreshold;
	}
	boolean countTaxon(int it) {
		if (forgiveTaxaWithoutData.getValue())
			return taxonHasData[it];
		return true;
	}
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

			if (forgiveTaxaWithoutData.getValue()) {
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
			else
				numTaxaCounted = numTaxa;
			if (siteGappiness == null || siteGappiness.length != numChars) {
				siteGappiness = new double[numChars];
			}
			for (int ic=0; ic<numChars; ic++) {
				int gapCount = 0;
				for (int it = 0; it<numTaxa; it++) {
					if (countTaxon(it) && data.isInapplicable(ic,it)) 
						gapCount++;
				}
				siteGappiness[ic] = 1.0*gapCount/numTaxaCounted;
				if (gapCount == numTaxaCounted)//if all gaps, delete regardless
					charFlags.setBit(ic, true);
				else if (filterSiteGappiness.getValue() && gappySite(ic))
					charFlags.setBit(ic, true); 				
			}

			if (filterBlockGappiness.getValue()) {
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
			return "Simple Gappiness Filter";
		}
		/*.................................................................................................................*/
		/** returns an explanation of what the module does.*/
		public String getExplanation() {
			return "Flags sites or regions of sites with a certain proportion of gaps." ;
		}
		/*.................................................................................................................*/
		/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
		 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
		 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
		public int getVersionOfFirstRelease(){
			return NEXTRELEASE;  
		}


	}

