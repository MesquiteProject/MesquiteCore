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
import java.text.*;

/* ======================================================================== */
/**Interface for object to claim it is incrementable, e.g. chooses different characters, and will accept command to change to different one*/
public interface Incrementable {
 	public void setCurrent(long i);  //SHOULD NOT notify (e.g., parametersChanged)
 	public long getCurrent();
 	public String getItemTypeName();
 	public long getMin();
 	public long getMax();
 	public long toInternal(long i); //return whether 0 based or 1 based counting
 	public long toExternal(long i); //return whether 0 based or 1 based counting
}

