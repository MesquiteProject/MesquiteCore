/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.diverse.BiSSELikelihoodRatio;


import mesquite.categ.lib.CategoricalDistribution;
import mesquite.categ.lib.RequiresExactlyCategoricalData;
import mesquite.diverse.lib.*;
import mesquite.diverse.BiSSELikelihood.*;
import mesquite.lib.*;
import mesquite.lib.characters.CharacterDistribution;
import mesquite.lib.duties.*;
import mesquite.lib.tree.Tree;

public class BiSSELikelihoodRatio extends NumForCharAndTreeDivers {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(BiSSELikelihood.class, getName() + "  needs a method to calculate likelihoods.",
		"The method to calculate likelihoods is arranged initially");
		e.setSuppressListing(true);
	}

	BiSSELikelihood calcTask1, calcTask2;



	/*.................................................................................................................*/
	public boolean showCitation(){
		return true;
	}
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		calcTask1 = (BiSSELikelihood)hireEmployee(BiSSELikelihood.class, "Calculator for Unconstrained BiSSE Likelihood");
		if (calcTask1 == null)
			return sorry(getName() + " couldn't start because no likelihood calculator module obtained.");
		calcTask2 = (BiSSELikelihood)hireEmployee(BiSSELikelihood.class, "Calculator for Constrained BiSSE Likelihood");
		if (calcTask2 == null)
			return sorry(getName() + " couldn't start because no likelihood calculator module obtained.");
		return true;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 200;  
	}
	public boolean requestPrimaryChoice(){
		return true;
	}
 	public CompatibilityTest getCompatibilityTest(){
		return new RequiresExactlyCategoricalData();
	}
 	public String purposeOfEmployee(MesquiteModule mb){

 		if (calcTask1 == null || mb == calcTask1)  //will be null when first started
 			return "Unconstrained calculation of BiSSE likelihood";
		if (calcTask2 == null || mb == calcTask2)
 			return "Constrained calculation of BiSSE likelihood";
		return "";
 	}
	public void initialize(Tree tree, CharacterDistribution charStates1) {
		// TODO Auto-generated method stub
	}
	/*.................................................................................................................*/

	public Snapshot getSnapshot(MesquiteFile file) {
		final Snapshot temp = new Snapshot();
		temp.addLine("getCalc1 ", calcTask1);
		temp.addLine("getCalc2 ", calcTask2);
		return temp;
	}
	/*.................................................................................................................*/
	/*  the main command handling method.  */
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(getClass(), "Returns first likelihood calculator", null, commandName, "getCalc1")) {
			return calcTask1;
		}
		else if (checker.compare(getClass(), "Returns second likelihood calculator", null, commandName, "getCalc2")) {
			return calcTask2;
		}
		else
			return  super.doCommand(commandName, arguments, checker);
	}


	/*------------------------------------------------------------------------------------------*/
	public void calculateNumber(Tree tree, CharacterDistribution charStates, MesquiteNumber result, MesquiteString resultString) {
		if (result == null)
			return;
		clearResultAndLastResult(result);
		if (tree == null || charStates == null)
			return;
		if (!CategoricalDistribution.isBinaryNoMissing(charStates, tree)){
			if (resultString!=null)
	            resultString.setValue(getName() + " unassigned because the character is not binary or has missing data");
			return;
		}
		MesquiteNumber num1 = new MesquiteNumber();
		MesquiteNumber num2 = new MesquiteNumber();
		calcTask1.calculateNumber(tree, charStates, num1, resultString);
		calcTask2.calculateNumber(tree, charStates, num2, resultString);

		result.setValue(num2);
		result.subtract(num1);
		result.setName("Ln Likelihood Difference");
		result.copyAuxiliaries(new MesquiteNumber[]{num1, num2});
		saveLastResult(result);
		saveLastResultString(resultString);
	}

 	public boolean returnsMultipleValues(){
  		return true;
  	}

	/*------------------------------------------------------------------------------------------*/
	public String getName() {
		return "BiSSE Ln Likelihood Difference";
	}
	
	public String getVeryShortName(){
		return "BiSSE Ln Like. Diff.";
	}

	public String getAuthors() {
		return "Peter E. Midford & Wayne P. Maddison";
	}

	public String getVersion() {
		return "1.0";
	}

	public String getExplanation(){
		return "Calculates the difference in log likelihoods between two BiSSE speciation/extinction models.";
	}

	/*.................................................................................................................*/
	/** returns keywords related to what the module does, for help-related searches. */ 
	public  String getKeywords()  {
		return "diversification birth death";
	}

	public boolean isPrerelease(){
		return false;
	}


}

