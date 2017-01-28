/* Mesquite source code, Treefarm package.  Copyright 1997 and onward, W. Maddison, D. Maddison and P. Midford. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.treefarm.Concat2TreeSources;
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.treefarm.lib.*;

/* ======================================================================== */
public class Concat2TreeSources extends TreeSource {
	public String getName() {
		return "Concatenate Two Tree Sources";
	}
	public String getNameForMenuItem() {
		return "Concatenate 2 Tree Sources...";
	}
	public String getExplanation() {
		return "Concatenates two tree sources to yield a tree source first supplying trees from one source, then supplying trees from another.  The trees are marked with values 1 and 2 to indicate the source.";
	}

	public int getVersionOfFirstRelease(){
		return 270;  
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		super.getEmployeeNeeds();
		EmployeeNeed e = registerEmployeeNeed(TreeSource.class, getName() + "  needs a method to modify trees.",
		"The method to modify trees can be chosen initially or in the Transformer of Trees submenu");
	}
	TreeSource treeSourceA, treeSourceB;
	MesquiteString sourceAName, sourceBName;
	MesquiteCommand stA, stB;
	Taxa taxa;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		treeSourceA = (TreeSource)hireEmployee(TreeSource.class, "First source of trees to be concatenated");
		if (treeSourceA == null) {
			return sorry(getName() + " couldn't start because no source of trees was obtained.");
		}
		treeSourceB = (TreeSource)hireEmployee(TreeSource.class, "Second source of trees to be concatenated");
		if (treeSourceB == null) {
			return sorry(getName() + " couldn't start because no source of trees was obtained.");
		}
		sourceAName = new MesquiteString();
		sourceAName.setValue(treeSourceA.getName());
		sourceBName = new MesquiteString();
		sourceBName.setValue(sourceBName.getName());
		stA = makeCommand("setTreeSourceA",  this);
		treeSourceA.setHiringCommand(stA);
		stB = makeCommand("setTreeSourceB",  this);
		treeSourceA.setHiringCommand(stB);
		if (numModulesAvailable(TreeSource.class)>1){
			MesquiteSubmenuSpec mss = addSubmenu(null, "First source of trees to be concatenated", stA, TreeSource.class);
			mss.setSelected(sourceAName);
			MesquiteSubmenuSpec mss2 = addSubmenu(null, "Second source of trees to be concatenated", stB, TreeSource.class);
			mss2.setSelected(sourceBName);
		}

