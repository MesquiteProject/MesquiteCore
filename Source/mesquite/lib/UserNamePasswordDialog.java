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

import javax.swing.*;

/*===============================================*/
/** A dialog box to return two strings*/
public class UserNamePasswordDialog extends ExtensibleDialog {
	SingleLineTextField str1Result;
	MesquitePasswordField str2Result;
	TextArea str2ResultLong;
	MesquiteString str1;
	MesquiteString str2;
	MesquiteBoolean answer;



	public UserNamePasswordDialog (MesquiteWindow parent,  String title, String label1, String label2, MesquiteBoolean answer,  MesquiteString str1, MesquiteString str2) {
		super(parent,title);
		this.str2 = str2;
		this.str1 = str1;
		this.answer = answer;
		answer.setValue(false);


		addLabel(title, Label.CENTER, true, true);
		addBlankLine();
		str1Result = addTextField(label1, this.str1.getValue(),25);
		addBlankLine();

		str2Result = addPasswordField(label2,this.str2.getValue(), 20);

		str2Result.requestFocus();

		completeAndShowDialog(true,null);
		boolean ok = (query()==0);
		if (ok) {
			str1.setValue(str1Result.getText());
			str2.setValue(new String(str2Result.getPassword()));

			answer.setValue(true);
		}
		dispose();

	}

}



