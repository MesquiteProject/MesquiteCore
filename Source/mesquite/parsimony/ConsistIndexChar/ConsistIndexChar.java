/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.parsimony.ConsistIndexChar;
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.categ.lib.CategoricalDistribution;
import mesquite.categ.lib.MCategoricalDistribution;
import mesquite.categ.lib.RequiresAnyCategoricalData;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.parsimony.lib.*;

/* ======================================================================== */
public class ConsistIndexChar extends NumberForCharAndTree {
	public String getName() {
		return "Consistency Index for Character";
	}
	public String getExplanation() {
		return "Calculates the Consistency Index (CI) for a tree and character." ;
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed

		EmployeeNeed e = registerEmployeeNeed(mesquite.parsimony.ParsCharSteps.ParsCharSteps.class, getName() + " needs parsimony character steps calculated.",
		"This is hired automatically.");

	}
	public int getVersionOfFirstRelease(){
		return 270;  
	}
	/*.................................................................................................................*/
	CharacterSteps stepsTask;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		stepsTask = (CharacterSteps)hireNamedEmployee(CharacterSteps.class, "#ParsCharSteps");
		if (stepsTask == null)
			return sorry(getName() + " couldn't start because no calculator of parsimony steps could be obtained.");
		return true; //false if no appropriate employees!
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	public void employeeQuit(MesquiteModule m){
		if (m==stepsTask)
			iQuit();
	}


	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Tree tree, CharacterDistribution observedStates){
	}
	MesquiteTree bush;
	MesquiteNumber onTree = new MesquiteNumber();
	MesquiteNumber onSoftBush = new MesquiteNumber();
	/*.................................................................................................................*/
	/*in future this will farm out the calculation to the modules that deal with appropriate character model*/
	public  void calculateNumber(Tree tree, CharacterDistribution observedStates, MesquiteNumber result, MesquiteString resultString) {  
		if (result==null)
			return;
	   	clearResultAndLastResult(result);
		if (resultString!=null)
			resultString.setValue("");
	   	if (!(observedStates instanceof CategoricalDistribution)){
			if (resultString != null)
				resultString.setValue("C.I. is calculated only for categorical characters.");
	   		return;
	   	}
		if (observedStates==null || tree == null || !(tree instanceof MesquiteTree)) {
			return;
		}
		onTree.setToUnassigned();
		onSoftBush.setToUnassigned();
		stepsTask.calculateNumber(tree, observedStates, onTree, resultString);

		if (bush == null)
			bush = new MesquiteTree(observedStates.getTaxa());
		bush.setToClone((MesquiteTree)tree);  //this used rather than setToDefaultBush in case incoming tree doesn't include all taxa
		bush.collapseAllBranches(bush.getRoot(), false, false);
		/** Sets the polytomies assumption for this tree; 0= hard; 1 = soft; 2 = unassigned.*/

		bush.setPolytomiesAssumption(1, false);  //soft
		stepsTask.calculateNumber(bush, observedStates, onSoftBush, resultString);

		result.setValue(onSoftBush);
		if (onTree.isZero())
			result.setValue(0);
		else
			result.divideBy(onTree);

		//(minimum)/(steps)
		saveLastResult(result);
		if (resultString != null)
			resultString.setValue("C.I.: " + result);
		saveLastResultString(resultString);
	}
	/*.................................................................................................................*/
	/** Returns the name of the module in very short form.  For use for column headings and other constrained places.  Unless overridden returns getName()*/
	public String getVeryShortName(){
		return "C.I.";
	}
	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return false;
	}
	public boolean showCitation(){
		return true;
	}
	public CompatibilityTest getCompatibilityTest(){
		return new RequiresAnyCategoricalData();
	}

}


