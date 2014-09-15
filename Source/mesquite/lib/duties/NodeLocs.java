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
/**This class of modules assigns locations to nodes in a drawn tree.  Most modules use subclasses
of this (e.g., NodeLocsVH, NodeLocsCircle).*/

public abstract class NodeLocs extends MesquiteModule  {
	/** calculates the locations for a tree drawn within the given rectangle.  Should not be called during paint(); hence Graphics g removed as parameter May 02*/
	public abstract void calculateNodeLocs(TreeDisplay treeDisplay, Tree tree, int drawnRoot, Rectangle rect);
	 public String getFunctionIconPath(){
   		 return getRootImageDirectoryPath() + "functionIcons/treeWindow.gif";
   	 }
  	
	/** returns whether compatible with given orientation (e.g., UP, RIGHT, CIRCULAR, etc.)*/
   	public boolean compatibleWithOrientation(int orientation) {
   		return false;
   	}
	/** sets the TreeDisplay's orientation to be the default desired by the module.*/
   	public void setDefaultOrientation(TreeDisplay treeDisplay) {
   	}
	/** gets the module's default tree display orientation.*/
   	public int getDefaultOrientation(){
   		return TreeDisplay.FREEFORM;
   	}
   	
   	 public Dimension getPreferredSize(){
   	 	return null;
   	 }
 	/** Returns true if it will have a preferred size of the tree drawing */
 	public boolean hasPreferredSize(){
 		return false;
 	}
	public String getDutyName() {
 		return "Node Location Calculator";
   	 }
   	 public Class getDutyClass() {
   	 	return NodeLocs.class;
   	 }
   	public boolean isSubstantive(){
   		return false;  
   	}
}


