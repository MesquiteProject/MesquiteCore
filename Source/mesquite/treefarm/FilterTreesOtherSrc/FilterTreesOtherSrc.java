/* Mesquite source code, Treefarm package.  Copyright 1997 and onward, W. Maddison, D. Maddison and P. Midford. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.treefarm.FilterTreesOtherSrc;
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.treefarm.lib.*;

/* ======================================================================== */
public class FilterTreesOtherSrc extends SourceFromTreeSource {
	public String getName() {
		return "Filter Trees from Other Source";
	}
	public String getNameForMenuItem() {
		return "Filter Trees from Other Source...";
	}
	public String getExplanation() {
		return "Filters trees from another source.";
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		super.getEmployeeNeeds();
		EmployeeNeed e = registerEmployeeNeed(BooleanForTree.class, getName() + "  needs a criterion by which to filter trees.",
		"The criterion to filter trees can be chosen initially or in the Filter of Trees submenu");
	}
	int currentTree=0;
	int lastGoodTree = -1;
	int lastTreeRequested = -1;
	int maxAvailable = -2;
	BooleanForTree booleanTask;
	MesquiteString booleanName;
	MesquiteCommand stC;
	MesquiteBoolean positiveFilter;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (!super.startJob(arguments, condition, hiredByName))
			return false;
		booleanTask = (BooleanForTree)hireEmployee(BooleanForTree.class, "Filter of trees");
		if (booleanTask == null) {
			return sorry(getName() + " couldn't start because no tree filter was obtained.");
		}
		positiveFilter = new MesquiteBoolean(true);
		stC = makeCommand("setBoolean",  this);
		booleanTask.setHiringCommand(stC);
		booleanName = new MesquiteString();
		booleanName.setValue(booleanTask.getName());
		if (numModulesAvailable(BooleanForTree.class)>1){
			MesquiteSubmenuSpec mss = addSubmenu(null, "Filter of Trees", stC, BooleanForTree.class);
			mss.setSelected(booleanName);
		}
		if (!MesquiteThread.isScripting())
			positiveFilter.setValue(AlertDialog.query(containerOfModule(), "Keep Trees that Satisfy?", "The filter can either keep those trees that satisfy the criterion, or those trees that fail the criterion.  Which to keep?", 
					"Satisfy", "Fail", 0, null));
					
		addCheckMenuItem(null, "Keep Trees Satisfying Criterion", makeCommand("togglePositiveFilter", this), positiveFilter);
		return true;
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
		temp.addLine("setBoolean ", booleanTask); 
		temp.addLine("togglePositiveFilter " + positiveFilter.toOffOnString());
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the tree filter", "[name of module]", commandName, "setBoolean")) {
			BooleanForTree temp = (BooleanForTree)replaceEmployee(BooleanForTree.class, arguments, "Filter of trees", booleanTask);
			if (temp !=null){
				booleanTask = temp;
				booleanName.setValue(booleanTask.getName());
				booleanTask.setHiringCommand(stC);
				lastGoodTree = -1;
				lastTreeRequested = -1;
				maxAvailable = -2;
				parametersChanged();
				return booleanTask;
			}
		}
		else if (checker.compare(this.getClass(), "Sets whether or not to keep those trees that satisfy the criterion, or not", "[on or off]", commandName, "togglePositiveFilter")) {
			positiveFilter.toggleValue(parser.getFirstToken(arguments));
			parametersChanged();
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		lastGoodTree = -1;
		lastTreeRequested = -1;
		maxAvailable = -2;
		super.employeeParametersChanged(this, source, notification);
	}
	/*.................................................................................................................*/
	public Tree getTree(Taxa taxa, int ic) {  
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
		MesquiteBoolean result = new MesquiteBoolean(false);
		TreeSource ts = getBasisTreeSource();
		int numTrees = ts.getNumberOfTrees(taxa); 
		while (count<ic) {
			tree = getBasisTree(taxa, iTry);
			if (tree == null)  {
				maxAvailable = count;
				return null;
			}
			booleanTask.calculateBoolean(tree, result, null);	
			if ((result.getValue() && positiveFilter.getValue()) || (!result.getValue() && !positiveFilter.getValue())){
				count++;
			}		
			else
				tree = null;
			iTry++;
		}
		currentTree = ic;
		lastGoodTree = iTry-1;
		lastTreeRequested = ic;
		return tree.cloneTree();
	}
	/*.................................................................................................................*/
	public int getNumberOfTrees(Taxa taxa) {
		if (maxAvailable<-1) {
			if (getBasisTreeSource().getNumberOfTrees(taxa) == MesquiteInteger.infinite)
				return MesquiteInteger.infinite;
			return MesquiteInteger.finite; //don't know how many will be filtered
		}
		else
			return maxAvailable+1;
	}
	/*.................................................................................................................*/
	public String getTreeNameString(Taxa taxa, int itree) {
		return "Tree #" + (itree +1) + " filtered";
	}
	/*.................................................................................................................*/
	public String getParameters() {
		if (getBasisTreeSource() == null || booleanTask == null)
			return null;
		return"Filtering trees from: " + getBasisTreeSource().getNameAndParameters() + " using the filter " + booleanTask.getNameAndParameters();
	}
	/*.................................................................................................................*/

}

