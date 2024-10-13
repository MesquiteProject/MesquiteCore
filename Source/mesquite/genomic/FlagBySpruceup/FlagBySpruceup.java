/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.genomic.FlagBySpruceup;
/*~~  */



import java.awt.Button;

import java.awt.Checkbox;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import mesquite.categ.lib.CategoricalData;
import mesquite.categ.lib.CategoricalState;
import mesquite.categ.lib.RequiresAnyCategoricalData;
import mesquite.categ.lib.RequiresAnyMolecularData;
import mesquite.lib.CommandChecker;
import mesquite.lib.CommandRecord;
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
import mesquite.lib.MesquiteTimer;
import mesquite.lib.Snapshot;
import mesquite.lib.StringUtil;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.MatrixFlags;
import mesquite.lib.duties.MatrixFlagger;
import mesquite.lib.duties.MatrixFlaggerForTrimming;

/* ======================================================================== */
public class FlagBySpruceup extends MatrixFlaggerForTrimming implements ActionListener {

	/* This replicates the calculations of Spruceup v. 2024.07.22 */

	/* suggestions:
	 * -- Option to remove gaps only characters (default yet)
	 * -- Option to do it iteratively until nothing left
	 * */
	/* parameters =================================*/
	static double cutoffDEFAULT = 5.0; 
	static int windowSizeDEFAULT = 50;
	static int overlapDEFAULT = 25;
	static int numThreadsDEFAULT = 2;
	static boolean booleanOptionDEFAULT = false;
	int windowSize =windowSizeDEFAULT;
	int overlap = overlapDEFAULT;
	int numThreads = numThreadsDEFAULT;
	double cutoff = cutoffDEFAULT;
	boolean suspended = false;
	MesquiteBoolean flagGapsOnly = new MesquiteBoolean(true);
	MesquiteBoolean iterate = new MesquiteBoolean(false);

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		if (!MesquiteThread.isScripting()) {
			if (!queryOptions())
				return false;
		}
		else
			suspended = true; // to avoid doing the calculations twice, wait for snapshot signal to resume
		addMenuItem(null, "Spruceup options...", makeCommand("setOptions", this));

