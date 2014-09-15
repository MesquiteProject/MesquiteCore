/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trees.NumForCharRespTree;

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class NumForCharRespTree extends NumberForCharacter {
	public String getName() {
		return "Character Value with Respective Tree";
	}
	public String getNameForMenuItem() {
		return "Character Value with Respective Tree...";
	}
	public String getVeryShortName() {
		if (numberTask ==null)
			return "Char. value with respective tree";
		else
			return numberTask.getVeryShortName() + " (with tree)";
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 200;  
	}

	public String getExplanation() {
		return "Coordinates the calculation of a number for a character based on a respective tree." ;
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(NumberForCharAndTree.class, getName() + "  needs a method to calculate the value for a character using the trees.",
		"The method to calculate values can be selected initially or in the Values submenu");
		EmployeeNeed e2 = registerEmployeeNeed(TreeSource.class, getName() + "  needs a source for trees.",
		"The source for trees can be requested initially or using the Tree Source submenu");
		//e2.setDivertChainMessage("testing");
	}
	/*.................................................................................................................*/
	NumberForCharAndTree numberTask;
	TreeSource treeTask;
	Taxa taxa;
	Tree tree;
	MesquiteString treeSourceName;
	MesquiteCommand tsC;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		treeTask = (TreeSource)hireEmployee(TreeSource.class, "Source of trees (for Character Value)");
		if (treeTask == null)
			return sorry(getName() + " couldn't start because no source of trees obtained");
		treeSourceName = new MesquiteString(treeTask.getName());
		tsC = makeCommand("setTreeSource",  this);
		treeTask.setHiringCommand(tsC);
		if (numModulesAvailable(TreeSource.class)>1) {
			MesquiteSubmenuSpec mss = addSubmenu(null, "Tree Source", tsC);
			mss.setSelected(treeSourceName);
			mss.setList(TreeSource.class);
		}
		numberTask = (NumberForCharAndTree)hireEmployee(NumberForCharAndTree.class, "Value to calculate for character");
		if (numberTask == null)
			return sorry(getName() + " couldn't start because no calculator module obtained.");

		return true;
	}
	public boolean returnsMultipleValues(){
		return numberTask.returnsMultipleValues();
	}
	public CompatibilityTest getCompatibilityTest(){
		if (numberTask !=null)
			return numberTask.getCompatibilityTest();
		return null;
	}
	public void employeeQuit(MesquiteModule m){
		iQuit();
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();

		temp.addLine("setTreeSource " , treeTask);

		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the source of trees", "[name of tree source module]", commandName, "setTreeSource")) {
			TreeSource temp = (TreeSource)replaceEmployee(TreeSource.class, arguments, "Source of trees (for Character Value)", treeTask);
			if (temp !=null){
				treeTask = temp;
				treeTask.setPreferredTaxa(taxa);
				treeTask.setHiringCommand(tsC);
				treeSourceName.setValue(treeTask.getName());
				parametersChanged();
				return treeTask;
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(CharacterDistribution charStates){
		if (charStates == null)
			return;
		CharacterData data = charStates.getParentData();
		if (data!=null) {
			taxa = data.getTaxa();
			treeTask.initialize(taxa);
			numberTask.initialize(treeTask.getTree(taxa, 0), charStates);
		}
		if (taxa==null)
			taxa = getProject().chooseTaxa(containerOfModule(), "Taxa"); 

	}
	MesquiteString rs = new MesquiteString();
	/*.................................................................................................................*/
	public  void calculateNumber(CharacterDistribution charStates, MesquiteNumber result, MesquiteString resultString) {
		if (result==null)
			return;
	   	clearResultAndLastResult(result);
		if (taxa==null){
			initialize(charStates);
		}
		int currentTree = charStates.getParentCharacter();
		tree = treeTask.getTree(taxa, currentTree);
		rs.setValue("");
		if (tree != null){
			numberTask.calculateNumber(tree, charStates, result, rs);
			if (resultString!=null) {
				resultString.setValue("For tree " + tree.getName() + ", ");
				resultString.append(rs.toString());
			}
		}
		else if (resultString != null){
			resultString.setValue("No Tree");
		}
		
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	/*.................................................................................................................*/

	/*.................................................................................................................*/
	public String getParameters(){
		if (tree ==null)
			return "Calculator: " + numberTask.getName(); //of which tree??
		else
			return "Calculator: " + numberTask.getName() + " with tree \"" + tree.getName() + "\""; 
	}


}

