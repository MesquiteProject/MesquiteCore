/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.opentree.CopyNewickForOpenTree;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.opentree.lib.OpenTreeUtil;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.*;

/** ======================================================================== */

public class CopyNewickForOpenTree extends TreeUtility implements ItemListener {
	boolean convertToBranchLengths = true;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;  
	}

	Checkbox[] nodeAssociatedLabels;
	int associatedNumberToUseAsLabel = -1;

	/*.................................................................................................................*/
	public boolean queryOptions(Tree tree){
		ListableVector names = new ListableVector();
		ListableVector v = new ListableVector();
		int num = tree.getNumberAssociatedDoubles();
		boolean[] shown = new boolean[num + names.size()]; //bigger than needed probably
		int count = 0;
		for (int i = 0; i< num; i++){
			DoubleArray da = tree.getAssociatedDoubles(i);
			if (da != null){
				if (StringUtil.notEmpty(da.getName())){
					count++;
				}
			}
		}
		if (count==0)
			return true;

		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), "Copy Newick Tree Description for Open Tree", buttonPressed);
		String helpString = "This will copy a tree in Newick format to the clipboard ready to be uploaded into Open Tree (opentreeoflife.org). " +
				" It will optionally convert node values "+
				"such as consensus frequences as branch lengths (as that is one way Open Tree imports support values for branches).";
		dialog.appendToHelpString(helpString);
		dialog.setDefaultButton(null);

		dialog.addLabel("Value to include as branch labels", Label.CENTER);


		String[] nodeAssocDoubleLabels = new String[count];
		count=0;
		for (int i = 0; i< num; i++){
			DoubleArray da = tree.getAssociatedDoubles(i);
			if (da != null){
				if (StringUtil.notEmpty(da.getName())){
					nodeAssocDoubleLabels[count]=da.getName();
					count++;
				}
			}
		}
		Choice nodeAssocDoublesChoice = dialog.addPopUpMenu("Node associated doubles to write as node labels", nodeAssocDoubleLabels, 0);

		//Checkbox convertToBranchLengthsBox = dialog.addCheckBox("convert node values to branch lengths", convertToBranchLengths);

		dialog.completeAndShowDialog();

		boolean ok = (dialog.query()==0);

		if (ok) {
			String selectedItem = nodeAssocDoublesChoice.getSelectedItem();
			count=0;
			for (int i = 0; i< num; i++){
				DoubleArray da = tree.getAssociatedDoubles(i);
				if (da != null){
					if (StringUtil.notEmpty(da.getName())){
						if (da.getName().equals(selectedItem)){
							associatedNumberToUseAsLabel=i;
							break;
						}
						count++;
					}
				}
			}
		}

		//		convertToBranchLengths = convertToBranchLengthsBox.getState();

		dialog.dispose();
		return ok;
	}	


	/*.................................................................................................................*/
	public void itemStateChanged(ItemEvent e){

		for (int i=0; i<nodeAssociatedLabels.length; i++) {
			if (e.getItemSelectable() == nodeAssociatedLabels[i]){  //we have clicked on one
				if (nodeAssociatedLabels[i].getState()) {  // this one is on, need to turn the rest off
					for (int j=0; j<nodeAssociatedLabels.length; j++) {
						if (i!=j)
							nodeAssociatedLabels[j].setState(false);
					}
				}
			}
		}

	}

	/*.................................................................................................................*/

	public  void useTree(Tree treeT) {
		if (treeT instanceof MesquiteTree) {
			OpenTreeTree tree = new OpenTreeTree(treeT.getTaxa());
			tree.setToClone((MesquiteTree)treeT);
			if (tree == null)
				return;
			Taxa taxa = tree.getTaxa();

			if (queryOptions(treeT)) {
				if (convertToBranchLengths && tree instanceof AdjustableTree) {
					//OpenTreeUtil.convertNodeValuesToBranchLengths(this,(AdjustableTree)tree);
				}
				Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
				String description = tree.writeSimpleTreeByNamesWithNoAssociated();
				StringSelection ss = new StringSelection(description);
				clip.setContents(ss, ss);
			}
		}

	}
	/*.................................................................................................................*/


	public boolean isSubstantive(){
		return true;
	}
	public boolean isPrerelease(){
		return true;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Copy Newick Tree for Open Tree";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "This will copy a tree in Newick format to the clipboard ready to be uploaded into Open Tree (opentreeoflife.org).  It will optionally convert node values such as consensus frequences as branch lengths (as that is one way Open Tree imports support values for branches).";
	}

	/*.................................................................................................................*/
	/*.................................................................................................................*/

	class OpenTreeTree extends MesquiteTree {

		public OpenTreeTree(Taxa taxa) {
			super(taxa);
		}

		/*-----------------------------------------*/
		/** Returns a string describing the tree in standard parenthesis notation (Newick standard), using taxon
		names or numbers to refer to the taxa, depending on the boolean parameter byNames.*/
		public String writeSimpleTreeByNamesWithNoAssociated() {
			StringBuffer s = new StringBuffer(numberOfNodesInClade(root)*40);
			//	private void writeTreeByNames(int node, StringBuffer treeDescription, boolean includeBranchLengths, boolean includeAssociated, boolean associatedUseComments) {
			writeTreeByNames(root, s, true, false, false);
			s.append(';');
			return s.toString();
		}

		/*-----------------------------------------*/
		/** Writes a tree description into the StringBuffer using taxon names */
		private void writeTreeByNames(int node, StringBuffer treeDescription, boolean includeBranchLengths, boolean includeAssociated, boolean associatedUseComments) {
			if (nodeIsInternal(node)) {
				treeDescription.append('(');
				int thisSister = firstDaughterOfNode(node);
				writeTreeByNames(thisSister, treeDescription, includeBranchLengths, includeAssociated, associatedUseComments);
				while (nodeExists(thisSister = nextSisterOfNode(thisSister))) {
					treeDescription.append(',');
					writeTreeByNames(thisSister, treeDescription,includeBranchLengths, includeAssociated, associatedUseComments);
				}
				treeDescription.append(')');
				if (nodeHasLabel(node))
					treeDescription.append(StringUtil.tokenize(getNodeLabel(node)));
			}
			else {
				treeDescription.append(StringUtil.tokenize(taxa.getTaxonName(taxonNumberOfNode(node))));
			}
			if ( includeBranchLengths && !branchLengthUnassigned(node)) {
				treeDescription.append(':');
				treeDescription.append(MesquiteDouble.toStringDigitsSpecified(getBranchLength(node), -1)); //add -1 to signal full accuracy 17 Dec 01
			}
			if (includeAssociated){
				String a = writeAssociated(node, associatedUseComments);
				treeDescription.append(a);
			}
		}


	}

}


