/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.ConstrainNodeAge;
/*~~  */
import java.awt.*;
import java.util.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class ConstrainNodeAge extends TreeDisplayAssistantI {
	public Vector extras;
	public boolean first = true;
	static NameReference nodeAgeConstrRef = NameReference.getNameReference("nodeAgeConstraints");

	public int getVersionOfFirstRelease(){
		return 270;  
	}
	public String getFunctionIconPath(){
		return getPath() + "anchorTool.gif";
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		extras = new Vector();
		setUseMenubar(false); //menu available by touching button
		return true;
	} 
	/*.................................................................................................................*/
	public   TreeDisplayExtra createTreeDisplayExtra(TreeDisplay treeDisplay) {
		BranchNotesToolExtra newPj = new BranchNotesToolExtra(this, treeDisplay);
		extras.addElement(newPj);
		return newPj;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Node Age Constraints";
	}
	public boolean isPrerelease(){
		return true;
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Supplies a tool for tree windows to set node age constraints.  A short text string summarizes constraints.  E.g. to say minimum age is 2.0, enter \"2.0+\".  To say range is 2.0 to 4.0, enter \"2.0-4.0\".  To say maximum is 4.0, enter \"0.0-4.0\".  To say age is exactly 3.0, enter \"3.0\"" ;
	}
	public boolean isSubstantive(){
		return false;
	}   	 
}

/* ======================================================================== */
class BranchNotesToolExtra extends TreeDisplayExtra implements Commandable  {
	TreeTool nodeAgeConstrTool;
	ConstrainNodeAge nodeAgeConstrModule;
	//MesquiteLabel message;
	MesquiteCommand branchCommand;
	MiniStringEditor miniEditor;
	boolean editorOn = false;
	int editorNode = -1;
	Image anchor;
	Font small = new Font("SanSerif", Font.PLAIN, 9);

	public BranchNotesToolExtra (ConstrainNodeAge ownerModule, TreeDisplay treeDisplay) {
		super(ownerModule, treeDisplay);
		anchor = MesquiteImage.getImage(ownerModule.getPath() + "anchor.gif");
		nodeAgeConstrModule = ownerModule;
		nodeAgeConstrTool = new TreeTool(this, "NodeAgeConstraint", ownerModule.getPath(), "anchorTool.gif", 1,1,"Node Age Constraints", "This tool allows you to edit constraints on the ages of nodes of the tree.  Enter one number for fixed age; 0-max for maximum age; min+ for minimum; min-max for range."); 		branchCommand = MesquiteModule.makeCommand("editNodeAgeConstraint",  this);
		nodeAgeConstrTool.setTouchedCommand(branchCommand);
		if (ownerModule.containerOfModule() instanceof MesquiteWindow) {
			((MesquiteWindow)ownerModule.containerOfModule()).addTool(nodeAgeConstrTool);
		//	nodeAgeConstrTool.setPopUpOwner(ownerModule);
		}
	
	}
	StringInABox box = new StringInABox( "", treeDisplay.getFont(),150);
	/*.................................................................................................................*/
	public   void drawOnTree(Tree tree, int node, Graphics g) {
		if (editorOn && node == editorNode) {
			if (tree.nodeExists(editorNode))
				miniEditor.setLocation((int)treeDisplay.getTreeDrawing().x[editorNode], (int)treeDisplay.getTreeDrawing().y[editorNode]);
			else hideMiniEditor();
		}
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			drawOnTree(tree, d, g);
		drawAtNode(tree, node, g, false);
	}
	void drawHS(Graphics g, String s, int x, int y){
		g.setColor(Color.white);
		g.drawString(s, x+1,y+1);
		g.drawString(s, x-1,y+1);
		g.drawString(s, x+1,y-1);
		g.drawString(s, x-1,y-1);
		g.setColor(Color.black);
		g.drawString(s, x,y);
	}
	void drawAtNode(Tree tree, int node, Graphics g, boolean mouse){
		if (getConstraints(tree, node)!=null) {
			Font f = g.getFont();
			g.setFont(small);
			int x = (int)treeDisplay.getTreeDrawing().x[node];  // integer nodeloc approximation
			int y = (int)treeDisplay.getTreeDrawing().y[node]; // integer nodeloc approximation
			g.drawImage(anchor, x, y, treeDisplay);
			//if (mouse){
			//	g.drawString("age", x+11, y + 8);
			//}
			//else {
			String constraint = getConstraints(tree, node);
			drawHS(g, constraint, x+11, y + 9);
			//}
			//StringUtil.highlightString(g, constraint, x+10, y + 10, Color.black, Color.white);
			g.setFont(f);
			//box.setColors(Color.red, Color.white);
			//box.setString("^ " +note);
			//box.draw(g,  x, y);
		}
	}
	private void setMiniEditor(int node, int x,int y){
		Tree t = treeDisplay.getTree();
		if (t==null)
			return;
		editorNode = node;
		if (miniEditor == null) {
			miniEditor = new MiniStringEditor(ownerModule, ownerModule.makeCommand("acceptAge", this));
			addPanelPlease(miniEditor);
		}
		miniEditor.setLocation((int)treeDisplay.getTreeDrawing().x[node], (int)treeDisplay.getTreeDrawing().y[node]);
		miniEditor.setText(getConstraints(t, node));
		miniEditor.setVisible(true);
		miniEditor.prepare();
		editorOn = true;
	}
	private void hideMiniEditor(){
		miniEditor.setVisible(false);
		editorOn = false;
	}

