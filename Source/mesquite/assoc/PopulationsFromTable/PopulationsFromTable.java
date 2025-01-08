/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.assoc.PopulationsFromTable;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.assoc.lib.*;

/* ======================================================================== */
public class PopulationsFromTable extends PopulationsAndAssociationMaker {

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	public String getKeywords(){
		return "genes species";
	}

	public boolean isPrerelease(){
		return true;
	}
	/*.................................................................................................................*/
	

	
	/*.................................................................................................................*/
   	public TaxaAssociation makePopulationsAndAssociation(Taxa specimensTaxa, ObjectContainer populationTaxaContainer) {
		Taxa populations = null;
		//find population names in table and make taxa block
		Debugg.println("OOPS NOT WORKING YET");
		if (populations == null)
			return null;
		AssociationsManager manager = (AssociationsManager)findElementManager(TaxaAssociation.class);
		TaxaAssociation	association = manager.makeNewAssociation(populations, specimensTaxa, "Populations-Specimens");
			//now associate populations with specimens
			//see PopulationsFromSpecimenNames for useful code
			if (populationTaxaContainer != null)
				populationTaxaContainer.setObject(populations);
			return association;
	
   	}

	/*.................................................................................................................*/
	public String getName() {
		return "Make Populations from Specimens Table";
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Specimens – Populations Table";
	}

	
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Makes a new taxa block of populations based on a table of a specimens and populations.";
	}
	
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}

}

