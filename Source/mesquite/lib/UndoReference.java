/* Mesquite source code.  Copyright 1997-2009 W. Maddison and D. Maddison.
Version 2.71, September 2009.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lib;

import mesquite.lib.characters.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.table.*;

public class UndoReference {

	Undoer[] undoer;
	MesquiteModule responsibleModule;

	public UndoReference() {
	}

	public UndoReference(CharacterData data, MesquiteModule responsibleModule) {
		UndoInstructions undoInstructions = data.getUndoInstructionsAllData();
		if (undoInstructions!=null) 
			undoInstructions.setNewData(data);
		setUndoer(new Undoer[] {undoInstructions});
		setResponsibleModule(responsibleModule);
	}

	public UndoReference(CharacterData data, MesquiteModule responsibleModule, int icStart, int icEnd, int itStart, int itEnd) {
		UndoInstructions undoInstructions = new UndoInstructions (UndoInstructions.DATABLOCK, data, data, icStart,icEnd, itStart, itEnd, icStart, icEnd, itStart, itEnd,true);
		//undoInstructions.setNewData(data);
		setUndoer(new Undoer[] {undoInstructions});
		setResponsibleModule(responsibleModule);
	}
	
	public static final int ALLCHARACTERNAMES = 7;

	/** a constructor for an Undoreference that preserves both data and the taxon names */
	public UndoReference(CharacterData data, MesquiteModule responsibleModule, int icStart, int icEnd, int itStart, int itEnd, int[] undoableObjects) {
		if (undoableObjects==null) return;
		UndoInstructions[] undoInstructions = new UndoInstructions[undoableObjects.length];
		for (int i=0; i<undoableObjects.length; i++) {
			switch(undoableObjects[i]) {
			case UndoInstructions.ALLDATACELLS:
				undoInstructions[i]= data.getUndoInstructionsAllData();
				if (undoInstructions!=null)
					undoInstructions[i].setNewData(data);
				break;
			case UndoInstructions.DATABLOCK:
				undoInstructions[i]= new UndoInstructions (UndoInstructions.DATABLOCK, data, data, icStart,icEnd, itStart, itEnd, icStart, icEnd, itStart, itEnd,true);
				break;

			case UndoInstructions.ALLTAXONNAMES:
				if (data!=null)
					undoInstructions[i]= new UndoInstructions(UndoInstructions.ALLTAXONNAMES,data.getTaxa(), data.getTaxa());
				break;
			case UndoInstructions.ALLCHARACTERNAMES:
				undoInstructions[i] = new UndoInstructions(UndoInstructions.ALLCHARACTERNAMES,data, data);
				break;
			}
		}
		setUndoer(undoInstructions);
		setResponsibleModule(responsibleModule);
	}

	public UndoReference(Undoer[] undoer, MesquiteModule responsibleModule) {
		this.undoer = undoer;
		this.responsibleModule = responsibleModule;
	}
	
	public UndoReference(Undoer undoer, MesquiteModule responsibleModule) {
		this.undoer = new Undoer[] {undoer};
		this.responsibleModule = responsibleModule;
	}

	public MesquiteModule getResponsibleModule() {
		return responsibleModule;
	}

	public void setResponsibleModule(MesquiteModule responsibleModule) {
		this.responsibleModule = responsibleModule;
	}

	public Undoer[] getUndoer() {
		return undoer;
	}

	public void setUndoer(Undoer[] undoer) {
		this.undoer = undoer;
	}

	public void setUndoer(Undoer undoer) {
		if (this.undoer!=null) {
			for (int i=0; i<this.undoer.length; i++)
				if (this.undoer[i]!=null && undoer != this.undoer[i])
				this.undoer[i].dispose();
		}
		this.undoer = new Undoer[] {undoer};
	}
	
	public void dispose() {
		if (undoer!=null) {
			for (int i=0; i<undoer.length; i++)
				if (this.undoer[i]!=null)
					undoer[i].dispose();
		}
	}


	public static UndoReference getUndoReferenceForMatrixSelection(CharacterData data, MesquiteTable table, MesquiteModule responsibleModule){
		if (data!=null) 
			if (table!=null) {
				MesquiteInteger firstRow= new MesquiteInteger();
				MesquiteInteger lastRow= new MesquiteInteger();
				MesquiteInteger firstColumn= new MesquiteInteger();
				MesquiteInteger lastColumn= new MesquiteInteger();
				if (table.singleCellBlockSelected( firstRow,  lastRow,  firstColumn,  lastColumn)) {
					if (table.numRowNamesSelected()>0) 
						return new UndoReference(data,responsibleModule,firstColumn.getValue(), lastColumn.getValue(), firstRow.getValue(),lastRow.getValue(), new int[] {UndoInstructions.DATABLOCK, UndoInstructions.ALLTAXONNAMES});
					else
						return new UndoReference(data,responsibleModule,firstColumn.getValue(), lastColumn.getValue(), firstRow.getValue(),lastRow.getValue(), new int[] {UndoInstructions.DATABLOCK});
				}
			}
			else
				return new UndoReference(data,responsibleModule);
		return null;
	}


}


