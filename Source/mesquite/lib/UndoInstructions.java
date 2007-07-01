package mesquite.lib;

import mesquite.lib.*;
import mesquite.lib.characters.*;
//import mesquite.charMatrices.lib.*;
import mesquite.lib.table.*;


/*
 * There are direct edits to the matrix via the table: cells, taxon names, char names, add row/column, delete row/column, reorder row/column.

Ideal would be to have these events store a UndoInstructions object attached to the table or data editor module or window, or store null if can't undo.  
I'd start in the editor in this direct way, but with the idea for the system to be transported to assistant modules later.

This "UndoInstructions" wouldn't be a MesquiteCommand but rather a special object with whatever it needed to recover; 
it might just have the old state at the cell; or it might have the whole matrix copied.

Then we could later modify the alter data method of data alterers so that they had to return one of these UndoInstructions.

If the system received notification of a substantive alteration in the matrix that was not represented by the latest UndoInstructions, 
the UndoInstructions object would be set to null.

from MacClade:
	const
		lastChangeCantUndo = -1;
		lastChangeDataCell = 0;
		lastChangeNameCell=1;
		lastChangeCellBlock = 2;
		lastChangeInsertCharAndCellBlock = 3;
		lastChangeAddCharacters = 4;
		lastChangeAddTaxa = 5;
		lastChangeInsertTaxaAndCellBlock = 6;

		lastChangeDestroyCharacters =7;
		lastChangeMoveCharacters = 8;
		lastChangeDestroyTaxa =9;
		lastChangeMoveTaxa = 10;
		lastChangeNameRow=11;

 * */

public class UndoInstructions {
	public static final int SINGLEDATACELL = 1;
	public static final int SINGLETAXONNAME = 2;
	public static final int SINGLECHARACTERNAME = 3;
	public static final int EDITTEXTFIELD = 4;
	public static final int ALLDATACELLS = 5;


	int changeClass;
	int itStart;
	int itEnd;
	int icStart;
	int icEnd;
	Object oldState;
	Object newState;
	CharacterData data;
	CharacterData oldData;
	CharacterData newData;
	MesquiteTable table;
	EditorTextField textField;

	/** This is the constructor for single-cell changes.  */
	public UndoInstructions ( int changeClass, int ic, int it, Object oldState, Object newState, CharacterData data, MesquiteTable table) {

		this.table = table;
		this.changeClass = changeClass;
		this.itStart = it;
		this.itEnd = it;
		this.icStart = ic;
		this.icEnd = ic;
		this.oldState = oldState;
		this.newState = newState;
		this.data = data;
	}

	/** This is the constructor for whole-matrix changes.  */
	public UndoInstructions ( int changeClass, CharacterData oldData, CharacterData data) {

		this.changeClass = changeClass;
		this.data = data;
		if (data!=null)
			this.oldData = oldData.cloneData();
	}

	/** This is the constructor for changes to a TextField.  */
	public UndoInstructions ( int changeClass, Object oldState, Object newState, EditorTextField textField) {
		this.changeClass = changeClass;
		this.oldState = oldState;
		this.newState = newState;
		this.textField = textField;
	}

	public void setNewState (Object newState) {
		this.newState = newState;
	}
	public void setNewData (CharacterData data) {
		this.newData = data.cloneData();
	}

	public UndoInstructions undo() {

		switch (changeClass) {

		case SINGLEDATACELL:
			table.offAllEditingSelection();
			table.setFocusedCell(icStart, itStart, true);
			data.setState(icStart, itStart, (CharacterState)oldState); //receive errors?
			data.notifyListeners(this, new Notification(MesquiteListener.DATA_CHANGED, new int[] {icStart, itStart}));
			return new UndoInstructions(changeClass, icStart,itStart,newState, oldState, data, table);

		case SINGLETAXONNAME:
			table.offAllEditingSelection();
			table.setFocusedCell(-1, itStart, true);
			data.getTaxa().setTaxonName(itStart, ((MesquiteString)oldState).getValue());
			return new UndoInstructions(changeClass, -1, itStart, newState, oldState, data, table);

		case SINGLECHARACTERNAME:
			// problems if no name in cell, as with undo will be fixed as "Character 25", for example, and will display as such
			table.offAllEditingSelection();
			table.setFocusedCell(icStart, -1, true);
			data.setCharacterName(icStart, ((MesquiteString)oldState).getValue());
			return new UndoInstructions(changeClass, icStart, -1, newState, oldState, data, table);
			
		case EDITTEXTFIELD:
			if (textField!=null)
				textField.setText(((MesquiteString)oldState).getValue());
			return new UndoInstructions(changeClass, newState, oldState, textField);

		case ALLDATACELLS:
			newData = data.cloneData();
			data.copyData(oldData);
			return new UndoInstructions(changeClass, newData, data);
			
		}
		
			

		return null;
	}

}
