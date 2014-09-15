/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.parsimony.ParsCharSteps;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.parsimony.lib.*;

/* ======================================================================== */
public class ParsCharSteps extends CharacterSteps {
	public String getName() {
		return "Parsimony Character Steps";
	}
	public String getExplanation() {
		return "Calculates the number of parsimony steps in a character." ;
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
	CharacterDistribution observedStates;
	MesquiteNumber steps;
	CharacterModelSource modelTask;
	MesquiteString modelTaskName;
	boolean firstWarning = true;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		hireAllEmployees(ParsAncStatesForModel.class);
		modelTask = (ParsModelSource)hireNamedEmployee(ParsModelSource.class, "#CurrentParsModels");
		if (modelTask == null)
			modelTask = (ParsModelSource)hireEmployee(ParsModelSource.class, "Source of parsimony character models");
		if (modelTask == null)
			return sorry(getName() + " couldn't start because no source of models of character evolution obtained.");
		modelTaskName = new MesquiteString(modelTask.getName());
		MesquiteSubmenuSpec mss = addSubmenu(null, "Source of parsimony models", makeCommand("setModelSource", this), ParsModelSource.class);
		mss.setSelected(modelTaskName);
		steps = new MesquiteNumber();
		return true; //false if no appropriate employees!
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	public void employeeQuit(MesquiteModule m){
		if (m==modelTask)
			iQuit();
	}

	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("setModelSource ",modelTask);
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
					parametersChanged();
				decrementMenuResetSuppression();
			}
			return modelTask;
		}
		else
			return  super.doCommand(commandName, arguments, checker);
	}


	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Tree tree, CharacterDistribution observedStates){
	}
	ParsAncStatesForModel reconstructTask = null;
	boolean  warned = false;
	/*.................................................................................................................*/
	/*in future this will farm out the calculation to the modules that deal with appropriate character model*/
	public  void calculateNumber(Tree tree, CharacterDistribution observedStates, MesquiteNumber result, MesquiteString resultString) {  
		if (result==null)
			return;
	   	clearResultAndLastResult(result);
		if (resultString!=null)
			resultString.setValue("");
		if (tree==null || observedStates==null) {
			steps.setToUnassigned();
			return;
		}

		this.observedStates = observedStates;

		CharacterModel model = modelTask.getCharacterModel(observedStates);

		if (model==null) {
			if (!warned){
				discreetAlert( "Sorry, there is no parsimony model of evolution available for the character; Parsimony character steps cannot be calculated");
				warned = true;
			}
			if (resultString!=null)
				resultString.setValue("Parsimony calculations could not be completed; no model available for character");
			result.setToUnassigned();
			return;
		}

		//finding a module that can do parsimony calculations for this model 
		if (reconstructTask == null || !reconstructTask.compatibleWithContext(model, observedStates)) { 
			reconstructTask = null;
			for (int i = 0; i<getNumberOfEmployees() && reconstructTask==null; i++) {
				Object e=getEmployeeVector().elementAt(i);
				if (e instanceof ParsAncStatesForModel)
					if (((ParsAncStatesForModel)e).compatibleWithContext(model, observedStates))
						reconstructTask=(ParsAncStatesForModel)e;
			}
		}
		if (reconstructTask != null) {
			reconstructTask.calculateSteps( tree,  observedStates, model, resultString, steps);
		}
		else {
			String s = "Parsimony calculations cannot be performed because no module was found to perform the calculations for the model \"" + model.getName() + "\" with the character specified: " +  observedStates.getName();
			if (firstWarning) {
				discreetAlert( s);
				firstWarning = false;
			}
			steps.setToUnassigned();
			if (resultString!=null)
				resultString.setValue("Steps unassigned because no compatible reconstructor found");
		}
		result.setValue(steps);
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	/*.................................................................................................................*/
	/** Returns the name of the module in very short form.  For use for column headings and other constrained places.  Unless overridden returns getName()*/
	public String getVeryShortName(){
		return "Steps";
	}
	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return false;
	}
	public boolean showCitation(){
		return true;
	}

}


