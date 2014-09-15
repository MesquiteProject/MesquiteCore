/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.stochchar.aStochCharIntro;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;


/* ======================================================================== */
public class aStochCharIntro extends PackageIntro {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		return true;
  	 }
  	 public Class getDutyClass(){
  	 	return aStochCharIntro.class;
  	 }
	/*.................................................................................................................*/
    	 public String getExplanation() {
		return "Provides stochastic models and simulations of character evolution.";
   	 }
   
	/*.................................................................................................................*/
    	 public String getName() {
		return "Stochastic Character Evolution Package Introduction";
   	 }
	/*.................................................................................................................*/
	/** Returns the name of the package of modules (e.g., "Basic Mesquite Package", "Rhetenor")*/
 	public String getPackageName(){
 		return "Stochastic Character Evolution Package";
 	}
	/*.................................................................................................................*/
	/** Returns whether package is built-in (comes with default install of Mesquite)*/
	public boolean isBuiltInPackage(){
		return true;
	}
	/*.................................................................................................................*/
	/** Returns citation for a package of modules*/
 	public String getPackageCitation(){
 		return "Maddison, W.P. & D.R. Maddison. 2006.  StochChar: A package of Mesquite modules for stochastic models of character evolution.  Version 1.1.";
 	}
	/*.................................................................................................................*/
	/** Returns whether there is a splash banner*/
	public boolean hasSplash(){
 		return false; 
	}
}

