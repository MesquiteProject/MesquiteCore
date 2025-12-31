/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.genomic.TreesFromSelMatricesParallel;
/* created May 02 */

import java.awt.Point;
import java.util.Vector;

import mesquite.categ.lib.MolecularData;
import mesquite.lib.Bits;
import mesquite.lib.CommandChecker;
import mesquite.lib.IntegerField;
import mesquite.lib.ListableVector;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteMessage;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteString;
import mesquite.lib.MesquiteThread;
import mesquite.lib.NumberArray;
import mesquite.lib.ResultCodes;
import mesquite.lib.StringArray;
import mesquite.lib.StringUtil;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.MCharactersDistribution;
import mesquite.lib.duties.MatrixSourceCoord;
import mesquite.lib.duties.TreeInferer;
import mesquite.lib.duties.TreeSearcherFromMatrix;
import mesquite.lib.parallel.ParallelParams;
import mesquite.lib.parallel.Parallelizable;
import mesquite.lib.parallel.Parallelizer;
import mesquite.lib.table.MesquiteTable;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeDisplay;
import mesquite.lib.tree.TreeVector;
import mesquite.lib.ui.ExtensibleDialog;
import mesquite.lib.ui.ProgressIndicator;
import mesquite.lib.ui.RadioButtons;
import mesquite.lists.lib.CharMatricesListUtility;

/* ======================================================================== */
public class TreesFromSelMatricesParallel extends CharMatricesListUtility {
	int storageChoice = 0; //0 = single tree block; 1 = multiple tree blocks; 2 = multiple tree files
	static final int SINGLE_TREE_BLOCK = 0;
	static final int MULTIPLE_TREE_BLOCKS = 1;
	static final int MULTIPLE_FILES = 2;

	/*.................................................................................................................*/
	public String getName() {
		return "Infer Trees from Matrices (Parallelized)";
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Infer Trees from Matrices (Parallelized)...";
	}

