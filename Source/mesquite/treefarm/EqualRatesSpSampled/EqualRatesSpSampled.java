/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.treefarm.EqualRatesSpSampled;

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class EqualRatesSpSampled extends TreeSimulate {
	RandomBetween randomTaxon;
	ExponentialDistribution waitingTime;
	double scaling = 10.0;
	int totalSpecies = 1000;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		randomTaxon= new RandomBetween(1);
 		waitingTime = new ExponentialDistribution(1);
 		if (!MesquiteThread.isScripting()) {
 			scaling = MesquiteDouble.queryDouble(containerOfModule(), "Total tree depth", "Total tree depth", scaling);
 			totalSpecies = MesquiteInteger.queryInteger(containerOfModule(), "Total Species", "Total number of species from which observed number sampled", totalSpecies);
 		}
 		if (!MesquiteDouble.isCombinable(scaling))
 			return false;
   		addMenuItem("Total tree depth (simulation)...", makeCommand("setTime",  this));
   		addMenuItem("Total Species...", makeCommand("setTotalSpecies",  this));
		return true;
  	 }
  	 
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return false;  
   	}
	/*.................................................................................................................*/
   	 public boolean showCitation(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
   	 public boolean isSubstantive(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) {
   	 	Snapshot temp = new Snapshot();
  	 	temp.addLine("setTime " + scaling);
  	 	temp.addLine("setTotalSpecies " + totalSpecies);
  	 	return temp;
  	 }
  	 MesquiteInteger pos = new MesquiteInteger(0);
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Sets the total tree depth", "[number]", commandName, "setTime")) {
    	 		pos.setValue(0);
 			double s = MesquiteDouble.fromString(arguments, pos);
	 		if (!MesquiteDouble.isCombinable(s))
 				s = MesquiteDouble.queryDouble(containerOfModule(), "Total tree depth", "Total tree depth", scaling);
	 		if (MesquiteDouble.isCombinable(s)) {
	 			scaling = s;
	 			parametersChanged();
	 		}
    		}
    	 	else if (checker.compare(this.getClass(), "Sets the total number of species from which observed number sampled", "[number]", commandName, "setTotalSpecies")) {
    	 		pos.setValue(0);
 			int s = MesquiteInteger.fromString(arguments, pos);
	 		if (!MesquiteInteger.isCombinable(s))
 				s = MesquiteInteger.queryInteger(containerOfModule(), "Total Species", "Total number of species from which observed number sampled", totalSpecies);
	 		if (MesquiteInteger.isCombinable(s)) {
	 			totalSpecies = s;
	 			parametersChanged();
	 		}
    		}
   	 	else return  super.doCommand(commandName, arguments, checker);
		return null;
   	 }
	/*.................................................................................................................*/
  	 private void addLengthToAllTerminals(QuickTree tree, int node, double increment){
  	 	if (tree.taxon[node]>=0) {
  	 		double current = tree.getBranchLength(node);
  	 		if (MesquiteDouble.isCombinable(current))
  	 			tree.setBranchLength(node, current + increment);
  	 		else
  	 			tree.setBranchLength(node, increment);  	 		
		}
		else {
			addLengthToAllTerminals(tree, tree.left[node], increment);
			addLengthToAllTerminals(tree, tree.right[node], increment);
		}
  	 }
  	private double getWaitingTime(int numTaxa){
  		return waitingTime.nextExponential(1.0/numTaxa);
  	}
	/*.................................................................................................................*/
   	public int getNumberOfTrees(Taxa taxa) {
   		return MesquiteInteger.infinite;
   	}
	/*.................................................................................................................*/
   	public Tree getSimulatedTree(Taxa taxa, Tree tree, int treeNumber, ObjectContainer extra, MesquiteLong seed) { //todo: should be two seeds passed!
   			//save random seed used to make tree under tree.seed for use in recovering later
  			randomTaxon.setSeed(seed.getValue());
  			waitingTime.setSeed(seed.getValue());
  			if (tree==null || !(tree instanceof MesquiteTree))
  				 tree = new MesquiteTree(taxa);
  			MesquiteTree mTree = ((MesquiteTree)tree);
			
			//Operate on a simple tree class with given total number of species, then subsample from this quick tree
			QuickTree qTree = new QuickTree(totalSpecies);
			qTree.init();
			
			//Evolving tree by simple Yule process
			int whichTaxon;
			for (int taxon = 2; taxon < totalSpecies; taxon++) {
				whichTaxon=randomTaxon.randomIntBetween(0, taxon-1);
				addLengthToAllTerminals(qTree, qTree.root, getWaitingTime(taxon));
				qTree.splitTerminal(whichTaxon, taxon);
			}
			
			//adding to all terminals waiting time uniformly distributed between 0 and waiting time to next speciation
			addLengthToAllTerminals(qTree, qTree.root, waitingTime.nextDouble()*getWaitingTime(taxa.getNumTaxa()));
			
			//sampling species
			if (totalSpecies>taxa.getNumTaxa()){
				int toCut = totalSpecies-taxa.getNumTaxa();
				for (int i= 0; i< toCut; i++){
					int unlucky=randomTaxon.randomIntBetween(0, totalSpecies-i -1);
					int cut = qTree.findTaxon(unlucky);
					qTree.cutTerminalNode(cut);
					qTree.renumberTaxaAbove(qTree.root, unlucky);
				}
			}
			//rescale to make it desired total depth
			qTree.scaleTree(scaling);
			
			//write tree in Newick form and then reread into MesquiteTree
			mTree.readTree(qTree.writeTree(qTree.root) + ";");
			
			mTree.reshuffleTerminals(randomTaxon); //added after 1.03 to fix bug
			
			seed.setValue(randomTaxon.nextLong());  //seed for next time
	   		return mTree;
   	}
   
   	public void initialize(Taxa taxa){
   	}
	/*.................................................................................................................*/
   	public boolean isPrerelease(){
   		return false;
   	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Uniform Speciation with Sampling";
   	 }
   	 
	/*.................................................................................................................*/
   	 
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Generates tree by simple uniform probability speciation (a Yule process), with subsequent sampling of species." ;
   	 }
	/*.................................................................................................................*/
   	public String getParameters() {
		return "Trees constrained to branch length depth: " + scaling + "; total number of species from which observed number sampled: " + totalSpecies;
   	}
	/*.................................................................................................................*/
}


