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

import java.awt.*;
import java.util.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.cont.lib.*;


  /*==================================================*/
	public class CVAOrdination extends Ordination{
		public CVAOrdination(double[][] original, TaxaPartition partition){
			Ordination wpcaResults	=   new wPCAOrdination(original,partition); 
			double[][] rotated 	=  	wpcaResults.getScores();  
			double[][] rescaled = 	MatrixUtil.rescalesqrt(rotated, wpcaResults.getEigenvalues() ); 
			Ordination apcaResults =  new aPCAOrdination(rescaled,partition);
			setResults(apcaResults);
		}
	 	public String getAxisName(int i){
	 		if (percentExplained==null || i>=percentExplained.length || i<0)
	 			return "Error in Canonical Variates";
	 		else
	 			return "CVA" + CharacterStates.toExternal(i) + " (" +MesquiteDouble.toStringDigitsSpecified(100*percentExplained[i], 3) +"%)";
	 	}
  	}

