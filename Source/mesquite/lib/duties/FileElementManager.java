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
/** Manages a type of FileElement and Reads and writes NEXUS block.  Currently subclasses of this are *not*
hired on the basis of being FileElementManager, but rather because they supplied a
nexusBlockTest object that allows the NEXUS interpreting module to figure out what blocks
they can read.  Thus, other modules than FileElementManager's can read and write NEXUS blocks
and commands.  The class FileElementManager exists mostly to supply a default dutyname.  Example modules:
class ManageAssumptionsBlock; class ManageMesquiteBlock; class ManageNotesBlock.  Others subclass subclasses
(e.g. TreesManager).*/

public abstract class FileElementManager extends MesquiteModule implements ElementManager   {

	public boolean getSearchableAsModule(){
		return false;
	}
  	 public Class getDutyClass() {
   	 	return FileElementManager.class;
   	 }
 	public String getDutyName() {
 		return "Manager of file elements";
   	 }
	/*.................................................................................................................*/
	public abstract MesquiteModule showElement(FileElement e);
	/*.................................................................................................................*/
	public void deleteElement(FileElement e){
		MesquiteMessage.warnProgrammer("oops, " + getName() + " doesn't delete yet");
	}
	/*.................................................................................................................*/
	/*introduced in 1. 06 to get around issue of sequence of ID assignments; this allows ID assignments to happen early in Mesquite block.
	This method is called in the Mesquite block manager */
  	 public Snapshot getIDSnapshot(MesquiteFile file) {
  	 	return null;
  	 }

}


