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



/*===============================================*/
/** a field for doubles */
public class DoubleField  {
	ExtensibleDialog dialog;
	SingleLineTextField textField;
	double initialValue=MesquiteDouble.unassigned;
	double min = MesquiteDouble.unassigned;
	double max = MesquiteDouble.unassigned;
	boolean permitUnassigned = false;
	int digits = 8;
	double currentValue = initialValue;  //used only for resetting digits
	
	/*.................................................................................................................*/
	public DoubleField (ExtensibleDialog dialog, String message, double initialValue, int fieldLength, double min, double max) {
		super();
		this.dialog = dialog;
		this.initialValue = initialValue;
		if (initialValue==MesquiteDouble.unassigned)
			textField = dialog.addTextField (message, "", fieldLength);
		else
			textField = dialog.addTextField (message, MesquiteDouble.toStringDigitsSpecified(initialValue, digits), fieldLength);
		this.min = min;
		this.max = max;
		dialog.focalComponent = textField;
		currentValue = initialValue;
	}
	/*.................................................................................................................*/
	public DoubleField (ExtensibleDialog dialog, String message, double initialValue, int fieldLength) {
		super();
		this.dialog = dialog;
		this.initialValue = initialValue;
		if (initialValue==MesquiteDouble.unassigned)
			textField = dialog.addTextField (message, "", fieldLength);
		else
			textField = dialog.addTextField (message, MesquiteDouble.toStringDigitsSpecified(initialValue, digits), fieldLength);
		dialog.focalComponent = textField;
		currentValue = initialValue;
	}
	/*.................................................................................................................*/
	public DoubleField (ExtensibleDialog dialog, String message, int fieldLength) {
		super();
		this.dialog = dialog;
		this.initialValue = 0.0;
		textField = dialog.addTextField (message, "", fieldLength);
		dialog.focalComponent = textField;
	}
	
	/*.................................................................................................................*/
	public void setDigits(int d){
		digits = d;
		textField.setText(MesquiteDouble.toStringDigitsSpecified(currentValue, digits));
	}
	/*.................................................................................................................*/
	public void setPermitUnassigned(boolean permit){
		permitUnassigned = permit;
		if (initialValue == MesquiteDouble.unassigned && StringUtil.blank(textField.getText()))
			textField.setText("?");
	}
	/*.................................................................................................................*/
	public SingleLineTextField getTextField() {
		return textField;
	}
	/*.................................................................................................................*/
	public double getValue () {
		String s = textField.getText();
		double value = MesquiteDouble.fromString(s);
		if (!MesquiteDouble.isCombinable(value)) {
			if (!(permitUnassigned && value == MesquiteDouble.unassigned))
				value=initialValue;
		} else if (value< min && MesquiteDouble.isCombinable(min))
			value = min;
		else if (value>max && MesquiteDouble.isCombinable(max)) 
			value = max;
		return value;
	}
	/*.................................................................................................................*/
	/** This method returns the value as the initial value if it is out of bounds, otherwise return the value in the field.
	It also sets the value of "success" to false if the value is out of bounds, but it does NOT set the value to true if it is in bounds.*/
	public double getValue (MesquiteBoolean success) {  // 
		String s = textField.getText();
		double value = MesquiteDouble.fromString(s);
		if (!MesquiteDouble.isCombinable(value)) {
			if (!(permitUnassigned && value == MesquiteDouble.unassigned)){
				value=initialValue;
				
				success.setValue(false);
			}
		}
		else if (value< min) {
			value = initialValue;
			success.setValue(false);
		}
		else if (value>max){
			value = initialValue;
			success.setValue(false);
		}
	       	return value;
	}
	/*.................................................................................................................*/
	public void setValue (double value) {
		textField.setText(MesquiteDouble.toStringDigitsSpecified(value, digits));
		currentValue = value;
	}

}

