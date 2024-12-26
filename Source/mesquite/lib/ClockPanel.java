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
import java.awt.image.*;


/* ======================================================================== */
/** A little panel placed within a container to indicate an ongoing calculations.  Serves like a 
thermometer window, but embedded in relevant container since there may be several ongoing
at once.*/
public class ClockPanel extends Panel {
	int current=0;
	int total = 0;
	public void setTime(int total, int current) {
		this.current = current;
		this.total = total;
		if (total<=0)
			total = 1;
		if (current> total)
			current = total;
		Graphics g = getGraphics();
		if (g!=null)  {
			paint(g);
			g.dispose();
		}
		/*{
			if (current == 0) {
				g.setColor(Color.white);
				g.fillOval(0,0, getBounds().width, getBounds().height);
			}
			g.setColor(Color.black);
			g.drawOval(0,0, getBounds().width, getBounds().height);
			g.drawOval(1,1, getBounds().width-2, getBounds().height-2);
			g.fillArc(4,4, getBounds().width-8, getBounds().height-8, 90, -current * 360 /total);
		}*/
	}
	public void paint (Graphics g) {
	   	if (MesquiteWindow.checkDoomed(this))
	   		return;
		g.setColor(Color.white);
		g.fillOval(0,0, getBounds().width, getBounds().height);
		g.setColor(Color.black);
		g.drawOval(0,0, getBounds().width, getBounds().height);
		g.drawOval(1,1, getBounds().width-2, getBounds().height-2);
		if (total!=0)
			g.fillArc(4,4, getBounds().width-8, getBounds().height-8, 90, -current * 360 /total);
		MesquiteWindow.uncheckDoomed(this);
	}
}

