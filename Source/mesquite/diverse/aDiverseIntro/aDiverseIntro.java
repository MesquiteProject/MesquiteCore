/* Mesquite source code.  Copyright 1997 and onward, W. Maddison. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.diverse.aDiverseIntro;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.coalesce.lib.*;


/* ======================================================================== */
public class aDiverseIntro extends PackageIntro {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		return true;
  	 }
  	 public Class getDutyClass(){
  	 	return aDiverseIntro.class;
  	 }
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}
	/*.................................................................................................................*/
    	 public String getExplanation() {
		return "Diverse is a Mesquite package for examining speciation and extinction.";
   	 }
   
	/*.................................................................................................................*/
    	 public String getName() {
		return "Diverse Package";
   	 }
    		/*.................................................................................................................*/
    		/** Returns whether package is built-in (comes with default install of Mesquite)*/
    		public boolean isBuiltInPackage(){
    			return true;
    		}
	/*.................................................................................................................*/
	/** Returns citation for a package of modules*/
 	public String getPackageCitation(){
 		return "Midford, P. & W.P. Maddison. 2011.  Diverse Package for Mesquite.  Version 2.75. http://mesquiteproject.org";
 	}
	/*.................................................................................................................*/
  	 public String getPackageVersion() {
		return "2.75";
   	 }
 	/*.................................................................................................................*/
  	 public int getVersionOfFirstRelease() {
		return 200;
   	 }
	/*.................................................................................................................*/
  	 public String getPackageAuthors() {
		return "P. Midford & W. Maddison";
   	 }
	/*.................................................................................................................*/
  	 public boolean hasSplash() {
		return true;
   	 }
	/*.................................................................................................................*/
	/** Returns the name of the package of modules (e.g., "Basic Mesquite Package", "Rhetenor")*/
 	public String getPackageName(){
 		return "Diverse Package";
 	}
}

