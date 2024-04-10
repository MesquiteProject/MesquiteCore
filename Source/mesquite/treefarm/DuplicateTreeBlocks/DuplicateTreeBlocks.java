/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.treefarm.DuplicateTreeBlocks;

import java.awt.*;
import java.util.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lists.lib.*;


/* ======================================================================== */
public class DuplicateTreeBlocks extends TreeBlockListUtility  {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		return true;
	}
	/*.................................................................................................................*/
   	public boolean isPrerelease(){
   		return false;
   	}
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}

   	/** if returns true, then requests to remain on even after operateOnTrees is called.  Default is false*/
   	public boolean pleaseLeaveMeOn(){
   		return false;
   	}
   	/** Called to operate on the tree blocks.  Returns true if tree blocks altered*/
   	public boolean operateOnTreeBlocks(TreeVector[] blocks){
   		if (blocks == null || blocks.length <1 || blocks[0]==null) {
   			alert("Sorry, no tree blocks were chosen or available for duplication.");
   			return false;
   		}
		incrementMenuResetSuppression();
		getProject().incrementProjectWindowSuppression();
		Vector v = pauseAllPausables();
  		for (int i=0; i<blocks.length; i++){
   			blocks[i].doCommand("duplicateMe", null, CommandChecker.defaultChecker);
   		}
  		unpauseAllPausables(v);
		getProject().decrementProjectWindowSuppression();
		decrementMenuResetSuppression();
		resetAllMenuBars();
		return true;
   	}
	/*.................................................................................................................*/
    	 public String getNameForMenuItem() {
		return "Duplicate tree blocks...";
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Duplicate tree blocks";
   	 }
   	 
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Duplicates selected tree blocks into a new tree block.";
   	 }
}

