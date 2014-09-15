/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.stochchar.AsymmModelCurator;
/*~~  */

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import mesquite.stochchar.lib.*;
/* ======================================================================== */
public class AsymmModelCurator extends CategProbModelCurator  implements EditingCurator, CuratorWithSettings {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		
		 EmployeeNeed e = registerEmployeeNeed(WindowHolder.class, getName() + " needs assistance to hold a window" ,
				 "This is arranged automatically");
	}
	MesquiteBoolean rbNotation; //, flatPrior;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		rbNotation = new MesquiteBoolean(true);
	//	flatPrior = new MesquiteBoolean(false);
		return true;
  	 }
  	 public void endJob(){
		if (getProject()!=null && getProject().getCentralModelListener() !=null)
			getProject().getCentralModelListener().removeListener(this);
  	 	super.endJob();
  	 }
  	 
  	 public boolean isPrerelease(){
  		 return true;
  	 }
	/*.................................................................................................................*/
	public void processPreferencesFromFile (String[] prefs) {
		if (prefs!=null && prefs.length>0) {
			int mode = MesquiteInteger.fromString(prefs[0]);
			if (MesquiteInteger.isCombinable(mode))
				AsymmModel.optimizationMode = mode;
		}
	}
	
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer();
		StringUtil.appendXMLTag(buffer, 2, "optimizationMode", AsymmModel.optimizationMode);   
		return buffer.toString();
	}
	
	public void processSingleXMLPreference (String tag, String content) {
		 if ("optimizationMode".equalsIgnoreCase(tag))
			AsymmModel.optimizationMode = MesquiteInteger.fromString(content);
	}

	
	/*.................................................................................................................*/
  	 public void projectEstablished(){
		getProject().getCentralModelListener().addListener(this);
		 AsymmModel model;
 		model = new AsymmModel("Asymm. 2 param. (estimate)", CategoricalState.class);
    		model.setBuiltIn(true);
    		model.addToFile(null, getProject(), null);
    		super.projectEstablished();
  	 }
	/*.................................................................................................................*/
	/** passes which object changed, along with optional code number (type of change) and integers (e.g. which character)*/
	public void changed(Object caller, Object obj, Notification notification){
		if (obj instanceof AsymmModel){
	 		AsymmModel model = (AsymmModel)obj;
	 		int i = getModelNumber(model);
	 		if (i>=0) {
	 			AsymmModelEditor dsw = (AsymmModelEditor)getWindow(i);
	 			if (dsw==null)
	 				return;
	 			
	 			dsw.setAllowEstimation(model instanceof CModelEstimator);
				if (!model.getUseRateBiasNotation()){
					dsw.setText(1, "Forward");
					dsw.setText(2, "Backward");
				}
				else {
					dsw.setText(1, "Bias");
					dsw.setText(2, "Rate");
				}
				dsw.setUseEquil(model.getUseEquilFreqAsPrior());
				dsw.setValue(1, model.getParam1());
				dsw.setValue(2, model.getParam0());
	 		}
		}
		super.changed(caller, obj, notification);
	}
	public void editSettings(){
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog settingsDialog = new ExtensibleDialog(containerOfModule(), "AsymmMk Optimization Settings",  buttonPressed);
		settingsDialog.addLargeOrSmallTextLabel("Settings for optimizing parameters in " + getName() + ". These settings are used when both parameters of the model are unspecified and need to be estimated.");
		RadioButtons rb = settingsDialog.addRadioButtons(new String[]{"Use estimated rate from Mk1 model as basis","Try even and two asymmetries","Try both of the above (Mk1 plus even and two asymmetries)"}, AsymmModel.optimizationMode);
		settingsDialog.completeAndShowDialog(true);
		boolean ok = (settingsDialog.query()==0);
		
		if (ok) {
			AsymmModel.optimizationMode = rb.getValue();
			
		}
		
		settingsDialog.dispose();
		if (ok)
			CentralModelListener.staticChanged(this, AsymmModel.class, new Notification());

	 	storePreferences();
	}
   
	/*.................................................................................................................*/
   	public MesquiteModule editModelNonModal(CharacterModel model, ObjectContainer w){
		if (model!=null && model instanceof AsymmModel) {
	   		AsymmModel modelToEdit =  ((AsymmModel)model);
			String param0Name, param1Name;
			if (modelToEdit.getUseRateBiasNotation()) {
				param0Name = "Rate";
				param1Name = "Bias";
				rbNotation.setValue(true);
			}
			else {
				param0Name = "Backward";
				param1Name = "Forward";
				rbNotation.setValue(false);
			}
			MesquiteModule windowServer = hireNamedEmployee(WindowHolder.class, "#WindowBabysitter");
			if (windowServer == null)
				return null;
			
			AsymmModelEditor dsw = new AsymmModelEditor(windowServer, "Edit model " + modelToEdit.getName(), param1Name, makeCommand("setParam1", modelToEdit), modelToEdit.getParam1(), 0, MesquiteDouble.infinite, 0, 1, param0Name, makeCommand("setParam0", modelToEdit), modelToEdit.getParam0(), 0, MesquiteDouble.infinite, 0, 1); 
			dsw.setPrior(modelToEdit.getUseEquilFreqAsPrior(), makeCommand("toggleEquilibPrior", modelToEdit));
			dsw.setAllowEstimation(modelToEdit instanceof CModelEstimator);
			dsw.useExponentialScale(true);
			windowServer.makeMenu("AsymmMk_Model");
		//	windowServer.addCheckMenuItem(null, "Use Flat Prior", makeCommand("toggleEquilibPrior",  (Commandable)modelToEdit), flatPrior);
		//	flatPrior.setValue(!modelToEdit.getUseEquilFreqAsPrior());
			windowServer.addCheckMenuItem(null, "Use Rate-Bias Notation", makeCommand("toggleNotation",  (Commandable)modelToEdit), rbNotation);
		//as of 1. 1, can be binary only	windowServer.addMenuItem("Set Maximum Allowed State...", makeCommand("setMaxState",  modelToEdit));
			MesquiteWindow.centerWindow(dsw);
			if (w!=null)
				w.setObject(dsw);
			
			return windowServer;
		}
		return null;
   	}
	/*.................................................................................................................*/
 		/*if (modal && false){
			MesquiteDouble r = new MesquiteDouble(modelToEdit.getParam0());
			MesquiteDouble b = new MesquiteDouble(modelToEdit.getParam1());
			if (MesquiteDouble.queryTwoDoubles(containerOfModule(), "Asymmetrical model", "Set " + param0Name, r, "Set " + param1Name, b)){
				if (r.getValue()>0 && b.getValue()>0) {
					modelToEdit.setParam0(r.getValue());
					modelToEdit.setParam1(b.getValue());
				}
			}
		}
		 */
		
	public boolean curatesModelClass(Class modelClass){
		return AsymmModel.class.isAssignableFrom(modelClass);
	}
	/*.................................................................................................................*/
	public String getNameOfModelClass() {
		return "Asymmetrical 2-param. Markov-k Model";
	}
	/*.................................................................................................................*/
	public String getNEXUSNameOfModelClass() {
		return "AsymmMk";
	}
	/*.................................................................................................................*/
	public Class getModelClass() {
		return AsymmModel.class;
	}
	/*.................................................................................................................*/
   	public CharacterModel makeNewModel(String name) {
 		AsymmModel model = new AsymmModel(name, CategoricalState.class);
 		model.setMaxStateDefined(1);
       		return model;
   	}
	/*.................................................................................................................*/
   	public CharacterModel readCharacterModel(String name, MesquiteInteger stringPos, String description, int format) {
 		AsymmModel model = new AsymmModel( name, CategoricalState.class);
   		model.fromString(description, stringPos, format);
  		return model;
   	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Asymmetrical 2-param. Markov-k Model";
   	 }
   	 
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Defines and maintains simple Markov k-state asymmetrical 2-parameter stochastic models of character evolution." ;
   	 }
 	
 	
}

