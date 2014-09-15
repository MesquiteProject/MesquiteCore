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
import mesquite.lib.duties.*;

/* ��������������������������� commands ������������������������������� */
/* includes commands,  buttons, miniscrolls
/*=================*/
public class MiniNumberEditor extends MiniStringEditor{
	double origDouble = MesquiteDouble.impossible;
	public MiniNumberEditor (MesquiteModule ownerModule,  MesquiteCommand command) {
		super(ownerModule, command);
	}
 	public void acceptText(){
			String resultString= null;
			String newText =text.getText();
			if (!origText.equals(newText)) {
				double newS =MesquiteDouble.fromString(newText);
				if (newS!=origDouble)
					resultString = MesquiteDouble.toStringDigitsSpecified(newS, -1); //changed 17 Dec for full accuracy
 			}
 			command.doItMainThread(resultString, CommandChecker.getQueryModeString("Mini number editor", command, this), this);  
 	}

	public void setNumber(double s){
		origText = MesquiteDouble.toStringDigitsSpecified(s, -1);//changed 17 Dec for full accuracy
		origDouble =s;
		text.setText(origText);
	}
}


