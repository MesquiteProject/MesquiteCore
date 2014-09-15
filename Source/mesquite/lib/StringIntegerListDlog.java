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
/** A dialog box to return a string and an integer and an item selected from a list.*/
public class StringIntegerListDlog extends StringIntegerDialog   {
	DoubleClickList list;
	MesquiteString listString;
	String listTitle;
	boolean chooseFromList=true;
	String[] names;
	MesquiteInteger selected;
	boolean exactlyOneSelected = true;
	boolean acceptsDoubleClicks = false;
	
	public StringIntegerListDlog (MesquiteWindow parent, String title, String stringTitle, String numberTitle,String listTitle,String[] names, boolean exactlyOneSelected, MesquiteString string, MesquiteInteger number, MesquiteString listString, int min, int max, MesquiteInteger buttonPressed,String helpString) {
		super(parent,title,stringTitle,numberTitle,string, number,min,max,buttonPressed,helpString,false);
		this.names=names;
		if (names==null)
			chooseFromList = false;
		else if (names.length==0)
			chooseFromList = false;
		selected = new MesquiteInteger(0);
		this.listString = listString;
		this.listTitle = listTitle;
		this.exactlyOneSelected = exactlyOneSelected;
		completeAndShowDialog(true,this);
	}	
	/*.................................................................................................................*/
	public int getSelectedIndexFromString(String s) {
		for (int i=0; i<names.length; i++)
			if (names[i].equalsIgnoreCase(s))
				return i;
		return -1;
	}
	/*.................................................................................................................*/
	public void addAuxiliaryDefaultPanels(){
		if (chooseFromList) {
			addBlankLine();
			addLabel(listTitle + ": ",Label.LEFT);
			Panel mainPanel = addNewDialogPanel();
			int defaultItem =getSelectedIndexFromString(listString.getValue());
			if (exactlyOneSelected && defaultItem>=0 && defaultItem<=names.length)
				selected.setValue(defaultItem);
			list = createListenedList (names,selected, 6, this,null, false);
			list.setEnableDoubleClicks(acceptsDoubleClicks);  
			list.setForceSize(true);
			mainPanel.add(list);
		}
			
	}
	/*.................................................................................................................*/
	 public  String getListString() {
	 	if (chooseFromList) {
			int i = list.getSelectedIndex();
			if (i>=0 && i<names.length)
				return names[i];
	 	}
	 	return null;
	 }
	/*.................................................................................................................*/
	 public  void setValues() {
 		number.setValue(getNumber());
 		string.setValue(getString());
 		listString.setValue(getListString());
	 }
}



