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
/**Displays the character states of many characters on the tree, for instance using charts at each node.  Example
module: ChartCharsStOnTree.  Example of use: TraceAllCharacters. 
*/

public abstract class DisplayCharsStsAtNodes extends MesquiteModule  {
   	 public Class getDutyClass() {
   	 	return DisplayCharsStsAtNodes.class;
   	 }
 	public String getDutyName() {
 		return "Display States of characters at nodes";
   	 }
   	 
	/** 	Create TreeDecorator.  The TreeDecorator is used to display the states using a two step process:
	first, its method calculateOnTree(Tree tree, Object obj) is called to prepare the decorator's memory.
	Then, when graphical updates are needed, drawOnTree(tree, drawnRoot, g) is called.  */
	public   TreeDecorator createTreeDecorator(TreeDisplay treeDisplay, TreeDisplayExtra ownerExtra) {
		return null;
	}
}


