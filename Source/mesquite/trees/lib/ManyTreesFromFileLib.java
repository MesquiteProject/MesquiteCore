/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.lib;
/*~~  */

import java.io.File;
import java.util.Random;
import java.util.Vector;

import mesquite.lib.Bits;
import mesquite.lib.CommandChecker;
import mesquite.lib.CommandRecord;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteCommand;
import mesquite.lib.MesquiteDouble;
import mesquite.lib.MesquiteException;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteListener;
import mesquite.lib.MesquiteLong;
import mesquite.lib.MesquiteMessage;
import mesquite.lib.MesquiteThread;
import mesquite.lib.Notification;
import mesquite.lib.ParseUtil;
import mesquite.lib.PathHolder;
import mesquite.lib.Snapshot;
import mesquite.lib.StringUtil;
import mesquite.lib.duties.FileCoordinator;
import mesquite.lib.duties.TreeSource;
import mesquite.lib.duties.TreesManager;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeVector;
import mesquite.lib.ui.AlertDialog;
import mesquite.lib.ui.ProgressIndicator;

/** Supplies trees from tree blocks in a file.  Reads trees only when needed; hence suitable for files with too many trees to be held in memory at once, but slower than StoredTrees.*/
public abstract class ManyTreesFromFileLib extends TreeSource implements MesquiteListener, PathHolder {
	int currentTree=0;
	Taxa preferredTaxa =null;
	Taxa savedTaxa = null;
	Taxa taxaInBlock = null;
	TreesManager manager;
	MesquiteFile file = null;
	TreeVector trees = null;
	Vector filePosVector;
	int arraySize = 1000;
	int highestTreeMarked = -1;
	int lastTreeRead = -1;
	String currentTreeName = null;
	int numTrees = MesquiteInteger.finite;
	int highestSuccessfulTree = -1;
	FIleCheckThread fileCheckingThread = null;
	protected MesquiteBoolean rereadWholeFileIfGrows = new MesquiteBoolean(true);
	//protected MesquiteBoolean lastTreeOnly = new MesquiteBoolean(false);
	protected MesquiteBoolean live;
	MesquiteCommand fileGrewCommand, fileChangedCommand;
	boolean fileWasModified = false;
	static MesquiteBoolean warningGiven = new MesquiteBoolean(false);
	protected int numTreesInTreeBlock = 0;
	protected Bits treesToSample = new Bits(0);
	protected boolean sampleTrees = true;
	protected int numTreesToSample = MesquiteInteger.unassigned;
	protected int numStartTreesToIgnore = 0;
	boolean isNEXUS = false;  //not yet using this
	/*NOTE: 
	 * -- to ask that it remains open even when finding the fail has failed, pass "remain" as the second token in arguments at hiring and in command setFilePath
	 * -- to ask this to use not the  taxon names, but standardized taxon names (t0, t1, t2, ...) pass "useStandardizedTaxonNames" as the third token in arguments at hiring and in command setFilePath
	 * -- to tell it what taxa block to use (because if may not be apparent if using the standardized taxon names) pass taxa block in initialize
	 * */
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		if (!MesquiteThread.isScripting()){
			if (!warningGiven.getValue() && getHiredAs()==TreeSource.class){
				alert("If you are using the function \"" + getName() + "\" as a Source of Trees for a tree window or a chart, you should be aware that it does not import the trees from the separate file into the current data file.  " + 
						"It merely reads the trees temporarily for use by the tree window or chart.  If you want to import the trees into the data file, use Import File With Trees (from the Taxa & Trees menu) or Include File (from the File menu).");
				warningGiven.setValue(true);
				storePreferences();
			}
		}
		filePosVector = new Vector();
		fileGrewCommand = new MesquiteCommand("fileGrew", this);
		fileChangedCommand = new MesquiteCommand("fileChanged", this);
		fileCheckingThread = new FIleCheckThread(this);
		/*TODO hiring of TreesManager previously occurred after the succeeding conditional which called obtainFile() 
		 * and processFile() (it is commented out below in its original position).  The latter method called 
		 * processTreeBlock(), which requires a non-null manager. oliver*/
		manager = (TreesManager)findElementManager(TreeVector.class);
		if (manager == null)
			return sorry("Tree manager not found.");
		if (!MesquiteThread.isScripting()){
			if (!obtainFile(arguments)){
				return sorry("No tree file was obtained.");
			}
			if (!processFile())				
				return sorry("The file could not be processed.");
		}
		//		manager = (TreesManager)findElementManager(TreeVector.class);
		//		if (manager == null)
		//			return sorry("Tree manager not found.");
		return additionStartJobItems();
	}

	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("warningGiven".equalsIgnoreCase(tag)) {
			warningGiven.setValue(content);
		}
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "warningGiven", warningGiven);  
		return buffer.toString();
	}
	 /**Returns name to show in windows etc. for tree block or source of trees.*/
	public String getTreesDescriptiveString(Taxa taxa){
		return getName();
	}

	/*.................................................................................................................*/
	protected boolean additionStartJobItems(){
		addMenuItem("File for \"Use Trees from Separate File\"...", makeCommand("setFilePath",  this));
		live = new MesquiteBoolean(canDoLiveUpdate());
		addCheckMenuItem( null, "Respond to Tree File Changes", makeCommand("toggleLive",  this), live);
		addCheckMenuItem( null, "Reread Whole File If Enlarged", makeCommand("toggleReread",  this), rereadWholeFileIfGrows);
		return true;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return true;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	public void setPreferredTaxa(Taxa taxa) {
		if (taxa !=null && taxa.isDoomed())
			return;
		if (preferredTaxa!=taxa) {
			preferredTaxa = taxa;
		}
	}
	/*.................................................................................................................*/
	public void initialize(Taxa taxa) {
		savedTaxa = taxa;
	}	/*.................................................................................................................*/
	protected boolean canIgnoreStartTrees(){
		return false;
	}
	/*.................................................................................................................*/
	protected boolean getSampleTrees(){
		return false;
	}
	/*.................................................................................................................*/
	protected boolean canDoLiveUpdate(){
		return true;
	}

	/*.................................................................................................................*/
	protected void setTreesToSample(int numTreesToSample) {
		int availableTrees = numTreesInTreeBlock - numStartTreesToIgnore;
		if (!MesquiteInteger.isCombinable(numTreesToSample)|| numTreesToSample>= availableTrees) {
			treesToSample.setAllBits();
			for (int i = 0; i<numStartTreesToIgnore; i++) {  //unset all of the initial ones (e.g., burnin ones)
				treesToSample.setBit(i, false);
			}
		}
		else {
			Random rng = new Random(System.currentTimeMillis());

			if (numTreesToSample<=availableTrees/2) {   // we set less than half
				treesToSample.clearAllBits();
				for (int i = 0; i<numTreesToSample; i++) {
					int candidate=-1;
					while (candidate<0 || treesToSample.isBitOn(candidate+numStartTreesToIgnore))
						candidate = (int)(rng.nextDouble()*availableTrees);
					treesToSample.setBit(candidate+numStartTreesToIgnore);
				}
			}
			else {  //we set more than half; therefore, set them all, then unset the need amount
				treesToSample.setAllBits();
				for (int i = 0; i<numStartTreesToIgnore; i++) {  //unset all of the initial ones (e.g., burnin ones)
					treesToSample.setBit(i, false);
				}
				int numTreesToUnSet = availableTrees -  numTreesToSample;
				for (int i = 0; i<numTreesToUnSet; i++) {
					int candidate=-1;
					while (candidate<0 || !treesToSample.isBitOn(candidate+numStartTreesToIgnore))
						candidate = (int)(rng.nextDouble()*availableTrees);
					treesToSample.setBit(candidate+numStartTreesToIgnore, false);
				}
			}
		}
	}
	/*.................................................................................................................*/
	protected String reportTreesSampled(){
		StringBuffer sb = new StringBuffer(100);
		int availableTrees = numTreesInTreeBlock - numStartTreesToIgnore;
		if (!getSampleTrees()){
			if (MesquiteInteger.isCombinable(numTrees))
				sb.append("" + numTrees + " trees.");
		}
		else if (!MesquiteInteger.isCombinable(numTreesToSample)|| numTreesToSample>= availableTrees) {
			sb.append("All " + availableTrees + " trees sampled.");
		}
		else {
			sb.append("Trees sampled: \n");
			int count = 1;
			for (int i = 0; i<numTreesInTreeBlock; i++) {
				if (treesToSample.isBitOn(i)) {
					sb.append("  " + (i+1));
					if (count % 10 == 0)
						sb.append("\n");
					count++;
				}
			}
			count--;
			sb.append("\n(" + count + " trees total)");

		}
		return sb.toString();
	}
	/*.................................................................................................................*
	private int nextTreeToSample(int prevTree) {
		if (getSampleTrees()) {
			for (int i=prevTree+1; i<=numTreesInTreeBlock; i++) {
				if (treesToSample.isBitOn(i))
					return i;
			}
			return MesquiteInteger.unassigned;
		}
		return prevTree++;
	}
	/*.................................................................................................................*/
	private int findTreeNumber(int treeNum) {
		if (getSampleTrees()) {
			int count=0;
			for (int i=0; i<=numTreesInTreeBlock; i++) {
				if (treesToSample.isBitOn(i)) {
					if (count==treeNum) 
						return i;
					count++;
				}
			}
			return MesquiteInteger.unassigned;
		}
		return treeNum;
	}
	protected boolean fileReady = false;
	/*.................................................................................................................*/
	private boolean obtainFile(String arguments){
		if (verbose) System.err.println("OBTAIN FILE");
		fileReady = false;
		if (ended){
			discreetAlert("WARNING:  Attempt to use module that has ended (ManyTreesFromFileLib)" + getEmployerPath());
		}
		FileCoordinator fCoord = getFileCoordinator();
		if (fCoord == null)
			return false;

		for (int j = 0; j<filePosVector.size(); j++){
			long[] filePosTrees = (long[])filePosVector.elementAt(j);

			for (int i=0; i<arraySize; i++)
				filePosTrees[i] = MesquiteLong.unassigned;
		}
		currentTree=0;
		highestTreeMarked = -1;
		lastTreeRead = -1;
		taxaInBlock = null;
		numTrees = MesquiteInteger.finite;
		highestSuccessfulTree = -1;
		//MesquiteFile trf = MesquiteFile.open(true, null, "Choose Tree File", );
		//	public static MesquiteFile open(boolean local, FilenameFilter fileFilter, String message, String suggestedDirectory) {

		MesquiteBoolean fileChosenIsNexus = new MesquiteBoolean(false);
		MesquiteFile treeFile = fCoord.getFileForReading(arguments, "Choose Tree File (NEXUS or simple Newick/Phylip format)", fileChosenIsNexus);  //arguments should already have the path!!!
		isNEXUS = fileChosenIsNexus.getValue();
		if (treeFile != null) {  //not yet allowing non-NEXUS
			if (file !=null) {
				file.closeReading();
				file.dispose();
				if (trees !=null) {
					trees.dispose();
					trees = null;
				}
			}
			file = treeFile;
			String p = parser.getTokenNumber(arguments, 3);
			file.useStandardizedTaxonNames = p!= null && p.equalsIgnoreCase("useStandardizedTaxonNames");
			fileCheckingThread.setPath(file.getPath());
		}
		else
			return false;
		return true;
	}

	boolean verbose = false;
	public String getFilePath(){
		return file.getPath();
	}

	protected boolean processFile(){
		if (verbose) System.err.println("PROCESS FILE");
		if (!goToTreeBlock(file))  	 		
			return false;
		if (!processTreeBlock())
			return false;
		fileReady = true;
		return true;
	}
	boolean ended = false;
	/*.................................................................................................................*/
	public void endJob(){
		if (file !=null){
			file.closeReading();
			file.dispose();
		}
		fileCheckingThread.abort = true;
		fileCheckingThread.interrupt();
		ended = true;
		super.endJob();
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		if (this.file == null)
			return null;
		Snapshot temp = new Snapshot();
		String arguments = StringUtil.tokenize(MesquiteFile.decomposePath(getProject().getHomeFile().getDirectoryName(), this.file.getPath())) + " remain ";
		if (this.file.useStandardizedTaxonNames)
			arguments += " useStandardizedTaxonNames";
		temp.addLine("setFilePath " + arguments);  //quote //todo: should parse name relative to path to home file!!!!!
		temp.addLine("toggleReread " + rereadWholeFileIfGrows.toOffOnString());
		if (canDoLiveUpdate()) {
			temp.addLine("toggleLive " + live.toOffOnString());
		}
		additionalSnapshot(temp);
		return temp;
	}
	/*.................................................................................................................*/
	public void additionalSnapshot(Snapshot snapshot) {
	}	
	/*.................................................................................................................*/
	MesquiteInteger pos = new MesquiteInteger(0);
	/*.................................................................................................................*/
	public boolean additionalDoCommands(String commandName, String arguments, CommandChecker checker) {
		return false;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Specifies the tree file to use", "[path to file]", commandName, "setFilePath")) {
			String path = parser.getFirstToken(arguments);
			filePosVector.removeAllElements();
			String secondToken =parser.getNextToken() ;  // have to store this as obtainFile uses the parser
			if (verbose) System.err.println("DOCOMMAND setFilePath");
			if (obtainFile(arguments)){
				if (processFile()){
					if (!MesquiteThread.isScripting())
						parametersChanged();
					return null;
				}
				else
					discreetAlert( "File could not be processed for " + getName() + " (path " + path + ")");
			}
			else 
				discreetAlert( "File was not obtained for " + getName() + " (path " + path + ")");
			if (!("remain".equalsIgnoreCase(secondToken)))
				iQuit();
		}
		else if (checker.compare(this.getClass(), "Sets whether or not to reread the whole file if the file enlarges", "[on or off]", commandName, "toggleReread")) {
			boolean current = rereadWholeFileIfGrows.getValue();
			rereadWholeFileIfGrows.toggleValue(parser.getFirstToken(arguments));
			if (current!=rereadWholeFileIfGrows.getValue() && !MesquiteThread.isScripting())
				parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets whether or not to listen to changes in the file", "[on or off]", commandName, "toggleLive")) {
			if (verbose) System.err.println("DOCOMMAND toggleLive");
			if (canDoLiveUpdate()) {
				boolean current = live.getValue();
				live.toggleValue(parser.getFirstToken(arguments));
				if (current!=live.getValue()){
					if (fileWasModified)
						fileModified(-1);
					else
						parametersChanged();
				}
			}
		}
		else if (checker.compare(this.getClass(), "Uses quite mode (no proress indicator, few messages)", null, commandName, "quietOperation")) {
			quietOperation = true;
		}
		else if (checker.compare(this.getClass(), "Receives message that file changed", null, commandName, "fileChanged")) {
			numTrees = MesquiteInteger.finite;
			highestTreeMarked = -1;
			lastTreeRead = -1;
			highestSuccessfulTree = -1;
			filePosVector.removeAllElements(); 
			boolean wasQuiet = quietOperation;
			quietOperation = true;
			boolean success = processFile();
			quietOperation = wasQuiet;
			if (verbose) System.err.println("DOCOMMAND fileCHANGED success: " + success);
			parametersChanged();

		}
		else if (checker.compare(this.getClass(), "Receives message that file grew", null, commandName, "fileGrew")) {
			int s = MesquiteInteger.fromString(arguments, new MesquiteInteger(0));
			parametersChanged(new Notification(MesquiteListener.ITEMS_ADDED, new int[]{s, MesquiteInteger.finite }));

		}
		else if (additionalDoCommands(commandName, arguments, checker))
			return  null;
		else
			super.doCommand(commandName, arguments, checker);
		return null;
	}
	/**Returns whether or not the source can handle asking for the last tree, i.e. for what the source says is maxTrees - 1, even if that is unassigned or infinite, i.e., is not a combinable number. 
	 * If asked, and the source has an indefinite number, it will supply a tree (e.g. from a live file) rather than just trying forever. 
	 * Used for Pin to Last Tree in BasicTreeWindow.*/
	public boolean permitsRequestForLastTree(Taxa taxa){
		return true;
	}

	/*.=========================================================================.*/
	/*.=========================================================================.*/
	boolean quietOperation = false;

	/** finds the tree block .*/
	private boolean goToTreeBlock(MesquiteFile mNF){
		if (verbose) System.err.println("goToTreeblock " + mNF.getFilePosition());
		ProgressIndicator progIndicator = null;
		if (!quietOperation){
			progIndicator =  new ProgressIndicator(getProject(),"Processing File "+ mNF.getName() + " to find trees", mNF.existingLength());
			progIndicator.start();
		}
		boolean found = false;
		if (mNF.openReading()) {
			try {
				mNF.goToFilePosition(0);
				//	long blockStart = 0;
				if (!quietOperation)
					logln("Processing File "+ mNF.getName() + " to find tree block");
				if (verbose) System.err.println("openREading " + mNF.getFilePosition());
				String token= mNF.firstToken(null);
				if (verbose) System.err.println("token " + token);
				MesquiteLong startPos = new MesquiteLong();
				if (token!=null) {
					//================================ NEXUS ============================
					if (isNEXUS){
						if (verbose) System.err.println("NEXUS ");
					if (!token.equalsIgnoreCase("#NEXUS")) {  //This shouldn't happen, because we've already checked
							String mes = "Not a valid NEXUS file (first token is \"" + token + "\")";
							if (!quietOperation)
								discreetAlert(mes);
							else
								logln(mes);
						}
						else {
							String name = null;

							while (!found && (name = mNF.goToNextBlockStart(startPos ))!=null) {
								if ("TREES".equalsIgnoreCase(name)){
									found = true;

									mNF.goToFilePosition(startPos.getValue()-1); //go back to start of trees block
								}
								else if ("TAXA".equalsIgnoreCase(name) || "DATA".equalsIgnoreCase(name)){
									if (progIndicator!=null)
										progIndicator.goAway();
									if (!quietOperation && !MesquiteThread.isScripting() && !AlertDialog.query(containerOfModule(), "Use File?",  "Tree file contains a TAXA or DATA block.  " + 
											"If the TREES block has no translation table, and if the order of taxa is different in this file than in your current project in Mesquite, " +
											"then the trees may be misread.  Do you want to open this file anyway?"))
										return false;
								}
								//else
								//	blockStart = mNF.getFilePosition()+1;
							}
							if (progIndicator!=null)
								progIndicator.goAway();
							if (found && !quietOperation)
								logln("Tree block found");
						}
					}
					//================================ simple Newick tree file ============================
					else {  
						if (!token.equalsIgnoreCase("(")) { 
							String mes = "Not a valid Phylip/Newick tree file (first token is \"" + token + "\"; it should be a '(')";
							if (!quietOperation)
								discreetAlert(mes);
							else
								logln(mes);
						}
						else {
							mNF.goToFilePosition(0);
							found = true;
							if (progIndicator!=null)
								progIndicator.goAway();
							if (found && !quietOperation)
								logln("Trees found");
						}					
					}
				}
				//============================================================
			}
			catch (MesquiteException e){
				if (verbose) System.err.println("ERROR " + mNF.getFilePosition());
				if (progIndicator!=null)
					progIndicator.goAway();
				return false;
			}
		}
		else {
			if (progIndicator!=null)
				progIndicator.goAway();
			return false;
		}
		if (progIndicator!=null)
			progIndicator.goAway();
		return found;
	}
	/*=============================================================================*/
	/*=============================================================================*/
	private boolean processTreeBlock(){

		if (verbose) System.err.println("PROCESS TREE BLOCK");
		String s;
		int treeNum=0;
		if (getProject().getNumberTaxas()==1)
			taxaInBlock = getProject().getTaxa(0); //as default)
		trees = new TreeVector(taxaInBlock);
		trees.setTaxa(taxaInBlock);
		trees.setName("Trees from \"" + file.getName() + "\"");
		boolean nameSet = false;
		MesquiteInteger status = new MesquiteInteger(0);
		ProgressIndicator surveyTreesIndicator=null;

		if (getSampleTrees()) {
			surveyTreesIndicator =  new ProgressIndicator(getProject(),"Processing File "+ file.getName() + " to survey trees", file.existingLength());
			surveyTreesIndicator.start();
			//	surveyTreesIndicator.startTimer();
		}
		boolean treesEncountered=false;

		if (file.getFilePosition()>0)
			file.goToFilePosition(file.getFilePosition()-1);  //needed because of where file pos last left
		if (file.getFilePosition()<0)
			return false;
		recordFilePos(0, file.getFilePosition()); 
		// if sampling trees, we don't ask for the entire command if trees have already been found in the Trees block.  
		// This is dangerous, as it means that this may not work if TREE commands are intermingled with other relevant commands!

		//================================ NEXUS ============================
		if (isNEXUS){
			boolean translationTableRead = false;
			while (!StringUtil.blank(s=file.getNextCommand(status, null, !(getSampleTrees()&&treesEncountered)))) {

				if (status.getValue() == 2) { //end of block reached
					numTreesInTreeBlock = treeNum;
					if (getSampleTrees() && treesToSample!=null) {
						treesToSample.resetSize(numTreesInTreeBlock);
						if (MesquiteInteger.isCombinable(numTreesToSample))
							setTreesToSample(numTreesToSample);
					}
					if (surveyTreesIndicator!=null)
						surveyTreesIndicator.goAway();
					return true;
				}
				String punc = ",";
				String commandName = parser.getFirstToken(s);

				if (commandName.equalsIgnoreCase("TREE") || commandName.equalsIgnoreCase("UTREE") || commandName.equalsIgnoreCase("RTREE"))  {
					treesEncountered=true;
					if (getSampleTrees()) {
						if (surveyTreesIndicator != null) {
							if (surveyTreesIndicator.isAborted()) {
								int response = AlertDialog.query(containerOfModule(), "Continue with tree file processing?", "Continue with tree file processing?", "Continue", "Use Only Trees Processed", "Cancel", 1);
								if (response==2) {
									numTreesInTreeBlock = treeNum;
									if (treesToSample!=null) {
										treesToSample.resetSize(numTreesInTreeBlock);
										if (MesquiteInteger.isCombinable(numTreesToSample))
											setTreesToSample(numTreesToSample);
									}
									surveyTreesIndicator.goAway();
									return true;
								}
								else if (response==3) {
									surveyTreesIndicator.goAway();
									return true;
								}
							}
							if (treeNum % 50 == 0) {
								surveyTreesIndicator.setText("Counting trees in file " + treeNum);
								surveyTreesIndicator.setCurrentValue(file.getFilePosition());
							}
						}
						recordFilePos(treeNum+1, file.getFilePosition());
						treeNum++;
					}
					else {
						recordFilePos(treeNum+1, file.getFilePosition());

						return true;
					}
				}
				else if (commandName.equalsIgnoreCase("BEGIN")) {
					treesEncountered=false;
					recordFilePos(0, file.getFilePosition() + 1); 
				}
				else if (commandName.equalsIgnoreCase("END")  || commandName.equalsIgnoreCase("ENDBLOCK")) {
					//ignoring these
				}
				else if (commandName.equalsIgnoreCase("TRANSLATE")) {
					if (treesEncountered && getSampleTrees())
						MesquiteMessage.println("Warning: TRANSLATE command encountered after trees were found in TREES block; there may be a problem with processing this file");
					Vector table = null;
					translationTableRead = true;
					if (taxaInBlock == null)
						table = new Vector();
					String label =  parser.getNextToken();
					while (punc !=null && !punc.equalsIgnoreCase(";")) {
						String taxonName = parser.getNextToken();
						if (file.useStandardizedTaxonNames){
							if (taxaInBlock == null)
								taxaInBlock = savedTaxa;
							String numS = null;
							if (taxonName != null)
								numS = taxonName.substring(1, taxonName.length());
							int it = MesquiteInteger.fromString(numS);
							if (MesquiteInteger.isCombinable(it))
								taxonName = taxaInBlock.getTaxonName(it);
						}
						if (taxaInBlock==null) 
							table.addElement(StringUtil.tokenize(taxonName) + " " + StringUtil.tokenize(label));
						else
							trees.setTranslationLabel(label, taxonName, false);
						punc =  parser.getNextToken(); 
						if (punc !=null && !punc.equals(";")) {
							label =  parser.getNextToken();
							if (";".equalsIgnoreCase(label))
								punc = label;  //to pop out of loop
						}
					}

					if (taxaInBlock==null) {
						taxaInBlock = manager.findTaxaMatchingTable(trees, getProject(), file, table);
						if (taxaInBlock!=null) {
							trees.setTaxa(taxaInBlock);
							trees.setTranslationTable(table);
						}
						else  {
							String st = "FAILED.";
							discreetAlert( st);
						}

					}
					else {
						if (table!=null)
							trees.setTranslationTable(table);
					}
					trees.checkTranslationTable();
					recordFilePos(0, file.getFilePosition()); 
				}
				else if (commandName.equalsIgnoreCase("TITLE")) {
					if (treesEncountered && getSampleTrees())
						MesquiteMessage.println("Warning: TITLE command encountered after trees were found in TREES block; there may be a problem with processing this file");
					trees.setName(parser.getTokenNumber(2));
					nameSet = true;
					recordFilePos(0, file.getFilePosition()); 
				}
				else if (commandName.equalsIgnoreCase("LINK")) {
					if (treesEncountered && getSampleTrees())
						MesquiteMessage.println("Warning: LINK command encountered after trees were found in TREES block; there may be a problem with processing this file");
					if ("taxa".equalsIgnoreCase(parser.getTokenNumber(2))) {
						String taxaTitle = parser.getTokenNumber(4);
						taxaInBlock = getProject().getTaxa(file, taxaTitle);
						if (taxaInBlock == null)
							taxaInBlock = getProject().getTaxaLastFirst(taxaTitle);
						if (taxaInBlock == null) {
							if (getProject().getNumberTaxas(file)==1) //if translation table should search for match
								taxaInBlock = getProject().getTaxa(file, 0);
							else if (getProject().getNumberTaxas(file)==0 && getProject().getNumberTaxas()==1) //if translation table should search for match
								taxaInBlock = getProject().getTaxa(0);
							else
								discreetAlert( "Taxa block not found for tree block");
						}
						trees.setTaxa(taxaInBlock);
						if (!nameSet)
							trees.setName("Trees block from " + file.getName());
					}
					recordFilePos(0, file.getFilePosition()); 
				}
				else if (!treesEncountered)
					recordFilePos(0, file.getFilePosition()); 
			}
			if (!translationTableRead && file.useStandardizedTaxonNames){
				if (taxaInBlock == null)
					taxaInBlock = savedTaxa;
				for (int it = 0; it<taxaInBlock.getNumTaxa(); it++)
					trees.setTranslationLabel(Integer.toString(it+1), "t" + it, false);
				trees.checkTranslationTable();
			}
		}
		//============================== Simple Newick/Phylip tree file ==================================
		else {
			while (!StringUtil.blank(s=file.getNextCommand(status, null, !(getSampleTrees()&&treesEncountered)))) {

				if (status.getValue() == 2 || status.getValue() == -1) { //end 
					numTreesInTreeBlock = treeNum;
					if (getSampleTrees() && treesToSample!=null) {
						treesToSample.resetSize(numTreesInTreeBlock);
						if (MesquiteInteger.isCombinable(numTreesToSample))
							setTreesToSample(numTreesToSample);
					}
					if (surveyTreesIndicator!=null)
						surveyTreesIndicator.goAway();
					return true;
				}

				treesEncountered=true;
				if (getSampleTrees()) {
					if (surveyTreesIndicator != null) {
						if (surveyTreesIndicator.isAborted()) {
							int response = AlertDialog.query(containerOfModule(), "Continue with tree file processing?", "Continue with tree file processing?", "Continue", "Use Only Trees Processed", "Cancel", 1);
							if (response==2) {
								numTreesInTreeBlock = treeNum;
								if (treesToSample!=null) {
									treesToSample.resetSize(numTreesInTreeBlock);
									if (MesquiteInteger.isCombinable(numTreesToSample))
										setTreesToSample(numTreesToSample);
								}
								surveyTreesIndicator.goAway();
								return true;
							}
							else if (response==3) {
								surveyTreesIndicator.goAway();
								return true;
							}
						}
						if (treeNum % 50 == 0) {
							surveyTreesIndicator.setText("Counting trees in file " + treeNum);
							surveyTreesIndicator.setCurrentValue(file.getFilePosition());
						}
					}
					recordFilePos(treeNum+1, file.getFilePosition());
					treeNum++;
				}
				else {
					recordFilePos(treeNum+1, file.getFilePosition());
					return true;
				}


			}
			if (file.useStandardizedTaxonNames){
				if (taxaInBlock == null)
					taxaInBlock = savedTaxa;
				for (int it = 0; it<taxaInBlock.getNumTaxa(); it++)
					trees.setTranslationLabel(Integer.toString(it+1), "t" + it, false);
				trees.checkTranslationTable();
			}
		}
		//============================================================

		numTreesInTreeBlock = treeNum;
		if (getSampleTrees()) {
			if (surveyTreesIndicator!=null)
				surveyTreesIndicator.goAway();
		}
		if (getSampleTrees() && treesToSample!=null) {
			treesToSample.resetSize(numTreesInTreeBlock);
			setTreesToSample(numTreesToSample);
		}
		return true;
	}
	/*=============================================================================*/
	/*.................................................................................................................*/
	/* Note: this returns a file position only if the tree index is within the range in the file AND the tree reading has already passed
	that point.  It does not force a scan to find that tree if file reading has yet to proceed that far */
	public long getFilePos(int iTree){
		if (!MesquiteInteger.isCombinable(iTree))
			return MesquiteLong.unassigned;
		if (!posExists(iTree)) {
			if (!quietOperation)
				MesquiteMessage.warnProgrammer("NO POS RECORDED in " + getName() + " ( tree " + iTree + ")");
			return MesquiteLong.unassigned;
		}
		int vec = iTree / arraySize;
		int loc = iTree % arraySize;
		if (vec>= filePosVector.size())
			return 0;
		long[] filePosTrees = (long[])filePosVector.elementAt(vec);
		if (loc <0 || loc > filePosTrees.length)
			return 0;  
		return filePosTrees[loc];
	}
	boolean posExists(int iTree){
		return (iTree < arraySize*filePosVector.size()) ;
	}
	/*.................................................................................................................*/
	void recordFilePos(int iTree, long pos){
		if (pos<0 || pos > 100000000000L) {
			if (!quietOperation)
				MesquiteMessage.warnProgrammer("illegal file pos in " + getName());
			if (verbose) System.err.println("RECORDING bad file pos " + MesquiteLong.toString(pos));
			return;
		}
		long[] filePosTrees;
		if (verbose) System.err.println("$$$$ file pos tree " + iTree + " pos " + pos);
		while (!posExists(iTree)) {
			filePosTrees = new long[arraySize];
			for (int i=0; i<arraySize; i++)
				filePosTrees[i] = MesquiteLong.unassigned;
			filePosVector.addElement(filePosTrees);
		}
		int vec = iTree / arraySize;
		int loc = iTree % arraySize;
		filePosTrees = (long[])filePosVector.elementAt(vec);
		if (isNEXUS)
			filePosTrees[loc] = pos-1; //-1 to ensure not too far
		else
			filePosTrees[loc] = pos; //-1 to ensure not too far
			
		if (iTree > highestTreeMarked)
			highestTreeMarked = iTree;
	}

	/*.................................................................................................................*/
	//returns tree description command; if blank, then file is done
	String findNextTree (int currentTree, StringBuffer comment){
		String command  = null;
		boolean isTreeCommand = false;
		MesquiteInteger status = new MesquiteInteger(0);
		while (!isTreeCommand){
			command = file.getNextCommand(status, comment); 
			if (StringUtil.blank(command))
				return null;
			else {
				if (isNEXUS)
					isTreeCommand = ParseUtil.darkBeginsWithIgnoreCase(command, "TREE") || ParseUtil.darkBeginsWithIgnoreCase(command, "UTREE") || ParseUtil.darkBeginsWithIgnoreCase(command, "RTREE");
				else
					isTreeCommand = ParseUtil.darkBeginsWithIgnoreCase(command, "(") || ParseUtil.darkBeginsWithIgnoreCase(command, "[");
			}
			if (isTreeCommand && currentTree>highestSuccessfulTree){
				highestSuccessfulTree = currentTree;
				highestSuccessfulDescription = command;
			}
		}
		return command;
	}
	/*=============================================================================*/
	/*.................................................................................................................*/
	String getTreeDescription(int currentTree, StringBuffer comment){
		if (verbose) System.err.println("getTreeDescription " + currentTree);
		if (!fileCheckingThread.going())
			fileCheckingThread.start();
		MesquiteInteger status = new MesquiteInteger(0);
		if (file==null)
			return null;
		boolean fileDone = false;

		String command = null;
		if (getSampleTrees()) {
			if (verbose) System.err.println(" >>>> getSampleTrees  " + highestTreeMarked + " fPos " + MesquiteLong.toString(getFilePos(highestTreeMarked)));
		//	int treeSelected = treesToSample.numBitsOn();
			int treeNum = findTreeNumber(currentTree);
			long fPos = getFilePos(treeNum);
			if (verbose) System.err.println(" >> " + treeNum + " fPos " + MesquiteLong.toString(fPos));
			if (!MesquiteLong.isCombinable(fPos) || fPos<0) {
				if (verbose) System.err.println("bad file pos1 " + MesquiteLong.toString(fPos));
				return null;
			}
			file.goToFilePosition(fPos);

			if (verbose) System.err.println(" >> ");
			command = findNextTree(currentTree, comment);
			fileDone = command==null;
		}
		else if (currentTree==0){  //first tree
			if (verbose) System.err.println(" >>>> ==0  " + highestTreeMarked + " fPos " + MesquiteLong.toString(getFilePos(highestTreeMarked)));
			CommandRecord.tick("\"Trees from Separate File\": Going to tree " + (currentTree+1) + " [c]");
			long fPos = getFilePos(0);
			if (!MesquiteLong.isCombinable(fPos) || fPos<0) {
				if (verbose) System.err.println("bad file pos2 for 0 " + MesquiteLong.toString(fPos));
				return null;
			}
			file.goToFilePosition(fPos);
			command = findNextTree(currentTree, comment);
			fileDone = command==null;
			recordFilePos(1, file.getFilePosition()-1); 
		}
		else if (currentTree == lastTreeRead+1){ //last tree read was one less than requested; just continue to next without resetting file position
			if (verbose) System.err.println(" >>>> == lastTreeRead+1  " + highestTreeMarked + " fPos " + MesquiteLong.toString(getFilePos(highestTreeMarked)));
			CommandRecord.tick("Going to tree " + (currentTree+1) + " [a]");
			command = findNextTree(currentTree, comment);
			fileDone = command==null;
			if (currentTree % 1000 == 0 && command != null && ! file.atEOF()) {
				MesquiteMessage.println("Tree " + (currentTree) + " found by \"Trees from Separate File\"");
			}
			recordFilePos(currentTree+1, file.getFilePosition()-1); 
		}/**/
		else if (currentTree>highestTreeMarked){  // a tree not yet read & not next in line
			if (verbose) System.err.println(" >>>> currentTree>highestTreeMarked  " + highestTreeMarked + " fPos " + MesquiteLong.toString(getFilePos(highestTreeMarked)));
			if (highestTreeMarked>=0) {
				long fPos = getFilePos(highestTreeMarked);
				if (!MesquiteLong.isCombinable(fPos) || fPos<0) {
					if (verbose) System.err.println("bad file pos3 " + MesquiteLong.toString(fPos));
					return null;
				}
			file.goToFilePosition(fPos);
			}
			int timeout = 0;
			int startingTree = highestTreeMarked;
			if (startingTree<0)
				startingTree = 0;
			boolean isTreeCommand = false;
			for (int i = startingTree; i<=currentTree && !fileDone && !file.atEOF(); i++) {
				CommandRecord.tick("Going to tree " + (i+1) + " [d]");
				if (verbose) System.err.println("  ~~~ tree " + i );
				isTreeCommand = false;
				timeout = 0;
				while (!isTreeCommand && !fileDone && !file.atEOF() && timeout < 10000){
					command = file.getNextCommand(status, comment); 
					//if (verbose) System.err.println("     ~~~ command " + command );
					if (StringUtil.blank(command))
						fileDone = true;
					else {
						if (isNEXUS)
							isTreeCommand = ParseUtil.darkBeginsWithIgnoreCase(command, "TREE") || ParseUtil.darkBeginsWithIgnoreCase(command, "UTREE") || ParseUtil.darkBeginsWithIgnoreCase(command, "RTREE");
						else
							isTreeCommand = ParseUtil.darkBeginsWithIgnoreCase(command, "(") || ParseUtil.darkBeginsWithIgnoreCase(command, "[");
					}
					if (!isTreeCommand)
						timeout++;
					else
						if (verbose) System.err.println("        ~~~ TREE!!! " );

					if (isTreeCommand && i>highestSuccessfulTree){
						highestSuccessfulTree = i;
						highestSuccessfulDescription = command;
						if (verbose) System.err.println("        ~~~ highestSuccessfulTree " + highestSuccessfulTree);
					}

				}
				if (i % 1000 == 0 && !fileDone && ! file.atEOF()) {
					MesquiteMessage.println("Tree " + (i) + " found by \"Trees from Separate File\"");
				} 
				if (timeout >= 10000)
					alert("Error in reading tree file; 10000 commands found other than tree commands [last command (" + command + ")]");
				recordFilePos(i+1, file.getFilePosition()-1); 
			}
		}
		else { //a tree that is at or before the lastTreeRead, thus position should be known
			CommandRecord.tick("Going to tree " + (currentTree+1) + " [b]");

			long fPos = getFilePos(currentTree);
			if (!MesquiteLong.isCombinable(fPos) || fPos<0) {
				if (verbose) 	System.err.println("bad file pos4 " + MesquiteLong.toString(fPos));
				return null;
			}
			file.goToFilePosition(fPos);
			command = findNextTree(currentTree, comment);
			fileDone = command==null;
	}
		lastTreeRead = currentTree;
		//numTrees = highestSuccessfulTree+1;
		//numTreesInTreeBlock = highestSuccessfulTree+1;
		if (fileDone) {
			numTrees = highestSuccessfulTree+1;
			if (verbose) System.err.println("##### NUMTREES  " + numTrees);
		}
		/*	if (StringUtil.blank(command) && fileDone) {
			//discreetAlert("The last tree in the file " + file.getFileName() + " has been reached.");
			currentTree = highestSuccessfulTree;
			command = null;
			//parametersChanged(new Notification(MesquiteListener.NUM_ITEMS_CHANGED));
		}*/
		return command;
	}
	String highestSuccessfulDescription = null;
	/*.................................................................................................................*/
	private Tree getCurrentTree(Taxa taxa, boolean processTree, MesquiteTree t) {
		if (verbose) System.err.println(" ---gCT1 ");
		if (!fileReady)
			return null;
		String treeDescription = null;
		if (verbose) System.err.println(" ---gCT1b ");

		MesquiteInteger cPos = new MesquiteInteger(0);
		if (taxa != null && taxa != taxaInBlock) {
			if (taxaInBlock !=null) {
				discreetAlert( "Sorry, the trees found in file are for a different set of taxa than that requested");
				return null;
			}
			else {
				taxaInBlock = taxa;
			}
		}
		if (taxa == null)
			taxa = taxaInBlock;
		if (verbose) System.err.println(" ---gCT2 currentTree " + currentTree);
		MesquiteInteger status = new MesquiteInteger(0);
		StringBuffer comment = new StringBuffer();
		String treeCommand = getTreeDescription(currentTree, comment);
		if (verbose) System.err.println(" ---gCT2b " + treeCommand);
		if (treeCommand == null)
			return null;
		if (treeCommand.length()<=2)
			return null;
		/* === */
		int whichType = 1;
		boolean treeDescriptionBad = false;

		if (isNEXUS){  //============= NEXUS ===============
			String commandName = parser.getFirstToken(treeCommand);
			if (commandName == null)
				return null;
			if (verbose) System.err.println(" ---gCT3 "+ commandName);
			if (commandName.equalsIgnoreCase("UTREE")) 
				whichType =2;
			currentTreeName=parser.getNextToken();
			if (currentTreeName != null && currentTreeName.equals("*"))
				currentTreeName=parser.getNextToken();
			treeDescriptionBad = (currentTreeName == null);
			parser.getNextToken(); //eat up "equals"
			if (processTree)
				treeDescription=treeCommand.substring((int)parser.getPosition(), treeCommand.length());
		}
		else {  //============= Simple Newick/Phylip ===============
			if (!MesquiteInteger.isCombinable(currentTree))
				currentTreeName = "Tree from tree file";
			else
				currentTreeName = "Tree #" + (currentTree+1) + " from tree file";
			if (verbose) System.err.println("current " + currentTree + " plus1 " + (currentTree+1));
			
			parser.setPosition(0);
			if (processTree)
				treeDescription=treeCommand;
		}

		/* ===*
		
		String commandName = parser.getFirstToken(treeCommand);
		if (commandName == null)
			return null;
		if (verbose) System.err.println(" ---gCT3 "+ commandName);
		int whichType = 1;
		if (commandName.equalsIgnoreCase("UTREE")) 
			whichType =2;
		
		currentTreeName=parser.getNextToken();
		if (currentTreeName != null && currentTreeName.equals("*"))
			currentTreeName=parser.getNextToken();
		boolean treeDescriptionBad = (currentTreeName == null);
		parser.getNextToken(); //eat up "equals"
		if (processTree)
			treeDescription=treeCommand.substring((int)parser.getPosition(), treeCommand.length());
		/*================*/
		if (treeDescription== null || treeDescription.length()<=2)
			return null;
		MesquiteTree thisTree =t; //t is supplied mostly for skipping trees
		if (t == null)
			thisTree = new MesquiteTree(taxa);
		thisTree.setFileIndex(currentTree);
		String commentString = comment.toString();
		if (verbose) System.err.println(" ---gCT4 " + treeDescription);

		if (processTree && commentString!=null && commentString.length()>1){
			if (commentString.charAt(0)=='!')
				thisTree.setAnnotation(commentString.substring(1, commentString.length()), false);
			else {
				int wpos = commentString.indexOf("&W");
				if (wpos <0)
					wpos = commentString.indexOf("&w");
				if (wpos>=0) {
					cPos.setValue(wpos+2);
					String num = ParseUtil.getToken(commentString, cPos);
					String slash = ParseUtil.getToken(commentString, cPos);
					String denom = ParseUtil.getToken(commentString, cPos, null, "$");  //ZQ what was this? It seems to be prepared for a special sort of Newick comment involving $. Example file? (re: changes in parser
					double w = 0;
					if (slash !=null && "/".equals(slash))
						w = 1.0*(MesquiteInteger.fromString(num))/(MesquiteInteger.fromString(denom));
					else
						w = MesquiteDouble.fromString(num);
					if (MesquiteDouble.isCombinable(w)) {
						MesquiteDouble d = new MesquiteDouble(w);
						d.setName(TreesManager.WEIGHT);
						thisTree.attachIfUniqueName(d);
					}
				}
			}
		}
		if (verbose) System.err.println(" ---gCT5 ");
		if (processTree){
			thisTree.setTreeVector(trees);
			if (trees != null)
				trees.setTaxa(taxa);

			trees.addElement(thisTree, false);
			boolean success = (!treeDescriptionBad) && thisTree.readTree(treeDescription);
			if (verbose) System.err.println(" ---gCT6 ");
			//thisTree.warnRetIfNeeded();
			thisTree.setName(currentTreeName);
			if (whichType ==2) 
				thisTree.setRooted(false, false);
			trees.removeElement(thisTree, false);
			if (!success)
				return null;
		}
		if (verbose) System.err.println(" ---gCT7 ");
		return thisTree;
	}
	/*.................................................................................................................*/
	public Tree getTree(Taxa taxa, int itree) {
		setPreferredTaxa(taxa);
		currentTree=itree;
		return getCurrentTree(taxa, true, null);
	}
	public void findNumTrees(Taxa taxa){
		if (verbose) System.err.println(" findNumTrees ################"); 
		if (taxa == null){
			if (verbose) System.err.println(" findNumTrees TAXA NULL "); 
			currentTree = 0;
			numTrees = 0;
			return;
		}
		int i = 0;
		if (MesquiteInteger.isCombinable(numTrees))
			i = numTrees;
		int lastFound = -1;
		MesquiteTree dummyTree = new MesquiteTree(taxa);
		int oldCurrent = currentTree;
		currentTree = i;
		if (verbose) System.err.println(" findNumTrees before " + MesquiteInteger.toString(numTrees)); 
		while((dummyTree = (MesquiteTree)getCurrentTree(taxa, false, dummyTree))!=null){
			lastFound = i;
			i++;
			currentTree = i;
			if (verbose) System.err.println(" dummy " + i + " " + dummyTree); 
		}
		if (verbose) System.err.println(" findNumTrees after " + MesquiteInteger.toString(numTrees)); 
		currentTree = oldCurrent;
		if (lastFound >=0)
			numTrees = lastFound;
	}

	/*.................................................................................................................*/
	public int getNumberOfTrees(Taxa taxa) {
		setPreferredTaxa(taxa);
		if (getSampleTrees() && MesquiteInteger.isCombinable(numTreesToSample))
			return numTreesToSample;

		return numTrees; 
	}
	/*.................................................................................................................*/
	public int getNumberOfTrees(Taxa taxa, boolean determineNumberIfFinite) {
		if (!determineNumberIfFinite)
			return getNumberOfTrees(taxa);
		setPreferredTaxa(taxa);

		if (!MesquiteInteger.isCombinable(numTrees)) { // fix oliver.Feb.12.'10.
			if(getSampleTrees() && MesquiteInteger.isCombinable(numTreesToSample)){
				return numTreesToSample;
			}
			findNumTrees(taxa);
		} 
		return numTrees; 
	}

	/*.................................................................................................................*/
	public String getTreeNameString(Taxa taxa, int itree) {
		if (itree == currentTree)
			return getCurrentTreeNameString(taxa);
		setPreferredTaxa(taxa);
		return "Tree " + (itree +1);
	}
	/*.................................................................................................................*/
	public String getCurrentTreeNameString(Taxa taxa) {
		setPreferredTaxa(taxa);
		if (currentTreeName == null)
			return "Tree " + (currentTree +1);
		return currentTreeName;
	}

	public void fileModified(long longer){
		fileWasModified = true;
		if (!live.getValue())
			return;
		fileWasModified = false;
		int s = numTrees;
		numTrees = MesquiteInteger.finite;
		highestSuccessfulTree = -1;
		if (verbose) System.err.println("fileModified " + longer);
		if (longer>0 && !rereadWholeFileIfGrows.getValue())
			fileGrewCommand.doItMainThread(Integer.toString(s), null, false, false);  
		else
			fileChangedCommand.doItMainThread(null, null, false, false);  

	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Use Trees from Separate File";
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Use Trees from Separate File...";
	}

	/*.................................................................................................................*/
	public String getExplanation() {
		return "Supplies trees directly from a file, without bringing the contained tree block entirely into memory.  " + 
				"This is a special purpose module designed to allow much larger blocks of trees to be used within constraints of memory, but will make some calculations slower.  " + 
				"Except for this special use, we recommend you use Include or Link from the file menu to access external tree files.  " + 
				"This module does NOT copy the trees into your main data file, and so if you save your main data file then move it or the tree file, the data file will no longer be able to find the trees.  " + 
				"This module does not know how many trees are in the file, and hence may attempt to read files beyond the number in the file.";
	}
	/*.................................................................................................................*/
	public String getParameters() {
		if (file == null)
			return null;
		String s = "Trees obtained from file " + file.getName();
		s += "\n" + reportTreesSampled();
		return s;
	}
}

class FIleCheckThread extends Thread {
	ManyTreesFromFileLib ownerModule;
	boolean abort = false;
	File treeFile;
	String path;
	long lastModified = 0;
	long lastLength = 0;
	boolean going = false;
	public FIleCheckThread (ManyTreesFromFileLib ownerModule){
		this.ownerModule = ownerModule;
	}
	public void setPath(String path){
		this.path = path;
		treeFile = new File(path);
		lastModified = treeFile.lastModified();
		lastLength = treeFile.length();
	}
	public void start(){
		going = true;
		super.start();
	}
	public boolean going(){
		return going;
	}
	public void run() {

		while (!abort){
			try {
				Thread.sleep(1000);
				if (treeFile != null){
					long mod = MesquiteFile.fileOrDirectoryLastModified(treeFile.getPath());
					long length = treeFile.length();
					if (mod > lastModified) {
						ownerModule.fileModified(length - lastLength);
						lastLength = length;
						lastModified = mod;
					}
				}
			}
			catch (InterruptedException e){
				Thread.currentThread().interrupt();
			}
		}

	}


}

