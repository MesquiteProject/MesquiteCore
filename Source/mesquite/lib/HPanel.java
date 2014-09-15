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
public abstract class HPanel extends ScrollPane{
	public HPanel () {
	}
	public abstract void setRootNode(HNode node);
	public abstract void setTitle(String title);
	public abstract void renew();
	public abstract void disposeReferences();
	public abstract void dispose();
	public abstract void highlightNode(HNode node);
	public abstract void setBackground(Color color);
	public abstract void setDefaultDepth(int depth);
	public abstract void showTypes(boolean s);
}

