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
/*~~  */

import java.awt.*;
import java.awt.event.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import mesquite.genesis.lib.*;
import mesquite.stochchar.lib.*;

public class SimulationDNAModel extends ProbabilityDNAModel  {
	CategProbModelCurator curator;
	boolean hasDefaultValues=true;
	MesquiteInteger pos = new MesquiteInteger(0);
	Listable[] rootStatesModelArray;
	Listable[] equilibriumStatesModelArray;
	Listable[] charRatesModelArray;
	Listable[] rateMatrixModelArray;
	ProbSubmodelEdit rootStatesEditListener;
	ProbSubmodelEdit equilStatesEditListener;
	ProbSubmodelEdit charRatesEditListener;
	ProbSubmodelEdit rateMatrixEditListener ;
	Choice rootStatesModelChoice;
	Choice equilibriumStatesModelChoice;
	Choice charRatesModelChoice;
	Choice rateMatrixModelChoice;
	Button editRootStatesButton;
	Button editEquilStatesButton;
	Button editCharRatesButton; 
	Button editRateMatrixButton;
	boolean noCheckFlag = false;
	
 	/*.................................................................................................................*/
	public SimulationDNAModel (String name, Class dataClass, CategProbModelCurator curator){
		//
		super(name, dataClass, curator);
		this.curator = curator;
	}
	
	public String getModelTypeName(){
		return "Simulation DNA Model";
	}
 	/*.................................................................................................................*/
	public void clearNoCheckFlag(){
		noCheckFlag=false;
	}
 	/*.................................................................................................................*/
	public void setNoCheckFlag(boolean noCheckFlag){
		this.noCheckFlag=noCheckFlag;
	}

