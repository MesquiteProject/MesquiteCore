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
package mesquite.stochchar.Mk1Rate;

import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.stochchar.lib.*;
import mesquite.categ.lib.*;


/* ======================================================================== */
/**Calculates a number for a character and a tree.*/

public class Mk1Rate extends NumberForCharAndTree  {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(mesquite.stochchar.zMargLikeCateg.zMargLikeCateg.class, getName() + "  needs a module to calculate likelihoods.",
		"The module to calculate likelihoods is arranged automatically");
	}
	MargLikelihoodForModel reconstructTask = null;
	MkModel mk1Model;

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		reconstructTask = (MargLikelihoodForModel)hireNamedEmployee(MargLikelihoodForModel.class, "#zMargLikeCateg");
 		if (reconstructTask == null)
 			return sorry(getName() + " couldn't start because no likelihood calculator obtained");
 		mk1Model = new MkModel("Estimating mk1", CategoricalState.class);
		getProject().getCentralModelListener().addListener(this);
 		return true; 
 	}
 	
	/*.................................................................................................................*/
	public void endJob() {
		getProject().getCentralModelListener().removeListener(this);
		super.endJob();
  	 }
	/*.................................................................................................................*/
	public void changed(Object caller, Object obj, Notification notification){
		int code = Notification.getCode(notification);
		if (obj instanceof Class && MkModel.class.isAssignableFrom((Class)obj)) {
				parametersChanged(notification);
		}
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
   	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
   	public void initialize(Tree tree, CharacterDistribution charStates){
   	}
	MesquiteString resultStringMk1 = new MesquiteString();
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
			
			mk1Model.setInstantaneousRate(MesquiteDouble.unassigned, false);
			reconstructTask.estimateParameters( tree,  observedStates, mk1Model, resultStringMk1);
			result.setValue(mk1Model.getInstantaneousRate());
			if (resultString!=null) {
				resultString.setValue("Rate assuming mk1 model: " + resultStringMk1.getValue());
			}
		}
		saveLastResult(result);
		saveLastResultString(resultString);
	}
   
	/*.................................................................................................................*/
    	 public boolean showCitation() {
		return true;
   	 }
	/*.................................................................................................................*/
    	 public String getVeryShortName() {
		return "Mk1 Rate";
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Mk1 Estimated Rate";
   	 }
 	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Estimates the rate of a character's evolution under the simple Mk1 model." ;
   	 }
   	 
   	public String getParameters(){
   		if (mk1Model !=null)
   			return " Mk1 Model settings: " +  mk1Model.getSettingsString();
   		return null;
   	}
	/*.................................................................................................................*/
	/** Returns CompatibilityTest so other modules know if this is compatible with some object. */
	public CompatibilityTest getCompatibilityTest(){
		return new RequiresExactlyCategoricalData();
	}
 
}


