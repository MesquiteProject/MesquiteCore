package mesquite.molec.MultipleAlignService;


import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.categ.lib.*;
import mesquite.lib.table.*;
import mesquite.molec.lib.SequenceTrimmer;
import mesquite.align.lib.*;



/* ======================================================================== */
public class MultipleAlignService extends CategDataAlterer  implements AltererAlignShift{
	MultipleSequenceAligner aligner;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		aligner= (MultipleSequenceAligner)hireNamedEmployee(MultipleSequenceAligner.class, arguments);
		if (aligner !=null) {
			aligner = (MultipleSequenceAligner)hireNamedEmployee(MultipleSequenceAligner.class, arguments);
			if (aligner == null)
				return sorry(getName() + " couldn't start because the requested aligner wasn't successfully hired.");
		}
		else {
			aligner = (MultipleSequenceAligner)hireEmployee(MultipleSequenceAligner.class, "Aligner");
			if (aligner == null)
				return sorry(getName() + " couldn't start because no aligner module obtained.");
		}
		return true;
	}
	/**/
	public  Class getHireSubchoice(){
		return MultipleSequenceAligner.class;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
	/*.................................................................................................................*/
 	 public Snapshot getSnapshot(MesquiteFile file) { 
  	 	Snapshot temp = new Snapshot();
 	 	temp.addLine("setAligner ", aligner); 
 	 	return temp;
 	 }
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
   	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
   	 	if (checker.compare(this.getClass(), "Sets the aligner", "[name of module]", commandName, "setAligner")) {
   	 	MultipleSequenceAligner temp = (MultipleSequenceAligner)replaceEmployee(MultipleSequenceAligner.class, arguments, "Aligner", aligner);
			if (temp !=null){
				aligner = temp;
   	 			return aligner;
   	 		}
   	 	}
   	 	
		else return  super.doCommand(commandName, arguments, checker);
		return null;
  	 }

	/*.................................................................................................................*/
   	public void alterCell(CharacterData data, int ic, int it){
   	}
	/*.................................................................................................................*/
   	/** Called to alter data in those cells selected in table*/
   	public boolean alterData(CharacterData data, MesquiteTable table,  UndoReference undoReference){
		if (data==null)
			return false;
		
		if (!(data instanceof DNAData))
			return false;
		long[][] m  = aligner.alignSequences((MCategoricalDistribution)data.getMCharactersDistribution(), null, 0, data.getNumChars()-1, 0, data.getNumTaxa()-1);
		
		if (m==null)
			return false;
		boolean success = AlignUtil.integrateAlignment(m, (MolecularData)data,  0, data.getNumChars()-1, 0, data.getNumTaxa()-1);
		return success;
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
	return "Align Sequences in Matrix...";
	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Performs mulitple sequence alignment using an available aligner." ;
   	 }
   	 
}


