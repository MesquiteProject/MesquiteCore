/* Mesquite source code.  Copyright 2001 and onward, D. Maddison and W. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib.ui;

import java.awt.*;

import mesquite.lib.MesquiteInteger;

/*===============================================*/
/** a field for text that cannot have line feeds/carriage returns in it.  */
public class SimpleIntegerField extends MQTextField  {
	boolean isInteger=true;
	int initialValue=0;
	int min=MesquiteInteger.unassigned;
	int max=MesquiteInteger.unassigned;
	/*.................................................................................................................*/
	public SimpleIntegerField (int initialValue, int fieldLength, int min, int max) {
		super(""+initialValue,fieldLength);
		this.initialValue = initialValue;
		this.min = min;
		this.max = max;
	}
	/*.................................................................................................................*/
	public Dimension getPreferredSize() {
		Dimension d = super.getPreferredSize();
		d.height +=4;
		return d;
	}
	/*.................................................................................................................*/
	public SimpleIntegerField (String initialString) {
		super(initialString);
	}
	/*.................................................................................................................*/
	public int getValue () {
		String s = getText();
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
