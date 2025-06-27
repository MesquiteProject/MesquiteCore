/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.treefarm.GraftTree;

import java.awt.Checkbox;

import mesquite.lib.CommandChecker;
import mesquite.lib.Listened;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteListener;
import mesquite.lib.MesquiteMessage;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteString;
import mesquite.lib.Notification;
import mesquite.lib.Snapshot;
import mesquite.lib.duties.OneTreeSource;
import mesquite.lib.duties.TreeAltererMult;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.tree.AdjustableTree;
import mesquite.lib.tree.Tree;
import mesquite.lib.ui.ExtensibleDialog;

/* ======================================================================== */
public class GraftTree extends TreeAltererMult {
	public String getName() {
		return "Graft Other Tree";
	}
	public String getNameForMenuItem() {
		return "Graft Other Tree...";
	}
	public String getExplanation() {
		return "Grafts a tree in a tree window onto given tree; requires that the other tree includes none of the same terminal taxa.  If a taxon in receiving tree is selected, graft occurs there; otherwise, graft occurs at root." ;
	}
	OneTreeSource currentTreeSource;
	int[] termsG = null;
	String graftTreeDescription = null;
	boolean removeSelectedNode = false;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		currentTreeSource = (OneTreeSource)hireCompatibleEmployee(OneTreeSource.class, new MesquiteBoolean(false), "Source of tree to be modified");
		if (currentTreeSource == null) {
			return sorry(getName() + " couldn't start because no source of a graftable tree was obtained.");
		}
		if (currentTreeSource == getEmployer())
			return sorry(getName() + " couldn't start because it would be attempting to graft a tree onto itself, resulting in an infinite recursion.");
		return true;
	}

	public void employeeQuit(MesquiteModule m){
		iQuit();
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("setTreeSource ", currentTreeSource); 
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the source of the tree to be grafted", "[name of module]", commandName, "setTreeSource")) {
			OneTreeSource temp = (OneTreeSource)replaceCompatibleEmployee(OneTreeSource.class, "Source of tree to be grafted", currentTreeSource, new MesquiteBoolean(false));
			if (temp !=null){
				currentTreeSource = temp;
				parametersChanged();
				return currentTreeSource;
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
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
	public boolean overlap(int[] a, int[] b){
		if (a == null || b == null)
			return false;
		for (int ia = 0; ia<a.length; ia++){
			int v = a[ia];
			for (int ib = 0; ib<b.length; ib++)
				if (b[ib] == v)
					return true;
		}
		return false;
	}
	/*.................................................................................................................*/
	public boolean queryOptions(boolean singleTerminalSelected) {
//		if (!singleTerminalSelected)
//			return true;
		if (!okToInteractWithUser(CAN_PROCEED_ANYWAY, "Querying Options")) 
			return true;
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), "Graft Tree Options",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
		dialog.addLabel("Graft Tree Options");

		Checkbox deleteSelectedNode = null;
		
		if (singleTerminalSelected)
			deleteSelectedNode = dialog.addCheckBox("delete selected terminal taxon in original tree", removeSelectedNode);
		else
			deleteSelectedNode= dialog.addCheckBox("delete selected clade in original tree", removeSelectedNode);
		
		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			removeSelectedNode = deleteSelectedNode.getState();
		}
		dialog.dispose();
		parser.setPosition(0);
		return (buttonPressed.getValue()==0) ;
	}	
	/*.................................................................................................................*/
	public  boolean transformTree(AdjustableTree tree, MesquiteString resultString, boolean notify){

		Taxa taxa = tree.getTaxa();
		if (!queryOptions(taxa.numberSelected()==1))
				return false;
		
		if (getHiredAs() != TreeAltererMult.class || graftTreeDescription == null){  // if mult, use last one saved
			Tree t =  currentTreeSource.getTree(taxa);
			if (t == null)
				return false;
			termsG = t.getTerminalTaxa(t.getRoot());
			graftTreeDescription = t.writeTree(Tree.BY_NAMES);
			}
		int[] termsR = tree.getTerminalTaxa(tree.getRoot());
		if (overlap(termsR, termsG)){
			if (getHiredAs() != TreeAltererMult.class)
				discreetAlert("Sorry, to graft a tree, it must share NO terminal taxa with the receiving tree");
			else
				MesquiteMessage.warnUser("Sorry, to graft a tree onto " + tree.getName() + ", it must share NO terminal taxa with the receiving tree");
			return false;
		}
 		int node;
 		if (tree.anySelected()) {
			node = tree.insertNode(tree.getFirstSelected(tree.getRoot()), false);
			node = tree.sproutDaughter(node, false);
 		}
 		else if (taxa.numberSelected() ==1 && tree.taxonInTree(taxa.firstSelected())){  
			//node = tree.nodeOfTaxonNumber(taxa.firstSelected());
			node = tree.insertNode(tree.nodeOfTaxonNumber(taxa.firstSelected()), false);
			node = tree.sproutDaughter(node, false);
		}
		else {
			node = tree.insertNode(tree.mrcaSelected(), false);
			node = tree.sproutDaughter(node, false);
		}
		if (!tree.graftCladeFromDescription(graftTreeDescription, node, new MesquiteInteger(0), null))
			return false;

		if (removeSelectedNode) {
			if (tree.anySelected()){
				tree.deleteClade(tree.getFirstSelected(tree.getRoot()),false);
			}
			else if (taxa.numberSelected() ==1 && tree.taxonInTree(taxa.firstSelected())){  
				tree.deleteClade(tree.nodeOfTaxonNumber(taxa.firstSelected()), false);
			} else {
				tree.deleteClade(tree.mrcaSelected(),false);
			}
		}

		if (notify && tree instanceof Listened) ((Listened)tree).notifyListeners(this, new Notification(MesquiteListener.BRANCHES_REARRANGED));
		return true;
	}

	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 273;  
	}

}

