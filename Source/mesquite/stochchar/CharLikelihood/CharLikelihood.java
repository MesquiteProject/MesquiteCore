/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.stochchar.CharLikelihood;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import mesquite.cont.lib.*;
import mesquite.stochchar.CurrentProbModelsSim.*;
import mesquite.stochchar.lib.*;

/* ======================================================================== */
public class CharLikelihood extends CharacterLikelihood {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(ProbModelSourceLike.class, getName() + " needs an indication of what probabilistic model to apply to the character.",
		"The indicator of probabilistic models can be selected initially");
		EmployeeNeed e2 = registerEmployeeNeed(MargLikelihoodForModel.class, getName() + "  needs methods to calculate likelihoods.",
		"The methods to calculate likelihoods are chosen automatically according to the probability model used in the calculation");
	}
	/*.................................................................................................................*/
	CharacterDistribution observedStates;
	MesquiteNumber likelihood;
	ProbModelSourceLike modelTask;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		hireAllEmployees(MargLikelihoodForModel.class);
		modelTask = (ProbModelSourceLike)hireEmployee(ProbModelSourceLike.class, "Source of probability models (for Character Likelihood)");
		if (modelTask == null)
			return sorry(getName() + " couldn't start because no source of models of character evolution obtained");
		likelihood = new MesquiteNumber();
		getProject().getCentralModelListener().addListener(this);//to listen for changes to model or static changes to class of current model
		return true; //TODO: false if no appropriate employees!
	}

	/*.................................................................................................................*/
	public void endJob() {
		getProject().getCentralModelListener().removeListener(this);
		super.endJob();
	}
	public void changed(Object caller, Object obj, Notification notification) { //this is a suboptimal way to do this; should it be responsibility of modelTask????
		if (obj instanceof ProbabilityModel) {
			parametersChanged(notification);
		}
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("setModelSource ",modelTask);
		return temp;
	}
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets module used to supply character models", "[name of module]", commandName, "setModelSource")) {
			ProbModelSourceLike temp=  (ProbModelSourceLike)replaceEmployee(ProbModelSourceLike.class, arguments, "Source of probability character models", modelTask);
			if (temp!=null) {
				modelTask= temp;
				parametersChanged();
			}
			return modelTask;
		}
		else
			return  super.doCommand(commandName, arguments, checker);
	}

	public void employeeQuit(MesquiteModule m){
		if (m == modelTask)
			iQuit();
	}
	/*.................................................................................................................*/
	public void initialize(Tree tree, CharacterDistribution observedStates){
	}
	boolean warned = false;
	boolean warnedNoCalc = false;
	CharacterData oldData = null;
	MargLikelihoodForModel reconstructTask = null;
	/*.................................................................................................................*/
	public  void calculateNumber(Tree tree, CharacterDistribution observedStates, MesquiteNumber result, MesquiteString resultString) {  
		if (result==null)
			return;
		clearResultAndLastResult(result);
		if (tree==null || observedStates==null) {
			if (resultString!=null)
				resultString.setValue("Log Likelihood unassigned");
			return;
		}

		result.setToUnassigned();
		this.observedStates = observedStates;

		//a barrier (temporary) while likelihood calculations support only simple categorical
		Class stateClass = observedStates.getStateClass();
		if (DNAState.class.isAssignableFrom(stateClass) || ProteinState.class.isAssignableFrom(stateClass) || ContinuousState.class.isAssignableFrom(stateClass)) {
			String s = "Likelihood calculations cannot be performed ";
			if (DNAState.class.isAssignableFrom(stateClass))
				s += "currently with DNA or RNA data.  The calculations were not done.";
			else if (ProteinState.class.isAssignableFrom(stateClass))
				s += "currently with protein data.  The calculations were not done.";
			else if (ContinuousState.class.isAssignableFrom(stateClass))
				s += "currently with continuous valued data.  The calculations were not done.";
			if (!warnedNoCalc) {
				discreetAlert( s);
				warnedNoCalc = true;
			}
			if (resultString!=null)
				resultString.setValue(s);
			return;
		}

		CharacterModel model = modelTask.getCharacterModel(observedStates);

		if (model == null){
			if (observedStates.getParentData()!= oldData)
				warned = false;
			if (!warned){
				if (observedStates.getParentData()!=null && modelTask instanceof CurrentProbModels  && !MesquiteThread.isScripting()) {
					oldData = observedStates.getParentData();
					if (AlertDialog.query(containerOfModule(), "Assign models?", "There are currently no probability models assigned to the characters.  Do you want to assign a model to all characters unassigned?")) {
						((CurrentProbModels)modelTask).chooseAndFillUnassignedCharacters(observedStates.getParentData());
						model = modelTask.getCharacterModel(observedStates);
					}
					else {
					}
				}
				else {
					if (resultString!=null)
						resultString.setValue("Likelihood calculations cannot be accomplished because no probabilistic model of evolution is available for the character");
					discreetAlert( "Sorry, there is no probabilistic model of evolution available for the character; Char. likelihood cannot be calculated.");
				}
			}
			warned = true;
		}


		if (reconstructTask == null || !reconstructTask.compatibleWithContext(model, observedStates)) { 
			reconstructTask = null;
			for (int i = 0; i<getNumberOfEmployees() && reconstructTask==null; i++) {
				Object e=getEmployeeVector().elementAt(i);
				if (e instanceof MargLikelihoodForModel)
					if (((MargLikelihoodForModel)e).compatibleWithContext(model, observedStates))
						reconstructTask=(MargLikelihoodForModel)e;
			}
		}
		if (reconstructTask != null) {
			reconstructTask.calculateLogProbability( tree,  observedStates, model, resultString, likelihood);
			result.setValue(likelihood);
			result.copyAuxiliaries(likelihood.getAuxiliaries());
			result.setName("-lnLikelihood");
		}
		else {
			String s = "Likelihood calculations cannot be performed because no module was found to perform the calculations for the probability model \"" + model.getName() + "\" with the characters specified.";

			if (!warnedNoCalc) {
				discreetAlert( s);
				warnedNoCalc = true;
			}
			if (resultString!=null)
				resultString.setValue(s);
		}
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	public boolean returnsMultipleValues(){
		return true;
	}

	/*.................................................................................................................*/
	public String getVeryShortName() {
		return "Char. -ln Likelihood";
	}
	/*.................................................................................................................*/
	public boolean showCitation() {
		return true;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return false;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Character Likelihood";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Calculates the negative log likelihood of a tree for a given character." ;
	}

}


