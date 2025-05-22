/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charMatrices.PartitionManagementInMatrix; 

import java.awt.event.InputEvent;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.ui.MesquitePopup;


/* ======================================================================== */
public class PartitionManagementInMatrix extends DataWindowAssistantI implements SelectionInformer {
	MesquiteTable table;
	Taxa taxa;
	TaxaSelectedUtility taxaHelperTask;
	CharactersSelectedUtility charsHelperTask;


	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		taxaHelperTask = (TaxaSelectedUtility)hireNamedEmployee(TaxaSelectedUtility.class, "#TaxonPartitionHelper");
		charsHelperTask = (CharactersSelectedUtility)hireNamedEmployee(CharactersSelectedUtility.class, "#CharPartitionHelper");
		setUseMenubar(false);
		return true;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}
	/*.................................................................................................................*/
	public void setTableAndData(MesquiteTable table, CharacterData data){
		this.table = table;
		this.taxa = data.getTaxa();
		taxaHelperTask.setTaxaAndSelectionInformer(taxa, this);
		charsHelperTask.setDataAndSelectionInformer(data, this);
	}
	public boolean isItemSelected (int item, Object caller){
		if (table == null)
			return false;
			if (caller == taxaHelperTask)
				return table.isRowSelected(item) ||  table.isCellSelectedAnyWay(-1, item);
			else
				return table.isCellSelectedAnyWay(item, -1) || table.isColumnSelected(item);
				
	}
	public boolean anyItemsSelected (Object caller){
		if (table == null)
			return false;
	if (caller == taxaHelperTask)
			return table.anyRowSelected() || table.anyCellsInColumnSelectedAnyWay(-1) || table.anyRowNameSelected();
		else
			return table.anyCellsInRowSelectedAnyWay(-1) || table.anyColumnSelected() || table.anyColumnNameSelected();
		
	}
	/**/
	public boolean rowTouched(boolean isArrowEquivalent, int row, EditorPanel panel, int x, int y, int modifiers) {
		if (MesquiteEvent.rightClick(modifiers)){
			taxaHelperTask.taxonTouched(row);
			taxaHelperTask.showPopUp(panel, x, y);
			return true;
		}
		return false;
	}
	/**/
	public boolean columnTouched(boolean isArrowEquivalent, int column, EditorPanel panel, int x, int y,  int modifiers) {
		if (MesquiteEvent.rightClick(modifiers)){
			charsHelperTask.characterTouched(column);
			charsHelperTask.showPopUp(panel, x, y);
			return true;
		}
		return false;
	}
	

	
	/*.................................................................................................................*/
	public String getName() {
		return "Matrix Partitions Helper";
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Helps manage taxa and character partitions in the character matrix editor." ;
	}

}



