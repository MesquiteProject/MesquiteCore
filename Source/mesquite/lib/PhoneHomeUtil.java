package mesquite.lib;

import java.util.*;

import org.dom4j.*;

public class PhoneHomeUtil {	

	static int OS = 0;
	static int OSVERSION=1;
	static int JAVAVERSION=2;

	
	/*.................................................................................................................*/
	public static void readOldPhoneRecords(String path, ListableVector phoneRecords) {
		if (StringUtil.blank(path))
			return ;
		String oldPhoneRecords = null;
		try{
			oldPhoneRecords = MesquiteFile.getFileContentsAsString(path, -1, 100, false);
		} catch (Exception e) {
			return ;
		}
		if (StringUtil.blank(oldPhoneRecords))
			return;
		
		Element root = XMLUtil.getRootXMLElementFromString("mesquite", oldPhoneRecords);
		if (root==null)
			return;
		
		Element messagesFromHome = root.element("phoneRecords");
		if (messagesFromHome != null) {
			Element versionElement = messagesFromHome.element("version");
			if (versionElement == null || !versionElement.getText().equals("1")) {
				return ;
			}

//let's get the phone records
			List noticesFromHomeList = messagesFromHome.elements("record");
			for (Iterator iter = noticesFromHomeList.iterator(); iter.hasNext();) {   // this is going through all of the notices
				Element messageElement = (Element) iter.next();
				String moduleName = messageElement.elementText("module");
				MesquiteModuleInfo mmi = MesquiteTrunk.mesquiteModulesInfoVector.findModule(MesquiteModule.class, moduleName);
				int lastVersionUsedInt = MesquiteInteger.fromString(messageElement.elementText("lastVersionUsed"));
				int lastNotice = MesquiteInteger.fromString(messageElement.elementText("lastNotice"));
				int lastNoticeForMyVersion = MesquiteInteger.fromString(messageElement.elementText("lastNoticeForMyVersion"));
				if (mmi!=null && lastVersionUsedInt != getVersion(mmi))
					lastNoticeForMyVersion = 0;
				int lastVersionNoticed = MesquiteInteger.fromString(messageElement.elementText("lastVersionNoticed"));
				int lastNewerVersionReported = MesquiteInteger.fromString(messageElement.elementText("lastNewerVersionReported"));
				
				PhoneHomeRecord phoneRecord = new PhoneHomeRecord(moduleName, lastVersionUsedInt, lastNotice,  lastNoticeForMyVersion,  lastVersionNoticed, lastNewerVersionReported);
				phoneRecords.addElement(phoneRecord, false);
			}
			
		} 
	}
	/*.................................................................................................................*/
	public static void writePhoneRecords(String path, ListableVector phoneRecords) {
		if (StringUtil.blank(path))
			return ;
		Element mesquiteElement = DocumentHelper.createElement("mesquite");
		Document doc = DocumentHelper.createDocument(mesquiteElement);
		Element phoneRecordElement = DocumentHelper.createElement("phoneRecords");
		mesquiteElement.add(phoneRecordElement);
		Element versionElement = DocumentHelper.createElement("version");
		versionElement.addText("1");
		phoneRecordElement.add(versionElement);

		for (int i= 0; i<phoneRecords.size(); i++){
			Element recordElement = DocumentHelper.createElement("record");
			phoneRecordElement.add(recordElement);
			PhoneHomeRecord phoneRecord = (PhoneHomeRecord)phoneRecords.elementAt(i);
			Element element = DocumentHelper.createElement("module");
			element.add(DocumentHelper.createCDATA(phoneRecord.getModuleName()));
			recordElement.add(element);
			MesquiteModuleInfo mmi = MesquiteTrunk.mesquiteModulesInfoVector.findModule(MesquiteModule.class, phoneRecord.getModuleName());
			XMLUtil.addFilledElement(recordElement, "lastVersionUsed",MesquiteInteger.toString(phoneRecord.getLastVersionUsed()));
			XMLUtil.addFilledElement(recordElement, "lastNotice",MesquiteInteger.toString(phoneRecord.getLastNotice()));
			XMLUtil.addFilledElement(recordElement, "lastNoticeForMyVersion",MesquiteInteger.toString(phoneRecord.getLastNoticeForMyVersion()));
			XMLUtil.addFilledElement(recordElement, "lastVersionNoticed",MesquiteInteger.toString(phoneRecord.getLastVersionNoticed()));
			XMLUtil.addFilledElement(recordElement, "lastNewerVersionReported",MesquiteInteger.toString(phoneRecord.getLastNewerVersionReported()));
		}
		String xml = XMLUtil.getDocumentAsXMLString(doc);
		if (!StringUtil.blank(xml))
			MesquiteFile.putFileContents(path, xml, true);

	}



	

	
	/*.................................................................................................................*/
	public static int getVersion(MesquiteModuleInfo mmi) {
		if (mmi.getIsPackageIntro())
			return mmi.getPackageVersionInt();
		else
			return mmi.getVersionInt();
	}
	/*.................................................................................................................*/
	public static boolean currentBuildGreaterThan(String buildLetter, int buildNumber) {
		String currentBuildLetter = MesquiteModule.getBuildLetter();
		int currentBuildNumber = MesquiteModule.getBuildNumber();
		boolean greater = StringUtil.firstStringIsGreaterThan(currentBuildLetter,buildLetter);
		return greater || (currentBuildLetter.equalsIgnoreCase(buildLetter)&&currentBuildNumber>buildNumber);
	}
	/*.................................................................................................................*/
	public static void processSingleNotice(MesquiteModuleInfo mmi, StringBuffer notices, MesquiteInteger countNotices, int noticeVersion, int notice, String noticeType, String message, int lastVersionNoticed, int lastNoticeForMyVersion, int lastNotice, PhoneHomeRecord phoneHomeRecord, Vector osVector, String forBuildLetter, int forBuildNumber) {
		if (MesquiteInteger.isCombinable(noticeVersion)){
			if (MesquiteInteger.isCombinable(notice)){

				boolean appliesToOSVersion = true;
				if (osVector!=null) {
					appliesToOSVersion=false;
					for (int i=0; i<osVector.size() && !appliesToOSVersion; i++) {
						String [] osStrings = (String[])osVector.get(i);
						boolean osMatches =(StringUtil.blank(osStrings[OS])|| System.getProperty("os.name").startsWith(osStrings[OS]));
						boolean osVersionMatches =(StringUtil.blank(osStrings[OSVERSION])|| System.getProperty("os.version").startsWith(osStrings[OSVERSION]));
						boolean javaMatches =(StringUtil.blank(osStrings[JAVAVERSION])|| MesquiteTrunk.getJavaVersionAsString().startsWith(osStrings[JAVAVERSION]));
						if (osMatches && osVersionMatches && javaMatches) {
							appliesToOSVersion=true;
						}
					}
				}
				
				boolean appliesToBuild = true;
				if (mmi.getName().equals("Mesquite") && !StringUtil.blank(forBuildLetter) && MesquiteInteger.isCombinable(forBuildNumber)) {
					appliesToBuild = !currentBuildGreaterThan(forBuildLetter, forBuildNumber);
				}

				//suppose Mesquite is version 2.01
				int currentVersion = getVersion(mmi);

				//notice assumed to have been seen before if its version number is less than current
				boolean seenBefore = noticeVersion < currentVersion;  //e.g., notice is version 2.0

				//or if Mesquite's version is same as notice's, but notice number is already seen for this version than last one noticed.
				seenBefore = seenBefore || (noticeVersion ==  currentVersion && notice <= lastNoticeForMyVersion);  //e.g., notice is 2.01; notice number has already been seen

				//or if Mesquite's version is less than notice's, and notice's is same as lastVersion noticed, but notice is already seen.
				seenBefore = seenBefore || (currentVersion<noticeVersion && lastVersionNoticed == noticeVersion && notice <= lastNotice);  //e.g., notice is 2.02; 2.02 notices previously read; notice already seen

				//or if Mesquite's version is less than notice's, and notice's is less than as lastVersion noticed, but notice is already seen.
				seenBefore = seenBefore || (currentVersion<noticeVersion && lastVersionNoticed> noticeVersion);  //e.g., notice is 2.02; 2.03 notices previously read

				// otherwise assumed to have been seen before if version is same as current and notice is at or before recalled one
				if (!seenBefore && appliesToOSVersion && appliesToBuild){  //relevant
					if (noticeType != null && noticeType.equalsIgnoreCase("alert")){
						//notices.append( countNotices.toString() + ". " + message + "<hr>\n");
						notices.append(message + "<hr>\n");
						countNotices.increment();
					}
					else
						MesquiteMessage.println("NOTICE " + message);
					if (noticeVersion ==  currentVersion){  //version of note is this version of Mesquite
						if (phoneHomeRecord.getLastNoticeForMyVersion() < notice)  // this is a later notice than we had seen before; record it
							phoneHomeRecord.setLastNoticeForMyVersion(notice);
						if (noticeVersion ==  phoneHomeRecord.getLastVersionNoticed() && notice >phoneHomeRecord.getLastNotice())
							phoneHomeRecord.setLastNotice(notice);
					}
					if (noticeVersion >=  phoneHomeRecord.getLastVersionNoticed()){
						phoneHomeRecord.setLastVersionNoticed(noticeVersion);
						if (notice >phoneHomeRecord. getLastNotice())
							phoneHomeRecord.setLastNotice(notice);
					}

				}
			}

		} //

	}

