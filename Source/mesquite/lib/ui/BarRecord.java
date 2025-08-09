/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib.ui;

import java.awt.Rectangle;



/* ======================================================================== */
	/** For bars on tree branches, e.g. TraceAllChanges
	*/
public class BarRecord {
	public Rectangle r; public int ic;  public long stateset; public int node;public boolean unambiguous;
	public BarRecord(Rectangle r, int ic, long stateset, int node, boolean unambiguous){
		this.r = r;
		this.ic = ic;
		this.stateset=stateset;
		this.node = node;
		this.unambiguous = unambiguous;
	}
	public boolean contains(int x, int y){
		return r.contains(x, y);
	}
}