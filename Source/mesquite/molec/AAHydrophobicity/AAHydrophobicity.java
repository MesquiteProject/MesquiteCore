/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.molec.AAHydrophobicity; 

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.categ.lib.*;
import mesquite.molec.lib.*;


/* ======================================================================== */
public class AAHydrophobicity extends AAProperty {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		return true;
	}
	public void initialize(CharacterData data){
	}
	
	//symbols = new char[] {		'A',  		'C', 	'D', 		'E', 		'F', 		'G', 		'H', 		'I', 		'K', 		'L', 		'M', 		'N', 		'P', 		'Q', 		'R', 		'S', 		'T', 		'V', 		'W', 		'Y', 		'*'};
	double[] kyteDoolittle = new double[] {1.8,	2.5,	-3.5,	 -3.5, 	+2.8, 	-0.4, 	-3.2, 	+4.5, 	-3.9,	 +3.8, 	+1.9, 	-3.5,	-1.6,	-3.5,	-4.5,	-0.8,	-0.7,	+4.2,	-0.9,	-1.3};
	//Kyte J and Doolittle RF Journal of Molecular Biology 157(6): 105-142, 1982

	//http://blanco.biomol.uci.edu/hydrophobicity_scales.html  white octanol scale
	
	double[] hydrophobs = kyteDoolittle;

	public double getProperty(int aa){

		if (aa>=0 && aa<hydrophobs.length)
			return hydrophobs[aa];
		return MesquiteDouble.unassigned;
   	 }

	/*.................................................................................................................*/
   	 public boolean showCitation(){
   	 	return true;
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Kyte & Doolittle Hydrophobicity";
   	 }
	/*.................................................................................................................*/
    	 public boolean isPrerelease() {
		return false;
   	 }
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Returns hydrophobicity of amino acid using the scale of Kyte, J & R.F. Doolittle (1982) J. Mol. Biol. 157:105-142.  Numbers from http://www.whatislife.com/reader/protein/aa.html";
   	 }
}


	


