package mesquite.externalCommunication.lib;

import java.awt.Button;
import java.awt.event.ItemListener;

import mesquite.lib.ExtensibleDialog;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteMessage;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteString;
import mesquite.lib.RadioButtons;
import mesquite.lib.SingleLineTextField;
import mesquite.lib.StringUtil;

public class AppChooserDialog extends ExtensibleDialog {
	RadioButtons builtInVsManual;
	SingleLineTextField alternativePathField;
	
	public AppChooserDialog (Object parent, String title, MesquiteInteger buttonPressed, boolean builtInExecutableAllowed, MesquiteModule ownerModule, String programName, String versionOfBuiltIn, MesquiteBoolean useDefaultExecutablePath, MesquiteString alternativeManualPath) {
		super(parent, title);
		setFont(defaultBigFont);
		intializeDialog(title,buttonPressed);
		addWindowListener(this);
		
		addBlankLine();
		String warningUseWorking = "";
		if (builtInExecutableAllowed) {
			String builtInString = "Use built-in " + programName + " (version " + versionOfBuiltIn + ")"; 
			int defaultValue = 1;
			if (useDefaultExecutablePath.getValue())
				defaultValue = 0;
			builtInVsManual = addRadioButtons(new String[] {builtInString, "Use alternative installed copy indicated below"}, defaultValue);
			if (ownerModule !=null && ownerModule instanceof ItemListener) {
				builtInVsManual.addItemListener((ItemListener)ownerModule);
			}
			addHorizontalLine(1);
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
	
	public boolean builtInAppChosen() {
		if (builtInVsManual !=null) {
			return builtInVsManual.getValue() == 0;
		}
		return false;
	}
	
	public SingleLineTextField getAlternativePathField() {
		return alternativePathField;
	}

	
	
	

}
