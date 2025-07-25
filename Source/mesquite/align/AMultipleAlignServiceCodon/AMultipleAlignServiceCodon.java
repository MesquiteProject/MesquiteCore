package mesquite.align.AMultipleAlignServiceCodon;


import mesquite.align.AMultipleAlignService.AMultipleAlignService;
import mesquite.align.lib.AMultipleAlignServiceLib;
import mesquite.align.lib.MultipleSequenceAligner;
import mesquite.categ.lib.MolecularData;
import mesquite.categ.lib.RequiresAnyDNAData;
import mesquite.lib.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.table.MesquiteTable;

/* ======================================================================== */
public class AMultipleAlignServiceCodon extends AMultipleAlignServiceLib{

	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e2 = registerEmployeeNeed(MultipleSequenceAligner.class, getName() + " needs a module to calculate alignments.",
		"The sequence aligner is chosen in dialogs or in the Codon Alignment of Sequences in Matrix submenu");
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		Debugg.println("   ************** AMultipleAlignService CODON");
		aligner= (MultipleSequenceAligner)hireNamedEmployee(MultipleSequenceAligner.class, arguments);
		if (aligner ==null) {
			aligner = (MultipleSequenceAligner)hireEmployee(MultipleSequenceAligner.class, "Aligner");
			if (aligner == null)
				return sorry(getName() + " couldn't start because no aligner module obtained.");
		}
		aligner.setCodonAlign(true);
		return true;
	}
	/** returns whether this module aligns codons */
   	public boolean isCodonAligner(){
   		return true;  
   	}
	/*.................................................................................................................*/
	/** Returns CompatibilityTest so other modules know if this is compatible with some object. */
	public CompatibilityTest getCompatibilityTest(){
		return new RequiresAnyDNAData();
	}
	/*.................................................................................................................*/
  	/** Called to alter data in those cells selected in table*/
   	public int alterData(CharacterData data, MesquiteTable table,  UndoReference undoReference){
		if (data==null)
			return -10;
		if (!(data instanceof MolecularData))
			return ResultCodes.INCOMPATIBLE_DATA;
		
		data.deselectAll();
		
		return super.alterData(data, table, undoReference);
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
      		return NEXTRELEASE;  
    }
    	/*.................................................................................................................*/
   	 public String getName() {
		return "Codon Alignment of Sequences in Matrix";
  	 }
 	/*.................................................................................................................*/
	 public String getNameAndParameters() {
		 if (aligner == null)
		return "Codon Alignment of Sequences in Matrix";
		 else
			 return "Codon Alignment of Sequences in Matrix (" + aligner.getName() + ")";
	 }
	/*.................................................................................................................*/
	 public String getNameForMenuItem() {
	return "Codon Alignment of Entire Matrix";
	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Performs multiple sequence alignment of translated amino acids using an available aligner, and then realigns nucleotides based on the amino acid alignment." ;
   	 }
   	 
}


