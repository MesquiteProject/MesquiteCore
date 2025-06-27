/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.genomic.ShowMatrixWithTreeController;

import mesquite.genomic.ShowMatrixInTreeWindow.ShowMatrixInTreeWindow;
import mesquite.lib.CommandChecker;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteThread;
import mesquite.lib.duties.TreeWindowAssistantN;
import mesquite.lib.tree.Tree;

/* ======================================================================== */
public class ShowMatrixWithTreeController extends TreeWindowAssistantN  {

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (!MesquiteThread.isScripting()){
			MesquiteModule sotitw = findNearestColleagueWithDuty(ShowMatrixInTreeWindow.class);
			sotitw.doCommand("queryOptions", null, CommandChecker.defaultChecker);
			iQuitMainThread();
		}
		return true;
	}
	
	/*.................................................................................................................*/
	public String getName() {
		return "Show Matrix in Tree Window...";
	}
	
	public boolean isPrerelease(){
		return false;
	}

	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Passes info to module that controls the display of a matrix with the tree." ;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 400;  
	}

	public void setTree(Tree tree) {
		
	}

}
