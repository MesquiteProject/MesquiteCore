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

import mesquite.externalCommunication.AppHarvester.AppHarvester;
import mesquite.lib.*;

public class AppChooser implements ActionListener {
	String alternativeManualPath;   // if user-specified, not built in
	String builtInAppPath;
	String nameOfApp;  //
	String programName;
	boolean useDefaultExecutablePath;
	boolean builtInExecutableAllowed = false;  // whether or not the built in executable is allowed to be used
					//- the system has already harvested app info and recorded whether or not a built-in executable exists.
	
	String versionOfBuiltIn;
	RadioButtons builtInVsManual;
	Button appButton, browseButton;
	JLabel usingLabelMainDlog;
	SingleLineTextField alternativePathField;
	ExtensibleDialog containingDialog;
	AppUser appUser;
	
	
	
	public AppChooser(AppUser appUser, boolean useDefaultExecutablePath, String alternativeManualPath) {
	//	this.useBuiltInIfAvailable = useBuiltInIfAvailable;
		this.appUser = appUser;
		if (appUser!=null) {
			nameOfApp = appUser.getAppOfficialName();
			programName = appUser.getProgramName();
			AppInformationFile appInfoFile = AppHarvester.getAppInfoFileForProgram(appUser);
			if (appInfoFile!=null) {
				builtInExecutableAllowed = true;
				builtInAppPath = appInfoFile.getFullPath();
				versionOfBuiltIn = appInfoFile.getVersion();
			}
		}

		this.alternativeManualPath = alternativeManualPath;
		
		this.useDefaultExecutablePath = useDefaultExecutablePath;
		
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
			ExtensibleDialog dialog = new ExtensibleDialog(containingDialog,  "Choose " + programName,buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
			dialog.addBlankLine();
			String warningUseWorking = "";
			if (builtInExecutableAllowed) {
				String builtInString = "Use built-in " + programName + " (version " + versionOfBuiltIn + ")"; 
				int defaultValue = 1;
				if (useDefaultExecutablePath)
					defaultValue = 0;
				builtInVsManual = dialog.addRadioButtons(new String[] {builtInString, "Use alternative installed copy indicated below"}, defaultValue);
				dialog.addHorizontalLine(1);
				dialog.addLabel("Path to alternative installed copy of " + programName + ":");
				warningUseWorking = "If you use the alternative installed copy of " + programName + ", p";
			}
			else {
				dialog.addLabel("Copy of " + programName + " installed on your computer to be used:");
				warningUseWorking = "There is no copy of " + programName + " built into your version of Mesquite. P";
			}
			alternativePathField = dialog.addTextField(alternativeManualPath, 40);
			
			Button programBrowseButton = dialog.addAListenedButton("Browse...",null, this);
			programBrowseButton.setActionCommand("programBrowse");
			dialog.addLargeOrSmallTextLabel(warningUseWorking + "lease make sure the indicated copy runs on its own from the command line/command prompt before attempting to run it from Mesquite.");
			dialog.completeAndShowDialog(true);
			if (buttonPressed.getValue()==0)  {
				if (builtInVsManual != null)
					useDefaultExecutablePath = (builtInVsManual.getValue() == 0);
				String tempPath = alternativePathField.getText();
				if (StringUtil.blank(tempPath) && !useDefaultExecutablePath){
					MesquiteMessage.discreetNotifyUser("If you do not use a built-in app, then the path to " +programName+ " must be entered.");
				} else
					alternativeManualPath = tempPath;
				usingLabelMainDlog.setText(getMainDialogUsingString());
				//Remember in receiving module to receive the various parts
				// set pathOfBuiltIn etc.?
			}
			dialog.dispose();
			//========================

		} 
		//Browse for the installed copy ========================
		else 	if (e.getActionCommand().equalsIgnoreCase("programBrowse")) {
			alternativeManualPath = MesquiteFile.openFileDialog("Choose " + programName + ":", null, null);
			if (!StringUtil.blank(alternativeManualPath)) {
				alternativePathField.setText(alternativeManualPath);
				usingLabelMainDlog.setText(getMainDialogUsingString());
			}
		}
	}
	/*.................................................................................................................*/
	String getMainDialogUsingString() {
		String usingString = "Using";
		Debugg.println(" builtInAvailable " +builtInExecutableAllowed + " builtInVsManual" + builtInVsManual);
		if (builtInVsManual != null)
			Debugg.println( "    builtInVsManual=" +builtInVsManual.getValue());
		if (usingBuiltIn())
			usingString += " built-in " + programName + " (version " + versionOfBuiltIn + ")";
		else
			usingString += " " + programName + " at " + alternativeManualPath;
		return usingString;
	}
	/*.................................................................................................................*/
	boolean usingBuiltIn() {
		if (builtInExecutableAllowed) {
			if (builtInVsManual != null)
				return (builtInVsManual.getValue()==0);
			return useDefaultExecutablePath;
		}
		return false;
	}

	public String getPathToUse() { 
		if (useDefaultExecutablePath)
			return builtInAppPath;
		else
			return alternativeManualPath;
	}

	public String getManualPath() { //for preference writing
		return alternativeManualPath;
	}

	public boolean useBuiltInExecutable() { //for preference writing
		return useDefaultExecutablePath;
	}

	public String getVersion() {
		return versionOfBuiltIn; //return string only for Built In
	}
}

