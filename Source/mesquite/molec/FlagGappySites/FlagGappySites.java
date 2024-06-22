/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.FlagGappySites;
/*~~  */



import java.awt.Checkbox;

import mesquite.lib.Bits;
import mesquite.lib.CommandChecker;
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
import mesquite.molec.lib.SiteFlagger;

/* ======================================================================== */
public class FlagGappySites extends SiteFlagger {
	
	/** TODO?
	 * -- ignore terminal gaps
	 * -- ignore taxa without data
	 */
	/*Gappiness assessment parameters =================================*/
	static boolean filterGapIndSiteDEFAULT = true;
	static boolean filterBlockGappinessDEFAULT = false;
	static double siteGappinessThresholdDEFAULT = 0.5; // A site is considered good (for gappiness) if it is less gappy than this (term or non-term).
	static int minGappyBlockSizeDEFAULT = 4; // If in a block of at least this many sites, the first and last site is bad,
	static double blockGappinessThresholdDEFAULT = 0.5; // and the proportion of bad sites is this high or higher,
	static int minGappyBoundaryDEFAULT = 4; // and there are no stretches of this many good sites in a row,

	MesquiteBoolean filterGapIndSite = new MesquiteBoolean(filterGapIndSiteDEFAULT);
	MesquiteBoolean filterBlockGappiness = new MesquiteBoolean(filterBlockGappinessDEFAULT);
	double siteGappinessThreshold = siteGappinessThresholdDEFAULT; // A site is considered good (for gappiness) if it is less gappy than this (term or non-term).
	int minGappyBlockSize = minGappyBlockSizeDEFAULT; // If in a block of at least this many sites, the first and last site is bad,
	double blockGappinessThreshold = blockGappinessThresholdDEFAULT; // and the proportion of bad sites is this high or higher,
	int minGappyBoundary = minGappyBoundaryDEFAULT; // and there are no stretches of this many good sites in a row,
	// then select the block.

