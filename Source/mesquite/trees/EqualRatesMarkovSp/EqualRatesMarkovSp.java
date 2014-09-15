/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.trees.EqualRatesMarkovSp;

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class EqualRatesMarkovSp extends TreeSimulate {
	RandomBetween randomTaxon;
	ExponentialDistribution waitingTime;
	double scaling = 10.0;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		randomTaxon= new RandomBetween(1);
 		waitingTime = new ExponentialDistribution(1);
 		if (!MesquiteThread.isScripting())
 			scaling = MesquiteDouble.queryDouble(containerOfModule(), "Total tree depth", "Total tree depth", scaling);
 		if (!MesquiteDouble.isCombinable(scaling))
 			return false;
   		addMenuItem("Total tree depth (simulation)...", makeCommand("setTime",  this));
		return true;
  	 }
  	 
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
   	 	return false;  
   	 }
	/*.................................................................................................................*/
   	 public boolean isSubstantive(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
   	 public boolean showCitation(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) {
   	 	Snapshot temp = new Snapshot();
  	 	temp.addLine("setTime " + scaling);
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
   	 	else return  super.doCommand(commandName, arguments, checker);
		return null;
   	 }
	/*.................................................................................................................*/
  	 private void addLengthToAllTerminals(MesquiteTree tree, int node, double increment){
  	 	if (tree.nodeIsTerminal(node)) {
  	 		double current = tree.getBranchLength(node, MesquiteDouble.unassigned);
  	 		if (MesquiteDouble.isCombinable(current))
  	 			tree.setBranchLength(node, current + increment, false);
  	 		else
  	 			tree.setBranchLength(node, increment, false);  	 		
		}
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
			addLengthToAllTerminals(tree, d, increment);
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
			mTree.setToDefaultBush(2, false);
			int whichTaxon;

			for (int taxon = 2; taxon < taxa.getNumTaxa(); taxon++) {
				whichTaxon=randomTaxon.randomIntBetween(0, taxon-1);
				addLengthToAllTerminals(mTree, tree.getRoot(), getWaitingTime(taxon));
				mTree.splitTerminal(whichTaxon, taxon, false);
			}
 /*			if (positions == null || positions.length != mTree.numberOfTerminalsInClade(mTree.getRoot()))
 				positions = new double[mTree.numberOfTerminalsInClade(mTree.getRoot())];*/
			mTree.reshuffleTerminals(randomTaxon); //added after 1.03
 		
			//adding to all terminals waiting time uniformly distributed between 0 and waiting time to next speciation
			addLengthToAllTerminals(mTree, tree.getRoot(), waitingTime.nextDouble()*getWaitingTime(taxa.getNumTaxa()));
			double depth = mTree.tallestPathAboveNode(tree.getRoot());
			if (depth>0) {
				double scaleFactor = scaling/depth;
	 			for (int i=0; i<mTree.getNumNodeSpaces(); i++)
	 				if (mTree.nodeExists(i)) {
	 					double b = mTree.getBranchLength(i, MesquiteDouble.unassigned);
	 					if (MesquiteDouble.isCombinable(b))
	 						mTree.setBranchLength(i, b*scaleFactor, false);
	 				}
			}
			seed.setValue(randomTaxon.nextLong());  //see for next time
	   		return mTree;
   	}
   
   	public void initialize(Taxa taxa){
   	}
	/*.................................................................................................................*/
	/*.................................................................................................................*/
    	 public String getName() {
		return "Uniform speciation (Yule)";
   	 }
   	 
	/*.................................................................................................................*/
   	 
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Generates tree by simple uniform probability speciation (a Yule process) as done by Harding (1971).  The"
 		+" chance of speciation is equal for all tips." ;
   	 }
	/*.................................................................................................................*/
   	public String getParameters() {
		return "Trees constrained to branch length depth: " + scaling;
   	}
	/*.................................................................................................................*/
}

