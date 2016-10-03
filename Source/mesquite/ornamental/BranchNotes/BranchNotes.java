/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.ornamental.BranchNotes;
/*~~  */

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class BranchNotes extends TreeDisplayAssistantI {
	public Vector extras;
	public boolean first = true;
	public Image asterisk;
	MesquiteBoolean alwaysOn;
	
	public String getFunctionIconPath(){
		return getPath() + "asteriskTool.gif";
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		extras = new Vector();
		asterisk = MesquiteImage.getImage(getPath() + "asterisk.gif", false);   //TODO:  add asterisk.gif!
		alwaysOn = new MesquiteBoolean(false);
		addCheckMenuItem(null, "Show Notes On Tree", makeCommand("setAlwaysOn",  this), alwaysOn);
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
		return "Annotate Branches";
   	 }
   	 
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) {
  	 	Snapshot temp = new Snapshot();
  	 	temp.addLine("setAlwaysOn " + alwaysOn.toOffOnString());
  	 	return temp;
  	 }
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Sets whether to show the node notes always, or only when the cursor passes over", "[on or off]", commandName, "setAlwaysOn")) {
			if (StringUtil.blank(arguments))
				alwaysOn.setValue(!alwaysOn.getValue());
			else
				alwaysOn.toggleValue(parser.getFirstToken(arguments));
			for (int i =0; i<extras.size(); i++){
				BranchNotesToolExtra e = (BranchNotesToolExtra)extras.elementAt(i);
				e.setShowAlways(alwaysOn.getValue());
			}
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
		return null;
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Supplies a tool for tree windows to attach and view footnotes for branches." ;
   	 }
	public boolean isSubstantive(){
		return false;
	}   	 
}

/* ======================================================================== */
class BranchNotesToolExtra extends TreeDisplayExtra implements Commandable  {
	TreeTool branchNotesTool;
	BranchNotes branchNotesModule;
	MesquiteLabel message;
	MesquiteCommand taxonCommand, branchCommand;
	boolean showAlways;
//	StringInABox
	public BranchNotesToolExtra (BranchNotes ownerModule, TreeDisplay treeDisplay) {
		super(ownerModule, treeDisplay);
		branchNotesModule = ownerModule;
		showAlways = branchNotesModule.alwaysOn.getValue();
		branchNotesTool = new TreeTool(this, "BranchNotes", ownerModule.getPath(), "asteriskTool.gif", 1,1,"Annotate Branch", "This tool allows you to edit footnotes attached to branches of the tree.."); //; hold down shift to enter a URL
		taxonCommand = MesquiteModule.makeCommand("editTaxonNote",  this);
		branchCommand = MesquiteModule.makeCommand("editBranchNote",  this);
		branchNotesTool.setTouchedTaxonCommand(taxonCommand);
		branchNotesTool.setTouchedCommand(branchCommand);
		if (ownerModule.containerOfModule() instanceof MesquiteWindow) {
			((MesquiteWindow)ownerModule.containerOfModule()).addTool(branchNotesTool);
			branchNotesTool.setPopUpOwner(ownerModule);
		}
		message = new MesquiteLabel(null, 0);
		message.setSize(4,4);
		message.setColor(Color.yellow);
		message.setVisible(false);
		treeDisplay.addPanelPlease(message);
	}
	StringInABox box = new StringInABox( "", treeDisplay.getFont(),150);
	/*.................................................................................................................*/
	public   void drawOnTree(Tree tree, int node, Graphics g) {
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			drawOnTree(tree, d, g);
		if (getNote(tree, node)!=null) {
			if (showAlways){
				String note = getNote(tree, node);
				box.setColors(Color.red, Color.white);
				box.setString("* " +note);
				box.draw(g,  (int)treeDisplay.getTreeDrawing().x[node],(int) treeDisplay.getTreeDrawing().y[node]);  //integer nodeloc approximation
			}
			else if (branchNotesModule.asterisk == null)
				StringUtil.highlightString(g, "*", (int)treeDisplay.getTreeDrawing().x[node]-8, (int) treeDisplay.getTreeDrawing().y[node]+8, Color.red, Color.white);  //integer nodeloc approximation
			else
				g.drawImage(branchNotesModule.asterisk, (int)treeDisplay.getTreeDrawing().x[node], (int)treeDisplay.getTreeDrawing().y[node], (ImageObserver)treeDisplay);  //integer nodeloc approximation
		}
	}
	
