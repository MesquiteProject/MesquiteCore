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

import java.awt.Checkbox;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.net.*;

/** ======================================================================== */

public class CopyNewickForOpenTree extends TreeUtility {
	boolean convertToBranchLengths = true;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;  
 	}
 	
	public boolean queryOptions(){
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), "Copy Newick Tree Description for Open Tree", buttonPressed);
		String helpString = "This will copy a tree in Newick format to the clipboard ready to be uploaded into Open Tree (opentreeoflife.org).  It will optionally convert node values "+
		"such as consensus frequences as branch lengths (as that is one way Open Tree imports support values for branches).";
		dialog.appendToHelpString(helpString);
		dialog.setDefaultButton(null);
		Checkbox convertToBranchLengthsBox = dialog.addCheckBox("convert node values to branch lengths", convertToBranchLengths);

		dialog.completeAndShowDialog();

		boolean ok = (dialog.query()==0);

		convertToBranchLengths = convertToBranchLengthsBox.getState();

		dialog.dispose();
		return ok;
	}	

	public  void useTree(Tree treeT) {
		MesquiteTree tree = (MesquiteTree)treeT;
		if (tree == null)
			return;
		Taxa taxa = tree.getTaxa();

		if (queryOptions()) {
			if (convertToBranchLengths && tree instanceof AdjustableTree) {
				OpenTreeUtil.convertNodeValuesToBranchLengths(this,(AdjustableTree)tree);
			}
			Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
			StringSelection ss = new StringSelection(tree.writeSimpleTreeByNamesWithNoAssociated());
			clip.setContents(ss, ss);
		}

	}
	
	
	/** These two methods adjust the vertical positions relative to the leftmost terminal taxon.*/
	void moveOne (Tree tree, int node, Taxa taxa, MesquiteInteger target, MesquiteInteger count){
		if (target.getValue()<0)
			return;
		if (tree.nodeIsTerminal(node)){
			int taxon = tree.taxonNumberOfNode(node);
			if (count.getValue() == target.getValue()) {
				taxa.moveTaxa(taxon, 1, target.getValue()-1, false);
				target.setValue(-1);
				return;
			}
			count.increment();
		}
		else {
			for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d) && target.getValue()>=0; d = tree.nextSisterOfNode(d))
				moveOne(tree, d, taxa, target, count);
		}
	}
	public boolean isSubstantive(){
		return true;
	}
	public boolean isPrerelease(){
		return false;
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
   	 
}


