/* Mesquite source code.  Copyright 1997-2010 W. Maddison and D. Maddison.
Version 2.74, October 2010.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.distance.TaxonDistance;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.distance.lib.*;

public class TaxonDistance extends NumberFor2Taxa {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(TaxaDistanceSource.class, getName() + "  needs a method to calculate distance between taxa.",
		"The method to calculate distance can be selected initially or in the Source of Distance submenu");
	}
	MesquiteNumber nt;
	TaxaDistanceSource distanceTask;
	TaxaDistance taxaDistance;
	MesquiteString distanceTaskName;
	MesquiteCommand atC;
	MesquiteSubmenuSpec mss;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		distanceTask = (TaxaDistanceSource)hireEmployee(TaxaDistanceSource.class, "Source of distance");
		if (distanceTask == null) {
 			return sorry("Can't start TaxonDistance because no distance source found");
 		}
		nt= new MesquiteNumber();
		atC = makeCommand("setDistanceSource",  this);
		distanceTask.setHiringCommand(atC);
		distanceTaskName = new MesquiteString(distanceTask.getName());
		if (numModulesAvailable(TaxaDistanceSource.class)>1){
			mss = addSubmenu(null, "Source of Distance", atC, TaxaDistanceSource.class);
			mss.setSelected(distanceTaskName);
		}
  		return true;
  	 }
  	public void employeeQuit(MesquiteModule m){
  		iQuit();
  	}
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) {
   	 	Snapshot temp = new Snapshot();
  	 	temp.addLine( "setDistanceSource " , distanceTask);
  	 	return temp;
  	 }
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Sets the module supplying distances", "[name of module]", commandName, "setDistanceSource")) {
    	 		TaxaDistanceSource temp =  (TaxaDistanceSource)replaceEmployee(TaxaDistanceSource.class, arguments, "Source of distances", distanceTask);
 			if (temp!=null) {
	    	 	distanceTask=  temp;
	    	 	taxaDistance = null;
		 		distanceTask.setHiringCommand(atC);
				distanceTaskName = new MesquiteString(distanceTask.getName());
				if (mss!=null){
					mss.setSelected(distanceTaskName);
				}
				parametersChanged();
 			}
 			return temp;
    	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
   	 }
   	public void initialize(Taxon t1, Taxon t2){
   	}
   	
	public void calculateNumber(Taxon t1, Taxon t2, MesquiteNumber result, MesquiteString resultString){
		if (result==null || t1==null || t2==null)
			return;
	   	clearResultAndLastResult(result);
		Taxa taxa = t1.getTaxa();
		if (taxaDistance==null) //need to recalculate only if change in distance calculator
			taxaDistance = distanceTask.getTaxaDistance(taxa);
		if (taxaDistance != null)
			result.setValue(taxaDistance.getDistance(t1.getTaxa().whichTaxonNumber(t1),t2.getTaxa().whichTaxonNumber(t2)));
		saveLastResult(result);
		saveLastResultString(resultString);
		
	}
	
	
	/*.................................................................................................................*/
   	 public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
   	 		taxaDistance=null; //to force recalculation
 			super.employeeParametersChanged(this, source, notification);
   	 }
	/*.................................................................................................................*/
    	 public String getNameAndParameters() {
		return distanceTask.getNameAndParameters();
   	 }
    		/*.................................................................................................................*/
    	 public String getParameters() {
		return "Distance used: " + distanceTask.getName();
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Distance between taxa";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Counts the difference between two taxa." ;
   	 }
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
   	 
}

