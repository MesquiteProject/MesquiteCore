/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lib;

import java.util.*;

import org.dom4j.*;

public class PhoneHomeUtil {	

	static int OS = 0;
	static int OSVERSION=1;
	static int JAVAVERSION=2;
	public static boolean phoneHomeSuccessful = false;
	public static boolean suppressErrorReporting = false;

	//vvvvvvvvvvvvvvvvvvvv====INSTALL/UPDATE SYSTEM ====vvvvvvvvvvvvvvvvvvvv
	/*updating/install system.  See also INSTALL/UPDATE SYSTEM: below 
	 * Explanation of system is in mesquite.minimal.Installer.Installer.java and in NoticesAndInstallationExplanation.xml.
	 * Also see example XML files in mesquite.minimal.Installer*/
	public static Vector updateRecords = new Vector();
	public static Vector installedReceipts = new Vector();
	public static Vector installMenuItems = new Vector();
	public static ListableVector adHocRecord = null;
	static MesquiteSubmenuSpec installSubmenu;

	public static void refreshUpdateMenuItems(){  //will call resetAllMenuBars at end of phoning all homes
		for (int i = 0; i< installMenuItems.size(); i++){
			MesquiteMenuItemSpec mmis = (MesquiteMenuItemSpec)installMenuItems.elementAt(i);
			MesquiteTrunk.mesquiteTrunk.deleteMenuItem(mmis);
		}
		if (installSubmenu == null)
			installSubmenu = MesquiteTrunk.mesquiteTrunk.addSubmenu(MesquiteTrunk.fileMenu, "Available to Install or Update");
		installMenuItems.removeAllElements();
		for (int i = 0; i< updateRecords.size(); i++){
			ListableVector rec = (ListableVector)updateRecords.elementAt(i);
			String identity = ((MesquiteString)rec.getElement("identity")).getValue();
			String version = ((MesquiteString)rec.getElement("updateVersion")).getValue();
			if (!alreadyInReceipts(identity, version)){
				String name = ((MesquiteString)rec.getElement("packageName")).getValue();
				String uniqueID = ((MesquiteString)rec.getElement("uniqueID")).getValue();
				MesquiteCommand phlt = new MesquiteCommand("phoneHomeLinkTouched", ParseUtil.tokenize("install:" + uniqueID), PhoneHomeUtil.getLinkHandler());
				installMenuItems.addElement(MesquiteTrunk.mesquiteTrunk.addItemToSubmenu(MesquiteTrunk.fileMenu, installSubmenu, name, phlt));
			}
		}

	}
	public static boolean alreadyInReceipts(ListableVector v){
		String identity = ((MesquiteString)v.getElement("identity")).getValue();
		String version = ((MesquiteString)v.getElement("updateVersion")).getValue();
		return alreadyInReceipts(identity, version);
	}
	public static boolean alreadyInReceipts(String identity, String version){
		for (int i = 0; i< installedReceipts.size(); i++){
			ListableVector rec = (ListableVector)installedReceipts.elementAt(i);
			String idInstalled =  ((MesquiteString)rec.getElement("identity")).getValue();
			String versInstalled =  ((MesquiteString)rec.getElement("updateVersion")).getValue();
			if (idInstalled != null && idInstalled.equalsIgnoreCase(identity)){
				int iv = MesquiteInteger.fromString(version);
				int ivInstalled = MesquiteInteger.fromString(versInstalled);
				if (MesquiteInteger.isCombinable(ivInstalled) && MesquiteInteger.isCombinable(iv) && ivInstalled>= iv){
					return true;
				}
			}
		}

		return false;
	}
	public static ListableVector getUpdateRecord(String uniqueID){
		for (int i = 0; i<updateRecords.size(); i++){
			ListableVector vec = (ListableVector)updateRecords.elementAt(i);
			MesquiteString id = (MesquiteString)vec.getElement("uniqueID");
			if (uniqueID.equals(id.getValue()))
				return vec;

		}
		return null;
	}
	public static MesquiteCommand getPhoneHomeDialogLinkCommand(){
		return new MesquiteCommand("phoneHomeLinkTouched", getLinkHandler());
	}
	static MesquiteModule getLinkHandler(){
		MesquiteModule installer = MesquiteModule.mesquiteTrunk.findEmployeeWithName("#mesquite.minimal.Installer.Installer");
		return installer;
	}
	//^^^^^^^^^^^^^^^^====install/update system ====^^^^^^^^^^^^^^^^



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
		if (mmi == null)
			return 0;
		if (mmi.getIsPackageIntro())
			return mmi.getPackageVersionInt();
		else
			return mmi.getVersionInt();
	}
	/*.................................................................................................................*/
	public static void processSingleNotice(MesquiteModuleInfo mmi, StringBuffer notices, boolean hideFromDialog, MesquiteInteger countNotices, int forMesquiteVersionLessOrEqual, 
			int noticeNumber, String noticeType, String message, int lastVersionNoticed, int lastNoticeForMyVersion, int lastNotice, 
			PhoneHomeRecord phoneHomeRecord, Vector osVector, 
			int forBuildNumberEqualOrGreater, int forBuildNumberEqualOrLess, int forBuildNumberExactly, 
			int forPackageVersionEqualOrGreater, int forPackageVersionEqualOrLess, int forPackageVersionExactly, ListableVector v, boolean adHoc) {
		boolean pleaseDeleteFromUpdates = false;
		if (!MesquiteInteger.isCombinable(forMesquiteVersionLessOrEqual))
			forMesquiteVersionLessOrEqual = MesquiteInteger.infinite;
		if (MesquiteInteger.isCombinable(noticeNumber)){

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
			if (mmi == null || mmi.getName().equals("Mesquite") || mmi.getName().equals("Installer") || mmi.getName().equals("Defaults")){
				if (MesquiteInteger.isCombinable(forBuildNumberExactly)) 
					appliesToBuild =  appliesToBuild && (forBuildNumberExactly == MesquiteModule.getBuildNumber());
				if (MesquiteInteger.isCombinable(forBuildNumberEqualOrGreater)) 
					appliesToBuild =  appliesToBuild && (forBuildNumberEqualOrGreater <= MesquiteModule.getBuildNumber());
				if (MesquiteInteger.isCombinable(forBuildNumberEqualOrLess))  
					appliesToBuild =  appliesToBuild && (forBuildNumberEqualOrLess >= MesquiteModule.getBuildNumber());
			}
			else {
				if (MesquiteInteger.isCombinable(forBuildNumberExactly)) 
					appliesToBuild =  appliesToBuild && (forBuildNumberExactly == MesquiteModule.getBuildNumber());
				if (MesquiteInteger.isCombinable(forBuildNumberEqualOrGreater)) 
					appliesToBuild =  appliesToBuild && (forBuildNumberEqualOrGreater <= MesquiteModule.getBuildNumber());
				if (MesquiteInteger.isCombinable(forBuildNumberEqualOrLess))  
					appliesToBuild =  appliesToBuild && (forBuildNumberEqualOrLess >= MesquiteModule.getBuildNumber());

				if (MesquiteInteger.isCombinable(forPackageVersionExactly)) 
					appliesToBuild =  appliesToBuild && (forPackageVersionExactly == mmi.getVersionInt());
				if (MesquiteInteger.isCombinable(forPackageVersionEqualOrGreater)) 
					appliesToBuild =  appliesToBuild && (forPackageVersionEqualOrGreater <= mmi.getVersionInt());
				if (MesquiteInteger.isCombinable(forPackageVersionEqualOrLess))  
					appliesToBuild =  appliesToBuild && (forPackageVersionEqualOrLess >= mmi.getVersionInt());

			}

			//suppose Mesquite is version 2. 01
			int currentMesquiteVersion = getVersion(mmi);

			//vvvvvvvvvvvvvvvvvvvv====INSTALL/UPDATE SYSTEM ====vvvvvvvvvvvvvvvvvvvv
			boolean critical = false;
			boolean appearsToNeedInstallation = false;
			if (noticeType != null && noticeType.equalsIgnoreCase("update")){
				MesquiteString uniqueLocation = (MesquiteString)v.getElement("uniqueLocation");
				MesquiteString identity = (MesquiteString)v.getElement("identity");
				MesquiteString updateVersion = (MesquiteString)v.getElement("updateVersion");
				if (uniqueLocation != null)
					appearsToNeedInstallation = !MesquiteFile.fileOrDirectoryExists(MesquiteTrunk.getRootPath() + uniqueLocation.getValue());

				MesquiteString updateOnly = (MesquiteString)v.getElement("updateOnly");
				if (updateOnly != null && !appearsToNeedInstallation && "critical".equalsIgnoreCase(updateOnly.getValue()) && identity != null && alreadyInReceipts(identity.getValue(), "0") && !alreadyInReceipts(identity.getValue(), updateVersion.getValue()))  
					critical = true;
				else if (updateOnly != null && !"false".equalsIgnoreCase(updateOnly.getValue()) && appearsToNeedInstallation)
					pleaseDeleteFromUpdates = true;
			}
			//^^^^^^^^^^^^^^^^====install/update system ====^^^^^^^^^^^^^^^^

			//notice assumed to have been seen before if its version number is less than current
			boolean seenBefore = false;

			if (mmi == null || mmi.getName().equals("Mesquite") || mmi.getName().equals("Installer") || mmi.getName().equals("Defaults")){  //a Mesquite update
				seenBefore = seenBefore || !adHoc && (forMesquiteVersionLessOrEqual < currentMesquiteVersion);  //e.g., notice is version 2.0

				//or if Mesquite's version is same as notice's, but notice number is already seen for this version than last one noticed.
				seenBefore = seenBefore || (forMesquiteVersionLessOrEqual ==  currentMesquiteVersion && noticeNumber <= lastNoticeForMyVersion);  //e.g., notice is 2. 01; notice number has already been seen

				//or if Mesquite's version is less than notice's, and notice's is same as lastVersion noticed, but notice is already seen.
				seenBefore = seenBefore || (currentMesquiteVersion<forMesquiteVersionLessOrEqual && lastVersionNoticed == forMesquiteVersionLessOrEqual && noticeNumber <= lastNotice);  //e.g., notice is 2.02; 2.02 notices previously read; notice already seen

				//or if Mesquite's version is less than notice's, and notice's is less than as lastVersion noticed, but notice is already seen.
				seenBefore = seenBefore || (currentMesquiteVersion<forMesquiteVersionLessOrEqual && lastVersionNoticed> forMesquiteVersionLessOrEqual);  //e.g., notice is 2.02; 2.03 notices previously read
			}
			else {  //third party update
				seenBefore = seenBefore || (noticeNumber <= lastNoticeForMyVersion);  //e.g., notice is 2. 01; notice number has already been seen

				seenBefore = seenBefore || (noticeNumber <= lastNotice);  //e.g., notice is 2.02; 2.02 notices previously read; notice already seen
			}
			boolean javaInsufficient = false;
			boolean requirementsNotMet = false;
			if (v != null){
				MesquiteString java = (MesquiteString)v.getElement("java");
				MesquiteString requiredPath = (MesquiteString)v.getElement("requiredPath");
				if (java != null){
					double jV = MesquiteDouble.fromString(java.getValue(), new MesquiteInteger(0));
					if (MesquiteTrunk.isJavaVersionLessThan(jV))
						javaInsufficient = true;
				}
				if (requiredPath != null && !StringUtil.blank(requiredPath.getValue())){
					String requiredP = requiredPath.getValue();
					if (!MesquiteFile.fileOrDirectoryExists(MesquiteTrunk.getRootPath() + requiredP)){
						requirementsNotMet = true;
					}
				}
			}

			// otherwise assumed to have been seen before if version is same as current and notice is at or before recalled one
			if ((!seenBefore || critical) && appliesToOSVersion && appliesToBuild){  //relevant
				boolean skip = false;
				if (noticeType != null && noticeType.equalsIgnoreCase("alert")){
					//notices.append( countNotices.toString() + ". " + message + "<hr>\n");
					if (!hideFromDialog){
						notices.append(message + "<hr>\n");
						countNotices.increment();
				}
				}
				//vvvvvvvvvvvvvvvvvvvv====INSTALL/UPDATE SYSTEM ====vvvvvvvvvvvvvvvvvvvv
				else if (noticeType != null && noticeType.equalsIgnoreCase("update")){
					if (v != null && !hideFromDialog){
						MesquiteString java = (MesquiteString)v.getElement("java");
						MesquiteString requiredName = (MesquiteString)v.getElement("requires");
						MesquiteString requiredPath = (MesquiteString)v.getElement("requiredPath");
						MesquiteString updateOnly = (MesquiteString)v.getElement("updateOnly");
						MesquiteString updateVersion = (MesquiteString)v.getElement("updateVersion");
						MesquiteString identity = (MesquiteString)v.getElement("identity");
						if (java != null){
							double jV = MesquiteDouble.fromString(java.getValue(), new MesquiteInteger(0));
							if (MesquiteTrunk.isJavaVersionLessThan(jV)){
								javaInsufficient = true;
								if (appearsToNeedInstallation || (identity != null && !alreadyInReceipts(identity.getValue(), "0")))  //this package is not already installed in some form									
									skip = true;
							}
						}
						if (requiredPath != null && !StringUtil.blank(requiredPath.getValue())){
							String requiredP = requiredPath.getValue();
							if (!MesquiteFile.fileOrDirectoryExists(MesquiteTrunk.getRootPath() + requiredP)){
								requirementsNotMet = true;
							}
						}
						if (!javaInsufficient && updateOnly != null && !"false".equalsIgnoreCase(updateOnly.getValue())){  //update only tag is active
							if (appearsToNeedInstallation || (identity != null && !alreadyInReceipts(identity.getValue(), "0")))  //this package is not already installed in some form									
								skip = true;
						}
						if (!skip){
							MesquiteString packageName = (MesquiteString)v.getElement("packageName");
							MesquiteString explanation = (MesquiteString)v.getElement("explanation");
							MesquiteString uniqueID = (MesquiteString)v.getElement("uniqueID");
							if (requirementsNotMet && requiredName != null){
								notices.append("<h2>Available for installation: " + packageName + "</h2>");
								notices.append("HOWEVER, this package requires " + requiredName.getValue() + ", which apparently is not installed.  If you install this other package, you may install " + packageName + " later by selecting the item in the \"Available to Install or Update\" submenu of the File menu.<hr>\n");
								pleaseDeleteFromUpdates = true;
								countNotices.increment();
							}
							else if (javaInsufficient){
								notices.append("<h2>Available for installation: " + packageName + "</h2>");
								notices.append("HOWEVER, your version of Java is too old for this package.  You need Java version " + java.getValue() + ".  If you install a sufficiently recent version of java, you may install this package later by selecting the item in the \"Available to Install or Update\" submenu of the File menu.<hr>\n");
								pleaseDeleteFromUpdates = true;
								countNotices.increment();
							}
							else {
								if (critical)
									notices.append("<h2>CRITICAL UPDATE Available for installation: " + packageName + "</h2>");
								else
									notices.append("<h2>Available for installation: " + packageName + "</h2>");
								notices.append(explanation);
								String installHTML = null;
								if (!MesquiteFile.canWrite(MesquiteTrunk.getRootPath() + "mesquite")){
									installHTML = ("<p>HOWEVER, you cannot install this update because you do not have privileges to write into the Mesquite_Folder." +
									"&nbsp; Once this is resolved, you may install later by selecting the item in the \"Available to Install or Update\" submenu of the File menu.");
								}
								else {
									installHTML = "<p><a href = \"install:" + uniqueID + "\"><img border = 0 src =\"" + MesquiteFile.massageFilePathToURL(MesquiteTrunk.mesquiteTrunk.getRootPath()+"images" + MesquiteFile.fileSeparator + "download.gif") + "\"> Install</a>";
									if (!adHoc)
										installHTML += "&nbsp; If you do not install now, you may install later by selecting the item in the \"Available to Install or Update\" submenu of the File menu.";
								}

								notices.append(installHTML);
								if (!appearsToNeedInstallation) notices.append("<p><b>Note:</b> A version of this package may already be installed.\n");
								notices.append("<hr>\n");
								countNotices.increment();
							}
						}
					}
				}
				//^^^^^^^^^^^^^^^^====install/update system ====^^^^^^^^^^^^^^^^
				else {
					String fromWhom = null;
					if (mmi != null){
						if (mmi.getModuleClass() == mesquite.Mesquite.class)
							fromWhom = "Mesquite";
						else if (!StringUtil.blank(mmi.getPackageName()))
							fromWhom = mmi.getPackageName();
						else
							fromWhom = mmi.getName();
					}
					MesquiteMessage.println("\n\nNOTICE from " + fromWhom + ": " + message + "\n");
				}
				if (mmi == null || mmi.getName().equals("Mesquite") || mmi.getName().equals("Installer") || mmi.getName().equals("Defaults")){  //Mesquite update
					if (!skip && MesquiteInteger.isCombinable(forMesquiteVersionLessOrEqual)){
						if (forMesquiteVersionLessOrEqual ==  currentMesquiteVersion){  //version of note is this version of Mesquite
							if (phoneHomeRecord.getLastNoticeForMyVersion() < noticeNumber)  // this is a later notice than we had seen before; record it
								phoneHomeRecord.setLastNoticeForMyVersion(noticeNumber);
							if (forMesquiteVersionLessOrEqual ==  phoneHomeRecord.getLastVersionNoticed() && noticeNumber >phoneHomeRecord.getLastNotice())
								phoneHomeRecord.setLastNotice(noticeNumber);
						}
						if (forMesquiteVersionLessOrEqual >=  phoneHomeRecord.getLastVersionNoticed()){
							phoneHomeRecord.setLastVersionNoticed(forMesquiteVersionLessOrEqual);
							if (noticeNumber >phoneHomeRecord. getLastNotice())
								phoneHomeRecord.setLastNotice(noticeNumber);
						}
					}
				}
				else {  //third party update
					if (!skip){
						if (phoneHomeRecord.getLastNoticeForMyVersion() < noticeNumber)  // this is a later notice than we had seen before; record it
							phoneHomeRecord.setLastNoticeForMyVersion(noticeNumber);
						if (noticeNumber >phoneHomeRecord.getLastNotice())
							phoneHomeRecord.setLastNotice(noticeNumber);

					}
				}

			}
			if (javaInsufficient ||  requirementsNotMet || !appliesToOSVersion  || !appliesToBuild)
				pleaseDeleteFromUpdates = true;
		}


		if (pleaseDeleteFromUpdates)
			updateRecords.removeElement(v);

	}
	/*.................................................................................................................*/

	public static void checkForNotices(String URLString) {
		if (StringUtil.blank(URLString))
			return;
		String noticesFromHome = null;
		try{
			noticesFromHome = MesquiteFile.getURLContentsAsString(URLString, -1, false);

		} catch (Exception e) {
			MesquiteTrunk.mesquiteTrunk.discreetAlert("Sorry, no relevant information found at that URL");
			return;
		}
		if (StringUtil.blank(noticesFromHome)){
			MesquiteTrunk.mesquiteTrunk.discreetAlert("Sorry, no relevant information found at that URL");
			return;
		}
		PhoneHomeRecord phr = new PhoneHomeRecord("");
		String notices = handleMessages(true, noticesFromHome, null, phr, null);
		if (!StringUtil.blank(notices)){
			String note = ("<h2>Notices from " + StringUtil.protectForXML(URLString) + "</h2><hr><b>NOTE:</b> these notices may include some that have already been seen and dealt with.  When you ask specifically to check for notices, you are shown all whether seen previously or not.<hr>" + notices.toString() + "<br>");
			if (!MesquiteThread.isScripting()){
				AlertDialog.noticeHTML(MesquiteTrunk.mesquiteTrunk.containerOfModule(),"Note", note, 600, 400, new MesquiteCommand("phoneHomeLinkTouchedAdHoc", getLinkHandler()), true);
			}
			else
				System.out.println(note);
		}
		else
			MesquiteTrunk.mesquiteTrunk.discreetAlert("Sorry, relevant no information found at that URL");
	}
	/*.................................................................................................................*/

	public static String retrieveMessagesFromHome(MesquiteModuleInfo mmi, PhoneHomeRecord phoneHomeRecord, StringBuffer logBuffer) {
		String url = mmi.getHomePhoneNumber();
		if (StringUtil.blank(url))
			return null;
		String noticesFromHome = null;
		try{
			noticesFromHome = MesquiteFile.getURLContentsAsString(url, -1, false);
			if (MesquiteTrunk.debugMode)
				MesquiteMessage.warnProgrammer("Phone home to " + url + " successful ");

		} catch (Exception e) {
			if (MesquiteTrunk.debugMode)
				MesquiteMessage.warnProgrammer("Phone home to " + url + " UNSUCCESSFUL " + e.getCause());
			return null;
		}
		if (StringUtil.blank(noticesFromHome))
			return null;
		if (mmi.getModuleClass() == mesquite.Mesquite.class)
			phoneHomeSuccessful = true;
		return handleMessages(false, noticesFromHome, mmi, phoneHomeRecord, logBuffer);
	}
	/*.................................................................................................................*/

	private static String handleMessages(boolean adHoc, String noticesFromHome, MesquiteModuleInfo mmi, PhoneHomeRecord phoneHomeRecord, StringBuffer logBuffer) {
		/*	String url = mmi.getHomePhoneNumber();
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
		if (mmi.getModuleClass() == mesquite.Mesquite.class)
			phoneHomeSuccessful = true;
		 */

		int lastNoticeForMyVersion = phoneHomeRecord.getLastNoticeForMyVersion();
		int lastNotice = phoneHomeRecord.getLastNotice();
		int lastVersionNoticed = phoneHomeRecord.getLastVersionNoticed();


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

			//let's get the notices
			List noticesFromHomeList = messagesFromHome.elements("notice");
			for (Iterator iter = noticesFromHomeList.iterator(); iter.hasNext();) {   // this is going through all of the notices
				Element messageElement = (Element) iter.next();
				int forMesquiteVersionLessOrEqual = MesquiteInteger.fromString(messageElement.elementText("forMesquiteVersionLessOrEqual"));    // notice is for this version and any previous version			
				if (!MesquiteInteger.isCombinable(forMesquiteVersionLessOrEqual))
					forMesquiteVersionLessOrEqual = MesquiteInteger.fromString(messageElement.elementText("forVersion"));    // old name
				//NOTE: for adhoc requests forMesquiteVersionLessOrEqual is not used

				int forPackageVersionExactly = MesquiteInteger.fromString(messageElement.elementText("forPackageVersionExactly"));    // notice is for this version and any previous version
				int forPackageVersionEqualOrGreater = MesquiteInteger.fromString(messageElement.elementText("forPackageVersionEqualOrGreater"));    // notice is for this version and any previous version
				int forPackageVersionEqualOrLess = MesquiteInteger.fromString(messageElement.elementText("forPackageVersionEqualOrLess"));    // notice is for this version and any previous version
				int forBuildNumberExactly = MesquiteInteger.fromString(messageElement.elementText("forBuildNumberExactly"));
				int forBuildNumberEqualOrGreater = MesquiteInteger.fromString(messageElement.elementText("forBuildNumberEqualOrGreater"));
				int forBuildNumberEqualOrLess = MesquiteInteger.fromString(messageElement.elementText("forBuildNumberEqualOrLess"));
				int noticeNumber = MesquiteInteger.fromString(messageElement.elementText("noticeNumber"));
				String hideFromDialogString = messageElement.elementText("hideFromDialog");
				boolean hideFromDialog = false;
				if (hideFromDialogString != null && hideFromDialogString.equalsIgnoreCase("true"))
					hideFromDialog = true;
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
				//INSTALLER: recording update record for later use in dialog and in menu items.
				//vvvvvvvvvvvvvvvvvvvv====INSTALL/UPDATE SYSTEM ====vvvvvvvvvvvvvvvvvvvv
				ListableVector v = null;
				if (messageType.equalsIgnoreCase("update")){
					v = new ListableVector();
					String packageName = messageElement.elementText("packageName");
					String versionNum = messageElement.elementText("updateVersion");
					String uniqueLocation = messageElement.elementText("uniqueLocation");
					String explanation = messageElement.elementText("explanation");
					String updateOnly = messageElement.elementText("updateOnly");
					String java = messageElement.elementText("java");
					String requires = messageElement.elementText("requires");
					String requiredPath = messageElement.elementText("requiredPath");
					String beforeMessage = messageElement.elementText("beforeMessage");
					String afterMessage = messageElement.elementText("afterMessage");
					String uniqueID = MesquiteTrunk.getUniqueIDBase() + updateRecords.size();
					v.addElement(new MesquiteString("uniqueID", uniqueID), false);
					v.addElement(new MesquiteString("identity", messageElement.elementText("identity")), false);
					v.addElement(new MesquiteString("packageName", packageName), false);
					v.addElement(new MesquiteString("explanation", explanation), false);
					if (updateOnly != null)
						v.addElement(new MesquiteString("updateOnly", updateOnly), false);
					if (requiredPath != null)
						v.addElement(new MesquiteString("requiredPath", requiredPath), false);
					if (requires != null)
						v.addElement(new MesquiteString("requires", requires), false);
					if (beforeMessage != null)
						v.addElement(new MesquiteString("beforeMessage", beforeMessage), false);
					if (afterMessage != null)
						v.addElement(new MesquiteString("afterMessage", afterMessage), false);
					v.addElement(new MesquiteString("updateVersion", versionNum), false);
					v.addElement(new MesquiteString("uniqueLocation", uniqueLocation), false);
					v.addElement(new ObjectContainer("install", messageElement.elements("install")), false);
					v.addElement(new MesquiteBoolean("isInstalled", false), false);
					if (java != null)
						v.addElement(new MesquiteString("java", java), false);
					String ux = XMLUtil.getElementAsXMLString(messageElement, "UTF-8", true) ;

					if (ux != null){
						ux = "<updateXML>" + ux + "</updateXML>";  //strip for more compact file when viewed
						ux = StringUtil.replace(ux, "\n", null);
						ux = StringUtil.replace(ux, "\r", null);
						ux = StringUtil.replace(ux, "\t", null);
						ux = StringUtil.stripStuttered(ux, "  ");
						v.addElement(new MesquiteString("updateXML", ux), false);
					}
					if (!adHoc)
						updateRecords.addElement(v);  
					else  
						adHocRecord = v;
				}
				//^^^^^^^^^^^^^^^^====install/update system ====^^^^^^^^^^^^^^^^

				// process other notice tags here if they are present
				processSingleNotice(mmi, notices, hideFromDialog, countNotices, forMesquiteVersionLessOrEqual, noticeNumber, messageType, message,  lastVersionNoticed, lastNoticeForMyVersion,  lastNotice,phoneHomeRecord, osVector, forBuildNumberEqualOrGreater, forBuildNumberEqualOrLess, forBuildNumberExactly, forPackageVersionEqualOrGreater, forPackageVersionEqualOrLess, forPackageVersionExactly, v, adHoc);

			}
			//INSTALLER: here go through updateRecords to figure out which are already installed, which not; which have newer versions already installed, etc.
			refreshUpdateMenuItems();

			// now see if there is a tag for the current release version
			Element currentReleaseVersion = messagesFromHome.element("currentReleaseVersion");
			if (mmi != null && currentReleaseVersion !=null) {
				String releaseString = "";
				String releaseStringHTML ="";
				String versionString = currentReleaseVersion.elementText("versionString");
				String versionStringInstalled = null;
				if (mmi.getIsPackageIntro() && !StringUtil.blank(mmi.getPackageVersion()))
					versionStringInstalled =  mmi.getPackageVersion();

				else if (!StringUtil.blank(mmi.getVersion()))
					versionStringInstalled = mmi.getVersion();
				String buildString = currentReleaseVersion.elementText("build");
				boolean skip = false;

				if (versionStringInstalled != null && versionString != null && versionStringInstalled.equals(versionString)){  //same version
					if (mmi.getModuleClass() != mesquite.Mesquite.class || buildString == null ||  buildString.equals(Integer.toString(MesquiteModule.getBuildNumber())))
						skip = true; //skip unless Mesquite and different build
				}
				if (!skip){
					String URL = currentReleaseVersion.elementText("URL");
					String downloadURL = currentReleaseVersion.elementText("downloadURL");
					int releaseVersionInt = MesquiteInteger.fromString(currentReleaseVersion.elementText("version"));
					int userVersionInt = getVersion(mmi);
					String fromWhom = null;
					if (mmi.getModuleClass() == mesquite.Mesquite.class)
						fromWhom = "Mesquite";
					else if (!StringUtil.blank(mmi.getPackageName()))
						fromWhom = mmi.getPackageName();
					else
						fromWhom = mmi.getName();
					if (!StringUtil.blank(versionString))
						releaseString+="The current release version of " + fromWhom + " is " + versionString;
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
							releaseStringHTML +="&nbsp;<a href=\"" + downloadURL + "\">Download page</a>.   You may also find an option to install this using Mesquite's automatic installation system (look in File menu under Available to Install or Update).";
							releaseString+=" The latest version is downloadable at: " + downloadURL+ ".   You may also be able to install this using Mesquite's automatic installation system (look in File menu under Available to Install or Update).";
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

			}

			// process other tags if they are there
			return notices.toString();

		} 
		return null;
	}

}
