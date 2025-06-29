/* Mesquite (package mesquite.io).  Copyright 2000 and onward, D. Maddison and W. Maddison. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.bayesian.ExportForBEAST;
/*~~  */

import mesquite.bayesian.lib.ExportForBEASTLib;



public class ExportForBEAST extends ExportForBEASTLib {

/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
 		return true;  //make this depend on taxa reader being found?)
  	 }
  	 
	public boolean isPrerelease(){
		return false;
	}
	public boolean isSubstantive(){
		return true;
	}
	/*.................................................................................................................*/
	public  String getProgramName(){
		return "BEAST2";
	}

	/*.................................................................................................................*/
 	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
 	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
 	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
    	public int getVersionOfFirstRelease(){
    		return 380;  
    	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Export NEXUS for BEAST2";
   	 }
	/*.................................................................................................................*/
   	 
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Exports NEXUS files for use by BEAST2 and BEAUTi.(Note: Taxon Selection Sets in Mesquite become Taxsets; the current Character groups/partition become Charsets.)" ;
   	 }
	/*.................................................................................................................*/
   	 
   	 
}
	

