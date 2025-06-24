/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.parsimony.NumCharsHomoplasious;
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.categ.lib.CategoricalState;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.Tree;
import mesquite.parsimony.lib.*;


/* ======================================================================== */
public class NumCharsHomoplasious extends NumberForMatrixAndTree {
	public String getName() {
		return "Num. Characters with Homoplasy on Tree";
	}
	public String getVeryShortName() {
		return "# Chars. w/Homoplasy on Tree";
	}
	public String getExplanation() {
		return "Calculates how many characters in a matrix are homoplasious for a given tree.";
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e2 = registerEmployeeNeed(CharacterSteps.class, getName() + " uses a module to calculate parsimony steps.",
		"The parsimony steps module is employed automatically; you don't have to do anything to choose it.");
	}
	/*.................................................................................................................*/
	MesquiteNumber numHomoplasious;
	CharacterSteps charStepsTask;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		numHomoplasious=new MesquiteNumber();
		charStepsTask = (CharacterSteps)hireEmployee(CharacterSteps.class, null);
		if (charStepsTask ==null)
			return sorry(getName() + " couldn't start because no step counting module was obtained.");
		return true;
	}
	public void employeeQuit(MesquiteModule m){
		iQuit();
	}

	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Tree tree, MCharactersDistribution matrix){
	}
	MesquiteTree bush;
	/*.................................................................................................................*/
	public void calculateNumber(Tree tree, MCharactersDistribution matrix, MesquiteNumber result, MesquiteString resultString) {
		if (result==null || tree == null || matrix == null)
			return;
		if (resultString !=null)
			resultString.setValue("");
	   	clearResultAndLastResult(result);

		numHomoplasious.setValue((int)0);
		int count=0; 
		if (bush == null)
			bush = new MesquiteTree(matrix.getTaxa());
		bush.setToClone((MesquiteTree)tree);  //this used rather than setToDefaultBush in case incoming tree doesn't include all taxa
		bush.setPolytomiesAssumption(1, false);

		bush.collapseAllBranches(bush.getRoot(), false, false);
		MesquiteNumber cNum = new MesquiteNumber();
		MesquiteNumber bNum = new MesquiteNumber();
		boolean someNotCounted = false;
		for (int ic=0;  ic<matrix.getNumChars(); ic++) {
			if (matrix.isCurrentlyIncluded(ic)){
				CharacterDistribution charStates = matrix.getCharacterDistribution(ic);
				if (charStates!=null){
					cNum.setToUnassigned();
					charStepsTask.calculateNumber(tree, charStates, cNum, null);
					bNum.setToUnassigned();
					charStepsTask.calculateNumber(bush, charStates, bNum, null);
					if (cNum.isCombinable()){
						if (cNum.isMoreThan(bNum))
							numHomoplasious.add(1);
						count++;
					}
					else
						someNotCounted = true;
				}
			}
			else
				someNotCounted = true;
		}
		String exclString = null;
		if (someNotCounted)
			exclString = " (" + count + " characters included)";
		else
			exclString = "";
		result.setValue(numHomoplasious);
		if (resultString!=null)
			resultString.setValue("Number homoplasious: "+ result.toString() + exclString);
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return false;
	}
	public boolean showCitation(){
		return false;
	}
 	/*.................................................................................................................*/
 	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
 	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
 	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
 	public int getVersionOfFirstRelease(){
 		return NEXTRELEASE;  
 	}
}

