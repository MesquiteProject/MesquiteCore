/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.NumForNodesAttached;

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
/**Suppliies numbers for each node of a tree.*/

public class NumForNodesAttached extends NumbersForNodes {
	public String getName() {
		return "Number for Nodes from Attached Values";
	}
	public String getExplanation() {
		return "Supplies numbers for each node of a tree using values already attached to the nodes.";
	}
		/*.................................................................................................................*/
	Taxa taxa;
	MesquiteString valueName;
	ListableVector choices;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		choices = new ListableVector();
		valueName = new MesquiteString();
		addMenuItem( "Choose Values to Show...", makeCommand("chooseValues",  this));
		addMenuSeparator();


		return true;
	}
	/*.................................................................................................................*/
	public Class getCharacterClass() {
		return null;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("chooseValues " + ParseUtil.tokenize(valueName.getValue()));
		return temp;
	}
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets or choose value to show", null, commandName, "chooseValues")) {
			String name = parser.getFirstToken(arguments);
			if (StringUtil.blank(name)) {
				int current = choices.indexOfByName(valueName.getValue());
				if (current < 0)
					current = 0;
				Listable choice = ListDialog.queryList(containerOfModule(), "Choose value to show", "Choose which value to obtain for the nodes", null, choices, current);
				if (choice != null) {
				valueName.setName(choice.getName());
				valueName.setValue(choice.getName());
				}
			}
			parametersChanged();
		}
	
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public void calculateNumbers(Tree tree, NumberArray result, MesquiteString resultString){
	   	clearResultAndLastResult(result);
		rememberChoices(tree);
	   	if (tree != null) {
			DoubleArray da = tree.getWhichAssociatedDouble(NameReference.getNameReference(valueName.getValue()));
			if (da == null)
				return;
			result.resetSize(da.getSize());
			for (int i = 0; i<da.getSize(); i++) {
				result.setValue(i, da.getValue(i));
			}
			resultString.setValue("Values of " + valueName.getValue());
	   	}
		saveLastResult(result);
		saveLastResultString(resultString);
	}

	void rememberChoices(Tree t) {
		if (t == null)
			return;
		if (! (t instanceof Associable))
			return;
		Associable tree = (Associable)t;
		for (int i = 0; i<tree.getNumberAssociatedDoubles(); i++) {
			DoubleArray da = tree.getAssociatedDoubles(i);
			if (choices.elementWithName(da.getName()) == null) {
				choices.addElement(new MesquiteString(da.getName()), false);
			}
		}
		if (valueName.isBlank() && choices.size()>0) {
			String n = choices.nameOfElementAt(0);
			valueName.setName(n);
			valueName.setValue(n);
		}
	}
	
	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
	 happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Tree tree){
		if (tree == null)
			return;
		taxa = tree.getTaxa();
		rememberChoices(tree);
	}

	/*.................................................................................................................*/
	public String getParameters(){
		return "Values: " + valueName.getValue() ; 
	}
	/*.................................................................................................................*/
	public String getNameAndParameters(){
		return valueName.getValue(); 
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return true;
	}

	/*.................................................................................................................*/
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return false;  
	}

}

