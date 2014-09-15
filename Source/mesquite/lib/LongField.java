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
/** a field for longs */
public class LongField  {
	ExtensibleDialog dialog;
	SingleLineTextField textField;
	long initialValue=0;
	/*.................................................................................................................*/
	public LongField (ExtensibleDialog dialog, String message, long initialValue, int fieldLength) {
		super();
		this.dialog = dialog;
		this.initialValue = initialValue;
		textField = dialog.addTextField (message, MesquiteLong.toString(initialValue), fieldLength);
	}
	/*.................................................................................................................*/
	public LongField (ExtensibleDialog dialog, String message, int fieldLength) {
		super();
		this.dialog = dialog;
		this.initialValue = 0;
		textField = dialog.addTextField (message, "", fieldLength);
	}
	/*.................................................................................................................*/
	public SingleLineTextField getTextField () {
		return textField;
	}
	/*.................................................................................................................*/
	public long getValue () {
		String s = textField.getText();
		if (s != null && s.equals("?"))
			return MesquiteLong.unassigned;
		long value = MesquiteLong.fromString(s);
		if (!MesquiteLong.isCombinable(value)) {
			value=initialValue;
		}
	        return value;
	}

}

