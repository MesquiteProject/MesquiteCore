package mesquite.externalCommunication.lib;

import java.awt.Button;
import java.awt.Choice;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import mesquite.externalCommunication.AppHarvester.AppHarvester;
import mesquite.lib.ListableVector;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteString;
import mesquite.lib.ui.ExtensibleDialog;
import mesquite.lib.ui.RadioButtons;
import mesquite.lib.ui.SingleLineTextField;

public class AppChooserDialog extends ExtensibleDialog implements ActionListener, ItemListener {
	RadioButtons builtInVsManual;
	SingleLineTextField alternativePathField;
	AppChooser appChooser;
	String programName;
	String alternativeManualPath;
	Choice builtInChoices = null;
	ListableVector choices;
	
	public AppChooserDialog (Object parent, String title, MesquiteInteger buttonPressed, boolean builtInExecutableAllowed, MesquiteModule ownerModule, AppChooser appChooser, MesquiteBoolean useDefaultExecutablePath, MesquiteString alternativeManualPath) {
		super(parent, title);
		setFont(defaultBigFont);
		intializeDialog(title,buttonPressed);
		addWindowListener(this);
		this.appChooser=appChooser;
		this.programName = appChooser.programName;
		if (alternativeManualPath!=null)
			this.alternativeManualPath=alternativeManualPath.getValue();
		
		addBlankLine();
		String warningUseWorking = "";
		if (builtInExecutableAllowed) {
			choices = AppHarvester.getAppInfoFilesForProgram(appChooser.officialAppNameInAppInfo);
			int numBuiltIn = 0;
			if (choices != null)
				numBuiltIn = choices.size();
			String builtInString = getBuiltInString(numBuiltIn);
			
			/*
			appChooser.builtInAppPath = appInfoFile.getFullPath();
			appChooser.versionOfBuiltIn = appInfoFile.getVersion();
			 */
			
			int defaultValue = 1;
			if (useDefaultExecutablePath.getValue())
				defaultValue = 0;
			builtInVsManual = addRadioButtons(new String[] {builtInString, "Use alternative installed copy indicated below"}, defaultValue);
			if (ownerModule !=null && ownerModule instanceof ItemListener) {
				//builtInVsManual.addItemListener((ItemListener)ownerModule);
			}
			builtInVsManual.addItemListener(this);
			addHorizontalLine(1);
			if (numBuiltIn>1) {
				int current = 0;
				for (int i = 0; i< choices.size(); i++){
					AppInformationFile appInfoFile = (AppInformationFile)choices.elementAt(i);
					if (appInfoFile.isPrimary()){
						current = i;
						break;
					}
				}
				builtInChoices = addPopUpMenu ("Built-in versions:", choices, current);
				builtInChoices.addItemListener(this);
				addHorizontalLine(1);
			}
			addLabel("Path to alternative installed copy of " + programName + ":");
			warningUseWorking = "If you use the alternative installed copy of " + programName + ", p";
		}
		else {
			addLabel("Copy of " + programName + " installed on your computer to be used:");
			warningUseWorking = "There is no copy of " + programName + " built into your version of Mesquite. P";
		}
		alternativePathField = addTextField(alternativeManualPath.getValue(), 40);

		
		Button programBrowseButton = addAListenedButton("Browse...",null, this);
		programBrowseButton.setActionCommand("programBrowse");
		addLargeOrSmallTextLabel(warningUseWorking + "lease make sure the indicated copy runs on its own from the command line/command prompt before attempting to run it from Mesquite.");

		

	}
	String getBuiltInString(int numBuiltIn){
		String builtInString = "Use built-in " + appChooser.programName + " (version " + appChooser.versionOfBuiltIn + ")"; 
		if (numBuiltIn>1)
			builtInString += " [Choice available below]";
		return builtInString;
	}
	public boolean builtInAppChosen() {
		if (builtInVsManual !=null) {
			return builtInVsManual.getValue() == 0;
		}
		return false;
	}
	
	
	public SingleLineTextField getAlternativePathField() {
		return alternativePathField;
	}

	
	/*.................................................................................................................*/
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equalsIgnoreCase("programBrowse")) {
			alternativeManualPath = MesquiteFile.openFileDialog("Choose " + programName + ":", null, null);
			if (!alternativeManualPath.isBlank()) {
				alternativePathField.setText(alternativeManualPath);
			}
		} 
	}

	/*.................................................................................................................*/
	AppInformationFile prefInfoFile;
	public void itemStateChanged(ItemEvent e) {
  		if (e.getItemSelectable() == builtInChoices){
  			int which = builtInChoices.getSelectedIndex();
  			prefInfoFile = (AppInformationFile)choices.elementAt(which);
			appChooser.builtInAppPath = prefInfoFile.getFullPath();
			appChooser.versionOfBuiltIn = prefInfoFile.getVersion();
			builtInVsManual.setLabel(0, getBuiltInString(2));
  			appChooser.informAppUser();
  		}
  		else {
  			appChooser.informAppUser();
  		}
	}
	public void storePrefsIfNeeded(){
		if (prefInfoFile != null)
  			AppHarvester.setAsPrimary(appChooser.officialAppNameInAppInfo, prefInfoFile, true);
			
	}
	
	

}
