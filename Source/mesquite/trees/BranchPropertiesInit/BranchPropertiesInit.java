package mesquite.trees.BranchPropertiesInit;

import mesquite.lib.Associable;
import mesquite.lib.CommandChecker;
import mesquite.lib.Debugg;
import mesquite.lib.ListableVector;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteDouble;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteListener;
import mesquite.lib.MesquiteMessage;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.NameReference;
import mesquite.lib.Notification;
import mesquite.lib.ParseUtil;
import mesquite.lib.Parser;
import mesquite.lib.PropertyRecord;
import mesquite.lib.StringUtil;
import mesquite.lib.duties.MesquiteInit;
import mesquite.lib.tree.MesquiteTree;
import mesquite.lib.tree.NewickDialect;
import mesquite.lib.tree.DisplayableBranchProperty;
import mesquite.lib.tree.BranchProperty;

import java.io.File;
import java.util.Vector;

import mesquite.externalCommunication.lib.*;

public class BranchPropertiesInit extends MesquiteInit implements MesquiteListener {

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		initiatePrefsList();
		/*search for Betweenness (Associable, ReintepretBranchLengths, NodeAssoc between (disable resetting one with pref specified), elsewhere.
		 * allow setting if not in settings file.
		 * */
		harvestPropertySettings();
		return true;
	}
	
	public void endJob(){
		super.endJob();
		storePreferences();

	}
	/*.................................................................................................................*/
	Parser parser = new Parser();

	public void initiatePrefsList(){
		ListableVector prefsList = DisplayableBranchProperty.branchPropertyDisplayPreferences;
		DisplayableBranchProperty nL = new DisplayableBranchProperty(MesquiteTree.nodeLabelName, Associable.BUILTIN);
		nL.setBelongsToBranch(false);
		nL.showOnTerminals = false;
		nL.showName = false;
		prefsList.addElement(nL, false);
		DisplayableBranchProperty bL = new DisplayableBranchProperty(MesquiteTree.branchLengthName, Associable.BUILTIN);
		prefsList.addElement(bL, false);
		DisplayableBranchProperty cF = new DisplayableBranchProperty("consensusFrequency", Associable.DOUBLES);
		prefsList.addElement(cF, false);
		// booleans sequence: showName, centered, whiteEdges, showOnTerminals, showIfUnassigned, percentage, vertical
		parser.setString(" false false false false false true false true ");
		cF.setBooleans(parser);
		// numbers sequence: fontSize, xOffset, yOffset, digits, color, thresholdValueToShow
		parser.setString(" x 0 0  0 x ?  ");
		cF.setNumbers(parser);
		DisplayableBranchProperty bF = new DisplayableBranchProperty("bootstrapFrequency", Associable.DOUBLES);
		prefsList.addElement(bF, false);
		bF.cloneFrom(cF);
		loadPreferences();
		DisplayableBranchProperty.mergeIntoPreferences(temp);
		DisplayableBranchProperty.branchPropertyDisplayPreferences.addListener(this);
		temp.removeAllElements(false);
	}

	ListableVector temp = new ListableVector();
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		for (int k = 0; k< DisplayableBranchProperty.branchPropertyDisplayPreferences.size(); k++){
			DisplayableBranchProperty property = (DisplayableBranchProperty)DisplayableBranchProperty.branchPropertyDisplayPreferences.elementAt(k);
			StringUtil.appendXMLTag(buffer, 2, "addRecord", StringUtil.tokenize(property.getName()) + " " + property.kind);  
			StringUtil.appendXMLTag(buffer, 2, "setBooleans", StringUtil.tokenize(property.getName()) + " " + property.kind + " " + property.getBooleansString());  
			StringUtil.appendXMLTag(buffer, 2, "setNumbers", StringUtil.tokenize(property.getName()) + " " + property.kind + " " + property.getNumbersString());  
		}
		return buffer.toString();
	}
	public void processSingleXMLPreference (String tag, String flavor, String content){
		processSingleXMLPreference(tag, null, content);
	}

	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("addRecord".equalsIgnoreCase(tag)){
			String name = parser.getFirstToken(content);
			NameReference nr = NameReference.getNameReference(name);
			int kind = MesquiteInteger.fromString(parser);
			DisplayableBranchProperty nL = new DisplayableBranchProperty(name, kind);
			DisplayableBranchProperty property = (DisplayableBranchProperty)DisplayableBranchProperty.findInList(temp, nr, kind);
			if (property == null) {
				property = new DisplayableBranchProperty(name, kind);
				temp.addElement(nL, false);			
			}
		}
		else if ("setBooleans".equalsIgnoreCase(tag)){
			String name = parser.getFirstToken(content);
			NameReference nr = NameReference.getNameReference(name);
			int kind = MesquiteInteger.fromString(parser);
			DisplayableBranchProperty property = (DisplayableBranchProperty)DisplayableBranchProperty.findInList(temp, nr, kind);
			if (property != null)
				property.setBooleans(parser);
		}
		else if ("setNumbers".equalsIgnoreCase(tag)){
			String name = parser.getFirstToken(content);
			NameReference nr = NameReference.getNameReference(name);
			int kind = MesquiteInteger.fromString(parser);
			DisplayableBranchProperty property = (DisplayableBranchProperty)DisplayableBranchProperty.findInList(temp, nr, kind);
			if (property != null)
				property.setNumbers(parser);
		}

	}
	/*.................................................................................................................*/
	//Assignments of properties as being at nodes versus branches are stored in Mesquite_Folder/settings/trees/BranchPropertiesInit
	public void harvestPropertySettings(){
		String settingsDirPath = getInstallationSettingsPath();
		File settingsDir = new File(settingsDirPath);
		StringBuffer sb = new StringBuffer();
		if (settingsDir.exists() && settingsDir.isDirectory()) {
			String[] settingFiles = settingsDir.list();
			ListableVector.sort(settingFiles);
			for (int i=0; i<settingFiles.length; i++) {
				if (settingFiles[i]!=null && settingFiles[i].endsWith("txt")&& !settingFiles[i].startsWith(".")) {
					String settingFilePath = settingsDirPath + MesquiteFile.fileSeparator + settingFiles[i];
					
					String[] lines = MesquiteFile.getFileContentsAsStrings(settingFilePath);
					if (lines !=null)
						for (int k = 0; k<lines.length; k++){
							String line = lines[k];
							if (ParseUtil.firstDarkChar(line) != '#'){
								String[] tokens = StringUtil.tabDelimitedTokensToStrings(line);
								if (tokens != null && tokens.length>1){
									String name = tokens[0];
									int kind = -1;
									if ("double".equalsIgnoreCase(tokens[1]))
											kind = Associable.DOUBLES;
									else if ("long".equalsIgnoreCase(tokens[1]))
										kind = Associable.LONGS;
									else if ("String".equalsIgnoreCase(tokens[1]))
										kind = Associable.STRINGS;
									else if ("boolean".equalsIgnoreCase(tokens[1]))
										kind = Associable.BITS;
									else if ("object".equalsIgnoreCase(tokens[1]))
										kind = Associable.OBJECTS;
									boolean belongsToBranches = true;
									if ("double".equalsIgnoreCase(tokens[1]))
											kind = Associable.DOUBLES;
									else if ("long".equalsIgnoreCase(tokens[1]))
										kind = Associable.LONGS;
									else if ("String".equalsIgnoreCase(tokens[1]))
										kind = Associable.STRINGS;
									else if ("boolean".equalsIgnoreCase(tokens[1]))
										kind = Associable.BITS;
									else if ("object".equalsIgnoreCase(tokens[1]))
										kind = Associable.OBJECTS;
									
									NameReference nr = NameReference.getNameReference(name);
									BranchProperty pr = BranchProperty.findInBranchPropertySettings(nr, kind);
									if (pr == null)
										pr = new BranchProperty(name, kind);
									if (tokens.length>2){
										String assignment = tokens[2];
										if ("node".equalsIgnoreCase(assignment) || "nodes".equalsIgnoreCase(assignment))
											pr.setBelongsToBranch(false, false);
									}
									BranchProperty.branchPropertiesSettingsVector.addElement(pr, false);
								}
							}
					}
				}
			}
		}
	}
    /** passes which object changed, along with optional Notification object with details (e.g., code number (type of change) and integers (e.g. which character))*/
	public void changed(Object caller, Object obj, Notification notification){
		int code = Notification.getCode(notification); 
		if (obj == DisplayableBranchProperty.branchPropertyDisplayPreferences){
			//if (MesquiteTrunk.developmentMode)
			//	System.out.println("Branch/node property display preferences saved.");
			storePreferences();
		}
				
	}

	public String getName() {
		return "Branch/Node Properties INIT";
	}

}
