/* Mesquite source code.  Copyright 1997-2011 W. Maddison and D. Maddison. 
Version 2.75, September 2011.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.DatasetsListDuplicate;

import mesquite.lists.lib.*;

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class DatasetsListDuplicate extends DatasetsListUtility {
	boolean duplicateExcludedCharacters = false;
	/*.................................................................................................................*/
	public String getName() {
		return "Duplicate Selected Matrices";
	}

	public String getExplanation() {
		return "Duplicates selected matrices in List of Character Matrices window." ;
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	/*.................................................................................................................*/
	public boolean queryOptions() {
		if (!MesquiteThread.isScripting()){

			duplicateExcludedCharacters = !AlertDialog.query(containerOfModule(), "Remove excluded characters?","Remove excluded characters?", "Yes", "No");
		}
		return true;
	}

	/** if returns true, then requests to remain on even after operateOnTaxas is called.  Default is false*/
	public boolean pleaseLeaveMeOn(){
		return false;
	}
	/** Called to operate on the CharacterData blocks.  Returns true if taxa altered*/
	public boolean operateOnDatas(ListableVector datas, MesquiteTable table){
		boolean anyExcluded = false;
		for (int im = 0; im < datas.size(); im++){
			CharacterData data = (CharacterData)datas.elementAt(im);
			if (data.numCharsCurrentlyIncluded() < data.getNumChars())
				anyExcluded = true;
		}
		if (anyExcluded)
			queryOptions();
		if (getProject() != null)
			getProject().incrementProjectWindowSuppression();
		for (int im = 0; im < datas.size(); im++){
			CharacterData data = (CharacterData)datas.elementAt(im);
			CharacterData starter = data.makeCharacterData(data.getMatrixManager(), data.getTaxa());  

			starter.addToFile(getProject().getHomeFile(), getProject(),  findElementManager(CharacterData.class));  

			boolean success = starter.concatenate(data, false, duplicateExcludedCharacters, false, false);
			if (success){
				starter.setName(data.getName() + " (duplicate)");
			}


		}
		if (getProject() != null)
			getProject().decrementProjectWindowSuppression();
		resetAllMenuBars();
		return true;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return true;  
	}
	public void endJob() {
		super.endJob();
	}

}

