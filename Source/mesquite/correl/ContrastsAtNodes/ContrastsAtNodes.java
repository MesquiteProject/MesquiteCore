/* Mesquite source code.  Copyright 1997-2006 W. Maddison and D. Maddison. 
 This module copyright 2006 P. Midford and W. Maddison

Version 1.11, June 2006.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */

package mesquite.correl.ContrastsAtNodes;


import mesquite.categ.lib.*;
import mesquite.correl.lib.*;
import mesquite.lib.*;
import mesquite.lib.characters.CharacterDistribution;
import mesquite.lib.duties.*;

public class ContrastsAtNodes extends NumbersForNodesWithChar {


	private CategoricalDistribution observedStates1;
	ContrastsForCharAndTree contrastsTask;
	MesquiteString contrastsTaskName;

	
	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {
		contrastsTask = (ContrastsForCharAndTree)hireEmployee(commandRec, ContrastsForCharAndTree.class, "Contrasts calculator");
		if (contrastsTask == null)
			return sorry(commandRec, getName() + " couldn't start because no contrasts calculator obtained.");
		contrastsTaskName = new MesquiteString(contrastsTask.getName());
		MesquiteSubmenuSpec mss = addSubmenu(null, "Contrast calculator", makeCommand("setContrastCalculator", this), ContrastsForCharAndTree.class);
		mss.setCompatibilityCheck(CategoricalState.class);
		mss.setSelected(contrastsTaskName);
		return true;
	}

	/*.................................................................................................................*/
	public void employeeQuit(MesquiteModule m){
		if (m == contrastsTask)
			iQuit();
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("setContrastCalculator ",contrastsTask);
		return temp;
	}
	public Object doCommand(String commandName, String arguments, CommandRecord commandRec, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets module used to calculate contrasts", "[name of module]", commandName, "setContrastCalculator")) {
			ContrastsForCharAndTree temp=  (ContrastsForCharAndTree)replaceEmployee(commandRec, ContrastsForCharAndTree.class, arguments, "Contrast calculator", contrastsTask);
			if (temp!=null) {
				contrastsTask= temp;
				contrastsTaskName.setValue(contrastsTask.getName());
				parametersChanged(null, commandRec);
				
			}
			return contrastsTask;
		}
		else
			return super.doCommand(commandName, arguments, commandRec, checker);
		
	}

public void initialize(Tree tree, CommandRecord commandRec){
}
String charName = "";
	public void calculateNumbers(Tree tree, CharacterDistribution charDistribution, NumberArray result, MesquiteString resultString, CommandRecord commandRec){
		charName = "Not successful";
		if (result == null)
			return;
		result.deassignArray();
		if (tree == null || charDistribution == null)
			return;
		if (!(charDistribution instanceof CategoricalDistribution)){
			if (resultString != null)
				resultString.setValue("Contrasts At Nodes can't be calculated because character is not categorical");
			return;
		}
		observedStates1 = (CategoricalDistribution)charDistribution;
		if (observedStates1.getMaxState() > 1) {
			if (resultString != null)
				resultString.setValue("Contrasts At Nodes can't be calculated because character is not binary");
			return;
		}
		charName = observedStates1.getName();
		result.resetSize(tree.getNumNodeSpaces());
		contrastsTask.calculateContrasts(tree, charDistribution, result, resultString, commandRec);
	}
	
	
	/*.................................................................................................................*/


	public String getVeryShortName() {
		return "CategContrasts at nodes";
	}
	
	public String getAuthors() {
		return "Wayne P. Maddison & Peter E. Midford";
	}

	public String getVersion() {
		return "0.1";
	}

	public String getName() {
		return "CategContrasts at nodes";
	}
	public String getNameAndParameters() {
		return "CategContrasts at nodes (" + charName + ")";
	}

	public String getExplanation(){
		return "Contrasts for categorical characters using an ad hoc procedure analogous to Felsenstein's contrasts";
	}

	public boolean isPrerelease(){
		return true;
	}

}

