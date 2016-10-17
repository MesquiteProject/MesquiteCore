/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 



Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lib;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.characters.*;
import mesquite.categ.lib.*;
//import mesquite.charMatrices.lib.*;
import mesquite.lib.table.*;

/*
 Undo PARTS_MOVED:
 	SortChars and SortTaxa done
 	BasicDataWIndow done

 	- test to see if linked matrices works with parts moved



 * */

public class UndoInstructions implements Undoer {
	// Undo mode: what branch of the undo system is being used; what will be remembered
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
	public static final int DATABLOCK = 10;

	//	Modes below here are not yet supported
	public static final int TAXA_DELETED = 30;
	public static final int CHARACTERS_DELETED = 31;


	// Clues for the ALLDATACELLS mode as to what might or might not change
	public static final int NO_CHAR_TAXA_CHANGES = 100;  //currently allowed
	public static final int CHAR_ADDED_TO_END = 101;  //currently allowed
	public static final int CHAR_ADDED_TO_START = 109;  //currently allowed
	public static final int CHAR_ADDED = 102;  //currently allowed
	public static final int CHAR_REORDERED = 103;
	public static final int CHAR_DELETED = 104;
	public static final int TAX_ADDED_TO_END = 105;  //currently allowed
	public static final int TAX_ADDED = 106; //currently allowed
	public static final int TAX_REORDERED = 107;
	public static final int TAX_DELETED = 108;
	public static final int TAX_ADDED_TO_START = 110;//currently allowed
	public static final int CHAR_SPECSETS_CHANGED = 111;
	public static final int TAX_SPECSETS_CHANGED = 112;
	public static final int[] allowedChanges = new int[]{NO_CHAR_TAXA_CHANGES, CHAR_ADDED_TO_END, CHAR_ADDED_TO_START, CHAR_ADDED, TAX_ADDED_TO_END, TAX_ADDED, TAX_ADDED_TO_START};



	int changeClass;

	int itStart;
	int itEnd;
	int icStart;
	int icEnd;
	int row;

	MesquiteModule ownerModule = null;
	private Object oldState=null;
	private Object newState=null;
	CharacterData data;
	CharacterData oldData;
	CharacterData newData;
	MesquiteTable table;
	EditorTextField textField;
	Taxa taxa;
	Associable assoc;
	String[] namesList;
	int[] order = null;

	String[][] oldStateNames;
	String[][] oldStateNotes;

	MesquiteCommand undoCommand;


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

	/** This is the constructor for single-cell changes to taxon names or character names. */
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

