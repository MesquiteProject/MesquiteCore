/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.OLDGBLOCKSSelector;
/*~~  */

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.molec.lib.*;

/* ======================================================================== */
public class OLDGBLOCKSSelector extends CharacterSelector {

	//Debugg.println RETAINED TEMPORARILY UNTIL NEW VERSION VIA FlagByGBLOCKS settles in. Turn on loadModule here and in other old modules to try out (DeleteCharsByGLBOCKS OLDDeleteCharsByGBLOCKS, OLDGBLOCKSSelector)

	MesquiteString xmlPrefs= new MesquiteString();
	String xmlPrefsString = null;

	OLDGBLOCKSCalculator gblocksCalculator;


	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences(xmlPrefs);
		xmlPrefsString = xmlPrefs.getValue();
		gblocksCalculator = new OLDGBLOCKSCalculator(this, xmlPrefsString);
		return true;
	}
	/*.................................................................................................................*/

	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer();
		if (gblocksCalculator!=null){
			String s = gblocksCalculator.preparePreferencesForXML();
			if (StringUtil.notEmpty(s))
				buffer.append(s);
		}
		return buffer.toString();
	}



	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return true;  //delete this module! Debugg.println
	}
	public boolean loadModule() {
		return false;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return false;  
	}


	/*.................................................................................................................*/
	/** Called to select characters*/
	public void selectCharacters(CharacterData data){
		if (data!=null && data.getNumChars()>0){

			if (!gblocksCalculator.queryOptions(this, "Select"))
				return;

			boolean[] setToSelect = new boolean[data.getNumChars()];

			StringBuffer results = new StringBuffer();
			if (gblocksCalculator.markCharacters(data, this, setToSelect, results)) {
				if (employer.okToInteractWithUser(CAN_PROCEED_ANYWAY, ""))
					logln(results.toString());

				// ======  now select the characters chosen
				for (int ic=0; ic<data.getNumChars() && ic<setToSelect.length; ic++)
					if (setToSelect[ic]==gblocksCalculator.getChooseAmbiguousSites())
						data.setSelected(ic, true);


				data.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
			}
		}
	}
	/*.................................................................................................................*/
	public String getName() {
		return "OLD GBLOCKS Selector";
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "OLD GBLOCKS Selector...";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Selects characters according to an extended version of the GBLOCKS algorithm (Castresana, 2000)." ;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 303;  
	}

}

