/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.trees.BranchInfo;

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class BranchInfo extends TreeDisplayAssistantI {
	public Vector extras;
	 public String getFunctionIconPath(){
   		 return getPath() + "branchInfo.gif";
   	 }
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		extras = new Vector();
		return true;
	} 
	/*.................................................................................................................*/
   	 public boolean isSubstantive(){
   	 	return false;
   	 }
	/*.................................................................................................................*/
	public   TreeDisplayExtra createTreeDisplayExtra(TreeDisplay treeDisplay) {
		InfoToolExtra newPj = new InfoToolExtra(this, treeDisplay);
		extras.addElement(newPj);
		return newPj;
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Branch Information";
   	 }
   	 
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Provides a tool that shows information about branches.";
   	 }
}

/* ======================================================================== */
class InfoToolExtra extends TreeDisplayExtra implements Commandable  {
	TreeTool infoTool;
	MesquiteMenuItemSpec hideMenuItem = null;
	BranchInfo infoModule;
	Tree tree;
	MesquiteCommand respondCommand;

	public InfoToolExtra (BranchInfo ownerModule, TreeDisplay treeDisplay) {
		super(ownerModule, treeDisplay);
		respondCommand = ownerModule.makeCommand("respond", this);
		infoModule = ownerModule;
		infoTool = new TreeTool(this, "BranchInfo", ownerModule.getPath(), "branchInfo.gif", 5,2,"Branch Info", "This tool is used to show information about a branch.");
		infoTool.setTouchedCommand(MesquiteModule.makeCommand("query",  this));
		if (ownerModule.containerOfModule() instanceof MesquiteWindow) {
			((MesquiteWindow)ownerModule.containerOfModule()).addTool(infoTool);
			//infoTool.setPopUpOwner(ownerModule);
			//ownerModule.setUseMenubar(false); //menu available by touching button
		}
	}
	/*.................................................................................................................*/
	public   void drawOnTree(Tree tree, int drawnRoot, Graphics g) {
		this.tree = tree;
	}
	
