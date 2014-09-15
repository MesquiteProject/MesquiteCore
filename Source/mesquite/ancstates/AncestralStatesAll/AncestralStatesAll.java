/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.ancstates.AncestralStatesAll;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class AncestralStatesAll extends CharsStatesForNodes {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e2 = registerEmployeeNeed(CharStatesForNodes.class, getName() + " needs a module to calculate ancestral state reconstructions.",
		"The reconstruction method is chosen initially or using the Reconstruction Method submenu");
	}
	CharStatesForNodes assignTask;
	MCharactersHistory resultStates;
	MesquiteString assignTaskName;
	MesquiteCommand atC;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		assignTask = (CharStatesForNodes)hireEmployee(CharStatesForNodes.class, "Reconstruction method");
		if (assignTask == null){
 			return sorry(getName() + " couldn't start because no reconstruction module was obtained");
 		}
		atC = makeCommand("setMethod",  this);
		assignTask.setHiringCommand(atC);

		assignTaskName = new MesquiteString(assignTask.getName());
		if (numModulesAvailable(CharStatesForNodes.class)>1){
			MesquiteSubmenuSpec mss = addSubmenu(null, "Reconstruction Method", atC, CharStatesForNodes.class);
			mss.setSelected(assignTaskName);
		}
  		return true;
 	}
 	
  	 public void employeeQuit(MesquiteModule m){
  	 	iQuit();
  	 }
	  	 public Snapshot getSnapshot(MesquiteFile file) {
   	 	Snapshot temp = new Snapshot();
  	 	temp.addLine("setMethod ",assignTask);
  	 	return temp;
  	 }
	public boolean allowsStateWeightChoice(){
		return false;
	}
/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Sets module used to reconstruct ancestral states", "[name of module]", commandName, "setMethod")) {
    	 		CharStatesForNodes temp=  (CharStatesForNodes)replaceEmployee(CharStatesForNodes.class, arguments, "Reconstruction method", assignTask);
 			if (temp!=null) {
 				assignTask= temp;
				assignTask.setHiringCommand(atC);
				assignTaskName.setValue(assignTask.getName());
				parametersChanged();
 			}
 			return assignTask;
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
   	 }
	/*...............................................................................................................*/
	public MCharactersHistory calculateStates(Tree tree, MCharactersDistribution observedStates, MCharactersHistory resultStates, MesquiteString resultString){
		if (observedStates==null || tree == null)
			return null;
		resultStates=observedStates.adjustHistorySize(tree, resultStates);
	     	CharacterDistribution oneObserved = observedStates.getCharacterDistribution(0);
		CharacterHistory oneReconstructed = oneObserved.adjustHistorySize(tree, null);
		if (assignTask!=null) {
     	 		for (int ic=0; ic<observedStates.getNumChars(); ic++) {
	     	 		oneObserved = observedStates.getCharacterDistribution(ic);
				assignTask.calculateStates(tree, oneObserved, oneReconstructed, null);
				resultStates.transferFrom(ic, oneReconstructed);
 			}
 		}
 		return resultStates;
	}
   
	/*.................................................................................................................*/
    	 public String getName() {
		return "Ancestral States All";
   	 }
	/*.................................................................................................................*/
   	 public boolean isSubstantive(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
	/*.................................................................................................................*/
   	 public boolean showCitation(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
    	 public String getNameAndParameters() {
		if (assignTask == null)
			return getName();
		return "Reconstructed Ancestral States (" + assignTask.getName() + ")";
   	 }
	/*.................................................................................................................*/
   	 
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Reconstructs ancestral states at nodes for all characters supplied by character source." ;
   	 }
	/*.................................................................................................................*/
 	/** returns current parameters, for logging etc..*/
 	public String getParameters() {
 		if (assignTask == null)
 			return "";
		return "Reconstructed Ancestral States (" + assignTask.getName() + ")";
   	 }
   	 
}


