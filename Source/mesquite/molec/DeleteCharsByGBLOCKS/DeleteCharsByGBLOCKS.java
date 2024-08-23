/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.molec.DeleteCharsByGBLOCKS;
/*~~  */



import mesquite.lib.duties.CharacterSelector;
import mesquite.molec.lib.DeleteCharsBySelector;

/* ======================================================================== */
public class DeleteCharsByGBLOCKS extends DeleteCharsBySelector  {
	/*RETAINED TEMPORARILY UNTIL NEW VERSION VIA FlagByGBLOCKS settles in. 
	 * Turn on loadModule here and in other old modules to try out (DeleteCharsByGLBOCKS OLDDeleteCharsByGBLOCKS, OLDGBLOCKSSelector)*/
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		selectorTask = (CharacterSelector)hireNamedEmployee(CharacterSelector.class, "#GBLOCKSSelector");
		if (selectorTask == null)
			return false;
		return true;
	}


	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return false;
	}
	public boolean loadModule() {
		return false;
	}

	/*.................................................................................................................*/
	public boolean showCitation(){
		return false;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Delete Sites by Gblocks";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Deletes characters according to Gblocks criteria." ;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 380;  
	}

}


