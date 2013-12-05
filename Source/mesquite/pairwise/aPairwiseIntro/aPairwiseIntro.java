/* Mesquite source code.  Copyright 1997-2011 W. Maddison and D. Maddison.
Version 2.75, September 2011.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.pairwise.aPairwiseIntro;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;


/* ======================================================================== */
public class aPairwiseIntro extends PackageIntro {
	public String getName() {
		return "Pairwise Comparisons Package Introduction";
	}
	public String getExplanation() {
		return "Performs pairwise comparisons character correlation analyses.";
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	public Class getDutyClass(){
		return aPairwiseIntro.class;
	}
	/*.................................................................................................................*/
	/** Returns the name of the package of modules (e.g., "Basic Mesquite Package", "Rhetenor")*/
	public String getPackageName(){
		return "Pairwise Comparisons Package";
	}
	/*.................................................................................................................*/
	/** Returns whether package is built-in (comes with default install of Mesquite)*/
	public boolean isBuiltInPackage(){
		return true;
	}
	/** Returns whether there is a splash banner*/
	public boolean hasSplash(){
		return false; 
	}
	/*.................................................................................................................*/
	/** Returns citation for a package of modules*/
	public String getPackageCitation(){
		return "Maddison, W.P. 2006.  Pairwise comparisons package for Mesquite, version 1.1.  http://mesquiteproject.org";
	}
	/*.................................................................................................................*/
	/** Returns the URL of document shown when splash screen icon touched. By default, returns path to module's manual*/
	public String getSplashURL(){
		String splashP =getPath()+"index.html";
		if (MesquiteFile.fileExists(splashP))
			return splashP;
		else
			return getManualPath();
	}
}

