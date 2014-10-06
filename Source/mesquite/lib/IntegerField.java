/* Mesquite source code.  Copyright 2001 and onward, D. Maddison and W. Maddison. 


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
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;



/*===============================================*/
/** a field for ints */
public class IntegerField  {
	ExtensibleDialog dialog;
	SingleLineTextField textField;
	boolean isInteger=true;
	int initialValue=0;
	int min=MesquiteInteger.unassigned;
	int max=MesquiteInteger.unassigned;
	/*.................................................................................................................*/
	public IntegerField (ExtensibleDialog dialog, String message, int initialValue, int fieldLength, int min, int max) {
		super();
		this.dialog = dialog;
		this.initialValue = initialValue;
		if (initialValue==MesquiteInteger.unassigned)
			textField = dialog.addTextField (message, "", fieldLength);
		else
			textField = dialog.addTextField (message, MesquiteInteger.toString(initialValue), fieldLength);
		this.min = min;
		this.max = max;
		dialog.focalComponent = textField;
		
	}
	/*.................................................................................................................*/
	public IntegerField (ExtensibleDialog dialog, String message, int initialValue, int fieldLength) {
		super();
		this.dialog = dialog;
		this.initialValue = initialValue;
		if (initialValue==MesquiteInteger.unassigned)
			textField = dialog.addTextField (message, "", fieldLength);
		else
			textField = dialog.addTextField (message, MesquiteInteger.toString(initialValue), fieldLength);
		dialog.focalComponent = textField;

	}
	/*.................................................................................................................*/
	public IntegerField (ExtensibleDialog dialog, String message, int fieldLength) {
		super();
		this.dialog = dialog;
		this.initialValue = 0;
		textField = dialog.addTextField (message, "", fieldLength);
		dialog.focalComponent = textField;
	}

	/*.................................................................................................................*/
	public void setLabelText(String s) {
		if (textField!=null)
			textField.setLabelText(s);
	}
	/*.................................................................................................................*/
	public SingleLineTextField getTextField() {
		return textField;
	}
	/*.................................................................................................................*/
	public void addActionListener(ActionListener actionListener) {
		textField.addActionListener(actionListener);
	}
	/*.................................................................................................................*/
	public void addKeyListener(KeyListener keyListener) {
		textField.addKeyListener(keyListener);
	}

	/*.................................................................................................................*/
	public boolean isValidInteger() {
		return isInteger;
	}
	/*.................................................................................................................*/
	public void setValue (int value) {
		textField.setText(MesquiteInteger.toString(value));
	}
	/*.................................................................................................................*/
	public int getValue () {
		String s = textField.getText();
		if (s != null && s.equals("?"))
			return MesquiteInteger.unassigned;
		int value = MesquiteInteger.fromString(s);
		isInteger=true;
		if (!MesquiteInteger.isCombinable(value)) {
			value=initialValue;
			isInteger=false;
		} else if (value< min && MesquiteInteger.isCombinable(min))
			value = min;
		else if (value>max && MesquiteInteger.isCombinable(max)) 
			value = max;
		return value;
	}

}

