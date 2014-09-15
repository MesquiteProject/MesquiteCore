/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.stochchar.Brownian;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.cont.lib.*;
import mesquite.stochchar.lib.*;

/* ======================================================================== */
public class Brownian extends WholeCharModelCurator implements EditingCurator  {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		 EmployeeNeed e = registerEmployeeNeed(WindowHolder.class, getName() + " needs assistance to hold a window" ,
				 "This is arranged automatically");
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		return true;
	}
	
	
  	 public void projectEstablished(){
 		BrownianMotionModel brownianModel = new BrownianMotionModel("Brownian default", ContinuousState.class);
    		brownianModel.setRate(1.0);
    		brownianModel.setBuiltIn(true);
    		brownianModel.addToFile(null, getProject(), null);
		ContinuousData.registerDefaultModel("Likelihood", "Brownian default");
    		super.projectEstablished();
  	 }
	/*.................................................................................................................*/
   	public MesquiteModule editModelNonModal(CharacterModel model, ObjectContainer w){
		if (model!=null && model instanceof BrownianMotionModel) {
	   		BrownianMotionModel modelToEdit =  ((BrownianMotionModel)model);
			double a = modelToEdit.getRate();
			MesquiteModule windowServer = hireNamedEmployee(WindowHolder.class, "#WindowBabysitter");
			if (windowServer == null)
				return null;
			SliderWindow sw = new SliderWindow(windowServer, "Edit model", "Model \"" + modelToEdit.getName() + "\"", makeCommand("setRate", modelToEdit), modelToEdit.getRate(),0.0, MesquiteDouble.infinite, 1.0, 5.0);  //MesquiteDouble.infinite
			sw.setAllowEstimation(modelToEdit instanceof CModelEstimator);
			windowServer.makeMenu("Brownian");
			MesquiteWindow.centerWindow(sw);
			if (w!=null)
				w.setObject(sw);
			return windowServer;
		}
		return null;
   	}
   	
   	
   	
			/*if (modal){
				a = MesquiteDouble.queryDouble(containerOfModule(), "Jukes Cantor rate", "Set rate of Jukes Cantor model", a);
				modelToEdit.setRate(a);
			}
			else */

	/*.................................................................................................................*/
	public boolean curatesModelClass(Class modelClass){
		return BrownianMotionModel.class.isAssignableFrom(modelClass);
	}
	/*.................................................................................................................*/
	public String getNameOfModelClass() {
		return "Brownian Motion";
	}
	/*.................................................................................................................*/
	public String getNEXUSNameOfModelClass() {
		return "Brownian";
	}
	/*.................................................................................................................*/
	public Class getModelClass() {
		return BrownianMotionModel.class;
	}
	/*.................................................................................................................*/
   	public CharacterModel makeNewModel(String name) {
 		BrownianMotionModel brownianModel = new BrownianMotionModel(name, ContinuousState.class);
    		brownianModel.setRate(1.0);
       		return brownianModel;
   	}
	/*.................................................................................................................*/
   	public CharacterModel readCharacterModel(String name, MesquiteInteger stringPos, String description, int format) {
 		BrownianMotionModel brownianModel = new BrownianMotionModel(name, ContinuousState.class);
   		ParseUtil.getToken(description, stringPos);
   		double a =  MesquiteDouble.fromString(ParseUtil.getToken(description, stringPos));
 		if (a>=0 && MesquiteDouble.isCombinable(a)) {
 			brownianModel.setRate(a);
 		}
  		return brownianModel;
   	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Brownian Model";
   	 }
	/*.................................................................................................................*/
    	 public String getNameForMenuItem() {
		return "Brownian Model...";
   	 }
   	 
	/*.................................................................................................................*/
   	 
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Initializes Brownian motion character model for likelihood and other probability calculations." ;
   	 }
}




