/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.treefarm.MRPMatrices;
/*~~  */

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;

/** 

 *new in 1.02*


/* ======================================================================== */
public class MRPMatrices extends CharMatrixSource {
	public String getName() {
		return "MRP Matrices from Trees";
	}
	public String getExplanation() {
		return "Supplies matrices which represent trees for MRP (Matrix Representation with Parsimony) supertree analyses.";
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(TreeSource.class, getName() + "  needs a source of trees from which to make MRP matrices.",
		"The source of trees can be chosen initially or in the Tree Source for MRP submenu");
	}
	CharacterDistribution states;
	MesquiteLong seed;
	TreeSource treeTask;
	MesquiteString treeTaskName;
	Taxa lastTaxa;	
	MesquiteCommand stC, ttC;
	boolean numTreesSet = false;
	int numTrees = 100;
	boolean initialized = false;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		ttC =  makeCommand("setTreeSource",  this);

		treeTask= (TreeSource)hireEmployee(TreeSource.class, "Source of trees from which to make MRP matrices");
		if (treeTask == null) {
			return sorry(getName() + " can't start because not appropiate tree source module was obtained");
		}
		treeTaskName = new MesquiteString();
		treeTaskName.setValue(treeTask.getName());
		if (numModulesAvailable(TreeSource.class)>1) {
			MesquiteSubmenuSpec mss = addSubmenu(null, "Tree Source for MRP", ttC);
			mss.setSelected(treeTaskName);
			mss.setList(TreeSource.class);
		}
		treeTask.setHiringCommand(ttC);

		return true; 
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	public void employeeQuit(MesquiteModule m){
		if (m==treeTask)
			iQuit();
	}

