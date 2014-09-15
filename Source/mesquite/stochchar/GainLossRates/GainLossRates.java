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
package mesquite.stochchar.GainLossRates;

import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.stochchar.lib.*;
import mesquite.categ.lib.*;


/* ======================================================================== */
/**Calculates a number for a character and a tree.*/

public class GainLossRates extends NumberForCharAndTree  {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(mesquite.stochchar.zMargLikeCateg.zMargLikeCateg.class, getName() + "  needs a module to calculate likelihoods.",
		"The module to calculate likelihoods is arranged automatically");
	}
	MargLikelihoodForModel reconstructTask = null;
	AsymmModel asymmModel;
	MesquiteNumber likelihood = new MesquiteNumber();
	static final int BIAS = 0;
	static final int GAIN = 1;  
	static final int LOSS = 2;
	static final int RATE = 3;
	StringArray reportModes;
	int reportMode = BIAS;
	MesquiteString reportModeName;

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		reconstructTask = (MargLikelihoodForModel)hireNamedEmployee(MargLikelihoodForModel.class, "#zMargLikeCateg");
 		if (reconstructTask == null)
 			return sorry(getName() + " couldn't start because no likelihood calculator obtained");
 		asymmModel = new AsymmModel("Estimating Asymm", CategoricalState.class);
		reportModes = new StringArray(4);  
			reportModes.setValue(BIAS, "Bias");  //the strings passed will be the menu item labels
			reportModes.setValue(GAIN, "Forward Rate");
			reportModes.setValue(LOSS, "Backward Rate");
			reportModes.setValue(RATE, "Overall Rate");
			reportModeName = new MesquiteString(reportModes.getValue(reportMode));  //this helps the menu keep track of checkmenuitems
			MesquiteSubmenuSpec mss = addSubmenu(null, "Show", makeCommand("setReportMode", this), reportModes); 
			mss.setSelected(reportModeName);
			
		getProject().getCentralModelListener().addListener(this);
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
		if (obj instanceof Class && (AsymmModel.class.isAssignableFrom((Class)obj))) {
				parametersChanged(notification);
		}
	}
	/*.................................................................................................................*/
   	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
   	public void initialize(Tree tree, CharacterDistribution charStates){
   	}
	/*.................................................................................................................*/
	public  void calculateNumber(Tree tree, CharacterDistribution observedStates, MesquiteNumber result, MesquiteString resultString) {  
    	 	if (result==null)
    	 		return;
    	clearResultAndLastResult(result);
		if (tree==null || observedStates==null) {
			if (resultString!=null)
				resultString.setValue("Gain or loss rate estimates unassigned because no tree or no character supplied");
			return;
		}
	

		if (reconstructTask != null) {
			asymmModel.deassignParameters();
			//asymmModel.setUseRateBiasNotation(reportMode == BIAS || reportMode == RATE);
			asymmModel.setUseRateBiasNotation(reportMode == BIAS || reportMode == RATE);
			reconstructTask.estimateParameters(tree, observedStates, asymmModel, resultString);
			if (reportMode == BIAS || reportMode == GAIN) {
				result.setValue(asymmModel.getParam1());
			}
			else if ( reportMode == RATE ||  reportMode == LOSS){
				result.setValue(asymmModel.getParam0());
			}

		}
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	/*.................................................................................................................*/
	 public Snapshot getSnapshot(MesquiteFile file) {
	 		
		Snapshot temp = new Snapshot();
		 temp.addLine("setReportMode " + ParseUtil.tokenize(reportModes.getValue(reportMode)));
	 	return temp;
	 }


	/*.................................................................................................................*/
	/*  the main command handling method.  */
	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(getClass(), "Sets the report mode", null, commandName, "setReportMode")) {
			String name = parser.getFirstToken(arguments); //get argument passed of option chosen
			int newMode = reportModes.indexOf(name); //see if the option is recognized by its name
			if (newMode >=0 && newMode!=reportMode){
				reportMode = newMode; //change mode
	    			reportModeName.setValue(reportModes.getValue(reportMode)); //so that menu item knows to become checked
	    	 		parametersChanged(); //this tells employer module that things changed, and recalculation should be requested
		 	}
	 	}
	 	else
	 		return  super.doCommand(commandName, arguments, checker);
		return null;
	}
   
	/*.................................................................................................................*/
    	 public String getVeryShortName() {
		return reportModes.getValue(reportMode);
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Forward/Backward Rates";
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
  	 public String getParameters() {
  	 	String s = "Reporting  " + reportModes.getValue(reportMode);
		if (asymmModel !=null)
   			return s + "; AsymmMk Model settings: " +  asymmModel.getSettingsString();
   		return s;
   	}
 	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Uses maximum likelihood to estmate the rates of forward and backward changes (0 to 1 and 1 to 0 changes respectively), or alternatively the overall rate and the bias in gains versus losses, using the AsymmMk model on a tree for a given character." ;
   	 }
	/*.................................................................................................................*/
	/** Returns CompatibilityTest so other modules know if this is compatible with some object. */
	public CompatibilityTest getCompatibilityTest(){
		return new RequiresExactlyCategoricalData();
	}
	 
}


