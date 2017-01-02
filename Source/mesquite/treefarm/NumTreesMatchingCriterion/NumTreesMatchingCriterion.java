package mesquite.treefarm.NumTreesMatchingCriterion;
/* Mesquite source code, NumTreesMatchingCriterion.  J.C. Oliver.  July 2010.

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.stochchar.lib.ProbModelSourceLike;

public class NumTreesMatchingCriterion extends NumberForTreeBlock {
	BooleanForTree treeCriterionTask;
	MesquiteCommand criterionCommand;
	
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e1 = registerEmployeeNeed(BooleanForTree.class, getName() + " needs a criterion for trees.", "The criterion for trees trees is indicated initially.");
	}
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		treeCriterionTask = (BooleanForTree)hireEmployee(BooleanForTree.class, "Criterion for Trees");
		if(treeCriterionTask == null)
			return sorry(getName() + " could not start because no tree criterion module was found.");
		criterionCommand =makeCommand("setCriterion",  this);
		treeCriterionTask.setHiringCommand(criterionCommand);
		MesquiteSubmenuSpec mss = addSubmenu(null, "Criterion for Trees", criterionCommand);
		mss.setList(BooleanForTree.class);
		return true;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("setCriterion ",treeCriterionTask);
		return temp;
	}
	/*.................................................................................................................*/
	public void calculateNumber(TreeVector trees, MesquiteNumber result, MesquiteString resultString) {
		if (result==null || trees ==null)
			return;
		clearResultAndLastResult(result);
		int metCriterion = 0;
		MesquiteBoolean criterionHolder = new MesquiteBoolean();
		for(int it = 0; it < trees.size(); it++){
			Tree tree = trees.getTree(it);
			criterionHolder.setToUnassigned();
			treeCriterionTask.calculateBoolean(tree, criterionHolder, null);
			if(criterionHolder.getValue()){
				metCriterion++;
			}
		}

		result.setValue(metCriterion);
		if (resultString!=null)
			resultString.setValue(result.toString() + " of " + trees.size() + " met the specified criterion (" + treeCriterionTask.getName() + ")");
		saveLastResult(result);
		saveLastResultString(resultString);

	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if(checker.compare(this.getClass(), "Sets module for tree criterion", "[name of module]", commandName, "setCriterion")){
			BooleanForTree tempCriterion = (BooleanForTree)replaceEmployee(BooleanForTree.class, arguments, "Criterion for trees", treeCriterionTask);
			if(tempCriterion != null){
				treeCriterionTask = tempCriterion;
				parametersChanged();
			}
			return treeCriterionTask;
		}
		else
			return  super.doCommand(commandName, arguments, checker);
	}
	/*.................................................................................................................*/
	public void initialize(TreeVector trees) {
		treeCriterionTask.initialize(trees.getTree(0));
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Number of Trees Matching Criterion";
	}
	/*.................................................................................................................*/
	public String getExplanation(){
		return "Counts the number of trees in a tree block that match a user-specified criterion.  For example, it could " +
				"count the number of trees in a tree block which are congruent with a particular tree topology (by " +
				"selecting the \"Tree Congruent with Specified Tree Topology\" module in the Criterion for Trees dialog).";
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
    /*........................................................*/
    public int getVersionOfFirstRelease(){
        return 273;
    }
}
