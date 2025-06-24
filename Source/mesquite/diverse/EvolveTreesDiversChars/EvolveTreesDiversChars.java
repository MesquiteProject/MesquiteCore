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
package mesquite.diverse.EvolveTreesDiversChars;


import mesquite.diverse.lib.TreeCharSimulate;
import mesquite.lib.CommandChecker;
import mesquite.lib.CommandRecord;
import mesquite.lib.EmployeeNeed;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteLong;
import mesquite.lib.MesquiteThread;
import mesquite.lib.Nameable;
import mesquite.lib.ObjectContainer;
import mesquite.lib.Snapshot;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.CharacterHistory;
import mesquite.lib.characters.CharacterState;
import mesquite.lib.duties.CharactersManager;
import mesquite.lib.duties.FileAssistantT;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeVector;

/** ======================================================================== */
public class EvolveTreesDiversChars extends FileAssistantT {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(TreeCharSimulate.class, getName() + "  needs a particular method to simulate trees and characters.",
		"The method to simulate trees and characters can be selected initially");
	}
	/*.................................................................................................................*/
public TreeCharSimulate simulator;
	Taxa taxa;
	MesquiteLong seed;
	MesquiteFile file;
	int numTrees = 100;
	long originalSeed=System.currentTimeMillis(); //0L;
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 200;  
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		simulator = (TreeCharSimulate)hireEmployee(TreeCharSimulate.class, "Simulator of trees and characters");
		if (simulator == null)
			return sorry(getName() + " couldn't start because no simulating module was obtained.");
		seed = new MesquiteLong(originalSeed);
		if (!MesquiteThread.isScripting()){
			taxa = getProject().chooseTaxa(containerOfModule(), "For which block of taxa do you want to evolve trees and characters?");
			if (taxa == null)
				return sorry("A taxa block must be created first before evolving trees with characters");
			file = getProject().chooseFile( "Select file to which to add the trees and matrix");
			numTrees = MesquiteInteger.queryInteger(containerOfModule(), "Number of Trees", "Number of Trees", 100);
			if (numTrees <=0 || !MesquiteInteger.isCombinable(numTrees))
				return false;
			long response = MesquiteLong.queryLong(containerOfModule(), "Seed for Tree simulation", "Set Random Number seed for tree simulation:", seed.getValue());
			if (MesquiteLong.isCombinable(response))
				originalSeed = response;
			evolve();
			return false;
		}
		taxa = getProject().getTaxa(0);
		file = getProject().getHomeFile();
		return true;
	}
	
	/*.................................................................................................................*/
	public void evolve(){
		if (taxa == null || numTrees <=0)
			return;
	    incrementMenuResetSuppression();
	    getProject().incrementProjectWindowSuppression();

	    seed.setValue(originalSeed);
	    TreeVector trees = new TreeVector(taxa);
	    CharactersManager charManager = (CharactersManager)getFileCoordinator().findImmediateEmployeeWithDuty(CharactersManager.class);
	    CharacterData data= (CharacterData)charManager.newCharacterData(taxa, numTrees, simulator.getDataType());

	    ObjectContainer treeContainer = new ObjectContainer();
	    ObjectContainer charHistoryContainer = new ObjectContainer();
	    CharacterState cs = data.makeCharacterState();
	    long timer = System.currentTimeMillis();

	    for (int i=0; i<numTrees; i++){

	        CommandRecord.tick("Simulating tree and character " + i);

	        if (System.currentTimeMillis()- timer > 10000){
	            timer = System.currentTimeMillis();
	            logln("Simulating tree " + (i + 1));
	        }

	        simulator.doSimulation(taxa, i, treeContainer, charHistoryContainer, seed);

	        Object t = treeContainer.getObject();
	        if (t == null || !(t instanceof Tree))
	            return;//TODO: insert more informative response
	        Object c = charHistoryContainer.getObject();
	        if (c == null || !(c instanceof CharacterHistory))
	            return; //TODO: insert more informative response
	        MesquiteTree tree = (MesquiteTree)t;
	       tree.resetTaxaInfo();
	        CharacterHistory ch = (CharacterHistory)c;
	        harvestStates(tree, tree.getRoot(), i, ch, data, cs);
	        if (tree instanceof Nameable && !tree.hasName())
	            ((Nameable)tree).setName("Simulated Tree (with character) " + (i+1));
	        MesquiteTree clone = tree.cloneTree();
		       clone.resetTaxaInfo();
	        trees.addElement(clone, false);
	        clone.setName("Simulated Tree (with character) " + (i+1));
	        data.setCharacterName(i, "Sim. with tree " + (i+1));
	    }

	    data.addToFile(file, getProject(), findElementManager(CharacterData.class));  
	    data.setName("Evolved on Simulated Trees [" + simulator.getParameters() + "]");
	    trees.setName("Evolved with Characters [" + simulator.getParameters() + "]");
	    trees.addToFile(file, getProject(), findElementManager(TreeVector.class));  
	    data.show();
	    trees.show();
	    getProject().decrementProjectWindowSuppression();
	    decrementMenuResetSuppression();
	    resetAllMenuBars();
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		final Snapshot temp = new Snapshot();
		temp.addLine("setSeed " + originalSeed);
  	 	temp.addLine("setSimulator ", simulator);
   	 	temp.addLine("setNumTrees " + numTrees);
		temp.addLine("setTaxa " + getProject().getTaxaReferenceExternal(taxa)); 
		return temp;
	}

	private MesquiteInteger pos = new MesquiteInteger(0);
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
	    if (checker.compare(this.getClass(), "Sets the module doing simulations", "[name of module]", commandName, "setSimulator")) {
	        final TreeCharSimulate temp=  (TreeCharSimulate)replaceEmployee(TreeCharSimulate.class, arguments, "Tree simulator", simulator);
	        if (temp!=null) {
	            simulator = temp;
	            if (!MesquiteThread.isScripting())
	                parametersChanged(); //?
	        }
	        return temp;
	    }
	    else if (checker.compare(this.getClass(), "Sets the random number seed", null, commandName, "setSeed")) {
	        pos.setValue(0);
	        originalSeed = MesquiteLong.fromString(arguments, pos);
	        if (!MesquiteThread.isScripting())
	            parametersChanged(); //?
	    }
	    else if (checker.compare(this.getClass(), "Sets the number of trees", null, commandName, "setNumTrees")) {
	        pos.setValue(0);
	        numTrees = MesquiteInteger.fromString(arguments, pos);
	        if (!MesquiteThread.isScripting())
	            parametersChanged(); //?
	    }
	    else if (checker.compare(this.getClass(), "Sets the taxa block used", "[block reference, number, or name]", commandName, "setTaxa")) {
	        taxa = getProject().getTaxa(checker.getFile(), parser.getFirstToken(arguments));

	    }
	    else if (checker.compare(this.getClass(), "Evolves", null, commandName, "evolve")) {
	        evolve();
	    }
	    else return  super.doCommand(commandName, arguments, checker);
	    return null;
	}

	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	private void harvestStates(Tree tree, int node, int i, CharacterHistory history, CharacterData data, CharacterState cs){
		if (tree.nodeIsTerminal(node)) {
			cs = history.getCharacterState(cs, node);
			data.setState(i, tree.taxonNumberOfNode(node), cs);
		}
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
			harvestStates(tree, d, i, history, data, cs);
		}
	}

	/*.................................................................................................................*/
	public String getName() {
		return "Trees & Diversification Characters";
	}
	/*.................................................................................................................*/
	public String getVersion() {
		return null;
	}

	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Evolves a series of trees, each tied to a single character controlling diversification rates." ;
	}
}


