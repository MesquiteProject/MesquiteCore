package mesquite.externalCommunication.AppHarvester;

import mesquite.lib.CommandChecker;
import mesquite.lib.Debugg;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteMessage;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.StringUtil;
import mesquite.lib.duties.MesquiteInit;

import java.io.File;
import java.util.Vector;

import mesquite.externalCommunication.lib.*;

public class AppHarvester extends MesquiteInit {
	static public Vector appInformationFileVector;

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		harvestApps();
		addMenuItem(MesquiteTrunk.helpMenu, "Helper Apps", makeCommand("showDetails", this));
		return true;
	}
	StringBuffer details = new StringBuffer();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Includes a file and optionally fuses taxa/characters block", null, commandName, "showDetails")) {
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
			boolean redundantRemoved = false;

			//For macOS or windows on aarch64, delete x86 if aarch64 is available
			if ((MesquiteTrunk.isMacOSX() ||MesquiteTrunk.isWindows()) && MesquiteTrunk.isAarch64()){ 
				for (int iv=numApps-1; iv>=0; iv--) {
					appInfoFile = (AppInformationFile)(appInformationFileVector.elementAt(iv));
					String programName = appInfoFile.getAppName();

					int numAarch64 = getNumAppsForProgram(programName, "aarch64");
					int numUniv = getNumAppsForProgram(programName, "univ");
					if (numAarch64>0 || numUniv >0) {
						String compiledAs = appInfoFile.getCompiledAs();
						String arch = StringUtil.getLastItem(compiledAs, ".");
						if (MesquiteTrunk.isX86(arch)) { //there is an aarch64 or universal version available, but this is x86; delete it
							appInformationFileVector.removeElement(appInfoFile);
							redundantRemoved = true;
							if (MesquiteTrunk.isMacOSX())
								details.append("— x86 version of " + programName + " ignored because an Apple Silicon version is available.\n");
							else 
								details.append("— x86 version of " + programName + " ignored because an Arm64 version is available.\n");
						}
					}

				}
			}

			numApps = appInformationFileVector.size();
			for (int iv=numApps-1; iv>=0; iv--) {
				appInfoFile = (AppInformationFile)(appInformationFileVector.elementAt(iv));
				String programName = appInfoFile.getAppName();
				int numComp = getNumAppsForProgram(programName);
				if (numComp>1) {
					details.append("— Ignored extra app of " + programName+ " (in " + appInfoFile.getAppNameWithinAppsDirectory() + "). Only the first will be used.\n");
					redundantRemoved = true;
					appInformationFileVector.removeElement(appInfoFile);
				}

			}
			if (redundantRemoved)
				sb.append("Mesquite ignored some versions of a built-in app because other copies were available. If you prefer to use the ignored version, please make sure that only the version you want to use is in the apps folder in Mesquite_Folder.\n");
			if (countFound>0)
				sb.append("For more details about apps in apps folder, choose Helper Apps from the Help menu.");
			//AppInformationFile.setAppInformationFileVector(appInformationFileVector);
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
		return getNumAppsForProgram(officialAppNameInAppInfo, null);
	}
	/*.................................................................................................................*/
	public static int getNumAppsForProgram(String officialAppNameInAppInfo, String architecture) {
		int count =0;
		if (officialAppNameInAppInfo!=null && appInformationFileVector!=null && StringUtil.notEmpty(officialAppNameInAppInfo)) {
			AppInformationFile appInfoFile;
			for (int iv=0; iv<appInformationFileVector.size(); iv++) {
				appInfoFile = (AppInformationFile)(appInformationFileVector.elementAt(iv));
				String compiledAs = appInfoFile.getCompiledAs();
				String os = StringUtil.getFirstItem(compiledAs, ".");
				String arch = StringUtil.getLastItem(compiledAs, ".");
				if (officialAppNameInAppInfo.equalsIgnoreCase(appInfoFile.getAppName()) && (StringUtil.blank(architecture) || ( (MesquiteTrunk.isX86(architecture) && MesquiteTrunk.isX86(arch))|| (MesquiteTrunk.isAarch64(architecture) && MesquiteTrunk.isAarch64(arch)))))
					count++;
			}
		}
		return count;
	}
	/*.................................................................................................................*/
	public static AppInformationFile getAppInfoFileForProgram(AppUser appUser) {
		if (appUser!=null && appInformationFileVector!=null && StringUtil.notEmpty(appUser.getAppOfficialName())) {
			AppInformationFile appInfoFile;
			for (int iv=0; iv<appInformationFileVector.size(); iv++) {
				appInfoFile = (AppInformationFile)(appInformationFileVector.elementAt(iv));
				if (appUser.getAppOfficialName().equalsIgnoreCase(appInfoFile.getAppName()))
					return appInfoFile;
			}
		}
		return null;
	}
	/*.................................................................................................................*/
	public static AppInformationFile getAppInfoFileForProgram(String officialAppNameInAppInfo) {
		if (appInformationFileVector!=null && StringUtil.notEmpty(officialAppNameInAppInfo)) {
			AppInformationFile appInfoFile;
			for (int iv=0; iv<appInformationFileVector.size(); iv++) {
				appInfoFile = (AppInformationFile)(appInformationFileVector.elementAt(iv));
				if (officialAppNameInAppInfo.equalsIgnoreCase(appInfoFile.getAppName()))
					return appInfoFile;
			}
		}
		return null;
	}

	/*.................................................................................................................*/
	public static boolean builtinAppExists(String officialAppNameInAppInfo) { 
		int numApps = getNumAppsForProgram(officialAppNameInAppInfo);
		if (numApps==1) {
			return true;

		} else if (numApps>1) {
			MesquiteMessage.warnUser("There is more than one compatible app for " + officialAppNameInAppInfo + " in the apps folder; please remove all but one copy, and restart Mesquite.");
		}
		return false;
	}


	public String getName() {
		return "App Harvester";
	}

}
