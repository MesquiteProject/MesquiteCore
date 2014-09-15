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
/** A class of modules that promise no specific function, except in some way to be employeable
to work with a project or file.  By being a FileAssistant, a module can be recognized as hireable by
another module such as the FileCoordinator, and can put menu items to hire it.  Example modules:
MultiTreeWindow, Spectrum, TreesChart, TwoValuesOverTrees.*/

public abstract class FileAssistant extends MesquiteModule  {

   	 public Class getDutyClass() {
   	 	return FileAssistant.class;
   	 }
 	public String getDutyName() {
 		return "Assistant for File";
   	 }
}


