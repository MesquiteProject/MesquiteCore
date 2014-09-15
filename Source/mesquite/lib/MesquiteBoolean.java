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

/*==========================  Mesquite Basic Class Library    ==========================*/
/*===  the basic classes used by the trunk of Mesquite and available to the modules
/* ���������������������������bits������������������������������� */

/* ======================================================================== */
/** This boolean wrapper class is used to be able to pass integers by reference and have the
	original change as needed.*/
public class MesquiteBoolean implements Listable {
	private boolean value;
	private MesquiteCMenuItemSpec cmis = null;
	public static MesquiteBoolean TRUE, FALSE;
	private String name = null;
	private boolean unassigned = true;
	static {
		TRUE = new MesquiteBoolean(true);
		FALSE = new MesquiteBoolean(false);
	}
	public MesquiteBoolean(String name, boolean value) {
		this.value=value;
		unassigned = false;
		this.name = name;
	}
	public MesquiteBoolean(boolean value) {
		this.value=value;
		unassigned = false;
	}
	public MesquiteBoolean() {
		value = false;
		unassigned = true;
	}
	
	public void setName(String s){
		name = s;
	}
	public String getName(){
		return name;
	}
	public boolean getValue() {
		return value;
	}
	public boolean isUnassigned() {
		return unassigned;
	}

	public void setToUnassigned() {
		unassigned=true;
		value = false;
	}

	public void setValue(boolean value) {
		unassigned = false;
		if (value != this.value) {
			this.value=value;
			if (cmis!=null) {
				MesquiteTrunk.resetCheckMenuItems();
			}
		}
	}
	public void setValue(String b) {
		unassigned = true;
		if (b == null){
			value = false;
			if (cmis!=null) 
				MesquiteTrunk.resetCheckMenuItems();
			return;
		}
		boolean v;
		if (b.equalsIgnoreCase("true"))
			v= true;
		else if (b.equalsIgnoreCase("false"))
			v = false;
		else {
			value = false;
			if (cmis!=null) 
				MesquiteTrunk.resetCheckMenuItems();
			return;
		}
		unassigned = false;
		if (v != this.value) {
			this.value=v;
			if (cmis!=null) 
				MesquiteTrunk.resetCheckMenuItems();
		}
	}
	
  	MesquiteInteger pos = new MesquiteInteger();
  	
	public void toggleValue(String arguments) {
	 	if (StringUtil.blank(arguments))
			toggleValue();
		else {
			 if ("on".equalsIgnoreCase(arguments))
				setValue(true);
			else if  ("off".equalsIgnoreCase(arguments))
				setValue(false);
			else {
				 String s = ParseUtil.getFirstToken(arguments, pos);
				 if ("on".equalsIgnoreCase(s))
					setValue(true);
				else if  ("off".equalsIgnoreCase(s))
					setValue(false);
				else
					toggleValue();
			}
		}
	}
	public void toggleValue() {
		unassigned = false;
		value=!value;
		if (cmis!=null) {
			MesquiteTrunk.resetCheckMenuItems();
		}
	}
	public void bindMenuItem(MesquiteCMenuItemSpec cmis) {
		this.cmis = cmis;
	}
	public void releaseMenuItem() {
		this.cmis = null;
	}
	public static String toOffOnString(boolean b) {
		if (b)
			return "on";
		else
			return "off";
	}
	
	public String toOffOnString() {
		if (unassigned)
			return "?";
		else if (value)
			return "on";
		else
			return "off";
	}
	public static boolean fromOffOnString(String b) {
		if (b == null || b.equalsIgnoreCase("off"))
			return false;
		else
			return true;
	}
	public static boolean fromTrueFalseString(String b) {
		if (b != null && b.equalsIgnoreCase("true"))
			return true;
		else
			return false;
	}
	public void setFromTrueFalseString(String b) {
		if (b != null && b.equalsIgnoreCase("true"))
			setValue(true);
		else
			setValue(false);
	}
	public static String toTrueFalseString(boolean b) {
		if (b)
			return "true";
		else
			return "false";
	}
	public String toString(){
		if (unassigned)
			return "?";
		else if (value)
			return "TRUE";
		else
			return "FALSE";
	}
	/*--------------------------------QUERY DIALOGS--------------------------*/
	/*.................................................................................................................*/
	/** queries user yes/no.*/
	public static boolean yesNoQuery(MesquiteWindow parent, String s) {
		return AlertDialog.query(parent,  "Query", s);
	}
	
	/*.................................................................................................................*/
	/** Presents a dialog with check box; returns true if OK hit.  New Dec 2001*/
	public static MesquiteBoolean queryCheckBox(MesquiteWindow parent, String title, String message, String label, String help, boolean current) {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog checkBoxDialog = new ExtensibleDialog(parent, title,buttonPressed);
		if (!StringUtil.blank(help))
			checkBoxDialog.appendToHelpString(help);
		checkBoxDialog.addLargeOrSmallTextLabel (message);
		Checkbox cb = checkBoxDialog.addCheckBox(label, current);
		
		checkBoxDialog.completeAndShowDialog(true);
		MesquiteBoolean value = new MesquiteBoolean();
		if (buttonPressed.getValue()==0) 
			value.setValue(cb.getState());
		checkBoxDialog.dispose();
		return value;
	}
	/*.................................................................................................................*/
	/** Presents a dialog with check box; returns true if OK hit.  New Dec 2001*/
	public static MesquiteBoolean queryCheckBox(MesquiteWindow parent, String title, String message, String label, boolean current) {
		return queryCheckBox(parent,title,message,label,null, current);
	}
	/*.................................................................................................................*/
	/** Presents a dialog with check box; returns true if OK hit.  THIS VERSION IS TO BE PHASED OUT*/
	public static boolean queryCheckBox(MesquiteWindow parent, String title, String message, String label, MesquiteBoolean value) {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog checkBoxDialog = new ExtensibleDialog(parent, title,buttonPressed);
		checkBoxDialog.addLargeOrSmallTextLabel (message);
		Checkbox cb = checkBoxDialog.addCheckBox(label, value.getValue());
		
		checkBoxDialog.completeAndShowDialog(true);
		
		if (buttonPressed.getValue()==0) 
			value.setValue(cb.getState());
		checkBoxDialog.dispose();
		return (buttonPressed.getValue()==0);
	}
}

