/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.ancstates.aAncStatesIntro;

import mesquite.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class aAncStatesIntro extends PackageIntro {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		return true;
  	 }
  	 public Class getDutyClass(){
  	 	return aAncStatesIntro.class;
  	 }
	/*.................................................................................................................*/
    	 public String getExplanation() {
		return "The ancestral states reconstruction package provides modules to coordinate and display reconstruction of ancestral states on the branches of trees.";
   	 }
   
	/*.................................................................................................................*/
    	 public String getName() {
		return "Ancestral States Reconstruction Package Introduction";
   	 }
    		/*.................................................................................................................*/
    		/** Returns whether package is built-in (comes with default install of Mesquite)*/
    		public boolean isBuiltInPackage(){
    			return true;
    		}
	/*.................................................................................................................*/
	/** Returns the name of the package of modules (e.g., "Basic Mesquite Package", "Rhetenor")*/
 	public String getPackageName(){
 		return "Ancestral States Reconstruction Package";
 	}
	/*.................................................................................................................*/
	/** Returns citation for a package of modules*
 	public String getPackageCitation()
 	
 	NOT overridden because part of the standard Mesquite packages; hence uses standard Mesquite citation
	/*.................................................................................................................*/
	/** Returns whether there is a splash banner*/
	public boolean hasSplash(){
 		return true; 
	}
}

