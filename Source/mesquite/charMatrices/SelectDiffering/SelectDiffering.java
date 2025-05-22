/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.charMatrices.SelectDiffering;
/*~~  */

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.ui.ListDialog;

/* ======================================================================== *

*new in 1.03*

/* ======================================================================== */
public class SelectDiffering extends DataWSelectionAssistant {
	MesquiteTable table;
	CharacterData data;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	public boolean pleaseLeaveMeOn(){
		return false;
	}
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
	public void setTableAndData(MesquiteTable table, CharacterData data){
		this.table = table;
		this.data = data;
		select();
	}
	private void select(){
		if (table == null || data == null)
			return;
		Taxa taxa = data.getTaxa();
		int numSets = getProject().getNumberCharMatricesVisible(taxa);
		int numSetsDiff = numSets;
		for (int i = 0; i<numSets; i++) {
			CharacterData pData =getProject().getCharacterMatrixVisible(taxa, i);
			if (pData== data)
				numSetsDiff--;
			else if (pData.getClass() != data.getClass())
				numSetsDiff--;
		}
		if (numSetsDiff<=0) {
			alert("Sorry, there are no other compatible data matrices available for comparison.  If the other matrix is in another file, open the file as a linked file before attempting to compare.");
			return;
		}
		else {
			Listable[] matrices = new Listable[numSetsDiff];
			int count=0;
			for (int i = 0; i<numSets; i++) {
				CharacterData pData =getProject().getCharacterMatrixVisible(taxa, i);
				if (pData!= data && (pData.getClass() == data.getClass())) {
					matrices[count]=pData;
					count++;
				}
			}
			boolean differenceFound=false;
			CharacterData oData = (CharacterData)ListDialog.queryList(containerOfModule(), "Compare with", "Compare data matrix with:", MesquiteString.helpString,matrices, 0);
			if (oData==null)
				return;
				
			CharacterState cs1 = null;
			CharacterState cs2 = null;
			for (int it = 0; it<data.getNumTaxa() && it<oData.getNumTaxa(); it++){
				for (int ic = 0; ic<data.getNumChars() && ic<oData.getNumChars(); ic++){
					cs1 = data.getCharacterState(cs1, ic, it);
					cs2 = oData.getCharacterState(cs2, ic, it);
					if (!cs1.equals(cs2)) {
						table.selectCell(ic, it);
					}
				}
			}
			for (int ic = 0; ic<data.getNumChars() && ic<oData.getNumChars(); ic++){
				String name = data.getCharacterName(ic);
				String oName = oData.getCharacterName(ic);
				if (name == null){
					if (oName != null)
						table.selectCell(ic, -1);
				}
				else if (!name.equals(oName)) {
					table.selectCell(ic, -1);
				}
			}
			table.repaintAll();
		}
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Select by Matrix Comparison";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Selects cells of the matrix that differ compared to another matrix" ;
   	 }
   	 
}


