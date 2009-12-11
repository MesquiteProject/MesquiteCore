/* Mesquite source code.  Copyright 1997-2009 W. Maddison and D. Maddison.
Version 2.71, September 2009.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.NumAttachedToTree;

import java.util.Enumeration;
import java.util.Vector;

import mesquite.lib.*;
import mesquite.lib.duties.*;

/* 
 * Walks a Mesquite tree and calculates the number of taxa in it.
 */

public class NumAttachedToTree extends NumberForTree {

	String nameOfAttached = null;
	ListableVector names = new ListableVector();
	/* ................................................................................................................. */
	/** Explains what the module does. */

	public String getExplanation() {
		return "Supplies a number attached to the tree";
	}

	/* ................................................................................................................. */
	/** Name of module */
	public String getName() {
		if (nameOfAttached !=null)
			return nameOfAttached;
		return "Attached Value";
	}
	/* ................................................................................................................. */

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		addMenuItem( "Value Attached to Trees...", makeCommand("setNameOfAttached",  this));
		return true;
	}

	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("setNameOfAttached " + ParseUtil.tokenize(nameOfAttached)); 
		return temp;
	}
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {

		if (checker.compare(this.getClass(), "Sets the name of the attached numbers to be used", "[name of attached]", commandName, "setNameOfAttached")) {
			String name= parser.getFirstToken(arguments);
			if (StringUtil.blank(name)){
				Listable L = ListDialog.queryList(containerOfModule(), "Attached number", "Which attached number to show? (This list may change as other tree sources are used or trees are read.)", MesquiteString.helpString, names, 0);
				if (L!=null)
					name = L.getName();
			}
			if (!StringUtil.blank(name)) {
				nameOfAttached = name;
				if (!MesquiteThread.isScripting()) parametersChanged();
			}

		}
		else {
			return  super.doCommand(commandName, arguments, checker);
		}
		return null;
	}

	void reviewAttachmentsAvailable(Tree tree){
		if (tree !=null && tree instanceof Attachable){
			Vector at = ((Attachable)tree).getAttachments();
			if (at !=null) {
				for (int i =0; i < at.size(); i++){
					Object obj = at.elementAt(i);
					if (obj instanceof MesquiteDouble || obj instanceof MesquiteNumber  || obj instanceof MesquiteInteger   || obj instanceof MesquiteLong) {
						String name = ((Listable)obj).getName();
						if (names.indexOfByNameIgnoreCase(name)<0) {
							MesquiteString ms = new MesquiteString(name);
							ms.setName(name);
							names.addElement(ms, false);
						}
					}
				}
			}
		}

	}
	Object getFirstAttachment(Tree tree){
		if (tree !=null && tree instanceof Attachable){
			Vector at = ((Attachable)tree).getAttachments();
			if (at !=null) {
				for (int i =0; i < at.size(); i++){
					Object obj = at.elementAt(i);
					if (obj instanceof MesquiteDouble || obj instanceof MesquiteNumber  || obj instanceof MesquiteInteger  || obj instanceof MesquiteLong) {
						nameOfAttached = ((Listable)obj).getName();
						return obj;
					}
				}
			}
		}
		return null;

	}
	/* ................................................................................................................. */
	public void calculateNumber(Tree tree, MesquiteNumber result, MesquiteString resultString) {
		if (result == null || tree == null)
			return;
		clearResultAndLastResult(result);
		reviewAttachmentsAvailable(tree);
		if (tree instanceof Attachable){
			Object obj = null;
			if (nameOfAttached != null)
				obj = ((Attachable)tree).getAttachment(nameOfAttached);
			else
				obj = getFirstAttachment(tree); //also sets nameOfAttached
			if (obj == null){
				if (resultString != null)
					resultString.setValue("No attachment is associated with this tree.");
				return;
			}

			if (obj instanceof MesquiteDouble)
				result.setValue(((MesquiteDouble)obj).getValue());
			else if (obj instanceof MesquiteNumber)
				result.setValue((MesquiteNumber)obj);
			else if (obj instanceof MesquiteInteger)
				result.setValue(((MesquiteInteger)obj).getValue());
			else if (obj instanceof MesquiteLong)
				result.setValue(((MesquiteLong)obj).getValue());
			/*else if (obj instanceof MesquiteString) {
				String s = (((MesquiteString)obj).getValue());
				int value = MesquiteInteger.fromString(s);
				if (MesquiteInteger.isCombinable(value))
					result.setValue(value);
			}*/
		}

		if (resultString != null) {
			resultString.setValue(nameOfAttached + " : " + result.toString());
		}
		saveLastResult(result);
		saveLastResultString(resultString);
	}

	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 272;  
	}
}
