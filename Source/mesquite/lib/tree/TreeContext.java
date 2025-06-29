/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib.tree;

import mesquite.lib.Context;
import mesquite.lib.Listable;
import mesquite.lib.MesquiteModule;


/* ======================================================================== */
/**An interface to be used by modules etc. so that employees can find the current tree in their context.*/
public interface TreeContext extends Context, Listable {
	public Tree getTree ();
	public String getContextName ();
	public void addTreeContextListener (TreeContextListener listener);
	public void removeTreeContextListener (TreeContextListener listener);
	public MesquiteModule getTreeSource ();  //returns source of trees for context so that source can avoid using itself as context for modifications (e.g. SourceModifiedTree)
}