		return true;
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "cutoff", cutoff);  
		StringUtil.appendXMLTag(buffer, 2, "windowSize", windowSize);  
		StringUtil.appendXMLTag(buffer, 2, "overlap", overlap);  
		StringUtil.appendXMLTag(buffer, 2, "numThreads", numThreads);  
		StringUtil.appendXMLTag(buffer, 2, "flagGapsOnly", flagGapsOnly);  
		 StringUtil.appendXMLTag(buffer, 2, "iterate", iterate);  
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
		if ("numThreads".equalsIgnoreCase(tag))
			numThreads = MesquiteInteger.fromString(content);
		if ("flagGapsOnly".equalsIgnoreCase(tag))
			flagGapsOnly.setValue(MesquiteBoolean.fromTrueFalseString(content));
		if ("iterate".equalsIgnoreCase(tag))
			iterate.setValue(MesquiteBoolean.fromTrueFalseString(content));
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("cutoff " + cutoff);
		temp.addLine("windowSize " + windowSize);
		temp.addLine("overlap " + overlap);
		temp.addLine("numThreads " + numThreads);
		temp.addLine("flagGapsOnly " + flagGapsOnly.toOffOnString());
		temp.addLine("iterate " + iterate.toOffOnString());
		temp.addLine("resume");
		return temp;
	}


	DoubleField cutoffField;
	IntegerField wS;
	IntegerField ov;
	IntegerField nT;
	Checkbox iter, fGO;

	private boolean queryOptions() {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(),  "Options for Spruceup",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()

		cutoffField = dialog.addDoubleField("Cutoff", cutoff, 4);
		wS = dialog.addIntegerField("Window Size", windowSize, 4);
		ov = dialog.addIntegerField("Overlap", overlap, 4);
		nT = dialog.addIntegerField("Number of processing cores to use", numThreads, 4);

		dialog.addHorizontalLine(1);

		if (forTrimming()){
			dialog.addLabel("Mesquite modifications");
			fGO = dialog.addCheckBox("After Spruceup trimming, trim gaps-only characters", flagGapsOnly.getValue()); 
			iter = dialog.addCheckBox("Iterate until no more to trim", iterate.getValue());  //Show ONLY IF HIRED FOR TRIMMING
			dialog.addHorizontalLine(1);
		}
		/*

		 */
		dialog.addBlankLine();

		Button useDefaultsButton = null;
		useDefaultsButton = dialog.addAListenedButton("Set to Defaults", null, this);
		useDefaultsButton.setActionCommand("setToDefaults");
		dialog.addHorizontalLine(1);
		dialog.addLargeOrSmallTextLabel("If you use this, please cite it as the implementation of Spruceup (Boroweic 2019) in Mesquite. See (?) help button for details.");
		String s = "This is a partial implementation of Spruceup v.2024.07.22. In a citation, indicate that the settings are "
				+ "\"criterion:mean\" and \"distance_method:uncorrected\" [plus the settings you have chosen for cutoff, window size, overlap, etc.]."
				+ "\nFor a more complete implementation, use the original Spruceup."
				+ "<p><b>Reference for Spruceup</b>: Boroweic ML (2019) Spruceup: fast and flexible identification, visualization, and removal of outliers from large multiple sequence alignments."
				+ " Journal of Open Source Software 4:1635. <a href= \"https://doi.org/10.21105/joss.01635\">https://doi.org/10.21105/joss.01635</a>";
		dialog.appendToHelpString(s);
		dialog.addBlankLine();


		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			cutoff = cutoffField.getValue();
			windowSize = wS.getValue();
			overlap = ov.getValue();
			numThreads = nT.getValue();
			if (fGO != null)
				flagGapsOnly.setValue(fGO.getState());
			if (iter != null)
			iterate.setValue(iter.getState());
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
			if (fGO != null)
			fGO.setState(false);
			if (iter != null)
			iter.setState(false);
		} 
	}
	public void queryLocalOptions () {
		if (queryOptions())
			storePreferences();
	}
	/*.................................................................................................................*/
	public boolean pleaseIterate(){
		return iterate.getValue();
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets options for Spruceup criterion", "", commandName, "setOptions")) {
			if (queryOptions()) {
				storePreferences();
				parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Sets window size.", "[integer]", commandName, "windowSize")) {
			int s = MesquiteInteger.fromString(parser.getFirstToken(arguments));
			if (MesquiteInteger.isCombinable(s)){
				windowSize = s;
				if (!MesquiteThread.isScripting())
					parametersChanged(); 
			}

		}
		else if (checker.compare(this.getClass(), "Sets overlap.", "[integer]", commandName, "overlap")) {
			int s = MesquiteInteger.fromString(parser.getFirstToken(arguments));
			if (MesquiteInteger.isCombinable(s)){
				overlap = s;
				if (!MesquiteThread.isScripting())
					parametersChanged(); 
			}
		}
		else if (checker.compare(this.getClass(), "Sets number of processing cores to use.", "[integer]", commandName, "numThreads")) {
			int s = MesquiteInteger.fromString(parser.getFirstToken(arguments));
			if (MesquiteInteger.isCombinable(s)){
				numThreads = s;
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
		else if (checker.compare(this.getClass(), "Sets the option to flag gaps-only characters.", "[on or off]", commandName, "flagGapsOnly")) {
			boolean current = flagGapsOnly.getValue();
			flagGapsOnly.toggleValue(parser.getFirstToken(arguments));
			if (current!=flagGapsOnly.getValue()) {
				parametersChanged();
			}
		}
		else if (checker.compare(this.getClass(), "Sets whether to iterate.", "[on or off]", commandName, "iterate")) {
		boolean current = iterate.getValue();
		iterate.toggleValue(parser.getFirstToken(arguments));
		if (current!=iterate.getValue()) {
			parametersChanged();
		}
	}
	
		else if (checker.compare(this.getClass(), "Indicates calculations can be resumed.", "[]", commandName, "resume")) {
			suspended = false;
			parametersChanged(); 

		}

		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	boolean suspend = false;
	/*======================================================*/
	SpruceupThread[] threads;
	/*======================================================*/
	int numWindowsDone =0;
	int numThreadsDone =0;
	/*======================================================*/
	public MatrixFlags flagMatrix(CharacterData data, MatrixFlags flags) {
		if (data!=null && data.getNumChars()>0 && data instanceof CategoricalData){
			if (flags == null)
				flags = new MatrixFlags(data);
			else 
				flags.reset(data);

			if (!MesquiteThread.isScripting())
				suspended = false;
			if (suspended)
				return flags;
			MesquiteTimer timer = new MesquiteTimer();
			timer.start();

			int numTaxa = data.getNumTaxa();
			int numChars = data.getNumChars();
			int windowIncrement = windowSize-overlap;
			int numWindows = (int)((numChars-overlap)/windowIncrement);

			double[][] lonelinessInWindow = new double[numWindows][numTaxa];

			/*-----------------*/
			// making threads and getting them started
			numWindowsDone =0;
			threads = new SpruceupThread[numThreads];
			int blockSize = numWindows/numThreads + 1;
			if (blockSize == 0)
				blockSize = 1;
			for (int i = 0; i<numThreads; i++) {
				int firstWindow = i*blockSize;
				int lastWindow = firstWindow+blockSize-1;
				if (lastWindow > numWindows-1)
					lastWindow = numWindows-1;
				if (firstWindow*windowIncrement<numChars){
					threads[i] = new SpruceupThread(this, data, firstWindow, lastWindow, windowSize, windowIncrement, flags, lonelinessInWindow);
					threads[i].start();  
				}
			}
			// checking on all of the threads
			boolean allDone = false;
			while (!allDone) {
				try {
					Thread.sleep(20);
					allDone = true;
					for (int i= 0; i<numThreads;i++) {
						if (threads[i] != null && !threads[i].done)
							allDone = false;
					}

					if (numWindowsDone%10==0)
						CommandRecord.tick("Spruceup window " + numWindowsDone + " of " + numWindows);				
				}
				catch(Exception e) {
				}

			}
			CommandRecord.tick("Spruceup distance calculations complete. Number of windows done " + numWindowsDone + " of " + numWindows);
			/*-----------------*/

			//now calculate average loneliness over all windows
			double[] lonelinessOverall = new double[numTaxa];
			for (int it = 0; it<numTaxa; it++) {
				lonelinessOverall[it] = 0;
				int numWindowsCompared = 0;

				for (int window=0; window<numWindows; window++) {
					if (lonelinessInWindow[window][it] >= 0) {
						lonelinessOverall[it] += lonelinessInWindow[window][it];
						numWindowsCompared ++;
					}
				}
				if (numWindowsCompared>0)
					lonelinessOverall[it] = lonelinessOverall[it]/numWindowsCompared;
			}


			boolean saveResults = false;
			if (saveResults){
				String output = ("Taxon,Overall");
				for (int window=0; window<numWindows; window++) {
					int windowStart = window*windowIncrement;
					output += "," + windowStart;
				}
				output +="\n";
				MesquiteFile.putFileContents("mesquiteSpruceupLonelinesses.csv", output, true);
				for (int it = 0; it<numTaxa; it++) {
					output = data.getTaxa().getTaxonName(it);
					logln("Writing Spruceup details for " + output);
					output += "," + MesquiteDouble.toStringDigitsSpecified(lonelinessOverall[it], 9);
					for (int window=0; window<numWindows; window++)
						output += "," + MesquiteDouble.toStringDigitsSpecified(lonelinessInWindow[window][it], 9);
					output += "\n";
					MesquiteFile.appendFileContents("mesquiteSpruceupLonelinesses.csv", output, true);
				}
			}

			/*-----------------*/
			//Now look for outliers
			long count = 0;
			for (int window=0; window<numWindows; window++) {
				int windowStart = window*windowIncrement;
				int windowEnd = windowStart+windowSize-1;
				if (windowEnd < numChars){
					for (int it = 0; it<numTaxa; it++) {
						if (lonelinessInWindow[window][it] > cutoff * lonelinessOverall[it]) {
							flags.addCellFlag(it, windowStart, windowEnd);
							count+= windowSize;
						}
					}
				}
			}
			CommandRecord.tick("Spruceup complete. Number of windows done " + numWindowsDone + " of " + numWindows);
			timer.end();
			long time = timer.getAccumulatedTime();
			log("Spruceup found to trim " + count + " cells");
			if (flagGapsOnly.getValue()){
				int countGO = 0;
				boolean[][] cellFlags = flags.getCellFlags();
				for (int ic = 0; ic<data.getNumChars(); ic++){
					if (willBeGapsOnly(data, ic, cellFlags)){
						flags.setCharacterFlag(ic, true);
						countGO++;
					}
				}
				log(" and " + countGO + " characters");
			}
			logln(" in " + MesquiteTimer.getHoursMinutesSecondsFromMilliseconds(time));
		}

		return flags;

	}

	boolean willBeGapsOnly(CharacterData data, int ic, boolean[][] cellFlags){
		for (int it = 0; it<data.getNumTaxa(); it++){
			if (!data.isInapplicable(ic, it) && !cellFlags[ic][it])
				return false;
		}
		return true;
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
		return "Spruceup";
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
	public CompatibilityTest getCompatibilityTest(){
		return new RequiresAnyCategoricalData();
	}


}




class SpruceupThread extends MesquiteThread {
	FlagBySpruceup ownerModule;
	CharacterData data;
	int firstWindow;
	int lastWindow;
	int windowIncrement;
	int windowSize;
	MatrixFlags flags;
	double[][] lonelinessInWindow;
	boolean done = false;
	int myWindowsDone = 0;
	boolean scaleByNumCompared = true;  
	public SpruceupThread(FlagBySpruceup ownerModule, CharacterData data, int startWindow, int endWindow, int windowSize, int windowIncrement, MatrixFlags flags, double[][] lonelinessInWindow){
		this.data = data;
		this.ownerModule = ownerModule;
		this.firstWindow = startWindow;
		this.lastWindow = endWindow;
		this.windowSize = windowSize;
		this.windowIncrement = windowIncrement;
		this.flags = flags;
		this.lonelinessInWindow = lonelinessInWindow;
	}
	public void run() {
		int numTaxa = data.getTaxa().getNumTaxa();
		int numChars = data.getNumChars();
		CategoricalState sJ = new CategoricalState();
		CategoricalState sK = new CategoricalState();
		int[][] numDiffs = new int[numTaxa][numTaxa];
		int[][] numSitesCompared = new int[numTaxa][numTaxa];
		int[] numOthersCompared = new int[numTaxa];
		boolean[] hasDataInWindow = new boolean[numTaxa];
		scaleByNumCompared = true;

		//WITHIN A WINDOW
		for (int window=firstWindow; window<=lastWindow; window++) {
			int windowStart = window*windowIncrement;
			int windowEnd = windowStart+windowSize-1;


			//Spruceup doesn't do last partial window
			for (int j=0; j<numTaxa; j++) {
				numOthersCompared[j] = 0;
				hasDataInWindow[j]=false;
				for (int k=0; k<numTaxa; k++) {
					numDiffs[j][k] = 0;
					numSitesCompared[j][k] = 0;
				}
			}
			for (int j = 0; j<numTaxa; j++)
				for (int ic=windowStart; ic<=windowEnd; ic++) { //for all characters in the window

					sJ = (CategoricalState)data.getCharacterState(sJ, ic, j);
					if (!sJ.isInapplicable())
						hasDataInWindow[j] = true;
				}
			//Build distance matrix
			for (int j = 0; j<numTaxa; j++) {
				for (int k=j+1; k<numTaxa; k++) { 
					int numColumnsCompared = 0;
					int numColumnsComparedJ = 0;
					int numColumnsComparedK = 0;
					for (int ic=windowStart; ic<=windowEnd; ic++) { //for all characters in the window
						//get state of both chars
						sJ = (CategoricalState)data.getCharacterState(sJ, ic, j);
						sK = (CategoricalState)data.getCharacterState(sK, ic, k);
						boolean sJHasState = !sJ.isInapplicable();
						boolean sKHasState = !sK.isInapplicable();
						if (sJHasState)
							numColumnsComparedJ++;
						if (sKHasState)
							numColumnsComparedK++;
						if (sJHasState && sKHasState){
							numColumnsCompared++;
							if (!sJ.statesShared(sK)){
								numDiffs[j][k]++;
								numDiffs[k][j]++;
							}
						}

					}
					if (numColumnsComparedJ>0 || numColumnsComparedK>0) {
						numSitesCompared[j][k] += numColumnsComparedJ;
						numSitesCompared[k][j] += numColumnsComparedK;
						if (numColumnsCompared>0)
							numOthersCompared[j]++;
						if (numColumnsCompared>0)
							numOthersCompared[k]++;
					}

				}
			}

			//Calculate loneliness of taxon
			for (int j = 0; j<numTaxa; j++) {
				lonelinessInWindow[window][j] = 0;

				//first get sums of distances to j
				for (int k=0; k<numTaxa; k++)
					if (numSitesCompared[j][k]!=0) 
						lonelinessInWindow[window][j] += numDiffs[j][k]*1.0/numSitesCompared[j][k]; 

				//then divide by number of comparisons
				if (numOthersCompared[j] > 0)
					lonelinessInWindow[window][j] = lonelinessInWindow[window][j]/numOthersCompared[j]; //average distance to all other taxa
				else
					lonelinessInWindow[window][j] = -1;

			}

			ownerModule.numWindowsDone++;
			myWindowsDone++;
		}

		done = true;
	}

}

