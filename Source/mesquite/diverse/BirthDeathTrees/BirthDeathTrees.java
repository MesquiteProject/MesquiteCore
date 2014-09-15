/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.diverse.BirthDeathTrees;


import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class BirthDeathTrees extends TreeSimulate {
	RandomBetween rng;
	final double defaultIncrement = 0.001;
	double increment = 0.001;
	double birthRate = 0.3;
	double birthProbPerIncrement = birthRate*increment;
	double deathRate = 0.1;
	double deathProbPerIncrement = deathRate*increment;
	MesquiteDouble b, d;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		rng= new RandomBetween(1);
		b = new MesquiteDouble(birthRate);
		d = new MesquiteDouble(deathRate);
		if (!MesquiteThread.isScripting()){
			if (!askRates())
				return false;
		}
		addMenuItem("Birth and Death Rates (simulation)...", makeCommand("setRates",  this));
		return true;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 200;  
	}
	private boolean askRates(){
		boolean ok = MesquiteDouble.queryTwoDoubles(containerOfModule(), "Speciation (birth) and Extinction (death) rates", 
				"Speciation (birth) rate", b, "Extinction (death) rate", d);
		if (ok){
			if (b.isCombinable())
				birthRate = b.getValue();
			if (d.isCombinable())
				deathRate = d.getValue();
		}
		return ok;
	}
	void calculateRates(int numTaxa){
		double maxRate = MesquiteDouble.maximum(birthRate, deathRate);
		if (maxRate>1.0){
			increment = 0.001/maxRate/Math.sqrt((double)numTaxa);  //narrow time slice as number of taxa increases
		}
		else
			increment = defaultIncrement/Math.sqrt((double)numTaxa);
		birthProbPerIncrement = birthRate*increment;
		deathProbPerIncrement = deathRate*increment;
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
		temp.addLine("setRates " + MesquiteDouble.toString(birthRate) + " " + MesquiteDouble.toString(deathRate));
		return temp;
	}
	MesquiteInteger pos = new MesquiteInteger(0);
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the birth and death rates", "[number][number]", commandName, "setRates")) {
			pos.setValue(0);
			if (StringUtil.blank(arguments)){
				if (!MesquiteThread.isScripting()){
					askRates();
					if (!MesquiteThread.isScripting()) {
						parametersChanged();
					}
				}
				return null;
			}
			double s = MesquiteDouble.fromString(arguments, pos);
			double e = MesquiteDouble.fromString(arguments, pos);
			if (MesquiteDouble.isCombinable(s))
				birthRate = s;
			if (MesquiteDouble.isCombinable(e))
				deathRate = e;

			if (!MesquiteThread.isScripting()) {
				parametersChanged();
			}
		}
		else return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	long countSpeciations = 0;
	long countExtinctions = 0;
	/*.................................................................................................................*/
	private boolean goExtinctIfUnlucky(Taxa taxa, boolean[] taxaInTree, MesquiteTree tree, MesquiteInteger countOfSpecies){
		for (int it = 0; it<taxa.getNumTaxa(); it++){
			if (taxaInTree[it]){
				int node = tree.nodeOfTaxonNumber(it);
				double probability = deathProbPerIncrement;
				if (rng.nextDouble()<probability) {

					tree.deleteClade(node, false);
					countOfSpecies.decrement();

					taxaInTree[it] = false;
					countExtinctions++;
					CommandRecord.tick("Went extinct at node " + node + " ; total number of species " + countOfSpecies + "; total speciations: " + countSpeciations  + "; total extinctions: " + countExtinctions );
					if (countOfSpecies.getValue() == 0 || tree.numberOfTerminalsInClade(tree.getRoot()) == 0)
						return true;
				}
			}
		}
		return false;

	}

	/*.................................................................................................................*/
	private void speciateIfLucky(MesquiteTree tree, int node, boolean[] taxaInTree, MesquiteInteger countOfSpecies, int numTaxa){
		if (tree.nodeIsTerminal(node) && MesquiteDouble.isCombinable(tree.getBranchLength(node))) {  //length will be uncombinable if this was just a daughter species
			if (countOfSpecies.getValue()<numTaxa) {
				int taxon = tree.taxonNumberOfNode(node);
				if (taxaInTree[taxon]){ //not extinct
					double probability = birthProbPerIncrement;

					if (rng.nextDouble()<probability) {
						tree.splitTerminal(taxon, -1, false);
						countSpeciations++;
						int firstD = tree.firstDaughterOfNode(node);
						int lastD = tree.lastDaughterOfNode(node);
						taxaInTree[tree.taxonNumberOfNode(firstD)] = true;
						taxaInTree[tree.taxonNumberOfNode(lastD)] = true;
						countOfSpecies.increment();
						CommandRecord.tick("Speciated at node " + node + " ; total number of species " + countOfSpecies + "; total speciations: " + countSpeciations  + "; total extinctions: " + countExtinctions );
					}
				}
			}
		}
		else
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
				if (countOfSpecies.getValue()<numTaxa)
					speciateIfLucky(tree, d, taxaInTree, countOfSpecies, numTaxa);
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
	public int getNumberOfTrees(Taxa taxa) {
		return MesquiteInteger.infinite;
	}
	/*.................................................................................................................*/
	public Tree getSimulatedTree(Taxa taxa, Tree baseTree, int treeNumber, ObjectContainer extra, MesquiteLong seed) { //todo: should be two seeds passed!
		//save random seed used to make tree under tree.seed for use in recovering later
		if (seed != null)
			rng.setSeed(seed.getValue());
		if (taxa == null)
			return null;
		if (baseTree==null || !(baseTree instanceof MesquiteTree))
			baseTree = new MesquiteTree(taxa);
		MesquiteTree tree = ((MesquiteTree)baseTree);
		

		boolean[] taxaInTree = new boolean[taxa.getNumTaxa()];
		for (int i=0; i< taxaInTree.length; i++)
			taxaInTree[i] = false;
		
		int attempts = 0;
		boolean done = false;
		boolean wentExtinct = false;
		int patience = 100; //TODO: make this user settable
		int numTaxa = taxa.getNumTaxa();
		long generations = 0;
	
		while (attempts < patience && !done){
			wentExtinct = false;
			tree.setToDefaultBush(2, false);
			for (int i=0; i< taxaInTree.length; i++)
				taxaInTree[i] = false;
			taxaInTree[0] = true;
			taxaInTree[1] = true;
			tree.setAllBranchLengths(0, false);
			MesquiteInteger countOfSpecies = new MesquiteInteger(2);
			countSpeciations = 0;
			countExtinctions = 0;
			generations = 0;
			CommandRecord.tick("Attempt " + (attempts+1) + " to simulate tree ");
			increment = defaultIncrement;
			addLengthToAllTerminals(tree, tree.getRoot(), increment);
			calculateRates(1);
			while (countOfSpecies.getValue()<numTaxa && !wentExtinct){
				generations++;
				boolean allExtinct = goExtinctIfUnlucky(taxa, taxaInTree, tree, countOfSpecies);

				if (allExtinct){
					wentExtinct = true;
					CommandRecord.tick("All Extinct [attempt: "+ (attempts+1) + "] ");
				}
				else {
					speciateIfLucky(tree, tree.getRoot(), taxaInTree, countOfSpecies, numTaxa);

					addLengthToAllTerminals(tree, tree.getRoot(), increment);

					CommandRecord.tick("Speciation event (species in tree currently:  " + countOfSpecies.getValue()  + ") [attempt: "+ (attempts+1) + "] ");
				}
				calculateRates(countOfSpecies.getValue());
		}
			if (!wentExtinct)
				done = true;
			attempts++;
		}
		tree.reshuffleTerminals(rng); //added after 1.03

		tree.setName("Sim. sp/ext " + (treeNumber +1));// + hitsString);
		tree.setAnnotation("(#extinctions " + countExtinctions + "; generations: " + generations + ")", false);// + hitsString);

		if (seed != null)
			seed.setValue(rng.nextLong());  //see for next time
		return tree;
	}

	public void initialize(Taxa taxa){
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Birth/Death Process Trees";
	}

	/*.................................................................................................................*/

	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Generates tree by simple birth/death model with a constant rate of speciation (birth) and of extinction (death).";
	}
	/*.................................................................................................................*/
	public String getParameters() {
		return "Speciation (birth) rate: " + birthRate + "; Extinction (death) rate: " + deathRate;
	}
	/*.................................................................................................................*/
}

