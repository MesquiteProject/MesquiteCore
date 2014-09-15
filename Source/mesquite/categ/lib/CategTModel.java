/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.categ.lib;

import java.awt.*;
import java.util.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;


/* ======================================================================== */
/**An interface for character models that contain a matrix concerning state-to-state transitions.*/

public interface CategTModel extends Listenable, Listable {
	/*.................................................................................................................*/
	/** Returns true if size (maximum state allowed) can be set externally. */
   	 public boolean canChangeSize();
	/*.................................................................................................................*/
	/** Sets maximum state allowed. */
	public void setMaxStateDefined(int maxState);
	/*.................................................................................................................*/
	/** Returns maximum state allowed. */
	public int getMaxStateDefined();
	/*.................................................................................................................*/
	/** Gets symbol for state. */
	public String getStateSymbol(int state);
	/*.................................................................................................................*/
	/** Returns the value (cost, rate, etc.) for a transition between beginState and endState. */
	public MesquiteNumber getTransitionValue (int beginState, int endState, MesquiteNumber result);
	/*.................................................................................................................*/
	/** Sets the value (cost, rate, etc.) for a transition between beginState and endState. */
	public void setTransitionValue (int beginState, int endState, MesquiteNumber result, boolean notify);

}


