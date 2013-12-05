/* Mesquite source code.  Copyright 1997-2010 W. Maddison and D. Maddison.
Version 2.74, October 2010.
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
/** Manages a type of FileElement and Reads and writes NEXUS block.  Currently subclasses of this are *not*
hired on the basis of being FileElementManager, but rather because they supplied a
nexusBlockTest object that allows the NEXUS interpreting module to figure out what blocks
they can read.  Thus, other modules than FileElementManager's can read and write NEXUS blocks
and commands.  The class FileElementManager exists mostly to supply a default dutyname.  Example modules:
class ManageAssumptionsBlock; class ManageMesquiteBlock; class ManageNotesBlock.  Others subclass subclasses
(e.g. TreesManager).*/

public interface ElementManager{
	/* put in place so that manager could respond by reordering the corresponding NEXUS blocks*/
	public void elementsReordered(ListableVector v);  
	/* put in place so manager could create corresponding NEXUS block*/
	public NexusBlock elementAdded(FileElement e);
	public void elementDisposed(FileElement e);
	public Class getElementClass();
	public MesquiteModule showElement(FileElement e);
	public void deleteElement(FileElement e);
}

