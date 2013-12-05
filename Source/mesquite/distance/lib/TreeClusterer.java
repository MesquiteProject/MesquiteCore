/* Mesquite source code.  Copyright 1997-2010 W. Maddison and D. Maddison.
Version 2.74, October 2010.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.distance.lib;

import java.awt.*;
import java.util.*;
import mesquite.lib.duties.*;
import mesquite.lib.*;


/* ======================================================================== */
/** Clusters to make a tree from a distance matrix for taxa.*/
public abstract class TreeClusterer extends MesquiteModule {
	MesquiteBoolean followTies;
	int MAXTREES = 100; 
   	 public Class getDutyClass() {
   	 	return TreeClusterer.class;
   	 }
   	 
	/*.................................................................................................................*/
	/** superStartJob is called automatically when an employee is hired.  This is intended for use by superclasses of modules that need
	their own constructor-like call, without relying on the subclass to be polite enough to call super.startJob().*/
	public boolean superStartJob(String arguments, Object condition, boolean hiredByName){
		followTies = new MesquiteBoolean(true); //this is in preparation for UI allowing choice of whether to follow ties
  		if (!MesquiteThread.isScripting()){
	    	 	int s = MesquiteInteger.queryInteger(containerOfModule(), "MAXTREES", "Maximum number of equally good trees to store during clustering (MAXTREES)", MAXTREES);
 	 		if (MesquiteInteger.isCombinable(s))
 	 			MAXTREES = s;
 	 		else
 				return false;
  		}
		return true;
	}
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) {
   	 	Snapshot temp = new Snapshot();
  	 	temp.addLine("setMAXTREES " + MAXTREES);
  	 	return temp;
  	 }
  	 MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Sets the maximum number of trees stored", "[number]", commandName, "setMAXTREES")) {
    	 		int s = MesquiteInteger.fromString(parser.getFirstToken(arguments));
    	 		if (!MesquiteInteger.isCombinable(s)){
    	 			s = MesquiteInteger.queryInteger(containerOfModule(), "MAXTREES", "Maximum number of equally good trees to store during clustering (MAXTREES)", MAXTREES);
    	 		}
    	 		if (MesquiteInteger.isCombinable(s)){
    	 			MAXTREES = s;
 				if (!MesquiteThread.isScripting())
 					parametersChanged(); 
 			}
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
		return null;
   	 }
   	 
	/*.................................................................................................................*/
	
   	public abstract double getDistanceBetweenClusters(double[][] distanceMatrix, int[] clusterI, int[] clusterJ);
	/*.................................................................................................................*/
	public void getTrees(Taxa taxa, TaxaDistance distances, TreeVector trees){
		if (followTies.getValue() && MAXTREES > 1)
			getTreesTies(taxa, distances, trees);
		else
			getTreesSingle(taxa, distances, trees);
	}
	
	public void getTreesSingle(Taxa taxa, TaxaDistance distances, TreeVector trees){
  		if (taxa==null || distances==null || trees == null)
  			return;
   		int numTaxa = taxa.getNumTaxa();
		MesquiteTree tree = new MesquiteTree(taxa);
		// start with default bush
		tree.setToDefaultBush(numTaxa, false);
		
		// make double[][] distanceMatrix, for each of daughters of root
		double[][] originalMatrix = distances.getMatrix();
		if (originalMatrix == null)
			return;
		// make double[][] distanceMatrix, for each of daughters of root
		double[][] matrix = new double[originalMatrix.length][originalMatrix[0].length];
		for (int i=0; i<numTaxa; i++){
			for (int j=0; j<numTaxa; j++){
				matrix[i][j] = originalMatrix[i][j];
			}
		}
		
		double bestDist;
		boolean done = false;
		for (int joining = 1; joining<numTaxa-1 && !done; joining++) {
			CommandRecord.tick("Clustering by " + getName() + ": Joining taxon " + joining);
			logln("Clustering by " + getName() + ": Joining taxon " + joining);
			bestDist = MesquiteDouble.unassigned;
			int pairI=MesquiteInteger.unassigned;
			int pairJ=MesquiteInteger.unassigned;

			// find two most similar daughters of root; in case of tie, first pair found is used
			for (int i=0; i<numTaxa-joining+1; i++) //numTaxa-joining+1 is number of daughters remaining
				for (int j=i+1; j<numTaxa-joining+1; j++) {
					double d = matrix[i][j];
					if (MesquiteDouble.lessThan(d, bestDist, 0)) {
						bestDist = d;
						pairI = i;
						pairJ = j;
					}
					else if (true && MesquiteDouble.equals(d, bestDist, 0)){
						pairI = i;
						pairJ = j;
					}
				}
			if (!MesquiteInteger.isCombinable(pairI) || !MesquiteInteger.isCombinable(pairJ))
				done=true;
			else {	
				// unite two daughters
				tree.moveBranch(tree.indexedDaughterOfNode(tree.getRoot(), pairI), tree.indexedDaughterOfNode(tree.getRoot(), pairJ), false);
				
				for (int i=0; i<numTaxa; i++){
					for (int j=0; j<numTaxa; j++){
						matrix[i][j] = MesquiteDouble.impossible;
					}
				}
				int numDaughtersOfRoot = numTaxa-joining;
				int[][] clusters = new int[numDaughtersOfRoot][];
 				for (int i=0; i<numDaughtersOfRoot; i++){
					clusters[i] = tree.getTerminalTaxa(tree.indexedDaughterOfNode(tree.getRoot(), i));
				}
				for (int i=0; i<numDaughtersOfRoot; i++){
					for (int j=0; j<numDaughtersOfRoot; j++){
						matrix[i][j] = getDistanceBetweenClusters(originalMatrix, clusters[i], clusters[j]);
					}
				}

					
			}
		}
  		
		tree.setName(getName() + " tree");
		//tree.setAnnotation(distances.getAnnotation(), false);
		trees.addElement(tree, false);
  	}
	/*.................................................................................................................*/
	public void getTreesTies(Taxa taxa, TaxaDistance distances, TreeVector trees){
  		if (taxa==null || distances==null || trees == null)
  			return;
   		int numTaxa = taxa.getNumTaxa();
		MesquiteTree tree = new MesquiteTree(taxa);
		// start with default bush
		tree.setToDefaultBush(numTaxa, false);
		
		// make double[][] distanceMatrix, for each of daughters of root
		double[][] originalMatrix = distances.getMatrix();
		if (originalMatrix == null)
			return;
		// make double[][] distanceMatrix, for each of daughters of root
		double[][] matrix = new double[originalMatrix.length][originalMatrix[0].length];
		for (int i=0; i<numTaxa; i++){
			for (int j=0; j<numTaxa; j++){
				matrix[i][j] = originalMatrix[i][j];
			}
		}
		countTrees = 0;
		countPaths = 0;
		warned = false;
		ProgressIndicator progIndicator = new ProgressIndicator(getProject(),"Cluster Analysis ", "Clustering in progress", 0, "Stop Clustering");
		progIndicator.setButtonMode(ProgressIndicator.FLAG_AND_HIDE);
		progIndicator.setText("Cluster Analysis (" + getName() + ") in progress ");
		progIndicator.setTertiaryMessage(getParameters());
		progIndicator.start();
		followTrees(tree, trees, matrix, originalMatrix, 1, numTaxa, 1, progIndicator);
		if (progIndicator.isAborted()) {
			trees.removeAllElements(false);
			discreetAlert( "Cluster Analysis was stopped.  No trees will result.");
		}
		progIndicator.goAway();
  	}
	/*.................................................................................................................*/
  	int countTrees;
  	int countPaths;
  	boolean warned = false;
  	//basic approach is to start with unresolved bush, then continuing to join daughters of the root until only two remain
  	void followTrees(MesquiteTree tree, TreeVector trees, double matrix[][], double originalMatrix[][], int joining, int numTaxa, int path, ProgressIndicator progIndicator){
		if (joining >= numTaxa-1 || trees.size()>=MAXTREES || progIndicator.isAborted())
			return;
		//pass trees and initial tree to recursive prcedure that passes new matrices even deeper, cloning trees as needed
		double bestDist = MesquiteDouble.unassigned;
		boolean done = false;
		progIndicator.setSecondaryMessage("Clustering by " + getName() + ": Joining taxon " + joining + "; working on cluster path #" + path);
		
		int[] pairI = new int[numTaxa*numTaxa];
		int[] pairJ = new int[numTaxa*numTaxa];
		IntegerArray.deassignArray(pairI);
		IntegerArray.deassignArray(pairJ);
		int countPairs = 0;
		int numDaughtersOfRoot = numTaxa-joining + 1;
		// find two most similar daughters of root; in case of tie, first pair found is used
		for (int i=0; i<numDaughtersOfRoot; i++) //
			for (int j=i+1; j<numDaughtersOfRoot; j++) {
				double d = matrix[i][j];
				if (MesquiteDouble.isCombinable(d)){
					if (MesquiteDouble.lessThan(d, bestDist, 0)) {
						countPairs = 0;
						bestDist = d;
						pairI[countPairs] = i;
						pairJ[countPairs] = j;
						countPairs++;
					}
					else if (MesquiteDouble.equals(d, bestDist, 0)){
						pairI[countPairs] = i;
						pairJ[countPairs] = j;
						countPairs++;
					}
				}
			}
		if (countPairs == 0 || !MesquiteInteger.isCombinable(pairI[0]) || !MesquiteInteger.isCombinable(pairJ[0]))
			return;
		else {	
			numDaughtersOfRoot--;  //this will be number once branch moved
			for (int tie = 0; tie<countPairs && !progIndicator.isAborted(); tie++){
				int thisPath = path;
				if (tie>0) {
					countPaths++;
					thisPath = countPaths;
				}
				if (trees.size()>=MAXTREES) {
					if (!warned){
						discreetAlert( "MAXTREES of " + MAXTREES + " reached in Cluster Analysis");
					}
					warned = true;
					return;
				}
				MesquiteTree tieTree = tree;
				if (countPairs>1)
					tieTree = tree.cloneTree(); //need to make copy of tree if search needs to fork
				
				// unite two daughters
				tieTree.moveBranch(tieTree.indexedDaughterOfNode(tieTree.getRoot(), pairI[tie]), tieTree.indexedDaughterOfNode(tieTree.getRoot(), pairJ[tie]), false);
				if (numDaughtersOfRoot== 2) {
					tieTree.standardize(tieTree.getRoot(), false); //standardizing to make it easy to detect if duplicate
					if (!treeAlreadyExists(tieTree, trees)){ //to make sure duplicate trees aren't added
						tieTree.setName(getName() + " tree " + (++countTrees));
						trees.addElement(tieTree, false);
					
					}
				}
				else {  //rebuild matrix of distances among daughters of root
					double[][] tieMatrix = new double[numTaxa][numTaxa];
					Double2DArray.deassignArray(tieMatrix);

					int[][] clusters = new int[numDaughtersOfRoot][];
					for (int i=0; i<numDaughtersOfRoot; i++){
						clusters[i] = tieTree.getTerminalTaxa(tieTree.indexedDaughterOfNode(tieTree.getRoot(), i));
					}
					for (int i=0; i<numDaughtersOfRoot; i++){
						for (int j=i+1; j<numDaughtersOfRoot; j++){
							tieMatrix[i][j] = getDistanceBetweenClusters(originalMatrix, clusters[i], clusters[j]);
						}
					}
					for (int i=0; i<numDaughtersOfRoot; i++){ //assumes symmetrical distances
						for (int j=0; j<i; j++){
							tieMatrix[i][j] = tieMatrix[j][i] ;
						}
					}
					followTrees(tieTree, trees, tieMatrix, originalMatrix, joining+1, numTaxa, thisPath, progIndicator);
				}
			}
				
		}
  	}
  	
	/*.................................................................................................................*/
  	boolean treeAlreadyExists(Tree tree, TreeVector trees){
  		for (int i=0; i<trees.size(); i++)
  			if (treesSame(tree, (Tree)trees.elementAt(i)))
  				return true;
  		return false;
  	}
  	
 	/*-----------------------------------------*/
	boolean treesSame(Tree t1, Tree t2){ //assumes order standardized
		int node1 = t1.getRoot();
		int node2 = t2.getRoot();
		while (t1.nodeExists(node1) && t2.nodeExists(node1)){
			if (t1.nodeIsTerminal(node1) && t2.nodeIsTerminal(node2)) {
				if (t1.taxonNumberOfNode(node1) != t2.taxonNumberOfNode(node2))
					return false;
			}
			else if (t1.nodeIsInternal(node1) && t2.nodeIsInternal(node2)) {
				if (t1.numberOfDaughtersOfNode(node1) != t2.numberOfDaughtersOfNode(node2))
					return false;
			}
			else
				return false;
			node1 = t1.nextInPreorder(node1);
			node2 = t2.nextInPreorder(node2);
		}
		return true;
	}
	/*.................................................................................................................*/
   	 public boolean hasLimitedTrees(Taxa taxa){
   	 	return true;
   	 }
}



