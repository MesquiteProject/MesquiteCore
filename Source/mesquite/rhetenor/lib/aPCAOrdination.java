/* Mesquite source code (Rhetenor package).  Copyright 1997 and onward E. Dyreson and W. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.rhetenor.lib; 

import mesquite.lib.MesquiteDouble;
import mesquite.lib.characters.CharacterStates;
import mesquite.lib.taxa.TaxaPartition;


  /*==================================================*/
	public class aPCAOrdination extends Ordination{
		public aPCAOrdination(double[][] original, TaxaPartition partition){
			doEigenAnalysis(MatrixUtil.covariance(MatrixUtil.withinGroupMeans(original, partition)), original);
		}
	 	public String getAxisName(int i){
	 		if (percentExplained==null || i>=percentExplained.length || i<0)
	 			return "Error in aPrincipal Components";
	 		else
	 			return "aPC" + CharacterStates.toExternal(i) + " (" +MesquiteDouble.toStringDigitsSpecified(100*percentExplained[i], 3) +"%)";
	 	}
  	}
 

