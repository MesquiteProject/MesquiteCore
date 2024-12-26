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
import mesquite.lib.*;

/* ======================================================================== */
/** A panel at the bottom of a table or window that can be used for temporary messages.*/
public class MessagePanel extends Panel {
	String message;
	Color textColor;
	boolean outline = true;
	public MessagePanel(int colorScheme) {  
		super();
		message="";
		setBackground(ColorTheme.getInterfaceBackground());
		
	}
	public MessagePanel(Color background, boolean outline) {  
		super();
		message="";
		setBackground(background);
		this.outline = outline;
	}
	public void paint(Graphics g) {
	   	if (MesquiteWindow.checkDoomed(this))
	   		return;
		if (outline)
			g.drawRect(0,0, getBounds().width, getBounds().height-1);
		if (textColor !=null)
			g.setColor(textColor);
		if (message != null)
			g.drawString(message,  4, 12);
		MesquiteWindow.uncheckDoomed(this);
	}
	public void setTextColor(Color c){
		textColor = c;
	}
	public void setMessage(String s) {
		if (s==null)
			message = "";
		else
			message = s;
		repaint();
	}
	public String getMessage(){
		return message;
	}
}

