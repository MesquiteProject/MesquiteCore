/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.stochchar.MargProbAncStates;
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
public class MargProbAncStates extends CharStatesForNodes {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(ProbModelSourceLike.class, getName() + " needs an indication of what probabilistic model to apply to the character.",
		"The indicator of probabilistic models can be selected initially");
		EmployeeNeed e2 = registerEmployeeNeed(MargLikeAncStForModel.class, getName() + "  needs methods to calculate likelihoods.",
		"The methods to calculate likelihoods are chosen automatically according to the probability model used in the calculation");
	}
	/*.................................................................................................................*/
	CharacterDistribution observedStates;
	MargLikeAncStForModel reconstructTask;
	ProbModelSourceLike modelTask;
	MesquiteString modelTaskName;
	CharacterModel model;
	MesquiteNumber likelihood;
	boolean warnedNoCalc = false;
	CharacterHistory statesAtNodes;
	int oldNumTaxa;
	boolean oneAtATime = false;
	boolean oneAtATimeCHGBL = false;
	
	public String getMappingTypeName(){
		return "Realization";
	}
	/*  following is for showing likelihood surfaces; not yet working
	double[] inputBounds, outputBounds;
	MatrixCharter surfaceTask;
	MesquiteCMenuItemSpec showSurfaceItem=null;
	MesquiteBoolean showSurface;
	*/
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		hireAllEmployees(MargLikeAncStForModel.class);
 		if (modelTask == null)
 			modelTask = (ProbModelSourceLike)hireEmployee(ProbModelSourceLike.class, "Source of probability character models (for likelihood calculations)");
 		if (modelTask == null)
 			return sorry(getName() + " couldn't start because no source of models of character evolution obtained.");
 		modelTaskName = new MesquiteString(modelTask.getName());
 		likelihood = new MesquiteNumber();
		MesquiteSubmenuSpec mss = addSubmenu(null, "Source of probability models", makeCommand("setModelSource", this), ProbModelSourceLike.class);
		mss.setCompatibilityCheck(new ModelCompatibilityInfo(ProbabilityCategCharModel.class, null));
		mss.setSelected(modelTaskName);
		//showSurface = new MesquiteBoolean(false); this is for showing likelihood surfaces; not yet working
 		return true;
 	}

 	
   	public void setOneCharacterAtATime(boolean chgbl){
		oneAtATimeCHGBL = chgbl;
		oneAtATime = true;
   		modelTask.setOneCharacterAtATime(chgbl);
		/* this is for showing likelihood surfaces; not yet working
		if (oneAtATime){
			if (showSurfaceItem==null) {
				showSurfaceItem = addCheckMenuItem(null, "Show Likelihood Surface", makeCommand("showSurface", this), showSurface);
 				resetContainingMenuBar();
 			}
		}
		else if (showSurfaceItem !=null){
			deleteMenuItem(showSurfaceItem);
			showSurfaceItem = null;
		}
		*/
   	}

	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
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
 				incrementMenuResetSuppression();
 				modelTaskName.setValue(modelTask.getName());
 				if (oneAtATime)
 					modelTask.setOneCharacterAtATime(oneAtATimeCHGBL);
				parametersChanged();
 				decrementMenuResetSuppression();
 			}
 			return modelTask;
    	 	}
    	 	/*  this is for showing likelihood surfaces; not yet working
    	 	else if (checker.compare(this.getClass(), "Shows likelihood surface", null, commandName, "showSurface")) {
 			surfaceTask = (MatrixCharter)hireEmployee(MatrixCharter.class, "Display method for likelihood surface");
 			outputBounds = new double[4];
 			return surfaceTask;
    	 	}
    	 	*/
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
   	 }
 	
	/*.................................................................................................................*/
 	public void employeeQuit(MesquiteModule m){
 		if (m == modelTask)
 			iQuit();
 	}
	public boolean allowsStateWeightChoice(){
		return true;
	}
 	boolean warned = false;
 	CharacterData oldData = null;
	/*.................................................................................................................*/
	public  boolean calculateStates(Tree tree, CharacterDistribution observedStates, CharacterHistory resultStates, MesquiteString resultString) {  
		this.observedStates = observedStates;
		if (tree==null || observedStates==null || resultStates == null)
			return false;
		resultStates.deassignStates();
		likelihood.setToUnassigned();
		
		//a barrier (temporary) while likelihood calculations support only simple categorical
		Class stateClass = observedStates.getStateClass();
		if (DNAState.class.isAssignableFrom(stateClass) || ProteinState.class.isAssignableFrom(stateClass) || ContinuousState.class.isAssignableFrom(stateClass)) {
			String s = "Likelihood calculations cannot be performed ";
			if (DNAState.class.isAssignableFrom(stateClass))
				s += "currently with DNA or RNA data.  The calculations were not done for some characters.";
			else if (ProteinState.class.isAssignableFrom(stateClass))
				s += "currently with protein data.  The calculations were not done for some characters.";
			else if (ContinuousState.class.isAssignableFrom(stateClass))
				s += "currently with continuous valued data.  The calculations were not done for some characters.";
			if (!warnedNoCalc) {
				discreetAlert( s);
				warnedNoCalc = true;
			}
			if (resultString!=null)
				resultString.setValue(s);
			return false;
		}


		boolean success = false;
		model = modelTask.getCharacterModel(observedStates);
		if (model == null && !MesquiteThread.isScripting()){
			if (observedStates.getParentData()!= oldData)
				warned = false;
			if (!warned){
				if (observedStates.getParentData()!=null && modelTask instanceof CurrentProbModels) {
					oldData = observedStates.getParentData();
					if (AlertDialog.query(containerOfModule(), "Assign models?", "There are currently no probability models assigned to the characters.  Do you want to assign a model to all characters unassigned?")) {
						((CurrentProbModels)modelTask).chooseAndFillUnassignedCharacters(observedStates.getParentData());
						model = modelTask.getCharacterModel(observedStates);
					}
				}
				else {
					if (resultString!=null)
						resultString.setValue("Likelihood calculations cannot be accomplished because no probabilistic model of evolution is available for the character");
					discreetAlert( "Sorry, there is no probabilistic model of evolution available for the character; likelihood calculations cannot be accomplished.  Please make sure that the source of models chosen is compatible with this character type.");
					warned = true;
					return false;
				}
			}
			warned = true;
		}
		
		if (reconstructTask == null || !reconstructTask.compatibleWithContext(model, observedStates)) { 
			reconstructTask = null;
			for (int i = 0; i<getNumberOfEmployees() && reconstructTask==null; i++) {
				Object e=getEmployeeVector().elementAt(i);
				if (e instanceof MargLikeAncStForModel)
					if (((MargLikeAncStForModel)e).compatibleWithContext(model, observedStates)) {
						reconstructTask=(MargLikeAncStForModel)e;
					}
			}
		}
		if (reconstructTask != null) { 
			reconstructTask.calculateStates( tree,  observedStates,  resultStates, model, resultString, likelihood);
			/*   this is for showing likelihood surfaces; not yet working
			if (oneAtATime && surfaceTask!=null && !((ProbabilityModel)model).isFullySpecified() && showSurface.getValue()){
				Object surface = reconstructTask.getLikelihoodSurface(tree,  observedStates, model, inputBounds, outputBounds);
				surfaceTask.setMatrix((double[][])surface, outputBounds[0], outputBounds[1], outputBounds[2], outputBounds[3]);
			}
			*/
			if (resultString!=null)
				resultString.append(" Calc. by " + reconstructTask);
			success=true;
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
		statesAtNodes=resultStates;
		return success;
	}
   
	/*.................................................................................................................*/
    	 public String getName() {
		return "Likelihood Ancestral States";
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
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Coordinates reconstruction of ancestral states by maximum likelihood.  Currently each node is estimated independently (i.e., corresponding to PAUP's marginal reconstruction)." ;
   	 }
   	 
}


