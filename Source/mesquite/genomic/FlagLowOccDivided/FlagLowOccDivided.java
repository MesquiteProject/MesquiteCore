/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.genomic.FlagLowOccDivided;
/*~~  */



import java.awt.Checkbox;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import mesquite.categ.lib.CategoricalData;
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
import mesquite.lib.Notification;
import mesquite.lib.Snapshot;
import mesquite.lib.StringUtil;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.MatrixFlags;
import mesquite.lib.duties.MatrixFlaggerForTrimmingSites;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.ui.DoubleField;
import mesquite.lib.ui.ExtensibleDialog;

/* ======================================================================== */
public class FlagLowOccDivided extends MatrixFlaggerForTrimmingSites implements ItemListener {

	/*TO DO:
	Explain that enter 0 to permit any gappiness
	 */
	/*Gappiness assessment parameters =================================*/
	static double siteOccupancyThresholdDEFAULT = 0.5; // A site is considered good (for gappiness) if it has at least this many non-gaps
	static boolean ignoreDatalessDEFAULT = false;

	boolean ignoreDataless = ignoreDatalessDEFAULT;

	// A site is considered good (for gappiness) if it is less or as gappy than these values among selected or unselected taxa.
	double siteOccupancyThresholdSEL = siteOccupancyThresholdDEFAULT; 	
	double siteOccupancyThresholdUNSEL = siteOccupancyThresholdDEFAULT; 


