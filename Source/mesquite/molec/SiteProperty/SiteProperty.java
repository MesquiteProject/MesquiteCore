/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.molec.SiteProperty; 
/*~~  */
import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.categ.lib.*;
import mesquite.cont.lib.ContinuousStateTest;
import mesquite.molec.lib.*;


public class SiteProperty extends NumberForCharacter implements NumForCharTreeIndep {   
	boolean warnedOnce = false;
	CategoricalDistribution cStates;
	AAProperty propertyTask;
	MesquiteString propertyTaskName;
	/*.................................................................................................................*/
	/** a method called when the module is started up.  You can put initialization stuff here*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (condition!=null && condition!= ProteinData.class && condition!=ProteinState.class)
			return sorry(getName() + " could not start because it works only with Protein data");
		propertyTask = (AAProperty)hireEmployee(AAProperty.class, "Property of Amino Acid");
		if (propertyTask == null)
			return sorry(getName() + " couldn't start because no properties module was obtained.");
 		propertyTaskName = new MesquiteString();
		if (numCompatibleModulesAvailable(AAProperty.class, condition, this)>0) {
			MesquiteSubmenuSpec mss = addSubmenu(null, "Amino Acid Property", makeCommand("setProperty", this), AAProperty.class);
 			mss.setSelected(propertyTaskName);
		}
 		return true;
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
				parametersChanged();
	 			return propertyTask;
 			}
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
    	 	return null;
    	 }
    	 
	public boolean requestPrimaryChoice(){
		return true;
	}
	/*.................................................................................................................*
  	 public CompatibilityTest getCompatibilityTest() {
  	 	return new ProteinStateTest();
  	 //	return new ProteinStateTest();
  	 }
   	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
   	public void initialize(CharacterDistribution charStates){
   	}
	public CompatibilityTest getCompatibilityTest(){
		return new RequiresProteinData();
	}
	/*.................................................................................................................*/
	/** the method that other modules call to tell this one to calculate the number for the character.  Note that the result is not immediately
	returned, but rather is remembered and returned when getNumber is called (this requires care in use!; perhaps should be redesigned)*/
	public  void calculateNumber(CharacterDistribution charStates, MesquiteNumber result, MesquiteString resultString) {
    	 	if (result==null)
    	 		return;
    	clearResultAndLastResult(result);
		if (charStates==null)
			result.setToUnassigned();
		else if  (!(charStates.getStateClass() == ProteinState.class)){
			if (!warnedOnce)
				discreetAlert( "Site properties can be calculated only for amino acid data");
			warnedOnce = true;
			result.setToUnassigned();
		}
		else {
			warnedOnce = false;
			cStates = (CategoricalDistribution)charStates;
			int numtaxa = cStates.getNumTaxa();
			double sum= 0.0;
			int n=0;
			for (int i=0; i<numtaxa; i++){
				long s = cStates.getState (i);
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
				if (ns!=0) {
					sum+= loc/ns;
					n++;
				}
			}
			if (n==0)
				result.setToUnassigned();
			else
				result.setValue(sum/n);
		}
		if (resultString!=null)
			resultString.setValue("Site average for " + propertyTask.getName() + ": "+ result.toString());
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
	/*.................................................................................................................*/
	/** name of this module as it appears in menus, etc.*/
    	 public String getName() {
		return "Protein Site Property";
   	 }
	/*.................................................................................................................*/
    	 public String getNameAndParameters() {
		if (propertyTask == null)
			return "Site Property";
		else
			return "Site Average for " + propertyTask.getName();
   	 }
	/*.................................................................................................................*/
    	 public String getVeryShortName() {
		if (propertyTask == null)
			return "Site Property";
		else
			return propertyTask.getVeryShortName();
   	 }
	/*.................................................................................................................*/
   	 public boolean showCitation(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Calculates the mean value of amino acid properties at a site across taxa." ;
   	 }
   	 
}

