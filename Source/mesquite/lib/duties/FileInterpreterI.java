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
/**Superclass of file interpreting modules (e.g., NEXUS file reader/writer).  Different subclasses are expected
to read different data file formats.  Example module: "Interpret NEXUS files" (class InterpretNexus).  Example of use:
see BasicFileCoordinator.*/

/** a subclass for importers (appear in File menu under Import)*/
public abstract class FileInterpreterI extends FileInterpreter  {
	
   	 public Class getDutyClass() {
   	 	return FileInterpreterI.class;
   	 }
	/** exports data to a file.*/
	public abstract boolean exportFile(MesquiteFile file, String arguments);
	
	/** called after hired in case interpreter wants to query user about import*/
	public void getImportOptions(boolean fuse){
	}
  	 public String[] getDefaultModule() {
    	 	return new String[] {"#InterpretFastaDNA", "#InterpretFastaProtein", "#InterpretNEXUS", "#InterpretPhylipTreesBasic"};
    	 }
	
}

