/* Mesquite source code.  Copyright 1997-2009 W. Maddison and D. Maddison. 
Version 2.71, September 2009.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */package mesquite.lib.duties;


import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.table.*;

public abstract class DataAltererCon extends DataAlterer {

	/** An abstract sublcass of DataAlterer intended to only work on contiguous blocks of selected cells in each taxon.
	 * It requires that AT MOST ONE such block of cells is selected in each taxon; i.e., within each taxon discontinuous selections
	 * are not allowed. */
		
		public boolean startJob(String arguments, Object condition, boolean hiredByName) {
			return true;
		}

	   	/** Called to alter data in those cells selected in table*/
	   	public boolean alterData(CharacterData data, MesquiteTable table,  UndoReference undoReference){
	   		if (data.getEditorInhibition()){
	   			discreetAlert("This matrix is protected against editing");
	   			return false;
	   		}
	  			boolean did=false;
	  			UndoInstructions undoInstructions = data.getUndoInstructionsAllData();
	   	 		if ((table==null || !table.anyCellSelectedAnyWay()) && data!=null){
	   	 				
						for (int i=0; i<data.getNumTaxa(); i++)
							alterBlockInTaxon(data, 0, data.getNumChars()-1, i);
						did = true;
	   	 		}
	   	 		else if (table!=null && data !=null){
	   	 			boolean okToFlip = true;
	   	 			boolean somethingToFlip = false;
	   				for (int j=0; j<table.getNumRows(); j++) {
	   					if (table.singleContiguousBlockSelected(j, null, null))
	   						somethingToFlip = true;
	   					else if (table.numCellsSelectedInRow(j)>0){ //selected, but not contiguous
	   						alert("Sorry, can't "+ getName() + " when selection of characters is discontinuous within any taxa");
	   						okToFlip = false;
	   						break;
	   					}
	   				}
	   	 			
	   	 			if (okToFlip && somethingToFlip){
	   	 					MesquiteInteger start = new MesquiteInteger();
	   						MesquiteInteger end = new MesquiteInteger();
	   						for (int j=0; j<table.getNumRows(); j++) {
	   							if (table.singleContiguousBlockSelected(j, start, end)){
	   								alterBlockInTaxon(data, start.getValue(), end.getValue(), j);
	   		 						did = true;
	   		 					}
	   						}
	   	 			}
					
				}
	   	 		if (undoInstructions!=null) {
	   	 			undoInstructions.setNewData(data);
	   	 			if (undoReference!=null){
	   	 				undoReference.setUndoer(undoInstructions);
	   	 				undoReference.setResponsibleModule(this);
	   	 			}
	   	 		}
	   			if (did)
	   				data.notifyInLinked(new Notification(DATA_CHANGED));
				return did;
	   	}
	   	
	   	
		public abstract boolean alterBlockInTaxon(CharacterData data, int icStart, int icEnd, int it);

	}