	/*.................................................................................................................*/
	public   void printOnTree(Tree tree, int drawnRoot, Graphics g) {
		drawOnTree(tree, drawnRoot, g);
	}
	/*.................................................................................................................*/
	public   void setTree(Tree tree) {
		this.tree = tree;
	}
	MesquitePopup popup;
	MesquiteInteger pos = new MesquiteInteger();
	void addToPopup(String s, int node, int response){
		if (popup==null)
			return;
		popup.addItem(s, ownerModule, respondCommand, Integer.toString(node) + " " + Integer.toString(response));
	}
	void respondToPopup(String s,  int responseRequested,  int response){
		if (responseRequested == response)
			ownerModule.alert(s);
	}
	/*.................................................................................................................*/
 	public Object doCommand(String commandName, String arguments, CommandChecker checker) { 

    	 	if (checker.compare(this.getClass(), "Shows popup menu with information about the branch", "[branch number]", commandName, "query")) {
  			int branchFound= MesquiteInteger.fromFirstToken(arguments, pos);
  			if (branchFound >0 && MesquiteInteger.isCombinable(branchFound)) {
				if (popup==null)
					popup = new MesquitePopup(treeDisplay);
				popup.removeAll();
				int responseNumber = 0;
				addToPopup("Branch/node number: " + branchFound, branchFound, responseNumber++);
				addToPopup("-", branchFound, responseNumber++);
				addToPopup("Length: " + MesquiteDouble.toString(tree.getBranchLength(branchFound)), branchFound, responseNumber++);
				int num = tree.getNumberAssociatedLongs();
				if (num>0)
					for (int i=0; i<num; i++) {
						LongArray lo = tree.getAssociatedLongs(i);
						NameReference nr = lo.getNameReference();
						if (nr==null)
							addToPopup("?: " + MesquiteLong.toString(lo.getValue(branchFound)), branchFound, responseNumber++);
						else
							addToPopup(nr.getValue() + ": " + MesquiteLong.toString(lo.getValue(branchFound)), branchFound, responseNumber++);
					}
				num = tree.getNumberAssociatedDoubles();
				if (num>0)
					for (int i=0; i<num; i++) {
						DoubleArray lo = tree.getAssociatedDoubles(i);
						NameReference nr = lo.getNameReference();
						if (nr==null)
							addToPopup("?: " + MesquiteDouble.toString(lo.getValue(branchFound)), branchFound, responseNumber++);
						else
							addToPopup(nr.getValue() + ": " + MesquiteDouble.toString(lo.getValue(branchFound)), branchFound, responseNumber++);
					}
				num = tree.getNumberAssociatedObjects();
				if (num>0)
					for (int i=0; i<num; i++) {
						ObjectArray lo = tree.getAssociatedObjects(i);
						Object obj = lo.getValue(branchFound);
						if (obj != null && (obj instanceof String || obj instanceof Listable)){
							String s = "";
							if (obj instanceof String)
								s = (String)obj;
							else if (obj instanceof Listable)
								s = ((Listable)obj).getName();
							NameReference nr = lo.getNameReference();
							if (nr==null)
								addToPopup("?: " + s, branchFound, responseNumber++);
							else
								addToPopup(nr.getValue() + ": " + s, branchFound, responseNumber++);
						}
					}
				
			Enumeration e = treeDisplay.getExtras().elements();
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					TreeDisplayExtra ex = (TreeDisplayExtra)obj;
					String strEx =ex.textAtNode(tree, branchFound);
		 			if (!StringUtil.blank(strEx)) {
			 			if (ex.ownerModule!=null)
			 				strEx = ex.ownerModule.getName() + ": " + strEx;
		 				addToPopup(strEx, branchFound, responseNumber++);
		 			}
		 		}
			popup.showPopup((int)treeDisplay.getTreeDrawing().x[branchFound], (int)treeDisplay.getTreeDrawing().y[branchFound]);
   			}
 	 	}
    	 	else if (checker.compare(this.getClass(), "Responds to choice of popup menu with information about the branch", "[branchNumber][choice number]", commandName, "respond")) {
  			int branchFound= MesquiteInteger.fromFirstToken(arguments, pos);
  			if (branchFound >0 && MesquiteInteger.isCombinable(branchFound)) {
	  			int responseRequested = MesquiteInteger.fromString(arguments, pos);
				int responseNumber = 2 ;
				if (responseRequested == responseNumber++)
					ownerModule.alert("This represents the length of the branch (stored in the primary branch length storage of the tree)");
				int num = tree.getNumberAssociatedLongs();
				if (num>0)
					for (int i=0; i<num; i++) {
						if (responseRequested == responseNumber++) {
							LongArray lo = tree.getAssociatedLongs(i);
							NameReference nr = lo.getNameReference();
							String name = "?";
							if (nr!=null)
								name = nr.getValue();
							ownerModule.alert("This represents an integral (long) value attached to the branch of the tree, named " + name);
						//todo: allow user to change value
						}
					}
				num = tree.getNumberAssociatedDoubles();
				if (num>0)
					for (int i=0; i<num; i++) {
						if (responseRequested == responseNumber++) {
							DoubleArray lo = tree.getAssociatedDoubles(i);
							NameReference nr = lo.getNameReference();
							String name = "?";
							if (nr!=null)
								name = nr.getValue();
							ownerModule.alert("This represents a floating-point (double) value attached to the branch of the tree, named " + name);
						//todo: allow user to change value
						}
					}
				
				num = tree.getNumberAssociatedObjects();
				if (num>0)
					for (int i=0; i<num; i++) {
						if (responseRequested == responseNumber++) {
							ObjectArray lo = tree.getAssociatedObjects(i);
							Object obj = lo.getValue(branchFound);
							if (obj != null && (obj instanceof String || obj instanceof Listable)){
								NameReference nr = lo.getNameReference();
								String name = "?";
								if (nr!=null)
									name = nr.getValue();
								String s = "";
								if (obj instanceof String) {
									s = (String)obj;
									if (tree instanceof MesquiteTree){
										String message = "This represents a String attached to the branch of the tree, with name " + name + " and value \"" + s + "\".  Do you want to change the string?";
										if (AlertDialog.query(ownerModule.containerOfModule(), "String at node", message)){
											String newString = MesquiteString.queryString(ownerModule.containerOfModule(), "Change String", "Change string to", s);
											if (newString !=null)
												((MesquiteTree)tree).setAssociatedObject(nr, branchFound, newString);
										}
									}
									else {
										ownerModule.alert("This represents a String attached to the branch of the tree, with name " + name + " and value \"" + s + "\"");
									}
								}
								else if (obj instanceof Listable) {
									s = ((Listable)obj).getName();
									ownerModule.alert("This represents an object attached to the branch of the tree, of category " + name + " and with name " + s);
								}
							}
						}
					}
				Enumeration e = treeDisplay.getExtras().elements();
				while (e.hasMoreElements()) {
					Object obj = e.nextElement();
					TreeDisplayExtra ex = (TreeDisplayExtra)obj;
					String strEx =ex.textAtNode(tree, branchFound);
		 			if (!StringUtil.blank(strEx)) {
			 			String name = "";
			 			if (ex.ownerModule!=null)
			 				name = ex.ownerModule.getName();
						if (responseRequested == responseNumber++){
							if (tree instanceof MesquiteTree){
								String message ="This represents the output of a tree display assistant named " + name + ". Do you want to store the result at this and other nodes in the tree as strings?";
								if (AlertDialog.query(ownerModule.containerOfModule(), "Tree display assistant output", message)){
									String stringName = MesquiteString.queryString(ownerModule.containerOfModule(), "Name", "Name to give to strings attached to nodes (single token please)", StringUtil.tokenize(name));
									if (!StringUtil.blank(stringName)){
										NameReference nr = NameReference.getNameReference(StringUtil.tokenize(stringName));
										transferToStrings((MesquiteTree)tree, tree.getRoot(), ex, nr);
								   		((MesquiteTree)tree).notifyListeners(this, new Notification(MesquiteListener.ANNOTATION_CHANGED));
									}
								}
							}
							else
								ownerModule.alert("This represents the output of a tree display assistant named " + name + ".");
						}
		 			}
		 		}
   			}
 	 	}
 		return null;
 	}
	/*.................................................................................................................*/
 	private void transferToStrings(MesquiteTree tree, int node, TreeDisplayExtra ex, NameReference nr){
		String s = ex.textAtNode(tree, node);
		if (!StringUtil.blank(s))
			tree.setAssociatedObject(nr, node, s);
		
		for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter))
			transferToStrings(tree, daughter, ex, nr);
						
 	}
	/*.................................................................................................................*/
	public void turnOff() {
		if (infoModule.extras != null)
			infoModule.extras.removeElement(this);
		super.turnOff();
	}
}

	


