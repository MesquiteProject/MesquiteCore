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

import java.awt.event.*;
import java.awt.*;
import mesquite.lib.duties.*;

/* ��������������������������� commands ������������������������������� */
/* includes commands,  buttons, miniscrolls

/*=================*/
public class MiniStringEditor extends Panel implements ActionListener, MiniControl{
	MesquiteModule ownerModule;
	EnterButton enterButton;
	protected TextField text;
	protected MesquiteCommand command;
	protected String origText ="";
	public MiniStringEditor (MesquiteModule ownerModule,  MesquiteCommand command) {
		setCursor(Cursor.getDefaultCursor());
		this.ownerModule=ownerModule;
		this.command = command;
		setSize(EnterButton.MIN_DIMENSION + 92,12 + MesquiteModule.textEdgeCompensationHeight);
		text = new TextField("");
		text.addActionListener(this);
		setLayout(null);
		add(text);
		text.setLocation(EnterButton.MIN_DIMENSION+1, 0);
		text.setSize(EnterButton.MIN_DIMENSION + 90, 12 + MesquiteModule.textEdgeCompensationHeight);
		add(enterButton = new EnterButton(this));
		enterButton.setVisible(false);
		enterButton.setLocation(0, (12 + MesquiteModule.textEdgeCompensationHeight)/2 - 8);
	}
 	public void acceptText(){
			String resultString = text.getText();
 			command.doItMainThread(resultString, CommandChecker.getQueryModeString("Mini text editor", command, this), this);  
	}
	public void prepare(){
		text.requestFocusInWindow();
		text.selectAll();
	}

	public void setVisible(boolean vis){
		if (!text.isVisible() && vis)
			prepare();
		text.setVisible(vis);
		enterButton.setVisible(vis);
		super.setVisible(vis);
	}
	public void setText(String s){
		if (s == null) //added 14 Feb 02
			s = "";
		origText = s;
		text.setText(origText);
	}
	public void actionPerformed(ActionEvent e){
		//Event queue
			acceptText();
	}
	

}

class EnterTextField extends TextField{
	
}


