/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 



Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/

package mesquite.externalCommunication.lib;

import java.awt.Button;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;

import mesquite.lib.*;

public class AppChooser implements ActionListener {
	String alternativeManualPath;
	String nameOfApp;
	boolean useBuiltInIfAvailable;
	boolean builtInAvailable = false;
	String versionOfBuiltIn, pathOfBuiltIn;
	RadioButtons builtInVsManual;
	Button appButton, browseButton;
	JLabel usingLabelMainDlog;
	SingleLineTextField alternativePathField;
	ExtensibleDialog containingDialog;
	public AppChooser(String nameOfApp, boolean useBuiltInIfAvailable, String alternativeManualPath) {
		this.nameOfApp = nameOfApp;
		this.useBuiltInIfAvailable = useBuiltInIfAvailable;
		this.alternativeManualPath = alternativeManualPath;
		
		//Not finished; these should depend on whether there is a built-in copy
		//Here get info from the AppHarvester
		builtInAvailable = false; //here should look to see!
		versionOfBuiltIn = "1.0";
		pathOfBuiltIn = "/PATH";
	}

	/*.................................................................................................................*/
	// adding to the module's queryOptions dialog box
	public void addToDialog(ExtensibleDialog dialog) {
		containingDialog = dialog;
		usingLabelMainDlog = dialog.addLabel(getMainDialogUsingString());
		appButton = dialog.addAListenedButton("App...", null, this);
		appButton.setActionCommand("chooseApp");
	}
	/*.................................................................................................................*/
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equalsIgnoreCase("chooseApp")) {

			//Show app chooser dialog ========================
			MesquiteInteger buttonPressed = new MesquiteInteger(1);
			ExtensibleDialog dialog = new ExtensibleDialog(containingDialog,  "Choose " + nameOfApp,buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
			dialog.addBlankLine();
			String warningUseWorking = "";
			if (builtInAvailable) {
				String builtInString = "Use built-in " + nameOfApp + " (version " + versionOfBuiltIn + ")"; 
				int defaultValue = 1;
				if (useBuiltInIfAvailable)
					defaultValue = 0;
				builtInVsManual = dialog.addRadioButtons(new String[] {builtInString, "Use alternative installed copy indicated below"}, defaultValue);
				dialog.addHorizontalLine(1);
				dialog.addLabel("Path to alternative installed copy of " + nameOfApp + ":");
				warningUseWorking = "If you use the alternative installed copy of " + nameOfApp + ", p";
			}
			else {
				dialog.addLabel("Copy of " + nameOfApp + " installed on your computer to be used:");
				warningUseWorking = "There is no copy of " + nameOfApp + " built into your version of Mesquite. P";
			}
			alternativePathField = dialog.addTextField(alternativeManualPath, 40);
			
			Button programBrowseButton = dialog.addAListenedButton("Browse...",null, this);
			programBrowseButton.setActionCommand("programBrowse");
			dialog.addLargeOrSmallTextLabel(warningUseWorking + "lease make sure the indicated copy runs on its own from the command line/command prompt before attempting to run it from Mesquite.");
			dialog.completeAndShowDialog(true);
			if (buttonPressed.getValue()==0)  {
				if (builtInVsManual != null)
					useBuiltInIfAvailable = (builtInVsManual.getValue() == 0);
				alternativeManualPath = alternativePathField.getText();
				usingLabelMainDlog.setText(getMainDialogUsingString());
				//Remember in receiving module to receive the various parts
				// set pathOfBuiltIn etc.?
			}
			dialog.dispose();
			//========================

		} 
		//Browse for the installed copy ========================
		else 	if (e.getActionCommand().equalsIgnoreCase("programBrowse")) {
			alternativeManualPath = MesquiteFile.openFileDialog("Choose " + nameOfApp + ":", null, null);
			if (!StringUtil.blank(alternativeManualPath)) {
				alternativePathField.setText(alternativeManualPath);
				usingLabelMainDlog.setText(getMainDialogUsingString());
			}
		}
	}
	/*.................................................................................................................*/
	String getMainDialogUsingString() {
		String usingString = "Using";
		Debugg.println(" builtInAvailable " +builtInAvailable + " builtInVsManual" + builtInVsManual);
		if (builtInVsManual != null)
			Debugg.println( "    builtInVsManual=" +builtInVsManual.getValue());
		if (usingBuiltIn())
			usingString += " built-in " + nameOfApp + " (version " + versionOfBuiltIn + ")";
		else
			usingString += " " + nameOfApp + " at " + alternativeManualPath;
		return usingString;
	}
	/*.................................................................................................................*/
	boolean usingBuiltIn() {
		if (builtInAvailable) {
			if (builtInVsManual != null)
				return (builtInVsManual.getValue()==0);
			return useBuiltInIfAvailable;
		}
		return false;
	}
	/*.................................................................................................................*/
	public String getPathToUse() {
		if (useBuiltInIfAvailable && builtInAvailable)
			return pathOfBuiltIn;
		else
			return alternativeManualPath;
	}

	public String getManualPath() { //for preference writing
		return alternativeManualPath;
	}

	public boolean useBuiltInIfAvailable() { //for preference writing
		return useBuiltInIfAvailable;
	}

	public String getVersion() {
		return versionOfBuiltIn; //return string only for Built In
	}
}

