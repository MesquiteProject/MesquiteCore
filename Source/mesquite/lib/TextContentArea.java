/* Mesquite source code.  Copyright 1997-2010 W. Maddison and D. Maddison.
Version 2.74, October 2010.
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
/** A ContentArea specifically for text display.  Has methods to control text, like a TextArea.*/
class TextContentArea extends ContentArea {
	TextArea tA;
	
	public TextContentArea () {
		super(null);
		mainPanel.setLayout(new CardLayout());
    	 	Font fontToSet = new Font ("Monospaced", 0, 12);
		if (fontToSet!=null)
			setFont(fontToSet);
		tA= new TextArea("", 50, 50,  TextArea.SCROLLBARS_BOTH); //or SCROLLBARS_VERTICAL_ONLY???
		tA.setEditable(false);
		setBackground(Color.white);
		tA.setBackground(Color.white);
		tA.setVisible(true);
		add(tA, "text");
	}
	public void setEditable(boolean ed) {
		tA.setEditable(ed);
	}
	public void print(Graphics g){
		tA.printAll(g);
	}
	public void append(String s) {
		tA.append(s);
	}
	public void setText(String s) {
		tA.setText(s);
	}
	public String getText() {
		return tA.getText();
	}
	public TextArea getTextArea() {
		return tA;
	}
}


