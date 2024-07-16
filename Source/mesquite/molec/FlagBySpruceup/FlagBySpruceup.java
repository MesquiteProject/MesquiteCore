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
import mesquite.lib.CommandChecker;
import mesquite.lib.CommandRecord;
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

/* ======================================================================== */
public class FlagBySpruceup extends MatrixFlagger implements ActionListener {

	/** ToDo:
	 * -- scale distances by num comparisons in window
	 * -- multithread
	 */

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
	//	MesquiteBoolean booleanOption = new MesquiteBoolean(booleanOptionDEFAULT);

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
		StringUtil.appendXMLTag(buffer, 2, "numThreads", numThreads);  
		//		StringUtil.appendXMLTag(buffer, 2, "booleanOption", booleanOption);  
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
		//		if ("booleanOption".equalsIgnoreCase(tag))
		//			booleanOption.setValue(MesquiteBoolean.fromTrueFalseString(content));
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("cutoff " + cutoff);
		temp.addLine("windowSize " + windowSize);
		temp.addLine("overlap " + overlap);
		temp.addLine("numThreads " + numThreads);
		//		temp.addLine("booleanOption " + booleanOption.toOffOnString());
		return temp;
	}


	DoubleField cutoffField;
	IntegerField wS;
	IntegerField ov;
	IntegerField nT;
	//Checkbox cFSL;

	private boolean queryOptions() {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(),  "Options for Spruceup",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()

		cutoffField = dialog.addDoubleField("Cutoff", cutoff, 4);
		wS = dialog.addIntegerField("Window Size", windowSize, 4);
		ov = dialog.addIntegerField("Overlap", overlap, 4);
		nT = dialog.addIntegerField("Number of processing cores to use", numThreads, 4);

		dialog.addHorizontalLine(1);
		//		dialog.addLabel("Mesquite modifications");
		//		cFSL = dialog.addCheckBox("XXXXXX", booleanOption.getValue());

		//		dialog.addHorizontalLine(1);
		dialog.addBlankLine();

		Button useDefaultsButton = null;
		useDefaultsButton = dialog.addAListenedButton("Set to Defaults", null, this);
		useDefaultsButton.setActionCommand("setToDefaults");
		dialog.addHorizontalLine(1);
		dialog.addLargeOrSmallTextLabel("If you use this, please cite it as the implementation of Spruceup (Boroweic 2018) in Mesquite. See (?) help button for details.");
		String s = "This is a limited implementation of Spruceup. In a citation, indicate that the settings are "
				+ "\"criterion:mean\" and \"distance_method:uncorrected\" [plus the settings you have chosen for cutoff, window size, overlap, etc.]."
				+ "\nFor a more complete implementation, use the original Spruceup."
				+ "\n\nReference: Boroweic ML (2018) Spruceup: fast and flexible identification, visualization, and removal of outliers from large multiple sequence alignments."
				+ " Journal of Open Source Software 4:1635. https://doi.org/10.21105/joss.01635";
		dialog.appendToHelpString(s);
		dialog.addBlankLine();


		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			cutoff = cutoffField.getValue();
			windowSize = wS.getValue();
			overlap = ov.getValue();
			numThreads = nT.getValue();
			//			booleanOption.setValue(cFSL.getState());

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
			//			cFSL.setState(booleanOptionDEFAULT);

		} 
	}
	public void queryOptionsOtherThanEmployees () {
		if (queryOptions())
			storePreferences();
	}
	
	//NOTE: Debugg.println: This doesn't recover correct flagging on restoring saved file
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
		else if (checker.compare(this.getClass(), "Sets number of processing cores to tuse.", "[integer]", commandName, "numThreads")) {
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
		/*	else if (checker.compare(this.getClass(), "Sets xxxxx.", "[on or off]", commandName, "booleanOption")) {
			boolean current = booleanOption.getValue();
			booleanOption.toggleValue(parser.getFirstToken(arguments));
			if (current!=booleanOption.getValue()) {
				parametersChanged();
			}
		}
		 */
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	boolean suspend = false;
	/*======================================================*/
	SpruceupThread[] threads;
	/*======================================================*/
	int numWindowsDone =0;
	/*======================================================*/
	public MatrixFlags flagMatrix(CharacterData data, MatrixFlags flags) {
		if (data!=null && data.getNumChars()>0 && data instanceof CategoricalData){
			if (flags == null)
				flags = new MatrixFlags(data);
			else 
				flags.reset(data);

			MesquiteTimer timer = new MesquiteTimer();
			timer.start();
	//		Debugg.println("Spruceup calculations: windowsize " + windowSize + " overlap " + overlap + " cutoff " + cutoff + " cores " + numThreads);

			int numTaxa = data.getNumTaxa();
			int numChars = data.getNumChars();
			int windowIncrement = windowSize-overlap;
			int numWindows = (int)(numChars/windowIncrement);
			if (numChars % windowIncrement !=0)
				numWindows++;
			threads = new SpruceupThread[numThreads];
			/*-----------------*/
			double[][] lonelinessInWindow = new double[numWindows][numTaxa];
			numWindowsDone =0;
	//		Debugg.println("numWindows " + numWindows);

			//int firstWindow = 0; //if done all at once
			//int lastWindow = numWindows-1;//if done all at once
			int blockSize = numWindows/numThreads + 1;
	//		Debugg.println("blockSize " + blockSize);
			if (blockSize == 0)
				blockSize = 1;
			for (int i = 0; i<numThreads; i++) {
				int firstWindow = i*blockSize;
				int lastWindow = firstWindow+blockSize-1;
				if (lastWindow > numWindows-1)
					lastWindow = numWindows-1;
				if (firstWindow*windowIncrement<numChars){
					threads[i] = new SpruceupThread(this, data, firstWindow, lastWindow, windowSize, windowIncrement, flags, lonelinessInWindow);
					threads[i] .start();  
				}
			}

			/*-----------------*/
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
						CommandRecord.tick("Spruceup window " + numWindowsDone + " of " + lonelinessInWindow.length);				
				}
				catch(Exception e) {
				}

			}
		//	Debugg.println("Spruceup complete. Number of windows done " + numWindowsDone + " of " + numWindows);
			//now calculate average loneliness over all windows
			double[] lonelinessOverall = new double[numTaxa];
			for (int it = 0; it<numTaxa; it++) {
				lonelinessOverall[it] = 0;
				int numWindowsCompared = 0;
				
				for (int window=0; window<numWindows; window++) {
				//	if (it == 3)
				//		Debugg.println("lonelinessInWindow for taxon 3, window " + window + " loneliness " + lonelinessInWindow[window][3]);
					if (lonelinessInWindow[window][it] >= 0) {
						lonelinessOverall[it] += lonelinessInWindow[window][it];
						numWindowsCompared ++;
					}
				}
				if (numWindowsCompared>0)
					lonelinessOverall[it] = lonelinessOverall[it]/numWindowsCompared;
			}
		//	for (int it = 0; it<numTaxa; it++)
		//		Debugg.println(" taxon " + it + " In first window " + lonelinessInWindow[0][it]+ " overall " + lonelinessOverall[it]);

			//Now look for outliers
			for (int window=0; window<numWindows; window++) {
				int windowStart = window*windowIncrement;
				int windowEnd = windowStart+windowSize-1;
				if (windowEnd>= numChars)
					windowEnd = numChars-1;
				for (int it = 0; it<numTaxa; it++) {
					if (lonelinessInWindow[window][it]>cutoff*lonelinessOverall[it]) {
						flags.addCellFlag(it, windowStart, windowEnd);
					}
				}
			}
			timer.end();
			long time = timer.getAccumulatedTime();
			if (time>1000){
				logln("Spruceup calculation took " + MesquiteTimer.getHoursMinutesSecondsFromMilliseconds(time));
			}
		}
		/* this reports on flagging*/
		if (false){
			boolean[][] cells = flags.getCellFlags();
			for (int it = 0; it<data.getNumTaxa(); it++){
				Debugg.println(data.getTaxa().getTaxonName(it));
				int removed = 0;
				int startOfStretch = -1;
				int endOfStretch = -1;
				for (int ic=0; ic<data.getNumChars(); ic++){
					if (cells[ic][it]){
						if (startOfStretch == -1)
							startOfStretch = ic;
						endOfStretch = ic;
						removed++;
					}
					else {
						if (startOfStretch>=0){
							Debugg.print(" " + startOfStretch + "-" + endOfStretch);
							startOfStretch = -1;
							endOfStretch = ic;
						}
					}
				}
				Debugg.println("\nRemoved " + removed + "\n");
			}
		}
		/* */
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
		CategoricalState s1 = new CategoricalState();
		CategoricalState s2 = new CategoricalState();
		int[][] numDiffs = new int[numTaxa][numTaxa];
		int[][] numSitesCompared = new int[numTaxa][numTaxa];
		int[] numOthersCompared = new int[numTaxa];

		//WITHIN A WINDOW
		for (int window=firstWindow; window<=lastWindow; window++) {
			int windowStart = window*windowIncrement;
			int windowEnd = windowStart+windowSize-1;
		//	if (window == 0) {
		//		Debugg.println("window " + window + " windowStart " + windowStart + " windowEnd " + windowEnd);
		//	}
			if (windowEnd>= numChars)
				windowEnd = numChars-1;

			for (int j=0; j<numTaxa; j++) {
				numOthersCompared[j] = 0;
				for (int k=0; k<numTaxa; k++) {
					numDiffs[j][k] = 0;
					numSitesCompared[j][k] = 0;
				}
			}
			//Build distance matrix
			for (int j = 0; j<numTaxa; j++) {
				for (int k=j+1; k<numTaxa; k++) { 
					int numNucleotideComparisons = 0;
					for (int ic=windowStart; ic<=windowEnd; ic++) { //for all characters in the window
						//get state of both chars
						s1 = (CategoricalState)data.getCharacterState(s1, ic, j);
						s2 = (CategoricalState)data.getCharacterState(s2, ic, k);
						if (!s1.isInapplicable() && !s2.isInapplicable()) {
							numNucleotideComparisons++;
							if (!s1.statesShared(s2)) {
								numDiffs[j][k]++;
								numDiffs[k][j]++;
							}
						}
					}
					if (numNucleotideComparisons>0) {
						numSitesCompared[j][k] += numNucleotideComparisons;
						numSitesCompared[k][j] += numNucleotideComparisons;
						numOthersCompared[j]++;
						numOthersCompared[k]++;
					}

				}
			}

			for (int j = 0; j<numTaxa; j++) {
				lonelinessInWindow[window][j] = 0;
				//first get sums of distances to j
				if (scaleByNumCompared) { //this isn't working right
					for (int k=0; k<numTaxa; k++){
						if (numSitesCompared[j][k]!=0)
							lonelinessInWindow[window][j] += numDiffs[j][k]*1.0/numSitesCompared[j][k];
					
					//	if (window == 0 && j == 3) {
					//		Debugg.println(" j " + j + " k " + k + " === numDiffs[j][k]] " + numDiffs[j][k] + " numSitesCompared[j][k] " + numSitesCompared[j][k] + " prop " + numDiffs[j][k]*1.0/numSitesCompared[j][k]);
					//	}
					}/*
					int sumDiffs =0;
					int sumSitesCompared =0;
					for (int k=0; k<numTaxa; k++){
						sumDiffs += numDiffs[j][k];
						sumSitesCompared += numSitesCompared[j][k];
					}
					if (sumSitesCompared!=0)
						lonelinessInWindow[window][j] += sumDiffs*1.0/sumSitesCompared;
	*/
				}
				else {
					for (int k=0; k<numTaxa; k++) 
						lonelinessInWindow[window][j] += numDiffs[j][k];
				}
				//then divide by number of comparisons
				if (numOthersCompared[j] != 0)
					lonelinessInWindow[window][j] = lonelinessInWindow[window][j]/numOthersCompared[j]; //average distance to all other taxa
				else
					lonelinessInWindow[window][j] = -1;
			}

			ownerModule.numWindowsDone++;
		}
		done = true;
	}
	/*
public void run() {
		int numTaxa = data.getTaxa().getNumTaxa();
		int numChars = data.getNumChars();
		CategoricalState s1 = new CategoricalState();
		CategoricalState s2 = new CategoricalState();
		for (int window=firstWindow; window<=lastWindow; window++) {
			int windowStart = window*windowIncrement;
			int windowEnd = windowStart+windowSize;
			if (windowEnd>= numChars)
				windowEnd = numChars-1;
			for (int it1 = 0; it1<numTaxa; it1++) {
				lonelinessInWindow[window][it1] = 0;
				int numOthersCompared = 0;
				for (int it2=0; it2<numTaxa; it2++) { //for all taxon pairs. Can make more efficient
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
							numOthersCompared++;
						lonelinessInWindow[window][it1] += numDiffs;
					}
				}
				if (numOthersCompared != 0)
					lonelinessInWindow[window][it1] = lonelinessInWindow[window][it1]/numOthersCompared; //average distance to all other taxa
				else
					lonelinessInWindow[window][it1] = -1;
			}
			ownerModule.numWindowsDone++;
		}
		done = true;
	}
	 * */
}

