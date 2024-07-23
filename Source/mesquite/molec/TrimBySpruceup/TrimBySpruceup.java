/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.TrimBySpruceup;
/*~~  */

import mesquite.lib.Debugg;
import mesquite.lib.duties.MatrixFlagger;
import mesquite.lib.duties.MatrixFlaggerForTrimming;
import mesquite.molec.lib.TrimSitesByFlagger;

/* ======================================================================== */
public class TrimBySpruceup extends TrimSitesByFlagger {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		flaggerTask = (MatrixFlagger)hireNamedEmployee(MatrixFlaggerForTrimming.class, "#FlagBySpruceup");
		if (flaggerTask == null)
			return false;
		return super.startJob(arguments, condition, hiredByName);  //call after to have superclass do extra things
	}

	
	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return true;
	}

	/*.................................................................................................................*/
	public boolean showCitation(){
		return false;
	}
	/*.................................................................................................................*/
	/*Subclass override and return true if they want to be called iteratively*/
	protected boolean pleaseIterate(){
		return ((mesquite.molec.FlagBySpruceup.FlagBySpruceup)flaggerTask).pleaseIterate();
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Trim by Spruceup";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Deletes sites or blocks of sites according to a reduced version of the Spruceup criterion (Boroweic 2018)." ;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}

}


