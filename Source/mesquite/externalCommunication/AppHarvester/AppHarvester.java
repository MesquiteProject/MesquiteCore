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
 			for (int i=0; i<appsFiles.length; i++) {
 				if (appsFiles[i]!=null && appsFiles[i].endsWith("app")) {
 					String appFilePath = appsDirPath + MesquiteFile.fileSeparator + appsFiles[i];
 					AppInformationFile appInfoFile = new AppInformationFile(appsFiles[i]);
 		 			boolean success = appInfoFile.processAppInfoFile();
 		 			if (success) {
 		 				appInformationFileVector.addElement(appInfoFile);
 	 					sb.append("Loading "+ appsFiles[i]+", version " + appInfoFile.getVersion() + "\n");
 		 			}
				}
 			}
 			//AppInformationFile.setAppInformationFileVector(appInformationFileVector);
 		}
 		logln(sb.toString());
	}
	/*.................................................................................................................*/
	public static int getNumAppsForProgram(AppUser appUser) {
		int count =0;
		if (appUser!=null && appInformationFileVector!=null && StringUtil.notEmpty(appUser.getAppOfficialName())) {
			AppInformationFile appInfoFile;
			for (int iv=0; iv<appInformationFileVector.size(); iv++) {
				appInfoFile = (AppInformationFile)(appInformationFileVector.elementAt(iv));
				if (appUser.getAppOfficialName().equalsIgnoreCase(appInfoFile.getAppName()))
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
	public static void examineAppsFolder(AppUser appUser) { 
		int numApps = getNumAppsForProgram(appUser);
		if (numApps==1) {
			appUser.setHasApp(true);
			
		} else if (numApps>1) {
			MesquiteMessage.warnUser("There is more than one " + appUser.getProgramName() + " app in the apps folder; please remove all but one copy, and restart Mesquite.");
		}
	}


	public String getName() {
		return "App Harvester";
	}

}
