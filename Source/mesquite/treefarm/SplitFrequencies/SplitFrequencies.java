/* Mesquite source code, Treefarm package.  Copyright 1997 and onward, W. Maddison, D. Maddison and P. Midford. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.treefarm.SplitFrequencies;
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.consensus.lib.Bipartition;
import mesquite.consensus.lib.BipartitionVector;
import mesquite.lib.*;
import mesquite.lib.duties.*;


/* ======================================================================== */
public class SplitFrequencies extends NumbersForNodes {
	Tree otherTree = null;
	TreeSource treeSourceTask;
	MesquiteString treeSourceName;
	MesquiteCommand tstC;
	Taxa taxa;
	boolean suspend = false;
	int startingTree = 0;
	int sampleSize = MesquiteInteger.unassigned;
	MesquiteBoolean useWeights = new MesquiteBoolean(true);
	MesquiteSubmenuSpec colorSubmenu;
	MesquiteInteger colorMode;
	String[] valueCalculatedNames = new String[]{"Frequency of Clade","Maximum Frequency of Contradictory Clade","Difference between Frequency of Clade and Contradictory Clade"};
	MesquiteString valueCalculatedName = new MesquiteString("Frequency of Clade");
	static int calculateCladeFreq = 0;
	static int calculateMaxContradictory = 1;
	static int calculateDifference = 2;
	int valueCalculated = calculateCladeFreq;
	
	MesquiteSubmenuSpec valueCalculatedSubmenu=null;
	

	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e2 = registerEmployeeNeed(TreeSource.class, getName() + " needs a source of trees whose split frequencies will be assessed.",
				"The source of other trees can be indicated initially or later under the Tree Source submenu.");
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {

		valueCalculatedSubmenu = addSubmenu(null, "Value Calculated");
		valueCalculatedSubmenu.setSelected(valueCalculatedName);
		addItemToSubmenu(null, valueCalculatedSubmenu, valueCalculatedNames[calculateCladeFreq], makeCommand("calculateCladeFreq",  this));
		addItemToSubmenu(null, valueCalculatedSubmenu, valueCalculatedNames[calculateMaxContradictory], makeCommand("calculateMaxContradictory",  this));
		addItemToSubmenu(null, valueCalculatedSubmenu, valueCalculatedNames[calculateDifference], makeCommand("calculateDifference",  this));
		
		addCheckMenuItem(null, "Consider Tree Weights", makeCommand("toggleUseWeights", this), useWeights);
//		addCheckMenuItem(null, "Show Maximum Frequency of Contradictory Clades", makeCommand("toggleMaxContradictory", this), maxContradictory);
		treeSourceTask = (TreeSource)hireEmployee(TreeSource.class, "Tree Source");
		if (treeSourceTask == null)
			return sorry(getName() + " couldn't start because no source of trees obtained");
		tstC =  makeCommand("setTreeSource",  this);
		treeSourceTask.setHiringCommand(tstC);
		treeSourceName = new MesquiteString(treeSourceTask.getName());
		if (numModulesAvailable(TreeSource.class)>1) {
			MesquiteSubmenuSpec mss = addSubmenu(null, "Tree Source", tstC, TreeSource.class);
			mss.setSelected(treeSourceName);
		}
		return true;
	}

	public boolean getDefaultShowLabels() {
		return true;
	}
	public boolean getDefaultShadeBranches() {
		return false;
	}
	public boolean getDefaultShadeInColor() {
		return false;
	}
	public boolean getDefaultLabelTerminals() {
		return false;
	}