	/**return a text version of information at node*/
	public String textAtNode(Tree tree, int node){
		if (getConstraints(tree, node)!=null)
			return "*(" + node + ")";
		else
			return null;
	}
	/*.................................................................................................................*/
	void getAllConstraints(Tree tree, int node, StringBuffer sb) {
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			getAllConstraints(tree, d, sb);
		String note = getConstraints(tree, node);
		if (note!=null) {
			sb.append("*(" + node + "): " + note + "\n");
		}
	}
	/**return a text version of any legends or other explanatory information*/
	public String textForLegend(){
		StringBuffer sb = new StringBuffer(100);
		getAllConstraints(treeDisplay.getTree(), treeDisplay.getTree().getRoot(), sb);
		return sb.toString();
	}
	/*.................................................................................................................*/
	public   void printOnTree(Tree tree, int drawnRoot, Graphics g) {
		//drawOnTree(tree, drawnRoot, g); //should draw numbered footnotes!
	}
	/*.................................................................................................................*/
	public   void setTree(Tree tree) {
	}
	String getConstraints(Tree tree, int node){
		return (String)tree.getAssociatedObject(ConstrainNodeAge.nodeAgeConstrRef, node);
	}
	Parser parser = new Parser();

	void setNote(Tree tree, int node, String note){
		if (tree instanceof Associable){
			((Associable)tree).setAssociatedObject(ConstrainNodeAge.nodeAgeConstrRef, node, note);
			((Associable)tree).notifyListeners(this, new Notification(MesquiteListener.ANNOTATION_CHANGED));

		}
	}
	//boolean shown = false;
	/**to inform TreeDisplayExtra that cursor has just entered name of terminal taxon M*/
	public void cursorEnterTaxon(Tree tree, int M, Graphics g){
	}
	/**to inform TreeDisplayExtra that cursor has just exited name of terminal taxon M*/
	public void cursorExitTaxon(Tree tree, int M, Graphics g){
	}
	/**to inform TreeDisplayExtra that cursor has just entered branch N*/
	public void cursorEnterBranch(Tree tree, int N, Graphics g){
		drawAtNode(tree, N, g, true);
		/**
		if (nodeAgeConstrTool.getInUse()){
			String link = getConstraints(tree, N);
			if (link!=null) {
				shown = true;
				message.setLocation(treeDisplay.getTreeDrawing().x[N], treeDisplay.getTreeDrawing().y[N]);
				message.setText(link);
				message.setVisible(true);
				message.setCommand(branchCommand);
				message.setArguments(Integer.toString(N));
			}
		}
		/**/
	}
	/**to inform TreeDisplayExtra that cursor has just exited branch N*/
	public void cursorExitBranch(Tree tree, int N, Graphics g){
		drawAtNode(tree, N, g, false);
		/*if (shown) {
			message.setVisible(false);
			//message.setSize(4,4);
		}
		shown = false;*/
	}

	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) { 
		if (checker.compare(this.getClass(), "Edits the node age constraints for the node", "[node number]", commandName, "editNodeAgeConstraint")) {
			Tree tree = treeDisplay.getTree();
			if (!(tree instanceof Associable)){
				ownerModule.alert("Sorry, this is not a standard Mesquite tree, and you can't edit its node age constraints");
				return null;
			}
			int M = MesquiteInteger.fromFirstToken(arguments, pos);
			if (M<0 || !MesquiteInteger.isCombinable(M))
				return null;
			int x= MesquiteInteger.fromString(arguments, pos);
			int y= MesquiteInteger.fromString(arguments, pos);
			String mod= ParseUtil.getRemaining(arguments, pos);
			setMiniEditor(M, x,y);
			return null;
		}
		else if (checker.compare(this.getClass(), "Enter the constraint of the current node", "[age]", commandName, "acceptAge")){
			Tree tree = treeDisplay.getTree();
			if (!(tree instanceof Associable)){
				ownerModule.alert("Sorry, this is not a standard Mesquite tree, and you can't edit its node age constraints");
				return null;
			}
			if (tree==null)
				return null;
			if (editorOn) {
				if (StringUtil.blank(arguments)){
					setNote(tree, editorNode, null);
					hideMiniEditor();
					return null;
				}

				if (arguments == null || "".equalsIgnoreCase(arguments) || "?".equalsIgnoreCase(arguments) || "unassigned".equalsIgnoreCase(arguments)) {
					setNote(tree, editorNode, null);
				}
				else {
					String minS = parser.getFirstToken(arguments);
					String to = parser.getNextToken();

					String maxS = parser.getNextToken();
					if (to != null && to.equals("+")){
						double min = MesquiteDouble.fromString(minS);
						if (MesquiteDouble.isCombinable(min)) {
							setNote(tree, editorNode, minS + "+");
						}
					}
					else {
						if (to != null && to.length()>1 && to.charAt(0) == '-')
							maxS = to.substring(1, to.length());
						double min = MesquiteDouble.fromString(minS);
						double max = MesquiteDouble.fromString(maxS);
						if (MesquiteDouble.isCombinable(min)) {
							if (MesquiteDouble.isCombinable(max))
								setNote(tree, editorNode, minS + "-" + maxS);
							else
								setNote(tree, editorNode, minS);
						}
					}
				}
			}
			hideMiniEditor();
		}
		/**
    	 	else 
    	 		return  super.doCommand(commandName, arguments, checker);
    	 	/**/

		return null;
	}
	public void turnOff() {
		nodeAgeConstrModule.extras.removeElement(this);
		super.turnOff();
	}
}


