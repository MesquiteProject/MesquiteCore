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
/** A dialog box to allow the user to enter a username and password for authentication. */
public class UserNamePasswordDialog extends ExtensibleDialog {
	SingleLineTextField userNameField;
	MesquitePasswordField passwordField;
	MesquiteString userName;
	MesquiteString passWord;
	MesquiteBoolean answer;



	public UserNamePasswordDialog (MesquiteWindow parent,  String title, String helpString, String helpURL, String hint, String label1, String label2, MesquiteBoolean answer,  MesquiteString str1, MesquiteString str2) {
		super(parent,title);
		this.passWord = str2;
		this.userName = str1;
		this.answer = answer;
		answer.setValue(false);

		if (StringUtil.notEmpty(helpString))
			appendToHelpString(helpString);

		if (StringUtil.notEmpty(helpURL))
			setHelpURL(helpURL);

		addLabel(title, Label.CENTER, true, true);
		addBlankLine();
		userNameField = addTextField(label1, userName.getValue(),26);
		addBlankLine();

		passwordField = addPasswordField(label2,passWord.getValue(), 20);

		addLabelSmallText(hint);

		passwordField.requestFocus();

		completeAndShowDialog(true,null);
		boolean ok = (query()==0);
		if (ok) {
			str1.setValue(userNameField.getText());
			str2.setValue(new String(passwordField.getPassword()));

			answer.setValue(true);
		}
		dispose();

	}

}



