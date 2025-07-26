package mesquite.align.AMultipleAlignService;


import mesquite.align.lib.AMultipleAlignServiceLib;
import mesquite.align.lib.AlignMultipleSequencesMachine;
import mesquite.align.lib.MultipleSequenceAligner;
import mesquite.categ.lib.MolecularData;
import mesquite.categ.lib.MolecularDataAlterer;
import mesquite.lib.CommandChecker;
import mesquite.lib.EmployeeNeed;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteListener;
import mesquite.lib.Notification;
import mesquite.lib.*;
import mesquite.lib.Snapshot;
import mesquite.lib.UndoReference;
import mesquite.lib.characters.AltererAlignShift;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.DataAltererParallelizable;
import mesquite.lib.table.MesquiteTable;

/* ======================================================================== */
public class AMultipleAlignService extends AMultipleAlignServiceLib{
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e2 = registerEmployeeNeed(MultipleSequenceAligner.class, getName() + " needs a module to calculate alignments.",
		"The sequence aligner is chosen in dialogs or in the Align Sequences or Selected Cell Block submenu");
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		aligner= (MultipleSequenceAligner)hireNamedEmployee(MultipleSequenceAligner.class, arguments);
		if (aligner ==null) {
			aligner = (MultipleSequenceAligner)hireEmployee(MultipleSequenceAligner.class, "Aligner");
			if (aligner == null)
				return sorry(getName() + " couldn't start because no aligner module obtained.");
		}
		aligner.setCodonAlign(isCodonAligner());
		return true;
	}
	/*.................................................................................................................*/
	/** returns whether this module aligns codons */
   	public boolean isCodonAligner(){
   		return false;  
   	}


 	/*.................................................................................................................*/
  	 public boolean showCitation() {
		return false;
   	 }
 	/*.................................................................................................................*/
   	 public boolean isSubstantive(){
   	 	return false;
   	 }
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
	   /*.................................................................................................................*/
   	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
   	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
   	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
      	public int getVersionOfFirstRelease(){
      		return 310;  
    }
    	/*.................................................................................................................*/
   	 public String getName() {
		return "Align Sequences in Matrix";
  	 }
 	/*.................................................................................................................*/
	 public String getNameAndParameters() {
		 if (aligner == null)
		return "Align Sequences in Matrix";
		 else
			 return "Align Sequences in Matrix (" + aligner.getName() + ")";
	 }
	/*.................................................................................................................*/
	 public String getNameForMenuItem() {
	return "Align Sequences or Selected Cell Block";
	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Performs multiple sequence alignment using an available aligner." ;
   	 }
   	 
}


