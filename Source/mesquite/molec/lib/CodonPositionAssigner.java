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

import mesquite.categ.lib.DNAData;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.characters.*;
import mesquite.lib.table.*;
/* ======================================================================== *

/* ======================================================================== */
public abstract class CodonPositionAssigner extends MesquiteModule {
   	 public Class getDutyClass() {
   	 	return CodonPositionAssigner.class;
   	 }
 	public String getDutyName() {
 		return "Assigner of Codon Positions";
   	 }
   	 public String[] getDefaultModule() {
   	 	return new String[] {"#MinimizeStopCodons"};
   	 }

   	 //Possible assigners: Deassign Selected
   	 
   	 
   	 /* Fill the passed modelSet with the new codon positions. Return codes from DataAlterer (e.g. DataAlterer.SUCCEEDED)
   	  * NOTE: Work on all sites, not just selected (until DNAData's code, e.g. getCodonTriplet, automatically ignores unselected or excluded). Can deassign selected
   	  * Does not make model set or notify; leave that up to the employer
   	  * */
	public abstract int assignCodonPositions(DNAData data, CodonPositionsSet modelSet);
	
}


