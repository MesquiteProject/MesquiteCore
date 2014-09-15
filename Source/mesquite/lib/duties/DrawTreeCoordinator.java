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
import java.util.*;


/* ======================================================================== */
/**A class that supervises the drawing of one or more tree.  It creates TreeDisplay objects
and probably hires DrawTree modules. Old and clunky, it either makes a single TreeDisplay, or multiple
ones.  Example module: BasicTreeDrawCoordinator*/

public abstract class DrawTreeCoordinator extends MesquiteModule  {
	protected TreeDisplay treeDisplay;
	protected TreeDisplay[] treeDisplays;
	protected Vector assistantTasks;
	protected int numDisplays = 0;
	public boolean getSearchableAsModule(){
		return false;
	}
	 public String getFunctionIconPath(){
   		 return getRootImageDirectoryPath() + "functionIcons/treeWindow.gif";
   	 }

   	 public Class getDutyClass() {
   	 	return DrawTreeCoordinator.class;
   	 }
 	public String getDutyName() {
 		return "Draw Tree Coordinator";
   	 }
   	 
	/** Returns the preferred size (if any) of the tree drawing */
	public abstract Dimension getPreferredSize();

	/** Returns true if will have preferred size for the tree drawing */
	public abstract boolean hasPreferredSize();

	/** return the module responsible for drawing terminal taxon names. */
   	 public abstract DrawNamesTreeDisplay getNamesTask();
   	 
	/** Create one tree display in the given window. */
 	public abstract TreeDisplay createOneTreeDisplay(Taxa taxa, MesquiteWindow window);
	/** Create a vector of tree displays. */
 	public abstract TreeDisplay[] createTreeDisplays(int numDisplays, Taxa taxa, MesquiteWindow window);
	/** Create a vector of tree displays, each with a different Taxa object. */
 	public abstract TreeDisplay[] createTreeDisplays(int numDisplays, Taxa[] taxa, MesquiteWindow window);

	/** sets branch color */
 	public abstract void setBranchColor(Color c);
 	
 	

	/** Add tree display assistant */
	public void addAssistantTask(TreeDisplayAssistant mb) {
		if (assistantTasks==null)
			assistantTasks= new Vector();
		assistantTasks.addElement(mb);
	}
	
	/** Remove tree display assistant */
	public void removeAssistantTask(TreeDisplayAssistant mb) {
		if (assistantTasks!=null)
			assistantTasks.removeElement(mb);
	}

   	public boolean isSubstantive(){
   		return false;  
   	}
}


