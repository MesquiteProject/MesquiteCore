/* NINJA source code.  Copyright 2010 Travis Wheeler and David Maddison.

NINJA is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.NINJA.aNINJAIntro;

import mesquite.lib.duties.PackageIntro;

/* ======================================================================== */
public class aNINJAIntro extends PackageIntro {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	public Class getDutyClass(){
		return aNINJAIntro.class;
	}
	/*.................................................................................................................*/
	public String getExplanation() {
		return "NINJA is a package which provides access to NINJA within Mesquite. If you use it, you should also cite\n" +
		"Wheeler, T.J. 2009. Large-scale neighbor-joining with NINJA.\n" + 	 
		"In S.L. Salzberg and T. Warnow (Eds.), Proceedings of the 9th Workshop on Algorithms in Bioinformatics. WABI 2009, pp. 375-389. Springer, Berlin.";
	}

	/*.................................................................................................................*/
	/** Returns whether package is built-in (comes with default install of Mesquite)*/
	public boolean isBuiltInPackage(){
		return true;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "NINJA Package";
	}
	/*.................................................................................................................*/
	/** Returns the name of the package of modules (e.g., "Basic Mesquite Package", "Rhetenor")*/
	public String getPackageName(){
		return "NINJA Package";
	}
	/*.................................................................................................................*/
	/** Returns citation for a package of modules*/
	public String getPackageCitation(){
		return "Wheeler, T. J., and Maddison, D.R.  2010. NINJA.  A package of modules for creating neighbor-joining trees in Mesquite. Version 1.0.  "+
		"Please also cite Wheeler, T.J. 2009. Large-scale neighbor-joining with NINJA. " + 	 
		"In S.L. Salzberg and T. Warnow (Eds.), Proceedings of the 9th Workshop on Algorithms in Bioinformatics. WABI 2009, pp. 375-389. Springer, Berlin.";
	}
	/*.................................................................................................................*/
	/** Returns whether there is a splash banner*/
	public boolean hasSplash(){
		return true; 
	}
	/*.................................................................................................................*/
	/** returns the URL of the notices file for this module so that it can phone home and check for messages */
	public String  getHomePhoneNumber(){ 
		return "http://mesquiteproject.org/packages/NINJA/notices.xml";
	}
	/*.................................................................................................................*/
	public String getPackageURL(){
		return "http://mesquiteproject.org/packages/NINJA";  
	}
	/*.................................................................................................................*/
	 public String getPackageVersion() {
		return "1.00";
 	 }
		/*.................................................................................................................*/
 	 public int getPackageVersionInt() {
		return 100;
  	 }
		/*.................................................................................................................*/
 	 public boolean loadModule() {
		return false;
  	 }

	/*.................................................................................................................*/
	public int getVersionOfFirstRelease(){
		return 272;  
	}
}
