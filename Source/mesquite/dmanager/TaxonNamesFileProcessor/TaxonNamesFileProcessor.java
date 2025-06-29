package mesquite.dmanager.TaxonNamesFileProcessor;

import mesquite.lib.CommandChecker;
import mesquite.lib.CompatibilityTest;
import mesquite.lib.Debugg;
import mesquite.lib.EmployeeNeed;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteProject;
import mesquite.lib.Snapshot;
import mesquite.lib.duties.FileProcessor;
import mesquite.lib.duties.TaxonNameAlterer;
import mesquite.lib.table.MesquiteTable;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.ui.MesquiteSubmenuSpec;

/* ======================================================================== */
public class TaxonNamesFileProcessor extends FileProcessor {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e2 = registerEmployeeNeed(TaxonNameAlterer.class, getName() + " needs a particular method to act upon taxon names.", null);
		e2.setPriority(2);
	}
	MesquiteTable table;
	MesquiteSubmenuSpec mss= null;
	TaxonNameAlterer utilityTask;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (arguments !=null) {
			utilityTask = (TaxonNameAlterer)hireNamedEmployee(TaxonNameAlterer.class, arguments);
			if (utilityTask == null)
				return sorry(getName() + " couldn't start because the requested taxon name alterer wasn't successfully hired.");
		}
		else {
			utilityTask = (TaxonNameAlterer)hireEmployee(TaxonNameAlterer.class, "Taxon Name alterer");
			if (utilityTask == null)
				return sorry(getName() + " couldn't start because no taxon name alterer module obtained.");
		}
		return true;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return false;  
   	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 351;  
	}
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
	/*.................................................................................................................*/
   	 public boolean isSubstantive(){
   	 	return true; //not really, but to force checking of prerelease
   	 }
 	/*.................................................................................................................*/
 	public Snapshot getSnapshot(MesquiteFile file) { 
 		Snapshot temp = new Snapshot();
 		temp.addLine("setNameAlterer ", utilityTask);  
 		return temp;
 	}
 	/*.................................................................................................................*/
 	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
 		if (checker.compare(this.getClass(), "Sets the name alterer module", "[name of module]", commandName, "setNameAlterer")) {
 			TaxonNameAlterer temp =  (TaxonNameAlterer)replaceEmployee(TaxonNameAlterer.class, arguments, "Method to alter taxon names", utilityTask);
 			if (temp!=null) {
 				utilityTask = temp;
 				return utilityTask;
 			}
 
 		}
 		else
 			return  super.doCommand(commandName, arguments, checker);
 		return null;
 	}

   	/** if returns true, then requests to remain on even after alterFile is called.  Default is false*/
   	public boolean pleaseLeaveMeOn(){
   		return false;
   	}
	/*.................................................................................................................*/
   	/** Called to alter file. */
   	public int processFile(MesquiteFile file){
   		MesquiteProject proj = file.getProject();
   		if (proj == null)
   			return -1;
   		boolean success = true;
   		CompatibilityTest test = utilityTask.getCompatibilityTest();
   		for (int im = 0; im < proj.getNumberTaxas(file); im++){
   			Taxa taxa = proj.getTaxa(file, im);
   			int result = utilityTask.alterTaxonNames(taxa, null);
   			success = success && (result >= 0);
  			}
   		if (!success)
   		return -1;
   		return 0;
   	}
	/*.................................................................................................................*/
	 public String getName() {
	return "Alter Taxon Names";
	 }
		/*.................................................................................................................*/
	 public String getNameAndParameters() {
		 if (utilityTask==null)
			 return "Alter Taxon Names";
		 else
			 return "Alter Taxon Names (" + utilityTask.getName() + ")";
	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Manages taxon renaming for file processing." ;
   	 }
   	 
}


