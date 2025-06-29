/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib.duties;

import mesquite.lib.MesquiteModule;


/* ======================================================================== */
/** DOCUMENT.*/

public abstract class MatrixCharter extends MesquiteModule  {

   	 public Class getDutyClass() {
   	 	return MatrixCharter.class;
   	 }
 	public String getDutyName() {
 		return "Draw Chart for Matrix";
   	 }

	public  abstract void setMatrix(double[][] matrix, double min1, double max1, double min2, double max2);
//	public  abstract void setMatrix(double[][] matrix, double min1, double max1, double min2, double max2, double best1, double best2, String axis1, String axis2);
	
   	public boolean isSubstantive(){
   		return false;  
   	}
}


