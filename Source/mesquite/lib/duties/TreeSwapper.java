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
import mesquite.lib.*;


/* ======================================================================== */
/**Rearranges a given tree.*/

public abstract class TreeSwapper extends MesquiteModule  {
   	public Class getDutyClass() {
   	 	return TreeSwapper.class;
   	}
 	public String getDutyName() {
 		return "Tree Rearranger";
	}
   	 
   	 public String[] getDefaultModule() {
   	 	return new String[] {"#SPRRearranger"};
   	 }
	/** Returns the number of rearrangments, not including the unarranged version of the tree.*/
	public abstract long numberOfRearrangements(AdjustableTree tree);

	/** Returns the number of rearrangments, not including the unarranged version of the tree.*/
	public abstract long numberOfRearrangements(AdjustableTree tree, int baseNode);

	/** Rearranges the tree to the i'th rearrangment. */
	public abstract void rearrange(AdjustableTree tree, long i);


	/** Rearranges the tree to the i'th rearrangment with the clade marked by the node. */
	public abstract void rearrange(AdjustableTree tree, int baseNode, long i);
}


