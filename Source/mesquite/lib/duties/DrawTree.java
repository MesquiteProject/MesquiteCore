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

import java.awt.Dimension;
import java.util.Vector;

import mesquite.lib.MesquiteModule;
import mesquite.lib.tree.TreeDisplay;
import mesquite.lib.tree.TreeDisplayHolder;
import mesquite.lib.tree.TreeDrawing;


/* ======================================================================== */
/** This is superclass of all Mesquite tree drawing modules.  Creates a TreeDrawing, which does
the drawing of the tree.  Example modules: ArcTree, BallsNSticks, CircularTree, Constellation, DiagonalDrawTree,
PlotTree, SquareTree.*/

public abstract class DrawTree extends MesquiteModule  {

   	public Class getDutyClass() {
   		return DrawTree.class;
   	}
   	 
	 public String getFunctionIconPath(){
   		 return getRootImageDirectoryPath() + "functionIcons/treeWindow.gif";
   	 }
	public String getDutyName() {
 		return "Draw Tree";
   	}
   	public String[] getDefaultModule() {
   		return new String[] { "#SquareLineTree", "#DiagonalDrawTree"};
   	}
   	 public abstract Vector getDrawings();
	/** Returns the preferred size (if any) of the tree drawing */
	public Dimension getPreferredSize(){
		return null;
	}
	/** Returns true if it will have a preferred size of the tree drawing */
	public boolean hasPreferredSize(){
		return false;
	}
	
	/** Returns true if other modules can control the orientation */
	public boolean allowsReorientation(){
		TreeDisplayHolder twm = (TreeDisplayHolder)findEmployerWithDuty(TreeDisplayHolder.class);
		if (twm == null)
			return true;
		return twm.allowsReorientation();
	}
   	 /** Returns a TreeDrawing to be used in the given TreeDisplay, with the given inital number of terminal taxa*/
	public abstract TreeDrawing createTreeDrawing(TreeDisplay treeDisplay, int numTaxa);
	

   	public boolean isSubstantive(){
   		return false;  
   	}
}


