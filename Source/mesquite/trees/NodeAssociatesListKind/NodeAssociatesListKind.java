/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.NodeAssociatesListKind;

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
public class NodeAssociatesListKind extends NodeAssociatesListAssistant  {
	MesquiteTree tree =null;
	MesquiteTable table = null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		addMenuItem("Move selected To Text", makeCommand("transformToText", this));
		addMenuItem("Copy selected To Text", makeCommand("copyToText", this));
		addMenuItem("Move selected To Decimal Number", makeCommand("transformToDecimal", this));
		addMenuItem("Copy selected To Decimal Number", makeCommand("copyToDecimal", this));
		addMenuItem("Move selected To Node Labels", makeCommand("transformToNodeLabel", this));
		addMenuItem("Copy selected To Node Labels", makeCommand("copyToNodeLabel", this));
		return true;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Kind of Node Associate";
	}
	public String getVeryShortName() {
		return "Kind";
	}
	public String getExplanation() {
		return "Shows the kind of associate (a number, string, or other object)." ;
	}


	public void setTableAndObject(MesquiteTable table, Object object) {
		this.table = table;
	}

	public void setTree(MesquiteTree tree){
		this.tree = tree;
		parametersChanged();
	}

	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Transforms the information to text", null, commandName, "transformToText")) {
			copyToText(true);
		}
		else if (checker.compare(this.getClass(), "Copies the information to a new text property", null, commandName, "copyToText")) {
			copyToText(false);
		}
		else if (checker.compare(this.getClass(), "Transforms the information to doubles", null, commandName, "transformToDecimal")) {
			copyToDoubles(true);
		}
		else if (checker.compare(this.getClass(), "Copies the information to a new number property", null, commandName, "copyToDecimal")) {
			copyToDoubles(false);
		}
		else	if (checker.compare(this.getClass(), "Transforms the information to node labels", null, commandName, "transformToNodeLabel")) {
			copyToNodeLabels(true);
		}
		else if (checker.compare(this.getClass(), "Copies the information to node labels", null, commandName, "copyToNodeLabel")) {
			copyToNodeLabels(false);
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	/*.................................................................................................................*/
	void copyToText(boolean deleteOriginal){
		if (table == null)
			return;
		if (!table.anyRowSelected()){
			discreetAlert("Please select rows before attempting to copy them here");
			return;
		}
		int[] rows = new int[table.numRowsSelected()];
		for (int i= 0; i<rows.length; i++)
			rows[i] = -1;
		int count = 0;
		for (int ir = 0; ir<table.getNumRows(); ir++){
			if (table.isRowSelected(ir)){
				PropertyRecord mi = getPropertyAtRow(ir);
				String currentName = mi.getName();
				String textName = currentName+".text";
				String candidateName =textName;
				int nameCount = 2;
				while (tree.getAssociatedStrings(NameReference.getNameReference(candidateName)) != null)
					candidateName = textName + (nameCount++);
				NameReference tnRef = NameReference.getNameReference(candidateName);
				NameReference currentRef = NameReference.getNameReference(currentName);
				tree.makeAssociatedStrings(candidateName);
				StringArray textArray = tree.getAssociatedStrings(tnRef);
				if (mi.kind == Associable.BUILTIN){
					if (mi.getName().equalsIgnoreCase(MesquiteTree.branchLengthName)){
						for (int node = 0; node<tree.getNumNodeSpaces() && node<textArray.getSize(); node++) 
							textArray.setValue(node, MesquiteDouble.toString(tree.getBranchLength(node)));
						rows[count++] =ir;  //in case needs to be deleted later
					}
					else if (mi.getName().equalsIgnoreCase(MesquiteTree.nodeLabelName)){
						for (int node = 0; node<tree.getNumNodeSpaces() && node<textArray.getSize(); node++) 
							textArray.setValue(node, tree.getNodeLabel(node));
						rows[count++] =ir;  //in case needs to be deleted later
					}

				}
				else {if (mi.kind == Associable.BITS){
					for (int node = 0; node<tree.getNumNodeSpaces() && node<textArray.getSize(); node++) 
						textArray.setValue(node, MesquiteBoolean.toTrueFalseString(tree.getAssociatedBit(currentRef, node)));
					rows[count++] =ir;  //in case needs to be deleted later
				}
				else if (mi.kind == Associable.LONGS) {
					for (int node = 0; node<tree.getNumNodeSpaces() && node<textArray.getSize(); node++) 
						textArray.setValue(node, MesquiteLong.toString(tree.getAssociatedLong(currentRef, node)));
					rows[count++] =ir;  //in case needs to be deleted later
				}
				else if (mi.kind == Associable.DOUBLES){
					for (int node = 0; node<tree.getNumNodeSpaces() && node<textArray.getSize(); node++) 
						textArray.setValue(node, MesquiteDouble.toString(tree.getAssociatedDouble(currentRef, node)));
					rows[count++] =ir;  //in case needs to be deleted later
				}
				else if (mi.kind == Associable.OBJECTS){
					for (int node = 0; node<tree.getNumNodeSpaces() && node<textArray.getSize(); node++) {
						Object obj = tree.getAssociatedObject(currentRef, node);
						if (obj!=null ){
							String s ="";
							if (obj instanceof DoubleArray){
								DoubleArray doubles = (DoubleArray)obj;
								s+= "{";
								boolean firstD = true;
								for (int k = 0; k<doubles.getSize(); k++){
									if (!firstD)
										s += ", ";
									firstD = false;
									s += MesquiteDouble.toString(doubles.getValue(k));
								}
								s+=  "} ";
							}
							else if (obj instanceof StringArray){
								StringArray words = (StringArray)obj;
								s+= "{";
								boolean firstD = true;
								for (int k = 0; k<words.getSize(); k++){
									if (!firstD)
										s += ", ";
									firstD = false;
									s += words.getValue(k);
								}
								s+=  "} ";
							}
							else if (obj instanceof Listable)
								s+= ((Listable)obj).getName() + " = " + obj;
							else if (obj instanceof String){
								s+= (String)obj;
							}
							else if (obj instanceof String[] && ((String[])obj).length>0){
								String[] words = (String[])obj;
								s+= "{";
								boolean firstD = true;
								for (int k = 0; k<words.length; k++){
									if (!firstD)
										s += ", ";
									firstD = false;
									s += words[k];
								}
								s+=  "} ";
							}
							textArray.setValue(node, s);
						}
					}
					rows[count++] =ir;  //in case needs to be deleted later
				}
				}
			}
		}

		if (deleteOriginal){
			for (int ir = rows.length-1; ir>=0; ir--) 
				pleaseDeleteRow(rows[ir], false);
		}
		tree.notifyListeners(this, new Notification(MesquiteListener.ASSOCIATED_CHANGED));
		parametersChanged();
	}
	/*.................................................................................................................*/
	void copyToDoubles(boolean deleteOriginal){
		if (table == null)
			return;
		if (!table.anyRowSelected()){
			discreetAlert("Please select rows before attempting to copy them here");
			return;
		}
		int[] rows = new int[table.numRowsSelected()];
		for (int i= 0; i<rows.length; i++)
			rows[i] = -1;
		int count = 0;
		for (int ir = 0; ir<table.getNumRows(); ir++){
			if (table.isRowSelected(ir)){
				PropertyRecord mi = getPropertyAtRow(ir);
				String currentName = mi.getName();
				String doubleName = currentName+".num";
				String candidateName =doubleName;
				int nameCount = 2;
				while (tree.getAssociatedDoubles(NameReference.getNameReference(candidateName)) != null)
					candidateName = doubleName + (nameCount++);
				NameReference tnRef = NameReference.getNameReference(candidateName);
				NameReference currentRef = NameReference.getNameReference(currentName);
				tree.makeAssociatedDoubles(candidateName);
				DoubleArray doublesArray = tree.getAssociatedDoubles(tnRef);
				if (mi.kind == Associable.BUILTIN){
					if (mi.getName().equalsIgnoreCase(MesquiteTree.branchLengthName)){
						for (int node = 0; node<tree.getNumNodeSpaces() && node<doublesArray.getSize(); node++) 
							doublesArray.setValue(node, tree.getBranchLength(node));
						rows[count++] =ir;  //in case needs to be deleted later
					}
					else if (mi.getName().equalsIgnoreCase(MesquiteTree.nodeLabelName)){
						for (int node = 0; node<tree.getNumNodeSpaces() && node<doublesArray.getSize(); node++) 
							doublesArray.setValue(node, fromString(tree.getNodeLabel(node)));
						rows[count++] =ir;  //in case needs to be deleted later
					}

				}
				else if (mi.kind == Associable.LONGS) {
					for (int node = 0; node<tree.getNumNodeSpaces() && node<doublesArray.getSize(); node++) 
						doublesArray.setValue(node, tree.getAssociatedLong(currentRef, node));
					rows[count++] =ir;  //in case needs to be deleted later
				}
				else if (mi.kind == Associable.DOUBLES){
					for (int node = 0; node<tree.getNumNodeSpaces() && node<doublesArray.getSize(); node++) 
						doublesArray.setValue(node, tree.getAssociatedDouble(currentRef, node));
					rows[count++] =ir;  //in case needs to be deleted later
				}
				else if (mi.kind == Associable.STRINGS){
					for (int node = 0; node<tree.getNumNodeSpaces() && node<doublesArray.getSize(); node++) {
						String s = tree.getAssociatedString(currentRef, node);
						doublesArray.setValue(node, fromString(s));
					}
					rows[count++] =ir;  //in case needs to be deleted later

				}
				else if (mi.kind == Associable.OBJECTS){
					for (int node = 0; node<tree.getNumNodeSpaces() && node<doublesArray.getSize(); node++) {
						Object obj = tree.getAssociatedObject(currentRef, node);
						if (obj!=null ){
							String s ="";
							if (obj instanceof String){
								s+= (String)obj;
							}
							doublesArray.setValue(node, fromString(s));
						}
					}
					rows[count++] =ir;  //in case needs to be deleted later

				}
			}
		}

		if (deleteOriginal){
			for (int ir = rows.length-1; ir>=0; ir--) 
				pleaseDeleteRow(rows[ir], false);
		}
		tree.notifyListeners(this, new Notification(MesquiteListener.ASSOCIATED_CHANGED));
		parametersChanged();
	}	
	double fromString(String s){
		double d = MesquiteDouble.fromString(s);
		if (!MesquiteDouble.isCombinable(d) && d != MesquiteDouble.unassigned)
			d = MesquiteDouble.unassigned;
		return d;
	}
	/*.................................................................................................................*/
	void copyToNodeLabels(boolean deleteOriginal){
		if (table == null)
			return;
		if (table.numRowsSelected()!=1){
			discreetAlert("Please select exactly one row before attempting to copy them here");
			return;
		}
		int ir = table.firstRowSelected();
		PropertyRecord mi = getPropertyAtRow(ir);
		String currentName = mi.getName();
		NameReference currentRef = NameReference.getNameReference(currentName);
		if (mi.kind == Associable.BUILTIN){
			if (mi.getName().equalsIgnoreCase(MesquiteTree.branchLengthName)){
				for (int node = 0; node<tree.getNumNodeSpaces(); node++) 
					tree.setNodeLabel(MesquiteDouble.toString(tree.getBranchLength(node)), node);
			}
			else if (mi.getName().equalsIgnoreCase(MesquiteTree.nodeLabelName)){
			}

		}
		else {if (mi.kind == Associable.BITS){
			for (int node = 0; node<tree.getNumNodeSpaces(); node++) 
				tree.setNodeLabel(MesquiteBoolean.toTrueFalseString(tree.getAssociatedBit(currentRef, node)), node);
		}
		else if (mi.kind == Associable.LONGS) {
			for (int node = 0; node<tree.getNumNodeSpaces(); node++) 
				tree.setNodeLabel(MesquiteLong.toString(tree.getAssociatedLong(currentRef, node)), node);
		}
		else if (mi.kind == Associable.DOUBLES){
			for (int node = 0; node<tree.getNumNodeSpaces(); node++) 
				tree.setNodeLabel(MesquiteDouble.toString(tree.getAssociatedDouble(currentRef, node)), node);
		}
		else if (mi.kind== Associable.STRINGS){
			for (int node = 0; node<tree.getNumNodeSpaces(); node++) 
				tree.setNodeLabel(tree.getAssociatedString(currentRef, node), node);
		}
		else if (mi.kind == Associable.OBJECTS){
			for (int node = 0; node<tree.getNumNodeSpaces(); node++) {
				Object obj = tree.getAssociatedObject(currentRef, node);
				if (obj!=null ){
					String s ="";
					if (obj instanceof DoubleArray){
						DoubleArray doubles = (DoubleArray)obj;
						s+= "{";
						boolean firstD = true;
						for (int k = 0; k<doubles.getSize(); k++){
							if (!firstD)
								s += ", ";
							firstD = false;
							s += MesquiteDouble.toString(doubles.getValue(k));
						}
						s+=  "} ";
					}
					else if (obj instanceof StringArray){
						StringArray words = (StringArray)obj;
						s+= "{";
						boolean firstD = true;
						for (int k = 0; k<words.getSize(); k++){
							if (!firstD)
								s += ", ";
							firstD = false;
							s += words.getValue(k);
						}
						s+=  "} ";
					}
					else if (obj instanceof Listable)
						s+= ((Listable)obj).getName() + " = " + obj;
					else if (obj instanceof String){
						s+= (String)obj;
					}
					else if (obj instanceof String[] && ((String[])obj).length>0){
						String[] words = (String[])obj;
						s+= "{";
						boolean firstD = true;
						for (int k = 0; k<words.length; k++){
							if (!firstD)
								s += ", ";
							firstD = false;
							s += words[k];
						}
						s+=  "} ";
					}
					tree.setNodeLabel(s, node);
				}
			}
		}
		}



		if (deleteOriginal){
			pleaseDeleteRow(ir, false);
		}
		tree.notifyListeners(this, new Notification(MesquiteListener.ASSOCIATED_CHANGED));
		tree.notifyListeners(this, new Notification(MesquiteListener.NAMES_CHANGED));
		parametersChanged();
	}	/*.................................................................................................................*/

	public String getWidestString(){
		return "88888888888888";
	}
	/*.................................................................................................................*/
	public String getTitle() {
		return "Kind";
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
		PropertyRecord property = getPropertyAtRow(ic);
		if (property != null){
			if (property.kind == Associable.BITS)
				return "Boolean";
			else if (property.kind == Associable.DOUBLES || (property.kind == Associable.BUILTIN && property.getNameReference().equals(MesquiteTree.branchLengthNameRef)))
				return "Decimal";
			else if (property.kind == Associable.LONGS)
				return "Integer";
			else if (property.kind == Associable.STRINGS || (property.kind == Associable.BUILTIN && property.getNameReference().equals(MesquiteTree.nodeLabelNameRef)))
				return "Text";
			else if (property.kind == Associable.OBJECTS) {
				if (tree == null)
					return "Objects";
				ObjectArray oa = tree.getAssociatedObjects(property.getNameReference());
				if (oa.oneKindOfObject()){
					Class commonClass = oa.getCommonClass();
					if (commonClass == DoubleArray.class)
						return "Decimals";
					else if (commonClass == LongArray.class)
						return "Integers";
					else if (commonClass == StringArray.class)
						return "Text";
					else if (commonClass == String.class)
						return "Text";
					else if (commonClass == null)
						return "(empty)";
				}
				return "Objects";

			}
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
