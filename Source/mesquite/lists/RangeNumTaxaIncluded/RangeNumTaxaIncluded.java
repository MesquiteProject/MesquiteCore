/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.RangeNumTaxaIncluded;
/*~~  */

import java.util.Vector;

import mesquite.lib.CommandChecker;
import mesquite.lib.EmployeeNeed;
import mesquite.lib.ListableVector;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteListener;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteNumber;
import mesquite.lib.MesquiteString;
import mesquite.lib.Notification;
import mesquite.lib.NumberArray;
import mesquite.lib.Pausable;
import mesquite.lib.Snapshot;
import mesquite.lib.StringArray;
import mesquite.lib.duties.NumberForTree;
import mesquite.lib.duties.NumberForTreeBlock;
import mesquite.lib.table.MesquiteTable;
import mesquite.lib.tree.TreeVector;
import mesquite.lists.RangeNumForTreeInBlockList.RangeNumForTreeInBlockList;
import mesquite.lists.lib.TreeblocksListAssistant;

/* ======================================================================== */
public class RangeNumTaxaIncluded extends RangeNumForTreeInBlockList {
	/*.................................................................................................................*/
	public String getName() {
		return "Range of Num. Taxa Included (in List of Tree Blocks window)";
	}
	public String getNameForMenuItem() {
		return "Range of # Taxa Included";
	}
	public String getVeryShortName() {
		return "Range # Taxa";
	}
	public String getExplanation() {
		return "Supplies range of number of taxa included for trees in tree blocks for a tree blocks list window." ;
	}
	/*.................................................................................................................*/
	public Class getHireSubchoice(){
		return null;
	}
	/*.................................................................................................................*/
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
			numberTask = (NumberForTree)hireNamedEmployee(NumberForTree.class, "#NumberOfTaxa");
			if (numberTask==null) {
				return sorry("Range in number for trees (for tree block list) can't start because the requested module was not successfully hired");
			}
		
		return true;
	}
	/** Returns whether or not it's appropriate for an employer to hire more than one instance of this module.  
 	If false then is hired only once; second attempt fails.*/
	public boolean canHireMoreThanOnce(){
		return false;
	}
	/*.................................................................................................................*/

	public String getTitle() {
		if (numberTask==null)
			return "";
		return "Range # taxa";
	}



}

