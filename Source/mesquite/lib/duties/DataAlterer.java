/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib.duties;

import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.table.*;
import mesquite.lib.characters.*;


/* ======================================================================== */
/**This is superclass of modules to alter a data matrix.*/

public abstract class DataAlterer extends MesquiteModule  {
	protected  long numCellsAltered =MesquiteLong.unassigned;
	 public String getFunctionIconPath(){
   		 return getRootImageDirectoryPath() + "functionIcons/matrixEditorUtil.gif";
   	 }

   	 public Class getDutyClass() {
   	 	return DataAlterer.class;
   	 }
 	public String getDutyName() {
 		return "Data Alterer";
   	}
   	
   	public long getNumCellsAltered(){
   		return numCellsAltered;
   	}
   	/** if returns true, then requests to remain on even after operateData is called.  Default is false*/
   	public boolean pleaseLeaveMeOn(){
   		return false;
   	}
	/*.................................................................................................................*/
   	/** Called to alter data in those cells selected in table.  Returns true if data altered.  For those DataAlterers that supply AlteredDataParameters, this method should be overridden. */
   	public  boolean alterData(mesquite.lib.characters.CharacterData data, MesquiteTable table, UndoReference undoReference, AlteredDataParameters alteredDataParameters){
   		return alterData(data,table,undoReference);
   	}
   	
	/*.................................................................................................................*/
   	/** Called to alter data in those cells selected in table.  Returns true if data altered*/
   	public abstract boolean alterData(mesquite.lib.characters.CharacterData data, MesquiteTable table, UndoReference undoReference);
   	
	/*.................................................................................................................*/
   	/** Called to alter the data in a single cell.  If you use the alterContentOfCells method of this class, 
   	then you must supply a real method for this, not just this stub. */
   	public void alterCell(mesquite.lib.characters.CharacterData data, int ic, int it){
   	}
   	
	/*.................................................................................................................*/
   	/** Called to alter data in cells in table. This is used if the altering procedure can be done on one cell
   	at a time, independent of all other cells.  If the altering procedure involves dependencies between cells,
   	then a different method must be built.  */
   	public boolean alterContentOfCells(mesquite.lib.characters.CharacterData data, MesquiteTable table, UndoReference undoReference){
   		if (data.isEditInhibited()){
			discreetAlert("This matrix is marked as locked against editing. To unlock, uncheck the menu item Matrix>Current Matrix>Editing Not Permitted");
   			return false;
   		}
   		UndoInstructions undoInstructions = data.getUndoInstructionsAllMatrixCells(new int[] {UndoInstructions.NO_CHAR_TAXA_CHANGES});
   		numCellsAltered =MesquiteLong.unassigned;
		boolean did=false;
 		if (table==null && data!=null){    // alter entire matrix
			for (int i=0; i<data.getNumChars(); i++)
				for (int j=0; j<data.getNumTaxa(); j++) {
						alterCell(data,i,j);
				}
			return true;
 		}
 		else if (table!=null && data !=null){
   	 		boolean[][] done = new boolean[table.getNumColumns()][table.getNumRows()];
   			if (table.anyCellSelected()) {
				for (int i=0; i<table.getNumColumns(); i++)
					for (int j=0; j<table.getNumRows(); j++)
						if (!done[i][j] && table.isCellSelected(i,j)) {
							alterCell(data,i,j);
							did = true;
							done[i][j]=true;
						}
			}
			if (table.anyRowSelected()) {
				for (int j=0; j<table.getNumRows(); j++) {
					if (table.isRowSelected(j))
						for (int i=0; i<table.getNumColumns(); i++) {
							if (!done[i][j]) {
								alterCell(data,i,j);
								done[i][j]=true;
							}
							did = true;
						}
				}
			}
			if (table.anyColumnSelected()) {
				for (int i=0; i<table.getNumColumns(); i++){
					if (table.isColumnSelected(i))
						for (int j=0; j<table.getNumRows(); j++) {
							if (!done[i][j]) {
								alterCell(data,i,j);
								done[i][j]=true;
							}
							did=true;
						}
				}
			}
			if (!table.anythingSelected()) {
				for (int i=0; i<data.getNumChars(); i++)
					for (int j=0; j<data.getNumTaxa(); j++) {
							if (!done[i][j])
								alterCell(data,i,j);
					}
				return true;
			}
		}
 		if (undoInstructions!=null) {
 			undoInstructions.setNewData(data);
 			if (undoReference!=null){
 				undoReference.setUndoer(undoInstructions);
 				undoReference.setResponsibleModule(this);
 			}
 		}
		if (MesquiteLong.isCombinable(numCellsAltered))
			logln("Number of data cells altered: " + numCellsAltered);
		return did;
   	}
	/*.................................................................................................................*/
	/** Returns CompatibilityTest so other modules know if this is compatible with some object. */
	public CompatibilityTest getCompatibilityTest(){
		return new CharacterStateTest();
	}
}





