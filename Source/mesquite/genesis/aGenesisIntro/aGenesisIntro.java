/* Mesquite source code (Genesis package).  Copyright 2001 and onward, D. Maddison and W. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.genesis.aGenesisIntro;
/*~~  */

import mesquite.lib.duties.PackageIntro;


/* ======================================================================== */
public class aGenesisIntro extends PackageIntro {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		return true;
  	 }
  	 public Class getDutyClass(){
  	 	return aGenesisIntro.class;
  	 }
	/*.................................................................................................................*/
    	 public String getExplanation() {
		return "Implements stochastic models of molecular evolution.";
   	 }
   
	/*.................................................................................................................*/
    	 public String getName() {
		return "Genesis Introduction";
   	 }
	/*.................................................................................................................*/
	/** Returns the name of the package of modules (e.g., "Basic Mesquite Package", "Rhetenor")*/
 	public String getPackageName(){
 		return "Genesis Package";
 	}
	/*.................................................................................................................*/
	/** Returns whether package is built-in (comes with default install of Mesquite)*/
	public boolean isBuiltInPackage(){
		return true;
	}
	/*.................................................................................................................*/
	/** Returns citation for a package of modules*/
 	public String getPackageCitation(){
 		return "Maddison, D.R.,  & W.P. Maddison.  2006.  Genesis: models of character evolution.  A package of modules for Mesquite. version 1.1.";
 	}
 	
	/*.................................................................................................................*/
  	 public String getPackageVersion() {
		return "1.11";
   	 }
	/*.................................................................................................................*/
  	 public String getPackageAuthors() {
		return "D. & W. Maddison";
   	 }
	/*.................................................................................................................*/
	/** Returns whether there is a splash banner*/
	public boolean hasSplash(){
 		return true; 
	}
}

