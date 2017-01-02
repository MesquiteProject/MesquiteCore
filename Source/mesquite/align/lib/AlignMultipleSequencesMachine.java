package mesquite.align.lib;

import mesquite.align.AlignSequences.AlignSequences;
import mesquite.categ.lib.MCategoricalDistribution;
import mesquite.categ.lib.MolecularData;
import mesquite.lib.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.table.MesquiteTable;

public class AlignMultipleSequencesMachine {
	MesquiteModule ownerModule;
	public static boolean separateThreadDefault = false;
	public static boolean separateThread = separateThreadDefault;

	MultipleSequenceAligner aligner;
	MolecularData data;
	MesquiteTable table;
	CalculationMonitor calculationMonitor;
	SeparateThreadStorage separateThreadStorage;

	public AlignMultipleSequencesMachine (MesquiteModule ownerModule,SeparateThreadStorage separateThreadStorage, CalculationMonitor calculationMonitor, 	MultipleSequenceAligner aligner) {
		this.ownerModule = ownerModule;
	//	this.separateThread = separateThread;
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
		if (separateThread)
			calculationMonitor.calculationCompleted(this);  // TODO:  this should ideally be in the full control of the module
		return success;
	}	


	/*.................................................................................................................*/
	/** Called to alter data in those cells selected in table*/
	public boolean alignData(CharacterData data, MesquiteTable table){
		this.data = (MolecularData)data;
		this.table = table;
		//to work, either nothing is selected (in which case it works on whole matrix), or 
		// whole characters are selected (and they must be contiguous, AND more than one character
//		if (table.anyCellSelectedAnyWay() && (!this.data.contiguousSelection() || !this.data.anySelected() || this.data.numberSelected()<=1)) {
		if (table.anyCellSelectedAnyWay() && !table.contiguousColumnsSelected()) {
			if (!MesquiteThread.isScripting()) {
				if (AlertDialog.query(ownerModule.containerOfModule(), "Align entire matrix?", "Some data are currently selected, but not a block of data that can be aligned by Mesquite.  Data can be aligned only for the whole matrix or for a contiguous set of selected characters. If you wish to align only part of the matrix, then press Cancel and select a contiguous set of whole characters. ", "Align entire matrix", "Cancel"))
					table.deselectAll();
				else
					return false;
			}
			else {
				ownerModule.discreetAlert( "Data can be aligned only for the whole matrix or for a contiguous set of selected characters.  Please make sure that nothing in the matrix is selected, or that a contiguous set of characters (sites) is selected.");
				return false;
			}
		}
		//firstRowWithSelectedCell() != 
		if (aligner.permitSeparateThread() && (separateThread= !AlertDialog.query(ownerModule.containerOfModule(), "Separate Thread?", "Run on separate thread? (Beware! Don't close matrix window before done)","No", "Separate", 1, MesquiteThread.SEPARATETHREADHELPMESSAGE))){
			AlignThread alignThread = new AlignThread(ownerModule, this, aligner, this.data, this.table);
			alignThread.separateThread = true;
			separateThreadStorage.setSeparateThread(separateThread);
			alignThread.start();
		}
		else {
			AlignThread alignThread = new AlignThread(ownerModule,this, aligner, this.data, this.table);
			alignThread.separateThread = false;
			separateThreadStorage.setSeparateThread(separateThread);
			alignThread.run();  
			return true;
		}
		return false;
	}

}



class AlignThread extends Thread {
	MesquiteModule ownerModule;
	AlignMultipleSequencesMachine alignmentMachine;
	MultipleSequenceAligner aligner;
	MolecularData data;
	MesquiteTable table;
	boolean separateThread = false;
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
		if (!table.singleCellBlockSelected(firstRow, lastRow,  firstColumn, lastColumn)) {
			firstRow.setValue(0);
			lastRow.setValue(data.getNumTaxa()-1);
			firstColumn.setValue(0);
			lastColumn.setValue(data.getNumChars()-1);
		}
		else 				
			entireColumnsSelected =  table.isColumnSelected(firstColumn.getValue());
		//NOTE: at present this deals only with whole character selecting, and with all taxa
		long[][] m  = aligner.alignSequences((MCategoricalDistribution)data.getMCharactersDistribution(), null, firstColumn.getValue(), lastColumn.getValue(), firstRow.getValue(), lastRow.getValue());
		alignmentMachine.integrateAlignment(m, data,  firstColumn.getValue(), lastColumn.getValue(), firstRow.getValue(), lastRow.getValue());
		if (entireColumnsSelected) {
			for (int ic = 0; ic<data.getNumChars(); ic++) 
				data.setSelected(ic,ic>=firstColumn.getValue() && ic<=lastColumn.getValue()- (oldNumChars - data.getNumChars()));
			table.selectColumns(firstColumn.getValue(),lastColumn.getValue()- (oldNumChars - data.getNumChars()));
		}
		if (separateThread)
			data.notifyListeners(ownerModule, new Notification(MesquiteListener.DATA_CHANGED));
		table.repaintAll();
	}
}
