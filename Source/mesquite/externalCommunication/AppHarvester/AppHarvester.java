package mesquite.externalCommunication.AppHarvester;

import mesquite.lib.CommandChecker;
import mesquite.lib.Debugg;
import mesquite.lib.ListableVector;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteDouble;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteMessage;
import mesquite.lib.MesquiteString;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.StringUtil;
import mesquite.lib.duties.MesquiteInit;

import java.io.File;
import java.util.Vector;

import mesquite.externalCommunication.lib.*;

public class AppHarvester extends MesquiteInit {
	static public Vector appInformationFileVector;
	static public ListableVector primaryPrefs = new ListableVector();

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		harvestApps();
		addMenuItem(MesquiteTrunk.helpMenu, "Helper Apps", makeCommand("showDetails", this));
		return true;
	}

	public void endJob(){
		storePreferences();
		super.endJob();
	}
	StringBuffer details = new StringBuffer();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Shows details of what apps are loaded.", null, commandName, "showDetails")) {
			logln("\n" + details.toString());
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/*.................................................................................................................*/
	public void harvestApps(){
		String appsDirPath = MesquiteFile.getPathWithSingleSeparatorAtEnd(MesquiteTrunk.appsDirectory);
		File appsDir = new File(appsDirPath);
		StringBuffer sb = new StringBuffer();
		StringBuffer incompReport = new StringBuffer();
		if (appsDir.exists() && appsDir.isDirectory()) {
			appInformationFileVector = new Vector();
			String[] appsFiles = appsDir.list();
			//StringArray.sort(nameRulesList);
			int countIncomp = 0;
			//Harvesting all compatible apps
			int countFound = 0;
			for (int i=0; i<appsFiles.length; i++) {
				if (appsFiles[i]!=null && appsFiles[i].endsWith("app")&& !appsFiles[i].startsWith(".")) {
					String appFilePath = appsDirPath + MesquiteFile.fileSeparator + appsFiles[i];
					AppInformationFile appInfoFile = new AppInformationFile(appsFiles[i]);
					boolean success = appInfoFile.processAppInfoFile();
					if (success) {
						countFound++;
						if (compatible(appInfoFile)) {
							appInformationFileVector.addElement(appInfoFile);
							if (countFound-countIncomp==1)
								sb.append("Loaded from apps folder: "); 
							sb.append("  "+ appInfoFile.getAppName() + " (" + appInfoFile.getVersion() + ");");
							details.append("Loaded "+ appInfoFile.getAppName() + " from " + appsFiles[i]+", version " + appInfoFile.getVersion() + "\n");
						}
						else {
							incompReport.append("— INCOMPATIBLE: "+ appInfoFile.getAppName() + " from " + appsFiles[i]+", version " + appInfoFile.getVersion() + " (compiledAs: " + appInfoFile.getCompiledAs() + ")\n");
							countIncomp++;
						}

					}
				}
			}
			if (countFound-countIncomp>0)
				sb.append("\n");
			if (countIncomp>0){
				if (countIncomp>1){
					sb.append("(" + countIncomp + " incompatible apps");
					details.append("\n" + countIncomp + " apps were found that were ");
				}
				else if (countIncomp==1){
					sb.append("(" + countIncomp + " incompatible app");
					details.append("\n" + countIncomp + " app was found that was ");
				};
				sb.append(" found, not loaded.)\n");
				details.append("incompatible with the processor or operating system, and not loaded.\n");
			}
			details.append(incompReport + "\n");
			if (MesquiteTrunk.debugMode)
				logln(incompReport.toString());
			//Filters to remove redundant apps
			int numApps = appInformationFileVector.size();
			AppInformationFile appInfoFile;
			boolean someSetAsSecondary = false;

			//For macOS or windows on aarch64, delete x86 if aarch64 is available
			if ((MesquiteTrunk.isMacOSX() ||MesquiteTrunk.isWindows()) && MesquiteTrunk.isAarch64()){ 
				for (int iv=numApps-1; iv>=0; iv--) {
					appInfoFile = (AppInformationFile)(appInformationFileVector.elementAt(iv));
					String programName = appInfoFile.getAppName();

					int numAarch64 = getNumAppsForProgram(programName, "aarch64", false);
					int numUniv = getNumAppsForProgram(programName, "univ", false);
					if (numAarch64>0 || numUniv >0) {
						String compiledAs = appInfoFile.getCompiledAs();
						String arch = StringUtil.getLastItem(compiledAs, ".");
						if (MesquiteTrunk.isX86(arch)) { //there is an aarch64 or universal version available, but this is x86; delete it
							appInfoFile.setPrimary(false);
							//	appInformationFileVector.removeElement(appInfoFile);
							someSetAsSecondary = true;
							/*	if (MesquiteTrunk.isMacOSX())
								details.append("— x86 version of " + programName + " set as secondary choice because an Apple Silicon version is available.\n");
							else 
								details.append("— x86 version of " + programName + " set as secondary choice because an Arm64 version is available.\n");
							 */
						}
					}

				}
			}

			numApps = appInformationFileVector.size();
			for (int iv=numApps-1; iv>=0; iv--) {
				appInfoFile = (AppInformationFile)(appInformationFileVector.elementAt(iv));
				String programName = appInfoFile.getAppName();
				int numComp = getNumAppsForProgram(programName, true);
				if (numComp>1) {
					//details.append("— Ignored extra app of " + programName+ " (in " + appInfoFile.getAppNameWithinAppsDirectory() + "). Only the first will be used.\n");
					someSetAsSecondary = true;
					appInfoFile.setPrimary(false);
					//	appInformationFileVector.removeElement(appInfoFile);
				}

			}
			//if (someSetAsSecondary)
			//	sb.append("Mesquite found multiple versions of a built-in app because other copies were available. If you prefer to use the ignored version, please make sure that only the version you want to use is in the apps folder in Mesquite_Folder.\n");
			if (countFound>0)
				sb.append("For more details about apps in apps folder, choose Helper Apps from the Help menu.");
			//AppInformationFile.setAppInformationFileVector(appInformationFileVector);
		}

		//now, impose the preferences on the app list
		for (int i =0; i< primaryPrefs.size(); i++){
			MesquiteString ms = (MesquiteString)primaryPrefs.elementAt(i);
			AppInformationFile appInfoFile = getAppInfoFile(ms.getName(), ms.getValue());
			setAsPrimary(ms.getName(), appInfoFile, false);
		}

		logln(sb.toString());
	}

	private boolean compatible(AppInformationFile appInfoFile) {
		String compiledAs = appInfoFile.getCompiledAs();
		String os = StringUtil.getFirstItem(compiledAs, ".");
		String arch = StringUtil.getLastItem(compiledAs, ".");
		if (StringUtil.blank(os) || StringUtil.blank(arch))
			return false;
		return appCompatibleWithArchitecture(os, arch);
	}

	static boolean appCompatibleWithArchitecture(String appOS, String appArch) {
		//both macos & windows under aarch64 can handle x86 programs
		if ((appOS.equalsIgnoreCase("macos") && MesquiteTrunk.isMacOSX()) || (appOS.equalsIgnoreCase("windows") && MesquiteTrunk.isWindows())) {
			if (appArch.equalsIgnoreCase("univ"))
				return true;
			else if ( MesquiteTrunk.isAarch64())
				return MesquiteTrunk.isAarch64(appArch) || MesquiteTrunk.isX86(appArch);
			else if ( MesquiteTrunk.isX86())
				return MesquiteTrunk.isX86(appArch);
		}
		else if (appOS.equalsIgnoreCase("linux") && MesquiteTrunk.isLinux()) {
			return (MesquiteTrunk.isAarch64(appArch) && MesquiteTrunk.isAarch64()) ||  (MesquiteTrunk.isX86(appArch) && MesquiteTrunk.isX86());
		}
		return false;
	}
	/*.................................................................................................................*/
	public static int getNumAppsForProgram(String officialAppNameInAppInfo) {
		return getNumAppsForProgram(officialAppNameInAppInfo, null, false);
	}
	/*.................................................................................................................*/
	static int getNumAppsForProgram(String officialAppNameInAppInfo, boolean primaryOnly) {
		return getNumAppsForProgram(officialAppNameInAppInfo, null, primaryOnly);
	}
	/*.................................................................................................................*/
	static int getNumAppsForProgram(String officialAppNameInAppInfo, String architecture, boolean primaryOnly) {
		int count =0;
		if (officialAppNameInAppInfo!=null && appInformationFileVector!=null && StringUtil.notEmpty(officialAppNameInAppInfo)) {
			AppInformationFile appInfoFile;
			for (int iv=0; iv<appInformationFileVector.size(); iv++) {
				appInfoFile = (AppInformationFile)(appInformationFileVector.elementAt(iv));
				if (appInfoFile.isPrimary() || !primaryOnly){
					String compiledAs = appInfoFile.getCompiledAs();
					//String os = StringUtil.getFirstItem(compiledAs, ".");
					String arch = StringUtil.getLastItem(compiledAs, ".");
					if (officialAppNameInAppInfo.equalsIgnoreCase(appInfoFile.getAppName()) && (StringUtil.blank(architecture) || ( (MesquiteTrunk.isX86(architecture) && MesquiteTrunk.isX86(arch))|| (MesquiteTrunk.isAarch64(architecture) && MesquiteTrunk.isAarch64(arch)))))
						count++;
				}
			}
		}
		return count;
	}
	/*.................................................................................................................*/
	public static AppInformationFile getAppInfoFile(String officialAppNameInAppInfo, String appBundleName) {
		if (appInformationFileVector!=null && StringUtil.notEmpty(officialAppNameInAppInfo)) {
			AppInformationFile appInfoFile;
			for (int iv=0; iv<appInformationFileVector.size(); iv++) {
				appInfoFile = (AppInformationFile)(appInformationFileVector.elementAt(iv));
				if (officialAppNameInAppInfo.equalsIgnoreCase(appInfoFile.getAppName()) && appBundleName.equals(appInfoFile.getAppNameWithinAppsDirectory())){
					return appInfoFile;
				}
			}
		}
		return null;
	}
	/*.................................................................................................................*/
	public static AppInformationFile getAppInfoFileForProgram(AppUser appUser) {
		if (appUser!= null)
			return getAppInfoFileForProgram(appUser.getAppOfficialName());
		return null;
	}
	/*.................................................................................................................*/
	public static AppInformationFile getAppInfoFileForProgram(String officialAppNameInAppInfo) {
		if (appInformationFileVector!=null && StringUtil.notEmpty(officialAppNameInAppInfo)) {
			AppInformationFile appInfoFile;
			for (int iv=0; iv<appInformationFileVector.size(); iv++) {
				appInfoFile = (AppInformationFile)(appInformationFileVector.elementAt(iv));
				if (appInfoFile.isPrimary() && officialAppNameInAppInfo.equalsIgnoreCase(appInfoFile.getAppName())){  
					return appInfoFile;
				}
			}
		}
		return null;
	}
	/*.................................................................................................................*/
	public static void setAsPrimary(String officialAppNameInAppInfo, AppInformationFile primary, boolean storePreferences) {
		if (primary == null)
			return;
		if (appInformationFileVector!=null && StringUtil.notEmpty(officialAppNameInAppInfo)) {
			AppInformationFile appInfoFile;
			for (int iv=0; iv<appInformationFileVector.size(); iv++) {
				appInfoFile = (AppInformationFile)(appInformationFileVector.elementAt(iv));
				if (officialAppNameInAppInfo.equalsIgnoreCase(appInfoFile.getAppName())){
					appInfoFile.setPrimary(appInfoFile == primary);
				}
			}
			if (storePreferences) {
				AppHarvester harvester = (AppHarvester)MesquiteTrunk.mesquiteTrunk.findEmployeeWithDuty(AppHarvester.class);
				if (harvester != null){
					int current = primaryPrefs.indexOfByNameIgnoreCase(officialAppNameInAppInfo);
					if (current>=0){
						MesquiteString ms = (MesquiteString)primaryPrefs.elementAt(current);
						ms.setValue(primary.getAppNameWithinAppsDirectory());
					}
					else 
						primaryPrefs.addElement(new MesquiteString(officialAppNameInAppInfo, primary.getAppNameWithinAppsDirectory()), false);
					harvester.storePreferences();
				}
			}
		}
	}

	/*.................................................................................................................*/
	public static ListableVector getAppInfoFilesForProgram(AppUser appUser) {
		if (appUser!= null)
			return getAppInfoFilesForProgram(appUser.getAppOfficialName());
		return null;
	}
	/*.................................................................................................................*/
	public static ListableVector getAppInfoFilesForProgram(String officialAppNameInAppInfo) {
		if (appInformationFileVector!=null && StringUtil.notEmpty(officialAppNameInAppInfo)) {
			ListableVector v = new ListableVector();
			AppInformationFile appInfoFile;
			for (int iv=0; iv<appInformationFileVector.size(); iv++) {
				appInfoFile = (AppInformationFile)(appInformationFileVector.elementAt(iv));
				if (appInfoFile.isPrimary() && officialAppNameInAppInfo.equalsIgnoreCase(appInfoFile.getAppName()))
					v.addElement(appInfoFile, false);
			}
			for (int iv=0; iv<appInformationFileVector.size(); iv++) {
				appInfoFile = (AppInformationFile)(appInformationFileVector.elementAt(iv));
				if (!appInfoFile.isPrimary() && officialAppNameInAppInfo.equalsIgnoreCase(appInfoFile.getAppName()))
					v.addElement(appInfoFile, false);
			}
			return v;
		}
		return null;
	}
	/*.................................................................................................................*/
	public static boolean builtinAppExists(String officialAppNameInAppInfo) { 
		int numApps = getNumAppsForProgram(officialAppNameInAppInfo);
		if (numApps>0) {
			return true;

		} /*else if (numApps>1) {
			MesquiteMessage.warnUser("There is more than one compatible app for " + officialAppNameInAppInfo + " in the apps folder; please remove all but one copy, and restart Mesquite.");
		}*/
		return false;
	}

	ListableVector primaryPreferences = new ListableVector();
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "whatever", " yes");  
		for (int i =0; i< primaryPrefs.size(); i++){
			MesquiteString ms = (MesquiteString)primaryPrefs.elementAt(i);
			StringUtil.appendXMLTag(buffer, 2, "setPrimary", StringUtil.tokenize(ms.getName()) +" " + StringUtil.tokenize(ms.getValue()));  
		}
		return buffer.toString();
	}
	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("setPrimary".equalsIgnoreCase(tag)){
			//first find role played & signature of identity
			parser.setString(content);
			String role = parser.getFirstToken(content); //e.g. iq-tree
			String signature = parser.getNextToken(); //e.g., iqtree.3.0.3-macos-univ.app
			primaryPrefs.addElement(new MesquiteString(role, signature), false);
			//then signature to identify
		}

	}

	public String getName() {
		return "App Harvester";
	}

}
