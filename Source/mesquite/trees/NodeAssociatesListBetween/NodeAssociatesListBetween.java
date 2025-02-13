/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.NodeAssociatesListBetween;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Shape;
import java.util.Vector;

import javax.swing.*;

import mesquite.lib.*;
import mesquite.lib.characters.CharInclusionSet;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.table.MesquiteTable;
import mesquite.lib.taxa.TaxaGroup;
import mesquite.lib.taxa.TaxaGroupVector;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.Tree;
import mesquite.lib.ui.ColorDistribution;
import mesquite.lib.ui.MesquiteSymbol;
import mesquite.lists.lib.*;
import mesquite.trees.lib.NodeAssociatesListAssistant;

/* ======================================================================== */
public class NodeAssociatesListBetween extends NodeAssociatesListAssistant  {
	MesquiteTree tree =null;
	MesquiteTable table = null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		addMenuItem("Ressign Branch vs. Node", makeCommand("reassign", this));
		addMenuItem("Explanation...", makeCommand("explain", this));
		return true;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Information Applies to Branch or Node?";
	}
	public String getNameForMenuItem() {
		return "For Branch or Node?";
	}
	public String getVeryShortName() {
		return "At Branch/Node?";
	}
	public String getExplanation() {
		return "Shows whether the associated information applies to the node or branch." ;
	}


	public void setTableAndObject(MesquiteTable table, Object object) {
		this.table = table;
	}

 	public void setTree(MesquiteTree tree){
 		this.tree = tree;
	}

	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Reassigns the selected to the branch", null, commandName, "reassign")) {
			discreetAlert("Sorry, designation of properties as assigned to branch vs. node is a system-level setting that can be edited only by changing the file properties.xml in Mesquite_Folder/settings/basic/AssociatesInit"); //Debugg.println
		}
		else if (checker.compare(this.getClass(), "Explains", null, commandName, "explain")) {
			discreetAlert("A property is assigned either to a node or the branch just below it. "
					+"\n\nThis is important when the tree is rerooted. A property assigned to the branch may appear to flip to a different node on rerooting, "
					+" while a property assigned to the node will appear to flip to a different branch on rerooting."
					+"\n\nFor instance, branch length, color, and clade confidence measures should belong to the branch, but some others should belong the node.");
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	
	/*.................................................................................................................*
	void reassign(boolean toBranch){
		if (table == null)
			return;
		if (!table.anyRowSelected()){
			discreetAlert("Please selected rows before attempting to reassign them here");
			return;
		}
		PropertyRecord[] mis = new PropertyRecord[table.numRowsSelected()];
		int count = 0;
		for (int ir = 0; ir<table.getNumRows(); ir++){
			if (table.isRowSelected(ir) && !associateInListIsBuiltIn(ir)){
				setBetweenness(ir, toBranch);
			}
		}
		parametersChanged();
	}
	void setBetweenness(int ic, boolean between) {
		if (associatedInfo == null)
			return;
		if (ic>=0 && ic<associatedInfo.size()){
			ObjectContainer objContainer = (ObjectContainer)associatedInfo.elementAt(ic);
			Object obj = objContainer.getObject();
			if (obj instanceof DoubleArray)
				((DoubleArray)obj).setBetweenness(between);
			else if (obj instanceof LongArray)
				((LongArray)obj).setBetweenness(between);
			else if (obj instanceof ObjectArray)
				((ObjectArray)obj).setBetweenness(between);
			else if (obj instanceof Bits)
				((Bits)obj).setBetweenness(between);
		}
	}

	/*.................................................................................................................*/
	
	public String getWidestString(){
		return "8888888888888";
	}
	/*.................................................................................................................*/
	public String getTitle() {
		return getVeryShortName();
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}

	/** Returns whether to use the string from getStringForRow; otherwise call drawInCell*/
	public boolean useString(int ic){
		return true;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;  
	}
	String nodeOrBranch(boolean isBetween){
		if (!isBetween)
			return "Node";
		return "Branch";
	}
	public String getStringForRow(int ic) {
		PropertyDisplayRecord property = getPropertyAtRow(ic);
		if (property != null)
			return nodeOrBranch(property.belongsToBranch);
		return "—";
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}


}
