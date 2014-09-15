/* Mesquite source code, Treefarm package.  Copyright 1997 and onward, W. Maddison, D. Maddison and P. Midford. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.treefarm.ExtractTreeBlock;
/*~~  */

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lists.lib.*;

/* ======================================================================== */
public class ExtractTreeBlock extends TreeListUtility { 
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		return true;
	}
   	/** if returns true, then requests to remain on even after operateData is called.  Default is false*/
   	public boolean pleaseLeaveMeOn(){
   		return false;
   	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean isPrerelease(){
   		return false;
   	}
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
   	/** Called to operate on the data in all cells.  Returns true if data altered*/
   	public boolean operateOnTrees(TreeVector trees){
   		if (trees == null)
   			return false;
   		Taxa taxa = trees.getTaxa();
   		int numTrees = trees.size();
   		if (!trees.anySelected()) {
   			discreetAlert( "Sorry, no trees are selected.");
   			return false;
   		}

		int treeCount = 0;
    	 	MesquiteFile file = getProject().chooseFile( "Select file to which to add the extracted tree block");
		incrementMenuResetSuppression();
		getProject().incrementProjectWindowSuppression();

		TreeVector fragment = new TreeVector(taxa);
		fragment.setName("Copied from " + trees.getName());
		
		for (int j=0; j<trees.size(); j++){
			if (trees.getSelected(j)){
				Tree tree = trees.getTree(j);
				if (tree!=null)
				fragment.addElement(tree.cloneTree(), false);
			}
		}
		
		fragment.addToFile(file, getProject(), findElementManager(TreeVector.class));  
		fragment.show();
		
		getProject().decrementProjectWindowSuppression();
		decrementMenuResetSuppression();
		resetAllMenuBars();
		return true;
		
	}
	/*.................................................................................................................*/
    	 public String getNameForMenuItem() {
		return "Extract selection as tree block";
   	 }
	/*.................................................................................................................*/
    	 public String getName() {
		return "Extract tree block";
   	 }
   	 
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Extracts selected trees and places them in a new tree block.";
   	 }
}


	


