/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.MinimizeStopCodons;
/*~~  */

import mesquite.categ.lib.DNAData;
import mesquite.categ.lib.ProteinData;
import mesquite.lib.MesquiteNumber;
import mesquite.lib.ResultCodes;
import mesquite.lib.characters.CodonPositionsSet;
import mesquite.lib.taxa.Taxa;
import mesquite.molec.lib.CodonPositionAssigner;

/* ======================================================================== */
public class MinimizeStopCodons extends CodonPositionAssigner {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		
		return true;
	}

	private void setPositions(DNAData data, CodonPositionsSet modelSet,  int firstPosition){
		MesquiteNumber num = new MesquiteNumber(firstPosition);
		for (int i=0; i<data.getNumChars(); i++) {
			modelSet.setValue(i, num);
			num.setValue(num.getIntValue()+1);
			if (num.getIntValue()>3)
				num.setValue(1);
		}
	}	
	private void setPositionsMinStops(DNAData data, CodonPositionsSet modelSet){
		Taxa taxa = data.getTaxa();
		int minStops = -1;
		int posMinStops = 1;

		for (int i = 1; i<=3; i++) {
			int totNumStops = 0;
			setPositions(data, modelSet, i);  //set them temporarily

			for (int it= 0; it<taxa.getNumTaxa(); it++) 
				totNumStops += ((DNAData)data).getAminoAcidNumbers(it,ProteinData.TER);					 
			if (minStops<0 || totNumStops<minStops) {
				minStops = totNumStops;
				posMinStops=i;
			}
		}
		setPositions(data, modelSet, posMinStops);

	}

	/*.................................................................................................................*/
	/** Called to alter data in those cells selected in table*/
	public int assignCodonPositions(DNAData data, CodonPositionsSet modelSet){
		//SUGGESTION: could ask about ignoring flanking gappy regions if getAminoAcidNumbers allowed start and end point
		if (data==null  || modelSet == null) 
			return ResultCodes.INPUT_NULL;
		setPositionsMinStops(data, modelSet);
		return ResultCodes.SUCCEEDED;
	}


	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return false;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Minimize Stop Codons";
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
		return "Assigns codon positions so as to minimize stop codons." ;
	}

}

