/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.distance.ShowDistance;

import mesquite.distance.lib.TaxaDistance;
import mesquite.distance.lib.TaxaDistanceSource;
import mesquite.lib.EmployeeNeed;
import mesquite.lib.MesquiteModule;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.DataWindowAssistantA;
import mesquite.lib.table.MesquiteTable;

public class ShowDistance extends DataWindowAssistantA {

	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(TaxaDistanceSource.class, getName() + "  needs a source of distances.",
		"The source of distances can be selected initially");
	}
	TaxaDistanceSource distanceTask;
	CharacterData data;

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		distanceTask = (TaxaDistanceSource)hireNamedEmployee(TaxaDistanceSource.class, "#TaxaDistFromMatrixSrc");
 		if (distanceTask == null) {
 			return sorry(getName() + " couldn't start because no source of distances was obtained.");
 		}
 		
  		return true;
  	 }

	 public void employeeQuit(MesquiteModule m){
	  	 	iQuit();
	  	 }

	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Writes distance matrix to log.";
   	 }
   	 
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
	public String getName() {
		return "Show Distance Matrix";
	}

	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 272;  
	}

	public void setTableAndData(MesquiteTable table, CharacterData data) {
		if (data==null || distanceTask==null) 
			return;
   		distanceTask.initialize(data.getTaxa());
 		TaxaDistance dist = distanceTask.getTaxaDistance(data.getTaxa());
		if (dist == null) {
			logln("\n\nNO DISTANCE MATRIX OBTAINED");
			return;
		}
		logln("\n\n=== Distance matrix for " + dist.getName() + " ===");

		dist.distancesToLog();

	}


}
