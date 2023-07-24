/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.SisterLengthsProportionAdjust;
/*~~  */

import java.util.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.trees.BranchLengthsAdjust.BranchLengthsAdjust;

/* ======================================================================== */
public class SisterLengthsProportionAdjust extends TreeDisplayAssistantI {
	public Vector extras;
	public String getFunctionIconPath(){
		return getPath() + "sisterLengthsAdjust.gif";
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		extras = new Vector();
		return true;
	} 
	/*.................................................................................................................*/
	public   TreeDisplayExtra createTreeDisplayExtra(TreeDisplay treeDisplay) {
		SLPAdjustToolExtra newPj = new SLPAdjustToolExtra(this, treeDisplay);
		extras.addElement(newPj);
		return newPj;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Adjust length balance of daughter nodes";
	}

	/*.................................................................................................................*/
	public String getExplanation() {
		return "Provides a tool to adjust the proportion of length allocated to two daughter nodes. Thus, if two daughters had lengths 1.0 and 1.0, the proportion of the " 
				+ "length belong to the left daughter would be 0.5. Adjusting to the proportion to 0.25 would shift the lengths to 0.5 and 1.5. Their total would be constant. This is especially "
				+ "helpful to shift the allocation at the root of the tree, e.g. the balance between the outgroup and study group.";

	}
	public boolean isSubstantive(){
		return false;
	}   	 
}

/* ======================================================================== */
class SLPAdjustToolExtra extends TreeDisplayExtra implements Commandable  {
	TreeTool adjustTool;
	SisterLengthsProportionAdjust selectModule;
	Tree tree;
	double originalX, originalY, lastX, lastY;
	boolean lineOn = true;
	double lastBL;	
	boolean editorOn = false;
	int editorNode = -1;

	public SLPAdjustToolExtra (SisterLengthsProportionAdjust ownerModule, TreeDisplay treeDisplay) {
		super(ownerModule, treeDisplay);
		selectModule = ownerModule;
		adjustTool = new TreeTool(this,  "adjustor", ownerModule.getPath() , "sisterLengthsAdjust.gif", 2,1,"Adjust daughter length proportions", "This tool adjusts branch lengths of left and right daughters, sliding their total branch length between them.");
		adjustTool.setTouchedCommand(MesquiteModule.makeCommand("touchedLengthsAdjust",  this));
		if (ownerModule.containerOfModule() instanceof MesquiteWindow) {
			((MesquiteWindow)ownerModule.containerOfModule()).addTool(adjustTool);
		}
	}
	public void doTreeCommand(String command, String arguments){
		Tree tree = treeDisplay.getTree();
		if (!(tree instanceof MesquiteTree)){
			MesquiteMessage.warnProgrammer("Action can't be completed since tree is not a native Mesquite tree");
		}
		else if (tree!=null) {
			((MesquiteTree)tree).doCommand(command, arguments, CommandChecker.defaultChecker);
		}
	}
	/*.................................................................................................................*/
	public   void drawOnTree(Tree tree, int drawnRoot, Graphics g) {
	}

	/*.................................................................................................................*/
	public   void printOnTree(Tree tree, int drawnRoot, Graphics g) {
	}
	/*.................................................................................................................*/
	public   void setTree(Tree tree) {
		this.tree = tree;
	}
	/*.................................................................................................................*/
	public Tree getTree() {
		return treeDisplay.getTree();
	}

	
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) { 
		MesquiteTree t=null; //
		if (treeDisplay!=null) {
			Tree trt = treeDisplay.getTree();
			if (trt instanceof MesquiteTree)
				t = (MesquiteTree)trt;
			else
				t = null;
		}
		
		if (checker.compare(this.getClass(), "Touch on branch to change lengths of two daughters", "[branch number] [x coordinate touched] [y coordinate touched] [modifiers]", commandName, "touchedLengthsAdjust")) {
			if (t==null)
				return null;
			MesquiteInteger io = new MesquiteInteger(0);
			int node= MesquiteInteger.fromString(arguments, io);
			int x= MesquiteInteger.fromString(arguments, io);
			int y= MesquiteInteger.fromString(arguments, io);
			String mod= ParseUtil.getRemaining(arguments, io);


			//If node has two daughters, proceed
			if (t.numberOfDaughtersOfNode(node)==2) {
				double leftLength = t.	getBranchLength(t.firstDaughterOfNode(node));
				double rightLength = t.	getBranchLength(t.lastDaughterOfNode(node));
				if (leftLength == MesquiteDouble.unassigned && rightLength == MesquiteDouble.unassigned)
					AlertDialog.notice(selectModule.containerOfModule(), "Tool needs assigned branch lengths", "This tool can be applied only when least one of daughters has an assigned branch length.");
				else if (leftLength + rightLength == 0)
					AlertDialog.notice(selectModule.containerOfModule(), "Tool needs nonzero branch lengths", "This tool can be applied only when least one of daughters has a non-zero branch length.");
				else {
					if (leftLength == MesquiteDouble.unassigned)
						leftLength = 0;
					if (rightLength == MesquiteDouble.unassigned)
						rightLength = 0;
					double totalLength = leftLength+rightLength;
					String totalAsString = MesquiteDouble.toStringDigitsSpecified(totalLength, 5);
					double proportionLeft = leftLength/totalLength;
					double newProportion = MesquiteDouble.queryDouble(selectModule.containerOfModule(), "Set proportion", "Branch lengths of the two daughter branches of the node touched sum to " + totalAsString 
							+ ". The proportion of this length currently assigned to the left daughter is currently shown below. " 
							+ "To change this proportion, and thus slide branch length from one daughter to another, please enter below a new proportion of length for the left daughter.", proportionLeft);
					if (!MesquiteDouble.isCombinable(newProportion) || newProportion<0 || newProportion>1) {
						AlertDialog.notice(selectModule.containerOfModule(), "Tool needs nonzero branch lengths", "This tool can be applied only when least one of daughters has a non-zero branch length.");
						return null;
					}
					//OK, at this point, a good new proportion has been returned. Adjust lengths.
					leftLength = totalLength*newProportion;
					rightLength = totalLength - leftLength;
					selectModule.logln("Setting left daughter's length to " + MesquiteDouble.toStringDigitsSpecified(leftLength, 5) + " and right daughter's to " +  MesquiteDouble.toStringDigitsSpecified(rightLength, 5));
					t.setBranchLength(t.firstDaughterOfNode(node), leftLength, false);
					t.setBranchLength(t.lastDaughterOfNode(node), rightLength, false);
					t.notifyListeners(this, new Notification(MesquiteListener.BRANCHLENGTHS_CHANGED));
				
					
				}
			}
			else AlertDialog.notice(selectModule.containerOfModule(), "Tool applies to binary divergences only", "This tool can be applied only to a node with two daughter branches.");

		}
		return null;
	}
	public void turnOff() {

		selectModule.extras.removeElement(this);
		super.turnOff();
	}
}




