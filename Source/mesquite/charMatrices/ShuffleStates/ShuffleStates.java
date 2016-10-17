/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.charMatrices.ShuffleStates;
/*~~  */

import java.util.*;
import java.awt.*;
import java.awt.image.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class ShuffleStates extends DataAlterer implements AltererRandomizations{
	RandomBetween randomTaxon;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
	 	randomTaxon= new RandomBetween();
		randomTaxon.setSeed(System.currentTimeMillis()); //TODO: allow user set seed;
		return true;
	}
		
   	/** Called to alter data in those cells selected in table*/
   	public boolean alterData(CharacterData data, MesquiteTable table,  UndoReference undoReference){
   			boolean did=false;
   			if (data == null)
   				return false;
   			UndoInstructions undoInstructions = data.getUndoInstructionsAllMatrixCells(new int[] {UndoInstructions.NO_CHAR_TAXA_CHANGES});
   	 		if (table==null){
					for (int i=0; i<data.getNumChars(); i++)
						shuffleCells(data, i, 0, data.getNumTaxa()-1);
					return true;
   	 		}
   			if (table.anythingSelected()) {
				for (int i=0; i<table.getNumColumns(); i++) {
	   				int minTax = table.getNumRows();
	   				int maxTax = -1;
					boolean someSelected = false;
					for (int j=0; j<table.getNumRows(); j++) {
						if (table.isCellSelectedAnyWay(i,j)) {
							if (j<minTax)
								minTax=j;
							else if (j>maxTax)
								maxTax = j;
							someSelected = true;
						}
					}
					if (someSelected)
						for (int j=minTax; j<=maxTax; j++)
							if (!table.isCellSelectedAnyWay(i,j)) {
								alert("Sorry, can't shuffle the states when selection of taxa is discontinuous within any character");
								return false;
							}
				}
				for (int i=0; i<table.getNumColumns(); i++) {
	   				int minTax = table.getNumRows();
	   				int maxTax = -1;
					boolean someSelected = false;
					for (int j=0; j<table.getNumRows(); j++) {
						if (table.isCellSelectedAnyWay(i,j)) {
							if (j<minTax)
								minTax=j;
							else if (j>maxTax)
								maxTax = j;
							someSelected = true;
							
						}
					}
					if (someSelected){
						shuffleCells(data, i, minTax, maxTax);
						did = true;
					}
				}
				
			}
			else {
				for (int i=0; i<data.getNumChars(); i++)
					shuffleCells(data, i, 0, data.getNumTaxa()-1);
			}
   			if (undoInstructions!=null) {
   				undoInstructions.setNewData(data);
   				if (undoReference!=null){
   					undoReference.setUndoer(undoInstructions);
   					undoReference.setResponsibleModule(this);
   				}
   			}
			return did;
			
   	}

   	
	void shuffleCells(CharacterData data, int ic, int it, int it2){
   		for (int i=it; i < it2; i++) {
   			int sh = randomTaxon.randomIntBetween(i, it2);
   			if (sh!=i)
   				data.tradeStatesBetweenTaxa(ic, i, sh);
   		}
	}
	
	/*.................................................................................................................*/
    	 public String getName() {
		return "Shuffle states among taxa";
   	 }
	/*.................................................................................................................*/
  	 public boolean showCitation() {
		return true;
   	 }
	/*.................................................................................................................*/
  	 public boolean isPrerelease() {
		return false;
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Alters data by shuffling states among taxa within a character." ;
   	 }
   	 
}


