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
/**This is superclass of modules to search a portion a data matrix.*/

public abstract class DataSearcher extends MesquiteModule  {

   	 public Class getDutyClass() {
   	 	return DataSearcher.class;
   	 }
 	public String getDutyName() {
 		return "Data Searcher";
   	}
   	
	/*.................................................................................................................*/
   	/** Called to search data in those cells selected in table.  Returns true if search successful*/
   	public abstract boolean searchData(mesquite.lib.characters.CharacterData data, MesquiteTable table);
   	
	/*.................................................................................................................*/
   	/** True iff one cannot ask for a search involving all or part of multiple taxa*/
   	public  boolean canSearchMoreThanOnePiece() {
   		return false;
   	}
   	
	/*.................................................................................................................*/
   	/** Processing to be done after each search.  Returns true iff the number of columns changed in the process).*/
   	public boolean processAfterEachTaxonSearch(mesquite.lib.characters.CharacterData data, int it, int passNumber){
   		return false;
   	}
	/*.................................................................................................................*/
	/** message if search failed to find anything.  */
	public void unsuccessfulSearchMessage(){
	}
	
	/** Returns the number of separate processings that are needed after each search.   E.g., LocalBlaster can require multiple
	 * processings if several local databases are searched.  */
	public int getNumberOfProcessingPassesPerSearch() {
		return 1;
	}
	
	
   	/** Called to search data in a table. This is used if the searching procedure can be done on the selected region in one taxon
   	at a time, independent of all other taxa.  If the searching procedure involves dependencies between taxa,
   	then a different method must be built.  */
   	public boolean searchSelectedTaxa(mesquite.lib.characters.CharacterData data, MesquiteTable table){
		boolean did=false;
 		if (table==null && data!=null){    // alter entire matrix
 			if (canSearchMoreThanOnePiece() || data.getNumTaxa()==1)
 				return false;
			for (int j=0; j<data.getNumTaxa(); j++) {
				if (searchOneTaxon(data,j,0,data.getNumChars())) {
 					for (int i=0; i<getNumberOfProcessingPassesPerSearch(); i++) 
 						processAfterEachTaxonSearch(data, j, i); 
				}
				else
					unsuccessfulSearchMessage();
			}
			did = true;
			return true;
 		}
 		else if (table!=null && data !=null){
 		//	table.resetLastInSelectedBlock();
 			MesquiteInteger row = new MesquiteInteger();
 			MesquiteInteger firstColumn = new MesquiteInteger();
 			MesquiteInteger lastColumn = new MesquiteInteger();
 	
 			while (table.nextSingleRowBlockSelected(row, firstColumn, lastColumn)) { 
 				if (searchOneTaxon(data,row.getValue(), firstColumn.getValue(), lastColumn.getValue())){
 					boolean resetColumns = false;
 					for (int i=0; i<getNumberOfProcessingPassesPerSearch(); i++) 
 						if (processAfterEachTaxonSearch(data, row.getValue(), i))
 							resetColumns=true;
 					if (resetColumns){  //
 						lastColumn.setValue(data.getNumChars());
 						firstColumn.setValue(0);
 					}
 				}
				else {
					unsuccessfulSearchMessage();
				}
				did = true;
 			}
 			if (!did)
				discreetAlert( "Sorry, to use the search you need to have one or more stretches of character states (e.g. a section of sequence) selected in one or more taxa.");
		}
		return did;
   	}
	/*.................................................................................................................*/
   	/** Called to search the data selected .  If you use the searchSelectedTaxa method of this class, 
   	then you must supply a real method for this, not just this stub. */
   	public boolean searchOneTaxon(mesquite.lib.characters.CharacterData data, int it, int icStart, int icEnd){
   		return false;
   	}
	/*.................................................................................................................*/
	/** Returns CompatibilityTest so other modules know if this is compatible with some object. */
	public CompatibilityTest getCompatibilityTest(){
		return new CharacterStateTest();
	}
}





