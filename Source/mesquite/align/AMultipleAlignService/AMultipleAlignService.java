package mesquite.align.AMultipleAlignService;


import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.categ.lib.*;
import mesquite.lib.table.*;
import mesquite.align.lib.*;

/* ======================================================================== */
public class AMultipleAlignService extends MolecularDataAlterer  implements AltererAlignShift{
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e2 = registerEmployeeNeed(MultipleSequenceAligner.class, getName() + " needs a module to calculate alignments.",
		"The sequence aligner is chosen in dialogs or in the Align Sequences or Selected Block submenu");
	}
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
   	public int alterData(CharacterData data, MesquiteTable table,  UndoReference undoReference){
		if (data==null)
			return -10;
		if (!(data instanceof MolecularData))
			return INCOMPATIBLE_DATA;
		
		
		AlignMultipleSequencesMachine alignmentMachine = new AlignMultipleSequencesMachine(this, null, null, aligner);
		boolean success = alignmentMachine.alignData(data,table);
		
		/*NOTE: In 3.x, alignment could be on a separate thread. As of 4.0, this is disallowed; 
		 * the alignment is on the main thread. If this goes back to allowing a separate thread, then for this module's function 
		 * as DataAlterer, which is synchronous, it should hold here in a loop until notified (e.g. by implementing
		 * CalculationMonitor and passing it along to the machine).
		 * */
		
		if (success) {
			if (table != null)
				table.repaintAll();
			data.notifyListeners(this, new Notification(MesquiteListener.DATA_CHANGED));
			data.notifyInLinked(new Notification(MesquiteListener.DATA_CHANGED));
		}
		
		

		/*======================* OLD 3.x
		long[][] m  = aligner.alignSequences((MCategoricalDistribution)data.getMCharactersDistribution(), null, 0, data.getNumChars()-1, 0, data.getNumTaxa()-1);
		
		if (m==null)
			return -1;
		boolean success = AlignUtil.integrateAlignment(m, (MolecularData)data,  0, data.getNumChars()-1, 0, data.getNumTaxa()-1);*/
		if (success)
		return SUCCEEDED;
		return MEH;
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
	return "Align Sequences or Selected Block";
	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Performs mulitple sequence alignment using an available aligner." ;
   	 }
   	 
}


