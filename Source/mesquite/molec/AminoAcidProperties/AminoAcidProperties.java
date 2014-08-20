/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.molec.AminoAcidProperties; 

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.categ.lib.*;
import mesquite.molec.lib.*;


/* ======================================================================== */
public class AminoAcidProperties extends NumberForCharAndTaxon {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(AAProperty.class, getName() + "  needs to choose a particular amino acid property to report.",
		"The amino acid property to report can be selected initially or in the Amino Acid Property submenu");
	}
	/*.................................................................................................................*/
	AAProperty propertyTask;
	MesquiteString propertyTaskName;
	static String ptName = null;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
 		if (!StringUtil.blank(ptName)) //remember last one used
			propertyTask = (AAProperty)hireNamedEmployee(AAProperty.class, ptName);
		if (propertyTask == null)
			propertyTask = (AAProperty)hireEmployee(AAProperty.class, "Property of Amino Acid");
		if (propertyTask == null)
			return sorry(getName() + " couldn't start because no properties module was obtained.");
 		propertyTaskName = new MesquiteString();
 		ptName = "#" +propertyTask.getShortClassName();
		if (numCompatibleModulesAvailable(AAProperty.class, condition, this)>0) {
			MesquiteSubmenuSpec mss = addSubmenu(null, "Amino Acid Property", makeCommand("setProperty", this), AAProperty.class);
 			mss.setSelected(propertyTaskName);
		}
		return true;
	}
	public void initialize(CharacterData data){
	}
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) { 
   	 	Snapshot temp = new Snapshot();
 	 	temp.addLine("setProperty ", propertyTask); 
  	 	return temp;
  	 }
  	 MesquiteInteger pos = new MesquiteInteger(0);
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Sets the module calculating the property", "[name of module]", commandName, "setProperty")) {
    	 		AAProperty temp =   (AAProperty)replaceEmployee(AAProperty.class, arguments, "Property of Amino Acid", propertyTask);
 			if (temp!=null) {
 				propertyTask = temp;
				propertyTaskName.setValue(propertyTask.getName());
 				ptName = "#" +propertyTask.getShortClassName();
				parametersChanged();
	 			return propertyTask;
 			}
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
    	 	return null;
    	 }
    	 

	public void calculateNumber(CharacterData data, int ic, int it, MesquiteNumber result, MesquiteString resultString){
   	 	if (result == null || propertyTask == null)
   	 		return;
	   	clearResultAndLastResult(result);
  	 	if (data == null || !(data instanceof ProteinData))
   	 		return;
   	 	CategoricalData dData = (CategoricalData)data;
		long s = dData.getState(ic, it);

		double loc = 0;
		int ns = 0;
		for (int is=0; is<=ProteinState.maxProteinState; is++){
			if (CategoricalState.isElement(s, is)) {
				double d = propertyTask.getProperty(is);
				if (MesquiteDouble.isCombinable(d)){
					loc += d;
					ns++;
				}
			}
		}
		if (ns!=0)
			result.setValue(loc/ns);
		saveLastResult(result);
		saveLastResultString(resultString);
  	 }

	/*.................................................................................................................*/
    	 public boolean isPrerelease() {
		return false;
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Amino Acid Properties";
   	 }
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Returns property of amino acid";
   	 }
	/*.................................................................................................................*/
	public CompatibilityTest getCompatibilityTest(){
		return new RequiresProteinData();
	}
}


	


