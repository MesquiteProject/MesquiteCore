/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charMatrices.LogMatrixDetails; 

import java.util.*;
import java.awt.*;

import mesquite.categ.lib.CategoricalState;
import mesquite.categ.lib.DNAData;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class LogMatrixDetails extends DataUtility { 
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		return true;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return false;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return true;
	}
	/** if returns true, then requests to remain on even after operateData is called.  Default is false*/
	public boolean pleaseLeaveMeOn(){
		return false;
	}
	/** Called to operate on the data in all cells.  Returns true if data altered*/
	public boolean operateOnData(CharacterData data){
		long[] checksums = data.getIDOrderedFullChecksum();
		logln("Data matrix: " + data.getName());
		logln("Number of taxa: " + data.getNumTaxa());
		logln("Number of characters: " + data.getNumChars());
		long numGaps = 0;
		long numUnassigned = 0;
		for (int it = 0; it<data.getNumTaxa(); it++){
			for (int ic = 0; ic<data.getNumChars(); ic++){
				if (data.isInapplicable(ic, it))
					numGaps++;
				if (data.isUnassigned(ic, it))
					numUnassigned++;
			}
		}
		logln("Number of unassigned cells (missing data): " + numUnassigned);
		logln("Number of inapplicable cells (gaps): " + numGaps);
		if (data instanceof DNAData){
			DNAData dd = (DNAData)data;
			long numA = 0;
			long numC = 0;
			long numG = 0;
			long numT = 0;
			long numUncertain = 0;
			for (int it = 0; it<data.getNumTaxa(); it++){
				for (int ic = 0; ic<data.getNumChars(); ic++){
					long state = dd.getState(ic,  it);
					if (CategoricalState.isElement(state, 0))
						numA++;
					if (CategoricalState.isElement(state, 1))
						numC++;
					if (CategoricalState.isElement(state, 2))
						numG++;
					if (CategoricalState.isElement(state, 3))
						numT++;
					if (CategoricalState.isUncertain(state))
						numUncertain++;
				}
			}
			logln("Number of As: " + numA);
			logln("Number of Cs: " + numC);
			logln("Number of Gs: " + numG);
			logln("Number of Ts: " + numT);
		}
		logln("Cell states checksum: " + checksums[CharacterData.CS_CellStates]);
		logln("Specs sets checksum: " + checksums[CharacterData.CS_SpecsSets]);

		return true;
	}

	/*.................................................................................................................*/
	public String getName() {
		return "Log Matrix Details";
	}
	/*.................................................................................................................*/
	public boolean showCitation() {
		return true;
	}

	/*.................................................................................................................*/
	public String getExplanation() {
		return "Writes to log details about the matrix, including its states checksum.";
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 330;  
	}
}