	/*.................................................................................................................*/
	public static String retrieveMessagesFromHome(MesquiteModuleInfo mmi, PhoneHomeRecord phoneHomeRecord, StringBuffer logBuffer) {
		String url = mmi.getHomePhoneNumber();
		if (StringUtil.blank(url))
			return null;
		String noticesFromHome = null;
		try{
			noticesFromHome = MesquiteFile.getURLContentsAsString(url, -1, false);
		} catch (Exception e) {
			return null;
		}
		if (StringUtil.blank(noticesFromHome))
			return null;
		Element root = XMLUtil.getRootXMLElementFromString("mesquite",noticesFromHome);
		if (root==null)
			return null;
		Element messagesFromHome = root.element("MessagesFromHome");
		if (messagesFromHome != null) {
			Element versionElement = messagesFromHome.element("version");
			if (versionElement == null || !versionElement.getText().equals("1")) { 
				return null;
			}

			StringBuffer notices = new StringBuffer();

			
			MesquiteInteger countNotices = new MesquiteInteger(1);
			int lastNoticeForMyVersion = phoneHomeRecord.getLastNoticeForMyVersion();
			int lastNotice = phoneHomeRecord.getLastNotice();
			int lastVersionNoticed = phoneHomeRecord.getLastVersionNoticed();

//let's get the notices
			List noticesFromHomeList = messagesFromHome.elements("notice");
			for (Iterator iter = noticesFromHomeList.iterator(); iter.hasNext();) {   // this is going through all of the notices
				Element messageElement = (Element) iter.next();
				int version = MesquiteInteger.fromString(messageElement.elementText("forVersion"));  
				String forBuildLetter = messageElement.elementText("forBuildLetter");
				int forBuildNumber = MesquiteInteger.fromString(messageElement.elementText("forBuildNumber"));
				int noticeNumber = MesquiteInteger.fromString(messageElement.elementText("noticeNumber"));
				String messageType = messageElement.elementText("messageType");
				String message = messageElement.elementText("message");
				
				Vector osVector = null;
				List osList = messageElement.elements("forOS");
				for (Iterator i = osList.iterator(); i.hasNext();) {   // this is going through all of the notices
					if (osVector==null)
						osVector = new Vector();
					Element osElement = (Element) i.next();
					String[] osStrings= new String[3];
					osStrings[OS] = osElement.elementText("OS");
					osStrings[OSVERSION] = osElement.elementText("OSVersion");
					osStrings[JAVAVERSION]  = osElement.elementText("JavaVersion");
					osVector.addElement(osStrings);
				}
				// process other notice tags here if they are present
				
				processSingleNotice(mmi, notices, countNotices, version, noticeNumber, messageType, message,  lastVersionNoticed, lastNoticeForMyVersion,  lastNotice,phoneHomeRecord, osVector, forBuildLetter, forBuildNumber);

			}
			
// now see if there is a tag for the current release version
			Element currentReleaseVersion = messagesFromHome.element("currentReleaseVersion");
			if (currentReleaseVersion !=null) {
				String releaseString = "";
				String releaseStringHTML ="";
				String versionString = currentReleaseVersion.elementText("versionString");
				String buildString = currentReleaseVersion.elementText("build");
				String URL = currentReleaseVersion.elementText("URL");
				String downloadURL = currentReleaseVersion.elementText("downloadURL");
				int releaseVersionInt = MesquiteInteger.fromString(currentReleaseVersion.elementText("version"));
				int userVersionInt = getVersion(mmi);
				if (!StringUtil.blank(versionString))
					releaseString+="The current release version of " + mmi.getName() + " is " + versionString;
				if (!StringUtil.blank(buildString))
					releaseString+= " build " + buildString;
				if (!StringUtil.blank(releaseString)) {
					if (mmi.getIsPackageIntro()) {
						if (!StringUtil.blank(mmi.getPackageVersion()))
							releaseString+= " (the version you have installed is "+ mmi.getPackageVersion() + ").";
					}
					else if (!StringUtil.blank(mmi.getVersion()))
						releaseString+= " (the version you have installed is "+ mmi.getVersion() + ").";
					releaseStringHTML = releaseString;
					if (!StringUtil.blank(URL)) {
						releaseStringHTML +=" <a href=\"" + URL + "\">Home page</a><BR>";
						releaseString+=" The home page is: " + URL+ ". ";
					}
					if (!StringUtil.blank(downloadURL)) {
						releaseStringHTML +=" <a href=\"" + downloadURL + "\">Download page</a>";
						releaseString+=" The latest version is downloadable at: " + downloadURL+ ". ";
					}
				}

				if (MesquiteInteger.isCombinable(releaseVersionInt) && userVersionInt<releaseVersionInt) {
					if (phoneHomeRecord.getLastNewerVersionReported()<releaseVersionInt) { // we've not reported on this new version yet
						notices.append("\n" +releaseStringHTML);  // there is a newer version that has been released
						phoneHomeRecord.setLastNewerVersionReported(releaseVersionInt);
					}
					if (logBuffer!=null)
						logBuffer.append("\n" +releaseString);
				}

			}

// process other tags if they are there
			return notices.toString();
			
		} 
		return null;
	}
	


}
