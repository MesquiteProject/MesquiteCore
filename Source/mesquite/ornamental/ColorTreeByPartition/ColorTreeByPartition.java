/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.ornamental.ColorTreeByPartition;
/*~~  */

import java.util.*;
import java.awt.*;
import java.awt.image.*;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.taxa.TaxaGroup;
import mesquite.lib.taxa.TaxaGroupVector;
import mesquite.lib.taxa.TaxaPartition;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeDisplay;
import mesquite.lib.tree.TreeDisplayEarlyExtra;
import mesquite.lib.tree.TreeDisplayExtra;
import mesquite.lib.ui.ColorDistribution;
import mesquite.lib.ui.MesquiteMenuSpec;

/* ======================================================================== */
public class ColorTreeByPartition extends TreeDisplayAssistantDI {
	public Vector extras;
	MesquiteBoolean colorByPartition;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		extras = new Vector();
		colorByPartition = new MesquiteBoolean(false);
		MesquiteMenuSpec colorMenu = findMenuAmongEmployers("Color");
		MesquiteModule mb = this;
		if (colorMenu != null && colorMenu.getOwnerModule() != null)
			mb = colorMenu.getOwnerModule();
		mb.addCheckMenuItem(colorMenu, "Color Branches by Partition", makeCommand("colorByPartition",  this), colorByPartition);
		return true;
	} 
	/*.................................................................................................................*/
	public   TreeDisplayExtra createTreeDisplayExtra(TreeDisplay treeDisplay) {
		ColorByPartitionExtra newPj = new ColorByPartitionExtra(this, treeDisplay);
		extras.addElement(newPj);
		return newPj;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Color Branches by Partition";
	}

	/*.................................................................................................................*/
	/** return whether or not this module should have snapshot saved when saving a macro given the current snapshot mode.*/
	public boolean satisfiesSnapshotMode(){
		return (MesquiteTrunk.snapshotMode == Snapshot.SNAPALL || MesquiteTrunk.snapshotMode == Snapshot.SNAPDISPLAYONLY);
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("colorByPartition " + colorByPartition.toOffOnString());
		return temp;
	}
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets whether to color the tree by the current taxon partition", "[on or off]", commandName, "colorByPartition")) {
			if (StringUtil.blank(arguments))
				colorByPartition.setValue(!colorByPartition.getValue());
			else
				colorByPartition.toggleValue(parser.getFirstToken(arguments));
			for (int i =0; i<extras.size(); i++){
				ColorByPartitionExtra e = (ColorByPartitionExtra)extras.elementAt(i);
				e.setShowColors(colorByPartition.getValue());
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Colors the tree's branches by the taxon partition." ;
	}
	public boolean isSubstantive(){
		return false;
	}   	 
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 110;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
}

