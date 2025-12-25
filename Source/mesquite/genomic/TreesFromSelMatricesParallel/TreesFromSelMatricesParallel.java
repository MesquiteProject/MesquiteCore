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

import java.util.Vector;

import mesquite.categ.lib.MolecularData;
import mesquite.lib.Bits;
import mesquite.lib.CommandChecker;
import mesquite.lib.IntegerField;
import mesquite.lib.ListableVector;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteInteger;
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
import mesquite.lists.lib.CharMatricesListUtility;

/* ======================================================================== */
public class TreesFromSelMatricesParallel extends CharMatricesListUtility {

	/*.................................................................................................................*/
	public String getName() {
		return "Infer Trees from Matrices (Parallel)";
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Trees from Matrices (Parallel)...";
	}

	public String getExplanation() {
		return "Infers trees for each of the matrices, and compiles them into a single tree block. Parallelized." ;
	}
	TreeSearcherFromMatrix inferenceTask;
	ThreadListOfMatrices matrixSourceTask;
	TreeInferenceParallelMachine machine;
	int numThreads = 1;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		inferenceTask = (TreeSearcherFromMatrix)hireCompatibleEmployee(TreeSearcherFromMatrix.class, "acceptImposedMatrixSource", "Tree inference method");
		machine = new TreeInferenceParallelMachine(this, 1);
		matrixSourceTask = new ThreadListOfMatrices(machine);
		if (inferenceTask == null || matrixSourceTask == null)
			return false;
		inferenceTask.setMatrixSource(matrixSourceTask);
		queryOptions();
		return true;
	}
	/* ................................................................................................................. */
	public boolean queryOptions() {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog queryDialog = new ExtensibleDialog(containerOfModule(), "Number of Parallel Tree Inferences", buttonPressed);
		queryDialog.addLargeOrSmallTextLabel("The tree inferences will be performed in parallel, on several threads. Choose the number of parallel threads according to your computer's multiprocessing capabilities.");
		IntegerField integerField = queryDialog.addIntegerField("Number of threads", numThreads, 20, 1, 255);
		queryDialog.addLargeOrSmallTextLabel("(Note: the first matrix will be processed alone, and then the others in parallel.)");
		queryDialog.setDefaultTextComponent(integerField.getTextField());
		queryDialog.setDefaultComponent(integerField.getTextField());

		queryDialog.completeAndShowDialog(true);

		boolean OK = buttonPressed.getValue() == 0;
		if (OK) {
			if (!integerField.isValidInteger()) {
				alert("The number of threads must be a valid integer.");
				OK = false;
			}
			else {
				int temp = integerField.getValue();
				if (MesquiteInteger.isCombinable(temp) && temp > 0 && temp < 256) {
					numThreads = temp;
					storePreferences();
				}
				else {
					alert("The number of threads must be between 1 and 255.");
					OK = false;
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
		inferenceTask.doCommand("numProcessors", "1", CommandChecker.defaultChecker);
		inferenceTask.initialize(taxa);
		inferenceTask.setMultipleMatrixMode(true);
		TreeInferer inferer = inferenceTask.getTreeInferer();
		if (inferer!= null){
			inferer.setAlwaysPrepareForAnyMatrices(true);
			//inferer.setPlaceAllAnalysisFilesInSubdirectory(true);
		}
		TreeVector trees = new TreeVector(((CharacterData)datas.elementAt(0)).getTaxa());
		Vector v = pauseAllPausables();
		int count = 0;
		int numFailed =0;
		String stringFailed = "";
		boolean stop = false;
		ProgressIndicator progIndicator = new ProgressIndicator(getProject(),"Tree inference on matrices", "", datas.size(), true);
		boolean userCancel = false;
		progIndicator.start();
		machine.setNumThreads(numThreads);
		machine.doCalcs();
		progIndicator.goAway();
		MesquiteThread.setQuietPlease(false);
		if (!userCancel) {
			System.err.println("@  " + machine.treeBlocks.size());
			//harvesttrees
			for (int i= 0; i< machine.treeBlocks.size(); i++){
				TreeVector tV = (TreeVector)machine.treeBlocks.elementAt(i);
				for (int it = 0; it<tV.size(); it++){
					//System.err.println("@  " + tV.elementAt(it));
					trees.addElement(tV.elementAt(it), false);
				}
			}
			trees.setName("@PARALLEL Trees from matrices (" + inferenceTask.getName() + ")");
			String annot = trees.getAnnotation();
			trees.setAnnotation("Information for trees from last of the matrices analyzed: " + annot, false);
			trees.addToFile(getProject().getHomeFile(), getProject(), findElementManager(Tree.class));
			//logln("Total matrices analyzed: " + count);
			if (numFailed > 0) {
				discreetAlert("Trees were not obtained for " + numFailed + " of the matrices. See log for details");
				logln("Trees were not obtained for these matrices:");
				logln(stringFailed);
			}
		}
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
	public TreeInferenceParallelMachine (TreesFromSelMatricesParallel ownerModule, int numThreads) {
		this.ownerModule = ownerModule;
		this.numThreads = numThreads;
		parallelizer = new Parallelizer(this, numThreads);
	}
	void setDatas(ListableVector datas){
		this.datas = datas;
	}
	void doCalcs(){
		/*+++++++++++++++++++++++++++++++++++++++++++++++++++++*/
		parallelizer.go();
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
	public void markInappropriateItems() {
			int iN = 0;
			while (iN< datas.size()){
				if (!ownerModule.compatibleMatrix((CharacterData)datas.elementAt(iN)))
					parallelizer.setItemStatus(iN, Parallelizer.INAPPLICABLE);
				iN++;
			}
	}
	/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
	/*\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\*/
	public synchronized int getNextParallelItemAndReserve() {
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
	public ParallelParams doFirstCalculation_Parallel(int firstItem) {

		ParallelParams pp = new ParallelParams();
		pp.responsibleEmployer = ownerModule;
		pp.employees = new MesquiteModule[]{(MesquiteModule)ownerModule.inferenceTask};
		pp.threadObjects = new Object[]{ ownerModule.matrixSourceTask};

		parallelizer.setItemStatus(firstItem, Parallelizer.BEINGCALCULATED);  //prob not necessary
		int result = doItemCalculation_Parallel(firstItem, pp);
		return pp;
	}

	/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
	/*\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\=\*/
	public ParallelParams cloneForParallel(ParallelParams params) {
		if (params == null)
			return null;
		ParallelParams pp = new ParallelParams();
		pp.responsibleEmployer = ownerModule;
		MesquiteModule mb = parallelizer.cloneEmployee(ownerModule, (MesquiteModule)ownerModule.inferenceTask, TreeSearcherFromMatrix.class);
		((TreeSearcherFromMatrix)mb).setMultipleMatrixMode(true);
		mesquite.lib.Commandable runner = (mesquite.lib.Commandable)mb.doCommand("getRunner", null, CommandChecker.defaultChecker); 
		runner.doCommand("numProcessors", "1", CommandChecker.defaultChecker);
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
	public int doItemCalculation_Parallel(int item, ParallelParams params) {
		ThreadListOfMatrices matrixSource = (ThreadListOfMatrices)params.threadObjects[0];
		matrixSource.setCurrentMatrix(item);
		MCharactersDistribution matrix = matrixSource.getCurrentMatrix((Taxa)null);
		Taxa taxa = matrix.getTaxa();
		TreeVector trees = new TreeVector(taxa);
		TreeSearcherFromMatrix inferenceTask = (TreeSearcherFromMatrix)params.employees[0];

		inferenceTask.initialize(taxa);
		MesquiteThread.setHintToSuppressProgressIndicatorCurrentThread(true);
		int result = inferenceTask.fillTreeBlock(trees);
		MesquiteThread.setHintToSuppressProgressIndicatorCurrentThread(false);
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
	treeBlocks.addElement(trees, false);
		return 0;
	}

	public boolean pleaseReuseParallelThreads() {
		return false;
	}
}

/* ################################################ */
class ThreadListOfMatrices extends MatrixSourceCoord  {
	TreeInferenceParallelMachine machine;
	Taxa taxa = null;
	public ThreadListOfMatrices(TreeInferenceParallelMachine parallelizer) {
		this.machine = parallelizer;
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

