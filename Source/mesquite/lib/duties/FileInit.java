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
/**A class of modules that promises no particular services, but which is automatically hired by a file coordinator 
module when a file is read.  Such modules can be used to initialize things for each file.  For instance, InitializeParsimony currently
initializes default CharacterModels for each file.  Example modules: "Data Window Coordinator", "Tree Window Coordinator"
"Initialize Parsimony", "Initialize Likelihood", "Manage Character Models", "Manage character selection sets",
"Manage model sets".*/

public abstract class FileInit extends MesquiteModule  {

   	 public Class getDutyClass() {
   	 	return FileInit.class;
   	 }
 	public String getDutyName() {
 		return "File Init";
   	 }
	public ProjPanelPanel getProjectPanelPanel(){
		return null;
	}

}


