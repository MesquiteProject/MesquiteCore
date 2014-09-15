/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 
. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.stochchar.AsymmetryLR;

import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.stochchar.lib.*;
import mesquite.categ.lib.*;
import JSci.maths.statistics.ChiSqrDistribution;


/* ======================================================================== */
/**Calculates a number for a character and a tree.*/

public class AsymmetryLR extends NumberForCharAndTree  {
	   ChiSqrDistribution chiSquare;
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(mesquite.stochchar.zMargLikeCateg.zMargLikeCateg.class, getName() + "  needs a module to calculate likelihoods.",
		"The module to calculate likelihoods is arranged automatically");
	}
	/*.................................................................................................................*/
	MargLikelihoodForModel reconstructTask = null;
	AsymmModel asymmModel;
	MkModel mk1Model;
	MesquiteNumber likelihood = new MesquiteNumber();

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		reconstructTask = (MargLikelihoodForModel)hireNamedEmployee(MargLikelihoodForModel.class, "#zMargLikeCateg");
 		if (reconstructTask == null)
 			return sorry(getName() + " couldn't start because no likelihood calculator obtained");
 		asymmModel = new AsymmModel("Estimating Asymm", CategoricalState.class);
 		mk1Model = new MkModel("Estimating mk1", CategoricalState.class);
		getProject().getCentralModelListener().addListener(this);
		chiSquare = new ChiSqrDistribution(1);
 		return true; 
 	}
 	
	/*.................................................................................................................*/
	public void endJob() {
		getProject().getCentralModelListener().removeListener(this);
		super.endJob();
  	 }
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
	/*.................................................................................................................*/
	public void changed(Object caller, Object obj, Notification notification){
		int code = Notification.getCode(notification);
		if (obj instanceof Class && (MkModel.class.isAssignableFrom((Class)obj) || AsymmModel.class.isAssignableFrom((Class)obj))) {
				parametersChanged(notification);
		}
	}
 	
	/*.................................................................................................................*/
   	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
   	public void initialize(Tree tree, CharacterDistribution charStates){
   	}
	MesquiteString resultStringMk1 = new MesquiteString();
	MesquiteString resultStringAsymmMk = new MesquiteString();
	/*.................................................................................................................*/
	public  void calculateNumber(Tree tree, CharacterDistribution observedStates, MesquiteNumber result, MesquiteString resultString) {  
    	 	if (result==null)
    	 		return;
    	clearResultAndLastResult(result);
		if (tree==null || observedStates==null) {
			if (resultString!=null)
				resultString.setValue("Likelihood ratio unassigned because no tree or no character supplied");
			return;
		}
	

		if (reconstructTask != null) {
			asymmModel.deassignParameters();
			mk1Model.deassignParameters();
			reconstructTask.calculateLogProbability( tree,  observedStates, mk1Model, resultStringMk1, likelihood);
			result.setValue(likelihood);
			reconstructTask.calculateLogProbability( tree,  observedStates, asymmModel, resultStringAsymmMk, likelihood);
			result.subtract(likelihood);
			likelihood.setValue(2.0);
			result.multiplyBy(likelihood);
			likelihood.setValue(result);
			if (resultString!=null) {
				if (result.isCombinable() && !result.isNegative()) {
					String p = " [Assuming chi-square 1 d.f., p = " + MesquiteDouble.toStringDigitsSpecified(1.0-chiSquare.cumulative(result.getDoubleValue()), 4) + "]";
				/*	double d = result.getDoubleValue();
					if (d <2.706)
						p+= "> 0.10";
					else if (d < 3.841)
						p+= "< 0.10";
					else if (d < 5.024)
						p+= "< 0.05";
					else if (d < 6.635)
						p+= "< 0.01";
					else if (d < 7.879)
						p+= "<0.005";
					else 
						p+= "< 0.001";
						*/
					resultString.setValue("2*ln(likelihood ratio) of asymmetrical vs. symmetrical model: " + result + p);
				}
				else
					resultString.setValue("Problem calculating likelihood ratio.  Details from symmetrical calculation: " + resultStringMk1.getValue() + " Details from asymmetrical calculation     " + resultStringAsymmMk.getValue());
			}
		}
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	/*.................................................................................................................*/
    	 public String getVeryShortName() {
		return "Asymmetry 2ln(LR)";
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Asymmetry Likelihood Ratio Test";
   	 }
	/*.................................................................................................................*/
    	 public boolean showCitation() {
		return true;
   	 }
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
  	 	return false;
  	 }
 	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Calculates the test statistic for the likelihood ratio test comparing the asymmetrical and one parameter models [2ln(L(Asymm.)/L(Mk1)], on a tree for a given character." ;
   	 }
  	 
 	public String getParameters(){
   		if (asymmModel !=null)
   			return " AsymmMk Model settings: " +  asymmModel.getSettingsString();
   		return null;
   	}
	/*.................................................................................................................*/
	/** Returns CompatibilityTest so other modules know if this is compatible with some object. */
	public CompatibilityTest getCompatibilityTest(){
		return new RequiresExactlyCategoricalData();
	}

}  