	public String getExplanation() {
		return "Infers trees for each of the matrices, and compiles them into a single tree block. Parallelized." ;
	}
	TreeSearcherFromMatrix inferenceTask;
	ThreadListOfMatrices matrixSourceTask;
	TreeInferenceParallelMachine machine;
	int numThreads = 1;
	int numCoresPerInference = 1;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		inferenceTask = (TreeSearcherFromMatrix)hireCompatibleEmployee(TreeSearcherFromMatrix.class, new String[]{"acceptImposedMatrixSource", "parallelReady"}, "Tree inference method");
		machine = new TreeInferenceParallelMachine(this, 1);
		matrixSourceTask = new ThreadListOfMatrices(machine);
		if (inferenceTask == null || matrixSourceTask == null)
			return false;
		inferenceTask.setMatrixSource(matrixSourceTask);
		return queryOptions();
	}
	/* ................................................................................................................. */
	public boolean queryOptions() {
		Point coreRequests = getEmployeeCoreRequests();
		int minCores = 1;
		int maxCores = MesquiteInteger.infinite;
		if (coreRequests != null) {
			minCores = coreRequests.x;
			maxCores = coreRequests.y;
		}
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog queryDialog = new ExtensibleDialog(containerOfModule(), "Parallel Tree Inferences", buttonPressed);
		String infoString = "The tree inferences will be performed in parallel, on several threads. Choose the number of parallel threads according to your computer's multiprocessing capabilities.";
		if (minCores>1){
			if (minCores == maxCores)
				infoString += "\n\nNote: the chosen tree inference program requires " + minCores + " processing cores for each of the separate threads.";
			else {
				if (MesquiteInteger.isCombinable(maxCores)) {
					infoString += "\n\nNote: the chosen tree inference program requires, for each of the separate threads, " + minCores + " processing cores, and can use as many as " + maxCores + " cores.";
				}
				else infoString += "\n\nNote: the chosen tree inference program requires at least " + minCores + " processing cores for each of the separate threads.";	
			}
		}
		infoString += "\n\nYour computer has " + Runtime.getRuntime().availableProcessors() + " processing cores available.";
		queryDialog.addLargeOrSmallTextLabel(infoString);

		IntegerField threadsField = queryDialog.addIntegerField("Number of Mesquite threads", numThreads, 20, 1, 255);
		IntegerField coresField = null;

		if (minCores>1){
			if (minCores == maxCores)
				numCoresPerInference = minCores;
			else {

				if (MesquiteInteger.isCombinable(maxCores))
					maxCores = MesquiteInteger.infinite;
				if (numCoresPerInference<minCores)
					numCoresPerInference = minCores;
				coresField = queryDialog.addIntegerField("Number of processor cores for each thread", numCoresPerInference, 20, minCores, maxCores);
			}
		}
		queryDialog.addHorizontalLine(1);
		queryDialog.addLargeOrSmallTextLabel("Where to save trees inferred from the matrices?");

		RadioButtons whereToSave = queryDialog.addRadioButtons (new String[] {"In single tree block", "In separate tree block for each matrix", "In external tree files"}, storageChoice);

		queryDialog.addHorizontalLine(1);
		queryDialog.addLargeOrSmallTextLabel("(Note: the first matrix will be processed alone, and then the others in parallel.)");
		queryDialog.setDefaultTextComponent(threadsField.getTextField());
		queryDialog.setDefaultComponent(threadsField.getTextField());

		queryDialog.completeAndShowDialog(true);

		boolean OK = buttonPressed.getValue() == 0;
		if (OK) {
			if (!threadsField.isValidInteger()) {
				alert("The number of threads must be a valid integer.");
				OK = false;
			}
			else	if (coresField != null && !coresField.isValidInteger()) {
				alert("The number of processor cores must be a valid integer.");
				OK = false;
			}
			else {
				int temp = threadsField.getValue();
				int tempCores = -1;
				if (coresField !=null)
					tempCores = coresField.getValue();
				if (MesquiteInteger.isCombinable(temp) && temp > 0 && temp < 256) 
					numThreads = temp;
				else {
					alert("The number of threads must be between 1 and 255.");
					OK = false;
				}
				if (coresField == null)
					;
				else if (MesquiteInteger.isCombinable(tempCores) && tempCores >= minCores && temp < 256) 
					numCoresPerInference = tempCores;
				else {
					alert("The number of processor cores must be between " + minCores + " and 255.");
					OK = false;
				}
				if (OK){
					storageChoice = whereToSave.getValue();
					storePreferences();
				}

			}
		}
		queryDialog.dispose();
		return (OK);
	}

	/** if returns true, then requests to remain on even after operateOnTaxas is called.  Default is false*/
	public boolean pleaseLeaveMeOn(){
		return false;
	}
	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {

		if ("numThreads".equalsIgnoreCase(tag))
			numThreads = MesquiteInteger.fromString(content);

		if ("numCoresPerInference".equalsIgnoreCase(tag))
			numCoresPerInference = MesquiteInteger.fromString(content);
		if ("storageChoice".equalsIgnoreCase(tag))
			storageChoice = MesquiteInteger.fromString(content);

		super.processSingleXMLPreference(tag, content);
	}

	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "numThreads", numThreads);  
		StringUtil.appendXMLTag(buffer, 2, "numCoresPerInference", numCoresPerInference);  
		StringUtil.appendXMLTag(buffer, 2, "storageChoice", storageChoice);  

		buffer.append(super.preparePreferencesForXML());
		return buffer.toString();
	}



	boolean compatibleMatrix(CharacterData data) {
		if (data.getNumTaxaWithAnyApplicable() <  4)
			return false;
		return data.isCompatible(inferenceTask.getCharacterClass(), getProject(), null, null);
	}

	/** Called to operate on the CharacterData blocks.  Returns true if taxa altered*/
	public boolean operateOnDatas(ListableVector datas, MesquiteTable table){
		if (datas.size() == 0)
			return false;
		if (getProject() != null)
			getProject().incrementProjectWindowSuppression();
		Taxa taxa = null;
		for (int im = 0; im < datas.size(); im++){
			CharacterData data = (CharacterData)datas.elementAt(im);
			if (compatibleMatrix(data)) {
				if (taxa == null)
					taxa = data.getTaxa();
				else if (taxa != data.getTaxa()) {
					discreetAlert("Sorry, trees can't be inferred for the selected matrices in a single request because the matrices pertain to different taxa blocks.");
					return false;
				}
			}
		}
		machine.setDatas(datas);
		mesquite.lib.Commandable runner = (mesquite.lib.Commandable)inferenceTask.doCommand("getRunner", null, CommandChecker.defaultChecker); 
		runner.doCommand("forceNumProcessors", MesquiteInteger.toString(numCoresPerInference), CommandChecker.defaultChecker);
		runner.doCommand("showIntermediateTrees", "false", CommandChecker.defaultChecker);
		inferenceTask.initialize(taxa);
		inferenceTask.setMultipleMatrixMode(true);
		TreeInferer inferer = inferenceTask.getTreeInferer();
		if (inferer!= null){
			inferer.setAlwaysPrepareForAnyMatrices(true);
			//inferer.setPlaceAllAnalysisFilesInSubdirectory(true);
		}
		String directoryPath = null;
		String basePath = null;
		String treeFileListPath = null;

		if (storageChoice == MULTIPLE_FILES){
			directoryPath = MesquiteFile.chooseDirectory("Where to save files?"); //MesquiteFile.saveFileAsDialog("Base name for files (files will be named <name>1.nex, <name>2.nex, etc.)", baseName);
			if (StringUtil.blank(directoryPath))
				return false;
			basePath = directoryPath + MesquiteFile.fileSeparator ; //+ baseName;
			treeFileListPath = StringUtil.getAllButLastItem(directoryPath, MesquiteFile.fileSeparator) + MesquiteFile.fileSeparator + "ListOfTreeFiles.txt";
		}


		Vector v = pauseAllPausables();
		long startTime = System.currentTimeMillis();
		int count = 0;
		int numFailed =0;
		String stringFailed = "";
		boolean stop = false;
		ProgressIndicator progIndicator = new ProgressIndicator(getProject(),"Tree inference on matrices", "", datas.size(), true);
		machine.parallelizer.setProgressIndicator(progIndicator);
		boolean userCancel = false;
		progIndicator.start();
		machine.setNumThreads(numThreads);
		machine.doCalcs(progIndicator);
		progIndicator.goAway();
		MesquiteThread.setQuietPlease(false);
		if (!userCancel) {
			//harvesttrees
			if (storageChoice == SINGLE_TREE_BLOCK) {  //accumulate into single tree block, and put in file
				TreeVector trees = new TreeVector(((CharacterData)datas.elementAt(0)).getTaxa());
				String annot = null;
				for (int i= 0; i< machine.treeBlocks.size(); i++){
					TreeVector tV = (TreeVector)machine.treeBlocks.elementAt(i);
					for (int it = 0; it<tV.size(); it++){
						trees.addElement(tV.elementAt(it), false);
						annot = tV.getAnnotation();
					}
				}
				trees.setName("Trees from matrices (" + inferenceTask.getName() + ")");
				trees.setAnnotation("Information for trees from last of the matrices analyzed: " + annot, false);
				trees.addToFile(getProject().getHomeFile(), getProject(), findElementManager(Tree.class));
			}
			else if (storageChoice == MULTIPLE_TREE_BLOCKS){
				for (int i= 0; i< machine.treeBlocks.size(); i++){
					TreeVector trees = new TreeVector(((CharacterData)datas.elementAt(0)).getTaxa());
					TreeVector tV = (TreeVector)machine.treeBlocks.elementAt(i);
					for (int it = 0; it<tV.size(); it++){
						trees.addElement(tV.elementAt(it), false);
					}
					trees.setAnnotation("Information for trees from last of the matrices analyzed: " + tV.getAnnotation(), false);
					trees.setName("Trees (" + inferenceTask.getName() + ") from matrix " + tV.getName());
					trees.addToFile(getProject().getHomeFile(), getProject(), findElementManager(Tree.class));
				}
			}
			else if (storageChoice == MULTIPLE_FILES){
				for (int i= 0; i< machine.treeBlocks.size(); i++){
					TreeVector tV = (TreeVector)machine.treeBlocks.elementAt(i);
					//SAVE TREE FILE 
					String fileName = tV.getName() + ".trees";
					MesquiteFile.putFileContents(basePath+fileName, "", false); //path, contents, ascii
					for (int it = 0; it<tV.size(); it++){
						Tree tree = tV.getTree(it);
						if (tree != null)
							MesquiteFile.appendFileContents(basePath+fileName, tree.writeTree() + "\n", false); //path, contents, ascii
						else
							System.err.println("Tree null " + i + " " +it);
					}
					MesquiteFile.appendFileContents(treeFileListPath, basePath+fileName + "\n", false); //path, contents, ascii
				}
			}

			if (numFailed > 0) {
				discreetAlert("Trees were not obtained for " + numFailed + " of the matrices. See log for details");
				logln("Trees were not obtained for these matrices:");
				logln(stringFailed);
			}
		}
		long totalTime = System.currentTimeMillis() - startTime;

		logln("\nTime used for parallel tree inferences: " + (totalTime/1000) +" seconds."); 
		unpauseAllPausables(v);

		if (getProject() != null)
			getProject().decrementProjectWindowSuppression();
		resetAllMenuBars();
		return true;
	}


	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return true;  
	}
	public void endJob() {
		super.endJob();
	}

}

