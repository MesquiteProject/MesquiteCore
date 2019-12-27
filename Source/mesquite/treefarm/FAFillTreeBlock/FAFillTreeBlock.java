/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 



Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.treefarm.FAFillTreeBlock; 

import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class FAFillTreeBlock extends FileProcessor {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e2 = registerEmployeeNeed(TreeBlockFiller.class, getName() + " needs a particular method to make tree blocks.",
				null);
		e2.setPriority(2);
	}
	MesquiteSubmenuSpec mss= null;
	TreeBlockFiller treeFillerTask;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (arguments !=null) {
			treeFillerTask = (TreeBlockFiller)hireNamedEmployee(TreeBlockFiller.class, arguments);
			if (treeFillerTask == null)
				return sorry(getName() + " couldn't start because the requested tree block filler wasn't successfully hired.");
		}
		else {
			treeFillerTask = (TreeBlockFiller)hireEmployee(TreeBlockFiller.class, "Tree supplier");
			if (treeFillerTask == null)
				return sorry(getName() + " couldn't start because no tree block filler module obtained.");
		}
		return true;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return false;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return true; //not really, but to force checking of prerelease
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("setTreeBlockFiller ", treeFillerTask);  
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the module that alters data", "[name of module]", commandName, "setTreeBlockFiller")) {
			TreeBlockFiller temp =  (TreeBlockFiller)replaceEmployee(TreeBlockFiller.class, arguments, "Tree supplier", treeFillerTask);
			if (temp!=null) {
				treeFillerTask = temp;
				return treeFillerTask;
			}

		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/** if returns true, then requests to remain on even after alterFile is called.  Default is false*/
	public boolean pleaseLeaveMeOn(){
		return false;
	}
	/*.................................................................................................................*/
	/** Called to alter file. */
	public boolean processFile(MesquiteFile file){
		MesquiteProject proj = file.getProject();
		if (proj == null)
			return false;
		boolean success = true;
		Taxa taxa = proj.getTaxa(0);
		if (taxa == null)
			return false;
		TreeVector trees = new TreeVector(taxa);
		int howManyTrees =0;
		if (treeFillerTask instanceof TreeSource)
			howManyTrees =((TreeSource)treeFillerTask).getNumberOfTrees(trees.getTaxa());
		if (!treeFillerTask.hasLimitedTrees(trees.getTaxa())){
			howManyTrees = MesquiteInteger.fromString(parser.getNextToken());
			if (!MesquiteInteger.isCombinable(howManyTrees)) {
				return false;
			}
		}


		int before = trees.size();
		mesquite.lib.characters.CharacterData data = proj.getCharacterMatrix(0);
		treeFillerTask.initialize(data.getTaxa());
		treeFillerTask.fillTreeBlock(trees, howManyTrees);

		if (trees.size()==before) {
			logln("Sorry, no trees were returned by " + treeFillerTask.getName());
			return false;
		}

		logln(Integer.toString(trees.size()) + " trees stored in tree block, from " + treeFillerTask.getName());
		trees.addToFile(file, getProject(), findElementManager(TreeVector.class));
		return true;

	}
	/*.................................................................................................................*/
	public String getName() {
		return "Make Trees and Add to File";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Manages tree block filling as an alteration to a file." ;
	}

}


