/* Mesquite (package mesquite.jama).  Copyright 1997-2005 W. Maddison and D. Maddison. 
Version 1.06, August 2005.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.iText.aiTextIntro;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;


/* ======================================================================== */
public class aiTextIntro extends PackageIntro {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, CommandRecord commandRec, boolean hiredByName) {
 		return true;
  	 }
  	 public Class getDutyClass(){
  	 	return aiTextIntro.class;
  	 }
	/*.................................................................................................................*/
    	 public String getExplanation() {
		return "Serves as an introduction to the iText library used in Mesquite.";
   	 }
   
	/*.................................................................................................................*/
    	 public String getName() {
		return "iText Introduction";
   	 }
	/*.................................................................................................................*/
	/** Returns the name of the package of modules (e.g., "Basic Mesquite Package", "Rhetenor")*/
 	public String getPackageName(){
 		return "iText";
 	}
	/*.................................................................................................................*/
	/** Returns information about a package of modules*/
 	public String getPackageCitation(){
 		return "iText";
 	}
	/** Returns whether there is a splash banner*/
	public boolean hasSplash(){
 		return true; 
	}
	/*.................................................................................................................*/
 	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE double,
 	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
 	 * If a NEGATIVE double,  thne the number refers to the local version of the package, e.g. a third party package*/
    	public double getVersionOfFirstRelease(){
    		return 1.07;  
    	}
    	/*.................................................................................................................*/
    	public boolean isPrerelease(){
    		return true;
    	}

}
