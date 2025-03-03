/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.DeassignCodonPositions;
/*~~  */

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.DataAlterer;
import mesquite.align.lib.MultipleSequenceAligner;
import mesquite.categ.lib.*;
import mesquite.lib.table.*;
import mesquite.lists.lib.ListModule;
import mesquite.molec.lib.CodonPositionAssigner;

/* ======================================================================== */
public class DeassignCodonPositions extends CodonPositionAssigner {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}

	private void deassign(DNAData data, CodonPositionsSet modelSet){
		boolean anySelected = data.anySelected();
				for (int i=0; i<data.getNumChars(); i++) {
						if (!anySelected || data.getSelected(i))
							modelSet.setValue(i, 0);
				}

	}

	/*.................................................................................................................*/
	/** Called to alter data in those cells selected in table*/
	public int assignCodonPositions(DNAData data, CodonPositionsSet modelSet){
		if (data==null  || modelSet == null) 
			return ResultCodes.INPUT_NULL;
		deassign(data, modelSet);
		return ResultCodes.SUCCEEDED;
	}


	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return true;
	}
	/*.................................................................................................................*/
	public boolean requestPrimaryChoice() {
		return true;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Deassign All or Selected Codon Positions";
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Deassign All or Selected";
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
		return "Deassigns codon positions." ;
	}

}

