/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.FlagBySpruceup;
/*~~  */



import java.awt.Button;
import java.awt.Checkbox;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import mesquite.categ.lib.CategoricalData;
import mesquite.categ.lib.CategoricalState;
import mesquite.lib.Bits;
import mesquite.lib.CommandChecker;
import mesquite.lib.Debugg;
import mesquite.lib.DoubleArray;
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
import mesquite.molec.lib.MatrixFlags;
import mesquite.molec.lib.MatrixFlagger;

/* ======================================================================== */
public class FlagBySpruceup extends MatrixFlagger implements ActionListener {

	/** ToDo:
	 * Ignore taxa with too little data, or at least warn?
	 * make dialog more informative, and add citation
	 */
	
	/* parameters =================================*/
	static double cutoffDEFAULT = 5.0; 
	static int windowSizeDEFAULT = 50;
	static int overlapDEFAULT = 25;

	int windowSize =windowSizeDEFAULT;
	int overlap = overlapDEFAULT;
	double cutoff = cutoffDEFAULT;

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		if (!MesquiteThread.isScripting()) {
			if (!queryOptions())
				return false;
		}
		addMenuItem(null, "Spruceup options...", makeCommand("setOptions", this));
		return true;
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "cutoff", cutoff);  
		StringUtil.appendXMLTag(buffer, 2, "windowSize", windowSize);  
		StringUtil.appendXMLTag(buffer, 2, "overlap", overlap);  
		return buffer.toString();
	}
	public void processSingleXMLPreference (String tag, String flavor, String content){
		processSingleXMLPreference(tag, null, content);
	}

	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("cutoff".equalsIgnoreCase(tag))
			cutoff = MesquiteDouble.fromString(content);
		if ("windowSize".equalsIgnoreCase(tag))
			windowSize = MesquiteInteger.fromString(content);
		if ("overlap".equalsIgnoreCase(tag))
			overlap = MesquiteInteger.fromString(content);
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("cutoff " + cutoff);
		temp.addLine("windowSize " + windowSize);
		temp.addLine("overlap " + overlap);
		return temp;
	}


	DoubleField cutoffField;
	IntegerField wS;
	IntegerField ov;

	private boolean queryOptions() {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(),  "Options for Spruceup",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
		dialog.addLargeOrSmallTextLabel("This is not a fill Spruceup implementation. It uses only the options \"criterion:mean\" and \"distance_method:uncorrected\".");

		cutoffField = dialog.addDoubleField("Cutoff (e.g. " + cutoffDEFAULT+ ")", cutoff, 4);
		wS = dialog.addIntegerField("Window Size (e.g. " + windowSizeDEFAULT + ")", windowSize, 4);
		ov = dialog.addIntegerField("Overlap (e.g. " + overlapDEFAULT+ ")", overlap, 4);

		dialog.addHorizontalLine(1);
		dialog.addBlankLine();
		Button useDefaultsButton = null;
		useDefaultsButton = dialog.addAListenedButton("Set to Defaults", null, this);
		useDefaultsButton.setActionCommand("setToDefaults");
		dialog.addHorizontalLine(1);
		dialog.addBlankLine();


		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			cutoff = cutoffField.getValue();
			windowSize = wS.getValue();
			overlap = ov.getValue();

			storePreferences();
		}
		dialog.dispose();
		return (buttonPressed.getValue()==0);
	}

	/*.................................................................................................................*/
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equalsIgnoreCase("setToDefaults")) {
			cutoffField.setValue(cutoffDEFAULT);
			wS.setValue(windowSizeDEFAULT);
			ov.setValue(overlapDEFAULT);

		} 
	}
	public void queryOptionsOtherThanEmployees () {
		if (queryOptions())
			storePreferences();
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets options for Spruceup criterion", "", commandName, "setOptions")) {
			if (queryOptions()) {
				storePreferences();
				parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Sets window size.", "", commandName, "windowSize")) {
			int s = MesquiteInteger.fromString(parser.getFirstToken(arguments));
			if (MesquiteInteger.isCombinable(s)){
				windowSize = s;
				if (!MesquiteThread.isScripting())
					parametersChanged(); 
			}

		}
		else if (checker.compare(this.getClass(), "Sets overlap.", "", commandName, "overlap")) {
			int s = MesquiteInteger.fromString(parser.getFirstToken(arguments));
			if (MesquiteInteger.isCombinable(s)){
				overlap = s;
				if (!MesquiteThread.isScripting())
					parametersChanged(); 
			}

		}
		else if (checker.compare(this.getClass(), "Sets cutoff factor.", "[number]", commandName, "cutoff")) {
			double s = MesquiteDouble.fromString(parser.getFirstToken(arguments));
			if (MesquiteDouble.isCombinable(s)){
				cutoff = s;
				if (!MesquiteThread.isScripting())
					parametersChanged(); 

			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/*======================================================*/
	/*======================================================*/
	/*======================================================*/
	public MatrixFlags flagMatrix(CharacterData data, MatrixFlags flags) {
		if (data!=null && data.getNumChars()>0 && data instanceof CategoricalData){
			if (flags == null)
				flags = new MatrixFlags(data);
			else 
				flags.reset(data);


			int numTaxa = data.getNumTaxa();
			int numChars = data.getNumChars();
			int windowIncrement = windowSize-overlap;
			int numWindows = (int)(numChars/windowIncrement);
			if (numChars % windowIncrement !=0)
				numWindows++;
			CategoricalState s1 = new CategoricalState();
			CategoricalState s2 = new CategoricalState();

			double[][] lonelinessInWindow = new double[numWindows][numTaxa];
			for (int window=0; window<numWindows; window++) {
				int windowStart = window*windowIncrement;
				int windowEnd = windowStart+windowSize;
				if (windowEnd>= numChars)
					windowEnd = numChars;
				for (int it1 = 0; it1<numTaxa; it1++) {
					lonelinessInWindow[window][it1] = 0;
					int numTaxaCompared = 0;
					for (int it2=0; it2<numTaxa; it2++) { //for all taxon pairs. Doing both triangles because would have to cycle twice to average over taxa anyway
						if (it1!=it2) {
							int numDiffs = 0;
							int numNucleotideComparisons = 0;
							for (int ic=windowStart; ic<windowEnd; ic++) { //for all characters in the window
								//get state of both chars
								if (ic<numChars) {
									s1 = (CategoricalState)data.getCharacterState(s1, ic, it1);
									s2 = (CategoricalState)data.getCharacterState(s2, ic, it2);
									if (!s1.isInapplicable() && !s2.isInapplicable()) {
										numNucleotideComparisons++;
										if (!s1.statesShared(s2)) {
											numDiffs++;
										}
									}
								}
							}
							if (numNucleotideComparisons>0)
								numTaxaCompared++;
							lonelinessInWindow[window][it1] += numDiffs;
						}
					}
					if (numTaxaCompared != 0)
						lonelinessInWindow[window][it1] = lonelinessInWindow[window][it1]/numTaxaCompared; //average distance to all other taxa
				}
			}

			//now calculate average loneliness over all windows
			double[] lonelinessOverall = new double[numTaxa];
			for (int it = 0; it<numTaxa; it++) {
				lonelinessOverall[it] = 0;
				for (int window=0; window<numWindows; window++)
					lonelinessOverall[it] += lonelinessInWindow[window][it];
				lonelinessOverall[it] = lonelinessOverall[it]/numWindows;
			}
			if (false) {
				int highlight = 16;
				Debugg.println("lonelinessOverall " + DoubleArray.toString( lonelinessOverall));
				Debugg.println("l&&&&&&&&&&&&&&& ");
				Debugg.println("lonelinessOverall[" +highlight + "] " +  lonelinessOverall[highlight]);
				Debugg.println("lonelinessInWindow " );
				for (int window=0; window<numWindows; window++) {
					int windowStart = window*windowIncrement;
					Debugg.println("  " + windowStart + " --- " + lonelinessInWindow[window][highlight]);
				}
			}
			//Now look for outliers
			for (int window=0; window<numWindows; window++) {
				int windowStart = window*windowIncrement;
				int windowEnd = windowStart+windowSize;
				if (windowEnd>= numChars)
					windowEnd = numChars;
				for (int it = 0; it<numTaxa; it++) {
					if (lonelinessInWindow[window][it]>cutoff*lonelinessOverall[it]) {
						flags.addCellFlag(it, windowStart, windowEnd);
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
		return "Spruceup Criterion";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Flags sites or regions of sites by Spruceup criterion (Mean/Uncorrected)." ;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}


}


