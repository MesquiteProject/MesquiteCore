/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.NodeAssociatesListValue;

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
public class NodeAssociatesListValue extends NodeAssociatesListAssistant  {
	MesquiteTree tree =null;
	MesquiteTable table = null;
	ListableVector associatedInfo = null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		addMenuItem("How to Edit Values...", makeCommand("howToEdit", this));
		return true;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Value of Node Associate";
	}
	public String getVeryShortName() {
		if (node >=0)
			return "Value at Node " + node;
		return "Value";
	}
	public String getExplanation() {
		return "Shows the value of associate (a number, string, or other object)." ;
	}


	public void setTableAndObject(MesquiteTable table, Object object) {
		this.table = table;
		if (object instanceof ListableVector)
			associatedInfo = (ListableVector)object;

	}

 	public void setTree(MesquiteTree tree){
 		this.tree = tree;
		parametersChanged();
	}

	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Explains how to edit", null, commandName, "howToEdit")) {
			alert("Some of these values can be edited for a node/branch by right-clicking on the branch, then choosing the item in the drop down menu that appears.");
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	int node = -1;
	
	/*.................................................................................................................*/
	public void cursorTouchBranch(MesquiteTree tree, int N){
		node = N;
		parametersChanged();
	}
	public void cursorEnterBranch(MesquiteTree tree, int N){
		node = N;
		parametersChanged();
	}
	public void cursorExitBranch(MesquiteTree tree, int N){
		node = -1;
		parametersChanged();
	}
	public void cursorMove(MesquiteTree tree){
		node = -1;
		parametersChanged();
	}
	/*.................................................................................................................*/
	int maxwidest = 20;
	public String getWidestString(){
		int w = maxwidest;
		if (table == null)
			return "88888888888888888888";
			
		for (int ic = 0; ic<table.getNumRows(); ic++){
			String sIC = getStringForRow(ic);
			if (StringUtil.notEmpty(sIC)){
				if (sIC.length()>w)
					w = sIC.length();
			}
		}
		String eights =  "8888888888888888888888888888888888888888888888888888888888888888";
		if (w>50)
			w = 50;
		maxwidest = w;
		return eights.substring(0, w);
	}
	/*.................................................................................................................*/
	public String getTitle() {
		if (node >=0)
			return "Value at Node " + node;
		return "Value";
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
	public String getStringForRow(int ic) {
		if (associatedInfo == null || tree == null || node<0)
			return "—";
		if (ic>=0 && ic<associatedInfo.size()){
			ObjectContainer objContainer = (ObjectContainer)associatedInfo.elementAt(ic);
			Object obj = objContainer.getObject();
			if (obj instanceof DoubleArray){
				DoubleArray d = (DoubleArray)obj;
				if (node<d.getSize())
				return MesquiteDouble.toString(d.getValue(node));
			}
			else if (obj instanceof LongArray){
				LongArray d = (LongArray)obj;
				if (node<d.getSize())
				return MesquiteLong.toString(d.getValue(node));
			}
			else if (obj instanceof StringArray) {
				StringArray d = (StringArray)obj;
				if (node<d.getSize())
				return d.getValue(node);
			}
			else if (obj instanceof ObjectArray) {
				ObjectArray oa = (ObjectArray)obj;
				if (node>=oa.getSize())
					return "—";
				Object oan = oa.getValue(node);
				if (oan == null)
					return "—";
				if (oan instanceof String)
					return "\"" + (String)oan + "\"";
				return oan.toString(); //Debugg.println temporary?
			}
			else if (obj instanceof Bits) {
				Bits oa = (Bits)obj;
				if (node>=oa.getSize())
					return "—";
				boolean oan = oa.isBitOn(node);
				return MesquiteBoolean.toTrueFalseString(oan); //Debugg.println temporary?
			}
			else if (obj instanceof Tree && MesquiteTree.branchLengthName.equalsIgnoreCase(objContainer.getName()))
				return MesquiteDouble.toString(tree.getBranchLength(node));
			else if (obj instanceof Tree && MesquiteTree.nodeLabelName.equalsIgnoreCase(objContainer.getName())){
				return tree.getNodeLabel(node);
			}
			else
				return "?";
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
