/* Mesquite source code (Genesis package).  Copyright 2001 and onward, D. Maddison and W. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.genesis.SimDNAModelCurator;
/*~~  */

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import mesquite.genesis.lib.*;
import mesquite.stochchar.lib.*;
/* ======================================================================== */
public class SimDNAModelCurator extends CategProbModelCurator implements EditingCurator {
	SimulationDNAModel modelToEdit;
		 SimulationDNAModel defaultModel;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		defaultModel = new SimulationDNAModel("Jukes-Cantor", DNAState.class, this);
		defaultModel.setRateMatrixModel(new RateMatrixEqualDNAModel(defaultModel));
		defaultModel.setRootStatesModel(new StateFreqDNAEqual(defaultModel));
		defaultModel.setEquilStatesModel(new StateFreqDNAEqual(defaultModel));
		defaultModel.setCharRatesModel(new CharRatesEqual());
    		defaultModel.setBuiltIn(true);
		DNAData.registerDefaultModel("Likelihood", defaultModel.getName());


		return true;
  	 }
	/*.................................................................................................................*/
  	 public void projectEstablished(){
    		defaultModel.addToFile(null, getProject(), null);
    		super.projectEstablished();
  	 }
   
  	 public Snapshot getSnapshot(MesquiteFile file) {
   	 	if (getModuleWindow() ==null || !getModuleWindow().isVisible())
   	 		return null;
   	 	Snapshot temp = new Snapshot();
   	 	temp.addLine("editModel " + ParseUtil.tokenize(modelToEdit.getName()));
  	 	temp.addLine("getWindow");
  	 	temp.addLine("tell It");
	  	temp.incorporate(getModuleWindow().getSnapshot(file), true);
  	 	temp.addLine("endTell");
  	 	temp.addLine("showWindow");
  	 	
  	 	return temp;
  	 }
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Edits the character model", "[name of character model]", commandName, "editModel")) {
			CharacterModel model = getProject().getCharacterModel(parser.getFirstToken(arguments));
			if (model !=null && model instanceof SimulationDNAModel) {
				showEditor(model);
			}
			return model;
		}
    	 	else
 			return  super.doCommand(commandName, arguments, checker);
		//return null;
   	 }
	/*.................................................................................................................*/
   	public MesquiteModule showEditor(CharacterModel model){
		//may not want to do this every request, but in response to a button push in curator window
		
   		modelToEdit =  ((SimulationDNAModel)model);
   		
 		modelToEdit.initialize("Edit model: \"" + model.getName() + "\"");
 		return this;
  	}
 	public void windowGoAway(MesquiteWindow whichWindow) {
 			destroyMenu();
			super.windowGoAway(whichWindow);
	}
	public boolean curatesModelClass(Class modelClass){
		return SimulationDNAModel.class.isAssignableFrom(modelClass);
	}
	/*.................................................................................................................*/
	public String getNameOfModelClass() {
		return "Composite DNA Simulation Model";
	}
	/*.................................................................................................................*/
	public String getNEXUSNameOfModelClass() {
		return "DNASimulationModel";
	}
	/*.................................................................................................................*/
	public Class getModelClass() {
		return SimulationDNAModel.class;
	}
	/*.................................................................................................................*/
   	public CharacterModel makeNewModel(String name) {
 		SimulationDNAModel model = new SimulationDNAModel(name, DNAState.class, this);
 		model.setProject(getProject());
       		return model;
   	}
	/*.................................................................................................................*/
   	public CharacterModel readCharacterModel(String name, MesquiteInteger stringPos, String description, int format) {
 		SimulationDNAModel model = new SimulationDNAModel( name, DNAState.class,this);
 		model.setProject(getProject());
   		model.fromString(description, stringPos, format);
  		return model;
   	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Composite DNA Simulation Model";
   	 }
	/*.................................................................................................................*/
    	 public String getNameForMenuItem() {
		return "Composite DNA Simulation Model...";
   	 }
   	 
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Defines and maintains simple stochastic models of DNA evolution for simulations." ;
   	 }
}


