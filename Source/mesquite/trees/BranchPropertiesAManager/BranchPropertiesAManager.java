/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.BranchPropertiesAManager;
/*~~  */


import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.accessibility.AccessibleContext;
import javax.swing.JLabel;

import mesquite.lib.*;
import mesquite.lib.duties.DrawNamesTreeDisplay;
import mesquite.lib.duties.TreeDisplayAssistantDI;
import mesquite.lib.duties.TreeDisplayAssistantI;
import mesquite.lib.duties.TreeWindowMaker;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.DisplayableBranchProperty;
import mesquite.lib.tree.Tree;
import mesquite.lib.tree.TreeDisplay;
import mesquite.lib.tree.TreeDisplayExtra;

/* ======================================================================== */
public class BranchPropertiesAManager extends TreeDisplayAssistantI {

	String[] reservedNames = new String[]{"!color"};
	String[] builtInNames = new String[]{MesquiteTree.branchLengthName, MesquiteTree.nodeLabelName};

	ListableVector propertyList;
	static boolean asked= false;

	boolean moduleIsNaive = true; //so as not to save the snapshot
	NAAMDisplayExtra extra;

	MesquiteTree tree;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		TreeWindowMaker twMB = (TreeWindowMaker)findEmployerWithDuty(TreeWindowMaker.class);
		if (twMB!= null){
			propertyList = twMB.getBranchPropertiesList();
		}
		propertyList.addElement(new DisplayableBranchProperty(MesquiteTree.nodeLabelName, Associable.BUILTIN), false);
		propertyList.addElement(new DisplayableBranchProperty(MesquiteTree.branchLengthName, Associable.BUILTIN), false);

		return true;
	}
	public void endJob(){
			
		if (this.tree != null)
			this.tree.removeListener(this);

		super.endJob();
	}
	/*.................................................................................................................*/
	void setTree(MesquiteTree tree){
		if (this.tree != null)
			this.tree.removeListener(this);
		this.tree = tree;
		this.tree.addListener(this);

		addPropertiesToList(tree);
	}
	public void changed(Object caller, Object obj, Notification notification){
		if (caller == tree || obj == tree){
			setTree(tree);
		}
	}

	/*.========================================================..*/

	public void writeList(ListableVector list){
		System.out.println("Properties on record & to show");

		for (int i=0; i<list.size(); i++){
			DisplayableBranchProperty mi = (DisplayableBranchProperty)list.elementAt(i);
			System.out.println(mi.getName() + "\t" + mi.kind + " showing " + mi.showing);
		}
	}
	/*...............................................................................*/
	DisplayableBranchProperty findInList(NameReference nr, int kind){
		return (DisplayableBranchProperty)DisplayableBranchProperty.findInList(propertyList, nr, kind);
	}
	/*...............................................................................*/
	DisplayableBranchProperty findInList(String s, int kind){
		if (propertyList.indexOfByName(s)<0)
			return null;
		for (int i=0; i<propertyList.size(); i++){
			DisplayableBranchProperty mi = (DisplayableBranchProperty)propertyList.elementAt(i);
			if (mi.getName().equalsIgnoreCase(s) && mi.kind ==kind)
				return mi;
		}
		return null;
	}
	/*...............................................................................*/
	int indexInList(DisplayableBranchProperty property){
		for (int i=0; i<propertyList.size(); i++){
			DisplayableBranchProperty mi = (DisplayableBranchProperty)propertyList.elementAt(i);
			if (mi.equals(property))
				return i;
		}
		return propertyList.indexOf(property);  //just in case?
	}

	/*...............................................................................*/
	public boolean isBuiltIn(DisplayableBranchProperty mi){
		return mi.kind== Associable.BUILTIN;
	}

	/*.................................................................................................................*/
	public void addPropertyFromScript(String name, int kind, boolean show){
		if (StringUtil.blank(name) || !MesquiteInteger.isCombinable(kind))
			return;
		if (name.equalsIgnoreCase("selected") && kind == Associable.BITS)
			return;
		DisplayableBranchProperty mi = findInList(name, kind);
		if (mi==null){  
			mi = new DisplayableBranchProperty(name, kind);
			mi.showing = show;
			propertyList.addElement(mi, false);
		}
		else
				mi.showing = show;
		if (mi != null && tree != null)
			mi.inCurrentTree = tree.isPropertyAssociated(mi);
	}
	/*.................................................................................................................*/
	NameReference selectedNRef = NameReference.getNameReference("selected");
	private void addPropertiesToList(MesquiteTree tree){
		if (tree == null)
			return;
		int count = 0;
		for (int i=0; i<propertyList.size(); i++){
			DisplayableBranchProperty property = (DisplayableBranchProperty)propertyList.elementAt(i);
			property.inCurrentTree = tree.isPropertyAssociated(property);
			count++;
			property.setBelongsToBranch(tree.propertyIsBetween(property), true);
		}
		DisplayableBranchProperty[] properties = tree.getPropertyRecords();
		if (properties == null)
			return;
		for (int i=0; i<properties.length; i++){
			boolean toBeAdded = indexInList(properties[i])<0;
			if (selectedNRef.equals(properties[i].getNameReference()) && properties[i].kind == Associable.BITS)
				toBeAdded = false;
			if (toBeAdded){  
				propertyList.addElement(properties[i], false);
				properties[i].inCurrentTree = true;
				count++;
			}
		}
	}

 	/*.................................................................................................................*/
 	public boolean isPrerelease(){
 		return false;  
 	}

	/*.................................................................................................................*/
	public String getName() {
		return "Branch/Node Properties Secretary";
	}
	public String getExplanation() {
		return "Keeps record of Branch/Node properties on the tree." ;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return MesquiteModule.NEXTRELEASE;  
	}

	public TreeDisplayExtra createTreeDisplayExtra(TreeDisplay treeDisplay) {
		extra = new NAAMDisplayExtra(this, treeDisplay);
		return extra;
	}

}


/* ======================================================================== */
class NAAMDisplayExtra extends TreeDisplayExtra{
	BranchPropertiesAManager controlModule;
	MesquiteTree myTree = null;

	/*.--------------------------------------------------------------------------------------..*/
	public NAAMDisplayExtra (BranchPropertiesAManager ownerModule, TreeDisplay treeDisplay) {
		super(ownerModule, treeDisplay);
		controlModule = ownerModule;
	}

	/*.................................................................................................................*/
	public   void setTree(Tree tree) {
		myTree = (MesquiteTree)tree;
		controlModule.setTree((MesquiteTree)tree);
	}

	public void drawOnTree(Tree tree, int drawnRoot, Graphics g) {
	}

	@Override
	public void printOnTree(Tree tree, int drawnRoot, Graphics g) {
	}

}

