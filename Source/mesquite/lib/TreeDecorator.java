/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib;

import java.awt.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
/** Used for decorations of tree - shading, node pictures, node labels, etc.  Modules can have classes to
subclass this to create objects whose methods are called when the tree drawing needs to be updated.  TreeDecorators
are more purely for display; TreeDisplayExtras often use them.  TreeDisplayExtras are more of a combination of calculation
and display.*/
public abstract class TreeDecorator {
	public TreeDisplay treeDisplay;
	public TreeDisplayExtra ownerExtra;
	public static long totalCreated = 0;
	public TreeDecorator (TreeDisplay treeDisplay, TreeDisplayExtra ownerExtra) {
		this.treeDisplay = treeDisplay;
		this.ownerExtra = ownerExtra;
		totalCreated++;
	}
	
	public void calculateOnTree(Tree tree, int drawnRoot, Object obj){
	}
	public void drawOnTree(Tree tree, int drawnRoot, Object obj, Object obj2, Object obj3, Graphics g){
	}
	public void drawOnTree(Tree tree, int drawnRoot, Graphics g){
	}
	public String textAtNode(Tree tree, int node) {
		return "";
	}
	public String additionalText(Tree tree, int node) {
		return "";
	}
	public abstract void turnOff();
	/*  */
	public void setThemeColor(Color c){
	}
	public void useGrayScale(boolean useGray){  
	}
	public void useColorTable(MesquiteColorTable table){  
	}
	
	public ColorRecord[] getLegendColorRecords(){
		return null;
	}
}


