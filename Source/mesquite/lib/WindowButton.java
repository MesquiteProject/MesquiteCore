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


/* ======================================================================== */

public class WindowButton extends Button implements  MouseListener  {
	MesquiteWindow window;
	public WindowButton(String label, MesquiteWindow window) {
		super(label);
		this.window = window;
		addMouseListener(this);
	}

	/*...............................................................................................................*/
	public void mouseClicked(MouseEvent e)   {
  	}
 	/*...............................................................................................................*/
	public void mouseEntered(MouseEvent e)   {
  	}
	/*...............................................................................................................*/
	public void mouseExited(MouseEvent e)   {
  	}
	/*...............................................................................................................*/
	public void  mousePressed(MouseEvent e)   {
	}
	/*...............................................................................................................*/
	public void mouseReleased(MouseEvent e)  {
   		if (window !=null)
   			window.buttonHit(getLabel(), this);
	}
	/*...............................................................................................................*/
	public void mouseDragged(MouseEvent e)  {
	}
	/*...............................................................................................................*/
	public void mouseMoved(MouseEvent e) {
	}
}

