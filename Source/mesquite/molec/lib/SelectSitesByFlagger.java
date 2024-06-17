/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.lib;
/*~~  */

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.*;
import mesquite.molec.lib.*;

/* ======================================================================== */
public abstract class SelectSitesByFlagger extends CharacterSelector {

 	 public String[] getDefaultModule() {
 	 	return new String[] {"#SelectByPhyIN", "#SelectGappySites"};
 	 }
	protected CharacterData data;
	protected SiteFlagger flaggerTask; // hired by specific subclasses representing those flaggers


	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}

	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}

	Bits flags = null;

	/*.................................................................................................................*/
	/** Called to select characters*/
	public void selectCharacters(CharacterData data){
		if (data!=null && data.getNumChars()>0){
			if (flaggerTask == null)
				return;
			flags = flaggerTask.flagSites( data, flags);
			if (flags == null || flags.getSize()<data.getNumChars())
				return;

				// ======  now select the characters chosen
				for (int ic=0; ic<data.getNumChars(); ic++)
					if (flags.isBitOn(ic))
						data.setSelected(ic, true);


				data.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
		}
	}

}

