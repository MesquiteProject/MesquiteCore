/* Mesquite source code.  Copyright 1997 and onward, W. Maddison. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.coalesce.lib;

import mesquite.lib.MesquiteLong;
import mesquite.lib.MesquiteModule;
import mesquite.lib.tree.MesquiteTree;


/* ======================================================================== */

public abstract class Coalescer extends MesquiteModule   {

   	 public Class getDutyClass() {
   	 	return Coalescer.class;
   	 }
 	public String getDutyName() {
 		return "Coalescence simulator";
   	 }
   	public abstract void coalesce(MesquiteTree tree,  int[] containing, int whichContaining, double neAdjustment, long generations, MesquiteLong seed, boolean untilOne);
	public abstract int getPopulationSize();
   	public abstract void setPopulationSize(int pS);
}


