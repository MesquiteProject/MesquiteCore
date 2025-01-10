/* Mesquite source code, Treefarm package.  Copyright 1997 and onward, W. Maddison, D. Maddison and P. Midford. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.treefarm.ReinterpretBranchLabels;

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.tree.AdjustableTree;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.ui.ExtensibleDialog;
import mesquite.lib.ui.RadioButtons;

/* ======================================================================== */
public class ReinterpretBranchLabels extends TreeAltererMult {
	/*.................................................................................................................*/
	public String getName() {
		return "Reinterpret Internal Node Labels...";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Reinterprets labels from all internal nodes as numbers or text attached to nodes or branches." ;
	}
	/*.................................................................................................................*/
	public int getVersionOfFirstRelease(){
		return 272;  
	}
	boolean appliesToBranch = true;
	boolean isNumber = true;
	boolean deleteAfter = true;
	NameReference nameRef = null;
	String name = "";
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (!showOptions())
			return false;
		return true;
	}
	boolean showOptions(){

		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog queryDialog = new ExtensibleDialog(containerOfModule(), "Reinterpret Node Labels",  buttonPressed);
		queryDialog.addLargeOrSmallTextLabel("Some programs write information as node labels; e.g. MrBayes writes posterior probabilities as if they were the names of clades (= node labels)." +
		"\nHere you can reintepret such information.");
		//name for information (e.g., "posteriorProbability", "bootstrapFrequency")
		queryDialog.addLabel("Name for information? (e.g., \"posteriorProbability\", \"bootstrapFrequency\")", Label.LEFT);
		TextField nameField = queryDialog.addTextField(name, 30);

		queryDialog.addHorizontalLine(2);

		//where to attach
		queryDialog.addLabel("Applies to branch or node?", Label.LEFT);
		String[] where  = new String[] {"Information applies to branch (e.g., posterior probability, bootstrap frequency)", "Information applies to node (e.g., clade name)"};
		RadioButtons whereButtons = queryDialog.addRadioButtons (where, 0);
		queryDialog.addLabel("(This determines how the information will behave when the tree is rerooted.)", Label.LEFT);
		queryDialog.addHorizontalLine(2);

		//how to treat
		queryDialog.addLabel("Number or text?", Label.LEFT);
		String[] what  = new String[] {"Treat as number (e.g., posterior probability)", "Treat as text (e.g., clade name)"};
		RadioButtons whatButtons = queryDialog.addRadioButtons (what, 0);
		queryDialog.addHorizontalLine(2);

		//delete internal node labels after reinterpreting?
		Checkbox delete = queryDialog.addCheckBox ("Delete internal node labels after reinterpreting?", deleteAfter);
		queryDialog.addHorizontalLine(2);


		queryDialog.completeAndShowDialog(true);

		boolean ok = (queryDialog.query()==0);

		if (ok) {
			if (StringUtil.blank(nameField.getText())){
				ok = false;
				alert("A name must be entered for the information");
			}
			else {
				name = nameField.getText();
				nameRef = NameReference.getNameReference(name);
				appliesToBranch = whereButtons.getValue() == 0;
				isNumber = whatButtons.getValue() == 0;
				deleteAfter = delete.getState();
			}
		}

		queryDialog.dispose();
		return ok;
	}
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return true;
	}

	void reinterpret(MesquiteTree tree, int node){
		if (tree.nodeIsInternal(node)){
			if (tree.nodeHasLabel(node)){
				String label = tree.getNodeLabel(node);
				if (isNumber){
					double d = MesquiteDouble.fromString(label);
					if (MesquiteDouble.isCombinable(d))
						tree.setAssociatedDouble(nameRef, node, d, appliesToBranch);
				}
				else {
					tree.setAssociatedObject(nameRef, node, label, appliesToBranch);
				}
				if (deleteAfter)
					tree.setNodeLabel(null, node);
			}
			for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter)) {
				reinterpret(tree, daughter);
			}
		}

	}

	/*.................................................................................................................*/
	public  boolean transformTree(AdjustableTree tree, MesquiteString resultString, boolean notify){
		if (!(tree instanceof MesquiteTree))
			return false;
		reinterpret((MesquiteTree)tree, tree.getRoot());
		if (notify && tree instanceof Listened) ((Listened)tree).notifyListeners(this, new Notification(MesquiteListener.NAMES_CHANGED));
		return true;
	}
}

