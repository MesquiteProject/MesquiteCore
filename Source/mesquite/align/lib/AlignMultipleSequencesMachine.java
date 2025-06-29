package mesquite.align.lib;


import mesquite.categ.lib.MCategoricalDistribution;
import mesquite.categ.lib.MolecularData;
import mesquite.lib.CalculationMonitor;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteListener;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteThread;
import mesquite.lib.Notification;
import mesquite.lib.ResultCodes;
import mesquite.lib.SeparateThreadStorage;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.table.MesquiteTable;
import mesquite.lib.ui.AlertDialog;

public class AlignMultipleSequencesMachine {
	MesquiteModule ownerModule;
	
	static boolean separateThreadDefault = false;
	/*NOTE: in 3.x, alignment could be on a separate thread. This is disabled as of 4.0.
	 * Thus assumes separateThread = false ALWAYS. 
	 * If this changes, either the machine's users, like MultipleAlignService, need to wait until complete (see notes there), 
	 * or the user's users, like AlterData, need to know the process might not be done synchronously. 
	 * 
	 * To ensure the separateThread isn't accidentally turned on before we have programmed elsewhere for it,
	 * these variables are no longer public*/
	static boolean separateThread = separateThreadDefault;  
	static boolean userCanRequestSeparateThread = false;   // currently no way for user to adjust this

	MultipleSequenceAligner aligner;
	MolecularData data;
	MesquiteTable table;
	CalculationMonitor calculationMonitor;
	SeparateThreadStorage separateThreadStorage;
	

	public AlignMultipleSequencesMachine (MesquiteModule ownerModule,SeparateThreadStorage separateThreadStorage, CalculationMonitor calculationMonitor, 	MultipleSequenceAligner aligner) {
		this.ownerModule = ownerModule;
		this.aligner = aligner;
		this.calculationMonitor = calculationMonitor;
		this.separateThreadStorage = separateThreadStorage;

	}

	
	/*.................................................................................................................*/
	public boolean integrateAlignment(long[][] alignedMatrix, MolecularData data, int icStart, int icEnd, int itStart, int itEnd){
		if (alignedMatrix == null || data == null)
			return false;
		ownerModule.getProject().incrementProjectWindowSuppression();
		boolean success = AlignUtil.integrateAlignment(alignedMatrix, data,  icStart,  icEnd,  itStart,  itEnd);
		ownerModule.getProject().decrementProjectWindowSuppression();
		if (separateThread && calculationMonitor != null)
			calculationMonitor.calculationCompleted(this);  // TODO:  this should ideally be in the full control of the module
		return success;
	}	


	/*.................................................................................................................*/
	/** Called to alter data in those cells selected in table*/
	public int alignData(CharacterData data, MesquiteTable table){
		this.data = (MolecularData)data;
		this.table = table;
		//to work, either nothing is selected (in which case it works on whole matrix), or 
		// whole characters are selected (and they must be contiguous, AND more than one character
//		if (table.anyCellSelectedAnyWay() && (!this.data.contiguousSelection() || !this.data.anySelected() || this.data.numberSelected()<=1)) {
		if (table != null && table.anyCellSelectedAnyWay() && !table.contiguousColumnsSelected()) {
			if (!MesquiteThread.isScripting()) {
				if (AlertDialog.query(ownerModule.containerOfModule(), "Align entire matrix?", "Some data are currently selected, but not the sort of cell block that can be aligned by Mesquite.  Data can be aligned only for the whole matrix or for a contiguous set of selected characters. If you wish to align only part of the matrix, then press Cancel and select a contiguous set of whole characters. ", "Align entire matrix", "Cancel"))
					table.deselectAll();
				else
					return ResultCodes.USER_STOPPED;
			}
			else {
				ownerModule.discreetAlert( "Data can be aligned only for the whole matrix or for a contiguous set of selected characters.  Please make sure that nothing in the matrix is selected, or that a contiguous set of characters (sites) is selected.");
				return ResultCodes.INCOMPATIBLE_DATA;
			}
		}
		//firstRowWithSelectedCell() != 
		if (!MesquiteThread.isScripting() && userCanRequestSeparateThread && aligner.permitSeparateThread() && (separateThread= !AlertDialog.query(ownerModule.containerOfModule(), "Separate Thread?", "Run on separate thread? (Beware! Don't close matrix window before done)","No", "Separate", 1, MesquiteThread.SEPARATETHREADHELPMESSAGE))){
			//As of 4.0, this branch can't be followed, because userCanRequestSeparateThread is false and there's not mechanism to change it
			AlignThread alignThread = new AlignThread(ownerModule, this, aligner, this.data, this.table);
			alignThread.separateThread = true;
			if (separateThreadStorage != null)
				separateThreadStorage.setSeparateThread(separateThread);
			alignThread.start();
			return alignThread.resultCode;
		}
		else {
			AlignThread alignThread = new AlignThread(ownerModule,this, aligner, this.data, this.table);
			alignThread.separateThread = false;
			if (separateThreadStorage != null)
				separateThreadStorage.setSeparateThread(separateThread);
			alignThread.run();  
			return alignThread.resultCode;
		}
	}

}



