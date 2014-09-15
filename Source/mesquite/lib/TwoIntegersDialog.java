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
/** A dialog box to return two integers*/
public class TwoIntegersDialog extends ExtensibleDialog {
	static int numIntegers=2;
	IntegerField[] numResult = new IntegerField[numIntegers];
	MesquiteInteger[] num = new MesquiteInteger[numIntegers];
	int[] min = new int[numIntegers];
	int[] max = new int[numIntegers];
	String[] label = new String[numIntegers];
	MesquiteBoolean answer;
	String report = "";
	boolean valuesChecked = false;
	
	//also take as input minimum and maximum allowed?
	public TwoIntegersDialog (MesquiteWindow parent,  String title, String label1, String label2, MesquiteBoolean answer,  MesquiteInteger num1, MesquiteInteger num2,int min1,int max1,int min2, int max2, String helpString) {
		super(parent,title);
		for (int i=0; i<numIntegers;i++) {
			min[i]=MesquiteInteger.unassigned;
			max[i]=MesquiteInteger.unassigned;
		}
		this.min[0] = min1;
		this.min[1] = min2;
		this.max[0] = max1;
		this.max[1] = max2;
		this.num[0] = num1;
		this.num[1] = num2;
		this.label[0]=label1;
		this.label[1]=label2;
		
		this.answer = answer;
		answer.setValue(false);
		
		for (int i=0; i<numIntegers;i++) {
			numResult[i] = addIntegerField(this.label[i] + ": ",this.num[i].getValue(),12);
		}

		if (!StringUtil.blank(helpString))
			appendToHelpString(helpString);
			
		setAutoDispose(false);

		completeAndShowDialog(true,this);
		
		if (!valuesChecked && answer.getValue())
			processOK();
	}
	public TwoIntegersDialog (MesquiteWindow parent,  String title, String label1, String label2, MesquiteBoolean answer,  MesquiteInteger num1, MesquiteInteger num2,String helpString) {
		this( parent,  title,  label1,  label2,  answer,   num1,  num2, MesquiteInteger.unassigned,MesquiteInteger.unassigned,MesquiteInteger.unassigned,MesquiteInteger.unassigned,helpString);
	}
	/*.................................................................................................................*/
	 public  boolean checkNumber(int field) {
	 	int number = getNumber(field);
	 	if (!numResult[field].isValidInteger()) {
	 		report = "Entry for "+label[field]+ " is not a valid number";
	 		return false;
	 	}
	 	if (!MesquiteInteger.isCombinable(number))
	 		return false;
	 	if (MesquiteInteger.isCombinable(min[field]))
	 		if (number<min[field]) {
	 			report = "Entry for "+label[field]+ " is below the minimum allowed";
	 			return false;
	 		}
	 	if (MesquiteInteger.isCombinable(max[field]))
	 		if (number>max[field]){
	 			report = "Entry for "+label[field]+ " is above the maximum allowed";
	 			return false;
	 		}
	 	return true;
	 }
	/*.................................................................................................................*/
	 public  void processOK() {
		if (checkNumber(0) && checkNumber(1)) {
			valuesChecked = true;
	 		answer.setValue(true);
	 		for (int i=0;i<numIntegers;i++)
	 			num[i].setValue(getNumber(i));
	 		dispose();
	 	}
	 	else
	 		MesquiteMessage.notifyUser(report);
	 }
	/*.................................................................................................................*/
	 public  int getNumber(int field) {
	 	return numResult[field].getValue();
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
			answer.setValue(false);
			dispose();
		}
		else
			super.actionPerformed(e);
	}
}