		return true;
	}
	/** Returns the purpose for which the employee was hired (e.g., "to reconstruct ancestral states" or "for X axis").*/
	public String purposeOfEmployee(MesquiteModule employee) {
		if (employee == treeSourceA)
			return "First source of trees"; //transfers info to employer, as ithis is coordinator
		else if (employee == treeSourceB)
			return "Second source of trees";	
		else
			return  "for " + getName();
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return false;  
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = super.getSnapshot(file);
		temp.addLine("setTreeSourceA ", treeSourceA); 
		temp.addLine("setTreeSourceB ", treeSourceB); 
		return temp;
	}
	/*.................................................................................................................*/
	MesquiteInteger pos = new MesquiteInteger(0);
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the first source of trees", "[name of module]", commandName, "setTreeSourceA")) {
			TreeSource temp = (TreeSource)replaceEmployee(TreeSource.class, arguments, "First source of trees", treeSourceA);
			if (temp !=null){
				treeSourceA = temp;
				sourceAName.setValue(treeSourceA.getName());
				treeSourceA.setHiringCommand(stA);
				resetContainingMenuBar();
				parametersChanged();
				return treeSourceA;
			}
		}
		else if (checker.compare(this.getClass(), "Sets the second source of trees", "[name of module]", commandName, "setTreeSourceB")) {
			TreeSource temp = (TreeSource)replaceEmployee(TreeSource.class, arguments, "Second source of trees", treeSourceB);
			if (temp !=null){
				treeSourceB = temp;
				sourceBName.setValue(treeSourceB.getName());
				treeSourceB.setHiringCommand(stB);
				resetContainingMenuBar();
				parametersChanged();
				return treeSourceB;
			}
		}
		else if (checker.compare(this.getClass(), "Sets the number of trees for the first source", "[number of trees]", commandName, "assignNumTreesA")) {
			int newNum = MesquiteInteger.fromFirstToken(arguments, pos);
			if (MesquiteInteger.isCombinable(newNum) && newNum>0 ) {
				numTreesAssignedA = newNum;
				assignedA = true;
			}
			else {
				assignedA = false;
				resetTreeSource(taxa, true);
			}
			parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Sets the number of trees for the second source", "[number of trees]", commandName, "assignNumTreesB")) {
			int newNum = MesquiteInteger.fromFirstToken(arguments, pos);
			if (MesquiteInteger.isCombinable(newNum) && newNum>0 ) {
				numTreesAssignedB = newNum;
				assignedB = true;
			}
			else {
				assignedB = false;
				resetTreeSource(taxa, true);
			}
			parametersChanged();
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	int defaultNumberOfItems = 100;
	int numTreesAssignedA = MesquiteInteger.unassigned;
	int numTreesAssignedB = MesquiteInteger.unassigned;
	boolean assignedA = false;
	boolean assignedB = false;
	MesquiteMenuItemSpec numTreesAItem = null;
	MesquiteMenuItemSpec numTreesBItem = null;
	/*.................................................................................................................*/
	public void resetTreeSource( Taxa taxa, boolean queryPlease){
		int numItems=treeSourceA.getNumberOfTrees(taxa, true); //this can't be infinite; if so then ask for number
		if (!MesquiteInteger.isCombinable(numItems) && MesquiteInteger.finite != numItems) { //not specified; need to set
			if (queryPlease || (!assignedA)) {
				numTreesAssignedA = defaultNumberOfItems;
				if (!MesquiteThread.isScripting()) {
					numTreesAssignedA = MesquiteInteger.queryInteger(containerOfModule(), "Number of Trees", "Number of trees from first source.", numTreesAssignedA);
					if (!MesquiteInteger.isCombinable(numTreesAssignedA)) 
						numTreesAssignedA = defaultNumberOfItems;
				}
			}
			assignedA = true;
			if (numTreesAItem == null) {
				numTreesAItem = addMenuItem( "Number of Trees from First Source...", makeCommand("assignNumTreesA",  this));
				resetContainingMenuBar();
			}
		}
		else  {
			if (numTreesAItem!= null) {
				deleteMenuItem(numTreesAItem);
				resetContainingMenuBar();
				numTreesAItem = null;
			}
			assignedA = false;
			numTreesAssignedA = numItems;

		}
		numItems=treeSourceB.getNumberOfTrees(taxa, true); //this can't be infinite; if so then ask for number
		if (!MesquiteInteger.isCombinable(numItems) && MesquiteInteger.finite != numItems) { //not specified; need to set
			if (queryPlease || (!assignedB)) {
				numTreesAssignedB = defaultNumberOfItems;
				if (!MesquiteThread.isScripting()) {
					numTreesAssignedB = MesquiteInteger.queryInteger(containerOfModule(), "Number of Trees", "Number of trees from second source.", numTreesAssignedB);
					if (!MesquiteInteger.isCombinable(numTreesAssignedB)) 
						numTreesAssignedB = defaultNumberOfItems;
				}
			}
			assignedB = true;
			if (numTreesBItem == null) {
				numTreesBItem = addMenuItem( "Number of Trees from Second Source...", makeCommand("assignNumTreesB",  this));
				resetContainingMenuBar();
			}
		}
		else  {
			if (numTreesBItem!= null) {
				deleteMenuItem(numTreesBItem);
				resetContainingMenuBar();
				numTreesBItem = null;
			}
			assignedB = false;
			numTreesAssignedB = numItems;

		}
	}
	/*.................................................................................................................*/
	public void setPreferredTaxa(Taxa taxa) {
		this.taxa = taxa;
		treeSourceA.setPreferredTaxa(taxa);
		treeSourceB.setPreferredTaxa(taxa);
	}
	/*.................................................................................................................*/
	public void initialize(Taxa taxa) {
		setPreferredTaxa(taxa);
		treeSourceA.initialize(taxa);
		treeSourceB.initialize(taxa);
		
	}

	/*.................................................................................................................*/
	MesquiteColorTable colorTable;
  	public void prepareItemColors(Taxa taxa){
  		colorTable = new MesquiteColorTable();
  		colorTable.setMode(MesquiteColorTable.COLORS_NO_BW);
   	}
  	
   	public Color getItemColor(Taxa taxa, int itree){
   		int whichSource = findSource(taxa, itree);
   		if (whichSource <0 || colorTable == null)
   			return Color.lightGray;
   		else
   			return colorTable.getColor(2, whichSource);

   	}
   	private int findSource(Taxa taxa, int itree){
		setPreferredTaxa(taxa);
		Tree t = null;
		if (numTreesAssignedA != MesquiteInteger.finite) {
			if (itree <numTreesAssignedA)
				return 1;
			else
				return 2;
		}
		else {
			t = treeSourceA.getTree(taxa, itree);
			if (t == null){
				int nA = treeSourceA.getNumberOfTrees(taxa, true);
				if (!MesquiteInteger.isCombinable(nA))
					return -1;
				numTreesAssignedA = nA;
				if (itree>= nA)
					return 2;
			}
			else
				return 1;
		}
		return -1;
 	}
	/*.................................................................................................................*/
	public Tree getTree(Taxa taxa, int itree) {
		setPreferredTaxa(taxa);
		int whichSource = 0;
		Tree t;
		if (numTreesAssignedA != MesquiteInteger.finite) {
			if (itree <numTreesAssignedA){
				t = treeSourceA.getTree(taxa, itree);
				whichSource = 1;
			}
			else{
				t= treeSourceB.getTree(taxa, itree - numTreesAssignedA);
				whichSource = 2;
			}
		}
		else {
			t = treeSourceA.getTree(taxa, itree);
			if (t == null){
				int nA = treeSourceA.getNumberOfTrees(taxa, true);
				if (!MesquiteInteger.isCombinable(nA))
					return null;
				numTreesAssignedA = nA;
				if (itree>= nA){
					t = treeSourceB.getTree(taxa, itree - numTreesAssignedA);
					whichSource = 2;
				}
			}
			else
				whichSource = 1;
		}
		if (t == null)
			return null;
		MesquiteTree tree = new MesquiteTree(taxa);
		if (t instanceof MesquiteTree)
			tree.setToClone((MesquiteTree)t);
		else 
			tree.setToCloneFormOnly(t);
		tree.attach(new MesquiteLong("Which Source",  whichSource));
		return tree;
	}
	/*.................................................................................................................*/
	public int getNumberOfTrees(Taxa taxa) {
		setPreferredTaxa(taxa);
		resetTreeSource(taxa, false);
		if (numTreesAssignedA == MesquiteInteger.finite || numTreesAssignedB == MesquiteInteger.finite)
			return MesquiteInteger.finite;
		return numTreesAssignedA + numTreesAssignedB;
	}
	int numTreesA = MesquiteInteger.unassigned;
	int numTreesB = MesquiteInteger.unassigned;
	/*.................................................................................................................*/
	public String getTreeNameString(Taxa taxa, int itree) {
		Tree t = getTree(taxa, itree);
		if (t == null)
			return "NO TREE";
		return t.getName();
	}
	/*.................................................................................................................*/
	public String getParameters() {
		return"Trees from two sources: " + treeSourceA.getNameAndParameters() + " AND " + treeSourceB.getNameAndParameters();
	}
	/*.................................................................................................................*/
}