class AlignThread extends Thread {
	MesquiteModule ownerModule;
	AlignMultipleSequencesMachine alignmentMachine;
	MultipleSequenceAligner aligner;
	MolecularData data;
	MesquiteTable table;
	boolean separateThread = false;
	int resultCode = ResultCodes.NOT_YET_DONE;
	public AlignThread(MesquiteModule ownerModule, AlignMultipleSequencesMachine alignmentMachine, MultipleSequenceAligner aligner, MolecularData data, MesquiteTable table){
		this.aligner = aligner;
		this.ownerModule = ownerModule;
		this.alignmentMachine = alignmentMachine;
		this.data = data;
		this.table = table;
	}

	public void run() {
		MesquiteInteger firstRow = new MesquiteInteger();
		MesquiteInteger lastRow = new MesquiteInteger();
		MesquiteInteger firstColumn = new MesquiteInteger();
		MesquiteInteger lastColumn = new MesquiteInteger();

		boolean entireColumnsSelected = false;
		int oldNumChars = data.getNumChars();
		if (table == null || !table.singleCellBlockSelected(firstRow, lastRow,  firstColumn, lastColumn)) {
			firstRow.setValue(0);
			lastRow.setValue(data.getNumTaxa()-1);
			firstColumn.setValue(0);
			lastColumn.setValue(data.getNumChars()-1);
		}
		else 				
			entireColumnsSelected =  table.isColumnSelected(firstColumn.getValue());
		//NOTE: at present this deals only with whole character selecting, and with all taxa
		MesquiteInteger resultCodeFromAligner = new MesquiteInteger(ResultCodes.NO_RESPONSE);
		long[][] m  = aligner.alignSequences((MCategoricalDistribution)data.getMCharactersDistribution(), null, firstColumn.getValue(), lastColumn.getValue(), firstRow.getValue(), lastRow.getValue(), resultCodeFromAligner);
		resultCode = resultCodeFromAligner.getValue();
		alignmentMachine.integrateAlignment(m, data,  firstColumn.getValue(), lastColumn.getValue(), firstRow.getValue(), lastRow.getValue());
		if (entireColumnsSelected) {
			for (int ic = 0; ic<data.getNumChars(); ic++) 
				data.setSelected(ic,ic>=firstColumn.getValue() && ic<=lastColumn.getValue()- (oldNumChars - data.getNumChars()));
			if (table !=null) 
				table.selectColumns(firstColumn.getValue(),lastColumn.getValue()- (oldNumChars - data.getNumChars()));
		}
		if (separateThread) 
			data.notifyListeners(ownerModule, new Notification(MesquiteListener.DATA_CHANGED));
		if (table != null)
			table.repaintAll();
	}
}
