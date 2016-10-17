/* Mesquite source code.  Copyright 2001 and onward, D. Maddison and W. Maddison. 


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

/** A utility class with static methods that present the user with a dialog box in which they can type a number.*/
public class QueryDialogs  {
	
	/*.................................................................................................................*/
	public static boolean queryInteger(MesquiteWindow parent, String title, String message,  boolean allowCancel, MesquiteInteger value) {
		return queryInteger(parent, title, message, "", allowCancel, value);
	}
	/*.................................................................................................................*/
	public static boolean queryInteger(MesquiteWindow parent, String title, String message, String help, boolean allowCancel, MesquiteInteger value) {
		return queryInteger(parent, title, message, null, help, allowCancel, value);
	}
	/*.................................................................................................................*/
	public static boolean queryInteger(MesquiteWindow parent, String title, String message, String secondaryMessage, String help, boolean allowCancel, MesquiteInteger value) {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog queryDialog = new ExtensibleDialog(parent, title,buttonPressed);
		queryDialog.addLargeOrSmallTextLabel(message);
		if (StringUtil.blank(help) && queryDialog.isInWizard())
			help = "<h3>" + StringUtil.protectForXML(title) + "</h3>Please enter a whole number (integer).  <p>The initial value is " + value;
		queryDialog.appendToHelpString(help);
		IntegerField integerField = queryDialog.addIntegerField("", value.getValue(), 20);
		
		queryDialog.setDefaultTextComponent(integerField.getTextField());
		queryDialog.setDefaultComponent(integerField.getTextField());
		if (StringUtil.notEmpty(secondaryMessage))
			queryDialog.addLabelSmallText(secondaryMessage);
		
		if (allowCancel)
			queryDialog.completeAndShowDialog(true);
		else
			queryDialog.completeAndShowDialog("OK", null, null, "OK");
		
		value.setValue(integerField.getValue());
		if (buttonPressed.getValue()!=0) 
			value.setValue(MesquiteInteger.unassigned);
		queryDialog.dispose();
		return (buttonPressed.getValue()==0);
	}
	/*.................................................................................................................*/
	public static boolean queryLong(MesquiteWindow parent, String title, String message, MesquiteLong value) {
		return queryLong(parent, title, message, "", value);
	}
	/*.................................................................................................................*/
	public static boolean queryLong(MesquiteWindow parent, String title, String message, String help, MesquiteLong value) {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog queryDialog = new ExtensibleDialog(parent, title,buttonPressed);
		queryDialog.addLargeOrSmallTextLabel(message);
		if (StringUtil.blank(help) && queryDialog.isInWizard())
			help = "<h3>" + StringUtil.protectForXML(title) + "</h3>Please enter a whole number (integer).  <p>The initial value is " + value;
		queryDialog.appendToHelpString(help);
		LongField longField = queryDialog.addLongField("", value.getValue(), 20);
		queryDialog.setDefaultTextComponent(longField.getTextField());
		queryDialog.completeAndShowDialog(true);
		value.setValue(longField.getValue());
		queryDialog.dispose();
		if (buttonPressed.getValue()!=0) 
			value.setValue(MesquiteLong.unassigned);
		return (buttonPressed.getValue()==0);
	}
	/*.................................................................................................................*/
	public static boolean queryDouble(MesquiteWindow parent, String title, String message, MesquiteDouble value) {
		return queryDouble(parent, title, message, "", value);
	}
	/*.................................................................................................................*/
	public static boolean queryDouble(MesquiteWindow parent, String title, String message, String help, MesquiteDouble value) {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog queryDialog = new ExtensibleDialog(parent, title,buttonPressed);
		queryDialog.addLargeOrSmallTextLabel(message);
		if (StringUtil.blank(help) && queryDialog.isInWizard())
			help = "<h3>" + StringUtil.protectForXML(title) + "</h3>Please enter a number.  It does not need to be a whole number.  To indicate exponentials, use E.  Thus, 2x10^6 is written 2e6.  <p>The initial value is " + value;
		queryDialog.appendToHelpString(help);
		SingleLineTextField textField = queryDialog.addTextField("", value.toString(), 30);
		queryDialog.setDefaultTextComponent(textField);
		queryDialog.completeAndShowDialog(true);
		value.setValue(MesquiteDouble.fromString(textField.getText()));
		queryDialog.dispose();
		if (buttonPressed.getValue()!=0) 
			value.setValue(MesquiteDouble.unassigned);
		return (buttonPressed.getValue()==0);
	}
	/*.................................................................................................................*/
	public static boolean queryTwoDoubles(MesquiteWindow parent, String title, String message1, MesquiteDouble value1, String message2, MesquiteDouble value2) {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog queryDialog = new ExtensibleDialog(parent, title,buttonPressed);
		if (StringUtil.blank(queryDialog.getHelpString()) && queryDialog.isInWizard())
			queryDialog.appendToHelpString("<h3>" + StringUtil.protectForXML(title) + "</h3>Please enter two numbers.  To indicate exponentials, use E.  Thus, 2x10^6 is written 2e6.  <p>The initial values are " + value1 + " and " + value2);
		
		queryDialog.addLargeOrSmallTextLabel(message1);
		SingleLineTextField textField1 = queryDialog.addTextField("", value1.toString(), 30);
		queryDialog.addLargeOrSmallTextLabel(message2);
		SingleLineTextField textField2 = queryDialog.addTextField("", value2.toString(), 30);
		queryDialog.completeAndShowDialog(true);
		value1.setValue(MesquiteDouble.fromString(textField1.getText()));
		value2.setValue(MesquiteDouble.fromString(textField2.getText()));
		
		queryDialog.dispose();
		if (buttonPressed.getValue()!=0) {
			value1.setValue(MesquiteDouble.unassigned);
			value2.setValue(MesquiteDouble.unassigned);
		}
		return (buttonPressed.getValue()==0);
	}
	/*.................................................................................................................*/
	public static boolean queryMesquiteNumber(MesquiteWindow parent, String title, String message, MesquiteNumber value) {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog queryDialog = new ExtensibleDialog(parent, title,buttonPressed);
		if (StringUtil.blank(queryDialog.getHelpString()) && queryDialog.isInWizard())
			queryDialog.appendToHelpString("<h3>" + StringUtil.protectForXML(title) + "</h3>Please enter a number.  <p>The initial value is " + value);
		queryDialog.addLargeOrSmallTextLabel(message);
		SingleLineTextField textField = queryDialog.addTextField("", value.toString(), 30);
		queryDialog.setDefaultTextComponent(textField);
		queryDialog.completeAndShowDialog(true);
		String s = textField.getText();
		if (s!=null && s.length()>0 && s.charAt(s.length()-1) == (char)10){
			s = s.substring(0,s.length()-1); 
		}
		value.setValue(s);
		queryDialog.dispose();
		if (buttonPressed.getValue()!=0) 
			value.setToUnassigned();
		return (buttonPressed.getValue()==0);
	}
	/*.................................................................................................................*/
	public static boolean queryShortString(MesquiteWindow parent, String title, String message, MesquiteString value, boolean hasDefault) {
		if (value==null)
			return false;
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog queryDialog = new ExtensibleDialog(parent, title,buttonPressed);
		if (StringUtil.blank(queryDialog.getHelpString()) && queryDialog.isInWizard())
			queryDialog.appendToHelpString("<h3>" + StringUtil.protectForXML(title) + "</h3>Please enter a short string of text.");
		if (!hasDefault)
			queryDialog.setDefaultButton(null);
		queryDialog.addLargeOrSmallTextLabel(message);
		String start = "";
		if (StringUtil.notEmpty(value.toString()))
				start = value.toString();
		SingleLineTextField textField = queryDialog.addTextField("", value.toString(), 30);
		queryDialog.setDefaultTextComponent(textField);
		if (hasDefault)
			queryDialog.completeAndShowDialog(true);
		else
			queryDialog.completeAndShowDialog();
		String s = textField.getText();
		if (s!=null && s.length()>0 && s.charAt(s.length()-1) == (char)10){
			s = s.substring(0,s.length()-1); 
		}
		value.setValue(s);
		queryDialog.dispose();
		if (buttonPressed.getValue()!=0) 
			value.setValue((String)null);
		return (buttonPressed.getValue()==0);
	}
	/*.................................................................................................................*/
	public static boolean queryShortString(MesquiteWindow parent, String title, String message, MesquiteString value) {
		return queryShortString(parent, title, message, value, true);
	}
	/*.................................................................................................................*/
	public static boolean queryMultiLineString(MesquiteWindow parent, String title, String message, MesquiteString value, int numRows, boolean hasDefault, boolean useSmallText) {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog queryDialog = new ExtensibleDialog(parent, title,buttonPressed);
		if (!hasDefault)
			queryDialog.setDefaultButton(null);
		if (StringUtil.blank(queryDialog.getHelpString()) && queryDialog.isInWizard())
			queryDialog.appendToHelpString("<h3>" + StringUtil.protectForXML(title) + "</h3>Please enter text.");
		queryDialog.addLargeOrSmallTextLabel(message);
		TextArea textArea = queryDialog.addTextArea(value.getValue(),numRows);
		queryDialog.setDefaultTextComponent((TextComponent)textArea);
		if (useSmallText){
			textArea.setFont(new Font ("SanSerif", Font.PLAIN, 10));
		}
		if (hasDefault)
			queryDialog.completeAndShowDialog(true);
		else
			queryDialog.completeAndShowDialog();
		String resultText = null;
		int count = 0;
		while (count++<10){
			try {
				resultText = textArea.getText();
				count = 10;
		        }
		        catch (Exception e){
		        }
	        }
		value.setValue(resultText);
		queryDialog.dispose();
		if (buttonPressed.getValue()!=0) 
			value.setValue((String)null);
		return (buttonPressed.getValue()==0);
	}
	/*.................................................................................................................*/
	public static boolean queryString(MesquiteWindow parent, String title, String message, String help, MesquiteString value, int numRows, boolean hasDefault, boolean useSmallText) {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog queryDialog = new ExtensibleDialog(parent, title,buttonPressed);
		if (!hasDefault)
			queryDialog.setDefaultButton(null);
		if (StringUtil.blank(queryDialog.getHelpString()) && queryDialog.isInWizard())
			queryDialog.appendToHelpString("<h3>" + StringUtil.protectForXML(title) + "</h3>Please enter a string of text. ");
		if (StringUtil.notEmpty(help))
			queryDialog.appendToHelpString(help);

		queryDialog.addLargeOrSmallTextLabel(message);
		SingleLineTextArea textArea = queryDialog.addSingleLineTextArea(value.getValue(),numRows);
		textArea.addKeyListener(queryDialog);
		queryDialog.setDefaultTextComponent((TextComponent)textArea);
		if (useSmallText){
			textArea.setFont(new Font ("SanSerif", Font.PLAIN, 10));
		}
		if (hasDefault)
			queryDialog.completeAndShowDialog(true);
		else
			queryDialog.completeAndShowDialog();
		value.setValue(textArea.getText());
		queryDialog.dispose();
		if (buttonPressed.getValue()!=0) 
			value.setValue((String)null);
		return (buttonPressed.getValue()==0);
	}
	/*.................................................................................................................*/
	public static boolean queryString(MesquiteWindow parent, String title, String message, MesquiteString value, int numRows, boolean hasDefault, boolean useSmallText) {
		return queryString( parent,  title,  message,  "", value, numRows,  hasDefault,  useSmallText);
	}
	/*.................................................................................................................*/
	public static boolean queryString(MesquiteWindow parent, String title, String message, MesquiteString value, int numRows) {
		return queryString(parent, title, message, value,numRows, true, false);
	}
	/*.................................................................................................................*/
	public static boolean queryString(MesquiteWindow parent, String title, String message, MesquiteString value) {
		return queryString(parent, title, message, value,3, true, false);
	}
	
