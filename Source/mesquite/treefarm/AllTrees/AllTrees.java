/* Mesquite source code.  Copyright 1997-2009 W. Maddison and D. Maddison.
Version 2.7, August 2009.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.treefarm.AllTrees;

import mesquite.lib.*;
import mesquite.lib.duties.TreeSimulate;
import mesquite.lib.duties.TreeSource;

public class AllTrees extends TreeSource {
	int currentTree=0; 
	Taxa currentTaxa;
	static final int maxNumTaxa = 11;
	boolean warned= false;

	MesquiteBoolean rooted = new MesquiteBoolean(true);   // set control for this

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		addMenuItem( "Rooting Options...", makeCommand("rootingOptions",  this));
		if (!MesquiteThread.isScripting()){
			boolean b = queryRootings();
			if (!b) 
				return false;
		}
		return true;
	}

	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("toggleRooted "+ rooted.toOffOnString());
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets whether or not the trees created are rooted.", "[on or off]", commandName, "toggleRooted")) {
			boolean current = rooted.getValue();
			rooted.toggleValue(parser.getFirstToken(arguments));
			if (current!=rooted.getValue())
				parametersChanged();
		}
		else  if (checker.compare(this.getClass(), "Allows user the choice of rooting options", "[]", commandName, "rootingOptions")) {
			boolean current = rooted.getValue();
			if (queryRootings() && current!=rooted.getValue())
				parametersChanged();
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	private boolean queryRootings(){
		String help = "If you select \"rooted trees\", then Mesquite will produce all possible rooted trees. ";
		help+="If you turn this option off, then Mesquite will produce unrooted trees. ";
		MesquiteBoolean mb = MesquiteBoolean.queryCheckBox(containerOfModule(), "Rooting of Trees", "Do you wish Mesquite to create rooted trees or unrooted trees?" , "rooted trees", help, rooted.getValue());
		if (mb.isUnassigned())
			return false;
		rooted.setValue(mb.getValue());
		return true;
	}
	/*.................................................................................................................*/
	private void addTaxon(MesquiteTree tree, int taxonToAdd, MesquiteInteger treesInPiece, MesquiteInteger treeNum) {
		int numPlacesToAdd = taxonToAdd*2-1;
		if (!rooted.getValue())
			numPlacesToAdd-=2;
		int treesInThisPiece = treesInPiece.getValue();
		int treesInNextPiece = treesInThisPiece/ numPlacesToAdd;

		int whichPlace = treeNum.getValue() / treesInNextPiece;
		if (!rooted.getValue())
			whichPlace +=2;

		tree.graftTaxon(taxonToAdd, tree.nodeInTraversal(whichPlace,tree.getRoot()), false);

		treeNum.setValue(treeNum.getValue() % treesInNextPiece);
		treesInPiece.setValue(treesInNextPiece);
	}


	/*.................................................................................................................*/
	public Tree getTree(Taxa taxa, int itree) {
		if (taxa==null)
			return null;
		if (taxa.getNumTaxa()>maxNumTaxa) {
			if (!warned)
				MesquiteMessage.discreetNotifyUser("There are too taxa to create all trees, as there will be too many trees.");
			warned=true;
			return null;
		}
		currentTree = itree;
		int numTaxa = taxa.getNumTaxa();

		MesquiteTree tree = new MesquiteTree(taxa);

		tree.graftTaxon(0, tree.getRoot(), false);
		tree.graftTaxon(1, tree.getRoot(), false);
		tree.deleteClade(2,false);

		//we now have a tree with just two taxa in it
		MesquiteInteger treeNum = new MesquiteInteger(itree);
		MesquiteInteger treesInPiece;
		treesInPiece = new MesquiteInteger(getNumberOfTrees(numTaxa));

		for (int i = 2; i<numTaxa; i++){
			addTaxon(tree, i, treesInPiece, treeNum);
		}

		if (!rooted.getValue())
			tree.setRooted(false, false);  //deroot tree
		return tree;
	}

	/*.................................................................................................................*/
	public int getNumberOfTrees(int numTaxa) {
		if (!rooted.getValue())
			numTaxa--;
		// maximum value of an int is 2,147,483,647; number of rooted trees for 11 taxa is  654,729,075
		if (numTaxa==2) 
			return 1;
		else if (numTaxa<=maxNumTaxa) { // then we can do this without worrying about overflow
			int numTrees = 3;  // number of rooted trees for 3 taxa
			if (numTaxa>3)
				for (int i = 3; i<numTaxa; i++) {
					numTrees *= (2*i-1);
				}
			return numTrees;
		}	
		if (numTaxa>maxNumTaxa) {
			if (!warned)
				MesquiteMessage.discreetNotifyUser("There are too taxa to create all trees, as there will be too many trees.");
			warned=true;
		}
		return 0;
	}

	/*.................................................................................................................*/
	public int getNumberOfTrees(Taxa taxa) {
		int numTaxa = taxa.getNumTaxa();
		return getNumberOfTrees(numTaxa);
	}

	/*.................................................................................................................*/
	/** passes which object changed*/
	public void changed(Object caller, Object obj, Notification notification){
		if (Notification.appearsCosmetic(notification))
			return;
		if (obj == currentTaxa) {
			parametersChanged(notification);
		}
	}
	/*.................................................................................................................*/
	public void setPreferredTaxa(Taxa taxa){
		if (currentTaxa!=null)
			currentTaxa.removeListener(this);
		currentTaxa = taxa;
		if (currentTaxa!=null)
			currentTaxa.addListener(this);
	}
	public void endJob(){
		if (currentTaxa!=null)
			currentTaxa.removeListener(this);
		super.endJob();
	}


	public String getTreeNameString(Taxa taxa, int i) {
		return null;
	}

	public void initialize(Taxa taxa) {
	}

	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return true;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Supplies all possible dichotomous rooted or unrooted trees (only works if number of taxa is <=" + maxNumTaxa + ").";
	}

	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 201;  
	}

	public String getName() {
		return "All Trees";
	}


}
