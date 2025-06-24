/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


 Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
 The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
 Perhaps with your help we can be more than a few, and make Mesquite better.

 Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
 Mesquite's web site is http://mesquiteproject.org

 This source code and its compiled class files are free and modifiable under the terms of 
 GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.basic.IncludeTreesSpecifyTreeDialect;
/*~~  */

import mesquite.basic.OpenFileSpecifyTreeDialect.OpenFileSpecifyTreeDialect;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class IncludeTreesSpecifyTreeDialect extends FileAssistantTM {
	
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		OpenFileSpecifyTreeDialect importerTask = (OpenFileSpecifyTreeDialect)hireNamedEmployee(OpenFileSpecifyTreeDialect.class, "#OpenFileSpecifyTreeDialect");
		if (importerTask == null)
			return false;
		importerTask.readFileWTreeDialect(getProject(), false, true);
		return true;
	}

	/*.................................................................................................................*/
	public boolean isPrerelease() { 
		return false;
	}
	/*.................................................................................................................*/
	public boolean requestPrimaryChoice() { 
		return true;
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Include Trees (Specify Tree Dialect)...";
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Include Trees (Specify Tree Dialect)";
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Reads trees from NEXUS files and PHYLIP/Newick tree files with more flexible reading than in the normal \"Include File\". "
				+"Copies trees into the current project, rather than establishing a new project (for which, see File>Open Special>Open File (Specify Tree Dialect)). "
					+"It can adjust for the diverse and incompatible formats (\"dialects\") used by different programs "
					+"(BEAST, MrBayes, IQ-TREE, etc.) to store special properties in trees such as posterior probabilities, "
					+ "divergence times, or concordance factors. (The regular \"Include File\" can read such tree information only if saved in the standard BEAST/Mesquite format.)"
					+ "" ;
	}

}


