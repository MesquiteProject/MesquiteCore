package mesquite.lib;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.characters.CharacterData;
//import mesquite.charMatrices.lib.*;
import mesquite.lib.table.*;

/*
 Undo PARTS_MOVED:
 	SortChars and SortTaxa done
 	BasicDataWIndow done
 	
 	- test to see if linked matrices works with parts moved
 		


 * */

public class UndoInstructions implements Undoer {
	public static final int CANTUNDO = -1;
	public static final int SINGLEDATACELL = 1;
	public static final int SINGLETAXONNAME = 2;
	public static final int SINGLECHARACTERNAME = 3;
	public static final int EDITTEXTFIELD = 4;
	public static final int ALLDATACELLS = 5;
	public static final int ALLTAXONNAMES = 6;
	public static final int ALLCHARACTERNAMES = 7;
	public static final int PARTS_MOVED = 8;
	public static final int PARTS_ADDED = 9;

// ones below here are not yet supported
	public static final int TAXA_ADDED = 9;
	public static final int CHARACTERS_ADDED = 10;
	public static final int TAXA_DELETED = 11;
	public static final int CHARACTERS_DELETED = 12;

	int changeClass;
	
	int itStart;
	int itEnd;
	int icStart;
	int icEnd;
	int row;

	MesquiteModule ownerModule = null;
	Object oldState;
	Object newState;
	CharacterData data;
	CharacterData oldData;
	CharacterData newData;
	MesquiteTable table;
	EditorTextField textField;
	Taxa taxa;
	Associable assoc;
	String[] namesList;
	int[] order = null;

