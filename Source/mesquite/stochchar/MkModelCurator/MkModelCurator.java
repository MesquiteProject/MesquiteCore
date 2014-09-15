/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.stochchar.MkModelCurator;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import mesquite.stochchar.lib.*;
/* ======================================================================== */
public class MkModelCurator extends CategProbModelCurator implements EditingCurator, CuratorWithSettings  {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		
		 EmployeeNeed e = registerEmployeeNeed(WindowHolder.class, getName() + " needs assistance to hold a window" ,
				 "This is arranged automatically");
	}
		 MkModel defaultModel;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
 		defaultModel = new MkModel("Mk1 (est.)", CategoricalState.class);
    		defaultModel.setBuiltIn(true);
		CategoricalData.registerDefaultModel("Likelihood", defaultModel.getName());
		return true;
  	 }
	/*.................................................................................................................*/
  	 public void projectEstablished(){
    		defaultModel.addToFile(null, getProject(), null);
    		super.projectEstablished();
  	 }
   
	/*.................................................................................................................*/
	public void processPreferencesFromFile (String[] prefs) {
		if (prefs!=null && prefs.length>0) {
			double mode = MesquiteDouble.fromString(prefs[0]);
			if (MesquiteDouble.isCombinable(mode))
				MkModel.optWidth = mode;
		}
	}
	
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer();
		StringUtil.appendXMLTag(buffer, 2, "optWidth", MkModel.optWidth);  
		return buffer.toString();
	}
	
	public void processSingleXMLPreference (String tag, String content) {
		if ("optWidth".equalsIgnoreCase(tag))
			MkModel.optWidth = MesquiteDouble.fromString(content);
	}


	/*.................................................................................................................*/
	/** passes which object changed, along with optional code number (type of change) and integers (e.g. which character)*/
	public void changed(Object caller, Object obj, Notification notification){
		if (obj instanceof MkModel){
	 		MkModel model = (MkModel)obj;
	 		int i = getModelNumber(model);
	 		if (i>=0) {
	 			SliderWindow dsw = (SliderWindow)getWindow(i);
	 			if (dsw==null)
	 				return;
	 			dsw.setAllowEstimation(model instanceof CModelEstimator);
				dsw.setText(model.getName() + " (max.State: " + model.getMaxState() + ")");
				dsw.getSlider().setCurrentValue(model.getInstantaneousRate());
	 		}
		}
		super.changed(caller, obj, notification);
	}
	/*.................................................................................................................*/
   	public MesquiteModule editModelNonModal(CharacterModel model, ObjectContainer w){
		if (model!=null && model instanceof MkModel) {
	   		MkModel modelToEdit =  ((MkModel)model);
			double a = modelToEdit.getInstantaneousRate();
			
			MesquiteModule windowServer = hireNamedEmployee(WindowHolder.class, "#WindowBabysitter");
			if (windowServer == null)
				return null;
			SliderWindow sw = new SliderWindow(windowServer, "Edit model", modelToEdit.getName() + " (max.State: " + ((MkModel)modelToEdit).getMaxState() + ")", makeCommand("setRate", modelToEdit), modelToEdit.getInstantaneousRate(), 0, MesquiteDouble.infinite,0, 1); //should allow more than one????
			windowServer.setModuleWindow(sw);
			windowServer.makeMenu("Mk1_Model");
			windowServer.addMenuItem("Set Maximum Allowed State...", makeCommand("setMaxState",  modelToEdit));
			sw.useExponentialScale(true);
			sw.setAllowEstimation(modelToEdit instanceof CModelEstimator);
			
			//setModuleWindow(new JCEditor(this, modelToEdit)); //should allow more than one????
			MesquiteWindow.centerWindow(sw);
			if (w!=null)
				w.setObject(sw);
				
			return windowServer;
		}
		return this;
   	}
		/*if (modal){
			a = MesquiteDouble.queryDouble(containerOfModule(), "Markov k-state model rate", "Set rate of Markov k-state model", a);
			modelToEdit.setInstantaneousRate(a);
		}
		else */

	public void editSettings(){
		double d = MesquiteDouble.queryDouble(containerOfModule(), "Mk1 Optimization Settings", "By default, optimization of parameter of Mk1 model surveys across intervals of width 1, then of width 10, then chooses best result.  Otherwise, you can indicate a single width to survey.  Wider interval may mean finding optimum more quickly when rates are high, but may make less accurate when rates are low.  Suggested: 1.0 to 20.0.  To use the default method, enter 0.0", MkModel.optWidth, 0, 100);
		if (MesquiteDouble.isCombinable(d)) {
			MkModel.optWidth = d;
			Notification nn = null;
			CentralModelListener.staticChanged(this, MkModel.class, nn = new Notification());
			CentralModelListener.staticChanged(this, AsymmModel.class, nn); //temporary; while AsymmModel might use MkModel.optWidth
		 	storePreferences();
		}
		
	}
	public boolean curatesModelClass(Class modelClass){
		return MkModel.class.isAssignableFrom(modelClass);
	}
	/*.................................................................................................................*/
	public String getNameOfModelClass() {
		return "Mk1 (Markov 1 parameter)";
	}
	/*.................................................................................................................*/
	public String getNEXUSNameOfModelClass() {
		return "Mk1";
	}
	/*.................................................................................................................*/
	public Class getModelClass() {
		return MkModel.class;
	}
	/*.................................................................................................................*/
   	public CharacterModel makeNewModel(String name) {
 		MkModel model = new MkModel(name, CategoricalState.class);
 		model.setMaxStateDefined(1);
       		return model;
   	}
	/*.................................................................................................................*/
   	public CharacterModel readCharacterModel(String name, MesquiteInteger stringPos, String description, int format) {
 		MkModel model = new MkModel( name, CategoricalState.class);
   		model.fromString(description, stringPos, format);
  		return model;
   	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Mk1 Model (Markov 1 parameter)";
   	 }
   	 
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Defines and maintains simple Markov k-state 1-parameter stochastic models (Lewis, 2001) of character evolution." ;
   	 }
}


