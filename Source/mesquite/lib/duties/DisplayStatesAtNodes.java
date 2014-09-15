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
/**Displays character states on tree, for instance for character tracing.  Example modules: LabelStatesOnTree,
ShadeStatesOnTree, SpotStatesOnTree.*/

public abstract class DisplayStatesAtNodes extends MesquiteModule  {

   	 public Class getDutyClass() {
   	 	return DisplayStatesAtNodes.class;
   	 }
 	public String getDutyName() {
 		return "Display States At Nodes";
   	 }
   	 
   	 public String[] getDefaultModule() {
   	 	return new String[] {"#ShadeStatesOnTree"};
   	 }
   	 
   	 /** create tree decorator which will do the drawing.  The tree decorator's 
   	 method drawOnTree(Tree tree, Object obj, Graphics g) will be called to draw the states*/
	public TreeDecorator createTreeDecorator(TreeDisplay treeDisplay, TreeDisplayExtra ownerExtra) {
		return null;
	}

}


