/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


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
		if (datas.size()>4 && MesquiteBoolean.yesNoQuery(containerOfModule(), "Are you sure you want to duplicate " + datas.size() + " matrices?")) {
			return false; 
		}
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

			boolean success = starter.concatenate(data, false, duplicateExcludedCharacters, false, false, false, false);
			if (success){
				starter.setName(datas.getUniqueName(data.getName() + " (duplicate)"));
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
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 300;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;  
	}
	public void endJob() {
		super.endJob();
	}

}

