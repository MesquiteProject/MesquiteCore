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

/*===============================================*/
/** A dialog box to return two strings*/
public class TwoStringsDialog extends ExtensibleDialog {
	SingleLineTextField str1Result;
	SingleLineTextField str2Result;
	TextArea str2ResultLong;
	MesquiteString str1;
	MesquiteString str2;
	MesquiteBoolean answer;

	public TwoStringsDialog (MesquiteWindow parent,  String title, String label1, String label2, MesquiteBoolean answer,  MesquiteString str1, MesquiteString str2, boolean secondLong) {
		this(parent, title, label1, label2, answer, str1, str2, false, secondLong);
	}
	
	public TwoStringsDialog (MesquiteWindow parent,  String title, String label1, String label2, MesquiteBoolean answer,  MesquiteString str1, MesquiteString str2, boolean obscureSecond, boolean secondLong) {
		super(parent,title);
		this.str2 = str2;
		this.str1 = str1;
		this.answer = answer;
		answer.setValue(false);
		
		addLabel(label1 + ": ", Label.LEFT);
		str1Result = addTextField(this.str1.getValue());
		addLabel(label2 + ": ", Label.LEFT);
		if (secondLong) {
			str2ResultLong = addTextArea(this.str2.getValue(),3);
			if (obscureSecond)
				str2ResultLong.setFont(defaultSmallFont);
		}
		else {
			str2Result = addTextField(this.str2.getValue());
			if (obscureSecond)
				str2Result.setFont(defaultSmallFont);
		}

		str1Result.requestFocus();

		completeAndShowDialog(true,null);
		boolean ok = (query()==0);
		if (ok) {
			str1.setValue(str1Result.getText());
			if (secondLong)
				str2.setValue(str2ResultLong.getText());
			else
				str2.setValue(str2Result.getText());
			answer.setValue(true);
		}
		dispose();

	}
	
}