	/** This is the constructor for whole-matrix changes or changes to lists of character names. */
	public UndoInstructions(int changeClass, Object obj, CharacterData data) {
		if (obj == null)
			return;
		this.changeClass = changeClass;
		this.data = data;
		if (changeClass == ALLCHARACTERNAMES) {

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
	}

	/** This is the constructor for whole-matrix changes or changes to lists of character names. */
	public UndoInstructions(int changeClass, Object obj, CharacterData data, int[] changesThatMightHappen) {
		if (obj == null)
			return;
		this.changeClass = changeClass;
		this.data = data;

		if (changeClass==ALLDATACELLS) {
			if (data != null && obj!=null)
				if (obj instanceof CharacterData) {
					if (changesThatMightHappen==null)  // no specification as to what might happen; currently, disallow that
						return;
					for (int i=0; i<changesThatMightHappen.length; i++) 
						if (IntegerArray.indexOf(allowedChanges, changesThatMightHappen[i])<0)// one of the possible changes is not allowed; bail
							return;

					this.oldData = ((CharacterData)obj).cloneData();
					if (oldData !=  null){
						this.oldData.setName("Undo Matrix [old]");
						this.oldData.disconnectListening();
						if (obj instanceof CategoricalData) {
							CategoricalData cd = (CategoricalData)obj;
							for (int ic=0; ic<cd.getNumChars(); ic++){
								if (cd.hasStateNames() && cd.hasStateNames(ic))
									for (int i = 0; i <= CategoricalState.maxCategoricalState; i++)
										if (cd.hasStateName(ic,i))
											((CategoricalData)oldData).setStateName(ic,i,cd.getStateName(ic,i));
								if (cd.hasStateNotes() && cd.hasStateNotes(ic))
									for (int i = 0; i <= CategoricalState.maxCategoricalState; i++)
										if (cd.hasStateNote(ic,i))
											((CategoricalData)oldData).setStateNote(ic,i,cd.getStateNote(ic,i));
							}
						}
					}
				}
		}
	}

	/** This is the constructor for a change to a block of the matrix. */
	public UndoInstructions(int changeClass, Object obj, CharacterData data, int icStartStore, int icEndStore, int itStartStore, int itEndStore, int icStart, int icEnd, int itStart, int itEnd, boolean recordRange) {
		if (obj == null)
			return;
		this.changeClass = changeClass;
		this.data = data;

		if (changeClass==DATABLOCK) {
			if (data != null && obj!=null)
				if (obj instanceof CharacterData) {
					this.oldData = ((CharacterData)obj).cloneDataBlock(icStart,  icEnd,  itStart, itEnd);
					this.oldData.setName("Undo Matrix [old]");
					this.oldData.disconnectListening();
					if (recordRange) {
						this.itStart = itStartStore;
						this.itEnd = itEndStore;
						this.icStart = icStartStore;
						this.icEnd = icEndStore;
					}
				}
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
		if (newState == null)
			return;
		if (this.newState.getClass().equals(newState.getClass()))
			this.newState = newState;
	}

	public void setUndoCommand(MesquiteCommand command) {
		undoCommand = command;
	}
	public void doCommand(String arguments) {
		undoCommand.doItMainThread(arguments, "", this);
	}

	public Object getNewState() {
		return newState;
	}
	public void setNewData(CharacterData data) {
		if (data == null){
			this.newData = null;
			return;
		}
		this.newData = data.cloneData();
		if (newData == null)
			return;
		this.newData.setName("Undo Matrix [new]");
		newData.disconnectListening();
	}
	public void setNewDataBlock(CharacterData data,int icStart, int icEnd, int itStart, int itEnd) {
		this.newData = data.cloneDataBlock( icStart,  icEnd,  itStart,  itEnd);
		this.newData.setName("Undo Matrix [new]");
		newData.disconnectListening();
	}

	public void deleteJustAdded(Associable assoc) {
		if (assoc!=null)
			assoc.deleteJustAdded();
	}

	public void dispose() {
		if (oldData!=null)
			oldData.dispose();
		if (newData!=null)
			newData.dispose();
		oldData=null;
		newData=null;
	}

	public void finalize() {
		dispose();
	}

	public Undoer undo() {
		String[] oldNamesList;
		Notification notification;
		int[] subcodes=null;

		switch (changeClass) {

		case SINGLEDATACELL:
			if (table != null) {
				table.offAllEditingSelection();
				table.setFocusedCell(icStart, itStart, true);
			}
			CharacterState csBefore = data.getCharacterState(null, icStart, itStart);
			if (oldState instanceof MesquiteString){

				CharacterState cs = data.makeCharacterState();
				String st = ((MesquiteString) oldState).getValue();

				if (StringUtil.blank(st)){
					return null;
				}
				cs.setValue(st, data);
				data.setState(icStart, itStart, cs); // receive
			}
			else if (data instanceof CategoricalData)  
				data.setState(icStart, itStart, (CategoricalState) oldState); // receive
			else
				data.setState(icStart, itStart, (CharacterState) oldState); // receive
			// errors?
			CharacterState csAfter = data.getCharacterState(null, icStart, itStart);
			notification =new Notification(MesquiteListener.DATA_CHANGED, new int[] { icStart, itStart }) ;
			subcodes = new int[] {MesquiteListener.SINGLE_CELL};
			if (csBefore != null && csAfter != null && csBefore.isInapplicable()==csAfter.isInapplicable())
				subcodes = new int[] {MesquiteListener.SINGLE_CELL, MesquiteListener.CELL_SUBSTITUTION};
			notification.setSubcodes(subcodes);

			data.notifyListeners(this, notification);
			UndoInstructions undoInst =  new UndoInstructions(changeClass, icStart, itStart, newState, oldState, data, table);

			return undoInst;

		case SINGLETAXONNAME:
			if (table != null) {
				table.offAllEditingSelection();
				table.setFocusedCell(-1, itStart, true);
			}
			if (taxa != null) {
				taxa.setTaxonName(itStart, ((MesquiteString) oldState).getValue());
				return new UndoInstructions(changeClass, itStart, newState,
						oldState, taxa, table);
			} else if (data != null) {
				data.getTaxa().setTaxonName(itStart, ((MesquiteString) oldState).getValue());
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
				data.setCharacterName(icStart, ((MesquiteString) oldState).getValue());
			return new UndoInstructions(changeClass, icStart, -1, newState, oldState, data, table);

		case EDITTEXTFIELD:
			if (oldState == null || ((MesquiteString) oldState).getValue() == null)
				return null;
			if (textField != null)
				textField.setText(((MesquiteString) oldState).getValue());

			return new UndoInstructions(changeClass, newState, oldState, textField);

		case ALLDATACELLS:
			newData = data.cloneData();   // note that this clones the current (live) matrix, which means if it has lost columns, they aren't recovered.  
			newData.setName("Undo Matrix [new]");
			newData.disconnectListening();
			if (data.getNumChars()>oldData.getNumChars())  //I added this to help   //we need a setToClone system in CharacterData, which we don't have yet.
				data.deleteCharacters(oldData.getNumChars(), data.getNumChars()-oldData.getNumChars(), false);
			else if (data.getNumChars()<oldData.getNumChars())//I added this to help
				data.addCharacters(data.getNumChars()-1, oldData.getNumChars()-data.getNumChars(), false);
			data.copyData(oldData, true);  //THIS DOES NOT expand data to be the same size as oldData
			if (oldData instanceof CategoricalData) {  ////David: This does NOT recover specssets, so codon positions etc. are all out of whack
				CategoricalData cd = (CategoricalData)oldData;
				for (int ic=0; ic<cd.getNumChars(); ic++){
					if (cd.hasStateNames() && cd.hasStateNames(ic))
						for (int i = 0; i <= CategoricalState.maxCategoricalState; i++)
							if (cd.hasStateName(ic,i) && StringUtil.notEmpty(cd.getStateName(ic,i))) {
								String s = cd.getStateName(ic,i);
								((CategoricalData)data).setStateName(ic,i,cd.getStateName(ic,i));
							}
					if (cd.hasStateNotes() && cd.hasStateNotes(ic))
						for (int i = 0; i <= CategoricalState.maxCategoricalState; i++)
							if (cd.hasStateNote(ic,i))
								((CategoricalData)data).setStateNote(ic,i,cd.getStateNote(ic,i));
				}
			}

			data.notifyListeners(this, new Notification(MesquiteListener.DATA_CHANGED));
			return new UndoInstructions(changeClass, newData, data);

		case DATABLOCK:
			newData = data.cloneDataBlock(icStart, icEnd, itStart, itEnd); //this will be just the size of the block
			newData.setName("Undo Matrix [new]");
			newData.disconnectListening();
			data.copyDataBlock(oldData, icStart, icEnd, itStart, itEnd);
			notification = new Notification(MesquiteListener.DATA_CHANGED);
			if (itStart==itEnd)
				subcodes = new int[] {MesquiteListener.SINGLE_TAXON};
			if (subcodes!=null)
				notification.setSubcodes(subcodes);
			data.notifyListeners(this, notification);
			if (undoCommand!=null)
				if (itStart==itEnd)
					doCommand(""+itStart);
				else
					doCommand("");
			return new UndoInstructions(changeClass, newData, data, icStart, icEnd, itStart, itEnd, 0, icEnd-icStart, 0, itEnd-itStart, true);

		case ALLTAXONNAMES:
			if (taxa == null || namesList==null)
				return null;
			oldNamesList = null;
			oldNamesList = new String[taxa.getNumTaxa()];
			for (int i = 0; i < oldNamesList.length; i++)
				oldNamesList[i] = taxa.getTaxonName(i);
			for (int i = 0; i < namesList.length && i < taxa.getNumTaxa(); i++)
				taxa.setTaxonName(i, namesList[i], false);
			taxa.notifyListeners(this, new Notification(MesquiteListener.NAMES_CHANGED));
			return new UndoInstructions(changeClass, oldNamesList, taxa);

		case ALLCHARACTERNAMES:
			if (data == null || namesList==null)
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