 	/*.................................................................................................................*/
	public CharRatesModel initCharRatesModel(){
		return null;
	}
 	/*.................................................................................................................*/
	public StateFreqModel initRootStatesModel(){
		return null;
	}
 	/*.................................................................................................................*/
	public StateFreqModel initEquilStatesModel(){
		return null;
	}
 	/*.................................................................................................................*/
	public RateMatrixCatModel initRateMatrixModel() {
		return null;
	}
 	/*.................................................................................................................*/
	public void initForNextCharacter(){
		CharRatesModel charRatesModel = getCharRatesModel();
		if (charRatesModel!=null)
			charRatesModel.initForNextCharacter();
	}
	/** Returns instantaneous rate of change from beginState to endState along branch in tree*
	public double instantaneousRate (int beginState, int endState, Tree tree, int node){
		return 0;  //NOT READY YET
	}
 	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) {
   	 	Snapshot temp = new Snapshot();
   	 	if (getRootStatesModel()!= null)
   	 		temp.addLine("setRootStatesModel " + ParseUtil.tokenize(getRootStatesModel().getName()));
   	 	if (getEquilStatesModel()!= null)
   	 		temp.addLine("setEquilStatesModel " + ParseUtil.tokenize(getEquilStatesModel().getName()));
   	 	if (getCharRatesModel()!= null)
   	 		temp.addLine("setCharRatesModel " + ParseUtil.tokenize(getCharRatesModel().getName()));
   	 	if (getRateMatrixModel()!= null)
   	 		temp.addLine("setRateMatrixModel " + ParseUtil.tokenize(getRateMatrixModel().getName()));
   	 	temp.addLine("setScalingFactor " + MesquiteDouble.toString(getScalingFactor()));
  	 	return temp;
  	 }
	/*.................................................................................................................*/
 	public boolean needsEmpirical(){
		if (getRootStatesModel()!=null && getRootStatesModel().needsEmpirical())
			return true;
		if (getEquilStatesModel() !=null && getEquilStatesModel().needsEmpirical())
			return true;
		if (getCharRatesModel() != null && getCharRatesModel().needsEmpirical())
			return true;
		if (getRateMatrixModel() != null && getRateMatrixModel().needsEmpirical())
			return true;
		return false;
	}
	/*.................................................................................................................*/
 	public void recalcAfterSetMCharactersStatesHolder (){
   	 	if (noCheckFlag)
   	 		return;
   	 	if (getRootStatesModel()!= null)
			getRootStatesModel().recalcAfterSetMCharactersStatesHolder();
  	 	if (getEquilStatesModel()!= null)
			getEquilStatesModel().recalcAfterSetMCharactersStatesHolder();
   	 	if (getCharRatesModel()!= null)
			getCharRatesModel().recalcAfterSetMCharactersStatesHolder();
   	 	if (getRateMatrixModel()!= null)
			getRateMatrixModel().recalcAfterSetMCharactersStatesHolder();
	}
 	/*.................................................................................................................*/
 	/** Performs command (for Commandable interface) */
   	public Object doCommand(String commandName, String arguments, CommandChecker checker){
    	 	if (checker.compare(this.getClass(), "Sets the root states model", "[name of model]", commandName, "setRootStatesModel")) {
    	 		pos.setValue(0);
    	 		String name = ParseUtil.getFirstToken(arguments, pos);
			Listable[] rootStatesModelArray =  getProject().getFileElements(StateFreqModel.class);
			int i = ListableVector.indexOf(rootStatesModelArray, name);
			if (i<0)
				return null;
			setRootStatesModel((StateFreqModel)rootStatesModelArray[i]);
			initializeRootStatesModel();
		}
    	 	else if (checker.compare(this.getClass(), "Sets the equilibrium states model", "[name of model]", commandName, "setEquilStatesModel")) {
    	 		pos.setValue(0);
    	 		String name = ParseUtil.getFirstToken(arguments, pos);
			Listable[] equilibriumStatesModelArray =  getProject().getFileElements(StateFreqModel.class);
			int i = ListableVector.indexOf(equilibriumStatesModelArray, name);
			if (i<0)
				return null;
			setEquilStatesModel((StateFreqModel)equilibriumStatesModelArray[i]);
			initializeEquilStatesModel();
		}
    	 	else if (checker.compare(this.getClass(), "Sets the character rates model", "[name of model]", commandName, "setCharRatesModel")) {
    	 		pos.setValue(0);
    	 		String name = ParseUtil.getFirstToken(arguments, pos);
			Listable[] charRatesModelArray =  getProject().getFileElements(CharRatesModel.class);
			int i = ListableVector.indexOf(charRatesModelArray, name);
			if (i<0)
				return null;
			setCharRatesModel((CharRatesModel)charRatesModelArray[i]);
			initializeCharRatesModel();
		}
    	 	else if (checker.compare(this.getClass(), "Sets the rate matrix model", "[name of model]", commandName, "setRateMatrixModel")) {
    	 		pos.setValue(0);
    	 		String name = ParseUtil.getFirstToken(arguments, pos);
			rateMatrixModelArray =  getProject().getFileElements(RateMatrixCatModel.class);
			int i = ListableVector.indexOf(rateMatrixModelArray, name);
			if (i<0)
				return null;
			setRateMatrixModel((RateMatrixCatModel)rateMatrixModelArray[i]);
			initializeRateMatrixModel();
			
		}
    	 	else if (checker.compare(this.getClass(), "Sets the scaling factor (overall rate)", "[number]", commandName, "setScalingFactor")) {
    	 		pos.setValue(0);
    	 		double s = MesquiteDouble.fromString(arguments, pos);
			if (MesquiteDouble.isCombinable(s))
				setScalingFactor(s);
		}
    	 	else
 			return  super.doCommand(commandName, arguments, checker);
		return null;
 	}
	
