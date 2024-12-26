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
/** DOCUMENT */
public class ColorDialog extends ExtensibleDialog implements Colorable  {
	Color color = null;
	//float[] initialColor = null;
	private	Color initC = null;
	ColorPickerPanel field;
	Panel cPanel;
	Panel buttons;
	public ColorDialog(MesquiteWindow parent, String title, String message) {
		super(parent, title);
		field = new ColorPickerPanel (this, Color.white, 50);
		addNewDialogPanel(field);

		/*
		
		Font f = new Font ("Dialog", Font.PLAIN, 12);
		add("Center", field = new ColorPickerPanel (this, Color.white, 50));
		buttons = new Panel();
		Button cancel = new Button("Cancel");
		Button ok= new Button("OK");
		cPanel = new Panel();
		//cPanel.setBackground(Color.white);
		buttons.add("West", cPanel);
		buttons.add("Center", cancel);
		buttons.add("East", ok );
		setDefaultButton("OK");
		//cancel.setBackground(Color.white);
		//ok.setBackground(Color.white);
		cancel.setFont(f);
		ok.setFont(f);
		add("South", buttons);
		setSize(360, 360);
		
		MesquiteWindow.centerWindow(this);
		*/
	}
	public void setInitialColor(Color current){
		initC = current;
		if (field == null)
			return;
	//initialColor =  Color.RGBtoHSB(current.getRed(), current.getGreen(), current.getBlue(), null);
		field.setInitialColor(current);
	}
	/*
	 * public void buttonHit(String buttonLabel, Button button) {
		if (buttonLabel.equalsIgnoreCase("OK")) {
				cPanel.getBackground();
				dispose();
		}
		else
			dispose();
	}
	*/
	public void setColor(Color c){
		setInitialColor(c);
	}
	
	public Color getColor(){

		return field.getColor();
	}
	public static Color queryColor(MesquiteWindow parent, String title, String message, Color current){
		ColorDialog cd = new ColorDialog(parent, title, message);
		cd.color = null;
		if (current!=null) {
			cd.setInitialColor(current);
		}
	 		cd.completeAndShowDialog();
			boolean ok = cd.query()==0;
			Color c = cd.getColor();
			cd.dispose();
			if (!ok)
				return null;

		return c;
	}
}




