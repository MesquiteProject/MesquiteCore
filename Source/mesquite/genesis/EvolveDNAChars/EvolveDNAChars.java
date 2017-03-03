/* Mesquite source code (Genesis package).  Copyright 2001 and onward, D. Maddison and W. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.genesis.EvolveDNAChars;
/*~~  */

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import mesquite.genesis.lib.*;
import mesquite.stochchar.lib.*;

/* ======================================================================== */
public class EvolveDNAChars extends CharacterSimulator implements MesquiteListener {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(ProbModelSourceSim.class, getName() + "  needs a source of a probabilistic model to use in the simulation.",
				"The source of a probabilistic model to use in the simulation can be selected initially or in the Source of Stochastic Models submenu");
		EmployeeNeed e2 = registerEmployeeNeed(mesquite.charMatrices.StoredMatrices.StoredMatrices.class, getName() + "  needs a source of character matrices.",
				"The source of character matrices is arranged automatically");
	}
	/*.................................................................................................................*/
	DNACharacterHistory evolvingStates;
	CharacterData data = null;
	ProbModelSourceSim modelSource; //ProbModelSourceSim
	MesquiteString modelSourceName;
	SimulationDNAModel  model, originalProbabilityModel;
	CharMatrixSource matrixSource;
	MCharactersDistribution matrix = null; //for empirical matrix
	boolean reclone = false;
	DNACharacterAdjustable representativeCharacter;
	String checkString = null;
	boolean modelChecked = false;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (condition !=null && condition instanceof CompatibilityTest)
			condition = ((CompatibilityTest)condition).getAcceptedClass();
		if (condition!=null && !(condition instanceof Class && (condition == DNAState.class || condition == DNAData.class || ((Class)condition).isAssignableFrom(DNAData.class) || ((Class)condition).isAssignableFrom(DNAState.class)) ) )
			return sorry("Module \"Evolve DNA characters\" has been asked to simulate a sort of data it's not able to simulate (" + ((Class)condition).getName() + "; employer path: " + getEmployerPath()+ ")");
		representativeCharacter = new DNACharacterAdjustable(null, 1);

		modelSource = (ProbModelSourceSim)hireCompatibleEmployee(ProbModelSourceSim.class, DNAState.class, "Source of stochastic model(s) for simulation of DNA evolution");
		if (modelSource == null) {
			return sorry("Evolve DNA characters could not start because no appropriate model source module could be obtained");
		}
		modelSourceName = new MesquiteString(modelSource.getName());
		if (modelSource.modelFromModelSet()) {
		} else {
			if (numCompatibleModulesAvailable(ProbModelSource.class, DNAState.class, this)>1){
				MesquiteSubmenuSpec mss = addSubmenu(null, "Source of Stochastic Models", makeCommand("setModelSource", this), ProbModelSource.class);
				mss.setSelected(modelSourceName);
				mss.setCompatibilityCheck(DNAState.class);
			}
		}
		CommandRecord cr = MesquiteThread.getCurrentCommandRecord();
		MesquiteThread.setCurrentCommandRecord(CommandRecord.scriptingRecord);
		matrixSource = (CharMatrixSource)hireNamedEmployee(CharMatrixSource.class, "#StoredMatrices", DNAState.class);
		MesquiteThread.setCurrentCommandRecord(cr);
		getProject().getCentralModelListener().addListener(this);

		return true;
	}

	/*.................................................................................................................*/
	public void endJob() {
		if (model !=null && !modelSource.modelFromModelSet())
			model.dispose();
		getProject().getCentralModelListener().removeListener(this);
		super.endJob();
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public void disposing(Object obj){
		if (obj == originalProbabilityModel) {
			originalProbabilityModel = null;
			checkString = checkModel(representativeCharacter, -1);
			parametersChanged();
		}
	}
	/*.................................................................................................................*/
	public void changed(Object caller, Object obj, Notification notification){
		if (obj == originalProbabilityModel) {
			reclone = true;
			checkString = checkModel(representativeCharacter, -1);
			parametersChanged();
		}
	}
	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		if (employee==matrixSource) {
			matrix = null;  // to force retrieval of new empirical matrix
			parametersChanged(notification);
		}
		else if (employee==modelSource) {
			originalProbabilityModel = null;
			checkString = checkModel(representativeCharacter, -1);
			parametersChanged(notification);
		}
	}
	/*.................................................................................................................*/
	public boolean requestPrimaryChoice(){
		return true;
	}
	/*.................................................................................................................*/
	/** This returns just the states at the terminal **/
	private void evolve(ProbabilityCategCharModel model, Tree tree, DNACharacterAdjustable statesAtTips, int node) {
		if (node!=tree.getRoot()) {
			long statesAtAncestor = evolvingStates.getState(tree.motherOfNode(node));
			int stateAtAncestor = DNAState.minimum(statesAtAncestor);
			long statesAtNode = DNAState.makeSet(model.evolveState(stateAtAncestor, tree, node));
			evolvingStates.setState(node, statesAtNode);  
			if (tree.nodeIsTerminal(node)) {
				statesAtTips.setState(tree.taxonNumberOfNode(node), statesAtNode);
			}
		}
		for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter))
			evolve(model, tree, statesAtTips, daughter);
	}
	/*.................................................................................................................*/
	/** This returns both the states at the terminal and at all the internal nodes **/
	private void evolve (ProbabilityCategCharModel model, Tree tree, DNACharacterHistory statesAtNodes, int node) {
		if (node!=tree.getRoot()) {
			long statesAtAncestor = statesAtNodes.getState(tree.motherOfNode(node));
			int stateAtAncestor = CategoricalState.minimum(statesAtAncestor);
			long statesAtNode = CategoricalState.makeSet(model.evolveState(stateAtAncestor, tree, node));
			statesAtNodes.setState(node, statesAtNode);
		}
		for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter))
			evolve(model, tree, (DNACharacterHistory)statesAtNodes, daughter);
	}
	/*.................................................................................................................*/
	/** Does any needed initializations. */ 
	public void initialize(Taxa taxa){
		checkString = checkModel(representativeCharacter = new DNACharacterAdjustable(taxa, taxa.getNumTaxa()), -1);
	}
	/*.................................................................................................................*/
	/** Returns maximum number of characters to simulate. */ 
	public int getMaximumNumChars(Taxa taxa){
		return MesquiteInteger.infinite;
	}
	/*.................................................................................................................*/
	/** Returns default number of characters to simulate. */ 
	public int getDefaultNumChars(Taxa taxa){
		if (model!=null) {
			int numChars = model.getDefaultNumChars();
			if (numChars>0)
				return numChars;
		} else if (modelSource.modelFromModelSet()) {
			MCharactersDistribution m = matrixSource.getCurrentMatrix(taxa);
			if (m!=null)
				return m.getNumChars();
		}
		return 100;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		temp.addLine("setModelSource ", modelSource);
		temp.addLine("getMatrixSource ", matrixSource);
		return temp;
	}
	public String purposeOfEmployee(MesquiteModule module){
		if (module  instanceof ProbModelSource){
			return "for simulation";
		}
		else if (module   instanceof CharMatrixSource){
			return "empirical, for simulation";
		}
		return "";
	}
	/*.................................................................................................................*/
	MesquiteInteger pos = new MesquiteInteger(0);
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the module used to supply stochastic models", "[name of module]", commandName, "setModelSource")) {
			ProbModelSourceSim temp;
			temp =  (ProbModelSourceSim)replaceCompatibleEmployee(ProbModelSourceSim.class, arguments, modelSource, DNAState.class);
			if (temp!=null) {
				modelSource=  temp;
				modelSourceName.setValue(modelSource.getName());
				parametersChanged(); //?
				return modelSource;
			}
		}
		else if (checker.compare(this.getClass(), "Returns the module used to supply matrix", null, commandName, "getMatrixSource")) {
			return matrixSource;
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	

	/*.................................................................................................................*/
	String checkModel(CategoricalDistribution statesAtNodes, int ic) {
		ProbabilityModel cmodel = originalProbabilityModel;
		modelChecked = true;

		if (modelSource.modelFromModelSet()) {
			MCharactersDistribution m = matrixSource.getCurrentMatrix(statesAtNodes.getTaxa());
			if (m!=null) {
				data = m.getParentData();
				cmodel = (ProbabilityModel)modelSource.getCharacterModel(m.getParentData(), ic);
			}
		} else {
			if (originalProbabilityModel == null) {
				cmodel = (ProbabilityModel)modelSource.getCharacterModel(statesAtNodes);
			}
		}
		if (cmodel == null)
			return "model not obtained.";
		else if (!(cmodel instanceof ProbabilityCategCharModel))
			return " the model of character evolution ("  + cmodel.getName() + ") isn't of the appropriate type.";
		else if (!cmodel.isFullySpecified())
			return " the model of character evolution ("  + cmodel.getName() + ") doesn't have all of its parameters specified.";

		if (originalProbabilityModel != cmodel) {
			originalProbabilityModel = (SimulationDNAModel)cmodel;
			reclone = true;
		}
		if (reclone){
			if (model !=null && !modelSource.modelFromModelSet())
				model.dispose();
			model = (SimulationDNAModel)cmodel.cloneModelWithMotherLink(model); //use a copy in case fiddled with
			model.setUserVisible(false);
			reclone = false;
		}
		if (model == null)
			return "model not obtained.";


		if (model.needsEmpirical() || modelSource.modelFromModelSet()) {
			MCharactersStatesHolder charMatrix = model.getMCharactersStatesHolder();
			if (charMatrix==null || matrix == null){
				MCharactersDistribution m = matrixSource.getCurrentMatrix(statesAtNodes.getTaxa());
				if (m == null)
					return "No matrix found";
				matrix = m;
				model.setMCharactersStatesHolder(matrix);
				model.setDefaultNumChars(matrix.getNumChars());
				model.recalcAfterSetMCharactersStatesHolder();
			}
		}
		return null;
	}
	int warned = 0;
	int warnedNoModel = 0;
	
	void stampWithCurrentSpecsSet (CharacterData currentData) {
		CharacterPartition characterPartition = (CharacterPartition)currentData.getCurrentSpecsSet(CharacterPartition.class);
		CharInclusionSet incl = (CharInclusionSet)currentData.getCurrentSpecsSet(CharInclusionSet.class);
	}

   	public void cleanupAfterSimulation(MAdjustableDistribution matrix){
		 if (modelSource.modelFromModelSet()) {
			 for (int ic=0; ic<data.getNumChars(); ic++) {
				 SimulationDNAModel currentModel = (SimulationDNAModel)((ProbModelSourceSim)modelSource).getCharacterModel(data,ic);
				 currentModel.clearNoCheckFlag();
			 }
		}
		 if (matrix!=null) {
			 CharacterData data = matrix.getParentData();
			 if (data !=null) {
			 }
		 }
   	}

	/*.................................................................................................................*/
	public CharacterDistribution getSimulatedCharacter(CharacterDistribution statesAtTips, Tree tree, MesquiteLong seed, int ic){
		if (tree == null)
			return null;
		else if (statesAtTips == null || !(statesAtTips instanceof DNACharacterAdjustable)){
			statesAtTips =  new DNACharacterAdjustable(tree.getTaxa(), tree.getTaxa().getNumTaxa());
		}
		else	{
			statesAtTips = (CharacterDistribution)((AdjustableDistribution)statesAtTips).adjustSize(tree.getTaxa());
		}

		if (!modelChecked || model==null)
			checkString = checkModel(representativeCharacter = new DNACharacterAdjustable(tree.getTaxa(), tree.getTaxa().getNumTaxa()), ic);
		 if (modelSource.modelFromModelSet()) {
			model = (SimulationDNAModel)((ProbModelSourceSim)modelSource).getCharacterModel(data,ic);
			if (matrix == null){
				MCharactersDistribution m = matrixSource.getCurrentMatrix(statesAtTips.getTaxa());
				if (m == null)
					return null;
				matrix = m;
			}
			model.setMCharactersStatesHolder(matrix);
			model.setDefaultNumChars(matrix.getNumChars());
			model.recalcAfterSetMCharactersStatesHolder();
			model.setNoCheckFlag(true);
		}

		if (statesAtTips instanceof CategoricalAdjustable)
			((CategoricalAdjustable)statesAtTips).deassignStates();
		if (checkString!=null){
			if ((warned<2 && !MesquiteThread.isScripting()) || warned<1)  //not warned or previous warning was under scripting
				discreetAlert( "Sorry, the simulation cannot continue because " + checkString);
			if (MesquiteThread.isScripting())
				warned = 1;
			else
				warned = 2; 
			return statesAtTips;
		}

		
		model.setMaxStateSimulatable(3); 

		evolvingStates =(DNACharacterHistory) statesAtTips.adjustHistorySize(tree, evolvingStates);

		/*For some reason, in some circumstances, this setSeed can use the same seed but the resulting simulation differs. This happens with composite model, and first time seed is
set, give different result from before, but second time, gives same result*/
		if (seed!=null && !modelSource.modelFromModelSet())  // we need to turn this off if from modelset as just resets to same thing every time.  TODO: find out why
			model.setSeed(seed.getValue());
		model.setCharacterDistribution(statesAtTips);
		model.initForNextCharacter();
		long rootState = model.getRootState(tree);
		evolvingStates.setState(tree.getRoot(), rootState);  //starting root
		evolve(model, tree, (DNACharacterAdjustable)statesAtTips, tree.getRoot());
		if (seed!=null)
			seed.setValue(model.getSeed());
		return statesAtTips;
	}
	/*.................................................................................................................*/
	public CharacterHistory getSimulatedHistory(CharacterHistory statesAtNodes, Tree tree, MesquiteLong seed){
		if (tree == null) 
			return null;
		else if (statesAtNodes == null || !(statesAtNodes instanceof DNACharacterHistory))
			statesAtNodes =  new DNACharacterHistory(tree.getTaxa(), tree.getNumNodeSpaces());
		else	
			statesAtNodes = statesAtNodes.adjustSize(tree);

		if (!modelChecked)
			checkString = checkModel(representativeCharacter = new DNACharacterAdjustable(tree.getTaxa(), tree.getTaxa().getNumTaxa()), -1);

		if (checkString!=null){
			if ((warned<2 && !MesquiteThread.isScripting()) || warned<1)  //not warned or previous warning was under scripting
				discreetAlert( "Sorry, the simulation cannot continue because " + checkString);
			if (MesquiteThread.isScripting())
				warned = 1;
			else
				warned = 2; 
			if (statesAtNodes instanceof CategoricalAdjustable)
				((CategoricalAdjustable)statesAtNodes).deassignStates();
			return statesAtNodes;
		}

		if (seed!=null)
			model.setSeed(seed.getValue());
		model.setCharacterDistribution(statesAtNodes);
		model.initForNextCharacter();
		((DNACharacterHistory)statesAtNodes).setState(tree.getRoot(), model.getRootState(tree));  //starting root at 0
		evolve(model, tree, (DNACharacterHistory)statesAtNodes, tree.getRoot());
		if (seed!=null)
			seed.setValue(model.getSeed());
		return statesAtNodes;
	}
	/*.................................................................................................................*/
	/** Indicates the type of character created */ 
	public Class getStateClass(){
		return DNAState.class;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Evolve DNA Characters";
	}

	/*.................................................................................................................*/
	public String getParameters() {
		if (model==null)
			return "Model NULL";
		else if (!model.isFullySpecified()){
			return "Simulation cannot be done because model is not fully specified";
		}
		if (modelSource.modelFromModelSet()) {
			return "Simulated evolution using models in current probability model set.";
		}
		String param = "Simulated evolution using model " + model.getName() + " with the following parameters:"+StringUtil.lineEnding() ;
		param += model.getParameters();
		return param;

	}
	/*.................................................................................................................*/
	public boolean showCitation(){
		return true;
	}
	/*.................................................................................................................*/
	public String getCitation(){
		return "(If the GTR model is used, the PAL project should also be cited:   Drummond, A. & K. Strimmer, 2001.  PAL: Phylogenetic Analysis Library, version 1.3.  http://www.cebl.auckland.ac.nz/pal-project/)";
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return true;
	}
	/*.................................................................................................................*/

	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Simulates evolution of DNA sequences on a tree." ;
	}
	/*.................................................................................................................*/
	/** returns current parameters, for logging etc..
 	public String getParameters() {
 		return "Probability of change: " + p ;
   	 }
	 */
	/*.................................................................................................................*/
	public CompatibilityTest getCompatibilityTest() {
		return new EDCCategoricalStateTest(); 
	}
}

//This has been added for now since modal model editing not functioning; i.e., must be pre-existing fully-specified model
class EDCCategoricalStateTest extends OffersDNAData{
	ModelCompatibilityInfo mci;
	public EDCCategoricalStateTest (){
		super();
		mci = new ModelCompatibilityInfo(CompositProbCategModel.class, DNAState.class);
	}
	public  boolean isCompatible(Object obj, MesquiteProject project, EmployerEmployee prospectiveEmployer){
		if (project==null){
			return super.isCompatible(obj, project, prospectiveEmployer);
		}
		Listable[] models = project.getCharacterModels(mci, DNAState.class);
		if (models == null || models.length == 0)
			return false;
		boolean oneFound = false;
		for (int i=0; i<models.length; i++)
			if (((CompositProbCategModel)models[i]).isFullySpecified()) {
				oneFound = true;
				break;
			}
		if (oneFound){
			boolean compatible = super.isCompatible(obj, project, prospectiveEmployer);
			return compatible;
		}
		return false;
	}
}




