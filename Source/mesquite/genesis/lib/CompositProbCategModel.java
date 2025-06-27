/* Mesquite source code (Genesis package).  Copyright 2001 and onward, D. Maddison and W. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.genesis.lib;

import java.util.Random;

import mesquite.categ.lib.CategoricalState;
import mesquite.lib.DoubleArray;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteListener;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.Notification;
import mesquite.lib.StringUtil;
import mesquite.lib.characters.CharacterModel;
import mesquite.lib.characters.CharacterState;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.tree.Tree;
import mesquite.lib.ui.ExtensibleDialog;
import mesquite.stochchar.lib.CategProbModelCurator;
import mesquite.stochchar.lib.ProbabilityCategCharModel;

/* ======================================================================== */
/** A character model for Categorical characters to be used in stochastic simulations and in likelihood calculations.
It must serve both for calculating probabilities (via the transitionProbability method) and for simulating character evolution
(via the evolveState method).*/
public abstract class CompositProbCategModel  extends ProbabilityCategCharModel {
	//long allStates = 0L;
	//protected int[] availableStates;
	double[] rootStateFrequencies;
	//protected Random randomNumGen;
	CategProbModelCurator curator=null;
	//protected CategoricalData data=null;
	Taxa taxa = null;
	double scalingFactor=1.0;
	boolean notify = true;
	
	StateFreqModel rootStatesModel;
	CharRatesModel charRatesModel;
	RateMatrixCatModel rateMatrixModel;
	StateFreqModel equilibriumStatesModel;

	StateFreqModel originalRootStatesModel;
	CharRatesModel originalCharRatesModel;
	RateMatrixCatModel originalRateMatrixModel;
	StateFreqModel originalEquilibriumStatesModel;

	long originalSeed = System.currentTimeMillis();
	
