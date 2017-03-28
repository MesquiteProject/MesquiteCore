/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.stochchar.EvolveCategChars;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import mesquite.stochchar.lib.*;

/* ======================================================================== */
public class EvolveCategChars extends CharacterSimulator implements MesquiteListener {
	CategoricalHistory evolvingStates;
	ProbPhenCategCharModel probabilityModel, originalProbabilityModel;
	MesquiteString modelName;
	long rootStateSim = 0L;  //state set for simulations; 0 = no state, use model's prior
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (condition!=null && condition!= CategoricalData.class && condition!=CategoricalState.class){
  	 		return sorry("Evolve Categorical cannot be used because it supplies only categorical characters");
		}
        loadPreferences();
		ProbPhenCategCharModel m = null;
		m = chooseModel();
		if (m==null) {
			return sorry("Evolve Categorical Characters could not start because no appropriate stochastic model of character evolution was found for use to simulate categorical character evolution");
		}
		setModel(m,  false); 
		
		MesquiteSubmenuSpec mss = addSubmenu(null, "Model (for simulation)", makeCommand("setModelByNumber", this), getProject().getCharacterModels());
		mss.setCompatibilityCheck(new SimModelCompatInfo(ProbPhenCategCharModel.class, null));
		modelName = new MesquiteString(m.getName());
		mss.setSelected(modelName);
		getProject().getCentralModelListener().addListener(this);
		return true;
  	 }
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer();
		StringUtil.appendXMLTag(buffer, 2, "rootStateSim", (int)rootStateSim);   
		return buffer.toString();
	}

	public void processSingleXMLPreference (String tag, String content) {
		if ("rootStateSim".equalsIgnoreCase(tag))
			rootStateSim = MesquiteLong.fromString(content);
	}
 	 
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
  	private ProbPhenCategCharModel chooseModel(){
		if (MesquiteThread.isScripting())
			return (ProbPhenCategCharModel)getProject().getCharacterModel(new SimModelCompatInfo(ProbPhenCategCharModel.class, CategoricalState.class),0);
		else
			return (ProbPhenCategCharModel)CharacterModel.chooseExistingCharacterModel(this, new SimModelCompatInfo(ProbPhenCategCharModel.class, CategoricalState.class), "Choose probability model for simulations");
  	}
	/*.................................................................................................................*/
	public void endJob() {
		storePreferences();
		getProject().getCentralModelListener().removeListener(this);
		super.endJob();
  	 }
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public void disposing(Object obj){
		if (obj == originalProbabilityModel) {
			setModel(chooseModel(), false);
			parametersChanged();
		}
	}
	/*.................................................................................................................*/
  	 private void setModel(ProbPhenCategCharModel m,  boolean notify){
  	 	originalProbabilityModel = m;
  	 	if (probabilityModel !=null)
  	 		probabilityModel.dispose();
    	 	if (m ==null)
    	 		probabilityModel = null;
    	 	else {
    	 		probabilityModel = (ProbPhenCategCharModel)m.cloneModelWithMotherLink(probabilityModel); //use a copy in case fiddled with
    	 		probabilityModel.setUserVisible(false);
   	 	}
  	 	if (notify)
  	 		parametersChanged();
  	 }
	/*.................................................................................................................*/
	public void changed(Object caller, Object obj, Notification notification){
		if (obj == originalProbabilityModel) {
			setModel((ProbPhenCategCharModel)obj,  true);
		}
	}
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) {
   	 	Snapshot temp = new Snapshot();
  	 	temp.addLine("setModelByName " + StringUtil.tokenize(probabilityModel.getName()));
  	 	return temp;
  	 }
  	 MesquiteInteger pos = new MesquiteInteger(0);
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Sets character model for simulation", "[name of model]", commandName, "setModelByName")) {
    	 		pos.setValue(0);
    	 		CharacterModel m = getProject().getCharacterModel(ParseUtil.getFirstToken(arguments, pos));
    	 		if (m!=null && m instanceof ProbPhenCategCharModel) {
    	 			setModel((ProbPhenCategCharModel)m,  true);
    	 			modelName.setValue(m.getName());
    	 			return m;
    	 		}
    	 	}
    	 	else if (checker.compare(this.getClass(), "Queries use for model for simulation", null, commandName, "userQueryModel")) {
			ProbPhenCategCharModel m =(ProbPhenCategCharModel)CharacterModel.chooseExistingCharacterModel(this, ProbPhenCategCharModel.class, "Choose probability model for simulations");
			if (m!=null)
				setModel(m,  true); 
			return m;
    	 	}
    	 	else if (checker.compare(this.getClass(), "Sets character model for simulation", "[name of model]", commandName, "setModelByNumber")) {
    	 		pos.setValue(0);
      			int whichModel = MesquiteInteger.fromFirstToken(arguments, pos);
      			if (!MesquiteInteger.isCombinable(whichModel))
      				return null;
 			CharacterModel m = getProject().getCharacterModel(new SimModelCompatInfo(ProbPhenCategCharModel.class, null), whichModel);
    	 		if (m!=null && m instanceof ProbPhenCategCharModel) {
    	 			setModel((ProbPhenCategCharModel)m,  true);
    	 			modelName.setValue(m.getName());
    	 			return m;
    	 		}
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
		return null;
   	 }
   	 CharacterState statesAtAncestor=null;
   	 CharacterState endStates=null;
 	/*.................................................................................................................*/
	private void evolve(Tree tree, CategoricalAdjustable statesAtTips, int node) {
   		if (node!=tree.getRoot()) {
   			long statesAtAncestor = evolvingStates.getState(tree.motherOfNode(node));
   			int stateAtAncestor = CategoricalState.minimum(statesAtAncestor);
   			long statesAtNode = CategoricalState.makeSet(probabilityModel.evolveState(stateAtAncestor, tree, node));
   			evolvingStates.setState(node, statesAtNode);  
   			if (tree.nodeIsTerminal(node)) {
   				statesAtTips.setState(tree.taxonNumberOfNode(node), statesAtNode);
   			}
   		}
		for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter))
				evolve(tree, statesAtTips, daughter);
	}
 	/*.................................................................................................................*/
	private void evolve (Tree tree, CategoricalHistory statesAtNodes, int node) {
   		if (node!=tree.getRoot()) {
   			long statesAtAncestor = statesAtNodes.getState(tree.motherOfNode(node));
   			int stateAtAncestor = CategoricalState.minimum(statesAtAncestor);
   			int stateAtNode = probabilityModel.evolveState(stateAtAncestor, tree, node);
   			long statesAtNode = CategoricalState.makeSet(stateAtNode);
   			statesAtNodes.setState(node, statesAtNode);
   		}
		for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter))
				evolve(tree, (CategoricalHistory)statesAtNodes, daughter);
	}
	boolean failed = false;
	int warnedNoModel = 0;
	/*.................................................................................................................*/
   	public CharacterDistribution getSimulatedCharacter(CharacterDistribution statesAtTips, Tree tree, MesquiteLong seed, int ic){
   		if (tree == null) {
   			//MesquiteMessage.warnProgrammer("tree null in Evolve categorical chars simple ");
   			return null;
   		}
   		else if (statesAtTips == null || !(statesAtTips instanceof CategoricalAdjustable)){
   			statesAtTips =  new CategoricalAdjustable(tree.getTaxa(), tree.getTaxa().getNumTaxa());
   		}
   		else	
   			statesAtTips = (CharacterDistribution)((AdjustableDistribution)statesAtTips).adjustSize(tree.getTaxa());
		if (statesAtTips instanceof CategoricalAdjustable)
			((CategoricalAdjustable)statesAtTips).deassignStates();
		if (probabilityModel == null){
			if (warnedNoModel++<10) {
				discreetAlert( "Sorry, the simulation cannot continue because no model of character evolution has been specified.");
				if (!MesquiteThread.isScripting())
					warnedNoModel = 10;
			}
			return statesAtTips;
   		}	
		
		probabilityModel.setMaxStateSimulatable(probabilityModel.getMaxStateDefined()); //should allow user to set # states
   		if (!probabilityModel.isFullySpecified()) {
   			if (failed) {
   				return statesAtTips;
   			}	
   			CharModelCurator mb = findModelCurator(probabilityModel.getClass());
   			mb.showEditor(probabilityModel);  //MODAL
   			if (!probabilityModel.isFullySpecified()) {
   				discreetAlert( "Sorry, the simulation cannot continue because the model of character evolution ("  + probabilityModel.getName() + ") doesn't have all of its parameters specified.");
   				failed = true;
   				return statesAtTips;
   			}
   		}
   		failed = false;
   		evolvingStates =(CategoricalHistory) statesAtTips.adjustHistorySize(tree, evolvingStates);
   		if (seed!=null)
   			probabilityModel.setSeed(seed.getValue());
   		long rootstate = probabilityModel.getRootState(tree);
   		if (rootStateSim != 0L)
   			rootstate = rootStateSim;
   		evolvingStates.setState(tree.getRoot(), rootstate);  //starting rootCategoricalState.makeSet(0)); //
   		evolve(tree, (CategoricalAdjustable)statesAtTips, tree.getRoot());
   		if (seed!=null)
   			seed.setValue(probabilityModel.getSeed());
  		return statesAtTips; 
   	}
  
	/*............................................. ....................................................................*/
   	public CharacterHistory getSimulatedHistory(CharacterHistory statesAtNodes, Tree tree, MesquiteLong seed){
   		if (tree == null) {
   			//MesquiteMessage.warnProgrammer("tree null in Evolve continuous chars simple ");
   			return null;
   		}
   		else if (statesAtNodes == null || !(statesAtNodes instanceof CategoricalHistory)){
   			statesAtNodes =  new CategoricalHistory(tree.getTaxa(), tree.getNumNodeSpaces());
   		}
   		else	
   			statesAtNodes = statesAtNodes.adjustSize(tree);

		if (probabilityModel == null){
			if (statesAtNodes instanceof CategoricalAdjustable)
				((CategoricalAdjustable)statesAtNodes).deassignStates();
			if (warnedNoModel++<10) {
				discreetAlert( "Sorry, the simulation cannot continue because no model of character evolution has been specified.");
				if (!MesquiteThread.isScripting())
					warnedNoModel = 10;
			}
			return statesAtNodes;
   		}	
		probabilityModel.setMaxStateSimulatable(probabilityModel.getMaxStateDefined()); //should allow user to set # states
   		if (!probabilityModel.isFullySpecified()) {
   			if (failed) {
   				statesAtNodes.deassignStates();
   				return statesAtNodes;
   			}	
   			CharModelCurator mb = findModelCurator(probabilityModel.getClass());
   			mb.showEditor(probabilityModel); //MODAL???
   			if (!probabilityModel.isFullySpecified()) {
   				alert("Sorry, the simulation cannot continue because the model of character evolution ("  + probabilityModel.getName() + ") doesn't have all of its parameters specified.");
   				statesAtNodes.deassignStates();
   				failed = true;
   				return statesAtNodes;
   			}
   		}
   		failed = false;
   		if (seed!=null)
   			probabilityModel.setSeed(seed.getValue());
   		long rootstate = probabilityModel.getRootState(tree);

  		((CategoricalHistory)statesAtNodes).setState(tree.getRoot(), rootstate);  
   		evolve(tree, (CategoricalHistory)statesAtNodes, tree.getRoot());
   		if (seed!=null)
   			seed.setValue(probabilityModel.getSeed());
  		return statesAtNodes;
   	}
	/*.................................................................................................................*/
	/** Indicates the type of character created */ 
	public Class getStateClass(){
		return CategoricalState.class;
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Evolve Categorical Characters";
   	 }
   	 
	/*.................................................................................................................*/
    	 public String getParameters() {
		if (probabilityModel == null)
			return "Model not available or specified.";
		else
			return "Markovian evolution using model: " + probabilityModel.getName() + " (" + probabilityModel.getParameters() + ")";
   	 }
	/*.................................................................................................................*/
   	 
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Simulates character evolution for categorical characters on a tree." ;
   	 }
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
  	 	return false;
  	 }
	/*.................................................................................................................*/
    	 public boolean showCitation() {
		return true;
   	 }
	/*.................................................................................................................*/
  	 public CompatibilityTest getCompatibilityTest() {
  	 	return new ECCCategoricalStateTest();
  	 }
}

class ECCCategoricalStateTest extends OffersCategoricalData{
	ModelCompatibilityInfo mci;
	public ECCCategoricalStateTest (){
		super();
		mci = new ModelCompatibilityInfo(ProbPhenCategCharModel.class, CategoricalState.class);
	}
	public  boolean isCompatible(Object obj, MesquiteProject project, EmployerEmployee prospectiveEmployer){
			if (project==null){
			 	return super.isCompatible(obj, project, prospectiveEmployer);
			}
			 Listable[] models = project.getCharacterModels(mci, CategoricalState.class);
			 if (models == null || models.length == 0)
			 	return false;
			 boolean oneFound = false;
			 for (int i=0; i<models.length; i++)
			 	if (((ProbPhenCategCharModel)models[i]).isFullySpecified()) {
			 		oneFound = true;
			 		break;
			 	}
			 if (oneFound){
			 	return super.isCompatible(obj, project, prospectiveEmployer);
			 }
			 return false;
	}
}