/* ################################################ */
/* ################################################ */
class TreeInferenceParallelMachine implements Parallelizable {
	TreesFromSelMatricesParallel ownerModule;
	int numThreads = 1;
	Parallelizer parallelizer;
	ListableVector datas;
	ProgressIndicator progIndicator;
	public TreeInferenceParallelMachine (TreesFromSelMatricesParallel ownerModule, int numThreads) {
		this.ownerModule = ownerModule;
		this.numThreads = numThreads;
		parallelizer = new Parallelizer(this, numThreads);
		//parallelizer.setStopWithFirstItemFailure(true);
	}
	void setDatas(ListableVector datas){
		this.datas = datas;
	}
	void doCalcs(ProgressIndicator progressIndicator){
		progIndicator = progressIndicator;
		progIndicator.start();
		progIndicator.toFront();
		/*+++++++++++++++++++++++++++++++++++++++++++++++++++++*/
		int result = parallelizer.go();
		parallelizer.shutDown();
		/*+++++++++++++++++++++++++++++++++++++++++++++++++++++*/
	}
	void setNumThreads(int numThreads){
		parallelizer.setNumThreads(numThreads);
	}
	/*
	int getNumberOfMatrices(Taxa taxa){
		return ownerModule.getProject().getNumberCharMatrices(null, taxa, MolecularData.class, true);

	}
	MCharactersDistribution getMatrix(Taxa taxa, int im){
		if (im < getNumberOfMatrices(taxa)) {
			CharacterData data = ownerModule.getProject().getCharacterMatrixVisible(taxa, im, MolecularData.class);
			return data.getMCharactersDistribution();
		}
		return null;
	}

	/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
	/*\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\*/
	public int getTotalPossibleParallelItemCount() {
		return datas.size();
	}
	/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
	/*\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\*/
	public void markInappropriateItems(Parallelizer parallelizer) {
		int iN = 0;
		while (iN< datas.size()){
			if (!ownerModule.compatibleMatrix((CharacterData)datas.elementAt(iN)))
				parallelizer.setItemStatus(iN, Parallelizer.INAPPLICABLE);
			iN++;
		}
	}
	/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
	/*\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\*/
	public synchronized int getNextParallelItemAndReserve(Parallelizer parallelizer) {
		int iN = 0;
		while (iN< datas.size()){
			if (ownerModule.compatibleMatrix((CharacterData)datas.elementAt(iN)) && parallelizer.itemUncalculated(iN)){
				parallelizer.setItemStatus(iN, Parallelizer.BEINGCALCULATED);
				return iN;
			}
			iN++;
		}
		return -1;
	}
	/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
	/*\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\*/
	public ParallelParams doFirstCalculation_Parallel(int firstItem, Parallelizer parallelizer, MesquiteInteger resultCode) {

		ParallelParams pp = new ParallelParams();
		pp.responsibleEmployer = ownerModule;
		pp.employees = new MesquiteModule[]{(MesquiteModule)ownerModule.inferenceTask};
		pp.threadObjects = new Object[]{ ownerModule.matrixSourceTask};

		parallelizer.setItemStatus(firstItem, Parallelizer.BEINGCALCULATED);  //prob not necessary
		int result = doItemCalculation_Parallel(firstItem, pp, parallelizer);
		resultCode.setValue(result);
		if (result != ResultCodes.NO_ERROR)
			return null;
		ownerModule.logln("\nRemaining trees will be inferred quietly, without logging.");
		return pp;
	}