	public CompositProbCategModel (String name, Class dataClass, CategProbModelCurator curator) {
		super(name, dataClass);
		this.curator = curator;
 		//randomNumGen = new Random();
 		//initAvailableStates();
 		rootStatesModel = initRootStatesModel();
 		charRatesModel = initCharRatesModel();
 		equilibriumStatesModel = initEquilStatesModel();
 		rateMatrixModel = initRateMatrixModel();  // this must be called after initEquilStatesModel, as it depends upon equilibriumStatesModel being set
 		
 	}
	public CompositProbCategModel (String name, Class dataClass) {
		super(name, dataClass);
 		randomNumGen = new Random();
 		//initAvailableStates();
 		rootStatesModel = initRootStatesModel();
 		charRatesModel = initCharRatesModel();
 		equilibriumStatesModel = initEquilStatesModel();
 		rateMatrixModel = initRateMatrixModel();  // this must be called after initEquilStatesModel, as it depends upon equilibriumStatesModel being set
 		
 	}
 	/* copy information from this to model passed (used in cloneModelWithMotherLink to ensure that superclass info is copied); should call super.copyToClone(pm) */
	public void copyToClone(CharacterModel md){
		if (md == null || !(md instanceof CompositProbCategModel))
			return;
		
		CompositProbCategModel pm = (CompositProbCategModel)md;
		pm.allStates = allStates;
		//pm.availableStates = IntegerArray.copy(availableStates);
		pm.rootStateFrequencies = DoubleArray.copy(rootStateFrequencies);
		pm.curator=curator;
		pm.taxa = taxa;
		pm.scalingFactor=scalingFactor;
		copySubModels(pm);
		pm.setSeed(originalSeed);
		super.copyToClone(pm);
	}
 	/*.................................................................................................................*/
 	public CategProbModelCurator getCurator() {
 		return curator;
 	}
 	/*.................................................................................................................*/
 	public void setCurator(CategProbModelCurator curator) {
 		this.curator = curator;
 	}
 	/*.................................................................................................................*/
 	public double getScalingFactor() {
		return scalingFactor;
 	}
 	/*.................................................................................................................*/
 	public void setScalingFactor(double scale) {
		scalingFactor = scale;
 	}
	/*.................................................................................................................*/
	public void copySubModels(CompositProbCategModel model){
		if (model == null)
			return;
		if (getRootStatesModel()!=null)
			model.setRootStatesModel(getOriginalRootStatesModel()); 
		if (getEquilStatesModel()!=null)
			model.setEquilStatesModel(getOriginalEquilStatesModel());
		if (getCharRatesModel()!=null)
			model.setCharRatesModel(getOriginalCharRatesModel());
		if (getRateMatrixModel()!=null)
			model.setRateMatrixModel(getOriginalRateMatrixModel());
	}
	/*.................................................................................................................*/
	public void initializeSubModels(){
		rootStatesModel.setCompositProbCategModel(this);
		equilibriumStatesModel.setCompositProbCategModel(this);
		charRatesModel.setCompositProbCategModel(this);
		rateMatrixModel.setCompositProbCategModel(this);
		
		setSeed(originalSeed);
		
		rootStatesModel.initialize();
		equilibriumStatesModel.initialize();
		charRatesModel.initialize();
		rateMatrixModel.initialize();
	}
	/*.................................................................................................................*/
	public void initializeRootStatesModel(){
		rootStatesModel.setCompositProbCategModel(this);
		rootStatesModel.initialize();
		setSeed(originalSeed);
	}
	/*.................................................................................................................*/
	public void initializeEquilStatesModel(){
		equilibriumStatesModel.setCompositProbCategModel(this);
		equilibriumStatesModel.initialize();
		setSeed(originalSeed);
	}
	/*.................................................................................................................*/
	public void initializeCharRatesModel(){
		charRatesModel.setCompositProbCategModel(this);
		charRatesModel.initialize();
		setSeed(originalSeed);
	}
	/*.................................................................................................................*/
	public void initializeRateMatrixModel(){
		rateMatrixModel.setCompositProbCategModel(this);
		rateMatrixModel.initialize();
		setSeed(originalSeed);
	}
	/*.................................................................................................................*/
	public void setTaxa(Taxa taxa){
		this.taxa = taxa;
		
		if (rootStatesModel !=null)
			rootStatesModel.taxaSet();
		if (equilibriumStatesModel !=null)
			equilibriumStatesModel.taxaSet();
		if (charRatesModel !=null)
			charRatesModel.taxaSet();
		if (rateMatrixModel !=null)
			rateMatrixModel.taxaSet();
	}
	/*.................................................................................................................*/
	public boolean initialize(String message){
		return true;
	}
	/*.................................................................................................................*/
	public void resetFromOriginalSubmodels(){
	}
	/*.................................................................................................................*/
	public Taxa getTaxa(){
		return taxa;
	}
	/*.................................................................................................................*/
	public void changed(Object caller, Object obj, Notification notification){
		if (obj == rootStatesModel || obj == equilibriumStatesModel || obj == charRatesModel || obj == rateMatrixModel) {
			if (notify) {
				if (!Notification.appearsCosmetic(notification))
					notifyListeners(this, new Notification(MesquiteListener.UNKNOWN));
			}
		}
		else if (obj == originalRootStatesModel || obj == originalEquilibriumStatesModel || obj == originalCharRatesModel || obj == originalRateMatrixModel) {
			if (notify) {
				if (!Notification.appearsCosmetic(notification))
					notifyListeners(this, new Notification(MesquiteListener.UNKNOWN));
			}
		}
		super.changed(caller, obj, notification);
	}
	/** passes which object was disposed*/
	public void disposing(Object obj){
		if (obj == originalRootStatesModel || obj == originalEquilibriumStatesModel || obj == originalCharRatesModel || obj == originalRateMatrixModel) {
			if (obj == originalRootStatesModel)
		 		rootStatesModel = initRootStatesModel();
			else if (obj == originalEquilibriumStatesModel)
		 		equilibriumStatesModel = initEquilStatesModel();
			else if (obj == originalCharRatesModel)
		 		charRatesModel = initCharRatesModel();
			else if (obj == originalRateMatrixModel)
	 			rateMatrixModel = initRateMatrixModel(); 
			if (notify) {
				notifyListeners(this, new Notification(MesquiteListener.UNKNOWN));
			}
		}
		super.disposing(obj);
	}
	/** Asks whether it's ok to delete the object as far as the listener is concerned (e.g., is it in use?)*/
	public boolean okToDispose(Object obj, int queryUser){
		return true;
	}
 	/*.................................................................................................................*/
	public abstract RateMatrixCatModel initRateMatrixModel();
 	/*.................................................................................................................*/
	public abstract StateFreqModel initRootStatesModel();
 	/*.................................................................................................................*/
	public abstract StateFreqModel initEquilStatesModel();
 	/*.................................................................................................................*/
	public abstract CharRatesModel initCharRatesModel() ;
  	/*.................................................................................................................*/
	public StateFreqModel getRootStatesModel() {
		return rootStatesModel;
	}
 	/*.................................................................................................................*/
	public StateFreqModel getEquilStatesModel() {
		return equilibriumStatesModel;
	}
 	/*.................................................................................................................*/
	public CharRatesModel getCharRatesModel() {
		return charRatesModel;
	}
 	/*.................................................................................................................*/
	public RateMatrixCatModel getRateMatrixModel() {
		return rateMatrixModel;
	}
  	/*.................................................................................................................*/
	public StateFreqModel getOriginalRootStatesModel() {
		return originalRootStatesModel;
	}
 	/*.................................................................................................................*/
	public StateFreqModel getOriginalEquilStatesModel() {
		return originalEquilibriumStatesModel;
	}
 	/*.................................................................................................................*/
	public CharRatesModel getOriginalCharRatesModel() {
		return originalCharRatesModel;
	}
 	/*.................................................................................................................*/
	public RateMatrixCatModel getOriginalRateMatrixModel() {
		return originalRateMatrixModel;
	}
	/*.................................................................................................................*/
	public void dispose() {
		if (rootStatesModel !=null) {
			rootStatesModel.dispose(); //NOTE: assumes submodel is a clone of original!
			rootStatesModel.removeListener(this); 
		}
		if (equilibriumStatesModel !=null) {
			equilibriumStatesModel.dispose(); //NOTE: assumes submodel is a clone of original!
			equilibriumStatesModel.removeListener(this); 
		}
		if (rateMatrixModel !=null){
			rateMatrixModel.dispose(); //NOTE: assumes submodel is a clone of original!
			rateMatrixModel.removeListener(this); 
		}
		if (charRatesModel !=null){
			charRatesModel.dispose(); //NOTE: assumes submodel is a clone of original!
			charRatesModel.removeListener(this); 
		}
		//todo: this is temporary until composite listens to its submodel disposal etc.
		if (getOriginalRootStatesModel() != null)
			getOriginalRootStatesModel().removeListener(this); 
		
		if (getOriginalEquilStatesModel() != null)
			getOriginalEquilStatesModel().removeListener(this); 
		
		if (getOriginalCharRatesModel() != null)
			getOriginalCharRatesModel().removeListener(this); 
		
		if (getOriginalRateMatrixModel() != null)
			getOriginalRateMatrixModel().removeListener(this); 
		
		super.dispose();
	}
	/*.................................................................................................................*/
	public void setRootStatesModel(StateFreqModel model) { //assumes original coming in
		if (model == null)
			return;
		if (originalRootStatesModel !=null)
			originalRootStatesModel.removeListener(this);
		originalRootStatesModel = model;
		if (originalRootStatesModel !=null)
			originalRootStatesModel.addListener(this);
		if (rootStatesModel !=null) {
			rootStatesModel.removeListener(this);
			rootStatesModel.dispose(); //Assumes submodel had been cloned locally && thus we're in charge of it
		}
		rootStatesModel = (StateFreqModel)model.cloneModelWithMotherLink(rootStatesModel);
		if (rootStatesModel !=null) {
			rootStatesModel.addListener(this);
			rootStatesModel.setCompositProbCategModel(this);
		}
	}
 	/*.................................................................................................................*/
	public void setEquilStatesModel(StateFreqModel model) {
		if (model == null)
			return;
		if (originalEquilibriumStatesModel !=null)
			originalEquilibriumStatesModel.removeListener(this);
		originalEquilibriumStatesModel = model;
		if (originalEquilibriumStatesModel !=null)
			originalEquilibriumStatesModel.addListener(this);

		if (equilibriumStatesModel !=null) {
			equilibriumStatesModel.removeListener(this);
			equilibriumStatesModel.dispose(); //Assumes submodel had been cloned locally && thus we're in charge of it
		}
		equilibriumStatesModel = (StateFreqModel)model.cloneModelWithMotherLink(equilibriumStatesModel);
		if (equilibriumStatesModel !=null) {
			equilibriumStatesModel.addListener(this);
			equilibriumStatesModel.setCompositProbCategModel(this);
		}
	}
 	/*.................................................................................................................*/
	public void setCharRatesModel(CharRatesModel model) {
		if (model == null)
			return;
		if (originalCharRatesModel !=null)
			originalCharRatesModel.removeListener(this);
		originalCharRatesModel = model;
		if (originalCharRatesModel !=null)
			originalCharRatesModel.addListener(this);
		if (charRatesModel !=null) {
			charRatesModel.removeListener(this);
			charRatesModel.dispose(); //Assumes submodel had been cloned locally && thus we're in charge of it
		}
		charRatesModel = (CharRatesModel)model.cloneModelWithMotherLink(charRatesModel);
		if (charRatesModel !=null) {
			charRatesModel.addListener(this);
			charRatesModel.setCompositProbCategModel(this);
		}
	}
	/*.................................................................................................................*/
	public void setRateMatrixModel(RateMatrixCatModel model) {
		if (model == null)
			return;
		if (originalRateMatrixModel !=null)
			originalRateMatrixModel.removeListener(this);
		originalRateMatrixModel = model;
		if (originalRateMatrixModel !=null)
			originalRateMatrixModel.addListener(this);

		if (rateMatrixModel !=null) {
			rateMatrixModel.removeListener(this);
			rateMatrixModel.dispose(); //Assumes submodel had been cloned locally && thus we're in charge of it
		}
		rateMatrixModel =  (RateMatrixCatModel)model.cloneModelWithMotherLink(rateMatrixModel);
		if (rateMatrixModel !=null) {
			rateMatrixModel.addListener(this);
			rateMatrixModel.setCompositProbCategModel(this);
		}
	}
 	/*.................................................................................................................*/
	public boolean isFullySpecified() {
		if (rateMatrixModel==null || !rateMatrixModel.isFullySpecified())
			return false;
		if (rootStatesModel==null || !rootStatesModel.isFullySpecified())
			return false;
		if (equilibriumStatesModel==null || !equilibriumStatesModel.isFullySpecified())
			return false;
		if (charRatesModel==null || !charRatesModel.isFullySpecified())
			return false;
		return true;
	}
	public int getMaximumNumChars(){
		return MesquiteInteger.infinite; //here put smaller number if empirical matrix is constraint
	}
	/*.................................................................................................................*
	public abstract void initAvailableStates();
	/*.................................................................................................................*/
	/** Returns natural log of the transition probability from beginning to ending state in the given tree on the given node*/
	public double transitionLnProbability (int beginState, int endState, Tree tree, int node){
		return Math.log(transitionProbability(beginState, endState, tree, node));
	}
	/*.................................................................................................................*/
	/** Returns transition probability from beginning to ending state in the given tree on the given node*/
	public double transitionProbability (int beginState, int endState, Tree tree, int node){
		if (rateMatrixModel==null) //added 21 mar 02
			return 0;
		return rateMatrixModel.transitionProbability(beginState,endState,tree,node);
	}
	/** Randomly generates a character state according to model, on branch on tree, from beginning state*/
	/*.................................................................................................................*/
	public void evolveState (CharacterState beginState, CharacterState endState, Tree tree, int node){
		if (endState==null) {
			return;
		}
		if (beginState==null || !(beginState instanceof CategoricalState) || !(endState instanceof CategoricalState)){
			endState.setToUnassigned();
			return;
		}
		CategoricalState bState = (CategoricalState)beginState;
		CategoricalState eState = (CategoricalState)endState;
		int r = evolveState(CategoricalState.minimum(bState.getValue()), tree, node);
		eState.setValue(CategoricalState.makeSet(r));
	}
	/*.................................................................................................................*/
	/** Randomly generates a character state according to model, on branch on tree, from beginning state*/
	public int evolveState (int beginState, Tree tree, int node) {
		if (rateMatrixModel==null)
			return -1;
		return rateMatrixModel.evolveState(beginState,tree,node);
	}
	/*.................................................................................................................*/
	/** Randomly generates according to model an ancestral state for root of tree*/
	public long getRootState (Tree tree){
		if (rootStatesModel==null)
			return CategoricalState.unassigned;
		if (tree == null)
			return rootStatesModel.getState( tree, 0);
		else
			return rootStatesModel.getState( tree, tree.getRoot());
	}
	/*.................................................................................................................*/
	/** Randomly generates according to model an ancestral state for root of tree*/
	public double getRootStateFreq (int state, Tree tree){
		if (rootStatesModel==null)
			return 0;
		if (tree == null)
			return rootStatesModel.getStateFreq(state, tree, 0);
		else
			return rootStatesModel.getStateFreq(state, tree, tree.getRoot());
		
	}
	/*.................................................................................................................*/
	/** Randomly generates according to model an ancestral state for root of tree*/
	public CharacterState getRootState (CharacterState state, Tree tree){
		if (state==null || !(state instanceof CategoricalState))
			state = new CategoricalState();
		((CategoricalState)state).setValue(getRootState(tree));
		return state;
	}
	/*.................................................................................................................*/
	/** returns prior probabilities for states (e.g., state frequencies)*
	public abstract double priorProbability (int state);
	
 	/*.................................................................................................................*/
	public boolean isFirstCharacter() {
		int ic = -1;
		if (getCharacterDistribution()!=null)
			ic = getCharacterDistribution().getParentCharacter();
		return (ic==0);
	}
 	/*.................................................................................................................*/
	public void setSeed(long seed){
		originalSeed = seed;
		randomNumGen.setSeed(seed);
		if (rootStatesModel !=null)
			rootStatesModel.setSeed(randomNumGen.nextLong());
		if (equilibriumStatesModel !=null)
			equilibriumStatesModel.setSeed(randomNumGen.nextLong());
		if (charRatesModel !=null)
			charRatesModel.setSeed(randomNumGen.nextLong());
		if (rateMatrixModel !=null)
			rateMatrixModel.setSeed(randomNumGen.nextLong());
	}
 	/*.................................................................................................................*/
	public long getSeed(){
		return randomNumGen.nextLong();
	}
 	/*.................................................................................................................*
	public boolean inStates(int state) { //todo: these should be part of standard categ models
		if (availableStates==null)
			return false;
		else {
			for (int i=0; i<availableStates.length; i++)
				if (state == availableStates[i])
					return true;
			return false;
		}
	}
 	/*.................................................................................................................*
	public void setStatesSimulatable(long stateSet) {  //todo: this should check to see that not already seen!
		if (CategoricalState.cardinality(stateSet)>1) {
			availableStates = CategoricalState.expand(stateSet);
			allStates = stateSet;
		}
	}
	/*.................................................................................................................*
	/** Returns the maximum state available for transitions*
	public int getMaxState() {
		int max = -1;
		for (int i=0; i<availableStates.length; i++)
			if (max < availableStates[i]) max=availableStates[i];
		return max;
	}
 	/*.................................................................................................................*/
	public void addOptionsOnCards(ExtensibleDialog dialog) {
		/*
		TabbedPanelOfCards cardPanels = dialog.addTabbedPanelOfCards("Tabbed Panel");
		if (rateMatrixModel!=null) {
			cardPanels.newCard("Rate Matrix", "Rate Matrix", "ratematrixtab.gif", "ratematrixtabOff.gif");
			rateMatrixModel.addModelOptions(dialog);
		}
		if (rootStatesModel!=null) {
			cardPanels.newCard("Root States", "Root States", "rootstatestab.gif", "rootstatestabOff.gif");
			rootStatesModel.addModelOptions(dialog);
		}
		if (equilibriumStatesModel!=null) {
			cardPanels.newCard("Equilibrium States", "Equilibrium States","equilstatestab.gif", "equilstatestabOff.gif");
			equilibriumStatesModel.addModelOptions(dialog);
		}
		if (charRatesModel!=null) {
			cardPanels.newCard("Site-to-site Rate Variation", "Site-To-Site","sitetositetab.gif","sitetositetabOff.gif");
			charRatesModel.addModelOptions(dialog);
		}
		cardPanels.finalizeCards();
	*/
	}
 	/*.................................................................................................................*/
	public void addOptions(ExtensibleDialog dialog) {
		if (rateMatrixModel!=null) 
			rateMatrixModel.addOptions(dialog);
		if (rootStatesModel!=null) 
			rootStatesModel.addOptions(dialog);
		if (equilibriumStatesModel!=null) 
			equilibriumStatesModel.addOptions(dialog);
		if (charRatesModel!=null) 
			charRatesModel.addOptions(dialog);
	}
 	/*.................................................................................................................*/
	public boolean recoverOptions() {
		boolean optionsRecovered = true;
		if (rateMatrixModel!=null)
			optionsRecovered = rateMatrixModel.recoverOptions();
		if (rootStatesModel!=null)
			if (optionsRecovered)
				optionsRecovered = rootStatesModel.recoverOptions();
			else
				rootStatesModel.recoverOptions();
		if (equilibriumStatesModel!=null)
			if (optionsRecovered)
				optionsRecovered = equilibriumStatesModel.recoverOptions();
			else
				equilibriumStatesModel.recoverOptions();
		if (charRatesModel!=null)
			if (optionsRecovered)
				optionsRecovered = charRatesModel.recoverOptions();
			else
				charRatesModel.recoverOptions();
		return optionsRecovered;
	}
  	/*.................................................................................................................*/
	public boolean checkOptions() {
		boolean optionsChecked = true;
		if (rateMatrixModel!=null)
			optionsChecked = rateMatrixModel.checkOptions();
		if (rootStatesModel!=null)
			if (optionsChecked)
				optionsChecked=rootStatesModel.checkOptions();
			else
				rootStatesModel.checkOptions();
		if (equilibriumStatesModel!=null)
			if (optionsChecked)
				optionsChecked=equilibriumStatesModel.checkOptions();
			else
				equilibriumStatesModel.checkOptions();
		if (charRatesModel!=null)
			if (optionsChecked)
				optionsChecked=charRatesModel.checkOptions();
			else
				charRatesModel.checkOptions();
		return optionsChecked;
	}
	/*.................................................................................................................*/
	public String checkOptionsReport() {
		String s;
		if (rateMatrixModel!=null) {
			 s = rateMatrixModel.checkOptionsReport();
			 if (!StringUtil.blank(s))
			 	return s;
		}
		if (rootStatesModel!=null) {
			s = rootStatesModel.checkOptionsReport();
			 if (!StringUtil.blank(s))
			 	return s;
		}
		if (equilibriumStatesModel!=null) {
			s = equilibriumStatesModel.checkOptionsReport();
			 if (!StringUtil.blank(s))
			 	return s;
		}
		if (charRatesModel!=null) {
			s = charRatesModel.checkOptionsReport();
			 if (!StringUtil.blank(s))
			 	return s;
		}
		return "";
	}
	/*.................................................................................................................*/
	public void setOptions() {
		if (rateMatrixModel!=null) 
			rateMatrixModel.setOptions();
		if (rootStatesModel!=null) 
			rootStatesModel.setOptions();
		if (equilibriumStatesModel!=null)
			equilibriumStatesModel.setOptions();
		if (charRatesModel!=null)
			charRatesModel.setOptions();
	}
 	/*.................................................................................................................*/
	public void queryModelOptions(String message, boolean useCardLayout) {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(MesquiteTrunk.mesquiteTrunk.containerOfModule(), message,buttonPressed);
		dialog.addLabel(message);
		if (useCardLayout)
			addOptionsOnCards(dialog);
		else
			addOptions(dialog);
		dialog.completeAndShowDialog(true);
		recoverOptions();
		dialog.dispose();
		if (buttonPressed.getValue()==0)  {
			setOptions();
			setEditCancel(false);
		}
		else
			setEditCancel(true);
	}
 	/*.................................................................................................................*/
	public void queryModelOptions(String message) {
		queryModelOptions(message, false);
	}
 	/*.................................................................................................................*/
	/** returns parameters of the model. */
	public String getParameters (){
		return "";
	}
}