	void setShowAlways(boolean a){
		showAlways = a;
	   	treeDisplay.pleaseUpdate(false);
	}
	/**return a text version of information at node*/
	public String textAtNode(Tree tree, int node){
		if (getNote(tree, node)!=null)
			return "*(" + node + ")";
		else
			return null;
	}
	/*.................................................................................................................*/
	public void getNotes(Tree tree, int node, StringBuffer sb) {
		for (int d = tree.firstDaughterOfNode(node); tree.nodeExists(d); d = tree.nextSisterOfNode(d))
			getNotes(tree, d, sb);
		String note = getNote(tree, node);
		if (note!=null) {
			sb.append("*(" + node + "): " + note + "\n");
		}
	}
	/**return a text version of any legends or other explanatory information*/
	public String textForLegend(){
		StringBuffer sb = new StringBuffer(100);
		getNotes(treeDisplay.getTree(), treeDisplay.getTree().getRoot(), sb);
		return sb.toString();
	}
	/*.................................................................................................................*/
	public   void printOnTree(Tree tree, int drawnRoot, Graphics g) {
		//drawOnTree(tree, drawnRoot, g); //should draw numbered footnotes!
	}
	/*.................................................................................................................*/
	public   void setTree(Tree tree) {
	}
	NameReference branchNotesRef = NameReference.getNameReference("note");
	String getNote(Tree tree, int node){
		return (String)tree.getAssociatedObject(branchNotesRef, node);
	}
	void setNote(Tree tree, int node, String note){
		if (tree instanceof Associable){
			((Associable)tree).setAssociatedObject(branchNotesRef, node, note);
			((Associable)tree).notifyListeners(this, new Notification(MesquiteListener.ANNOTATION_CHANGED));

		}
	}
	boolean shown = false;
	/**to inform TreeDisplayExtra that cursor has just entered name of terminal taxon M*/
	public void cursorEnterTaxon(Tree tree, int M, Graphics g){
		if (branchNotesTool.getInUse()){
			String link = (String)tree.getTaxa().getAnnotation(M);
			if (link!=null) {
				int tM = tree.nodeOfTaxonNumber(M);
				shown = true;
				message.setLocation((int)treeDisplay.getTreeDrawing().x[tM], (int)treeDisplay.getTreeDrawing().y[tM]);  //integer nodeloc approximation
				message.setText(link);
				message.setVisible(true);
				message.setCommand(taxonCommand);
				message.setArguments(Integer.toString(tM));
			}
		}
	}
	/**to inform TreeDisplayExtra that cursor has just exited name of terminal taxon M*/
	public void cursorExitTaxon(Tree tree, int M, Graphics g){
		if (shown) {
			message.setVisible(false);
			//message.setSize(4,4);
		}
		shown = false;
	}
	/**to inform TreeDisplayExtra that cursor has just entered branch N*/
	public void cursorEnterBranch(Tree tree, int N, Graphics g){
		if (branchNotesTool.getInUse()){
			String link = getNote(tree, N);
			if (link!=null) {
				shown = true;
				message.setLocation((int)treeDisplay.getTreeDrawing().x[N], (int)treeDisplay.getTreeDrawing().y[N]);  //integer nodeloc approximation
				message.setText(link);
				message.setVisible(true);
				message.setCommand(branchCommand);
				message.setArguments(Integer.toString(N));
			}
		}
	}
	/**to inform TreeDisplayExtra that cursor has just exited branch N*/
	public void cursorExitBranch(Tree tree, int N, Graphics g){
		if (shown) {
			message.setVisible(false);
			//message.setSize(4,4);
		}
		shown = false;
	}
	
  	 MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
 	public Object doCommand(String commandName, String arguments, CommandChecker checker) { 

    	 	if (checker.compare(this.getClass(), "Edits the annotation for the taxon", "[taxon number]", commandName, "editTaxonNote")) {
    	 		Tree tree = treeDisplay.getTree();
   	 		int M = MesquiteInteger.fromFirstToken(arguments, pos);
   	 		if (M<0 || !MesquiteInteger.isCombinable(M) || M>=tree.getTaxa().getNumTaxa())
   	 			return null;
			Taxa taxa = tree.getTaxa();
			
   	 		String chosen = MesquiteString.queryMultiLineString(ownerModule.containerOfModule(), "Annotation for taxon", "Annotation for taxon " + taxa.getTaxonName(M), taxa.getAnnotation(M), 8, false, true);
   	 		if (chosen==null)
   	 			return null;
			taxa.setAnnotation(M, chosen);
   	 		ownerModule.outputInvalid();
   	 		return null;
    	 	}
    	 	else if (checker.compare(this.getClass(), "Edits the annotation for the branch", "[node number]", commandName, "editBranchNote")) {
    	 		Tree tree = treeDisplay.getTree();
    	 		if (!(tree instanceof Associable)){
    	 			ownerModule.alert("Sorry, this is not a standard Mesquite tree, and you can't edit its branch notes");
    	 			return null;
    	 		}
    	 		int edit = 0;
   	 		int M = MesquiteInteger.fromFirstToken(arguments, pos);
   	 		if (M<0 || !MesquiteInteger.isCombinable(M))
   	 			return null;
						
   	 		String chosen = MesquiteString.queryMultiLineString(ownerModule.containerOfModule(), "Annotation for branch", "Annotation for branch", getNote(tree, M), 8, false, true);
   	 		if (chosen==null)
   	 			return null;
   	 		setNote(tree, M, chosen);
   	 		ownerModule.outputInvalid();
   	 		return null;
    	 	}
    	 	/**
    	 	else 
    	 		return  super.doCommand(commandName, arguments, checker);
    	 	/**/

 		return null;
 	}
	public void turnOff() {
		branchNotesModule.extras.removeElement(this);
		super.turnOff();
	}
}