	/*.................................................................................................................*/
	public static int queryTwoRadioButtons(MesquiteWindow parent, String title, String message, String help, String button1, String button2) {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog queryDialog = new ExtensibleDialog(parent, title,buttonPressed);
		queryDialog.addLargeOrSmallTextLabel(message);
		queryDialog.appendToHelpString(help);

		RadioButtons radios = new RadioButtons(queryDialog, new String[] {button1, button2}, 0);

		queryDialog.completeAndShowDialog(true);

		int returnValue=-1;

		if (buttonPressed.getValue()==0) 
			returnValue = radios.getValue();
		queryDialog.dispose();
		return returnValue;
	}

	/*.................................................................................................................*/
	public static boolean queryChar(MesquiteWindow parent, String title, String message, MesquiteString value) {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog queryDialog = new ExtensibleDialog(parent, title,buttonPressed);
			queryDialog.setDefaultButton(null);
		if (StringUtil.blank(queryDialog.getHelpString()) && queryDialog.isInWizard())
			queryDialog.appendToHelpString("<h3>" + StringUtil.protectForXML(title) + "</h3>Please enter a single character.");
		queryDialog.addLargeOrSmallTextLabel(message);
		SingleLineTextField symbolField = queryDialog.addTextField(value.getValue(),5);
		symbolField.addKeyListener(queryDialog);
		queryDialog.setDefaultTextComponent((TextComponent)symbolField);
			queryDialog.completeAndShowDialog(true);
		String s = symbolField.getText();
		boolean success = true;
		if (s.length()>1)
			s = s.substring(1,1);
		else if (s.length()<=0)
			success = false;
		value.setValue(s);

		queryDialog.dispose();
		if (buttonPressed.getValue()!=0) 
			value.setValue((String)null);
		return (buttonPressed.getValue()==0 && success);
	}

}



