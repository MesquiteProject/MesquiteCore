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
import java.awt.event.*;
import mesquite.lib.duties.*;
/* ======================================================================== */
/** The container fitting within the OuterContentArea, excluding the information bar.  The InterContentArea contains
the components (ContentAreas) in which the modules actually draw their stuff.  The ContentArea's added to this are alternative
pages that can be displayed (the primary graphics page, the primary text page, then a series of other pages to give the user
information.*/
class InterContentArea extends Panel {
	CardLayout layout;
	
	public InterContentArea () {
		setLayout(layout = new CardLayout());
	}
	/*.................................................................................................................*/
	/** Shows the page (ContentArea) requested.  The integer passed is the mode parameter from InfoBar (e.g., InfoBar.GRAPHICS) */
	public void showPage(int i) {
		 layout.show(this, Integer.toString(i));
	}
	/*.................................................................................................................*/
	/** Add the given page to the InterContentArea */
	public void addPage(Component c, String which) {
		 add(c, which);
		 layout.addLayoutComponent(c, which);
	}
	
}

