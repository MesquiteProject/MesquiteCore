/* Mesquite source code, Treefarm package.  Copyright 1997 and onward, W. Maddison, D. Maddison and P. Midford. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.treefarm.TerminalInstability;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.treefarm.lib.*;

/** ======================================================================== */
/*
Terminal instability -- the difference in the position of a particular target taxon is measured by the 
sum of the weighted differences between the trees in path lengths between that taxon and each of the other taxa.  
The path length between taxa is the number of nodes intervening on the path between the one taxon and the other 
(branch lengths not considered, hence scaling not yet needed). 
Thus if on tree 1 the path length between the target taxon and another taxon i is pti1 and the length on tree 2 is pti2, 
then the weighted difference is Math.abs(pti1 - pti2)/sqr(pti1 + pti2).  The weighting by sqr(pti1 + pti2) 
means that the emphasis on another taxon i fades away as it is farther from the target taxon in on or the other tree.  
This weighted difference is summed over all other taxa i to get the difference in the target taxon's position in the two trees.

*/

public class TerminalInstability extends NForTaxonWithTrees {
	double exponent = 2.0;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		addMenuItem("Exponent for Instability...", makeCommand("setExponent", this));
		return true;  
 	}
 	
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) {
   	 	Snapshot temp = new Snapshot();
  	 	temp.addLine("setExponent " + exponent);
  	 	temp.incorporate(super.getSnapshot(file), false);
  	 	return temp;
  	 }
  	 MesquiteInteger pos = new MesquiteInteger(0);
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Sets the exponent used in the denominator of the instability calculations", "[number]", commandName, "setExponent")) {
			pos.setValue(0);
			double newNum= MesquiteDouble.fromString(arguments, pos);
			if (!MesquiteDouble.isCombinable(newNum))
				newNum = MesquiteDouble.queryDouble(containerOfModule(), "Set exponent", "Enter the exponent used in the denominatory of the instability calculations.  Default is 2.  Larger exponents emphasize near neighbors more strongly relative to distant taxa.", exponent, 0.9999, 4);
    	 		if (newNum>0  && newNum!=exponent) {
    	 			exponent = newNum;
				if (!MesquiteThread.isScripting()) {
					needsRecalculation = true;
					parametersChanged();
				}
    	 		}
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
    	 	return null;
   	 }
	/*.................................................................................................................*/
	public void calculateNumbers(Taxa taxa, NumberArray results, MesquiteString resultsString){
		if (results==null|| taxa == null)
			return;
		int numTaxa = taxa.getNumTaxa();
		results.resetSize(numTaxa);
	   	clearResultAndLastResult(results);
		results.zeroArray();
		
		int numTrees = getNumTrees(taxa);
		
		MesquiteInteger minDiscord = new MesquiteInteger();
		MesquiteNumber b = new MesquiteNumber();
		double[][] originalDistances = new double[numTaxa][numTaxa];
		double[][] comparisonDistances = new double[numTaxa][numTaxa];
		double[][] heights = new double[MesquiteTree.standardNumNodeSpaces(numTaxa)][numTaxa];

		int numComparisons = numTrees*(numTrees-1)/2;
		ProgressIndicator progIndicator = null;
		if (numTrees>10){
			progIndicator = new ProgressIndicator(getProject(),getName(), "Calculating Terminal Instability by comparing to other trees", numComparisons, true);
			progIndicator.start();
		}
		int num = 0;
		PatristicDistances pdistTarget = new PatristicDistances();
		PatristicDistances pdistCompare = new PatristicDistances();
		
		for (int it = 0; it< numTrees  && (progIndicator==null || !progIndicator.isAborted()); it++){ //get tree for comparison
				Tree targetTree = getTree(taxa, it);
				if (targetTree != null) {
					
					if (heights == null || heights.length < targetTree.getNumNodeSpaces())
						heights = new double[targetTree.getNumNodeSpaces()][numTaxa];
					//calculatePatristic(targetTree, taxa, originalDistances); //for this tree calculate patristic distances (number of nodes separating terminals; no branch lengths)
					if (progIndicator !=null) {
						progIndicator.setCurrentValue(num);
						progIndicator.setText("Comparing tree " + (it+1) + " (of " + numTrees + ") to other trees");
					}
					CommandRecord.tick("Comparing tree " + (it+1) + " (of " + numTrees + ") to other trees");
					originalDistances = pdistTarget.calculatePatristic(targetTree, numTaxa, originalDistances); //for this tree calculate patristic distances (number of nodes separating terminals; no branch lengths)
					for (int it2 = 0; it2< it && (progIndicator==null || !progIndicator.isAborted()); it2++){ //get tree for comparison
							num++;
							
							Tree comparison = getTree(taxa, it2);
							if (heights == null || heights.length < comparison.getNumNodeSpaces())
								heights = new double[comparison.getNumNodeSpaces()][numTaxa];
							comparisonDistances = pdistTarget.calculatePatristic(comparison, numTaxa, comparisonDistances); //for this tree calculate patristic distances (number of nodes separating terminals; no branch lengths)
							//calculatePatristic(comparison, numTaxa, heights, comparisonDistances);
							for (int i = 0; i < numTaxa; i++){ //For each of the taxa, add to its score how unstable it is on this tree
								for (int j = 0; j < numTaxa; j++)  //for each of the OTHER taxa, calculate how much original and comparison distances differ
									if (j!=i) {
										b.setValue(strain(originalDistances[i][j], comparisonDistances[i][j]));
										results.addValue(i, b); //add difference in distances to running total
									}
						
					}
				}
			}
		}
		String s = getVeryShortName() + " calculated";
		
		if (resultsString != null){
			if (progIndicator!=null && progIndicator.isAborted()) 
				resultsString.setValue(s + " INCOMPLETE CALCULATION (only " + num + " of " + numComparisons + " counted).");
			else {
				resultsString.setValue(s);
			}
		}
		saveLastResult(results);
		saveLastResultString(resultsString);
		if (progIndicator!=null) 
			progIndicator.goAway();
	}
	/*-----------------------------------------*/
	private double strain(double orig, double comp){
		double div = Math.abs(orig)+ Math.abs(comp);
		if (div == 0)
			return 0;
		if (exponent >= 1.9999999 && exponent < 2.0000001)
			return Math.abs(orig - comp)/(div*div); //downweights distant taxa
		else
			return Math.abs(orig - comp)/Math.pow(div, exponent); //downweights distant taxa
	}
	/*-----------------------------------------*
	private void calculatePatristic(Tree tree, int numTaxa, double[][] heights, double[][] distances){
		for (int i = 0; i < numTaxa; i++){
			for (int j = 0; j < numTaxa; j++)
				if (i!=j)
					distances[i][j] = -1;
				else
					distances[i][j] = 0;
		}
		for (int i = 0; i < tree.getNumNodeSpaces(); i++){
			for (int j = 0; j < numTaxa; j++)
				heights[i][j] = -1;
		}
		getHeights(tree, tree.getRoot(), heights);
		getDistances(tree, tree.getRoot(), numTaxa, heights, distances);
  	 }
	/*-----------------------------------------*
	/** Finds heights to taxa for each node.  Beforehand initialize heights to -1*
	void getHeights(Tree tree, int node, double[][] heights) { 
  	 	if (tree.nodeIsTerminal(node)){
  	 		int taxon = tree.taxonNumberOfNode(node);
  	 		heights[node][taxon]=0;
  	 	}
  	 	else {
  	 		boolean firstDaughter = true;
	  	 	for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter)) {
	  			getHeights(tree, daughter, heights);
  	 			for (int i=0; i<heights[daughter].length; i++)
  	 				if (heights[daughter][i] >= 0){ //taxon i is in daughter clade
  	 					if (heights[node][i]<0)
  	 						heights[node][i] = 0;
  	 					if (tree.getRoot()==node && !firstDaughter && !tree.nodeIsPolytomous(node))
  	 						heights[node][i] += heights[daughter][i];
  	 					else
  	 						heights[node][i] += heights[daughter][i] + 1;
		    	 			firstDaughter = false;
	 				}
   	 		}
   	 	}
	}
	/** Finds distances among taxa.  Beforehand initialize distances to -1*
	void getDistances(Tree tree, int node,  int numTaxa, double[][] heights, double[][] distances) { 
  	 	if (tree.nodeIsInternal(node)){
	  	 	for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter)) {
	  			getDistances(tree, daughter, numTaxa, heights, distances);
  	 		}
  	  	 	for (int i= 0; i< numTaxa; i++)
	  	 		for (int j= 0; j< i; j++)
	  	 			if (distances[i][j]<=0) { //not yet assigned
	  	 				if (heights[node][i]>= 0 && heights[node][j] >=0){ //first common ancestor found
	  	 					distances[i][j] = heights[node][i] + heights[node][j];
	  	 					distances[j][i] = distances[i][j];
	  	 				}
	  	 			}
  	 	}
	}
	/*.................................................................................................................*/
    	 public String getVeryShortName() {
		return "Instability Among Trees";
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Taxon Instability Among Trees";
   	 }
	/*.................................................................................................................*/
  	 public String getVersion() {
		return null;
   	 }
	/*.................................................................................................................*/
  	 public boolean isPrerelease() {
		return false;
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Calculates for each taxon the degree to which its implied unweighted patristic distances between that taxon and others differs among trees. "
 		+ " For each taxon i this sums over all tree pairs x and y and over all other taxa j:  | Dijx - Dijy |/(Dijx + Dijy)^^z where Dijq is the distance between taxa i and j on tree q. "
 		+ "Close relationships are emphasized if the exponent z is higher (default is 2).";
   	 }
   	 
}