/* ======================================================================== */
class ColorByPartitionExtra extends TreeDisplayExtra implements MesquiteListener, TreeDisplayEarlyExtra   {
	ColorTreeByPartition branchNotesModule;
	TaxaPartition partitions = null;
	ColorDistribution[] colors;
	TaxaGroup[][] groupsAtNode;
	boolean showColors;
	public ColorByPartitionExtra (ColorTreeByPartition ownerModule, TreeDisplay treeDisplay) {
		super(ownerModule, treeDisplay);
		branchNotesModule = ownerModule;
		showColors = branchNotesModule.colorByPartition.getValue();
	}
	/*_________________________________________________*/
	public ColorDistribution colorsInClade(Tree tree, int node){
		if (tree.nodeIsTerminal(node))
			return colors[node];
		ColorDistribution cladeColor = null;
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)){
			ColorDistribution dsColor = colorsInClade(tree, d);
			if (cladeColor == null)
				cladeColor = dsColor;
			else
				cladeColor.concatenate(dsColor);
		}
		return cladeColor;
	}
	/*.................................................................................................................*/
	public   void drawOnTree(Tree tree, int node, Graphics g) {
		if (!tree.isVisibleEvenIfInCollapsed(node))
			return;
		if (showColors) {
			if (needsReharvesting)
				reharvest(tree);

			if (partitions != null){
				for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
					drawOnTree(tree, d, g);
				if (tree.isLeftmostTerminalOfCollapsedClade(node)){
					ColorDistribution cladeColors = colorsInClade(tree, tree.deepestCollapsedAncestor(node));
					if (cladeColors != null && cladeColors.anyColors())
						treeDisplay.getTreeDrawing().fillBranchWithColors(tree,  node, cladeColors, g);
				}
				else if (colors[node].anyColors())
					treeDisplay.getTreeDrawing().fillBranchWithColors(tree,  node, colors[node], g);
			}
		}
	}
	/*.................................................................................................................*/
	public   void printOnTree(Tree tree, int node, Graphics g) {
		drawOnTree(tree, node, g);
	}

	void setShowColors(boolean a){
		showColors = a;
		treeDisplay.pleaseUpdate(false);
	}
	/**return a text version of information at node*/
	public String textAtNode(Tree tree, int node){
		return null;
	}
	/**return a text version of any legends or other explanatory information*/
	public String textForLegend(){
		return null;
	}

	void fillNext(TaxaGroup[] t, TaxaGroup group){
		for (int i = 0; i<t.length; i++)
			if (t[i] == group){ //already there
				return;
			}
		for (int i = 0; i<t.length; i++)
			if (t[i] == null){
				t[i] = group;
				return;
			}

	}
	/*.................................................................................................................*/
	public   void harvestColorsDOWN(Tree tree, int node) {
		if (partitions != null){
			int count = 0;
			if (tree.nodeIsTerminal(node)){
				int taxonNumber = tree.taxonNumberOfNode(node);
				TaxaGroup mi = (TaxaGroup)partitions.getProperty(taxonNumber);
				if (mi!=null) {
					fillNext(groupsAtNode[node], mi); 
				}
			}
			else 	for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d)) {
				harvestColorsDOWN(tree, d);

				for (int i=0; i<groupsAtNode[d].length; i++){
					if (groupsAtNode[d][i] != null)
						fillNext(groupsAtNode[node], groupsAtNode[d][i]);
				}
			}
			int counter = 0;
			for (int i=0; i<groupsAtNode[node].length; i++){
				if (groupsAtNode[node][i] != null)
					colors[node].setColor(counter++, groupsAtNode[node][i].getColor());
			}

		}

	}

	boolean inArray(TaxaGroup[] gs, TaxaGroup g){
		for (int i = 0; i<gs.length; i++)
			if (gs[i] == g)
				return true;
		return false;
	}

	int numDescWithGroup(Tree tree, int node, TaxaGroup c){
		int count = 0;
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			if (inArray(groupsAtNode[d], c))
				count++;
		return count;
	}
	TaxaGroup[] tempGroups;
	/*.................................................................................................................*/
	public   void harvestColorsUP(Tree tree, int node) {
		if (partitions != null){
			if (tree.nodeIsInternal(node)){
				//rule is: if color is found in more than one descendent, the color it

				for (int i=0; i < tempGroups.length; i++)
					tempGroups[i] = null;

				for (int i=0; i<groupsAtNode[node].length; i++) //store groups in temporary space
					fillNext(tempGroups, groupsAtNode[node][i]);

				int count = 0;
				colors[node].initialize();
				for (int i=0; i < groupsAtNode[node].length; i++)
					groupsAtNode[node][i] = null;

				count = 0;
				for (int i=0; i<tempGroups.length; i++){
					if (tempGroups[i] != null){
						int numDesc = numDescWithGroup(tree, node, tempGroups[i]);
						if (numDesc>1 || (numDesc == 1 && node != tree.getRoot() && inArray(groupsAtNode[tree.motherOfNode(node)], tempGroups[i]))){
							groupsAtNode[node][count] = tempGroups[i];
							colors[node].setColor(count++, tempGroups[i].getColor());
						}
					}
				}				
				for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
					harvestColorsUP(tree, d);
			}
		}
	}
	Taxa taxa;
	boolean needsReharvesting = false;
	void reharvest(Tree tree){
		if (tree == null)
			return;
		if (colors == null || colors.length != tree.getNumNodeSpaces())
			colors = new ColorDistribution[ tree.getNumNodeSpaces()];
		if (colors == null)
			return;
		for (int i= 0; i< colors.length; i++){
			if (colors[i] == null)
				colors[i] = new ColorDistribution();
			colors[i].initialize();
		}

		if (taxa == null)
			partitions = (TaxaPartition)tree.getTaxa().getCurrentSpecsSet(TaxaPartition.class);
		else
			partitions = (TaxaPartition)taxa.getCurrentSpecsSet(TaxaPartition.class);
		if (groupsAtNode == null || groupsAtNode.length != tree.getNumNodeSpaces())
			groupsAtNode = new TaxaGroup[tree.getNumNodeSpaces()][];
		TaxaGroupVector groups = (TaxaGroupVector)ownerModule.getProject().getFileElement(TaxaGroupVector.class, 0);
		int numGroups = groups.size();
		if (tempGroups == null || tempGroups.length != numGroups)
			tempGroups = new TaxaGroup[numGroups];
		for (int i= 0; i< groupsAtNode.length; i++){
			if (groupsAtNode[i] == null || groupsAtNode[i].length != numGroups)
				groupsAtNode[i] = new TaxaGroup[numGroups];
			else for (int k = 0; k<groupsAtNode[i].length; k++)
				groupsAtNode[i][k] = null;
		}


		harvestColorsDOWN(tree, tree.getRoot());
		harvestColorsUP(tree, tree.getRoot());
		needsReharvesting = false;
	}
	/*.................................................................................................................*/
	public   void setTree(Tree tree) {
		if (taxa == null && tree != null){			
			taxa = tree.getTaxa();
			taxa.addListener(this);
		}
		needsReharvesting = true;
	}
	/** passes which object changed, along with optional Notification object with details (e.g., code number (type of change) and integers (e.g. which character))*/
	public void changed(Object caller, Object obj, Notification notification){
		if (obj == taxa)
			needsReharvesting = true;
	}
	/** passes which object was disposed*/
	public void disposing(Object obj){
	}
	/** Asks whether it's ok to delete the object as far as the listener is concerned (e.g., is it in use?)*/
	public boolean okToDispose(Object obj, int queryUser){
		return true;
	}
	public void turnOff() {
		branchNotesModule.extras.removeElement(this);
		if (taxa != null)
			taxa.removeListener(this);
		super.turnOff();
	}
}