	/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
	/*\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\*/
	public ParallelParams cloneForParallel(ParallelParams params, Parallelizer parallelizer) {
		if (params == null)
			return null;
		ParallelParams pp = new ParallelParams();
		pp.responsibleEmployer = ownerModule;
		MesquiteModule mb = parallelizer.cloneEmployee(ownerModule, (MesquiteModule)ownerModule.inferenceTask, TreeSearcherFromMatrix.class);
		((TreeSearcherFromMatrix)mb).setMultipleMatrixMode(true);
		mesquite.lib.Commandable runner = (mesquite.lib.Commandable)mb.doCommand("getRunner", null, CommandChecker.defaultChecker); 
		runner.doCommand("forceNumProcessors", MesquiteInteger.toString(ownerModule.numCoresPerInference), CommandChecker.defaultChecker);
		runner.doCommand("showIntermediateTrees", "false", CommandChecker.defaultChecker);
		runner.doCommand("setVerbose", "false", CommandChecker.defaultChecker);
		runner.doCommand("optionsHaveBeenSet", "true", CommandChecker.defaultChecker);  
		mb.setUseMenubar(false); 
		ThreadListOfMatrices matrixSourceTask = new ThreadListOfMatrices(this);
		((TreeSearcherFromMatrix)mb).setMatrixSource(matrixSourceTask);		
		pp.employees = new MesquiteModule[]{mb};
		pp.threadObjects = new Object[]{matrixSourceTask};
		return pp; 
	}
	ListableVector treeBlocks = new ListableVector();
	/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
	/*\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\*/
	public int doItemCalculation_Parallel(int item, ParallelParams params, Parallelizer parallelizer) {
		ThreadListOfMatrices matrixSource = (ThreadListOfMatrices)params.threadObjects[0];
		matrixSource.setCurrentMatrix(item);
		MCharactersDistribution matrix = matrixSource.getCurrentMatrix((Taxa)null);
		Taxa taxa = matrix.getTaxa();
		TreeVector trees = new TreeVector(taxa);
		TreeSearcherFromMatrix inferenceTask = (TreeSearcherFromMatrix)params.employees[0];
		progIndicator.setText("\nInferring trees from matrix " +matrix.getName());

		inferenceTask.initialize(taxa);
		MesquiteThread.setHintToSuppressProgressIndicatorCurrentThread(true);
		MesquiteThread.setThreadMaxLogLevel(MesquiteMessage.HIGH_PRIORITY);
		int result = inferenceTask.fillTreeBlock(trees);
		trees.setName(matrix.getName());
		MesquiteThread.releaseThreadMaxLogLevel();
		MesquiteThread.setHintToSuppressProgressIndicatorCurrentThread(false);
		if (result !=ResultCodes.NO_ERROR)
			return result;
		for (int itr = 0; itr<trees.size(); itr++) { //multiple trees from same matrix; number trees .#1, 2, 3
			String num = "";
			if (trees.size()>1)
				num = ".#" + (itr + 1);
			Tree t = trees.getTree(itr);
			if (t instanceof MesquiteTree){
				MesquiteTree tM = (MesquiteTree)t;
				tM.setName(matrix.getName() + num + ".tree");
				tM.attach(new MesquiteString("fromMatrix", matrix.getName()));
			}
		}
		ownerModule.log(".");
		treeBlocks.addElement(trees, false);
		progIndicator.setText("\nTrees inferred from matrix " +matrix.getName());
		int tot = parallelizer.getTotalCalculated();
		if (tot% 100 == 0)
			progIndicator.toFront();
		progIndicator.setCurrentValue(tot);
		return ResultCodes.NO_ERROR;
	}

