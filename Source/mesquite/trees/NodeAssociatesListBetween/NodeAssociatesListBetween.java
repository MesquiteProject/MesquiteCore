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
	ListableVector associatedInfo = null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Information Applies to Node or Branch?";
	}
	public String getNameForMenuItem() {
		return "For Node or Branch?";
	}
	public String getVeryShortName() {
		return "At Node/Branch?";
	}
	public String getExplanation() {
		return "Shows whether the associated information applies to the node or branch." ;
	}


	public void setTableAndObject(MesquiteTable table, Object object) {
		this.table = table;
		if (object instanceof ListableVector)
			associatedInfo = (ListableVector)object;

	}

 	public void setTree(MesquiteTree tree){
 		this.tree = tree;
	}

	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the color", null, commandName, "setColor")) {
			
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
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
		if (associatedInfo == null)
			return "—";
		if (ic>=0 && ic<associatedInfo.size()){
			ObjectContainer objContainer = (ObjectContainer)associatedInfo.elementAt(ic);
			Object obj = objContainer.getObject();
			boolean between = false;
			if (obj instanceof DoubleArray)
				between = ((DoubleArray)obj).isBetween();
			else if (obj instanceof LongArray)
				between = ((LongArray)obj).isBetween();
			else if (obj instanceof ObjectArray)
				between = ((ObjectArray)obj).isBetween();
			else if (obj instanceof Bits)
				between = ((Bits)obj).isBetween();
			else if (obj instanceof Tree && "Branch lengths".equalsIgnoreCase(objContainer.getName()))
				between = true;
			else
				return "?";
			return nodeOrBranch(between); //objContainer.getName() + " " + obj.getClass() + " " + between;
		}
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
