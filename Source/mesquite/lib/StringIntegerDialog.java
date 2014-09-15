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
import java.awt.event.*;

/*===============================================*/
/** A dialog box to return a string and an integer*/
public class StringIntegerDialog extends ExtensibleDialog  {
	SingleLineTextField stringField;
	IntegerField numberField;
	MesquiteString string;
	MesquiteInteger number;
	int min, max;
	String report = "";
	String numberTitle;
	boolean valuesChecked = false;
	
	public StringIntegerDialog (MesquiteWindow parent, String title, String stringTitle, String numberTitle, MesquiteString string, MesquiteInteger number, int min, int max, MesquiteInteger buttonPressed,String helpString, boolean showDialogInConstructor) {
		super(parent,title,buttonPressed);
		this.number = number;
		this.string = string;
		this.min = min;
		this.max = max;
		this.numberTitle = numberTitle;
		addLabel(stringTitle + ": ", Label.LEFT);
		stringField = addTextField(this.string.getValue());
		numberField = addIntegerField(numberTitle + ": ",this.number.getValue(),12);
		
		if (helpString!=null)
			appendToHelpString(helpString);
			
		setAutoDispose(false);

		if (showDialogInConstructor) {
			completeAndShowDialog(true,this);
				
			if (!valuesChecked && buttonPressed.getValue()==0)
				processOK();
		}
			
	}	
	public StringIntegerDialog (MesquiteWindow parent, String title, String stringTitle, String numberTitle, MesquiteString string, MesquiteInteger number, int min, int max, MesquiteInteger buttonPressed,String helpString) {
		this(parent,title,stringTitle,numberTitle,string,number,min, max,buttonPressed,helpString,true);
	}
	public StringIntegerDialog (MesquiteWindow parent, String title, String stringTitle, String numberTitle, MesquiteString string, MesquiteInteger number,MesquiteInteger buttonPressed,String helpString) {
		this(parent,title,stringTitle,numberTitle,string,number,MesquiteInteger.unassigned, MesquiteInteger.unassigned,buttonPressed,helpString,true);
	}
	/*.................................................................................................................*/
	 public  boolean checkNumber() {
	 	int num = getNumber();
	 	if (!numberField.isValidInteger()) {
	 		report = "Entry for "+numberTitle+ " is not a valid number";
	 		return false;
	 	}
	 	if (!MesquiteInteger.isCombinable(num))
	 		return false;
	 	if (MesquiteInteger.isCombinable(min))
	 		if (num<min) {
	 			report = "Entry for "+numberTitle+ " is below the minimum allowed";
	 			return false;
	 		}
	 	if (MesquiteInteger.isCombinable(max))
	 		if (num>max){
	 			report = "Entry for "+numberTitle+ " is above the maximum allowed";
	 			return false;
	 		}
	 	return true;
	 }
	/*.................................................................................................................*/
	 public  void setValues() {
 		number.setValue(getNumber());
 		string.setValue(getString());
	 }
	/*.................................................................................................................*/
	 public  void processOK() {
		if (checkNumber()) {
			valuesChecked = true;
	 		buttonPressed.setValue(0);
	 		setValues();
	 		dispose();
	 	}
	 	else
	 		MesquiteMessage.notifyUser(report);
	 }
	/*.................................................................................................................*/
	 public  String getString() {
	 	return stringField.getText();
	 }
	/*.................................................................................................................*/
	 public  int getNumber() {
	 	return numberField.getValue();
	 }
	/*.................................................................................................................*/
	public void keyReleased(KeyEvent e){
		if (e.getKeyCode() == 10) {
			processOK();
		}
	}
	/*.................................................................................................................*/
	 public  void actionPerformed(ActionEvent e) {
	 	if   (defaultOKLabel.equals(e.getActionCommand())) {
			processOK();
		}
		else if  (defaultCancelLabel.equals(e.getActionCommand())) {
			buttonPressed.setValue(1);
			dispose();
		}
		else
			super.actionPerformed(e);
	}
}