	// CCCCAA--A--A------TTTT--AACCCC
	// CCCCAAG-A--A------TTTT--AACCCC
	// CCCCAAG-A--A------TTTT--AACCCC
	// CCCCAAG----A------------AACCCC
	//        ***********
	//
	// in the above, the column after the Gs is the first bad site, and the two following As columns are too narrow to stop the block. However, the Ts
	// columns are 4 in a row, so they stop the block
	 /**/


	
//	double threshold = MesquiteDouble.unassigned;
	boolean queried = false;

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		if (!MesquiteThread.isScripting()) {
			if (!queryOptions())
				return false;
		}
		addMenuItem(null, "Set gappiness options...", makeCommand("setOptions", this));
		return true;
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
	//	StringUtil.appendXMLTag(buffer, 2, "threshold", threshold);  
		StringUtil.appendXMLTag(buffer, 2, "filterBlockGappiness", filterBlockGappiness);  
		StringUtil.appendXMLTag(buffer, 2, "filterGapIndSite", filterGapIndSite);  
		StringUtil.appendXMLTag(buffer, 2, "siteGappinessThreshold", siteGappinessThreshold);  
		StringUtil.appendXMLTag(buffer, 2, "minGappyBlockSize", minGappyBlockSize);  
		StringUtil.appendXMLTag(buffer, 2, "blockGappinessThreshold", blockGappinessThreshold);  
		StringUtil.appendXMLTag(buffer, 2, "minGappyBoundary", minGappyBoundary);  
		return buffer.toString();
	}
	public void processSingleXMLPreference (String tag, String flavor, String content){
		processSingleXMLPreference(tag, null, content);
	}

	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
	//	if ("threshold".equalsIgnoreCase(tag))
	//		threshold = MesquiteDouble.fromString(content);
		if ("blockGappinessThreshold".equalsIgnoreCase(tag))
			blockGappinessThreshold = MesquiteDouble.fromString(content);
		if ("siteGappinessThreshold".equalsIgnoreCase(tag))
			siteGappinessThreshold = MesquiteDouble.fromString(content);
		if ("minGappyBlockSize".equalsIgnoreCase(tag))
			minGappyBlockSize = MesquiteInteger.fromString(content);
		if ("minGappyBoundary".equalsIgnoreCase(tag))
			minGappyBoundary = MesquiteInteger.fromString(content);
		if ("filterBlockGappiness".equalsIgnoreCase(tag))
			filterBlockGappiness.setValue(MesquiteBoolean.fromTrueFalseString(content));
		if ("filterGapIndSite".equalsIgnoreCase(tag))
			filterGapIndSite.setValue(MesquiteBoolean.fromTrueFalseString(content));
	}
	/*.................................................................................................................*/
 	 public Snapshot getSnapshot(MesquiteFile file) { 
  	 	Snapshot temp = new Snapshot();
	 //	temp.addLine("setThreshold " + threshold); 
		temp.addLine("setfilterBlockGappiness " + filterBlockGappiness.toOffOnString());
		temp.addLine("setfilterGapIndSite " + filterGapIndSite.toOffOnString());
		temp.addLine("setSiteGappinessThreshold " + siteGappinessThreshold);
		temp.addLine("setMinGappyBlockSize " + minGappyBlockSize);
		temp.addLine("setBlockGappinessThreshold " + blockGappinessThreshold);
		temp.addLine("setMinGappyBoundary " + minGappyBoundary);
 	 	return temp;
 	 }

 	
 	DoubleField pgSField;
 	Checkbox fIGS;
 	Checkbox fG;
 	IntegerField minGBS;
 	IntegerField minGB;
 	DoubleField pgBField;
	 private boolean queryOptions() {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(),  "Select Gappy Sites or Blocks",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()

		pgSField = dialog.addDoubleField("Proportion of gaps that marks site as too gappy (\"bad\")", siteGappinessThreshold, 4);
		dialog.addHorizontalLine(1);
		dialog.addLabel("Selecting individual sites");
		fIGS = dialog.addCheckBox("Select individual gappy sites (whether or not part of gappy block)", filterGapIndSite.getValue());
		dialog.addHorizontalLine(1);
		dialog.addLabel("Selecting blocks");
		fG = dialog.addCheckBox("Select gappy blocks", filterBlockGappiness.getValue());
		minGBS = dialog.addIntegerField("Minimum length of bad block", minGappyBlockSize, 4);
		minGB = dialog.addIntegerField("Stretch of good that resets block", minGappyBoundary, 4);
		pgBField = dialog.addDoubleField("Proportion of bad sites for block to be bad", blockGappinessThreshold, 4);

		dialog.addHorizontalLine(1);
		dialog.addBlankLine();
		//Button useDefaultsButton = null;
		//useDefaultsButton = dialog.addAListenedButton("Set to Defaults", null, this);
		//useDefaultsButton.setActionCommand("setToDefaults");


		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			filterBlockGappiness.setValue(fG.getState());
			filterGapIndSite.setValue(fIGS.getState());
			siteGappinessThreshold = pgSField.getValue();
			minGappyBlockSize = minGBS.getValue();
			minGappyBoundary = minGB.getValue();
			blockGappinessThreshold = pgBField.getValue();

			storePreferences();
		}
		dialog.dispose();
		return (buttonPressed.getValue()==0);
	}

	/*.................................................................................................................*/
   	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
  	 	if (checker.compare(this.getClass(), "Sets options for gappiness", "", commandName, "setOptions")) {
   	 		if (queryOptions()) {
   	 			storePreferences();
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
		else if (checker.compare(this.getClass(), "Sets whether or not to filter individual sites by gappiness also.", "[on or off]", commandName, "setfilterGapIndSite")) {
			boolean current = filterGapIndSite.getValue();
			filterGapIndSite.toggleValue(parser.getFirstToken(arguments));
			if (current!=filterGapIndSite.getValue()) {
				parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Sets minimum size of bad gappy block.", "[on or off]", commandName, "setMinGappyBlockSize")) {
			int s = MesquiteInteger.fromString(parser.getFirstToken(arguments));
			if (MesquiteInteger.isCombinable(s)){
				minGappyBlockSize = s;
				if (!MesquiteThread.isScripting())
					parametersChanged(); 
			}

		}
		else if (checker.compare(this.getClass(), "Sets required size to form a non-gappy boundary.", "[on or off]", commandName, "setMinGappyBoundary")) {
			int s = MesquiteInteger.fromString(parser.getFirstToken(arguments));
			if (MesquiteInteger.isCombinable(s)){
				minGappyBoundary = s;
				if (!MesquiteThread.isScripting())
					parametersChanged(); 
			}

		}
		else if (checker.compare(this.getClass(), "Sets proportion of taxa than need to have gaps for site to be gappy.", "[on or off]", commandName, "setSiteGappinessThreshold")) {
			double s = MesquiteDouble.fromString(parser.getFirstToken(arguments));
			if (MesquiteDouble.isCombinable(s)){
				siteGappinessThreshold = s;
				if (!MesquiteThread.isScripting())
					parametersChanged(); 

			}
		}
		else if (checker.compare(this.getClass(), "Sets proportion of sites in span that are gappy to trigger selection.", "[on or off]", commandName, "setBlockGappinessThreshold")) {
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
 	boolean isGapBlockBoundary(int k) {
 		for (int i = 0; k+i<siteGappiness.length && i<minGappyBoundary; i++)
 			if (gappySite(k+i))
 				return false;
 		return true;
 	}
 	boolean gappySite(int k) {
 		return siteGappiness[k]>=siteGappinessThreshold;
 	}
 	
	public Bits flagSites(CharacterData data, Bits flags) {
		if (flags == null)
			flags = new Bits(data.getNumChars());
		else {
			if (flags.getSize()< data.getNumChars())
				flags.resetSize(data.getNumChars());
			flags.clearAllBits();
		}
		
		int numTaxa = data.getNumTaxa();
 		int numChars = data.getNumChars();
 		if (siteGappiness == null || siteGappiness.length != numChars) {
 			siteGappiness = new double[numChars];
 		}
 		for (int ic=0; ic<numChars; ic++) {
 			int gapCount = 0;
 			for (int it = 0; it<numTaxa; it++) {
 				if (data.isInapplicable(ic,it)) //if taxonSequenceStart < 0 then taxon has no data
 					gapCount++;
 			}
 			siteGappiness[ic] = 1.0*gapCount/numTaxa;
 			if (filterGapIndSite.getValue() && gappySite(ic))
 				flags.setBit(ic, true); 				
 		}

 		if (filterBlockGappiness.getValue())
 			for (int blockStart=0; blockStart<numChars; blockStart++) { //let's look for gappy blocks
 				if (gappySite(blockStart)) { // possible start of gappy block
 					
 					boolean boundaryFound = false;
 					for (int candidateNextBoundary = blockStart+1; candidateNextBoundary<numChars+1 && !boundaryFound; candidateNextBoundary++) {  // go ahead until next boundary reached
 						if (isGapBlockBoundary(candidateNextBoundary) || candidateNextBoundary == numChars-1) {
 							boundaryFound = true;
 							int blockEnd = candidateNextBoundary-1;

 							//blockStart is the potential start of a block; blockEnd is a possible end. If the block is long enough, ask if its blockGappiness is bad
 							if (blockEnd-blockStart+1 >= minGappyBlockSize){
 								//block is big enough, but is it bad enough?
 								int badSiteCount = 0;
 								for (int k = blockStart; k <= blockEnd; k++)
 									if (gappySite(k))  // stored as double[] in case criterion shifts, e.g., to average
 										badSiteCount++;
 								double blockGappiness = 1.0*badSiteCount/(blockEnd-blockStart+1);
 								if (blockGappiness >=blockGappinessThreshold)
 									for (int k = blockStart; k <= blockEnd; k++)
 										flags.setBit(k, true);


 							}

 						}
 					}
 				}
 			}

		return flags;

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


