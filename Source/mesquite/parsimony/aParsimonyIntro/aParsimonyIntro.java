/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.parsimony.aParsimonyIntro;
/*~~  */

import mesquite.lib.duties.PackageIntro;


/* ======================================================================== */
public class aParsimonyIntro extends PackageIntro {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		return true;
  	 }
  	 public Class getDutyClass(){
  	 	return aParsimonyIntro.class;
  	 }
	/*.................................................................................................................*/
    	 public String getExplanation() {
		return "Preforms parsimony calculations to count steps and treelength of characters on trees.";
   	 }
   
	/*.................................................................................................................*/
    	 public String getName() {
		return "Parsimony Package Introduction";
   	 }
	/*.................................................................................................................*/
	/** Returns the name of the package of modules (e.g., "Basic Mesquite Package", "Rhetenor")*/
 	public String getPackageName(){
 		return "Parsimony Package";
 	}
	/*.................................................................................................................*/
	/** Returns whether package is built-in (comes with default install of Mesquite)*/
	public boolean isBuiltInPackage(){
		return true;
	}
	/*.................................................................................................................*/
  	 public String getAuthors() {
		return "W.P. Maddison and D.R. Maddison";
   	 }
	/*.................................................................................................................*/
	/** 
 	public String getPackageCitation()
  	public String getPackageVersion()
  	public String getPackageDateReleased()
  	public String getPackageAuthors()
	
 	NOT overridden because part of the standard Mesquite packages; hence uses standard Mesquite citation */
}

