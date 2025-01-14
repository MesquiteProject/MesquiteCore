package mesquite.externalCommunication.AppHarvester;

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
		return true;
	}

	/*.................................................................................................................*/
	public void harvestApps(){
		String appsDirPath = MesquiteFile.getPathWithSingleSeparatorAtEnd(MesquiteTrunk.appsDirectory);
		File appsDir = new File(appsDirPath);
		StringBuffer sb = new StringBuffer();
		if (appsDir.exists() && appsDir.isDirectory()) {
			appInformationFileVector = new Vector();
			String[] appsFiles = appsDir.list();
			//StringArray.sort(nameRulesList);
			int countIncomp = 0;
			//Harvesting all compatible apps
			for (int i=0; i<appsFiles.length; i++) {
				if (appsFiles[i]!=null && appsFiles[i].endsWith("app")&& !appsFiles[i].startsWith(".")) {
					String appFilePath = appsDirPath + MesquiteFile.fileSeparator + appsFiles[i];
					AppInformationFile appInfoFile = new AppInformationFile(appsFiles[i]);
					boolean success = appInfoFile.processAppInfoFile();
					if (success) {
						if (compatible(appInfoFile)) {
							appInformationFileVector.addElement(appInfoFile);
							sb.append("Loading "+ appInfoFile.getAppName() + " from " + appsFiles[i]+", version " + appInfoFile.getVersion() + "\n");
						}
						else {
							if (MesquiteTrunk.debugMode)
								sb.append("INCOMPATIBLE: "+ appInfoFile.getAppName() + " from " + appsFiles[i]+", version " + appInfoFile.getVersion() + " (compiledAs: " + appInfoFile.getCompiledAs() + ")\n");
							countIncomp++;
						}

					}
				}
			}
			if (countIncomp>1)
				sb.append(" —" + countIncomp + " other apps also in found apps folder, but they were incompatible with the processor architecture or the operating system.\n");
			else if (countIncomp==1)
				sb.append(" —" + countIncomp + " other app also in found apps folder, but it was incompatible with the processor architecture or the operating system.\n");

			//Filters to remove redundant apps
			int numApps = appInformationFileVector.size();
			AppInformationFile appInfoFile;
			
			//For macOS, delete x86 if aarch64 is available
			if (MesquiteTrunk.isMacOSX() && MesquiteTrunk.isAarch64()){ 
				for (int iv=numApps-1; iv>=0; iv--) {
					appInfoFile = (AppInformationFile)(appInformationFileVector.elementAt(iv));
					String programName = appInfoFile.getAppName();

					int numAarch64 = getNumAppsForProgram(programName, "aarch64");
					int numUniv = getNumAppsForProgram(programName, "univ");
					if (numAarch64>0 || numUniv >0) {
						String compiledAs = appInfoFile.getCompiledAs();
						String arch = StringUtil.getLastItem(compiledAs, ".");
						if ("x86".equalsIgnoreCase(arch)) { //there is an aarch64 or universal version available, but this is x86; delete it
							appInformationFileVector.removeElement(appInfoFile);
							sb.append(" —x86 version of " + programName + " ignored because an Apple Silicon version is available.\n");
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
					sb.append(" —will ignore extra app of " + programName+ " (in " + appInfoFile.getAppNameWithinAppsDirectory() + "). Only the first will be used.\n");
					appInformationFileVector.removeElement(appInfoFile);
				}

			}

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
		if (os.equalsIgnoreCase("macos") && MesquiteTrunk.isMacOSX()) {
			if (arch.equalsIgnoreCase("univ"))
				return true;
			else if ( MesquiteTrunk.isAarch64())
				return arch.equalsIgnoreCase("aarch64") || arch.equalsIgnoreCase("x86");
			else if ( MesquiteTrunk.isX86())
				return arch.equalsIgnoreCase("x86");
		}
		else if (os.equalsIgnoreCase("windows") && MesquiteTrunk.isWindows()) {
			return arch.equalsIgnoreCase("aarch64") && MesquiteTrunk.isAarch64();
		}
		else if (os.equalsIgnoreCase("linux") && MesquiteTrunk.isLinux()) {
			return arch.equalsIgnoreCase("aarch64") && MesquiteTrunk.isAarch64();
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
				String arch = StringUtil.getLastItem(compiledAs, ".");
				if (officialAppNameInAppInfo.equalsIgnoreCase(appInfoFile.getAppName()) && (StringUtil.blank(architecture) || architecture.equalsIgnoreCase(arch)))
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
