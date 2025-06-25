/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.genomic.TreezBlocksFromMatrices;
/* created May 02 */

import java.util.Vector;

import mesquite.lib.ListableVector;
import mesquite.lib.MesquiteString;
import mesquite.lib.MesquiteThread;
import mesquite.lib.ResultCodes;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.MCharactersDistribution;
import mesquite.lib.duties.MatrixSourceCoord;
import mesquite.lib.duties.TreeInferer;
import mesquite.lib.duties.TreeSearcherFromMatrix;
import mesquite.lib.table.MesquiteTable;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeVector;
import mesquite.lib.ui.ProgressIndicator;
import mesquite.lists.lib.CharMatricesListUtility;

/* ======================================================================== */
public class TreezBlocksFromMatrices extends CharMatricesListUtility {
	//Name with z to make it sort after TreesFromSelMatrices

	/*.................................................................................................................*/
	public String getName() {
		return "Infer Tree Blocks from Matrices";
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Tree Blocks from Matrices...";
	}

	public String getExplanation() {
		return "Infers trees and puts them into a tree block for each of the matrices." ;
	}
	TreeSearcherFromMatrix inferenceTask;
	MatrixSourceCoord matrixSourceTask;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		inferenceTask = (TreeSearcherFromMatrix)hireCompatibleEmployee(TreeSearcherFromMatrix.class, "acceptImposedMatrixSource", "Tree inference method");
		matrixSourceTask = new MyListOfMatrices(this);
		if (inferenceTask == null || matrixSourceTask == null)
			return false;
		inferenceTask.setMatrixSource(matrixSourceTask);
		return true;
	}

	/** if returns true, then requests to remain on even after operateOnTaxas is called.  Default is false*/
	public boolean pleaseLeaveMeOn(){
		return false;
	}

	MCharactersDistribution currentMatrix = null;
	public MCharactersDistribution getCurrentMatrix(Taxa taxa) {
		return currentMatrix;
	}

	boolean compatibleMatrix(CharacterData data) {
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
		inferenceTask.initialize(taxa);
		TreeInferer inferer = inferenceTask.getTreeInferer();
		if (inferer!= null){
			inferer.setAlwaysPrepareForAnyMatrices(true);
			inferer.setPlaceAllAnalysisFilesInSubdirectory(true);
	}
		Vector v = pauseAllPausables();
		int count = 0;
		int numFailed =0;
		String stringFailed = "";
		boolean stop = false;
		ProgressIndicator progIndicator = new ProgressIndicator(getProject(),"Tree inference on matrices", "", datas.size(), true);
		boolean userCancel = false;
		progIndicator.start();
		for (int im = 0; im < datas.size() && !stop; im++){
			if (progIndicator.isAborted())
				stop = true;
			CharacterData data = (CharacterData)datas.elementAt(im);
			if (compatibleMatrix(data)) {
				TreeVector trees = new TreeVector(((CharacterData)datas.elementAt(0)).getTaxa());
				currentMatrix = data.getMCharactersDistribution();
				logln("\nInferring trees from matrix #" +(im+1) + " (" + data.getName() + ")"); 
				progIndicator.setText("Inferring trees from matrix " +data.getName());
				MesquiteThread.setHintToSuppressProgressIndicatorCurrentThread(true);
				int result = inferenceTask.fillTreeBlock(trees);
				MesquiteThread.setHintToSuppressProgressIndicatorCurrentThread(false);
				if (result == ResultCodes.USERCANCELONINITIALIZE) {
					logln("User cancelled the analyses."); 
					userCancel=true;
					stop = true;
				}
				else {
					progIndicator.increment();
					if (im == 0)
						progIndicator.toFront();
					if (trees.size() == 0) {
						numFailed++;
						stringFailed += "\t" + data.getName() + "\n";
						logln("Trees not inferred from matrix " +data.getName() + " because of some issue with the matrix or the inference program."); 
						//if (AlertDialog.query(MesquiteTrunk.mesquiteTrunk.containerOfModule(), "Stop?", "Do you want to stop the tree inferences?", "Stop", "Continue", 0)) {
						//	stop = true;
						//}
						MesquiteThread.setQuietPlease(true);
					}
					else {
						boolean mult = false;
						if (trees.size()>1)
							mult = true;
						for (int itr = 0; itr<trees.size(); itr++) { 
							String num = "";
							if (mult)
								num = ".#" + (itr+1);
							Tree t = trees.getTree(itr);
							count++;
							if (t instanceof MesquiteTree){
								MesquiteTree tM = (MesquiteTree)t;
								tM.setName(data.getName() + num + ".tree");
								tM.attach(new MesquiteString("fromMatrix", data.getName()));
							}
						}
						trees.setName("Trees (" + inferenceTask.getName() + ") from matrix " + data.getName());
						trees.addToFile(getProject().getHomeFile(), getProject(), findElementManager(Tree.class));
					}
				}
			}
			else
				logln("Trees not inferred from matrix " +data.getName() + " because it is of a data type incompatible with the tree inference method"); 

		}
		progIndicator.goAway();
		MesquiteThread.setQuietPlease(false);
		if (!userCancel) {logln("Total matrices analyzed: " + count);
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

class MyListOfMatrices extends MatrixSourceCoord  {
	TreezBlocksFromMatrices owner;
	Taxa taxa = null;
	
	public MyListOfMatrices(TreezBlocksFromMatrices owner) {
		this.owner = owner;
	}
	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
  	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Taxa taxa) {
		this.taxa = taxa;
	}

	/** gets the current matrix.*/
	public MCharactersDistribution getCurrentMatrix(Taxa taxa) {
		return owner.getCurrentMatrix(taxa);
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

