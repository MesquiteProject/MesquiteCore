/* Mesquite source code, Treefarm package.  Copyright 1997 and onward, W. Maddison, D. Maddison and P. Midford. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.treefarm.lib;
/*~~  */

import mesquite.lib.MesquiteModule;
import mesquite.lib.RandomBetween;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.Tree;

/* ======================================================================== */
public abstract class RndTreeModifier extends MesquiteModule {
  	 public Class getDutyClass() {
   	 	return RndTreeModifier.class;
   	 }
 	public String getDutyName() {
 		return "Random Tree Modifier";
   	 }
   	 
   	 public abstract void modifyTree(Tree tree, MesquiteTree modified, RandomBetween rng);
}

