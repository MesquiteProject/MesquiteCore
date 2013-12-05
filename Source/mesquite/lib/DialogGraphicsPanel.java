/* Mesquite source code.  Copyright 1997-2011 W. Maddison and D. Maddison.
Version 2.75, September 2011.
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

public class DialogGraphicsPanel extends Panel {
	ExtensibleDialog dialog;
	
	public DialogGraphicsPanel(ExtensibleDialog dialog){
		super();
		this.dialog = dialog;
//		setBackground(Color.blue);
	}
	/*.................................................................................................................*/
	public Dimension getPreferredSize () {
		return new Dimension(50,50);
	}
	/*.................................................................................................................*/
	public void paint (Graphics g) {
		g.setColor(Color.black);
		g.drawLine(0,0,50,50);
		g.drawLine(getLocation().x, getLocation().y, getLocation().x+getBounds().width, getLocation().y+getBounds().height);
	}
   	
}


