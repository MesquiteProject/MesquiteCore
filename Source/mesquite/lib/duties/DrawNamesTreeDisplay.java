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
/**This is superclass of Mesquite modules that draw terminal taxon names on trees.  Relies on information in the
TreeDisplay and contained TreeDrawing, such as node locations already calculated.  Example module:
BasicDrawTaxonNames.  Example of use: see BasicTreeDrawCoordinator.*/

public abstract class DrawNamesTreeDisplay extends MesquiteModule  {
	public boolean getSearchableAsModule(){
		return false;
	}
	 public String getFunctionIconPath(){
   		 return getRootImageDirectoryPath() + "functionIcons/treeWindow.gif";
   	 }

   	 public Class getDutyClass() {
   	 	return DrawNamesTreeDisplay.class;
   	 }
 	public String getDutyName() {
 		return "Draw Taxon Names";
   	 }
   	 /** Returns font for node locs modules to use for fontMetrics. */
   	public abstract Font getFont();
   	 /** Draw names on tree in given tree display. */
	public abstract void drawNames(TreeDisplay treeDisplay, Tree tree, int drawnRoot, Graphics g);
   	 /** Find the taxon name box at x,y (returns 0 if none).  NOTE: this system is not great; it relies on
   	 taxon name box locations calculated the last time drawNames was called.  This works fine if this module
   	 has only one TreeDisplay to work with, but if it has multiple it won't work well (would need to pass the
   	 TreeDisplay here) */
	public void setTree(Tree tree) {
	}
	public abstract  int findTaxon(Tree tree, int drawnRoot, int x, int y);
   	 /** Fill taxon M's taxon name box with current color. */
	public abstract  void fillTaxon(Graphics g, int M);
   	 /** Force a complete redraw of the names next time names are drawn. */
	public void invalidateNames(TreeDisplay treeDisplay) {}

   	public boolean isSubstantive(){
   		return false;  
   	}
}


