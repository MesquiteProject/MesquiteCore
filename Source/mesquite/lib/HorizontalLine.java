/* Mesquite source code.  Copyright 2001-2011 D. Maddison and W. Maddison. 
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

/*===============================================*/
/** Draws a horizontal line */
public class HorizontalLine extends Component  {
	int thickness;
	ExtensibleDialog dialog;
	int startx=0;
	
	HorizontalLine(ExtensibleDialog dialog, int thickness, int startx){
		this.thickness = thickness;
		this.dialog = dialog;
		this.startx = startx;
	}
	HorizontalLine(ExtensibleDialog dialog, int thickness){
		this.thickness = thickness;
		this.dialog = dialog;
		startx=0;
	}
	/*.................................................................................................................*/
	public void paint (Graphics g) {
		Dimension d = dialog.getPreferredSize();
		int length = d.width;
		setSize(new Dimension(length,thickness));
		setLocation(0,getLocation().y);
		g.setClip(0,0,getBounds().width, getBounds().height);
//		length=dialog.getBounds().width - 2*inset;
		g.setColor(Color.black);
		for (int i=0; i< thickness; i++)
			g.drawLine(startx,i,length,i);
	}
}



