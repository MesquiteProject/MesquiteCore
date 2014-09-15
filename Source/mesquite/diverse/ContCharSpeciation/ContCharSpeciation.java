/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 
. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.diverse.ContCharSpeciation;


import mesquite.lib.*;
import mesquite.diverse.lib.*;
import mesquite.categ.lib.CategoricalHistory;
import mesquite.cont.lib.*;

/** ======================================================================== */
public class ContCharSpeciation extends TreeCharSimulate {
	RandomBetween rng;
	double rate = 0.1;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		rng= new RandomBetween(1);
		if (!MesquiteThread.isScripting())
			rate = MesquiteDouble.queryDouble(containerOfModule(), "Rate", "Rate of Evolution of Speciation Rate", rate);
		if (!MesquiteDouble.isCombinable(rate))
			return false;
		addMenuItem("Rate of Evolution of Speciation Rate...", makeCommand("setRate",  (Commandable)this));
		return true;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 200;  
	}

	/*.................................................................................................................*/
	public boolean showCitation(){
		return true;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return false;  
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return true;
	}
	public boolean isPrerelease(){
		return false;
	}
	public String getDataType(){
		return ContinuousData.DATATYPENAME;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("setRate " + rate);
		return temp;
	}
	MesquiteInteger pos = new MesquiteInteger(0);
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the rate of change of the continuous character", "[number]", commandName, "setRate")) {
			pos.setValue(0);
			double s = MesquiteDouble.fromString(arguments, pos);
			if (!MesquiteDouble.isCombinable(s))
				s = MesquiteDouble.queryDouble(containerOfModule(), "Rate", "Rate of Evolution of Speciation Rate", rate);
			if (MesquiteDouble.isCombinable(s)) {
				rate = s;
				parametersChanged();
			}
		}
		else return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	private void speciateIfLucky(MesquiteTree tree, int node, ContinuousHistory rates, MesquiteInteger countOfSpecies, int numTaxa){
		if (tree.nodeIsTerminal(node) && MesquiteDouble.isCombinable(tree.getBranchLength(node))) {  //length will be uncombinable if this was just a daughter species
			if (countOfSpecies.getValue()<numTaxa) {
				int taxon = tree.taxonNumberOfNode(node);
				double rate = rates.getState(node, 0);  //negative absolute value of rate
				double probability = Math.exp(-Math.abs(rate));


				if (rng.nextDouble()<probability) {
					tree.splitTerminal(taxon, countOfSpecies.getValue(), false);
					
	  	 			rates.setState(tree.firstDaughterOfNode(node), rate);
	  	 			rates.setState(tree.lastDaughterOfNode(node), rate);
					countOfSpecies.increment();
				}
			}
		}
		else
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
				if (countOfSpecies.getValue()<numTaxa)
					speciateIfLucky(tree, d, rates, countOfSpecies, numTaxa);
			}
	}
	/*.................................................................................................................*/
	private void evolveRates(MesquiteTree tree, int node, ContinuousHistory rates){
		if (tree.nodeIsTerminal(node)) {
			double r = rates.getState(node, 0);
				if (rng.nextDouble()>0.5)
				r += rate;
			else
				r -= rate;
			rates.setState(node, 0, r);

		}
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
			evolveRates(tree, d, rates);
		}
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

	/*.................................................................................................................*/
	public  void doSimulation(Taxa taxa, int replicateNumber, ObjectContainer treeContainer, ObjectContainer characterHistoryContainer, MesquiteLong seed){
		//save random seed used to make tree under tree.seed for use in recovering later
		rng.setSeed(seed.getValue());
		MesquiteTree tree = null;
		ContinuousHistory charHistory = null;

		Object t = treeContainer.getObject();
		if (t == null || !(t instanceof MesquiteTree))
			tree = new MesquiteTree(taxa);
		else
			tree = (MesquiteTree)t;
		Object c = characterHistoryContainer.getObject();
		if (c == null || !(c instanceof CategoricalHistory))
			charHistory = new ContinuousHistory(taxa, 0, null);
		else  
			charHistory = (ContinuousHistory)c;

		charHistory = (ContinuousHistory)charHistory.adjustSize(tree);

		int numTaxa = taxa.getNumTaxa();


		double startRate = Math.log(0.001);
		for (int i=0; i<tree.getNumNodeSpaces(); i++)
			charHistory.setState(i, 0, startRate);


		tree.setToDefaultBush(2, false);
		tree.setAllBranchLengths(0, false);
		addLengthToAllTerminals(tree, tree.getRoot(), 0.01);
		MesquiteInteger countOfSpecies = new MesquiteInteger(2);
		long generations = 0;
		while (countOfSpecies.getValue()<numTaxa){
			generations++;
			evolveRates(tree, tree.getRoot(), charHistory);
			speciateIfLucky(tree, tree.getRoot(), charHistory, countOfSpecies, numTaxa);
			addLengthToAllTerminals(tree, tree.getRoot(), 0.01);
		}
		tree.reshuffleTerminals(rng);

		treeContainer.setObject(tree);
		characterHistoryContainer.setObject(charHistory);
		seed.setValue(rng.nextLong());  //see for next time

	}

	public void initialize(Taxa taxa){
	}
	/*.................................................................................................................*/
	/*.................................................................................................................*/
	public String getName() {
		return "Evolving Speciation Rate (Continuous Character)";
	}
	/*.................................................................................................................*/
	public String getVersion() {
		return null;
	}

	/*.................................................................................................................*/

	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Generates tree by a speciation model in which the speciation rate evolves by a Brownian motion model." ;
	}
	/*.................................................................................................................*/
	public String getParameters() {
		return "Rate of evolution of speciation rate: " + rate;
	}
	/*.................................................................................................................*/
}

