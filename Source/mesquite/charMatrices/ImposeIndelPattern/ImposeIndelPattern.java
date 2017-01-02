/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.charMatrices.ImposeIndelPattern;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.*;
import mesquite.lib.table.MesquiteTable;
import mesquite.categ.lib.DNAData;
import mesquite.categ.lib.ProteinData;
import mesquite.categ.lib.ProteinState;
import mesquite.charMatrices.CharMatrixCoordIndep.CharMatrixCoordIndep;
import mesquite.charMatrices.lib.*;

/* ======================================================================== */
public class ImposeIndelPattern extends DataAlterer implements AltererAlignShift {
	CharMatrixCoordIndep characterSourceTask;
	boolean adjustCodonPositions = true;

	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e2 = registerEmployeeNeed(CharMatrixCoordIndep.class, getName() + " needs a module to supply a character matrix.",
		"The matrix source is chosen initially.");
	}

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		characterSourceTask = (CharMatrixCoordIndep)hireEmployee(CharMatrixCoordIndep.class, "Source of Character Matrix containing indel pattern");
			if (characterSourceTask == null)
				return sorry(getName() + " couldn't start because no matrix obtained");
			return true;
  	 }

	/*.................................................................................................................*/
	public boolean alterData(CharacterData data, MesquiteTable table, UndoReference undoReference) {
		if (data==null)
			return false;
		Taxa taxa = data.getTaxa();

		MCharactersDistribution indelMatrix =  characterSourceTask.getCurrentMatrix(taxa);
		if (indelMatrix == null)
			return false;
		CharacterState cs = indelMatrix.getCharacterState(null, 0, 0);
		
		for (int it = 0; it<data.getNumTaxa() && it<indelMatrix.getNumTaxa(); it++) 
			for (int ic = 0;  ic<data.getNumChars() && ic<indelMatrix.getNumChars(); ic++) {
				cs = indelMatrix.getCharacterState(cs, ic, it);
				if (cs!=null && cs.isInapplicable()) {
					data.setToInapplicable(ic, it);
				}
	 	 	}

		return true;
	}

	/*.................................................................................................................*/
    	 public String getName() {
   		return "Impose Indel Pattern of Other Matrix";
   	 }
	/*.................................................................................................................*/
  	 public boolean showCitation() {
		return false;
   	 }
	/*.................................................................................................................*/
  	 public boolean isPrerelease() {
		return false;
   	 }
  	/*.................................................................................................................*/
 	 public boolean requestPrimaryChoice() {
		return false;
  	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Converts each cell in a matrix to a gap (inapplicable) if the equivalent cell in another matrix has a gap." ;
   	 }
	/*.................................................................................................................*/
   	public boolean isSubstantive(){
   		return true;  
   	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 320;  
	}

   	 
}

