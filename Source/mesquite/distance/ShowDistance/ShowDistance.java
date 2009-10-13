package mesquite.distance.ShowDistance;

import mesquite.distance.lib.TaxaDistance;
import mesquite.distance.lib.TaxaDistanceSource;
import mesquite.lib.EmployeeNeed;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.*;
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
 	//	distanceTask = (TaxaDistanceSource)hireEmployee(TaxaDistanceSource.class, "Source of distance");
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
		return NEXTRELEASE;  
	}

	public void setTableAndData(MesquiteTable table, CharacterData data) {
		if (data==null || distanceTask==null) 
			return;
   		distanceTask.initialize(data.getTaxa());
 		TaxaDistance dist = distanceTask.getTaxaDistance(data.getTaxa());
		logln("\n\n=== Distance matrix for " + dist.getName() + " ===");

		dist.distancesToLog();

	}


}
