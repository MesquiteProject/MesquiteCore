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
import java.awt.ItemSelectable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;

import javax.swing.JLabel;

import mesquite.externalCommunication.AppHarvester.AppHarvester;
import mesquite.lib.*;

public class AppChooser implements ActionListener {
	MesquiteString alternativeManualPath = new MesquiteString();   // if user-specified, not built in
	String builtInAppPath;
	String officialAppNameInAppInfo;  //
	String programName;
	MesquiteBoolean useDefaultExecutablePath = new MesquiteBoolean(false);
	boolean builtInExecutableAllowed = false;  // whether or not the built in executable is allowed to be used
					//- the system has already harvested app info and recorded whether or not a built-in executable exists.
	
	String versionOfBuiltIn;
	//RadioButtons builtInVsManual;
	Button appButton, browseButton;
	JLabel usingLabelMainDlog;
	SingleLineTextField alternativePathField;
	ExtensibleDialog containingDialog;
	AppUser appUser;
	MesquiteModule ownerModule;
	

	
	public AppChooser(String officialAppNameInAppInfo, String programNameForDisplay, boolean useDefaultExecutablePath, String alternativeManualPath) {
		this.officialAppNameInAppInfo = officialAppNameInAppInfo;
		programName = programNameForDisplay;
		AppInformationFile appInfoFile = AppHarvester.getAppInfoFileForProgram(officialAppNameInAppInfo);
		if (appInfoFile!=null) {
			builtInExecutableAllowed = true;
			builtInAppPath = appInfoFile.getFullPath();
			versionOfBuiltIn = appInfoFile.getVersion();
			if (!useDefaultExecutablePath && StringUtil.blank(alternativeManualPath)) //If manualPath requested, but there is none set, AND there's built in, swtich to built in
				useDefaultExecutablePath = true;
		}

		this.alternativeManualPath.setValue(alternativeManualPath);;
		this.useDefaultExecutablePath.setValue(useDefaultExecutablePath);;
		
	}

	public AppChooser(MesquiteModule ownerModule, AppUser appUser, boolean useDefaultExecutablePath, String alternativeManualPath) {
	//	this.useBuiltInIfAvailable = useBuiltInIfAvailable;
		this.appUser = appUser;
		this.ownerModule = ownerModule;
		if (appUser!=null) {
			officialAppNameInAppInfo = appUser.getAppOfficialName();
			programName = appUser.getProgramName();
			AppInformationFile appInfoFile = AppHarvester.getAppInfoFileForProgram(appUser);
			if (appInfoFile!=null) {
				builtInExecutableAllowed = true;
				builtInAppPath = appInfoFile.getFullPath();
				versionOfBuiltIn = appInfoFile.getVersion();
			}
		}

		this.alternativeManualPath.setValue(alternativeManualPath);
		this.useDefaultExecutablePath.setValue(useDefaultExecutablePath);
	}

	/*.................................................................................................................*/
	// adding to the module's queryOptions dialog box
	public void addToDialog(ExtensibleDialog dialog) {
		containingDialog = dialog;
		usingLabelMainDlog = dialog.addLabel(getMainDialogUsingString());
		appButton = dialog.addAListenedButton("App...", null, this);
		appButton.setActionCommand("chooseApp");
	}
	
	AppChooserDialog appChooserDialog;
	/*.................................................................................................................*/
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equalsIgnoreCase("chooseApp")) {

			//Show app chooser dialog ========================
			MesquiteInteger buttonPressed = new MesquiteInteger(1);
			appChooserDialog = new AppChooserDialog(containingDialog,  "Choose " + programName,buttonPressed, builtInExecutableAllowed, ownerModule, this, programName, versionOfBuiltIn, useDefaultExecutablePath, alternativeManualPath);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()

			appChooserDialog.completeAndShowDialog(true);
			
			if (buttonPressed.getValue()==0)  {
				useDefaultExecutablePath.setValue(appChooserDialog.builtInAppChosen());
				if (appChooserDialog.getAlternativePathField()!=null) {
					String tempPath = appChooserDialog.getAlternativePathField().getText();
					if (StringUtil.blank(tempPath) && !useDefaultExecutablePath.getValue()){
						MesquiteMessage.discreetNotifyUser("If you do not use a built-in app, then the path to " +programName+ " must be entered.");
					} else
						alternativeManualPath.setValue(tempPath);
				}
				//Remember in receiving module to receive the various parts
				// set pathOfBuiltIn etc.?
			}

			if (appUser!=null) {
				appUser.appChooserDialogBoxEntryChanged();
			}
		
			
			appChooserDialog.dispose();
			usingLabelMainDlog.setText(getMainDialogUsingString());
		//	containingDialog.repaintAll();
		} 
	}
	
	public boolean builtInAppChosen() {
		if (appChooserDialog !=null) {
			return appChooserDialog.builtInAppChosen();
		}
		return false;
	}


	/*.................................................................................................................*/
	String getMainDialogUsingString() {
		if (!useDefaultExecutablePath.getValue() && alternativeManualPath.isBlank())
			return "Please select app using button:";
		String usingString = "Using";
		if (usingBuiltIn())
			usingString += " built-in " + programName + " (version " + versionOfBuiltIn + ")";
		else
			usingString += " " + programName + " at " + alternativeManualPath;
		return usingString;
	}
	/*.................................................................................................................*/
	public void informAppUser() {
		if (appUser!=null) {
			appUser.appChooserDialogBoxEntryChanged();
		}
	}
/*.................................................................................................................*/
	boolean usingBuiltIn() {
		if (builtInExecutableAllowed) {
			return useDefaultExecutablePath.getValue();
		}
		return false;
	}

	public String getPathToUse() { 
		if (useDefaultExecutablePath.getValue())
			return builtInAppPath;
		else
			return alternativeManualPath.getValue();
	}

	public String getManualPath() { //for preference writing
		return alternativeManualPath.getValue();
	}

	public boolean useBuiltInExecutable() { //for preference writing
		return useDefaultExecutablePath.getValue();
	}

	public String getVersion() {
		return versionOfBuiltIn; //return string only for Built In
	}
}