	public boolean pleaseReuseParallelThreads() { //doesn't really matter here, becuase not persistent
		return true;
	}
}

/* ################################################ */
class ThreadListOfMatrices extends MatrixSourceCoord  {
	TreeInferenceParallelMachine machine;
	Taxa taxa = null;
	public ThreadListOfMatrices(TreeInferenceParallelMachine machine) {
		this.machine = machine;
	}
	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
  	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Taxa taxa) {
		this.taxa = taxa;
	}
	int current = 0;
	void setCurrentMatrix(int im){
		current = im;
	}
	/** gets the current matrix.*/
	public MCharactersDistribution getCurrentMatrix(Taxa taxa) {
		CharacterData data = (CharacterData) machine.datas.elementAt(current);
		return data.getMCharactersDistribution();
	}

	public void initialize(Tree tree){
		if (tree==null) return;
		else initialize(tree.getTaxa());
	}

	public boolean usesTree() {
		return false;
	}
	public MCharactersDistribution getCurrentMatrix(Tree tree){
		if (tree==null) return null;
		else return getCurrentMatrix(tree.getTaxa());
	}
	/*.................................................................................................................*/
	public  String getCurrentMatrixName(Taxa taxa) {
		return "";
	}

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}

	public String getName() {
		return null;
	}

}

