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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/*===============================================*/
/** A dialog box to allow the user to enter a username and password for authentication. */
public class AlertWithLinkToDirectory extends ExtensibleDialog implements ActionListener {
	
	Button directoryButton;
	String directoryPath;
	
	/*.................................................................................................................*/
	public void addDirectoryButton () {
		
		Panel buttons = addNewDialogPanel();
		addExtraButtons(buttons, this);
		directoryButton=addAListenedButton("Show Directory",buttons,this);
	}

	/*.................................................................................................................*/

	public AlertWithLinkToDirectory (MesquiteWindow parent,  String title, String message, String directoryPath) {
		super(parent,title);
		this.directoryPath = directoryPath;
		
		addTextArea(message, 5);
		
		//nullifyAddPanel();
		if (MesquiteFile.canShowDirectory())
			addDirectoryButton();

		completeAndShowDialog ("OK", null,true, this);

		boolean ok = (query()==0);
		if (ok) {
		}
		dispose();

	}
	/*.................................................................................................................*/
	public  void actionPerformed(ActionEvent e) {
		if  ("Show Directory".equals(e.getActionCommand())) {
			MesquiteFile.showDirectory(directoryPath);
		} else
			super.actionPerformed(e);
	}

}



