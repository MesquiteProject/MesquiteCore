/* Mesquite source code (Rhetenor package).  Copyright 1997 and onward E. Dyreson and W. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.rhetenor.PrincipalComponents;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.cont.lib.*;
import mesquite.rhetenor.lib.*;

/* ======================================================================== */

public class PrincipalComponents extends Ordinator {
	PCAOrdination ord;
	
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		return true;
 	}
 	
	/*.................................................................................................................*/
 	public Ordination getOrdination(MContinuousDistribution matrix, int item, Taxa taxa){
		double[][] x = matrix.getMatrix(item);//gets first 2Dmatrix from original
 		ord=  new PCAOrdination(x);
		return ord;
 	}
	/*.................................................................................................................*/
    	public String getName() {
		return "Principal Components Analysis";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Performs principal components analysis on a continous-valued matrix." ;
   	 }
   	 
	/*.................................................................................................................*/
   	 public boolean showCitation(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
	/*.................................................................................................................*
    	 public String getParameters() {
   		if (ord==null)
   			return null;
   		else
   			return "Principal Components Analysis\n"+ ord.report();
   	 }
	/*.................................................................................................................*/
}