	Taxa taxa;
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
		addMenuItem(null, "Set Options for Site Gappiness among Selected & Unselected Taxa...", makeCommand("setOptions", this));
		return true;
	}
	public void endJob(){
		if (taxa != null)
			taxa.removeListener(this);
		super.endJob();
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "ignoreDataless", ignoreDataless);  
		StringUtil.appendXMLTag(buffer, 2, "siteOccupancyThresholdSEL", siteOccupancyThresholdSEL);  
		StringUtil.appendXMLTag(buffer, 2, "siteOccupancyThresholdUNSEL", siteOccupancyThresholdUNSEL);  
		return buffer.toString();
	}
	public void processSingleXMLPreference (String tag, String flavor, String content){
		processSingleXMLPreference(tag, null, content);
	}

	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("siteOccupancyThresholdSEL".equalsIgnoreCase(tag))
			siteOccupancyThresholdSEL = MesquiteDouble.fromString(content);
		if ("siteOccupancyThresholdUNSEL".equalsIgnoreCase(tag))
			siteOccupancyThresholdUNSEL = MesquiteDouble.fromString(content);
		if ("ignoreDataless".equalsIgnoreCase(tag))
			ignoreDataless=  MesquiteBoolean.fromTrueFalseString(content);
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		if (!getProject().isProcessDataFilesProject)
			temp.addLine("ignoreDataless " + ignoreDataless);
		temp.addLine("siteOccupancyThresholdSEL " + siteOccupancyThresholdSEL);
		temp.addLine("siteOccupancyThresholdUNSEL " + siteOccupancyThresholdUNSEL);
		return temp;
	}


	DoubleField pgSFieldSEL, pgSFieldUNSEL;
	IntegerField sNT;
	Checkbox specifNumCB, ignoreDatalessCB;
	private boolean queryOptions() {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(),  "Sites Gappy among Both Selected & Unselected Taxa",buttonPressed);  

		dialog.addLabel("Minimum occupancy (proportion of non-gaps, i.e. observed states):");
		pgSFieldSEL = dialog.addDoubleField("... among selected taxa:", siteOccupancyThresholdSEL, 4);
		pgSFieldUNSEL = dialog.addDoubleField("... among unselected taxa):", siteOccupancyThresholdUNSEL, 4);
		dialog.addLargeOrSmallTextLabel("To be flagged, a site has to be judged too gappy within selected taxa and also too gappy within unselected taxa."
				+" A site is considered too gappy among a set of taxa if it has fewer observed states (non-gaps)"
				+" than the proportion specified."
				+"\n\nEnter a number above 1 for selected to make the choice depend only on unselected taxa. "
				+"Enter a number above 1 for unselected to make the choice depend only on selected taxa.");
		String s = "<b>Filter of low occupancy sites</b> selects sites with high levels of gaps."
				+ " It is not intended to identify sites that are unreliable or poorly aligned; it is intended simply to find sites"
				+ " where the amount of available data is too sparse to justify inclusion, just as one filters loci for occupancy.<hr>" 
				+"The choice of how to count taxa is relevant especially for data with multiple loci. A locus might have data for only some taxa."
				+ " This could result in different countings of the proportion of gaps depending on whether the trimming is done on individual files for each locus (because each file will know only"
				+" about the number of taxa that have data for that locus) versus in the compiled file (which will know about all of the taxa).";

		if (!getProject().isProcessDataFilesProject){
			ignoreDatalessCB = dialog.addCheckBox("Ignore gaps in taxa with no data in matrix", ignoreDataless);
			s += "<p>To mimic the results you would obtain were you to process the loci individually in separate files (e.g. in a scripted pipeline), choose \"Ignore gaps in taxa with no data in matrix\"." 
					+ " This will result in a more permissive trimming."
					+ "<p><b>Recommendation</b>: if you want to treat this trimming as a site-level occupancy criterion (just like filtering loci for occupancy) then DON'T select \"Ignore\".";
		}
		dialog.appendToHelpString(s);
		dialog.addBlankLine();


		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			siteOccupancyThresholdSEL = pgSFieldSEL.getValue();
			siteOccupancyThresholdUNSEL = pgSFieldUNSEL.getValue();
			if (ignoreDatalessCB!=null){
				ignoreDataless = ignoreDatalessCB.getState();
			}


			storePreferences();
		}
		dialog.dispose();
		return (buttonPressed.getValue()==0);
	}

	public void itemStateChanged(ItemEvent e) {
		if (sNT != null)
			sNT.setEnabled(specifNumCB.getState());

	}
	public void queryLocalOptions () {
		if (queryOptions())
			storePreferences();
	}
	public void changed(Object caller, Object obj, Notification notification){
		if (Notification.appearsCosmetic(notification))
			return;
		if (obj == taxa)
			parametersChanged();
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets options for gappiness", "", commandName, "setOptions")) {
			if (queryOptions()) {
				storePreferences();
				parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Sets whether to count taxa with no data.", "[true or false]", commandName, "ignoreDataless")) {
			boolean s = MesquiteBoolean.fromTrueFalseString(parser.getFirstToken(arguments));
			ignoreDataless = s;
			if (!MesquiteThread.isScripting())
				parametersChanged(); 

		}

		else if (checker.compare(this.getClass(), "Sets proportion of selected taxa with gaps above which site is considered gappy.", "[proportion]", commandName, "siteOccupancyThresholdSEL")) {
			double s = MesquiteDouble.fromString(parser.getFirstToken(arguments));
			if (MesquiteDouble.isCombinable(s)){
				siteOccupancyThresholdSEL = s;
				if (!MesquiteThread.isScripting())
					parametersChanged(); 

			}
		}
		else if (checker.compare(this.getClass(), "Sets proportion of unselected taxa with gaps above which site is considered gappy.", "[proportion]", commandName, "siteOccupancyThresholdUNSEL")) {
			double s = MesquiteDouble.fromString(parser.getFirstToken(arguments));
			if (MesquiteDouble.isCombinable(s)){
				siteOccupancyThresholdUNSEL = s;
				if (!MesquiteThread.isScripting())
					parametersChanged(); 

			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/*======================================================*/
	double[] siteGappinessSEL;
	double[] siteGappinessUNSEL;
	boolean[] taxonHasData;
	int numTaxaCountedSEL = 0;
	int numTaxaCountedUNSEL = 0;
	/*======================================================*/


	boolean gappySiteSEL(int k) {
		return siteGappinessSEL[k]> (1-siteOccupancyThresholdSEL);
	}
	boolean gappySiteUNSEL(int k) {
		return siteGappinessUNSEL[k]> (1-siteOccupancyThresholdUNSEL);
	}

	boolean ignoreDatalessTaxa(){
		return ignoreDataless && !getProject().isProcessDataFilesProject; //this option available only for stable project open to user
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
			Taxa dtaxa = data.getTaxa();
			if (taxa != dtaxa){
				if (taxa != null)
					taxa.removeListener(this);
				taxa = dtaxa;
				taxa.addListener(this);
			}

			numTaxaCountedSEL = 0;
			numTaxaCountedUNSEL = 0;
			if (ignoreDatalessTaxa()) {  //count how many taxa are all gaps
				if (taxonHasData == null || taxonHasData.length!= numTaxa)
					taxonHasData = new boolean[numTaxa];
				for (int it=0; it<numTaxa; it++){
					taxonHasData[it] = false;
					for (int ic=0; ic<numChars; ic++)
						if (!data.isInapplicable(ic,it)){
							taxonHasData[it] = true;
							break;
						}
					if (taxonHasData[it]){
						if (data.getTaxa().isSelected(it))
							numTaxaCountedSEL++;
						else

							numTaxaCountedUNSEL++;
					}
				}
			}
			else {
				for (int it=0; it<numTaxa; it++){
					if (data.getTaxa().isSelected(it))
						numTaxaCountedSEL++;
					else

						numTaxaCountedUNSEL++;
				}

			}

			if (siteGappinessSEL == null || siteGappinessSEL.length != numChars) {
				siteGappinessSEL = new double[numChars];
				siteGappinessUNSEL = new double[numChars];
			}
			for (int ic=0; ic<numChars; ic++) {
				int gapCountSEL = 0;
				int gapCountUNSEL = 0;
				siteGappinessSEL[ic] = 0;
				siteGappinessUNSEL[ic] = 0;
				boolean dataInSEL = false;
				boolean dataInUNSEL = false;
				for (int it = 0; it<numTaxa; it++) {
					if (countTaxon(it)){
						if (data.isInapplicable(ic,it)) {
							if (data.getTaxa().isSelected(it))
								gapCountSEL++;
							else
								gapCountUNSEL++;
						}
						else {
							if (data.getTaxa().isSelected(it))
								dataInSEL = true;
							else
								dataInUNSEL = true;
						}
					}
				}
				if (numTaxaCountedSEL == 0)
					siteGappinessSEL[ic] = 0;
				else
					siteGappinessSEL[ic] = 1.0*gapCountSEL/numTaxaCountedSEL;
				if (numTaxaCountedUNSEL == 0)
					siteGappinessUNSEL[ic] = 0;
				else
					siteGappinessUNSEL[ic] = 1.0*gapCountUNSEL/numTaxaCountedUNSEL;

				if (numTaxaCountedSEL == 0){ //effectively none selected, therefore depend only on gappySiteUNSEL
					if (gappySiteUNSEL(ic))
						charFlags.setBit(ic, true);
				}
				else if (numTaxaCountedUNSEL == 0){//effectively none selected, therefore depend only on gappySiteSEL
					if (gappySiteSEL(ic))
						charFlags.setBit(ic, true);
						
				}
				else if (!dataInUNSEL && gappySiteSEL(ic))
					charFlags.setBit(ic, true);
				else if (!dataInSEL && gappySiteUNSEL(ic))
					charFlags.setBit(ic, true);
				else if (gappySiteSEL(ic) && gappySiteUNSEL(ic))
					charFlags.setBit(ic, true); 				
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
		return false;
	}

	/*.................................................................................................................*/
	public boolean showCitation(){
		return false;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Sites Gappy among Both Selected & Unselected Taxa";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Flags sites whose proportion of gaps is above a threshold, i.e. sites with low occupancy, among both selected and unselected taxa." ;
	} 
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 403;  
	}


}


