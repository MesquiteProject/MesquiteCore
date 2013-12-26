/* Mesquite source code.  Copyright 1997-2011 W. Maddison and D. Maddison.
Version 2.75, September 2011.
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
import mesquite.lib.table.*;
import mesquite.lib.characters.*;


/* ======================================================================== */
/**This is superclass of modules to alter a file (e.g. to process matrices within the file).*/

public abstract class FileAlterer extends MesquiteModule  {

   	 public Class getDutyClass() {
   	 	return FileAlterer.class;
   	 }
 	public String getDutyName() {
 		return "File Alterer";
   	}
   	
   	/** if returns true, then requests to remain on even after alterFile is called.  Default is false*/
   	public boolean pleaseLeaveMeOn(){
   		return false;
   	}
	/*.................................................................................................................*/
   	/** Called to alter file. */
   	public  abstract boolean alterFile(MesquiteFile file);
   	

}




