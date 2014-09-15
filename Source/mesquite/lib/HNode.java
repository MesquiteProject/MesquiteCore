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
public interface HNode {
	public static int MOUSEDOWN = 0;
	public static int MOUSEMOVE = 1;
	public static int MOUSEEXIT = 2;
	public HNode[] getHDaughters();
	public HNode getHMother();
	public String getName();
	public String getTypeName();
	public int getNumSupplements();
	public String getSupplementName(int index);
	public void hNodeAction(Container c, int x, int y, int action);
	public void hSupplementTouched(int index);
	public Color getHColor();
	public Image getHImage();
	public boolean getHShow();
}


