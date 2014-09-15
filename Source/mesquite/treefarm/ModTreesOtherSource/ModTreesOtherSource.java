/* Mesquite source code, Treefarm package.  Copyright 1997 and onward, W. Maddison, D. Maddison and P. Midford. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.treefarm.ModTreesOtherSource;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.treefarm.lib.*;

/* ======================================================================== */
public class ModTreesOtherSource extends SourceFromTreeSource {
	public String getName() {
		return "Transform Trees from Other Source";
	}
	public String getNameForMenuItem() {
		return "Transform Trees from Other Source...";
	}
	public String getExplanation() {
		return "Transforms trees from another source.";
	}

	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		super.getEmployeeNeeds();
		EmployeeNeed e = registerEmployeeNeed(TreeTransformer.class, getName() + "  needs a method to modify trees.",
		"The method to modify trees can be chosen initially or in the Transformer of Trees submenu");
	}
	int currentTree=0;
	int lastGoodTree = -1;
	int lastTreeRequested = -1;
	int maxAvailable = -2;
	boolean noProblemsYet = true;
	boolean queried = false;
	TreeTransformer modifierTask;
	MesquiteString modifierName;
	MesquiteCommand stC;
	MesquiteBoolean discardUntransformable;
	boolean valueSpecified = false;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (!super.startJob(arguments, condition, hiredByName))
			return false;
		modifierTask = (TreeTransformer)hireEmployee(TreeTransformerMult.class, "Transformer of trees (transforms trees " + whatIsMyPurpose() + ")");
		if (modifierTask == null) {
			return sorry(getName() + " couldn't start because no tree transformer was obtained.");
		}
		discardUntransformable = new MesquiteBoolean(true);
		stC = makeCommand("setModifier",  this);
		modifierTask.setHiringCommand(stC);
		modifierName = new MesquiteString();
		modifierName.setValue(modifierTask.getName());
		if (numModulesAvailable(TreeTransformerMult.class)>1){
			MesquiteSubmenuSpec mss = addSubmenu(null, "Transformer of Trees (" + whatIsMyPurpose() + ")", stC, TreeTransformerMult.class);
			mss.setSelected(modifierName);
		}
		addCheckMenuItem(null, "Discard Untransformable Trees", makeCommand("discardUntransformable", this), discardUntransformable);

		return true;
	}
	/* ................................................................................................................. */
	/** Returns the purpose for which the employee was hired (e.g., "to reconstruct ancestral states" or "for X axis"). */
	public String purposeOfEmployee(MesquiteModule employee) {
		if (employee == currentTreeSource || employee instanceof TreeSource)
			return "to supply trees to be transformed";
		else if (employee == modifierTask)
		return "to transform trees"; // to be overridden
		else return super.purposeOfEmployee(employee);
	}
	/*.................................................................................................................*/
	public void setPreferredTaxa(Taxa taxa){
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = super.getSnapshot(file);
		temp.addLine("setModifier ", modifierTask); 
		temp.addLine("discardUntransformable " + discardUntransformable.toOffOnString()); 
		return temp;
	}
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		lastGoodTree = -1;
		lastTreeRequested = -1;
		maxAvailable = -2;
		noProblemsYet = true;
		super.employeeParametersChanged(employee, source, notification);
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the tree transformer", "[name of module]", commandName, "setModifier")) {
			TreeTransformer temp = (TreeTransformer)replaceEmployee(TreeTransformerMult.class, arguments, "Transformer of trees", modifierTask);
			if (temp !=null){
				modifierTask = temp;
				modifierName.setValue(modifierTask.getName());
				modifierTask.setHiringCommand(stC);
				lastGoodTree = -1;
				lastTreeRequested = -1;
				maxAvailable = -2;
				noProblemsYet = true;
				parametersChanged();
				return modifierTask;
			}
		}
		else if (checker.compare(this.getClass(), "Sets whether or not untransformable trees are discarded or retained", "[on = discard; off=retain]", commandName, "discardUntransformable")) {
			boolean current =discardUntransformable.getValue();
			discardUntransformable.toggleValue(parser.getFirstToken(arguments));
			if (current != discardUntransformable.getValue()){
				noProblemsYet = true;
				lastGoodTree = -1;
				lastTreeRequested = -1;
				maxAvailable = -2;
			}
			valueSpecified = true;
			if (!MesquiteThread.isScripting())
				parametersChanged();
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public MesquiteTree getTreeSimple(Taxa taxa, int ic, MesquiteBoolean success, MesquiteString message, MesquiteString originalTreeName) {  
		Tree tree = getBasisTree(taxa, ic);
		currentTree = ic;
		if (tree == null) {
			return null;
		}
		MesquiteTree modified =  tree.cloneTree();
		success.setValue(modifierTask.transformTree(modified, message, false));
		if (success.getValue())
			modified.setName("Transformed from " + tree.getName() + " (#" + (currentTree + 1) + ")");
		else
			modified.setName("Untransformed tree [" + tree.getName() + "] (#" + (currentTree + 1) + ")");
		originalTreeName.setValue(tree.getName());
		return modified;
	}
	/*.................................................................................................................*/
	public Tree getTree(Taxa taxa, int ic) {  
		MesquiteBoolean success = new MesquiteBoolean(false);
		MesquiteString message = new MesquiteString();
		MesquiteString originalTreeName = new MesquiteString();
		MesquiteTree modified = null;
		if (noProblemsYet){
			modified = getTreeSimple(taxa, ic, success, message, originalTreeName);
			if (modified == null)
				return null;
			else if (success.getValue())
				return modified;
			else {
				if (!queried && !MesquiteThread.isScripting() && !valueSpecified){  
					if (AlertDialog.query(containerOfModule(), "Untransformable tree", "A tree ("  + originalTreeName.getValue() + ") could not be transformed as requested (reason given: " + message + ").  Do you want to discard untransformable trees, or include them untransformed?", "Discard", "Include Untransformed")) {
						//usetrees anyway
						discardUntransformable.setValue(true);

					}
					else
						discardUntransformable.setValue(false);
					queried = true;
					valueSpecified = true;
				}
				if (!discardUntransformable.getValue()) {
					logln(originalTreeName.getValue() + " could not be transformed, and is included untransformed.");
					return modified;
				}
				lastGoodTree = -1;
				lastTreeRequested = -1;
				maxAvailable = -2;
				noProblemsYet = false;

			}
		}
		else 
			modified = new MesquiteTree(taxa);


		int iTry = 0;
		int count = -1;
		if (ic>maxAvailable && maxAvailable>-2)
			return null;
		if (lastTreeRequested == ic)  //recently requested same; return it without checking filter (since change in filter would have reset lastTreeRequested etc.)
			return getBasisTree(taxa, lastGoodTree);
		else if (lastTreeRequested >= 0 && lastTreeRequested == ic-1) {
			//go from last requested
			iTry = lastGoodTree+1;
			count = lastTreeRequested;

		}
		Tree tree = null;

		TreeSource ts = getBasisTreeSource();
		int numTrees = ts.getNumberOfTrees(taxa); 
		while (count<ic) {
			tree = getBasisTree(taxa, iTry);
			if (tree == null)  {
				maxAvailable = count;
				return null;
			}
			modified.setToClone((MesquiteTree)tree);
			if (modifierTask.transformTree(modified, message, false)){
				count++;
			}		
			else {
				tree = null;
			}
			iTry++;
		}
		currentTree = ic;
		lastGoodTree = iTry-1;
		lastTreeRequested = ic;
		return modified;
	}
	/*.................................................................................................................*/
	public int getNumberOfTrees(Taxa taxa) {
		if (noProblemsYet || !discardUntransformable.getValue())
			return getBasisTreeSource().getNumberOfTrees(taxa);
		if (maxAvailable<-1) {
			if (getBasisTreeSource().getNumberOfTrees(taxa) == MesquiteInteger.infinite)
				return MesquiteInteger.infinite;
			return MesquiteInteger.finite; //don't know how many will be discarded
		}
		else
			return maxAvailable+1;
	}
	/*.................................................................................................................*/
	public String getTreeNameString(Taxa taxa, int itree) {
		return "Transformation of tree #" + (itree +1);
	}
	/*.................................................................................................................*/
	public String getParameters() {
		return"Transforming trees from: " + getBasisTreeSource().getNameAndParameters();
	}
	/*.................................................................................................................*/
}

