/* Mesquite source code.  Copyright 1997 and onward, W. Maddison. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.coalesce.RecCoalescenceHistory;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.assoc.lib.*;
/* ======================================================================== */
public class RecCoalescenceHistory extends ReconstructAssociation {
	MesquiteBoolean reconstruct = new MesquiteBoolean(true); //currently cannot be set false; option buggy and not available
	MesquiteBoolean unrootedContained = new MesquiteBoolean(false);
	MesquiteBoolean allowResolve = new MesquiteBoolean(true);
	MesquiteBoolean  useBranchLengths = new MesquiteBoolean(true);
	Vector rerootVectors;
	static final int maxRerootStores = 20;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		
 		if (!MesquiteThread.isScripting()){
			MesquiteInteger buttonPressed = new MesquiteInteger(1);
			ExtensibleDialog checkBoxDialog = new ExtensibleDialog(containerOfModule(), "Contained (gene) tree interpetation",buttonPressed);
			checkBoxDialog.addLargeOrSmallTextLabel ("Options for interpreting contained tree (e.g., gene tree within species or population history.)");
			Checkbox ur = checkBoxDialog.addCheckBox("Treat contained as unrooted", unrootedContained.getValue());
			Checkbox polyAutoRes = checkBoxDialog.addCheckBox("Contained polytomies auto-resolve", allowResolve.getValue());
			Checkbox useLen = checkBoxDialog.addCheckBox("Use Branch lengths of Contained tree", useBranchLengths.getValue());
			
			checkBoxDialog.completeAndShowDialog(true);
			
			if (buttonPressed.getValue()==0) {
				unrootedContained.setValue(ur.getState());
				allowResolve.setValue(polyAutoRes.getState());
				useBranchLengths.setValue(useLen.getState());
			}
			checkBoxDialog.dispose();
 		}
		MesquiteSubmenuSpec mss = addSubmenu(null, "Reconstruct Coalescence");
		addCheckMenuItemToSubmenu( null, mss, "Treat contained as unrooted", makeCommand("toggleUnrooted", this), unrootedContained);
		addCheckMenuItemToSubmenu( null, mss, "Contained polytomies auto-resolve", makeCommand("toggleResolve", this), allowResolve);
		addCheckMenuItemToSubmenu( null, mss, "Use Branch lengths of Contained", makeCommand("toggleUseLengths", this), useBranchLengths);
		rerootVectors = new Vector();
 		return true;
  	 }
  	 
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) { 
   	 	Snapshot temp = new Snapshot();
		//temp.addLine("toggleReconstruct " + reconstruct.toOffOnString()); //this is currently buggy and not available
		temp.addLine("toggleUnrooted " + unrootedContained.toOffOnString());
		temp.addLine("toggleResolve " + allowResolve.toOffOnString());
		temp.addLine("toggleUseLengths " + useBranchLengths.toOffOnString());
  	 	return temp;
  	 }
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Sets whether or not the containing tree reconstructor attempts to reconstruct ancestral containing nodes, or finds containing node information already in the history", "[on = reconstruct; off]", commandName, "toggleReconstruct")) {
    	 		boolean current = reconstruct.getValue(); //this is currently buggy and not available
    	 		reconstruct.toggleValue(parser.getFirstToken(arguments));
    	 		if (current!=reconstruct.getValue()) {
    	 			discreetAlert( "There are bugs in the use of existing gene ancestor locations; reconstruction seems to be working well, but it places gene ancestors as shallowly as possible.");
    	 			parametersChanged();
    	 		}
    	 	}
    	 	else if (checker.compare(this.getClass(), "Sets whether or not the contained tree is treated as unrooted", "[on = unrooted; off]", commandName, "toggleUnrooted")) {
    	 		boolean current = unrootedContained.getValue();
    	 		if (StringUtil.blank(arguments)) {
    	 			unrootedContained.setValue(!current);
	    	 		parametersChanged();
    	 		}
    	 		else {
	    	 		unrootedContained.toggleValue(parser.getFirstToken(arguments));
	    	 		if (current!=unrootedContained.getValue()) {
	    	 			parametersChanged();
	    	 		}
    	 		}
    	 	}
    	 	else if (checker.compare(this.getClass(), "Sets whether or not any branch lengths of the contained tree are used in fitting it into the containing tree", "[on = use lengths; off]", commandName, "toggleUseLengths")) {
    	 		boolean current = useBranchLengths.getValue();
    	 		if (StringUtil.blank(arguments)) {
    	 			useBranchLengths.setValue(!current);
	    	 		parametersChanged();
    	 		}
    	 		else {
	    	 		useBranchLengths.toggleValue(parser.getFirstToken(arguments));
	    	 		if (current!=useBranchLengths.getValue()) {
	    	 			parametersChanged();
	    	 		}
    	 		}
    	 	}
    	 	else if (checker.compare(this.getClass(), "Sets whether or not polytomies in the contained tree are auto-resolved to minimize deep coalescences", "[on = auto-resolve; off]", commandName, "toggleResolve")) {
    	 		boolean current = allowResolve.getValue();
    	 		if (StringUtil.blank(arguments)) {
    	 			allowResolve.setValue(!current);
	    	 		parametersChanged();
    	 		}
    	 		else {
	    	 		allowResolve.toggleValue(parser.getFirstToken(arguments));
	    	 		if (current!=allowResolve.getValue()) {
	    	 			parametersChanged();
	    	 		}
    	 		}
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
		return null;
   	 }

	/*_________________________________________________*/
	public void calculateHistoryCost(Tree containing, MesquiteTree contained, TaxaAssociation association, MesquiteNumber cost, MesquiteString resultString){
		if (cost == null)
			return;
		reconstructHistory(containing, contained, association, cost, resultString);
	}

	/*_________________________________________________*/
	RerootStore getRerootings(MesquiteTree tree){
		//go through stored rerootings to find same one
		
		for (int i=0; i<rerootVectors.size(); i++) {
			RerootStore rs = (RerootStore)rerootVectors.elementAt(i);
			if (rs.sameTree(tree))
				return rs;
		}
		RerootStore rerootings = new RerootStore(tree.getTaxa(), tree.writeTree());
		tree.makeAllRootings(tree.getRoot(), rerootings);
		for (int i = 0; i<rerootings.size(); i++){
			MesquiteTree cr = (MesquiteTree)rerootings.getTree(i);
			cr.setAllBranchLengths(MesquiteDouble.unassigned, false);
		}
		rerootVectors.addElement(rerootings);
		if (rerootVectors.size()>=maxRerootStores) {
			RerootStore rerootings0 = (RerootStore)rerootVectors.elementAt(0);
			rerootings0.dispose();
			rerootVectors.removeElement(rerootings0);
		}
		return rerootings;
		
	}
	/*_________________________________________________*/
	/** reconstructs history of contained tree within containing.  Contained tree must be free to be modified; hence send in a clone instead of the original!*/
	public AssociationHistory reconstructHistory(Tree containing, MesquiteTree contained, TaxaAssociation association, MesquiteNumber cost, MesquiteString resultString){
		if (containing==null || contained==null || association == null) {
			if (resultString!=null)
				resultString.setValue("Association history not reconstructed (some information not supplied to calculating module)");
			return null;
		}
		if (contained!=null)
			contained.deselectAll();
			
        	/*tree coming in is gene tree, hence need to find out for each taxon in gene tree if it has more than one associate.  If so, then 
        	can't do calculations since gene copy in more than one species*/
		for (int i=0; i< contained.getTaxa().getNumTaxa(); i++){
			Taxon tax = contained.getTaxa().getTaxon(i);
			if (association.getNumAssociates(tax)>1){
				if (resultString!=null)
					resultString.setValue("Contained history not calculated (some contained taxa are in more than one containing taxon)");
				if (cost !=null)
					cost.setToUnassigned();
				return null;
			}
		}
		
		MesquiteInteger count = new MesquiteInteger(0);  //for counting deep coalescences
		
		if (unrootedContained.getValue()){  //treating contained tree as unrooted
			AssociationHistory resultHistory = null;
			int minCount = MesquiteInteger.unassigned;
			
			//Get rerootings of contained tree.  If contained tree was recently seen, rerootings don't need to be recalculated.
			RerootStore rerootings = getRerootings(contained);
			
			//Reconstruct History for all rerootings and choose best
			MesquiteTree bestTree = null;
			for (int i = 0; i<rerootings.size(); i++){
				AssociationHistory history = new AssociationHistory();
				MesquiteTree containedRerooted = (MesquiteTree)rerootings.getTree(i);
				history.setTrees(containing, containedRerooted);
				count.setValue(0);
				if (containedRerooted!=null)
					containedRerooted.deselectAll();
				calcContainedNodes(history, containing, containedRerooted, association, containing.getRoot(), false, count);
			
				if (!MesquiteInteger.isCombinable(minCount) || count.getValue()<minCount) {
					minCount = count.getValue();
					resultHistory = history; //just takes first one!
					bestTree = containedRerooted;
					if (cost!=null)
						cost.setValue(minCount);
				}
			}
			if (bestTree!=null)
				contained.setToClone(bestTree);
			if (resultString!=null && cost!=null)
				resultString.setValue("Deep coalescence cost: " + cost);
			return resultHistory;
		}
		else if (reconstruct.getValue()) { //treating contained tree as rooted; reconstruct its placement in containing tree
			if (!useBranchLengths.getValue()){
				contained.setAllBranchLengths(MesquiteDouble.unassigned, false);
			}
			AssociationHistory history = new AssociationHistory();
			history.setTrees(containing, contained);
			calcContainedNodes(history, containing, contained, association, containing.getRoot(), containing.hasBranchLengths() && contained.hasBranchLengths() && useBranchLengths.getValue(), count);
			if (cost!=null)
				cost.setValue(count.getValue());
			if (resultString!=null && cost!=null)
				resultString.setValue("Deep coalescence cost: " + cost);
			return history;
		}
		else {  //treating contained tree as rooted; look for its stored placement in containing tree (stored in "location" stored inside contained tree.  NOT AVAILABLE as option
			AssociationHistory history = new AssociationHistory();
			history.setTrees(containing, contained);
			LongArray locations =contained.getWhichAssociatedLong(NameReference.getNameReference("location"));
			if (locations!=null) {
				recoverContainedNodes(history, locations, contained, contained.getRoot());
				reconnect(history, containing,contained, association, containing.getRoot());
			}
			else {
				calcContainedNodes(history, containing, contained, association, containing.getRoot(), containing.hasBranchLengths() && contained.hasBranchLengths() && useBranchLengths.getValue(), count);
			}
			if (cost!=null)
				cost.setValue(count.getValue());
			if (resultString!=null && cost!=null)
				resultString.setValue("Deep coalescence cost: " + cost);
			
			return history;
		}
	}
	/*_________________________________________________*/
	/** this method recurses up the contained (gene?) tree to help reconnect a contained tree which already has location information, into the containing tree.  NOT AVAILABLE yet*/
	private void recoverContainedNodes(AssociationHistory history, LongArray locations, Tree containedTree, int node) {
				int loc = (int)locations.getValue(node);
				if (loc>0 && MesquiteInteger.isCombinable(loc))
					history.setContainedNode(loc, node);
				for (int d = containedTree.firstDaughterOfNode(node); containedTree.nodeExists(d); d = containedTree.nextSisterOfNode(d)) 
					recoverContainedNodes(history, locations,containedTree, d);
	}
	Taxon[] containedHere;
	/*_________________________________________________*/
	/** this method recurses up the containing (species?) tree and calculates how the contained tree fits into it */
	private void calcContainedNodes(AssociationHistory history, Tree containingTree, AdjustableTree containedTree, TaxaAssociation association, int node, boolean useDepths, MesquiteInteger count) {
		if (containingTree.nodeExists(node)) {
		
			if (containingTree.nodeIsTerminal(node)) { //containing node is terminal: simply set contained nodes within it
				int taxonNum = containingTree.taxonNumberOfNode(node);
				Taxon terminalTaxon =containingTree.getTaxa().getTaxon(taxonNum); //which containing terminal taxon are we at?
				if (containedHere==null ||  containedHere.length < containedTree.getTaxa().getNumTaxa())
					containedHere = new Taxon[containedTree.getTaxa().getNumTaxa()];
				containedHere = association.getAssociates(terminalTaxon, containedHere);
				if (containedHere != null&& containedHere.length!=0) { //there are contained-taxa contained within this containing-taxon
					for (int i=0; i<containedHere.length && containedHere[i]!=null; i++) {  //stores in AssociationHistory fact that these genes are contained in this species
						int containedNode = containedTree.nodeOfTaxonNumber(containedTree.getTaxa().whichTaxonNumber(containedHere[i]));
						history.setContainedNode(node, containedNode);
					}
				}
			}
			else {//containing node is internal: recurse up the tree, and on way down ask to condense the genes within each of the daughters to the representatives of monophyletic groups
				//first recurse up the tree
				for (int d = containingTree.firstDaughterOfNode(node); containingTree.nodeExists(d); d = containingTree.nextSisterOfNode(d)) 
					calcContainedNodes(history, containingTree,containedTree, association, d, useDepths, count);
				
				//on way down, for each of containing daughter nodes, condense contained nodes at tip of daughter so as to yield contained clades at base of daughter
				for (int d = containingTree.firstDaughterOfNode(node); containingTree.nodeExists(d); d = containingTree.nextSisterOfNode(d)) {
					if (history.getNumberContainedNodes(d)!=0) {
						//most of the hard work is done by AssociationHistory's condenseClades method
						int[] temp2 = history.condenseClades(containedTree, containingTree, d, history.getContainedNodes(d), allowResolve.getValue(), useDepths);  //this shouldn't condense if depth of node is greater than dept of containing node
						if (temp2!= null) {
							for (int i = 0; i<temp2.length; i++) {
								history.setContainedNode(node, temp2[i]);
							}
							if (!containingTree.nodeIsUnbranchedInternal(node) && temp2.length>0) //don't add if has only a single descendant (in which case no deep coalescence suffered yet))
								count.add(temp2.length -1);   //v. 2. 01 build j27 and before failed to have the temp2.length>0 restriction, hence added -1 to count if no lineages in population
						}
						
					}
				}
			}
		}
	}
	/*_________________________________________________*/
	/** this method recurses up the containing (species?) tree reconnecting a contained tree which already has location information, into the containing tree.  NOT AVAILABLE yet*/
	private void reconnect(AssociationHistory history, Tree containingTree, Tree containedTree, TaxaAssociation association, int node) {
		if (containingTree.nodeIsTerminal(node)) { //terminal
			int taxonNum = containingTree.taxonNumberOfNode(node);
			Taxon terminalTaxon =containingTree.getTaxa().getTaxon(taxonNum); //which containing terminal taxon are we at?
			if (containedHere==null ||  containedHere.length < containedTree.getTaxa().getNumTaxa())
				containedHere = new Taxon[containedTree.getTaxa().getNumTaxa()];
			containedHere = association.getAssociates(terminalTaxon, containedHere);
			if (containedHere != null&& containedHere.length!=0) { //there are contained-taxa contained within this containing-taxon
				for (int i=0; i<containedHere.length  && containedHere[i]!=null; i++) {  //stores in AssociationHistory fact that these genes are contained in this species
					int containedNode = containedTree.nodeOfTaxonNumber(containedTree.getTaxa().whichTaxonNumber(containedHere[i]));
					int anc = node;
					while (!history.isAncestorContained(anc, containedNode, containedTree) && anc != containingTree.getSubRoot()) {
						if (!history.isNodeContained(anc, containedNode))
							history.setContainedNode(anc, containedNode);
						anc = containingTree.motherOfNode(anc);
					}
				}
			}
		}
		else {
		 	for (int d = containingTree.firstDaughterOfNode(node); containingTree.nodeExists(d); d = containingTree.nextSisterOfNode(d)) {
				reconnect(history, containingTree,containedTree, association, d);
				int[] dContained = history.getContainedNodes(d);
				if (dContained!=null)
					for (int i=0; i<dContained.length; i++) {
						if (!history.isNodeContained(node, dContained[i])  && !history.isAncestorContained(node, dContained[i], containedTree))
							history.setContainedNode(node, dContained[i]);
					}
			}
		}
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Reconstruct Deep Coalescence";
   	 }
   	 
	/*.................................................................................................................*/
   	 
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Reconstructs a contained tree within a containing tree so as to minimize the amount of deep coalescence (failure of lineage sorting).  The contained tree is assumed to be rooted." ;
   	 }
	/*.................................................................................................................*/
   	public boolean isPrerelease(){
   		return false;
   	}
   	 public boolean showCitation(){
   	 	return true;
   	 }
}

class RerootStore extends TreeVector{
	String desc;
	public RerootStore(Taxa taxa, String desc){
		super(taxa);
		this.desc = desc;
	}
	boolean sameTree(MesquiteTree tree){
		return tree.writeTree().equals(desc); //doesn't use tree reference in case original is being cloned multiply
	}
}

