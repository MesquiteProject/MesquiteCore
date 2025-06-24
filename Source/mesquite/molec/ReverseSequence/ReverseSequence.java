/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.ReverseSequence;
/*~~  */

import mesquite.categ.lib.MolecularData;
import mesquite.lib.characters.AltererAlignShift;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.DataAltererCon;

/* ======================================================================== */
public class ReverseSequence extends DataAltererCon   implements AltererAlignShift {

	public boolean alterBlockInTaxon(CharacterData data, int icStart, int icEnd, int it) {
		if (data==null)
			return false; 

		if (data instanceof MolecularData){
			MolecularData mData = (MolecularData)data;
			mData.reverse(icStart, icEnd, it, false, true);
		}
		else 
			for (int i=0; i <= (icEnd-icStart)/2 && icStart+i< icEnd-i; i++) {  
				data.tradeStatesBetweenCharacters(icStart+i, icEnd-i, it,true);
			}

		return true;
	}

	public boolean alterBlockOfCharacters(CharacterData data, int icStart, int icEnd) {
		if (data==null)
			return false; 
		if (data instanceof MolecularData){
			MolecularData mData = (MolecularData)data;
			mData.reverse(icStart, icEnd, true);
		}
		else {
			for (int it = 0; it< data.getNumTaxa(); it++)
				for (int i=0; i <= (icEnd-icStart)/2 && icStart+i< icEnd-i; i++) {  
					data.tradeStatesBetweenCharacters(icStart+i, icEnd-i, it,true);
				}
		}

		return true;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Reverse Sequence";
	}
	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return false;
	}
	/*.................................................................................................................*/
	public boolean showCitation(){
		return true;
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Alters data by reversing sequence of states." ;
	}

}