	/*.................................................................................................................*/
	public void setUseWeights(boolean useWeights){
		this.useWeights.setValue(useWeights);
	}
	/*.................................................................................................................*/
	public boolean getUseWeights(){
		return useWeights.getValue();
	}
	/*.................................................................................................................*/
	public void employeeQuit(MesquiteModule m){
		iQuit();
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("suspend");
		temp.addLine("setTreeSource",treeSourceTask);
		temp.addLine("resume");
  	 	temp.addLine("toggleUseWeights " + useWeights.toOffOnString());
	
   		if (valueCalculated== calculateCladeFreq)
			temp.addLine("calculateCladeFreq"); 
		else if (valueCalculated== calculateMaxContradictory)
			temp.addLine("calculateMaxContradictory"); 
		else if (valueCalculated== calculateDifference)
			temp.addLine("calculateDifference"); 
		return temp;
	}
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the module supplying tree blocks", "[name of module]", commandName, "setTreeSource")) {
			TreeSource temp = (TreeSource)replaceEmployee(TreeSource.class, arguments, "Source of trees", treeSourceTask);
			if (temp!=null) {
				treeSourceTask = temp;
				startingTree = 0;
				treeSourceTask.setHiringCommand(tstC);
				treeSourceName.setValue(treeSourceTask.getName());
				parametersChanged();
			}
			return temp;
		}
		else if (checker.compare(this.getClass(), "Returns treeSourceTask", null, commandName, "getTreeSource")) 
			return treeSourceTask;
		else if (checker.compare(this.getClass(), "Toggles use of weights", null, commandName, "toggleUseWeights")) {
	 		useWeights.toggleValue(parser.getFirstToken(arguments));  
	 		parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets the value to be calculated to the frequency of a node in the set of trees", null, commandName, "calculateCladeFreq")) {
			valueCalculated = calculateCladeFreq;
			valueCalculatedName.setValue(valueCalculatedNames[valueCalculated]);
			valueCalculatedSubmenu.setSelected(valueCalculatedName);
			parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets the value to be calculated to the maximum frequency of a contradictory clade in the set of trees", null, commandName, "calculateMaxContradictory")) {
			valueCalculated = calculateMaxContradictory;
			valueCalculatedName.setValue(valueCalculatedNames[valueCalculated]);
			valueCalculatedSubmenu.setSelected(valueCalculatedName);
			parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets the value to be calculated to the difference between frequency of a node and the maximum frequency of a contradictory clade in the set of trees", null, commandName, "calculateDifference")) {
			valueCalculated = calculateDifference;
			valueCalculatedName.setValue(valueCalculatedNames[valueCalculated]);
			valueCalculatedSubmenu.setSelected(valueCalculatedName);
			parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Suspends calculations", null, commandName, "suspend")) {
			suspend = true;
		}
		else if (checker.compare(this.getClass(), "Resumes calculations", null, commandName, "resume")) {
			suspend = false;
			parametersChanged();
		}
		else 
			super.doCommand(commandName, arguments, checker);
		return null;
	}
	/** Called to provoke any necessary initialization.  This helps prevent the module's initialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Tree tree){
		if (tree ==null)
			return;
		Taxa taxa = tree.getTaxa();
		if (this.taxa!=taxa) {
			this.taxa = taxa;
		}
		if (treeSourceTask!=null)
			treeSourceTask.initialize(tree.getTaxa());
	}
	/*.................................................................................................................*/
	public void setStartingTree(int startingTree) {
		this.startingTree = startingTree;
	}
	/*.................................................................................................................*/
	public void setSampleSize(int sampleSize) {
		this.sampleSize = sampleSize;
	}
	/*.................................................................................................................*/
	public int getNumTrees(Tree tree) {
		if (treeSourceTask==null)
			return 0;
		if (taxa!=tree.getTaxa()) {
			taxa = tree.getTaxa();
			treeSourceTask.initialize(taxa);
		}
		return treeSourceTask.getNumberOfTrees(taxa);
	}
	/*.................................................................................................................*/
	public void initBipartitionVector(Taxa taxa, BipartitionVector bipartitions){
		if (bipartitions!=null) {
			if (valueCalculated!= calculateCladeFreq)
				bipartitions.setMode(BipartitionVector.MAJRULEMODE);
			else
				bipartitions.setMode(BipartitionVector.MATCHMODE);
			bipartitions.setTaxa(taxa);
			bipartitions.zeroFrequencies();
		}
	}
	/*.................................................................................................................*/
	protected void harvestResults(int node, Tree tree, BipartitionVector bipartitions, NumberArray result) {
		if (tree.nodeIsInternal(node)){
			//Bipartition bp = Bipartition.getBipartitionFromNode(tree, node);
			double freq = 0.0;
			if (valueCalculated==calculateMaxContradictory)
				freq = -bipartitions.getMaximumDecimalFrequencyOfContradictoryNode(tree, node, true);
			else if (valueCalculated==calculateDifference) 
				freq = bipartitions.getDecimalFrequencyOfNode(tree, node, true) - bipartitions.getMaximumDecimalFrequencyOfContradictoryNode(tree, node, true);
			else
				freq = bipartitions.getDecimalFrequencyOfNode(tree, node, true);
			result.setValue(node, freq);
		} else
			result.setValue(node, 0.0);
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) 
			harvestResults(d, tree, bipartitions, result);
	}
	public void addTree(BipartitionVector bipartitions, Tree t){
		if (t==null)
			return;
		if (useWeights.getValue()) {
			bipartitions.setUseWeights(useWeights.getValue());
			MesquiteDouble md = (MesquiteDouble)((Attachable)t).getAttachment(TreesManager.WEIGHT);
			if (md != null) {
				if (md.isCombinable())
					bipartitions.setWeight(md.getValue());
				else
					bipartitions.setWeight(1.0);
			} else
				bipartitions.setWeight(1.0);

		}
		bipartitions.addTree(t);
	}
	/*.................................................................................................................*/
	public void calculateNumbers(Tree tree, NumberArray result, MesquiteString resultString) {
		if (result==null)
			return;
		clearResultAndLastResult(result);
		if (resultString!=null)
			resultString.setValue("");
		if (tree == null || treeSourceTask==null)
			return;
		if (taxa!=tree.getTaxa()) {
			taxa = tree.getTaxa();
			treeSourceTask.initialize(taxa);
		}
		Tree otherTree;
		BipartitionVector bipartitions = null;
//		if (maxContradictory.getValue()) {
//			bipartitions = new BipartitionVector();
//		}
//		else
			bipartitions = BipartitionVector.getBipartitionVector(tree);  //start the bipartition vector with all the bipartitions in the tree;
			
		initBipartitionVector(taxa, bipartitions);
		bipartitions.setRooted(false);

		int numTrees = treeSourceTask.getNumberOfTrees(taxa);
		startingTree = 0;
		if (MesquiteInteger.isCombinable(sampleSize)) {
			startingTree = numTrees - sampleSize;
			if (startingTree<0)
				startingTree = 0;
		} 
		boolean done=false;
		int count = 0;
		
		if (numTrees>0 && startingTree <numTrees) {	
			for (int iTree = startingTree; iTree<numTrees && !done; iTree++){
				otherTree = treeSourceTask.getTree(taxa,iTree); 
				if (otherTree!=null) {
					addTree(bipartitions, otherTree);   //now just adjust bipartitions in the vector if they are present in this otherTree
					count++;
				} else
					done = true;
			}
			harvestResults(tree.getRoot(), tree, bipartitions, result);
		}
		logln("Number of trees examined: " + count);
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	/*.................................................................................................................*/
	public String getParameters() {
		if (treeSourceTask ==null)
			return null;
		return "Tree Source: " + treeSourceTask.getTreesDescriptiveString(taxa); 
	}
	/*.................................................................................................................*/
	public String getNameAndParameters(){
		if (treeSourceTask ==null)
			return getName();
		String s = getName()+"\nSource of trees examined: " +  treeSourceTask.getTreesDescriptiveString(taxa); 
		if (valueCalculated==calculateMaxContradictory)
			s+="\nShowing the negative value of the frequency of the contradictory clade that has the maximum frequency";
		else if (valueCalculated==calculateDifference)
			s+="\nShowing the difference between the frequency of a clade and the frequency of the contradictory clade that has the maximum frequency";
		return s;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return true;
	}
	public String getExplanation() {
		return "Calculates, for each clade in the tree, the frequency of that split among the trees in a specified tree source, or the frequency of the contradictory clade with the highest frequency, or the difference.";
	}
	public String getName() {
		return "Clade Frequencies in Trees";
	}


	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 300;  
	}
}