class QuickTree {
	int root =0;
	int[] left, right, taxon;
	double[] lengths;
	int numTaxa;
	int numNodes;
	int open = 0;
	public QuickTree(int numTaxa){
		this.numTaxa = numTaxa;
		numNodes = numTaxa*2+4;
		left = new int[numNodes];
		right = new int[numNodes];
		taxon = new int[numNodes];
		lengths = new double[numNodes];
	}
	void init(){
		for (int i=0; i<numNodes; i++) {
			left[i] = 0;
			right[i] = 0;
			lengths[i] = 0;
			taxon[i] = -1;
		}
		left[0] = 1;
		taxon[1] = 0;
		right[0] = 2;
		taxon[2] = 1;
		open = 3;
	}
	
	int recfindTaxon(int node, int t){
		if (taxon[node]>=0) {
			if (taxon[node]==t)
				return node;
			return -1;
		}
		else {
			int nodeFound = recfindTaxon(left[node],t);
			if (nodeFound<0)
				nodeFound = recfindTaxon(right[node],t);
			return nodeFound;
		}
	}
	int findTaxon(int t){
		return recfindTaxon(root, t);
	}
	/** Branches a terminal node off taxon number taxonNum, and assigns it taxon newNumber  */
	void splitTerminal(int taxonNum, int newNumber){
		int node = findTaxon(taxonNum);
		if (node>=0){
			left[node]=open++;
			right[node]=open++;
			taxon[node]=-1;
			taxon[left[node]] = taxonNum;
			taxon[right[node]] = newNumber;
		}
	}
	
	int findAncestor(int node, int anc, int target){
		if (taxon[node]>=0) {
			if (node==target)
				return anc;
			return -1;
		}
		else {
			if (node==target)
				return anc;
			int nodeFound = findAncestor(left[node],node, target);
			if (nodeFound<0)
				nodeFound = findAncestor(right[node],node, target);
			return nodeFound;
		}
	}
	int diagfindAncestor(int node, int anc, int target){
		if (taxon[node]>=0) {
			if (node==target)
				return anc;
			return -1;
		}
		else {
			if (node==target)
				return anc;
			int nodeFound = diagfindAncestor(left[node],node, target);
			if (nodeFound<0)
				nodeFound = diagfindAncestor(right[node],node, target);
			return nodeFound;
		}
	}

	void cutTerminalNode(int toCut){
		int anc = findAncestor(root, -1, toCut);
		int sister;
		if (left[anc] == toCut) 
			sister = right[anc];
		else 
			sister = left[anc];
		if (anc == root){
			root = sister;
		}
		else {
			int ancAnc = findAncestor(root, -1, anc);
			lengths[sister] += lengths[anc];
			if (left[ancAnc] == anc)
				left[ancAnc] = sister;
			else
				right[ancAnc] = sister;
		}
		
	}
	
	void renumberTaxaAbove(int node, int t){
		if (taxon[node]>=0) {
			if (taxon[node]>=t)
				taxon[node]--;
		}
		else {
			renumberTaxaAbove(left[node], t);
			renumberTaxaAbove(right[node], t);
		}
	}
	void setBranchLength(int node,double d){
		lengths[node] = d;
	}
	
	double getBranchLength(int node) {
		return lengths[node];
	}
	double tallestPathAboveNode(int node){
		if (taxon[node]>=0)
			return 0;
		double leftWay = tallestPathAboveNode(left[node]) + lengths[left[node]];
		double rightWay = tallestPathAboveNode(right[node]) + lengths[right[node]];
		if (leftWay > rightWay)
			return leftWay;
		else
			return rightWay;
	}
	void recScale(int node, double scaleFactor){
		lengths[node] *= scaleFactor;
		if (taxon[node]<0) {
			recScale(left[node], scaleFactor);
			recScale(right[node], scaleFactor);
		}
	}
	void scaleTree(double targetDepth){
		double depth = tallestPathAboveNode(root);
		if (depth!=0)
			recScale(root, targetDepth/depth);
		lengths[root] = 1.0;
	}

	boolean writeLengths = true;
	String writeTree(int node){
		if (!writeLengths){
			if (taxon[node]>=0)
				return Integer.toString(taxon[node] + 1);
			return "(" + writeTree(left[node]) + "," + writeTree(right[node]) + ")";
		}
		else {
			if (taxon[node]>=0)
				return Integer.toString(taxon[node] + 1) + ":" + lengths[node];
			return "(" + writeTree(left[node]) + "," + writeTree(right[node]) + "):" + lengths[node];
		}
	}
	String writeTreeWithNodeNumbers(int node){
			if (taxon[node]>=0)
				return Integer.toString(taxon[node] + 1) + "[" + node + "]";
			return "(" + writeTreeWithNodeNumbers(left[node]) + "," + writeTreeWithNodeNumbers(right[node]) + ")" + "[" + node + "]";
	}
}

