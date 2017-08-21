/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib.duties;

import java.awt.*;
import java.util.*;

import mesquite.lib.*;


/* ======================================================================== */
/** Manages lists of trees.  Also reads and writes TREES NEXUS block.  Example module: "Manage TREE blocks" (class ManageTrees).*/

public abstract class TreesManager extends FileElementManager   {
	public static final String WEIGHT = "Weight";

	public boolean getSearchableAsModule(){
		return false;
	}
  	 public Class getDutyClass() {
   	 	return TreesManager.class;
   	 }
 	public String getDutyName() {
 		return "Manager of lists of trees, including read/write TREES block";
   	 }
	
	public abstract void addBlockListener(MesquiteListener ml);
	public abstract void removeBlockListener(MesquiteListener ml);
	public abstract int getNumberTreeBlocks(Taxa taxa, MesquiteFile file);
	public abstract int getNumberTreeBlocks(Taxa taxa);
	public abstract int getNumberTreeBlocks();
	public abstract int getTreeBlockNumber(TreeVector trees);
	public abstract int getTreeBlockNumber(Taxa taxa, TreeVector trees);
	public abstract int getTreeBlockNumber(Taxa taxa, MesquiteFile file, TreeVector trees);
	public abstract TreeVector getTreeBlock(Taxa taxa, int i);
	public abstract TreeVector getTreeBlock(Taxa taxa, MesquiteFile file, int i);
	public abstract TreeVector getTreeBlockByID(long id);  //this uses the temporary run-time id of the tree vector
	public abstract TreeVector getTreeBlockByUniqueID(String uniqueID);  //this uses the unique id of the tree vector
	public abstract String getTreeBlock(TreeVector trees, NexusBlock tB);
	public abstract TreeVector makeNewTreeBlock(Taxa taxa, String name, MesquiteFile f);
	public abstract Taxa findTaxaMatchingTable(TreeVector trees, MesquiteProject proj, MesquiteFile file, Vector table);
	public abstract ListableVector getTreeBlockVector();
	public abstract boolean queryAboutNumericalLabelIntepretation(boolean[] interps, String c, MesquiteString n);
	public Class getElementClass(){
		return TreeVector.class;
	}
   	public boolean isSubstantive(){
   		return false;  
   	}
}


