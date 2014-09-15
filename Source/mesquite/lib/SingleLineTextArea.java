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
import java.awt.event.*;

import mesquite.lib.table.EditorTextField;

/*===============================================*/
/** a field for text that cannot have line feeds/carriage returns in it.  */
public class SingleLineTextArea extends TextArea  {
	KeyAdapterToConsumeKeys kListener;
	boolean allowReturn;
	/*.................................................................................................................*/
	public SingleLineTextArea (String initialString, int numRows, int fieldLength, int scrollVis, boolean allowReturn) {
		super(initialString,numRows,fieldLength, scrollVis);
		kListener = new KeyAdapterToConsumeKeys(allowReturn);
		addKeyListener(kListener);
		this.allowReturn = allowReturn;
	}
	/*.................................................................................................................*/
	public SingleLineTextArea (String initialString, boolean allowReturn) {
		super(initialString);
		this.allowReturn = allowReturn;
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
			        if (StringUtil.blank(s))
			        	return "";
			        else
			        	return s;
		        }
		        catch (Exception e){
		        	//MesquiteMessage.warnProgrammer("Exception in getText of SingleLineTextArea");
		        }
	        }
        	return "";
	}


}

