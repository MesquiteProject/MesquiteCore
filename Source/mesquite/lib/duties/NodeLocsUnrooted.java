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
/**This class of modules assigns node locations for circular trees (orientation UNROOTED).
Example subclass: NodeLocsCircular */

public abstract class NodeLocsUnrooted extends NodeLocs  {
	/**angle of each branch from 0 degrees (defined as direction of standard positive "x" axis).*/
	public double [] angleOfSector;
	/**angle of each branch from 0 degrees (defined as direction of standard positive "x" axis).*/
	public double [] angle;

	/**length of each branch measured radially from center.*/
	public double [] polarLength;
	/**center of circle about which tree is drawn.*/
	public Point treeCenter;
	
	/** gets the module's default tree display orientation.*/
   	public int getDefaultOrientation(){
   		return TreeDisplay.UNROOTED;
   	}
 	public String getDutyName() {
 		return "Node Location (Unrooted)";
   	 }
   	 
   	 public Class getDutyClass() {
   	 	return NodeLocsUnrooted.class;
   	 }
}

