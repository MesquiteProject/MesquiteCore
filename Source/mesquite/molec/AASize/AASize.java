/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.molec.AASize; 

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.categ.lib.*;
import mesquite.molec.lib.*;


/* ======================================================================== */
public class AASize extends AAProperty {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		return true;
	}

	//symbols = new char[] {		'A', 	'C', 	'D', 		'E', 		'F', 		'G', 		'H', 		'I', 		'K', 		'L', 		'M', 		'N', 		'P', 		'Q', 		'R', 		'S', 		'T', 		'V', 		'W', 		'Y', 		'*'};
	double[] weights = new double[] {	89.1,	121.16,	133.11,	147.13,	165.19,	75.01, 	155.16, 	131.18, 	146.19,	 131.18, 	149.21, 	132.12,	151.13,	128.10,	174.2,	105.10,	119.12,	117.15,	204.23,	181.19};
	//numbers from web page http://www.whatislife.com/reader/protein/aa.html which cites Kite & Doolittle, 1982
	
	
	public double getProperty(int aa){
		if (aa>=0 && aa<weights.length)
			return weights[aa];
		return MesquiteDouble.unassigned;
   	 }

	/*.................................................................................................................*/
    	 public boolean isPrerelease() {
		return false;
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Amino Acid Molecular Weight";
   	 }
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Returns molecular weight of amino acid";
   	 }
	/*.................................................................................................................*/
   	 public boolean showCitation(){
   	 	return true;
   	 }
}


	


