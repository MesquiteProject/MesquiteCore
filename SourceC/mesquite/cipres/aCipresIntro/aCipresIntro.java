/* Mesquite source code, CIPRES package.  Copyright 1997-2005 W. Maddison, D. Maddison and P. Midford. 
Version 1.06, August 2005.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.cipres.aCipresIntro;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;


/* ======================================================================== */
public class aCipresIntro extends PackageIntro {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {
 		return true;
  	 }
  	 public Class getDutyClass(){
  		 return aCipresIntro.class;
  	 }
	/*.................................................................................................................*/
    	 public String getExplanation() {
		return "Serves as an introduction to the CIPRES Rec-I-DCM3 package for Mesquite.";
   	 }
   
	/*.................................................................................................................*/
    	 public String getName() {
		return "CIPRES Rec-I-DCM3 Package Introduction";
   	 }
	/*.................................................................................................................*/
	/** Returns the name of the package of modules (e.g., "Basic Mesquite Package", "Rhetenor")*/
 	public String getPackageName(){
 		return "CIPRES Rec-I-DCM3";
 	}
	/*.................................................................................................................*/
	/** Returns citation for a package of modules*/
 	public String getPackageCitation(){
 		return "A. Borchers, M. T. Holder, P. O. Lewis, T. Liebowitz, D. R. Maddison, W. P. Maddison, D. L. Swofford: CIPRES Rec-I-DCM3 package for Mesquite";
 	}
	/*.................................................................................................................*/
  	 public String getPackageVersion() {
		return "1.00";
   	 }
	/*.................................................................................................................*/
  	 public String getPackageAuthors() {
		return "A. Borchers, M. T. Holder, P. O. Lewis, T. Liebowitz, D. R. Maddison, W. P. Maddison, D. L. Swofford";
   	 }
	/*.................................................................................................................*/
	/** Returns whether there is a splash banner*/
	public boolean hasSplash(){
 		return true; 
	}
}
