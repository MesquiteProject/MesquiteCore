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

/** An object of this kind can be returned by getNexusCommandTest that will be stored in the modulesinfo vector and used
to search for modules that can read a particular command in a particular block.  (Much as the NexusBlockObject.)*/
public abstract class NexusCommandTest  {
	/**returns whether or not the module can deal with command*/
	public abstract boolean readsWritesCommand(String blockName, String commandName, String command);  
}


