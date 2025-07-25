package mesquite.align.lib;


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
import mesquite.lib.ResultCodes;
import mesquite.lib.Snapshot;
import mesquite.lib.UndoReference;
import mesquite.lib.characters.AltererAlignShift;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.DataAltererParallelizable;
import mesquite.lib.table.MesquiteTable;

/* ======================================================================== */
public abstract class AMultipleAlignServiceLib extends MolecularDataAlterer  implements AltererAlignShift, DataAltererParallelizable{
	protected MultipleSequenceAligner aligner;
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
	/** returns whether this module aligns codons */
   	public boolean isCodonAligner(){
   		return false;  
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
			return ResultCodes.INCOMPATIBLE_DATA;
		
		
		AlignMultipleSequencesMachine alignmentMachine = new AlignMultipleSequencesMachine(this, null, null, aligner);
		int resultCode = alignmentMachine.alignData(data,table);
	
		/*NOTE: In 3.x, alignment could be on a separate thread. As of 4.0, this is disallowed; 
		 * the alignment is on the main thread. If this goes back to allowing a separate thread, then for this module's function 
		 * as DataAlterer, which is synchronous, it should hold here in a loop until notified (e.g. by implementing
		 * CalculationMonitor and passing it along to the machine).
		 * */
		
		if (resultCode >=0) {
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
		return resultCode;
   	}
   	

   	 
}