class AsymmModelEditor extends DoubleSliderWindow implements ItemListener {
	Checkbox useEquilFreq,useFlat;
	CheckboxGroup cbg;
	MesquiteCommand setPriorCommand;
	Label label;
	int topLabel = 100;
	public AsymmModelEditor(MesquiteModule module, String title, String name1, MesquiteCommand command1, double initialValue1, double min1, double max1,  double minSweet1, double maxSweet1, String name2, MesquiteCommand command2, double initialValue2, double min2, double max2, double minSweet2, double maxSweet2) {
		super(module, title, name1, command1, initialValue1, min1, max1,   minSweet1,  maxSweet1,  name2,  command2,  initialValue2,  min2,  max2,  minSweet2,  maxSweet2);
		cbg = new CheckboxGroup();
		label = new Label("Calculate Likelihoods and Simulate assuming:");
		addToWindow(label);
		addToWindow(useEquilFreq = new Checkbox("Root State Frequencies same as Equilibrium", cbg, false));
		addToWindow(useFlat = new Checkbox("Root State Frequencies Equal", cbg, true));
		topLabel = getHeight() - 94;
		sliderArea.setBounds(0, 10, getBounds().width, topLabel - 10);
		label.setBounds(0, topLabel, getBounds().width, 24);
		useEquilFreq.setBounds(10, topLabel + 24, getBounds().width, 30);
		useEquilFreq.addItemListener(this);
		useFlat.setBounds(10, topLabel + 54, getBounds().width, 30);
		useFlat.addItemListener(this);
	//	useEquilFreq.setBackground(Color.green);
	}
	
	/*.................................................................................................................*/
	public void windowResized(){
		super.windowResized();
		topLabel = getHeight() - 94;
		if (sliderArea != null) {
			sliderArea.setBounds(0, 10, getBounds().width, topLabel - 10);
			label.setBounds(10, topLabel, getBounds().width, 20);
			useEquilFreq.setBounds(16, topLabel + 16, getBounds().width, 24);
			useFlat.setBounds(16, topLabel + 42, getBounds().width, 24);
		}
	}
	void setPrior(boolean useEquil, MesquiteCommand setPriorCommand){
		useEquilFreq.setState(useEquil);
		this.setPriorCommand= setPriorCommand;
	}
	void setUseEquil(boolean useEquil){
		useEquilFreq.setState(useEquil);
	}

	public void itemStateChanged(ItemEvent e){
		if (setPriorCommand == null)
			return;
		if (e.getSource() == useEquilFreq){
			if (useEquilFreq.getState())
				setPriorCommand.doItMainThread("on", null, this);
			else
				setPriorCommand.doItMainThread("off", null, this);
		}
		else if (e.getSource() == useFlat){
			if (useFlat.getState())
				setPriorCommand.doItMainThread("off", null, this);
			else
				setPriorCommand.doItMainThread("on", null, this);
		}
	}
}

