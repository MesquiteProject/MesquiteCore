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

import java.awt.event.*;
import java.awt.*;
/*===============================================*/



public class AlertDialog  {
	static String okString = "OK";
	static String cancelString = "Cancel";
	static String noString = "No";


	public static void setButtonNames (String string1, String string2, String string3) {
		if (string1!=null)
			okString = string1;
		if (string2!=null)
			cancelString = string2;
		if (string3 !=null)
			noString = string3;
	}

	public static int query(Object parent, String title, String message, String string1, String string2, String string3, String helpString) {
		return query(parent, title, message, string1, string2, string3, 1, helpString);
	}
	public static int query(Object parent, String title, String message, String string1, String string2, String string3) {
		return query(parent, title, message, string1, string2, string3, 1, "");
	}
	public static int query(Object parent, String title, String message, String string1, String string2, String string3, int whichIsDefault) {
		return query(parent, title, message, string1, string2, string3, whichIsDefault, "");
	}
	public static int query(Object parent, String title, String message, String string1, String string2, String string3, int whichIsDefault, String helpString) {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog id = new ExtensibleDialog(parent, title,buttonPressed);
		id.addLargeTextLabel(message);
		if (StringUtil.blank(helpString) && id.isInWizard())
			helpString = "<h3>" + StringUtil.protectForXML(title) + "</h3>Please choose.";
		id.appendToHelpString(helpString);
		String defaultButton="";
		if (whichIsDefault == 1)
			defaultButton=string1;
		else if (whichIsDefault == 2)
			defaultButton=string2;
		else if (whichIsDefault == 3)
			defaultButton=string3;
		id.completeAndShowDialog(string1,string2,string3,defaultButton);
		id.dispose();
		return buttonPressed.getValue();
	}
	public static boolean query(Object parent, String title, String message, String okString, String cancelString, int whichIsDefault, String helpString) {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog id = new ExtensibleDialog(parent, title,buttonPressed);
		if (id.isInWizard())
			id.appendToHelpString("<h3>" + StringUtil.protectForXML(title) + "</h3>");
		if (!StringUtil.blank(helpString))
			id.appendToHelpString(helpString);
		if (id.isInWizard())
			id.appendToHelpString("Please choose.");
		id.addLargeTextLabel(message);
		String defaultButton="";
		if (whichIsDefault == 1)
			defaultButton=okString;
		else if (whichIsDefault == 2)
			defaultButton=cancelString;
		id.completeAndShowDialog(okString,cancelString,null,defaultButton);
		id.dispose();
		return (buttonPressed.getValue()==0);
	}
	public static int queryLongMessage(Object parent, String title, String message, String extraMessage, String string1, String string2, String string3, int whichIsDefault, String helpString) {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog id = new ExtensibleDialog(parent, title,buttonPressed);
		id.addLargeTextLabel(message);
		if (StringUtil.blank(helpString) && id.isInWizard())
			helpString = "<h3>" + StringUtil.protectForXML(title) + "</h3>Please choose.";
		if (StringUtil.notEmpty(extraMessage))
			id.addTextAreaSmallFont(extraMessage, 6);
		id.appendToHelpString(helpString);
		String defaultButton="";
		if (whichIsDefault == 1)
			defaultButton=string1;
		else if (whichIsDefault == 2)
			defaultButton=string2;
		else if (whichIsDefault == 3)
			defaultButton=string3;
		id.completeAndShowDialog(string1,string2,string3,defaultButton);
		id.dispose();
		return buttonPressed.getValue();
	}

	public static boolean query(Object parent, String title, String message, String okString, String cancelString, int whichIsDefault) {
		return query(parent, title, message, okString, cancelString, whichIsDefault, null);
	}
	public static boolean query(Object parent, String title, String message, String okString, String cancelString) {
		return query(parent, title, message, okString, cancelString, 1);
	}
	public static boolean query(Object parent, String title, String message) {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog id = new ExtensibleDialog(parent, title,buttonPressed);
		if (id.isInWizard())
			id.appendToHelpString("<h3>" + StringUtil.protectForXML(title) + "</h3>Please choose.");
		id.addLargeTextLabel(message);
		id.completeAndShowDialog();
		id.dispose();
		return (buttonPressed.getValue()==0);
	}
	public static void noticeHTML(Object parent, String title, String message, int w, int h, MesquiteCommand linkTouchedCommand) {
		noticeHTML(parent, title, message, w, h, linkTouchedCommand, false);
	}
	public static void noticeHTML(Object parent, String title, String message, int w, int h, MesquiteCommand linkTouchedCommand, boolean checkTextEdge) {

		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog id = new ExtensibleDialog(parent, title,buttonPressed, true);
		id.setCheckTextEdge(checkTextEdge);
		if (id.isInWizard())
			id.appendToHelpString("<h3>Notice</h3>");
		id.addHTMLPanel(message, w, h, linkTouchedCommand);
		id.completeAndShowDialog(okString,null,null,okString);
		id.dispose();
	}
	public static void notice(Object parent, String title, String message) {
		if (message!=null && message.length()>800){
			verboseNotice(parent, title, message);
			return;
		}
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog id = new ExtensibleDialog(parent, title,buttonPressed, true);
		if (id.isInWizard())
			id.appendToHelpString("<h3>Notice</h3>");
		id.addLargeTextLabel(message);
		id.completeAndShowDialog(okString,null,null,okString);
		id.dispose();
	}
	public static void bigNotice(Object parent, String title, String message) {
		if (message!=null && message.length()>800){
			verboseNotice(parent, title, message);
			return;
		}
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog id = new ExtensibleDialog(parent, title,buttonPressed, true);
		if (id.isInWizard())
			id.appendToHelpString("<h3>Notice</h3>");
		id.addLargeTextLabel(message);
		id.completeAndShowDialog(okString,null,null,okString);
		id.dispose();
	}
	public static void verboseNotice(Object parent, String title, String message) {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog id = new ExtensibleDialog(parent, title,buttonPressed, true);
		if (id.isInWizard())
			id.appendToHelpString("<h3>Notice</h3>");
		id.addTextAreaSmallFont(message, 20, 76);
		id.completeAndShowDialog(okString,null,null,okString);
		id.dispose();
	}
}