	/** This is the constructor for single-cell changes. */
	public UndoInstructions(int changeClass, int ic, int it, Object oldState, Object newState, CharacterData data, MesquiteTable table) {

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

	/** This is the constructor for single-cell changes. */
	public UndoInstructions(int changeClass, int row, Object oldState, Object newState, Object obj, MesquiteTable table) {

		this.table = table;
		this.changeClass = changeClass;
		this.row = row;
		this.itStart = row;
		this.itEnd = row;
		this.icStart = row;
		this.icEnd = row;
		this.oldState = oldState;
		this.newState = newState;
		if (obj instanceof CharacterData)
			data = (CharacterData) obj;
		if (obj instanceof Taxa)
			taxa = (Taxa) obj;
	}

	/** This is the constructor for whole-matrix changes. */
	public UndoInstructions(int changeClass, CharacterData oldData, CharacterData data) {

		this.changeClass = changeClass;
		this.data = data;
		if (data != null)
			this.oldData = oldData.cloneData();
	}

	/** This is the constructor for changes to a TextField. */
	public UndoInstructions(int changeClass, Object oldState, Object newState, EditorTextField textField) {
		this.changeClass = changeClass;
		this.oldState = oldState;
		this.newState = newState;
		this.textField = textField;
	}

	/** This is the constructor for changes to lists of taxon names. */
	public UndoInstructions(int changeClass, Object obj, Taxa taxa) {
		if (obj == null)
			return;
		this.changeClass = changeClass;
		data = null;
		this.taxa = taxa;
		namesList = null;
		if (obj instanceof Taxa) {
			taxa = (Taxa) obj;
			namesList = new String[taxa.getNumTaxa()];
			for (int i = 0; i < namesList.length; i++)
				namesList[i] = taxa.getTaxonName(i);

		} else if (obj instanceof String[]) {
			namesList = new String[((String[]) obj).length];
			for (int i = 0; i < namesList.length; i++)
				namesList[i] = ((String[]) obj)[i];
		}
	}

	/** This is the constructor for changes to lists of character names. */
	public UndoInstructions(int changeClass, Object obj, CharacterData data) {
		if (obj == null)
			return;
		this.changeClass = changeClass;
		this.data = data;
		namesList = null;
		if (obj instanceof CharacterData) {
			data = (CharacterData) obj;
			namesList = new String[data.getNumChars()];
			for (int i = 0; i < namesList.length; i++)
				namesList[i] = data.getCharacterName(i);

		} else if (obj instanceof String[]) {
			namesList = new String[((String[]) obj).length];
			for (int i = 0; i < namesList.length; i++)
				namesList[i] = ((String[]) obj)[i];
		}
	}
	
	public UndoInstructions(int changeClass, Object obj) {
		if (obj == null)
			return;
		this.changeClass = changeClass;
		if (obj instanceof Associable) {
			assoc = (Associable) obj;
			if (assoc!=null)
				if (changeClass==PARTS_MOVED) 
					recordCurrentOrder(assoc);
				else if (changeClass==PARTS_ADDED){
					assoc.resetJustAdded();
				}
					
		}
	}

	/** This is the constructor for changes to taxa. */
	public UndoInstructions(int changeClass) {
		this.changeClass = changeClass;
	}

	public void copyCurrentToPreviousOrder(Associable assoc) {
		assoc.copyCurrentToPreviousOrder()	;
		if (assoc instanceof CharacterData) {
			CharacterData data = (CharacterData)assoc;
			data.copyCurrentToPreviousOrderInLinked();
		}
			
	}
	public void restoreToPreviousOrder(Associable assoc) {
		assoc.restoreToPreviousOrder()	;
		if (assoc instanceof CharacterData) {
			CharacterData data = (CharacterData)assoc;
			data.restoreToPreviousOrderInLinked();
		}
			
	}
	public void recordCurrentOrder(Associable assoc) {
		assoc.recordCurrentOrder()	;
		if (assoc instanceof CharacterData) {
			CharacterData data = (CharacterData)assoc;
			data.recordCurrentOrderInLinked();
		}
			
	}
	public void recordPreviousOrder(Associable assoc) {
		assoc.recordPreviousOrder()	;
		if (assoc instanceof CharacterData) {
			CharacterData data = (CharacterData)assoc;
			data.recordPreviousOrderInLinked();
		}
	}

	public void setNewState(Object newState) {
		this.newState = newState;
	}

	public void setNewData(CharacterData data) {
		this.newData = data.cloneData();
	}

	public void deleteJustAdded(Associable assoc) {
		if (assoc!=null)
				assoc.deleteJustAdded();
	}

	public Undoer undo() {
		String[] oldNamesList;

		switch (changeClass) {

		case SINGLEDATACELL:
			if (table != null) {
				table.offAllEditingSelection();
				table.setFocusedCell(icStart, itStart, true);
			}
			data.setState(icStart, itStart, (CharacterState) oldState); // receive
																		// errors?
			data.notifyListeners(this, new Notification(
					MesquiteListener.DATA_CHANGED,
					new int[] { icStart, itStart }));
			return new UndoInstructions(changeClass, icStart, itStart, newState, oldState, data, table);

		case SINGLETAXONNAME:
			if (table != null) {
				table.offAllEditingSelection();
				table.setFocusedCell(-1, itStart, true);
			}
			if (taxa != null) {
				taxa.setTaxonName(itStart, ((MesquiteString) oldState)
						.getValue());
				return new UndoInstructions(changeClass, itStart, newState,
						oldState, taxa, table);
			} else if (data != null) {
				data.getTaxa().setTaxonName(itStart,
						((MesquiteString) oldState).getValue());
				return new UndoInstructions(changeClass, -1, itStart, newState, oldState, data, table);
			}
			return null;

		case SINGLECHARACTERNAME:
			// problems if no name in cell, as with undo will be fixed as
			// "Character 25", for example, and will display as such
			if (table != null) {
				table.offAllEditingSelection();
				table.setFocusedCell(icStart, -1, true);
			}
			if (data != null)
				data.setCharacterName(icStart, ((MesquiteString) oldState)
						.getValue());
			return new UndoInstructions(changeClass, icStart, -1, newState, oldState, data, table);

		case EDITTEXTFIELD:
			if (textField != null)
				textField.setText(((MesquiteString) oldState).getValue());
			return new UndoInstructions(changeClass, newState, oldState, textField);

		case ALLDATACELLS:
			newData = data.cloneData();
			data.copyData(oldData);
			return new UndoInstructions(changeClass, newData, data);

		case ALLTAXONNAMES:
			if (taxa == null)
				return null;
			oldNamesList = null;
			oldNamesList = new String[taxa.getNumTaxa()];
			for (int i = 0; i < oldNamesList.length; i++)
				oldNamesList[i] = taxa.getTaxonName(i);
			for (int i = 0; i < namesList.length && i < taxa.getNumTaxa(); i++)
				taxa.setTaxonName(i, namesList[i]);
			return new UndoInstructions(changeClass, oldNamesList, taxa);

		case ALLCHARACTERNAMES:
			if (data == null)
				return null;
			oldNamesList = null;
			oldNamesList = new String[data.getNumChars()];
			for (int i = 0; i < oldNamesList.length; i++)
				oldNamesList[i] = data.getCharacterName(i);
			for (int i = 0; i < namesList.length && i < data.getNumChars(); i++)
				data.setCharacterName(i, namesList[i]);
			return new UndoInstructions(changeClass, oldNamesList, data);
			
		case PARTS_MOVED:
			if (assoc == null)
				return null;
			UndoInstructions undoInstructions = new UndoInstructions(changeClass, assoc);
			recordCurrentOrder(assoc);
			restoreToPreviousOrder(assoc);
			assoc.notifyListeners(this, new Notification(MesquiteListener.PARTS_MOVED));
			copyCurrentToPreviousOrder(assoc);
			return undoInstructions;		

		case PARTS_ADDED:
			if (assoc == null)
				return null;
			deleteJustAdded(assoc);
			assoc.notifyListeners(this, new Notification(MesquiteListener.PARTS_DELETED));
			return null;		
		}

		return null;
	}

}