	public String getNexusSpecification(){
		
		Snapshot snap = getSnapshot(getFile());
		String spec = "";
		for (int i=0; i<snap.getNumLines(); i++)
			spec += snap.getLine(i) + " ";
		return ParseUtil.tokenize(spec);
	}
	public void fromString(String description, MesquiteInteger stringPos, int format){
		Puppeteer p = new Puppeteer(MesquiteTrunk.mesquiteTrunk);
		String commands = ParseUtil.getToken(description, stringPos);
		MesquiteInteger pos = new MesquiteInteger(0);
		MesquiteModule.incrementMenuResetSuppression();	
		p.sendCommands(this, commands, pos, null, false,null, CommandChecker.defaultChecker);
		MesquiteModule.decrementMenuResetSuppression();	
	}
	/*.................................................................................................................*/
	/** returns name of model class (e.g. "stepmatrix")*/
	public String getNEXUSClassName() {
		return "DNASimulationModel";
	}
  	/*.................................................................................................................*
	public double priorProbability (int state){
		if (!inStates(state)) 
			return 0;
		else
			return (1.0/(4));  // flat prior
	}
  	/*.................................................................................................................*/
	private Listable[]  filterOutNeedingEmpirical(Listable[] array){
		int count = 0;
		for (int i=0; i<array.length; i++)
			if (!((ProbSubModel)array[i]).needsEmpirical())
				count++;
		Listable [] filtered = new Listable[count];
		count = 0;
		for (int i=0; i<array.length; i++)
			if (!((ProbSubModel)array[i]).needsEmpirical()) {
				filtered[count] = array[i];
				count++;
			}
		return filtered;
	}
	/*.................................................................................................................*/
	public Choice addSubmodelPopup(ExtensibleDialog dialog, ProbSubModel probSubModel, Listable[] array, String prompt) {
		int index  = 0;
		if (probSubModel != null)
			index = ListableVector.indexOf(array, probSubModel.getName());
		if (index <0)
			index = 0;
		return dialog.addPopUpMenu(prompt,array,index);
	}
	/*.................................................................................................................*/
	public void arrayToChoice(Choice choice, Listable[] array, int current) {
		choice.removeAll();
		for (int i=0; i<array.length; i++) {
			choice.add(array[i].getName());
			
		}
		if (current < array.length)
			choice.select(current);
	}
	/*.................................................................................................................*/
	public void checkEditButton(Choice choice,Listable[] modelArray, Button editButton) {
	 		ProbSubModel model = (ProbSubModel)modelArray[choice.getSelectedIndex()];
	 		editButton.setEnabled(!model.isBuiltIn());
	}
	/*.................................................................................................................*/
	public void buildAllChoices(Class modelClass) {
		int current = 0;
		current = rootStatesModelChoice.getSelectedIndex();
		rootStatesModelArray =  getProject().getFileElements(StateFreqModel.class);
		arrayToChoice(rootStatesModelChoice,rootStatesModelArray,current);
		rootStatesEditListener.setArray(rootStatesModelArray);

		current = equilibriumStatesModelChoice.getSelectedIndex();
		equilibriumStatesModelArray =  getProject().getFileElements(StateFreqModel.class);
		arrayToChoice(equilibriumStatesModelChoice,equilibriumStatesModelArray,current);
		equilStatesEditListener.setArray(equilibriumStatesModelArray);

		if (modelClass==StateFreqModel.class) {
			rootStatesModelChoice.select(rootStatesModelChoice.getItemCount()-1);
			equilibriumStatesModelChoice.select(equilibriumStatesModelChoice.getItemCount()-1);
		}
		
		current = charRatesModelChoice.getSelectedIndex();
		charRatesModelArray =  getProject().getFileElements(CharRatesModel.class);
		arrayToChoice(charRatesModelChoice,charRatesModelArray,current);
		charRatesEditListener.setArray(charRatesModelArray);

		if (modelClass==CharRatesModel.class) 
			charRatesModelChoice.select(charRatesModelChoice.getItemCount()-1);
		
		current = rateMatrixModelChoice.getSelectedIndex();
		rateMatrixModelArray =  getProject().getFileElements(RateMatrixCatModel.class);
		arrayToChoice(rateMatrixModelChoice,rateMatrixModelArray,current);
		rateMatrixEditListener.setArray(rateMatrixModelArray);

		if (modelClass==RateMatrixCatModel.class) 
			rateMatrixModelChoice.select(rateMatrixModelChoice.getItemCount()-1);
			
		checkEditButton(rootStatesModelChoice, rootStatesModelArray, editRootStatesButton);
		checkEditButton(equilibriumStatesModelChoice, equilibriumStatesModelArray, editEquilStatesButton);
		checkEditButton(charRatesModelChoice, charRatesModelArray, editCharRatesButton);
		checkEditButton(rateMatrixModelChoice, rateMatrixModelArray, editRateMatrixButton);

	}
	/*.................................................................................................................*/
	public Choice addSubmodelCuratorsPopup(ExtensibleDialog dialog, Class modelClass, String prompt) {
		int index  = 0;
	 	MesquiteModule[] curators = null;
		curators = CharacterModel.findCurators(curator, modelClass);
		if ((curator!=null) && (curators.length > 0)) {
			String[] curatorsList = new String[curators.length];
			for (int i=0; i<curators.length; i++) {
				if (curators[i] instanceof EditingCurator)
					curatorsList[i] = curators[i].getNameForMenuItem();
				else
					curatorsList[i] = "";
			}
			return dialog.addPopUpMenu(prompt,curatorsList,0);
		}
		return null;
	}
	/*.................................................................................................................*
	public rebuildChoicePopup(ExtensibleDialog dialog, ProbSubModel probSubModel, Listable[] array) {
		int index;
		index = 0;
		if (probSubModel != null)
			index = ListableVector.indexOf(array, probSubModel.getName());
		if (index <0)
			index = 0;
		return dialog.addPopUpMenu(prompt,array,index);
	}
	/*.................................................................................................................*/
	public boolean initialize(String message) { 
		notify = false;
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(MesquiteTrunk.mesquiteTrunk.containerOfModule(), message,buttonPressed);
		//dialog.setExplainable(this);
		dialog.addLabel(message);
		
		
		rootStatesModelArray =  getProject().getFileElements(StateFreqModel.class);
		equilibriumStatesModelArray =  getProject().getFileElements(StateFreqModel.class);
		charRatesModelArray =  getProject().getFileElements(CharRatesModel.class);
		rateMatrixModelArray =  getProject().getFileElements(RateMatrixCatModel.class);
/*		if (getCharacterData()==null){
			rootStatesModelArray =  filterOutNeedingEmpirical(rootStatesModelArray);
			equilibriumStatesModelArray =  filterOutNeedingEmpirical(equilibriumStatesModelArray);
			charRatesModelArray =  filterOutNeedingEmpirical(charRatesModelArray);
			rateMatrixModelArray =  filterOutNeedingEmpirical(rateMatrixModelArray);
			
		}
*/		


		rootStatesModelChoice = addSubmodelPopup(dialog, (ProbSubModel)getRootStatesModel(), rootStatesModelArray, "root states model:");
		dialog.suppressNewPanel();
		Panel rootStatesButtonPanel = dialog.addNewDialogPanel();
		editRootStatesButton = dialog.addAButton("Edit...",rootStatesButtonPanel);

		equilibriumStatesModelChoice = addSubmodelPopup(dialog, (ProbSubModel)getEquilStatesModel(),equilibriumStatesModelArray,"equilibrium states model:");
		dialog.suppressNewPanel();
		Panel equilStatesButtonPanel = dialog.addNewDialogPanel();
		 editEquilStatesButton = dialog.addAButton("Edit...",equilStatesButtonPanel);

		equilStatesEditListener  = new ProbSubmodelEdit(this,editEquilStatesButton, equilibriumStatesModelChoice,equilibriumStatesModelArray,  getEquilStatesModel(), StateFreqModel.class,  curator);
		rootStatesEditListener = new ProbSubmodelEdit(this,editRootStatesButton, rootStatesModelChoice, rootStatesModelArray, getRootStatesModel(), StateFreqModel.class,  curator);
		Choice newEquilStatesModelChoice = addSubmodelCuratorsPopup(dialog, StateFreqModel.class, "New:");
		editEquilStatesButton.addActionListener(equilStatesEditListener);
		editEquilStatesButton.setActionCommand("EditSubModel");
	 	StateFreqModel selectedStateFreqModel = ((StateFreqModel)equilibriumStatesModelArray[equilibriumStatesModelChoice.getSelectedIndex()]);
	 	if (selectedStateFreqModel != null)
	 		editEquilStatesButton.setEnabled(!selectedStateFreqModel.isBuiltIn());
		equilibriumStatesModelChoice.addItemListener(equilStatesEditListener);
		newEquilStatesModelChoice.addItemListener(equilStatesEditListener);
		rootStatesModelChoice.addItemListener(rootStatesEditListener);

		editRootStatesButton.addActionListener(rootStatesEditListener);
		editRootStatesButton.setActionCommand("EditSubModel");
	 	selectedStateFreqModel = ((StateFreqModel)rootStatesModelArray[rootStatesModelChoice.getSelectedIndex()]);
	 	if (selectedStateFreqModel != null)
	 		editRootStatesButton.setEnabled(!selectedStateFreqModel.isBuiltIn());



		dialog.addHorizontalLine(1);
		charRatesModelChoice = addSubmodelPopup(dialog, (ProbSubModel)getCharRatesModel(),charRatesModelArray,"character rates model:");
		dialog.suppressNewPanel();
		Panel charRatesButtonPanel = dialog.addNewDialogPanel();
		editCharRatesButton = dialog.addAButton("Edit...",charRatesButtonPanel);
		Choice newCharRatesModelChoice = addSubmodelCuratorsPopup(dialog, CharRatesModel.class, "New:");
		charRatesEditListener = new ProbSubmodelEdit(this,editCharRatesButton, charRatesModelChoice, charRatesModelArray, getCharRatesModel(), CharRatesModel.class,  curator);
		editCharRatesButton.addActionListener(charRatesEditListener);
		editCharRatesButton.setActionCommand("EditSubModel");
	 	CharRatesModel selectedCharRatesModel = ((CharRatesModel)charRatesModelArray[charRatesModelChoice.getSelectedIndex()]);
	 	if (selectedCharRatesModel != null)
	 		editCharRatesButton.setEnabled(!selectedCharRatesModel.isBuiltIn());
		charRatesModelChoice.addItemListener(charRatesEditListener);
		newCharRatesModelChoice.addItemListener(charRatesEditListener);
		
		dialog.addHorizontalLine(1);
		rateMatrixModelChoice = addSubmodelPopup(dialog, (ProbSubModel)getRateMatrixModel(), rateMatrixModelArray, "rate matrix model:");
		dialog.suppressNewPanel();
		Panel rateMatrixButtonPanel = dialog.addNewDialogPanel();
		editRateMatrixButton = dialog.addAButton("Edit...",rateMatrixButtonPanel);
		Choice newRateMatrixModelChoice = addSubmodelCuratorsPopup(dialog, RateMatrixCatModel.class, "New:");
		rateMatrixEditListener = new ProbSubmodelEdit(this,editRateMatrixButton, rateMatrixModelChoice,rateMatrixModelArray, getRateMatrixModel(), RateMatrixCatModel.class, curator);
		editRateMatrixButton.addActionListener(rateMatrixEditListener);
		editRateMatrixButton.setActionCommand("EditSubModel");
	 	RateMatrixCatModel selectedRateMatrixModel = ((RateMatrixCatModel)rateMatrixModelArray[rateMatrixModelChoice.getSelectedIndex()]);
	 	if (selectedRateMatrixModel != null)
	 		editRateMatrixButton.setEnabled(!selectedRateMatrixModel.isBuiltIn());
		rateMatrixModelChoice.addItemListener(rateMatrixEditListener);
		newRateMatrixModelChoice.addItemListener(rateMatrixEditListener);
		dialog.addHorizontalLine(1);

		
		double scale = getScalingFactor();
		DoubleField scalingFactorField = dialog.addDoubleField("scaling factor:",scale, 8);

		String s = "This is a user-defined model of nucleotide evolution.  Choose one of each of the four submodels: (1) the states at the root of the tree, (2) the equilibrium frequencies of states on other branches, (3) the relative rates of characters, and (4) the relative rates of change from state to state.";
		s += " You can edit an existing submodel by selecting it in the drop-down menu, and then clicking on the Edit... button.";
		s += " You can create a new submodel by choosing the appropriate type of submodel from the drop-down menu next to \"New\".";
		dialog.appendToHelpString(s);

		dialog.completeAndShowDialog(true);

		if (buttonPressed.getValue()==0) {
			setRootStatesModel((StateFreqModel)rootStatesModelArray[rootStatesModelChoice.getSelectedIndex()]);
			setEquilStatesModel((StateFreqModel)equilibriumStatesModelArray[equilibriumStatesModelChoice.getSelectedIndex()]);
			setCharRatesModel((CharRatesModel)charRatesModelArray[charRatesModelChoice.getSelectedIndex()]);
			setRateMatrixModel( (RateMatrixCatModel)rateMatrixModelArray[rateMatrixModelChoice.getSelectedIndex()]);
			
			scale = scalingFactorField.getValue();
			setScalingFactor(scale);

			initializeSubModels();
			setEditCancel(false);
		}
		else
			setEditCancel(true);
		setSeed(originalSeed);
		dialog.dispose();
		notify = true;
		notifyListeners(this, new Notification(MesquiteListener.UNKNOWN));

		return (buttonPressed.getValue()==0);

	}
	/*.................................................................................................................*/
	public boolean isOneOfMine(ProbSubModel submodel){
		return submodel ==getOriginalRootStatesModel() || submodel ==getOriginalEquilStatesModel() || submodel ==getOriginalCharRatesModel() || submodel ==getOriginalRateMatrixModel();
	}
	/*.................................................................................................................*/
	public void resetFromOriginalSubmodels(){
		setRootStatesModel(getOriginalRootStatesModel());
		setEquilStatesModel(getOriginalEquilStatesModel());
		setCharRatesModel(getOriginalCharRatesModel());
		setRateMatrixModel(getOriginalRateMatrixModel());

		initializeSubModels();
	}
 	/*.................................................................................................................*/
	public CharacterModel cloneModelWithMotherLink(CharacterModel formerClone){
		SimulationDNAModel model = new SimulationDNAModel(name, getStateClass(), getCurator()); 
		completeDaughterClone(formerClone, model);
		return model;
	}
 	/* copy information from this to model passed (used in cloneModelWithMotherLink to ensure that superclass info is copied); should call super.copyToClone(pm) */
	public void copyToClone(CharacterModel pm){
		if (pm == null || !(pm instanceof SimulationDNAModel))
			return;
		((SimulationDNAModel)pm).hasDefaultValues = hasDefaultValues;

		super.copyToClone(pm);
	}
 	/*.................................................................................................................*/
	/** returns parameters of the model. */
	public String getParameters (){
		String param = "";
		if (getRootStatesModel()!=null)
			param += "        Root states model (" + getRootStatesModel().getName() + "): "+ getRootStatesModel().getParameters()+StringUtil.lineEnding();
		if (getEquilStatesModel()!=null)
			param += "        Equilibrium states model (" + getEquilStatesModel().getName() + "): "+ getEquilStatesModel().getParameters()+StringUtil.lineEnding();
		if (getCharRatesModel()!=null)
			param += "        Character rates model (" + getCharRatesModel().getName() + "): "+ getCharRatesModel().getParameters()+StringUtil.lineEnding();
		if (getRateMatrixModel()!=null)
			param += "        Rate matrix model (" + getRateMatrixModel().getName() + "): "+ getRateMatrixModel().getParameters()+StringUtil.lineEnding();
		return param;
	}
	/* --------------------------------------------- */
	public MesquiteNumber[] getParameterValues() {  //fill in when estimation is done
		return null;
	}
 	/*.................................................................................................................*/
	/** return an explanation of the model. */
	public String getExplanation (){
		return "This is a user-defined model of nucleotide evolution.  It has four submodels: the states at the root of the tree, the equilibrium frequencies of states on other branches, the relative rates of characters, and the relative rates of change from state to state.";
	}
}

 class ProbSubmodelEdit implements ActionListener, ItemListener{
 	Choice editChoice;
 	ProbSubModel subModel;
 	 CategProbModelCurator curator;
 	 Listable[] modelArray;
 	 Class modelClass;
 	 Button editButton;
 	 SimulationDNAModel generalModel;
 	
	/*.................................................................................................................*/
	public ProbSubmodelEdit(SimulationDNAModel generalModel, Button editButton, Choice editChoice, Listable[] modelArray, ProbSubModel subModel, Class modelClass,  CategProbModelCurator curator) {
		this.subModel = subModel;
		this.generalModel = generalModel;
		this.editChoice = editChoice;
		this.curator = curator;
		this.modelArray = modelArray;
		this.modelClass = modelClass;
		this.editButton = editButton;
	}
	/*.................................................................................................................*/
	 public  void setArray( Listable[] array) {
	 	this.modelArray = array;
	 }
	/*.................................................................................................................*/
	 public  void itemStateChanged(ItemEvent e) {
	 	if (e.getItemSelectable() == editChoice) {    // the choice for choosing among existing models was chosen
	 		ProbSubModel model = (ProbSubModel)modelArray[editChoice.getSelectedIndex()];
	 		editButton.setEnabled(!model.isBuiltIn());
	 	}
	 	else if (e.getStateChange()==e.SELECTED) {   // the New model choice was chosen
		 	MesquiteModule[] curators = null;
			curators = CharacterModel.findCurators(curator, modelClass);
			if ((curator!=null) && (curators.length > 0)) {
				int modelCurator = -1;
				for (int i = 0; i < curators.length; i++) {
					if (curators[i].getNameForMenuItem() == e.getItem())
						modelCurator = i;
				}
				if (modelCurator >=0) {
					CharacterModel newModel = (CharacterModel)((CharModelCurator)curators[modelCurator]).doCommand("newModel", "Model", CommandChecker.defaultChecker);
	 				if (newModel != null) {
	 					generalModel.buildAllChoices(modelClass);
	 				}
	 			}
	 		}
	 	}
	 }
	/*.................................................................................................................*/
	 public  void actionPerformed(ActionEvent e) {
	 	ProbSubModel model = (ProbSubModel)modelArray[editChoice.getSelectedIndex()];
	 	MesquiteModule[] curators = null;
		curators = CharacterModel.findCurators(curator, model.getClass());
		if ((curators!=null) && (curators.length > 0)) {
	 		if (e.getActionCommand() == "EditSubModel") {
				if (!model.isBuiltIn())
					((CharModelCurator)curators[0]).showEditor(model);
			}
		}
	 }
}


