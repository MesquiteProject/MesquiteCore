/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.TaxonsetsListSelect;
/* created May 02 */

import mesquite.lists.lib.*;

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class TaxonsetsListSelect extends TaxonsetsListUtility {
	/*.................................................................................................................*/
	public String getName() {
		return "Select Taxa in Set";
	}

	public String getExplanation() {
		return "Selects taxa that are in that set." ;
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	



	/** if returns true, then requests to remain on even after operateOnTaxas is called.  Default is false*/
	public boolean pleaseLeaveMeOn(){
		return false;
	}
	/** Called to operate on the Taxa block using the taxSet.  Returns true if action is done*/
	public boolean operateOnTaxaWithTaxonSet(Taxa taxa,TaxaSelectionSet taxSet, MesquiteTable table){
		if (taxa !=null && taxSet!=null&& table!=null) {
			
			logln("selecting taxa in Taxon Set " + taxSet.getName());
			for (int i=0; i<taxa.getNumTaxa(); i++)
				taxa.setSelected(i, taxSet.isBitOn(i));
			taxa.notifyListeners(this, new Notification(MesquiteListener.SELECTION_CHANGED));
			return true;
		}

		return false;
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
		return NEXTRELEASE;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;  
	}
	public void endJob() {
		super.endJob();
	}

	
	
 	
 	
 	
}

