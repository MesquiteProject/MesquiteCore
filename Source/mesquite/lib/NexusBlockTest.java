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

/** Objects of the NexusBlockTest class can be returned by modules on Mesquite's startup.  They are stored in modulesinfo and used to test, 
without having to employ the module, whether the module would be able to read a nexus block.  Thus, if a module
wants to read a particular sort of block, it overrides that MesquiteModule method getNexusBlockTest() so as to instantiate and return
a NexusBlockTest object.  When the NEXUS file is being read and a block is found,
 the nexus file reading module (InterpretNEXUS) looks into the modulesinfo of all the modules and when it finds a NexusBlockTest object, it
 passes it the name of the block and the block itself, and the object returns whether it is readable by the module.  If so, the module is 
 hired and used to process the block.*/
public abstract class NexusBlockTest  {
	/**returns whether or not the module can deal with given block with the given blockName*/
	public abstract boolean readsWritesBlock(String blockName, FileBlock block);  
}

