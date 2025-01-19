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

/** a subclass for reading nexus files*/
public abstract class NexusFileInterpreter extends FileInterpreterI  {  
	/** returns whether module can read given file.*/
	public abstract boolean canReadFile(MesquiteFile f);
	/** writes the given MesquiteFile belonging to the MesquiteProject.*/
	public abstract void writeFile(MesquiteProject mf, MesquiteFile mNF);
	/** adds nexus block to project (block contains reference to the file it belongs to)*/
	public abstract void addBlock(NexusBlock nb);
	public abstract void removeBlock(NexusBlock nb);
	public abstract NexusBlock findBlock(FileElement e);
	/** finds the ith block of a given type and returns it raw.*/
	public abstract FileBlock readOneBlock(MesquiteProject mf, MesquiteFile f, String blockType, int i);
	public abstract void readFile(MesquiteProject mf, MesquiteFile mNF, String arguments, String[] justTheseBlocks) ;
}


