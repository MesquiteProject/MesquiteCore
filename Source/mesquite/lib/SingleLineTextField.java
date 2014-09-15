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
/** a field for text that cannot have line feeds/carriage returns in it.  */
public class SingleLineTextField extends TextField  {
	Label fieldLabel; 
	boolean preserveBlanks = false;
	/*.................................................................................................................*/
	public SingleLineTextField (String initialString, int fieldLength, boolean preserveBlanks) {
		super(initialString,fieldLength);
		this.preserveBlanks = preserveBlanks;
	}
	/*.................................................................................................................*/
	public SingleLineTextField (String initialString, int fieldLength) {
		super(initialString,fieldLength);
	}
	/*.................................................................................................................*/
	public SingleLineTextField (String initialString, boolean preserveBlanks) {
		super(initialString);
		this.preserveBlanks = preserveBlanks;
	}
	/*.................................................................................................................*/
	public Label getLabel() {
		return fieldLabel;
	}
	/*.................................................................................................................*/
	public void setLabel(Label fieldLabel) {
		this.fieldLabel = fieldLabel;
	}
	/*.................................................................................................................*/
	public void setLabelText(String s) {
		if (fieldLabel!=null)
			fieldLabel.setText(s);
	}
	/*.................................................................................................................*/
	public Dimension getPreferredSize() {
		Dimension d = super.getPreferredSize();
		d.height +=4;
		return d;
	}
	/*.................................................................................................................*/
	public SingleLineTextField (String initialString) {
		super(initialString);
	}
	/*.................................................................................................................*/
	public String getText () {
		int count = 0;
		while (count++<10){
			try {
				String s = super.getText();
				if (s==null)
					return "";
				s = StringUtil.removeNewLines(s);
			        if (StringUtil.blank(s)) {
			        	if (preserveBlanks)
			        		return s;
			        	else
			        	 	return "";
			        }
			        else
			        	return s;
		        }
		        catch (Exception e){
		        	//MesquiteMessage.warnProgrammer("Exception in getText of SingleLineTextField");
		        }
	        }
        	return "";
	}

}

