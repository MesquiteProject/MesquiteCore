/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.parsimony.ParsAncestralStates;
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.categ.lib.CategoricalHistory;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.parsimony.lib.*;

/* ======================================================================== */
public class ParsAncestralStates extends CharStatesForNodes {
	public String getName() {
		return "Parsimony Ancestral States";
	}
	public String getExplanation() {
		return "Coordinates the parsimony reconstruction of ancestral states." ;
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed

		EmployeeNeed e = registerEmployeeNeed(ParsAncStatesForModel.class, getName() + " uses various parsimony calculators to handle different assumptions (unordered, ordered, etc.).",
				"The parsimony calculator used depends on the parsimony model assigned to the character, " + 
		"for instance the model assigned in the List of Characters window (parsimony model column), which is called the Stored Parsimony Model for the character.");
		//e.setAsEntryPoint(true);
		EmployeeNeed e2 = registerEmployeeNeed(ParsModelSource.class, getName() + " needs parsimony models to use as assumptions for calculations.",
				"You can request the source of parsimony models when " + getName() + " starts, or later under the submenu \"Source of parsimony models\".");
		//e.setAsEntryPoint(true);

	}
	/*.................................................................................................................*/
//	CharacterDistribution observedStates;
	ParsAncStatesForModel reconstructTask;
	CharacterModelSource modelTask;
	CharacterModel model = null;
	boolean oneCharAtTime = false;
	CharacterHistory statesAtNodes;
	MesquiteNumber steps;
	int oldNumTaxa;
	MesquiteString modelTaskName;
	boolean firstWarning = true;
	MesquiteBoolean useMPRsMode;
	MesquiteCMenuItemSpec mprsModeItem;
	MesquiteMenuItemSpec mprNumberItem;
	boolean mprsPermissive = false;

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		hireAllEmployees(ParsAncStatesForModel.class);
		modelTask = (ParsModelSource)hireNamedEmployee(ParsModelSource.class, "#CurrentParsModels");
		if (modelTask == null)
			modelTask = (ParsModelSource)hireEmployee(ParsModelSource.class, "Source of parsimony character models");
		if (modelTask == null)
			return sorry(getName() + " couldn't start because no source of models of character evolution found.");
		steps = new MesquiteNumber();
		modelTaskName = new MesquiteString(modelTask.getName());
		MesquiteSubmenuSpec mss = addSubmenu(null, "Source of parsimony models", makeCommand("setModelSource", this), ParsModelSource.class);
		useMPRsMode = new MesquiteBoolean(false);
		resetMode();
		mss.setSelected(modelTaskName);
		return true;  
	}
	public  void prepareForMappings(boolean permissiveOfNoSeparateMappings) {
		useMPRsMode.setValue(true);
		mprsPermissive = permissiveOfNoSeparateMappings;
		resetMode();

	}
	public String getMappingTypeName(){
		return "MPR";
	}

	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("setModelSource ",modelTask);
		temp.addLine("toggleMPRsMode " + useMPRsMode.toOffOnString());
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets module used to supply character models", "[name of module]", commandName, "setModelSource")) {
			ParsModelSource temp=  (ParsModelSource)replaceEmployee(ParsModelSource.class, arguments, "Source of parsimony character models", modelTask);
			if (temp!=null) {
				modelTask= temp;
				incrementMenuResetSuppression();
				modelTaskName.setValue(modelTask.getName());
				modelTask.setOneCharacterAtATime(oneCharAtTime);
				parametersChanged();
				decrementMenuResetSuppression();
			}
			return modelTask;
		}
		else if (checker.compare(this.getClass(), "Sets whether or not to use MPRs Mode", "[on; off]", commandName, "toggleMPRsMode")) {
			useMPRsMode.toggleValue(parser.getFirstToken(arguments));
			resetMode();
			parametersChanged();
		}
		else if (checker.compare(this.getClass(), "Shows the number of MPRs for the character", "", commandName, "calcNumMPRs")) {
			boolean oldMode = useMPRsMode.getValue();
			useMPRsMode.setValue(true);
			resetMode();
			parametersChanged();
			long numMPRs = getNumberOfMappings();
			MesquiteMessage.discreetNotifyUser("Number of MPRs: " + numMPRs);
			useMPRsMode.setValue(oldMode);
			resetMode();
			parametersChanged();
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	public void resetMode(){
		for (int i = 0; i<getNumberOfEmployees(); i++) {
			Object e=getEmployeeVector().elementAt(i);
			if (e instanceof ParsAncStatesForModel)
				((ParsAncStatesForModel)e).setCalcConditionalMPRSets(useMPRsMode.getValue());
		}

	}
	public void employeeQuit(MesquiteModule m){
		if (m == modelTask)
			iQuit();
	}
	public boolean allowsStateWeightChoice(){
		return false;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	public void setOneCharacterAtATime(boolean chgbl){
		oneCharAtTime = chgbl;
		modelTask.setOneCharacterAtATime(chgbl);
		if (chgbl){
			if (mprsModeItem == null) {
				mprsModeItem = addCheckMenuItem(null, "MPRs Mode", makeCommand("toggleMPRsMode", this), useMPRsMode);
				mprNumberItem= addMenuItem( "Number of MPRs...", makeCommand("calcNumMPRs",  this));

			}
		}
		else {
			if (mprsModeItem != null)
				deleteMenuItem(mprsModeItem);
			if (mprNumberItem != null)
				deleteMenuItem(mprNumberItem);
		}

	}





	/*.................................................................................................................*/
	public  boolean calculateStates(Tree tree, CharacterDistribution observedStates, CharacterHistory resultStates, MesquiteString resultString) {  
		this.observedStates = observedStates;
		if (resultString!=null)
			resultString.setValue("");
		if (tree==null)
			return false; 
		else if (observedStates==null)
			return false; 
		else if (resultStates==null)
			return false; 

		//
		model = modelTask.getCharacterModel(observedStates);
		if (model==null) {
			String mes = "Parsimony calculations could not be completed; no model is available for character: " + observedStates.getName();
			discreetAlert( mes);
			statesAtNodes.deassignStates();
			if (resultString!=null)
				resultString.setValue(mes);
			return false;
		}


		//finding a module that can do parsimony calculations for this model 
		if (reconstructTask == null || !reconstructTask.compatibleWithContext(model, observedStates)) {
			reconstructTask = null;
			for (int i = 0; i<getNumberOfEmployees() && reconstructTask==null; i++) {
				Object e=getEmployeeVector().elementAt(i);
				if (e instanceof ParsAncStatesForModel)
					if (((ParsAncStatesForModel)e).compatibleWithContext(model, observedStates)) {
						reconstructTask=(ParsAncStatesForModel)e;
					}
			}
		}
		boolean success = false;
		resultStates.deassignStates();
		if (reconstructTask != null) {
			reconstructTask.calculateStates( tree,  observedStates,  resultStates, model, resultString, steps);
			success = true;
		}
		else {
			if (resultString!=null)
				resultString.setValue("Ancestral states unassigned.  No compatible module found for model: " + model.getName());
			if (firstWarning && !MesquiteThread.isScripting()) {
				String s ="Parsimony ancestral states: NO COMPATIBLE RECONSTRUCTOR FOUND for a character " + observedStates.getName() + " (model = ";
				s += model.getName() + ") ";
				alert(s);
			}
			if (!MesquiteThread.isScripting())
				firstWarning = false;
			
		}
		statesAtNodes=resultStates;
		((CharacterStates)statesAtNodes).setExplanation("Most parsimonious state(s)");
		if (reconstructTask !=null && reconstructTask.calculatingConditionalMPRSets()){
			numMappings = statesAtNodes.getNumResolutions(tree);
			mprsAvailable = true;
		}
		else {
			numMappings =1;
			mprsAvailable = false;
		}
		return success;
	}
	/*.................................................................................................................*/
	long numMappings = 1;
	boolean mprsAvailable = false;
	MesquiteString prevResult = new MesquiteString();
	public  void setObservedStates(Tree tree, CharacterDistribution observedStates){
		this.tree = tree;
		this.observedStates = observedStates;
		statesAtNodes = observedStates.adjustHistorySize(tree, statesAtNodes);
		calculateStates(tree, observedStates, statesAtNodes, prevResult);

	}
	public  boolean getMapping(long i, CharacterHistory resultStates, MesquiteString resultString){
		if (tree == null || observedStates == null)
			return false;

		if (useMPRsMode.getValue() && !mprsAvailable && !mprsPermissive){
			statesAtNodes.deassignStates();
			if (resultString != null)
				resultString.setValue("MPRs mode not available for this character type");
			return false;
		}
		else {
			if (!useMPRsMode.getValue() || !mprsAvailable)
				statesAtNodes.clone(resultStates);
			else
				resultStates = statesAtNodes.getResolution(tree, resultStates, i);
			if (resultString != null)
				resultString.setValue(prevResult);
			return resultStates!=null;
		}
	}	



	public  long getNumberOfMappings(){
		if (useMPRsMode.getValue() && mprsAvailable)
			return numMappings;
		return 1;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return false;
	}
	public boolean showCitation(){
		return true;
	}

}


