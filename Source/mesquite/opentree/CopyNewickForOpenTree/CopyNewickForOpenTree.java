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
import java.awt.Label;
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
 	
	/* have node value: branch length 
	/*.................................................................................................................*/
	void showChoiceDialog(Associable tree, ListableVector names) {
		if (tree == null)
			return;
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ListableVector v = new ListableVector();
		int num = tree.getNumberAssociatedDoubles();
		boolean[] shown = new boolean[num + names.size()]; //bigger than needed probably
		for (int i = 0; i< num; i++){
			DoubleArray da = tree.getAssociatedDoubles(i);
			if (da != null){
				v.addElement(new MesquiteString(da.getName(), ""), false);
			if (names.indexOfByName(da.getName())>=0)
				shown[i] = true;
			}
		}
		for (int i = 0; i<names.size(); i++){
			String name = ((MesquiteString)names.elementAt(i)).getName();
			if (v.indexOfByName(name)<0){
				v.addElement(new MesquiteString(name, " (not in current tree)"), false);
				if (v.size()-1>= shown.length)
					shown[v.size()-1] = true;
			}
		}
		if (v.size()==0)
			alert("This Tree has no values associated with nodes");
		else {
			ExtensibleDialog queryDialog = new ExtensibleDialog(containerOfModule(), "Values to show",  buttonPressed);
			queryDialog.addLabel("Values to display on tree", Label.CENTER);
			Checkbox[] checks = new Checkbox[v.size()];
			for (int i=0; i<v.size(); i++){
				MesquiteString ms = (MesquiteString)v.elementAt(i);
				checks[i] = queryDialog.addCheckBox (ms.getName() + ms.getValue(), shown[i]);
			}

			queryDialog.completeAndShowDialog(true);

			boolean ok = (queryDialog.query()==0);

			if (ok) {
				names.removeAllElements(false);
				for (int i=0; i<checks.length; i++){
					MesquiteString ms = (MesquiteString)v.elementAt(i);
					if (checks[i].getState())
						names.addElement(new MesquiteString(ms.getName(), ms.getName()), false);
				}
/*				for (int i =0; i<extras.size(); i++){
					NodeAssocValuesExtra e = (NodeAssocValuesExtra)extras.elementAt(i);
					e.setOn(on.getValue());
				}
*/			}

			queryDialog.dispose();
		}
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
   	 
}


