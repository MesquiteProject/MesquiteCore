/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.trees.ColorTaxonByAssigned;

import java.awt.Color;

import mesquite.lib.duties.TaxonNameStyler;
import mesquite.lib.taxa.Taxa;

/* ======================================================================== */
public class ColorTaxonByAssigned extends TaxonNameStyler {
	double resultNum;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
  	 }
  	 
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
	/*.................................................................................................................*/
   	 public boolean isSubstantive(){
   	 	return false;
   	 }
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
   	 	return false;
   	 }
 	/*.................................................................................................................*/
 	public int getVersionOfFirstRelease(){
 		return 360;  
 	}
 
 	/*.................................................................................................................*/
	
	public Color getTaxonNameColor(Taxa taxa, int ic){
		Color color = taxa.getColor(ic);
		return color;
	
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Color by Assigned Color";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Supplies the colors assigned individually to the taxa." ;
   	 }
}
