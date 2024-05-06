/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.genomic.TreesFromSelMatrices;
/* created May 02 */

import mesquite.lists.lib.*;

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.duties.MatrixSourceCoord;
import mesquite.lib.table.*;

/* ======================================================================== */
public class TreesFromSelMatrices extends DatasetsListUtility {

	/*.................................................................................................................*/
	public String getName() {
		return "Infer Trees from Selected Matrices";
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Trees from Selected Matrices...";
	}

	public String getExplanation() {
		return "Infers trees for each of the selected matrices, and puts into a single tree block." ;
	}
	TreeSearcher inferenceTask;
	MatrixSourceCoord matrixSourceTask;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		inferenceTask = (TreeSearcher)hireCompatibleEmployee(TreeSearcher.class, "acceptImposedMatrixSource", "Tree inference method");
		matrixSourceTask = new MyListOfMatrices(this);
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
		TreeVector trees = new TreeVector(((CharacterData)datas.elementAt(0)).getTaxa());
		Vector v = pauseAllPausables();
		int count = 0;
		int numFailed =0;
		String stringFailed = "";
		boolean stop = false;
		for (int im = 0; im < datas.size() && !stop; im++){
			CharacterData data = (CharacterData)datas.elementAt(im);
			if (compatibleMatrix(data)) {
				currentMatrix = data.getMCharactersDistribution();
				int lastNumTrees = trees.size();
				logln("Inferring trees from matrix " +data.getName()); 
				inferenceTask.fillTreeBlock(trees);
				if (trees.size() == lastNumTrees) {
					numFailed++;
					stringFailed += "\t" + data.getName() + "\n";
					if (AlertDialog.query(MesquiteTrunk.mesquiteTrunk.containerOfModule(), "Stop?", "Do you want to stop the tree inferences?", "Stop", "Continue", 0)) {
						stop = true;
					}
					MesquiteThread.setQuietPlease(true);
				}
				boolean mult = false;
				if (trees.size()-lastNumTrees>1)
					mult = true;
				for (int itr = lastNumTrees; itr<trees.size(); itr++) {
					String num = "";
					if (mult)
						num = "." + (lastNumTrees - itr + 1);
					Tree t = trees.getTree(itr);
					count++;
					if (t instanceof MesquiteTree)
						((MesquiteTree)trees.getTree(itr)).setName(data.getName() + num);
				}
			}
			else
				logln("Tree not inferred from matrix " +data.getName() + " because it is of a data type incompatible with the tree inference method"); 
		}
		MesquiteThread.setQuietPlease(false);
		trees.setName("Trees from matrices (" + inferenceTask.getName() + ")");
		String annot = trees.getAnnotation();
		trees.setAnnotation("Information for trees from last of the matrices analyzed: " + annot, false);
		trees.addToFile(getProject().getHomeFile(), getProject(), findElementManager(Tree.class));
		logln("Total matrices analyzed: " + count);
		if (numFailed > 0) {
			discreetAlert("Trees were not obtained for " + numFailed + " of the matrices. See log for details");
			logln("Trees were not obtained for these matrices:");
			logln(stringFailed);
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
	TreesFromSelMatrices owner;
	Taxa taxa = null;
	public MyListOfMatrices(TreesFromSelMatrices owner) {
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