	/*.................................................................................................................*/
	public void endJob(){
		if (lastTaxa!=null)
			lastTaxa.removeListener(this);
		super.endJob();
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public void disposing(Object obj){
		if (obj instanceof Taxa && (Taxa)obj == lastTaxa) {
			iQuit();
		}
	}
	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		if (!MesquiteThread.isScripting() && (employee != treeTask || Notification.getCode(notification) != MesquiteListener.SELECTION_CHANGED))
			super.employeeParametersChanged(employee, source, notification);
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		if (MesquiteInteger.isCombinable(numTrees))
			temp.addLine("setNumTrees " + numTrees);
		temp.addLine("setTreeSource ", treeTask);
		return temp;
	}
	/*.................................................................................................................*/
	MesquiteInteger pos = new MesquiteInteger(0);
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the module used to supply trees for matrix simulation", "[name of module]", commandName, "setTreeSource")) {
			TreeSource temp=  (TreeSource)replaceEmployee(TreeSource.class, arguments, "Source of trees on which to simulate matrices", treeTask);
			if (temp!=null) {
				treeTask = temp;
				treeTask.setHiringCommand(ttC);
				treeTaskName.setValue(treeTask.getName());
				if (!MesquiteThread.isScripting())
					parametersChanged(); 
			}
			return temp;
		}
		else if (checker.compare(this.getClass(), "Sets the number of trees (if indefinite number of trees allowed)", "[number of trees]", commandName, "setNumTrees")) {
			int newNum= MesquiteInteger.fromFirstToken(arguments, pos);
			if (!MesquiteInteger.isCombinable(newNum))
				newNum = MesquiteInteger.queryInteger(containerOfModule(), "Set number of trees", "Number of trees used to make MRP matrix:", numTrees);
			if (MesquiteInteger.isCombinable(newNum) && newNum>0 && newNum<1000000 && newNum!=numTrees) {
				numTrees=newNum;
				numTreesSet = true;
				if (!MesquiteThread.isScripting())
					parametersChanged(); 
			}
		}
		else if (checker.compare(this.getClass(), "Sets the block of taxa used", "[block number]", commandName, "setTaxa")) {
			int setNumber = MesquiteInteger.fromFirstToken(arguments, pos);
			if (lastTaxa!=null)
				lastTaxa.removeListener(this);
			lastTaxa = getProject().getTaxa(checker.getFile(), setNumber);
			if (lastTaxa!=null)
				lastTaxa.addListener(this);
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	public void initialize(Taxa taxa){
		treeTask.initialize(taxa);
		initialized = true;
		if (!numTreesSet){
			int nT = treeTask.getNumberOfTrees(taxa); 
			if (!MesquiteThread.isScripting() && !MesquiteInteger.isCombinable(nT)){

				int tempNumTrees = MesquiteInteger.queryInteger(containerOfModule(), "Number of trees", "Number of trees with which to make MRP matrix:", numTrees, 1, 1000000, false);

				if (MesquiteInteger.isCombinable(tempNumTrees)) {
					numTrees = tempNumTrees;
					numTreesSet = true;
				}
				if (!MesquiteInteger.isCombinable(numTrees))
					numTrees = 100;
			}
			else if (!MesquiteInteger.isCombinable(numTrees))
				numTrees = 100;
		}
	}
	/*.................................................................................................................*/
	void assignCharacters(Tree tree, int node, int[] allTermsInTree, CategoricalData data){
		if (tree.nodeIsInternal(node)) { 
			if (node != tree.getRoot()){
				int ic = data.getNumChars()-1;
				data.addParts(ic, 1);
				ic++;
				int[] terms = tree.getTerminalTaxa(node);
				for (int it = 0; it<allTermsInTree.length; it++)
					data.setState(ic, allTermsInTree[it], 1L);  //state 0 to all in tree
				for (int iti = 0; iti<terms.length; iti++)
					data.setState(ic, terms[iti], 2L);  //state 1
			}
			for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter)) {
				assignCharacters(tree, daughter, allTermsInTree, data);
			}
		}
	}
	/*.................................................................................................................*/
	private MCharactersDistribution getM(Taxa taxa){
		if (treeTask == null) {
			System.out.println("Tree task null");
			return null;
		}
		else if (taxa==null){
			System.out.println("taxa null");
			return null;
		}
		if (!initialized)
			initialize(taxa);
		int useNumTrees = treeTask.getNumberOfTrees(taxa);
		if (!MesquiteInteger.isCombinable(useNumTrees))
			useNumTrees = numTrees;
		CategoricalData data = new CategoricalData(null, taxa.getNumTaxa(), 0, taxa);
		data.addParts(-1, 1);


		for (int it = 0; it<taxa.getNumTaxa(); it++)
			data.setToUnassigned(0, it);  //changed 2. 01
			//data.setState(0, it, 2L);  //state 1 to all as first dummy character


		for (int i=0; i<useNumTrees; i++){
			Tree tree = treeTask.getTree(taxa, i);
			int[] terms = tree.getTerminalTaxa(tree.getRoot());
			assignCharacters(tree, tree.getRoot(), terms, data);
		}
		
		data.removeCharactersThatAreEntirelyUnassigned(false);  //added 2. 01

		MCategoricalAdjustable matrix = new MCategoricalAdjustable(taxa, data.getNumChars(), data.getNumTaxa());
		for (int ic = 0; ic<data.getNumChars(); ic++)
			matrix.transferFrom(ic, data.getCharacterDistribution(ic));
		String n = "MRP Matrix from " + useNumTrees + " tree";
		if (useNumTrees !=1)
			n += "s";
		matrix.setName(n);
		data.setAnnotation(accumulateParameters(" "), false);
		

		return matrix;
	}
	/*.................................................................................................................*/
	public String getMatrixName(Taxa taxa, int ic) {
		return "MRP Matrix from trees from " + treeTask.getName() + " (#" + (ic+1) + ")";
	}
	/*.................................................................................................................*/
	public MCharactersDistribution getCurrentMatrix(Taxa taxa){
		return getM(taxa);
	}
	/*.................................................................................................................*/
	public MCharactersDistribution getMatrix(Taxa taxa, int im){
		return getM(taxa);
	}
	/*.................................................................................................................*/
	public  int getNumberOfMatrices(Taxa taxa){
		return 1;
	}
	/*.................................................................................................................*/
	/** returns the number of the current matrix*/
	public int getNumberCurrentMatrix(){
		return 0;
	}
	/*.................................................................................................................*/
	public String getParameters() {
		String s = "MRP Matrix from trees from " + treeTask.getName();
		return s;
	}

	/*.................................................................................................................*/
	public boolean showCitation(){
		return true;
	}
	/*.................................................................................................................*/
}


